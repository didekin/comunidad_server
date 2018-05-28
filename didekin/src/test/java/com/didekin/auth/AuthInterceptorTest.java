package com.didekin.auth;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.userservice.controller.UserComuMockEndPoints;
import com.didekinlib.http.HttpHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.controller.UserComuMockController.CLOSED_AREA_MSG;
import static com.didekin.userservice.controller.UserComuMockController.OPEN_AREA_MSG;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doHttpAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.BAD_REQUEST;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.usuario.UsuarioServConstant.OPEN;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * User: pedro@didekin
 * Date: 20/05/2018
 * Time: 17:33
 */
public abstract class AuthInterceptorTest {

    @Autowired
    private AuthInterceptor interceptor;
    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private EncrypTkProducerBuilder producerBuilder;

    private UserComuMockEndPoints userComuMockEndPoint;

    @Before
    public void setUp()
    {
        userComuMockEndPoint = retrofitHandler.getService(UserComuMockEndPoints.class);
    }

    @Test
    public void test_GetBuilder()
    {
        assertThat(interceptor.getConsumerBuilder(), notNullValue());
    }

    @Test
    public void test_PreHandle_1() throws IOException
    {
        // Path in open area and header empty.
        assertThat(userComuMockEndPoint.tryTokenInterceptor("", OPEN.substring(1), "login").execute().body(), is(OPEN_AREA_MSG));
    }

    @Test
    public void test_PreHandle_2() throws IOException
    {
        // Path in open area and header not empty.
        Response<String> response = userComuMockEndPoint.tryTokenInterceptor("HEADER", OPEN.substring(1), "login").execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Test
    public void test_PreHandle_3() throws IOException
    {
        // Path in closed area and header empty.
        Response<String> response = userComuMockEndPoint.tryTokenInterceptor("", USER_PATH.substring(1), "read").execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(BAD_REQUEST.getHttpMessage()));
    }

    @Test
    public void test_PreHandle_4() throws IOException
    {
        // Path in closed area and header wrong.
        Response<String> response = userComuMockEndPoint.tryTokenInterceptor("HEADER", USER_PATH.substring(1), "read").execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(TOKEN_ENCRYP_DECRYP_ERROR.getHttpMessage()));
    }

    @Test
    public void test_PreHandle_5() throws IOException
    {
        // Path in closed area and header OK.
        assertThat(userComuMockEndPoint.tryTokenInterceptor(doHttpAuthHeader(pedro, producerBuilder), USER_PATH.substring(1), "hola").execute().body(), is(CLOSED_AREA_MSG));
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class, RetrofitConfigurationDev.class}, webEnvironment = DEFINED_PORT)
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL})
    public static class AuthInterceptorDevTest extends AuthInterceptorTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class}, webEnvironment = DEFINED_PORT)
    @Category({AwsPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE})
    public static class AuthInterceptorAwsTest extends AuthInterceptorTest {
    }
}