package com.didekin.userservice.auth;

import com.didekin.common.auth.TkAuthClaims;
import com.didekin.common.auth.TkHeaders;
import com.didekin.common.auth.TkKeyServerProviderIf;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.BeanBuilder;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.security.Key;
import java.util.Map;

import static com.didekin.common.auth.TkAuthClaims.doClaimsFromMap;
import static com.didekin.common.auth.TkAuthClaims.doDefaultAuthClaims;
import static com.didekin.common.auth.TkHeaders.doHeadersSymmetricKey;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: pedro@didekin
 * Date: 25/05/2018
 * Time: 12:52
 */
public final class EncrypTkProducerBuilder implements BeanBuilder<EncrypTkProducerBuilder.EncryptedTkProducer> {

    private TkAuthClaims claims;
    private TkHeaders headers;
    private Key key;

    private static final Logger logger = getLogger(EncrypTkProducerBuilder.class.getCanonicalName());

    @Autowired
    public EncrypTkProducerBuilder(TkKeyServerProviderIf keyProviderIn)
    {
        key = keyProviderIn.getCurrentKeyForTk();
    }

    public EncrypTkProducerBuilder defaultHeadersClaims(String userName)
    {
        claims = TkAuthClaims.doDefaultAuthClaimsFromUserName(userName);
        headers = doHeadersSymmetricKey();
        return this;
    }

    EncrypTkProducerBuilder defaultClaims(Map<TkParamNames, ?> claimsIn)
    {
        claims = doDefaultAuthClaims(claimsIn);
        return this;
    }

    EncrypTkProducerBuilder headers(TkHeaders headersIn)
    {
        headers = headersIn;
        return this;
    }

    @Profile(value = {NGINX_JETTY_LOCAL, NGINX_JETTY_PRE})
    EncrypTkProducerBuilder claims(Map<TkParamNames, ?> initClaimsIn)
    {
        claims = doClaimsFromMap(initClaimsIn);
        return this;
    }

    @Override
    public EncryptedTkProducer build()
    {
        logger.debug("build()");

        if (headers == null || claims == null || key == null) {
            logger.error(error_message_bean_building
                    + " headers = " + headers
                    + " claims = " + claims
                    + " key = " + key);
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

    /**
     * User: pedro@didekin
     * Date: 09/05/2018
     * Time: 14:07
     */
    public static final class EncryptedTkProducer implements EncryptedTkProducerIf {

        private final String encryptedTkStr;

        private EncryptedTkProducer(String encryptedTkFromBuilder)
        {
            encryptedTkStr = encryptedTkFromBuilder;
        }

        /**
         * @return string with pattern : <header>.<encrypted key>.<initialization vector>.<ciphertext>.<authentication tag>,
         * where <encrypted key> is in the case of encryption with a DIRECT generated key.
         * Example:
         *  "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0
         *  .
         *  ._L86WbOFHY-3g0E2EXejJg
         *  .UB1tHZZq0TYFTZKPVZXY83GRxHz770Aq7BuMCEbNnaSC5cVNOLEOgBQrOQVJmVL-9Ke9KRSwuq7MmVcA2EB_0xRBr_YbzmMWbpUcTQUFtE5OZOFiCsxL5Yn0gA_DDLZboivpoSqndQRP-44mWVkM1A
         *  .RIvTWRrsyoJ1mpl8vUhQDQ"
         */
        @Override
        public String getEncryptedTkStr()
        {
            return encryptedTkStr;
        }
    }
}
