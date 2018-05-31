package com.didekin.userservice.auth;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.LocalDev;
import com.didekin.common.auth.EncrypTkConsumerBuilder;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekinlib.http.usuario.TkParamNames;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.didekin.common.auth.TkAuthClaims.getDefaultClaim;
import static com.didekin.common.auth.TkHeaders.doHeadersSymmetricKey;
import static com.didekin.common.auth.TkHeaders.getDefaultHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.getDefaultTestClaims;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.TkParamNames.algorithm_ce;
import static com.didekinlib.http.usuario.TkParamNames.algorithm_cek;
import static com.didekinlib.http.usuario.TkParamNames.appId;
import static com.didekinlib.http.usuario.TkParamNames.audience;
import static com.didekinlib.http.usuario.TkParamNames.issuer;
import static com.didekinlib.http.usuario.TkParamNames.subject;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * User: pedro@didekin
 * Date: 14/05/2018
 * Time: 16:27
 */
public abstract class EncryptedTkConsumerTest {

    @Autowired
    private EncrypTkProducerBuilder producerBuilder;
    @Autowired
    private EncrypTkConsumerBuilder consumerBuilder;

    private String encryptedTkStr;

    @Before
    public void setUp()
    {
        encryptedTkStr = producerBuilder
                .headers(doHeadersSymmetricKey())
                .claims(getDefaultTestClaims(pedro.getUserName()))  // default data test for subject and appId.
                .build()
                .getEncryptedTkStr();
    }

    @Test
    public void test_GetClaims_1() throws MalformedClaimException
    {
        JwtClaims claimsDesEnc = consumerBuilder.defaultInit(encryptedTkStr)
                .build()
                .getClaims();
        checkPlainTxtClaims(claimsDesEnc);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void test_GetClaims_2() throws MalformedClaimException
    {
        JwtClaims claimsDesEnc = consumerBuilder.tokenToConsume(encryptedTkStr)
                .contentEncryptAlg(getDefaultHeader(algorithm_ce))
                .keyManagementAlg(getDefaultHeader(algorithm_cek))
                .expectAudience((List<String>) getDefaultClaim(audience))
                .expectedIssuer((String) getDefaultClaim(TkParamNames.issuer))
                .build()
                .getClaims();

        checkPlainTxtClaims(claimsDesEnc);
    }

    private void checkPlainTxtClaims(JwtClaims claimsDesEnc) throws MalformedClaimException
    {
        /*{"exp":1531916447000,"aud":["didekin_web"],"sub":"pedro@didekin.es","appId":"appId_mock","iss":"didekin_auth"}*/
        assertThat(claimsDesEnc.getAudience(), is(getDefaultClaim(audience)));
        assertThat(claimsDesEnc.getIssuer(), is(getDefaultClaim(issuer)));
        assertThat(claimsDesEnc.getClaimsMap().get(appId.getName()), is(getDefaultTestClaims(pedro.getUserName()).get(appId)));
        assertThat(claimsDesEnc.getSubject(), is(getDefaultTestClaims(pedro.getUserName()).get(subject)));
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class, RetrofitConfigurationDev.class}, webEnvironment = DEFINED_PORT)
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL})
    public static class EncryptedTkConsumerDevTest extends EncryptedTkConsumerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class}, webEnvironment = DEFINED_PORT)
    @Category({AwsPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE})
    public static class EncryptedTkConsumerAwsTest extends EncryptedTkConsumerTest {
    }
}