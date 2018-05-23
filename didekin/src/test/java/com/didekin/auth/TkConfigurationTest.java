package com.didekin.auth;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.LocalDev;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.KeyStore;
import java.security.KeyStoreException;

import static com.didekin.auth.TkConfiguration.PKCS12_keystore_type;
import static com.didekin.auth.TkConfiguration.aliasKey;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * User: pedro@didekin
 * Date: 17/05/2018
 * Time: 19:39
 */
public abstract class TkConfigurationTest {

    @Autowired
    private KeyStore keyStore;
    @Autowired
    private AuthInterceptor interceptor;

    @Test
    public void test_KeyStore() throws KeyStoreException
    {
        assertThat(keyStore.getType(), is(PKCS12_keystore_type));
        assertThat(keyStore.size(), is(1));
        assertThat(keyStore.containsAlias(aliasKey), is(true));
    }

    @Test
    public void test_AuthInterceptor()
    {
        assertThat(interceptor.getBuilder(), notNullValue());
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class}, webEnvironment = DEFINED_PORT)
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL})
    public static class TkConfigurationDevTest extends TkConfigurationTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class}, webEnvironment = DEFINED_PORT)
    @Category({AwsPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE})
    public static class TkConfigurationAwsTest extends TkConfigurationTest {
    }
}