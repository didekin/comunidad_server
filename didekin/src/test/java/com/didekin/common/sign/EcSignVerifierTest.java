package com.didekin.common.sign;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

import static com.didekinlib.crypto.EcSignatureConfig.alg_for_signing;
import static com.didekinlib.crypto.EcSignatureConfig.alg_for_signing_keys;
import static com.didekinlib.crypto.EcSignatureConfig.ec_curve_name;
import static com.didekinlib.crypto.EcSignatureConfig.ec_public_key_format;
import static com.didekinlib.crypto.EcSignatureConfig.signing_key_size;
import static java.security.KeyPairGenerator.getInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 10/12/2018
 * Time: 13:48
 */
public class EcSignVerifierTest {

    private static final String msgToSign = "Hola Pedro";
    private Signature signature;
    private String sigBase64;
    private ECPublicKey ecPublicKey;

    @Before
    public void setUp() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        KeyPairGenerator keyGen = getInstance(alg_for_signing_keys);
        keyGen.initialize(signing_key_size);
        KeyPair pair = keyGen.generateKeyPair();

        signature = Signature.getInstance(alg_for_signing);
        ECPrivateKey priv = (ECPrivateKey) pair.getPrivate();
        signature.initSign(priv);
        signature.update(msgToSign.getBytes());
        byte[] sig = signature.sign();
        // Encode signed message.
        sigBase64 = Base64.getEncoder().encodeToString(sig);
        ecPublicKey = (ECPublicKey) pair.getPublic();
    }

    @Test
    public void test_VerifySignedMsg1() throws Exception
    {
        // Simple case.
        signature.initVerify(ecPublicKey);
        signature.update(msgToSign.getBytes());
        byte[] decodedSig = Base64.getDecoder().decode(sigBase64);
        boolean verifies = signature.verify(decodedSig);
        assertThat(verifies, is(true));

        // Verify with home made key.
        ECPublicKey myKey = new EcSignVerifier.EcSignPublicKey(ecPublicKey.getW().getAffineX(), ecPublicKey.getW().getAffineY());
        signature.initVerify(myKey);
        signature.update(msgToSign.getBytes());
        verifies = signature.verify(decodedSig);
        assertThat(verifies, is(true));
    }

    @Test
    public void test_VerifySignedMsg2() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        // Simulate the building of a public key from a couple of strings.
        String ecXStr = ecPublicKey.getW().getAffineX().toString(10);
        assertThat(ecXStr.length() < 100, is(true));
        String ecYStr = ecPublicKey.getW().getAffineY().toString(10);
        assertThat(ecYStr.length() < 100, is(true));
        EcSignVerifier.EcSignPublicKey publicKey = new EcSignVerifier.EcSignPublicKey(new BigInteger(ecXStr), new BigInteger(ecYStr));
        assertThat(new EcSignVerifier(msgToSign, sigBase64, publicKey).verifySignedMsg(), is(true));
    }

    @Test
    public void test_Config_inLocal()  // To test local configuration.
    {
        assertThat(ecPublicKey.getAlgorithm(), is(alg_for_signing_keys));
        assertThat(ecPublicKey.getFormat(), is(ec_public_key_format));
        assertThat(ecPublicKey.getParams().getCurve().getField().getFieldSize(), is(signing_key_size));
        assertThat(ecPublicKey.toString().contains(ec_curve_name), is(true));
    }
}