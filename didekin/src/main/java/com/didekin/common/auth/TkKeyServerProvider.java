package com.didekin.common.auth;

import com.didekin.common.repository.ServiceException;

import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.ECParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.didekin.common.auth.TkCommonConfig.aliasKey;
import static com.didekin.common.auth.TkCommonConfig.default_alg_for_symmetric_keys;
import static com.didekin.common.auth.TkCommonConfig.default_key_size;
import static com.didekin.common.auth.TkCommonConfig.storePswd;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static javax.crypto.KeyGenerator.getInstance;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * User: pedro@didekin
 * Date: 14/05/2018
 * Time: 11:18
 * <p>
 * TkKeyServerProviderIf implementation based on a keyStore as key repository.
 */
public class TkKeyServerProvider implements TkKeyServerProviderIf {

    private static final Logger logger = getLogger(TkKeyServerProvider.class.getCanonicalName());
    private final KeyStore keyStore;

    @Autowired
    public TkKeyServerProvider(KeyStore keyStoreIn)
    {
        keyStore = keyStoreIn;
    }

    @Override
    public SecretKey getCurrentKeyForTk()
    {
        try {
            return (SecretKey) keyStore.getKey(aliasKey, storePswd.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            logger.error(e.getMessage());
            throw new ServiceException(TOKEN_ENCRYP_DECRYP_ERROR);
        }
    }

    KeyStore getKeyStore()
    {
        return keyStore;
    }

    /**
     * Default configuration: keySize is 32 octets long for AES_128_CBC_HMAC_SHA_256; algorithm: "AES".
     */
    @Override
    public SecretKey getNewSymmetricKey() throws NoSuchAlgorithmException
    {
        return getNewSymmetricKey(default_alg_for_symmetric_keys, default_key_size);
    }

    @Override
    public SecretKey getNewSymmetricKey(String algorithm, int keySizeInBits) throws NoSuchAlgorithmException
    {
        KeyGenerator keyGenerator = getInstance(algorithm);
        keyGenerator.init(keySizeInBits);
        return keyGenerator.generateKey();
    }

    @Override
    public KeyPair getNewPkiKeys(String algorithmStr, int sizeKey) throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithmStr);
        keyGen.initialize(sizeKey);
        return keyGen.generateKeyPair();
    }

    @Override
    public KeyPair getNewEcKeys() throws NoSuchAlgorithmException
    {
        return getNewPkiKeys("EC", default_key_size);
    }

    @Override
    public KeyPair getNewEcKeys(ECParameterSpec parameterSpec) throws JoseException
    {
        EllipticCurveJsonWebKey eCKey = EcJwkGenerator.generateJwk(parameterSpec);
        return new KeyPair(eCKey.getECPublicKey(), eCKey.getEcPrivateKey());
    }
}
