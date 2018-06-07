package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.http.HttpHandler;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

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
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PACO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkUserNotFound;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UserComuMockControllerTest {

    private UserComuMockEndPoints userComuMockEndPoint;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UsuarioManager usuarioManager;

    @Before
    public void setUp()
    {
        userComuMockEndPoint = retrofitHandler.getService(UserComuMockEndPoints.class);
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
                new Usuario.UsuarioBuilder().copyUsuario(USER_PACO).userName("newPaco").build(),
                "portalC", null, "planta3", null,
                PRESIDENTE.function);

        userComuMockEndPoint.regUserAndUserComu(userComu)
                .test()
                .assertValue(response -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(response.body()));
        assertThat(usuarioManager.getComusByUser(userComu.getUsuario().getUserName()).size(), is(1));
        assertThat(usuarioManager.getComusByUser(userComu.getUsuario().getUserName()).get(0), is(calle_plazuela_23));
    }

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class})
    @DirtiesContext
    public static class UserComuMockControllerDevTest extends UserComuMockControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class},
            webEnvironment = DEFINED_PORT)
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({DbPre.class})
    @DirtiesContext
    public static class UserComuMockControllerPreTest extends UserComuMockControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            UsuarioRepoConfiguration.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UserComuMockControllerAwsTest extends UserComuMockControllerTest {
    }
}