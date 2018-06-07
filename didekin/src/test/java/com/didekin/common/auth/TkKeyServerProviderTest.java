package com.didekin.common.auth;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.LocalDev;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import static com.didekin.common.auth.TkCommonConfig.PKCS12_keystore_type;
import static com.didekin.common.auth.TkCommonConfig.default_alg_for_symmetric_keys;
import static com.didekin.common.auth.TkCommonConfig.default_key_size;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.jose4j.keys.EllipticCurves.P256;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 14/05/2018
 * Time: 15:45
 */
public abstract class TkKeyServerProviderTest {

    @Autowired
    private TkKeyServerProvider provider;

    @Test
    public void test_KeyStore()
    {
        assertThat(provider.getKeyStore().getType(), is(PKCS12_keystore_type));
    }

    @Test
    public void test_GetCurrentKeyForTk()
    {
        SecretKey symmetricKey = provider.getCurrentKeyForTk();
        assertThat(symmetricKey.getAlgorithm(), is(default_alg_for_symmetric_keys));
        assertThat(symmetricKey.getEncoded().length, is(default_key_size / 8));  // length is in bytes.
    }

    @Test
    public void test_GetNewSymmetricKey_1() throws NoSuchAlgorithmException
    {
        Key symmetricKey = provider.getNewSymmetricKey();
        assertThat(symmetricKey.getEncoded().length, is(default_key_size / 8)); // length is in bytes.
        assertThat(symmetricKey.getAlgorithm(), is(default_alg_for_symmetric_keys));
    }

    @Test
    public void test_GetNewSymmetricKey_2() throws NoSuchAlgorithmException
    {
        Key symmetricKey = provider.getNewSymmetricKey(default_alg_for_symmetric_keys, 128);
        assertThat(symmetricKey.getEncoded().length, is(128 / 8));
        assertThat(symmetricKey.getAlgorithm(), is(default_alg_for_symmetric_keys));
    }

    @Test
    public void test_GetNewPkiKeys() throws Exception
    {
        KeyPair keyPair = provider.getNewPkiKeys("EC", 256);
        assertThat(keyPair.getPublic(), notNullValue());
        assertThat(keyPair.getPrivate(), notNullValue());

        KeyPair keyPair2 = provider.getNewEcKeys();
        assertThat(keyPair.getPublic().getAlgorithm(), is(keyPair2.getPublic().getAlgorithm()));
        assertThat(keyPair.getPublic().getFormat(), is(keyPair2.getPublic().getFormat()));
        assertThat(keyPair.getPublic().getEncoded().length, is(keyPair2.getPublic().getEncoded().length));
        assertThat(keyPair.getPrivate().getAlgorithm(), is(keyPair2.getPrivate().getAlgorithm()));
        assertThat(keyPair.getPrivate().getFormat(), is(keyPair2.getPrivate().getFormat()));
        assertThat(keyPair.getPrivate().getEncoded().length, is(keyPair2.getPrivate().getEncoded().length));

        KeyPair keyPair3 = provider.getNewEcKeys(P256);
        assertThat(keyPair.getPublic().getAlgorithm(), is(keyPair3.getPublic().getAlgorithm()));
        assertThat(keyPair.getPrivate().getAlgorithm(), is(keyPair3.getPrivate().getAlgorithm()));
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class})
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL})
    public static class TkKeyServerProviderDevTest extends TkKeyServerProviderTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class})
    @Category({AwsPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE})
    public static class TkKeyServerProviderAwsTest extends TkKeyServerProviderTest {
    }
}