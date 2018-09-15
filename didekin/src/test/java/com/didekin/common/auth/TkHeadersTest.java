package com.didekin.common.auth;

import com.didekin.common.LocalDev;
import com.didekin.userservice.auth.TkParamNames;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwx.JsonWebStructure;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

import static com.didekin.common.auth.TkHeaders.doHeadersSymmetricKey;
import static com.didekin.userservice.auth.TkParamNames.algorithm_ce;
import static com.didekin.userservice.auth.TkParamNames.algorithm_cek;
import static com.didekin.userservice.auth.TkParamNames.jwkey;
import static org.hamcrest.CoreMatchers.is;
import static org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
import static org.jose4j.jwe.KeyManagementAlgorithmIdentifiers.DIRECT;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 09/05/2018
 * Time: 11:59
 */
@Category({LocalDev.class})
public class TkHeadersTest {

    @Test
    public void test_getDefaultHeadersSymmetricKey_1()
    {
        Map<TkParamNames, String> headers = new HashMap<>();
        headers.putIfAbsent(jwkey, "json_web_key_object_to_string");
        TkHeaders headersFinal = doHeadersSymmetricKey(headers);
        checkMap(headersFinal);
        assertThat(headersFinal.getTokenHeader(jwkey), is("json_web_key_object_to_string"));
    }

    @Test
    public void test_getDefaultHeadersSymmetricKey_2()
    {
        Map<TkParamNames, String> headers = new HashMap<>();
        headers.putIfAbsent(algorithm_cek, "mock_alg_cek");
        TkHeaders headersFinal = doHeadersSymmetricKey(headers);
        // Mantiene el valor inicial.
        assertThat(headersFinal.getTokenHeader(algorithm_cek), is("mock_alg_cek"));
    }

    @Test
    public void test_getDefaultHeadersSymmetricKey_3()
    {
        checkMap(doHeadersSymmetricKey());
    }

    @Test
    public void test_putHeadersIn()
    {
        JsonWebStructure structureIn = new JsonWebEncryption();
        doHeadersSymmetricKey(null).putHeadersIn(structureIn);
        assertThat(structureIn.getHeader(algorithm_cek.getName()), is(DIRECT));
        assertThat(structureIn.getHeader(algorithm_ce.getName()), is(AES_128_CBC_HMAC_SHA_256));
    }

    // ===========================  Help methods ========================

    public static void checkMap(TkHeaders headersFinal)
    {
        assertThat(headersFinal.getTokenHeader(algorithm_cek), is(DIRECT));
        assertThat(headersFinal.getTokenHeader(algorithm_ce), is(AES_128_CBC_HMAC_SHA_256));
    }
}