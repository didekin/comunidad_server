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
import com.didekin.userservice.repository.UsuarioManagerIf;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.http.oauth2.SpringOauthToken;
import com.didekinlib.http.retrofit.Oauth2EndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioComunidadEndPoints;
import com.didekinlib.model.comunidad.Comunidad;
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

import java.io.EOFException;
import java.io.IOException;
import java.util.List;

import retrofit2.Response;

import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.common.testutils.Constant.twoComponent_local_ES;
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
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenLuis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPaco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPedro;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthTokenHelper.HELPER;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_DUPLICATE;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
    private Oauth2EndPoints OAUTH_ENDPOINT;

    @Autowired
    private RetrofitHandler retrofitHandler;
    @Autowired
    private UsuarioManagerIf sujetosService;

    @Before
    public void setUp()
    {
        OAUTH_ENDPOINT = retrofitHandler.getService(Oauth2EndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

    @After
    public void clear()
    {
    }

//  ===========================================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComu() throws IOException
    {
        /* Usuario con una comunidad y comunidad con un usuario: paco en comunidad 6.*/
        // Exec
        assertThat(USERCOMU_ENDPOINT.deleteUserComu(tokenPaco(retrofitHandler), calle_olmo_55.getC_Id()).execute().body(), is(IS_USER_DELETED));
        // Check comunidad deleted
        try {
            sujetosService.getComunidadById(calle_olmo_55.getC_Id());
            fail();
        } catch (EntityException e) {
            // Comunidad borrada.
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        // Check no token para el usuario borrado.
        Response<SpringOauthToken> response = OAUTH_ENDPOINT.getPasswordUserToken(
                new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                paco.getUserName(),
                paco.getPassword(),
                PASSWORD_GRANT
        ).execute();
        // BAD CREDENTIALS, una vez borrado el usuario.
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getHttpStatus(), is(400));
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
        // No existe la comunidad.
        try {
            USERCOMU_ENDPOINT.getUserComuByUserAndComu(tokenPedro(retrofitHandler), 99L).execute();
            fail();
        } catch (Exception e) {
            assertThat(e instanceof EOFException, is(true));
        }

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
        // luis: PRO, not oldest, in comunidad 3.
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, luis)
                .portal("portal")
                .planta("planta2")
                .puerta("puertaB")
                .roles(PROPIETARIO.function).build();
        int isInserted = USERCOMU_ENDPOINT.regUserComu(tokenLuis(retrofitHandler), usuarioComunidad).execute().body();
        assertThat(isInserted, is(1));
        assertThat(USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(tokenLuis(retrofitHandler), calle_el_escorial.getC_Id()).execute().body(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestAdmonUserComu_2() throws IOException, EntityException
    {
        String token = new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(pedro.getUserName(), "password3");
        Response<Boolean> response = USERCOMU_ENDPOINT.isOldestOrAdmonUserComu(token, 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws IOException
    {
        assertThat(USERCOMU_ENDPOINT.modifyComuData(tokenPedro(retrofitHandler), calle_el_escorial).execute().body(),
                is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserComu() throws IOException
    {
        // Preconditions.
        assertThat(sujetosService.getUserComuByUserAndComu(pedro_plazuelas_10bis.getUsuario().getUserName(), pedro_plazuelas_10bis.getComunidad().getC_Id()).getEscalera(), is(nullValue()));
        UsuarioComunidad userComuMod = new UsuarioComunidad.UserComuBuilder(ronda_plazuela_10bis, pedro).userComuRest(pedro_plazuelas_10bis).escalera("MOD").build();
        // Exec.
        assertThat(USERCOMU_ENDPOINT.modifyUserComu(tokenPedro(retrofitHandler), userComuMod).execute().body(), is(1));
        assertThat(sujetosService.getUserComuByUserAndComu(pedro_plazuelas_10bis.getUsuario().getUserName(), pedro_plazuelas_10bis.getComunidad().getC_Id()).getEscalera(), is("MOD"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu() throws IOException
    {
        // Preconditions a user already registered: luis en comunidades 1 y 2.
        UsuarioComunidad usuarioCom = new UsuarioComunidad.UserComuBuilder(COMU_OTRA, null).portal("B").escalera
                ("ESC").planta("11").puerta("puerta").roles(ADMINISTRADOR.function).build();
        // Exec.
        boolean isRegOk = USERCOMU_ENDPOINT.regComuAndUserComu(tokenLuis(retrofitHandler), usuarioCom).execute().body();
        // Check.
        assertThat(isRegOk, is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu() throws IOException
    {
        boolean isInserted = USERCOMU_ENDPOINT.regComuAndUserAndUserComu(oneComponent_local_ES, COMU_REAL_JUAN).execute().body();
        assertThat(isInserted, is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws IOException
    {
        // Preconditions: a comunidad is already associated to other users.
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(twoComponent_local_ES, COMU_TRAV_PLAZUELA_PEPE).execute();
        Comunidad comunidad = sujetosService.getComusByUser(USER_PEPE.getUserName()).get(0);

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, USER_JUAN, "portalC", null, "planta3", null,
                PRESIDENTE.function);
        boolean isInserted = USERCOMU_ENDPOINT.regUserAndUserComu(oneComponent_local_ES, userComu).execute().body();
        assertThat(isInserted, is(true));
        assertThat(sujetosService.getComusByUser(USER_JUAN.getUserName()).size(), is(1));
        assertThat(sujetosService.getComusByUser(USER_JUAN.getUserName()).get(0), is(comunidad));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_2() throws EntityException, IOException
    {
        // Duplicate user (and comunidad).
        sujetosService.regComuAndUserAndUserComu(COMU_REAL_JUAN, oneComponent_local_ES);
        Usuario usuario = sujetosService.getUserByUserName(USER_JUAN.getUserName());
        Comunidad comunidad = sujetosService.getComusByUser(USER_JUAN.getUserName()).get(0);

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, usuario, "portal", "esc",
                "plantaY", "door22", PRESIDENTE.function);
        Response<Boolean> response = USERCOMU_ENDPOINT.regUserAndUserComu(twoComponent_local_ES, userComu).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USER_NAME_DUPLICATE.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserComu() throws IOException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, null).portal("portal")
                .planta("planta2").puerta("puertaB").roles(PROPIETARIO.function).build();
        // Exec.
        int rowInserted = USERCOMU_ENDPOINT.regUserComu(tokenLuis(retrofitHandler), usuarioComunidad).execute().body();
        // Check.
        assertThat(rowInserted, is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComuByComu() throws IOException
    {
        // This is a registered user not asssociated to the comunidad 1 used in the tesst.
        List<UsuarioComunidad> usuarioComus = USERCOMU_ENDPOINT.seeUserComusByComu(tokenLuis(retrofitHandler), calle_la_fuente_11.getC_Id()).execute().body();
        assertThat(usuarioComus.size(), is(2));
        assertThat(usuarioComus.get(1).getUsuario(), is(pedro));
        assertThat(usuarioComus.get(0).getUsuario(), is(luis));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser() throws IOException
    {
        // The password in data base is encrypted.
        List<UsuarioComunidad> comunidades = USERCOMU_ENDPOINT.seeUserComusByUser(tokenLuis(retrofitHandler)).execute().body();
        assertThat(comunidades.size(), is(3));
        assertThat(comunidades.get(0).getUsuario(), is(luis));
        assertThat(comunidades.get(0).getComunidad(), is(calle_plazuela_23));
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