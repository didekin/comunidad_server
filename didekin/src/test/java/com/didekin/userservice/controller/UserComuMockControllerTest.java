package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.http.retrofit.HttpHandler;
import com.didekinlib.model.relacion.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuario.http.UserMockEndPoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.controller.UserComuMockController.CLOSED_AREA_MSG;
import static com.didekin.userservice.controller.UserComuMockController.OPEN_AREA_MSG;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PACO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkUserNotFound;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doHttpAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.CommonServConstant.OPEN;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuario.http.UsuarioServConstant.USER_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UserComuMockControllerTest {

    private UserMockEndPoints userComuMockEndPoint;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UsuarioManager usuarioManager;

    @Before
    public void setUp()
    {
        userComuMockEndPoint = retrofitHandler.getService(UserMockEndPoints.class);
    }

//  ===========================================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUser()
    {
        // Borra al usuario y las comunidades previamente encontradas en la consulta.
        userComuMockEndPoint.deleteUser(pedro.getUserName()).map(Response::body).test().assertValue(true);
        checkUserNotFound(pedro.getUserName(), usuarioManager);
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu()
    {
        userComuMockEndPoint.regComuAndUserAndUserComu(COMU_REAL_JUAN)
                .test()
                .assertValue(response -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(response.body()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1()
    {
        /* Preconditions: a comunidad is already associated to other users.*/
        UsuarioComunidad userComu = makeUsuarioComunidad(calle_plazuela_23,
                new Usuario.UsuarioBuilder().copyUsuario(USER_PACO).userName("new@paco.com").build(),
                "portalC", null, "planta3", null);

        userComuMockEndPoint.regUserAndUserComu(userComu)
                .test()
                .assertValue(response -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(response.body()));
    }

    @Test
    public void test_TryTokenInterceptor() throws IOException
    {
        assertThat(
                userComuMockEndPoint.tryTokenInterceptor("mockAuthHeader",
                        OPEN.substring(1),
                        "hola")
                        .execute().body(),
                is(OPEN_AREA_MSG));

        assertThat(
                userComuMockEndPoint.tryTokenInterceptor(
                        doHttpAuthHeader(pedro, usuarioManager.getProducerBuilder()),
                        USER_PATH.substring(1),
                        "hola")
                        .execute().body(),
                is(CLOSED_AREA_MSG));
    }

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class, DbPre.class})
    @DirtiesContext
    public static class UserComuMockCtrlerDevTest extends UserComuMockControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            UsuarioRepoConfiguration.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UserComuMockCtrlerAwsTest extends UserComuMockControllerTest {
    }
}