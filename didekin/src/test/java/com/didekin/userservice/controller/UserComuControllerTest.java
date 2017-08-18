package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.EntityException;
import com.didekin.common.LocalDev;
import com.didekin.common.Profiles;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.http.oauth2.SpringOauthToken;
import com.didekinlib.http.retrofit.Oauth2EndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioComunidadEndPoints;
import com.didekinlib.http.retrofit.UsuarioEndPoints;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import retrofit2.Response;

import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_EL_ESCORIAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_FUENTE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_OTRA;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_TRAV_PLAZUELA_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.insertUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenLuis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPepe;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthTokenHelper.HELPER;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_DUPLICATE;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UserComuControllerTest {

    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;
    private UsuarioEndPoints USER_ENDPOINT;
    private Oauth2EndPoints OAUTH_ENDPOINT;

    @Autowired
    private RetrofitHandler retrofitHandler;
    @Autowired
    private UsuarioServiceIf sujetosService;

    @Before
    public void setUp() throws Exception
    {
        OAUTH_ENDPOINT = retrofitHandler.getService(Oauth2EndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
    }

    @After
    public void clear()
    {
    }

//  ===========================================================================================================

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComu_1() throws IOException
    {
        // Usuario con una comunidad y comunidad con un usuario.
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_PEPE).execute().body();

        List<UsuarioComunidad> uComus_1 = sujetosService.seeUserComusByUser(USER_PEPE.getUserName());

        assertThat(USERCOMU_ENDPOINT.deleteUserComu(tokenPepe(retrofitHandler), uComus_1.get(0).getComunidad().getC_Id()).execute().body(), is(IS_USER_DELETED));

        try {
            sujetosService.getComunidadById(uComus_1.get(0).getComunidad().getC_Id());
            fail();
        } catch (EntityException e) {
            // Comunidad borrada.
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }

        Response<SpringOauthToken> response = OAUTH_ENDPOINT.getPasswordUserToken(
                new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                USER_PEPE.getUserName(),
                USER_PEPE.getPassword(),
                PASSWORD_GRANT
        ).execute();
        // BAD CREDENTIALS, una vez borrado el usuario.
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getHttpStatus(), is(400));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComu_2() throws EntityException, IOException
    {
        // Usuario con una comunidad y comunidad con dos usuarios.

        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_PEPE).execute().body();
        List<UsuarioComunidad> uComusPepe = sujetosService.seeUserComusByUser(USER_PEPE.getUserName());

        UsuarioComunidad uC_juan = makeUsuarioComunidad(uComusPepe.get(0).getComunidad(), USER_JUAN,
                "portalA", null, null, "23", "pro");
        USERCOMU_ENDPOINT.regUserAndUserComu(uC_juan).execute().body();
        SpringOauthToken tokenJuan = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                USER_JUAN.getUserName(),
                USER_JUAN.getPassword(),
                PASSWORD_GRANT).execute().body();
        List<UsuarioComunidad> uComusJuan = sujetosService.seeUserComusByUser(USER_JUAN.getUserName());

        assertThat(USERCOMU_ENDPOINT.deleteUserComu(
                HELPER.doBearerAccessTkHeader(tokenJuan), uComusJuan.get(0).getComunidad().getC_Id()).execute().body(),
                is(IS_USER_DELETED));

        Response<Boolean> response = USER_ENDPOINT.deleteUser(HELPER.doBearerAccessTkHeader(tokenJuan)).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_1() throws IOException
    {
        List<Comunidad> comunidades = USERCOMU_ENDPOINT.getComusByUser(tokenPedro(retrofitHandler))
                .execute().body();
        assertThat(comunidades, hasItems(COMU_LA_PLAZUELA_10bis, COMU_LA_FUENTE, COMU_EL_ESCORIAL));
    }

    @Test
    public void testGetComusByUser_2() throws IOException
    {
        // Intento de consulta con token de usuario no registrado.
        Response<List<Comunidad>> response = USERCOMU_ENDPOINT.getComusByUser(
                HELPER.doBearerAccessTkHeader(
                        new SpringOauthToken("faked_token", null, null, new SpringOauthToken.OauthToken("faked_token_refresh", null), null)))
                .execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComuByUserAndComu() throws IOException
    {
        Response<UsuarioComunidad> response = USERCOMU_ENDPOINT.getUserComuByUserAndComu(tokenPedro(retrofitHandler), 99L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));

        // Comunidad asociada a usuario.
        UsuarioComunidad userComu = USERCOMU_ENDPOINT.getUserComuByUserAndComu(tokenPedro(retrofitHandler), 2L).execute().body();
        assertThat(userComu.getComunidad(), is(COMU_LA_FUENTE));
        assertThat(userComu.getUsuario().getAlias(), is("pedronevado"));
        assertThat(userComu.getRoles(), is("adm"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestAdmonUserComu_1() throws IOException, EntityException
    {
        // Oldest.
        String token = new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader("pedro@pedro.com", "password3");
        assertThat(sujetosService.getHighestFunctionalRol("pedro@pedro.com", 1L), is(PROPIETARIO.function));
        assertThat(USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(token, 1L).execute().body(), is(true));

        // ADM.
        token = new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader("luis@luis.com", "password5");
        assertThat(sujetosService.getHighestFunctionalRol("luis@luis.com", 1L), is(ADMINISTRADOR.function));
        assertThat(USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(token, 1L).execute().body(), is(true));

        // luis: PRO, not oldest, in comunidad 2.
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(2L).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, null).portal("portal")
                .planta("planta2").puerta("puertaB").roles(PROPIETARIO.function).build();
        int isInserted = USERCOMU_ENDPOINT.regUserComu(token, usuarioComunidad).execute().body();
        assertThat(isInserted, is(1));
        assertThat(sujetosService.getHighestFunctionalRol("luis@luis.com", 2L), is(PROPIETARIO.function));
        assertThat(USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(token, 2L).execute().body(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestAdmonUserComu_2() throws IOException, EntityException
    {
        String token = new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader("pedro@pedro.com", "password3");
        Response<Boolean> response = USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(token, 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws IOException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(3L).tipoVia("tipoV1").nombreVia("nombreV1")
                .municipio(new Municipio((short) 13, new Provincia((short) 3))).build();
        SpringOauthToken token = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                "luis@luis.com",
                "password5",
                PASSWORD_GRANT).execute().body();

        Response<Integer> response = USERCOMU_ENDPOINT.modifyComuData(HELPER.doBearerAccessTkHeader(token), comunidad).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));

        token = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                "pedro@pedro.com",
                "password3",
                PASSWORD_GRANT).execute().body();
        assertThat(USERCOMU_ENDPOINT.modifyComuData(HELPER.doBearerAccessTkHeader(token), comunidad).execute().body(),
                is(1));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserComu_1() throws IOException
    {
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_PEPE).execute().body();
        SpringOauthToken token_1 =
                OAUTH_ENDPOINT.getPasswordUserToken(
                        new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                        USER_PEPE.getUserName(),
                        USER_PEPE.getPassword(),
                        PASSWORD_GRANT)
                        .execute().body();

        List<UsuarioComunidad> uComus_1 = sujetosService.seeUserComusByUser(USER_PEPE.getUserName());

        UsuarioComunidad uc_1 = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(uComus_1.get(0).getComunidad().getC_Id()).build(),
                null,
                null,
                null,
                null,
                null,
                "inq"
        );

        assertThat(USERCOMU_ENDPOINT.modifyUserComu(HELPER.doBearerAccessTkHeader(token_1), uc_1).execute().body(), is(1));
        List<UsuarioComunidad> uComus_2 = sujetosService.seeUserComusByUser(USER_PEPE.getUserName());
        assertThat(uComus_2.get(0).getPortal(), is(uc_1.getPortal()));
        assertThat(uComus_2.get(0).getEscalera(), is(uc_1.getEscalera()));
        assertThat(uComus_2.get(0).getPlanta(), is(uc_1.getPlanta()));
        assertThat(uComus_2.get(0).getPuerta(), is(uc_1.getPuerta()));
        assertThat(uComus_2.get(0).getRoles(), is(uc_1.getRoles()));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu() throws IOException
    {
        // Preconditions a user already registered in a communidad.
        insertUsuarioComunidad(COMU_REAL_JUAN, USERCOMU_ENDPOINT, USER_ENDPOINT, retrofitHandler);

        UsuarioComunidad usuarioCom = new UsuarioComunidad.UserComuBuilder(COMU_OTRA, null).portal("AB").escalera
                ("ESC").planta("11").puerta("puerta").roles(ADMINISTRADOR.function).build();

        // The password encoded in the http request for an accessToken should be the original one, not encoded.
        boolean isRegOk = USERCOMU_ENDPOINT.regComuAndUserComu(
                new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader(USER_JUAN.getUserName(), USER_JUAN.getPassword()),
                usuarioCom)
                .execute().body();

        assertThat(isRegOk, is(true));
    }

    @Test
    public void testRegComuAndUserAndUserComu() throws IOException
    {
        boolean isInserted = USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_JUAN).execute().body();
        assertThat(isInserted, is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws IOException
    {
        // Preconditions: a comunidad is already associated to other users.
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_TRAV_PLAZUELA_PEPE).execute().body();
        Comunidad comunidad = sujetosService.getComusByUser(USER_PEPE.getUserName()).get(0);

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, USER_JUAN, "portalC", null, "planta3", null,
                PRESIDENTE.function);
        boolean isInserted = USERCOMU_ENDPOINT.regUserAndUserComu(userComu).execute().body();
        assertThat(isInserted, is(true));
        assertThat(sujetosService.getComusByUser(USER_JUAN.getUserName()).size(), is(1));
        assertThat(sujetosService.getComusByUser(USER_JUAN.getUserName()).get(0), is(comunidad));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_2() throws SQLException, EntityException, IOException
    {
        // Duplicate user (and comunidad).
        sujetosService.regComuAndUserAndUserComu(COMU_REAL_JUAN);
        Usuario usuario = sujetosService.getUserByUserName(USER_JUAN.getUserName());
        Comunidad comunidad = sujetosService.getComusByUser(USER_JUAN.getUserName()).get(0);

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, usuario, "portal", "esc",
                "plantaY", "door22", PRESIDENTE.function);
        Response<Boolean> response = USERCOMU_ENDPOINT.regUserAndUserComu(userComu).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USER_NAME_DUPLICATE.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserComu() throws IOException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(3L).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, null).portal("portal")
                .planta("planta2").puerta("puertaB").roles(PROPIETARIO.function).build();

        int rowInserted = USERCOMU_ENDPOINT.regUserComu(tokenLuis(retrofitHandler),
                usuarioComunidad).execute().body();

        assertThat(rowInserted, is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComuByComu() throws IOException
    {
        // This is a registered user not asssociated to the comunidad 1 used in the tesst.
        List<UsuarioComunidad> usuarioComus = USERCOMU_ENDPOINT.seeUserComusByComu(tokenLuis(retrofitHandler), 2L).execute().body();
        assertThat(usuarioComus.size(), is(1));
        assertThat(usuarioComus.get(0).getUsuario().getUserName(), is("pedro@pedro.com"));

        usuarioComus = USERCOMU_ENDPOINT.seeUserComusByComu(tokenLuis(retrofitHandler), 1L).execute().body();
        assertThat(usuarioComus.size(), is(2));
        assertThat(usuarioComus.get(0).getUsuario().getUserName(), is("luis@luis.com"));
        assertThat(usuarioComus.get(1).getUsuario().getUserName(), is("pedro@pedro.com"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser() throws IOException
    {
        // The password in data base is encrypted.
        List<UsuarioComunidad> comunidades = USERCOMU_ENDPOINT.seeUserComusByUser(tokenLuis(retrofitHandler)).execute().body();
        assertThat(comunidades.size(), is(1));

        UsuarioComunidad userComu = comunidades.get(0);
        Comunidad comuPlazuela10bis = userComu.getComunidad();
        assertThat(comuPlazuela10bis.getNombreComunidad(), is(COMU_LA_PLAZUELA_10bis.getNombreComunidad()));
        assertThat(comuPlazuela10bis.getMunicipio().getNombre(), is("Motilleja"));
        assertThat(comuPlazuela10bis.getMunicipio().getProvincia().getNombre(), is("Albacete"));

        assertThat(userComu, allOf(
                hasProperty("portal", is(isEmptyOrNullString())),
                hasProperty("escalera", is(isEmptyOrNullString())),
                hasProperty("planta", is(isEmptyOrNullString())),
                hasProperty("puerta", is(isEmptyOrNullString()))
        ));
    }

// ............................................... HELPER CLASSES AND METHODS .................................

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({LocalDev.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UserComuControllerDevTest extends UserComuControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({DbPre.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UserComuControllerPreTest extends UserComuControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {RetrofitConfigurationPre.class,
            UsuarioRepoConfiguration.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_PRE, Profiles.MAIL_PRE})
    @Category({AwsPre.class})
    public static class UserComuControllerAwsTest extends UserComuControllerTest {
    }
}