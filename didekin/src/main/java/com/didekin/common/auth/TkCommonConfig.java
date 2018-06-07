package com.didekin.common.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.getenv;

/**
 * User: pedro@didekin
 * Date: 16/05/2018
 * Time: 14:34
 */
@Configuration
public class TkCommonConfig {

    static final String default_alg_for_symmetric_keys = "AES";
    static final int default_key_size = 256;
    static final String storePswd = getenv("TOKEN_KEYSTORE_PSWD");
    static final String PKCS12_keystore_type = "PKCS12";
    static final String aliasKey = getenv("TOKEN_KEY_ALIAS");
    private static final String keystore_path = "/didekin_web_sym.pkcs12";
    // Singleton
    private static final AtomicReference<KeyStore> encryptKeyStore = new AtomicReference<>();

    @Bean
    public KeyStore keyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
    {
        KeyStore keyStore = KeyStore.getInstance(PKCS12_keystore_type);
        keyStore.load(getClass().getResourceAsStream(keystore_path), storePswd.toCharArray());
        encryptKeyStore.compareAndSet(null, keyStore);
        return encryptKeyStore.get();
    }

    @Bean
    public TkKeyServerProvider tkKeyServerProvider(KeyStore keyStoreIn)
    {
        return new TkKeyServerProvider(keyStoreIn);
    }

    @Bean
    public EncrypTkConsumerBuilder encrypTkConsumerBuilder(TkKeyServerProviderIf keyProvider)
    {
        return new EncrypTkConsumerBuilder(keyProvider);
    }
}
