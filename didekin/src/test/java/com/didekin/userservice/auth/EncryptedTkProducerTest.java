package com.didekin.userservice.auth;


import com.didekin.Application;
import com.didekin.common.auth.TkAuthClaims;
import com.didekin.common.auth.TkHeaders;
import com.didekin.common.AwsPre;
import com.didekin.common.LocalDev;
import com.didekinlib.http.usuario.TkParamNames;

import org.jose4j.jwt.MalformedClaimException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.auth.TkAuthClaimsTest.checkMap;
import static com.didekin.common.auth.TkHeaders.doHeadersSymmetricKey;
import static com.didekin.common.auth.TkHeadersTest.checkMap;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doHttpAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.getDefaultTestClaims;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.TkParamNames.appId;
import static com.didekinlib.http.usuario.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.common.dominio.BeanBuilder.error_message_bean_building;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * User: pedro@didekin
 * Date: 11/05/2018
 * Time: 15:16
 */
public abstract class EncryptedTkProducerTest {

    @Autowired
    private EncrypTkProducerBuilder producerBuilder;

    @Test
    public void test_getEncryptedTkStr_1()
    {
        String encryptedTkStr = producerBuilder
                .headers(doHeadersSymmetricKey())
                .claims(getDefaultTestClaims(pedro.getUserName())) // default data test.
                .build()
                .getEncryptedTkStr();

        /*
            eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0
            .
            ._L86WbOFHY-3g0E2EXejJg
            .UB1tHZZq0TYFTZKPVZXY83GRxHz770Aq7BuMCEbNnaSC5cVNOLEOgBQrOQVJmVL-9Ke9KRSwuq7MmVcA2EB_0xRBr_YbzmMWbpUcTQUFtE5OZOFiCsxL5Yn0gA_DDLZboivpoSqndQRP-44mWVkM1A
            . RIvTWRrsyoJ1mpl8vUhQDQ
        */
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(encryptedTkStr), is(true));
    }

    @Test
    public void test_getEncryptedTkStr_2()
    {
        EncrypTkProducerBuilder builder = producerBuilder
                .headers(doHeadersSymmetricKey())
                .claims(getDefaultTestClaims(pedro.getUserName()));

        // The same builder produces different tokens.
        assertThat(builder.build().getEncryptedTkStr().equals(builder.build().getEncryptedTkStr()), is(false));
    }

    @Test
    public void test_getEncryptedTkStr_3()
    {
        try {
            producerBuilder.headers(null)
                    .claims(getDefaultTestClaims(pedro.getUserName()))
                    .build();
            fail();
        } catch (Exception e) {
            assertThat(e instanceof IllegalStateException, is(true));
            assertThat(e.getMessage(), is(error_message_bean_building + EncrypTkProducerBuilder.class.getName()));
        }
    }

    @Test
    public void test_defaultHeadersClaims_1() throws MalformedClaimException
    {
        EncrypTkProducerBuilder builder = producerBuilder.defaultHeadersClaims("user@name.com", "appId_1234");
        TkHeaders headers = builder.getHeaders();
        checkMap(headers);
        TkAuthClaims claims = builder.getClaims();
        checkMap(claims);
        assertThat(claims.getAuthClaim(TkParamNames.subject), is("user@name.com"));
        assertThat(claims.getAuthClaim(appId), is("appId_1234"));

        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(builder.build().getEncryptedTkStr()), is(true));
    }

    @Test
    public void test_defaultHttpHeaders_1()
    {
        String httpHeader1 = doHttpAuthHeader(pedro, producerBuilder);
        String httpHeader2 = doHttpAuthHeader(pedro, producerBuilder);
        assertThat(httpHeader1.equals(httpHeader2), is(false));
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class}, webEnvironment = DEFINED_PORT)
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL})
    public static class EncryptedTkProducerDevTest extends EncryptedTkProducerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class}, webEnvironment = DEFINED_PORT)
    @Category({AwsPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE})
    public static class EncryptedTkProducerAwsTest extends EncryptedTkProducerTest {
    }
}