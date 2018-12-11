package com.didekin.common.sign;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

import static com.didekinlib.crypto.EcSignatureConfig.alg_for_signing;
import static com.didekinlib.crypto.EcSignatureConfig.alg_for_signing_keys;
import static com.didekinlib.crypto.EcSignatureConfig.ec_public_key_format;
import static com.didekinlib.crypto.EcSignatureConfig.signing_key_size;
import static java.security.KeyPairGenerator.getInstance;
import static java.util.Base64.getDecoder;
import static java.util.Objects.requireNonNull;

/**
 * User: pedro@didekin
 * Date: 10/12/2018
 * Time: 12:16
 * <p>
 * secp256r1 [NIST P-256, X9.62 prime256v1]
 */
public class EcSignVerifier {

    private final EcSignPublicKey publicKey;
    private final String msgToVerify;
    /**
     * Base64 encoded string.
     */
    private final String signedMsg;


    public EcSignVerifier(String msgToVerify, String signedMsg, EcSignPublicKey publicKey)
    {
        this.msgToVerify = msgToVerify;
        this.signedMsg = signedMsg;
        this.publicKey = publicKey;
    }

    boolean verifySignedMsg() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        Signature signature = Signature.getInstance(alg_for_signing);
        signature.initVerify(publicKey);
        signature.update(msgToVerify.getBytes());
        return signature.verify(getDecoder().decode(signedMsg.getBytes()));
    }

    static class EcSignPublicKey implements ECPublicKey {

        private final BigInteger ecX;
        private final BigInteger ecY;
        static final ECParameterSpec parameterSpec = getParamsFromPkey();

        static ECParameterSpec getParamsFromPkey()
        {
            KeyPairGenerator keyGen;
            try {
                keyGen = getInstance(alg_for_signing_keys);
                requireNonNull(keyGen).initialize(signing_key_size);
                KeyPair pair = keyGen.generateKeyPair();
                return ((ECPublicKey) pair.getPublic()).getParams();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        EcSignPublicKey(BigInteger ecX, BigInteger ecY)
        {
            this.ecX = ecX;
            this.ecY = ecY;
        }

        @Override
        public ECPoint getW()
        {
            return new ECPoint(ecX, ecY);
        }

        @Override
        public String getAlgorithm()
        {
            return alg_for_signing_keys;
        }

        @Override
        public String getFormat()
        {
            return ec_public_key_format;
        }

        @Override
        public byte[] getEncoded()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ECParameterSpec getParams()
        {
            return parameterSpec;
        }
    }
}
