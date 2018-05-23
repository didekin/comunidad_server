package com.didekin.auth;

import com.didekin.auth.api.EncryptedTkProducerIf;
import com.didekin.auth.api.TkKeyServerProviderIf;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.http.usuario.TkParamNames;
import com.didekinlib.model.common.dominio.BeanBuilder;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Map;

import static com.didekin.auth.TkAuthClaims.doDefaultAuthClaims;
import static com.didekin.auth.TkHeaders.doHeadersSymmetricKey;
import static com.didekinlib.http.usuario.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: pedro@didekin
 * Date: 09/05/2018
 * Time: 14:07
 */
public final class EncryptedTkProducer implements EncryptedTkProducerIf {

    private static final Logger logger = getLogger(EncryptedTkProducer.class.getCanonicalName());
    private final String encryptedTkStr;

    private EncryptedTkProducer(String encryptedTkFromBuilder)
    {
        encryptedTkStr = encryptedTkFromBuilder;
    }

    /**
     * @return string with pattern : <header>.<encrypted key>.<initialization vector>.<ciphertext>.<authentication tag>,
     * where <encrypted key> is in the case of encryption with a DIRECT generated key.
     */
    @Override
    public String getEncryptedTkStr()
    {
        return encryptedTkStr;
    }

    // ..............................  Builder  ...............................

    @Component
    public static class JweTkProducerBuilder implements BeanBuilder<EncryptedTkProducer> {

        private TkAuthClaims claims;
        private TkHeaders headers;
        private Key key;

        @Autowired
        public JweTkProducerBuilder(TkKeyServerProviderIf keyProviderIn)
        {
            key = keyProviderIn.getCurrentKeyForTk();
        }

        JweTkProducerBuilder defaultHeadersClaims(String userName, String appId)
        {
            // Check for previous initialization.
            if (claims != null || headers != null) {
                throw new IllegalStateException(error_message_bean_building + this.getClass().getName());
            }
            claims = doDefaultAuthClaims(userName, appId);
            headers = doHeadersSymmetricKey();
            return this;
        }

        JweTkProducerBuilder claims(Map<TkParamNames, ?> claimsIn)
        {
            claims = TkAuthClaims.doDefaultAuthClaims(claimsIn);
            return this;
        }

        JweTkProducerBuilder headers(TkHeaders headersIn)
        {
            headers = headersIn;
            return this;
        }

        @Override
        public EncryptedTkProducer build()
        {
            if (headers == null || claims == null || key == null) {
                throw new IllegalStateException(error_message_bean_building + this.getClass().getName());
            }

            JsonWebEncryption encryption = new JsonWebEncryption();
            headers.putHeadersIn(encryption);
            encryption.setPayload(claims.getJwtClaimsFromMap().toJson());
            encryption.setKey(key);
            String encryptedTkStr;
            try {
                encryptedTkStr = encryption.getCompactSerialization();
                if (!tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(encryptedTkStr)) {
                    throw new IllegalStateException(error_message_bean_building + this.getClass().getName());
                }
            } catch (JoseException e) {
                logger.error(e.getMessage());
                throw new ServiceException(TOKEN_ENCRYP_DECRYP_ERROR);
            }
            return new EncryptedTkProducer(encryptedTkStr);
        }

        TkAuthClaims getClaims()
        {
            return claims;
        }

        TkHeaders getHeaders()
        {
            return headers;
        }
    }
}
