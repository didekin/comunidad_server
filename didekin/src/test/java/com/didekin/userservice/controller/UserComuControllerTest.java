package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.auth.EncrypTkProducerBuilder;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UserMockRepoConfiguration;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.http.retrofit.HttpHandler;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadEndPoints;

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
import java.util.List;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.common.testutils.LocaleConstant.twoComponent_local_ES;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_EL_ESCORIAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_FUENTE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_OTRA;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_TRAV_PLAZUELA_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_olmo_55;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doHttpAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.model.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
@SuppressWarnings("ConstantConditions")
public abstract class UserComuControllerTest {

    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UsuarioManager usuarioManager;
    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private EncrypTkProducerBuilder producerBuilder;

    @Before
    public void setUp()
    {
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

//  ===========================================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComu()
    {
        /* Usuario con una comunidad y comunidad con un usuario: paco en comunidad 6.*/
        USERCOMU_ENDPOINT
                .deleteUserComu(
                        userMockManager.insertAuthTkGetNewAuthTkStr(paco.getUserName()),
                        calle_olmo_55.getC_Id()
                ).map(Response::body).test().assertValue(IS_USER_DELETED);
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_1()
    {
        List<Comunidad> comunidades = USERCOMU_ENDPOINT
                .getComusByUser(userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName()))
                .blockingGet().body();
        assertThat(comunidades, hasItems(COMU_LA_PLAZUELA_10bis, COMU_LA_FUENTE, COMU_EL_ESCORIAL));
    }

    @Test
    public void testGetComusByUser_2() throws IOException
    {
        // Intento de consulta por usuario no registrado.
        Response<List<Comunidad>> response = USERCOMU_ENDPOINT
                .getComusByUser(
                        doHttpAuthHeader(
                                new Usuario.UsuarioBuilder().userName("faked@user.com").gcmToken("faked.token").build(),
                                producerBuilder
                        )
                ).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USER_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComuByUserAndComu() throws IOException
    {
        // No existe la comunidad.
        String httpAuthHeader = userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName());
        Response<UsuarioComunidad> response = USERCOMU_ENDPOINT.getUserComuByUserAndComu(httpAuthHeader, 99L).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));

        // No existe el par usuario-comunidad.
        assertThat(USERCOMU_ENDPOINT.getUserComuByUserAndComu(httpAuthHeader, calle_plazuela_23.getC_Id()).blockingGet().body(), nullValue());

