package com.didekin.auth;

import com.didekin.auth.api.TkKeyServerProviderIf;
import com.didekinlib.http.usuario.TkParamNames;
import com.didekinlib.model.common.dominio.BeanBuilder;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

import static com.didekin.auth.TkAuthClaims.getDefaultClaim;
import static com.didekin.auth.TkHeaders.getDefaultHeader;
import static com.didekinlib.http.usuario.TkParamNames.algorithm_ce;
import static com.didekinlib.http.usuario.TkParamNames.algorithm_cek;
import static com.didekinlib.http.usuario.TkParamNames.audience;
import static org.jose4j.jwa.AlgorithmConstraints.ConstraintType.WHITELIST;

/**
 * User: pedro@didekin
 * Date: 11/05/2018
 * Time: 17:11
 */
public final class EncryptedTkConsumer {

    private final JwtClaims claims;

    private EncryptedTkConsumer(JwtClaims builderClaims)
    {
        claims = builderClaims;
    }

    JwtClaims getClaims()
    {
        return claims;
    }

    // ..............................  Builder  ...............................

    @Component
    public static class EncrypTkConsumerBuilder implements BeanBuilder<EncryptedTkConsumer> {

        private final JwtConsumerBuilder builder;
        private String encryptedTkStr;
        private final Key decryptionKey;
        private List<String> audiences;
        private String issuer;
        private String keyManagementAlg;
        private String contentEncryptionAlg;

        @Autowired
        EncrypTkConsumerBuilder(TkKeyServerProviderIf keyProvider)
        {
            builder = new JwtConsumerBuilder();
            // A signature on the JWT is required by default.
            builder.setDisableRequireSignature();
            // Require that the JWT be encrypted, which is not required by default.
            builder.setEnableRequireEncryption();
            // Require that the JWT contain an expiration time ("exp") claim.
            builder.setRequireExpirationTime();
            // Require that a subject ("sub") claim be present in the JWT.
            builder.setRequireSubject();
            decryptionKey = keyProvider.getCurrentKeyForTk();
            builder.setDecryptionKey(decryptionKey);
        }

        @SuppressWarnings("unchecked")
        EncrypTkConsumerBuilder defaultInit(String jsonWebTokenStr)
        {
            if (audiences != null || issuer != null || keyManagementAlg != null || contentEncryptionAlg != null) {
                throw new IllegalStateException(error_message_bean_building + this.getClass().getName());
            }

            tokenToConsume(jsonWebTokenStr)
                    .expectAudience((List<String>) getDefaultClaim(audience))
                    .expectedIssuer((String) getDefaultClaim(TkParamNames.issuer))
                    .keyManagementAlg(getDefaultHeader(algorithm_cek))
                    .contentEncryptAlg(getDefaultHeader(algorithm_ce));
            return this;
        }

        EncrypTkConsumerBuilder tokenToConsume(String jsonWebTokenStr)
        {
            encryptedTkStr = jsonWebTokenStr;
            return this;
        }

        /**
         * Set the audience value(s) to use when validating the audience ("aud") claim of a JWT
         * and require that an audience claim be present.
         */
        EncrypTkConsumerBuilder expectAudience(List<String> audiencesIn)
        {
            if (audiencesIn != null) {
                audiences = audiencesIn;
                builder.setExpectedAudience(audiences.toArray(new String[]{}));
            }
            return this;
        }

        /**
         * Indicates the expected value of the issuer ("iss") claim and that the claim is required.
         */
        EncrypTkConsumerBuilder expectedIssuer(String issuerIn)
        {
            if (issuerIn != null) {
                issuer = issuerIn;
                builder.setExpectedIssuer(issuer);
            }
            return this;
        }

        /**
         * Set the JWE algorithm constraints to be applied to key management when processing the JWT.
         */
        EncrypTkConsumerBuilder keyManagementAlg(String keyAlgIn)
        {
            if (keyAlgIn != null) {
                keyManagementAlg = keyAlgIn;
                builder.setJweAlgorithmConstraints(new AlgorithmConstraints(WHITELIST, keyAlgIn));
            }
            return this;
        }

        /**
         * Set the JWE algorithm constraints to be applied to content encryption when processing the JWT.
         */
        EncrypTkConsumerBuilder contentEncryptAlg(String contentEncAlgIn)
        {
            if (contentEncAlgIn != null) {
                contentEncryptionAlg = contentEncAlgIn;
                builder.setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(WHITELIST, contentEncAlgIn));
            }
            return this;
        }

        @Override
        public EncryptedTkConsumer build()
        {
            try {
                if (encryptedTkStr == null || decryptionKey == null || audiences == null
                        || issuer == null || keyManagementAlg == null || contentEncryptionAlg == null) {
                    throw new IllegalStateException(error_message_bean_building + this.getClass().getName());
                }
                return new EncryptedTkConsumer(builder.build().processToClaims(encryptedTkStr));
            } catch (InvalidJwtException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }
}
