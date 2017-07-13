package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.Profiles;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.userservice.repository.ServOneRepoConfiguration;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.http.oauth2.SpringOauthToken;
import com.didekinlib.http.retrofit.ComunidadEndPoints;
import com.didekinlib.http.retrofit.Oauth2EndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioComunidadEndPoints;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
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

import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthTokenHelper.HELPER;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class ComunidadControllerTest {

    private ComunidadEndPoints COMU_ENDPOINT;
    private Oauth2EndPoints OAUTH_ENDPOINT;
    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;

    @Autowired
    private RetrofitHandler retrofitHandler;
    @Autowired
    UsuarioServiceIf sujetosService;

    @Before
    public void setUp() throws Exception
    {
        OAUTH_ENDPOINT = retrofitHandler.getService(Oauth2EndPoints.class);
        COMU_ENDPOINT = retrofitHandler.getService(ComunidadEndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

    @After
    public void clear()
    {
    }

//  ===========================================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComuData() throws IOException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(3L).tipoVia("tipoV1").nombreVia("nombreV1")
                .municipio(new Municipio((short) 13, new Provincia((short) 3))).build();
        SpringOauthToken token = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                "luis@luis.com",
                "password5",
                PASSWORD_GRANT).execute().body();
        // NO existe el par (comunidad, usuario).
        Response<Comunidad> response = COMU_ENDPOINT.getComuData(HELPER.doBearerAccessTkHeader(token), comunidad.getC_Id()).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));

        token = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                "pedro@pedro.com",
                "password3",
                PASSWORD_GRANT).execute().body();
        assertThat(COMU_ENDPOINT.getComuData(HELPER.doBearerAccessTkHeader(token), comunidad.getC_Id()).execute().body(),
                is(comunidad));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidades_1() throws SQLException, IOException
    {
        // Exige comunidadDao.searchThree. Dos ocurrencias en DB que se ajustan a la regla 3.

        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("de la Mujer de la Plazuela")
                .numero((short) 10)
                .sufijoNumero("Bis")
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, USER_JUAN, "portal1", "esc2",
                "planta3", "puerta12", PROPIETARIO.function);

        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(userComu);

        // Datos de comunidad de búsqueda.
        Comunidad comunidadSearch = new Comunidad.ComunidadBuilder()
                .tipoVia("Ronda")
                .nombreVia("de la Plazuela")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = COMU_ENDPOINT.searchComunidades(comunidadSearch).execute().body();

        // Sólo devuelve la primera ocurrencia, porque se ajusta a la regla_1 de búsqueda.
        assertThat(comunidades.size(), is(1));

        assertThat(comunidades.get(0).getNombreComunidad(), is("Ronda de la Plazuela 10 bis"));
        assertThat(comunidades.get(0).getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getMunicipio().getProvincia().getNombre(), is("Albacete"));
        assertThat(comunidades.get(0).getMunicipio().getCodInProvincia(), is((short) 52));
        assertThat(comunidades.get(0).getMunicipio().getNombre(), is("Motilleja"));
    }

    @Test
    public void testSearchComunidades_2() throws IOException
    {
        // NO existe comunidad en DB.
        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Rincón")
                .nombreVia("Inexistente")
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = COMU_ENDPOINT.searchComunidades(comunidad).execute().body();

        // Return a not null list with 0 items.
        assertThat(comunidades.size(), is(0));
    }

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({LocalDev.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class ComunidadControllerDevTest extends ComunidadControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({DbPre.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class ComunidadControllerPreTest extends ComunidadControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {RetrofitConfigurationPre.class,
            ServOneRepoConfiguration.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_PRE, Profiles.MAIL_PRE})
    @Category({AwsPre.class})
    public static class ComunidadControllerAwsTest extends ComunidadControllerTest {
    }
}