        // Comunidad asociada a usuario.
        UsuarioComunidad userComu = USERCOMU_ENDPOINT.getUserComuByUserAndComu(httpAuthHeader, 2L).blockingGet().body();
        assertThat(userComu.getComunidad(), is(COMU_LA_FUENTE));
        assertThat(userComu.getUsuario().getAlias(), is("pedronevado"));
        assertThat(userComu.getRoles(), is("adm"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestAdmonUserComu_1() throws ServiceException
    {
        // luis: PRO, not oldest, in comunidad 3.
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, luis)
                .portal("portal")
                .planta("planta2")
                .puerta("puertaB")
                .roles(PROPIETARIO.function).build();
        final String accessToken = userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName());
        int isInserted = USERCOMU_ENDPOINT.regUserComu(accessToken, usuarioComunidad).blockingGet().body();
        assertThat(isInserted, is(1));
        assertThat(USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(accessToken, calle_el_escorial.getC_Id()).blockingGet().body(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestAdmonUserComu_2() throws ServiceException, IOException
    {
        Response<Boolean> response = USERCOMU_ENDPOINT
                .isOldestOrAdmonUserComu(userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName()), 999L)
                .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData()
    {
        assertThat(
                USERCOMU_ENDPOINT.modifyComuData(
                        userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName()),
                        calle_el_escorial)
                        .blockingGet().body(),
                is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserComu()
    {
        // Preconditions.
        assertThat(
                usuarioManager.getUserComuByUserAndComu(
                        pedro_plazuelas_10bis.getUsuario().getUserName(),
                        pedro_plazuelas_10bis.getComunidad().getC_Id())
                        .getEscalera(),
                is(nullValue())
        );
        UsuarioComunidad userComuMod = new UsuarioComunidad.UserComuBuilder(ronda_plazuela_10bis, pedro)
                .userComuRest(pedro_plazuelas_10bis)
                .escalera("MOD")
                .build();
        // Exec.
        assertThat(
                USERCOMU_ENDPOINT.modifyUserComu(
                        userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName()),
                        userComuMod)
                        .blockingGet().body(),
                is(1));
        assertThat(usuarioManager
                        .getUserComuByUserAndComu(
                                pedro_plazuelas_10bis.getUsuario().getUserName(),
                                pedro_plazuelas_10bis.getComunidad().getC_Id()
                        ).getEscalera(),
                is("MOD"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu()
    {
        // Preconditions a user already registered: luis en comunidades 1 y 2.
        UsuarioComunidad usuarioCom = new UsuarioComunidad.UserComuBuilder(COMU_OTRA, null).portal("B").escalera
                ("ESC").planta("11").puerta("puerta").roles(ADMINISTRADOR.function).build();
        // Exec.
        boolean isRegOk =
                USERCOMU_ENDPOINT.regComuAndUserComu(
                        userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName()),
                        usuarioCom)
                        .blockingGet().body();
        // Check.
        assertThat(isRegOk, is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu()
    {
        boolean isInserted = USERCOMU_ENDPOINT
                .regComuAndUserAndUserComu(oneComponent_local_ES, COMU_REAL_JUAN).blockingGet().body();
        assertThat(isInserted, is(true));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1()
    {
        // Preconditions: a comunidad is already associated to other users.
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(twoComponent_local_ES, COMU_TRAV_PLAZUELA_PEPE).blockingGet();
        Comunidad comunidad = usuarioManager.getComusByUser(USER_PEPE.getUserName()).get(0);
        // Data, exec, check.
        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, USER_JUAN, "portalC", null, "planta3", null,
                PRESIDENTE.function);
        boolean isInserted = USERCOMU_ENDPOINT.regUserAndUserComu(oneComponent_local_ES, userComu).blockingGet().body();
        assertThat(isInserted, is(true));
        assertThat(usuarioManager.getComusByUser(USER_JUAN.getUserName()).size(), is(1));
        assertThat(usuarioManager.getComusByUser(USER_JUAN.getUserName()).get(0), is(comunidad));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserComu()
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, null).portal("portal")
                .planta("planta2").puerta("puertaB").roles(PROPIETARIO.function).build();
        // Exec.
        int rowInserted =
                USERCOMU_ENDPOINT.regUserComu(
                        userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName()),
                        usuarioComunidad)
                        .blockingGet().body();
        // Check.
        assertThat(rowInserted, is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComuByComu()
    {
        // This is a registered user not asssociated to the comunidad 1 used in the tesst.
        List<UsuarioComunidad> usuarioComus =
                USERCOMU_ENDPOINT.seeUserComusByComu(
                        userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName()),
                        calle_la_fuente_11.getC_Id())
                        .blockingGet().body();
        assertThat(usuarioComus.size(), is(2));
        assertThat(usuarioComus.get(1).getUsuario(), is(pedro));
        assertThat(usuarioComus.get(0).getUsuario(), is(luis));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser()
    {
        // The password in data base is encrypted.
        List<UsuarioComunidad> comunidades =
                USERCOMU_ENDPOINT.seeUserComusByUser(
                        userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName()))
                        .blockingGet().body();
        assertThat(comunidades.size(), is(3));
        assertThat(comunidades.get(0).getUsuario(), is(luis));
        assertThat(comunidades.get(0).getComunidad(), is(calle_plazuela_23));
    }

// ............................................... HELPER CLASSES AND METHODS .................................

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class, DbPre.class})
    @DirtiesContext
    public static class UserComuCtrlerDevTest extends UserComuControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            UserMockRepoConfiguration.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UserComuCtrlerAwsTest extends UserComuControllerTest {
    }
}