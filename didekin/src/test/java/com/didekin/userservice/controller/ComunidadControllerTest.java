package com.didekin.userservice.controller;


import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.springprofile.Profiles;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UserMockRepoConfiguration;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.http.retrofit.HttpHandler;
import com.didekinlib.model.entidad.Domicilio;
import com.didekinlib.model.entidad.Municipio;
import com.didekinlib.model.entidad.Provincia;
import com.didekinlib.model.entidad.comunidad.Comunidad;
import com.didekinlib.model.entidad.comunidad.http.ComunidadEndPoints;
import com.didekinlib.model.relacion.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.relacion.usuariocomunidad.http.UsuarioComunidadEndPoints;

import org.junit.After;
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

import java.util.List;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
@SuppressWarnings("ConstantConditions")
public abstract class ComunidadControllerTest {

    private ComunidadEndPoints COMU_ENDPOINT;
    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    UsuarioManager usuarioManager;
    @Autowired
    private UserMockManager userMockManager;

    @Before
    public void setUp()
    {
        COMU_ENDPOINT = retrofitHandler.getService(ComunidadEndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

    @After
    public void clear()
    {
    }

//  ===========================================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComuData()
    {
        // Premisa: usuario en comunidad.
        assertThat(usuarioManager.isUserInComunidad(pedro.getUserName(), calle_el_escorial.getId()), is(true));
        COMU_ENDPOINT
                .getComuData(
                        userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName()),
                        calle_el_escorial.getId()
                ).map(Response::body).test().assertValue(calle_el_escorial);
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearcRhComunidades_1()
    {
        // Exige comunidadDao.searchThree. Dos ocurrencias en DB que se ajustan a la regla 3.

        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("de la Mujer de la Plazuela")
                        .numero((short) 10)
                        .sufijoNumero("Bis")
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, USER_JUAN, "portal1", "esc2", "planta3", "puerta12");

        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(oneComponent_local_ES, userComu);

        // Datos de comunidad de búsqueda.
        Comunidad comunidadSearch = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Ronda")
                        .nombreVia("de la Plazuela")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = COMU_ENDPOINT.searchComunidades(comunidadSearch).blockingGet().body();

        // Sólo devuelve la primera ocurrencia, porque se ajusta a la regla_1 de búsqueda.
        assertThat(comunidades.size(), is(1));

        assertThat(comunidades.get(0).getDomicilio().getDomicilioStr(), is("Ronda de la Plazuela 10 bis"));
        assertThat(comunidades.get(0).getDomicilio().getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getProvincia().getNombre(), is("Albacete"));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getCodInProvincia(), is((short) 52));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getNombre(), is("Motilleja"));
    }

    @Test
    public void testSearchComunidades_2()
    {
        // NO existe comunidad en DB.
        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Rincón")
                        .nombreVia("Inexistente")
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        // Return a not null list with 0 items.
        assertThat(COMU_ENDPOINT.searchComunidades(comunidad).blockingGet().body().size(), is(0));
    }

//  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class, RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class, DbPre.class})
    @DirtiesContext
    public static class ComunidadCtrlerDevTest extends ComunidadControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class, UserMockRepoConfiguration.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class ComunidadCtrlerAwsTest extends ComunidadControllerTest {
    }
}