package com.didekin.incidservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekinlib.http.retrofit.IncidenciaServEndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioEndPoints;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.EOFException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

import static com.didekin.incidservice.testutils.IncidenciaTestUtils.calle_la_fuente_11;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.RESOLUCION_DUPLICATE;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 01/05/16
 * Time: 20:07
 * <p>
 * This class contains the most restrictive set of tests that can be run in all the environments.
 * Subclasses:
 * - IncidenciaControlerAutowireTest: set of tests that requires a full application context initialization,
 * for autowiring controllers.
 * - IncidenciaControllerAwsTest: set of test executed in client mode (server instantiated in AWS) and, therefore,
 * cannot autowire controllers in the tests.
 */

@SuppressWarnings("unchecked")
abstract class IncidenciaControllerTest {

    IncidenciaServEndPoints ENDPOINT;
    UsuarioEndPoints USER_ENDPOINT;

    @Autowired
    RetrofitHandler retrofitHandler;

    @Before
    public void setUp() throws Exception
    {
        ENDPOINT = retrofitHandler.getService(IncidenciaServEndPoints.class);
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
    }

//  ==============================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws EntityException, IOException
    {
        // Caso OK: añadimos un avance y cerramos la incidservice.
        // Premisas.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        assertThat(resolucion.getAvances().size(), is(2));
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        assertThat(incidencia.getFechaCierre(), nullValue());

        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance3").userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        Timestamp fechaPrevNew = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .avances(avances)
                .build();

        assertThat(ENDPOINT.closeIncidencia(tokenLuis(), resolucion).execute().body(), is(3));
        resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(3));
        // No encuentra la incidservice porque ya está cerrada.
        assertThat(isIncidenciaFound(ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_2() throws EntityException, IOException
    {
        // Caso OK: cierra la incidservice sin añadir avance.
        // Premisas.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        assertThat(resolucion.getAvances().size(), is(2));
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        assertThat(incidencia.getFechaCierre(), nullValue());

        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("").userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();
        // Devuelve 2: no modifica avances.
        assertThat(ENDPOINT.closeIncidencia(tokenLuis(), resolucion).execute().body(), is(2));
        resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(2));
        // No encuentra la incidservice porque ya está cerrada.
        assertThat(isIncidenciaFound(ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_1() throws IOException
    {
        // Caso OK: existe incidservice.
        assertThat(ENDPOINT.deleteIncidencia(tokenPedro(), 2L).execute().body(), is(1));

        /* Caso: no existe incidservice (es la incidservice borrada).*/
        assertThat(isIncidenciaFound(ENDPOINT.deleteIncidencia(tokenLuis(), 2L).execute()), is(false));
    }

    @Test
    public void testDeleteIncidencia_2() throws IOException
    {
        // Caso: no existe token.
        Response<Integer> response = ENDPOINT.deleteIncidencia("token_faked", 2L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_3() throws IOException
    {
        // Caso: incidservice con resolución abierta.
        assertThat(isIncidenciaFound(ENDPOINT.deleteIncidencia(tokenLuis(), 3L).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException, IOException
    {
        // Caso OK: usuario 'adm', con incidImportancia registrada, modifica incidservice e importancia.
        // Valores anteriores: ambito 22, importancia 1, descripción 'incidencia_2_2'.

        Incidencia incidencia = doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "new_description", calle_la_fuente_11.getC_Id(), (short) 33);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 4)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(tokenPedro(), incidImportancia).execute().body(), is(2));

        Incidencia incidenciaDB = ENDPOINT.seeIncidImportancia(tokenPedro(), incidencia.getIncidenciaId()).execute().body().getIncidImportancia().getIncidencia();
        assertThat(incidenciaDB, allOf(
                hasProperty("incidenciaId", is(incidencia.getIncidenciaId())),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 33))),
                hasProperty("descripcion", is("new_description")))
        );

        IncidImportancia incidImportanciaDB = ENDPOINT.seeIncidImportancia(tokenPedro(), incidencia.getIncidenciaId()).execute().body().getIncidImportancia();
        assertThat(incidImportanciaDB, allOf(
                hasProperty("incidencia", hasProperty("incidenciaId", is(incidencia.getIncidenciaId()))),
                hasProperty("importancia", is((short) 4))
        ));
        assertThat(incidImportanciaDB.getUserComu().hasAdministradorAuthority(), is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1_B() throws EntityException, IOException
    {
        // Caso OK: usuario 'adm', con incidImportancia NO registrada, modifica incidencia e inserta importancia.
        // Premisas.
        assertThat(IncidenciaTestUtils.luis_plazuelas_10bis.hasAdministradorAuthority(), is(true));
        IncidImportancia incidImportancia0 = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia();
        // No hay registro: fechaAlta == null.
        assertThat(incidImportancia0.getFechaAlta(), nullValue());
        assertThat(incidImportancia0.getImportancia(), is((short) 0));
        assertThat(incidImportancia0.getIncidencia(), allOf(
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("descripcion", is("incidencia_3_1"))
        ));

        incidImportancia0 = new IncidImportancia.IncidImportanciaBuilder(
                new Incidencia.IncidenciaBuilder().copyIncidencia(incidImportancia0.getIncidencia()).descripcion("new_3_1").build())
                .copyIncidImportancia(incidImportancia0)
                .importancia((short) 1)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(tokenLuis(), incidImportancia0).execute().body(), is(2));
        incidImportancia0 = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia();
        assertThat(incidImportancia0.getFechaAlta(), notNullValue());
        assertThat(incidImportancia0.getImportancia(), is((short) 1));
        assertThat(incidImportancia0.getIncidencia(), hasProperty("descripcion", is("new_3_1")));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException, IOException
    {
        // Caso OK: usuario 'adm', con incidImportancia registrada, que sólo modifica incidencia.
        Incidencia incidencia = doIncidenciaWithIdDescUsername("pedro@pedro.com", 4L, "new_desc", calle_la_fuente_11.getC_Id(), (short) 44);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).build();
        // Sólo modifica incidencia.
        assertThat(ENDPOINT.modifyIncidImportancia(tokenPedro(), incidImportancia).execute().body(), is(1));
        IncidImportancia incidImportanciaDB = ENDPOINT.seeIncidImportancia(tokenPedro(), incidencia.getIncidenciaId()).execute().body().getIncidImportancia();
        assertThat(incidImportanciaDB.getIncidencia().getDescripcion(), is("new_desc"));
        assertThat(incidImportanciaDB.getIncidencia().getAmbitoIncidencia().getAmbitoId(), is((short) 44));
        assertThat(incidImportanciaDB.getImportancia(), is((short) 0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_3() throws EntityException, IOException
    {
        // Caso OK: usuario no 'adm', ni iniciador modifica SÓLO importancia.
        IncidImportancia incidImportancia_0 = ENDPOINT.seeIncidImportancia(tokenLuis(), 4L).execute().body().getIncidImportancia();
        // Premisas.
        assertThat(incidImportancia_0.getIncidencia().getDescripcion(), is("incidencia_4_2"));
        assertThat(incidImportancia_0.getImportancia(), is((short) 2));
        assertThat(incidImportancia_0.getUserComu().hasAdministradorAuthority(), is(false));
        assertThat(incidImportancia_0.isIniciadorIncidencia(), is(false));

        Incidencia incidenciaIn_0 = new Incidencia.IncidenciaBuilder().copyIncidencia(incidImportancia_0.getIncidencia())
                .descripcion("new_4_2")
                .build();
        IncidImportancia incidImportancia_0_2 = new IncidImportancia.IncidImportanciaBuilder(incidenciaIn_0)
                .importancia((short) 1)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(tokenLuis(), incidImportancia_0_2).execute().body(), is(1));
        incidImportancia_0 = ENDPOINT.seeIncidImportancia(tokenLuis(), 4L).execute().body().getIncidImportancia();
        // Modificación.
        assertThat(incidImportancia_0.getImportancia(), is((short) 1));
        assertThat(incidImportancia_0.getIncidencia().getDescripcion(), is("incidencia_4_2"));


        // Caso OK: usuario iniciador, sin autoridad 'adm', modifica incidencia e importancia.
        IncidImportancia incidImportancia_1 = ENDPOINT.seeIncidImportancia(tokenLuis(), 5L).execute().body().getIncidImportancia();
        // Premisas.
        assertThat(incidImportancia_1.getIncidencia().getDescripcion(), is("incidencia_5_4"));
        assertThat(incidImportancia_1.getImportancia(), is((short) 4));
        assertThat(incidImportancia_1.getUserComu().hasAdministradorAuthority(), is(false));
        assertThat(incidImportancia_1.isIniciadorIncidencia(), is(true));

        Incidencia incidenciaIn = new Incidencia.IncidenciaBuilder().copyIncidencia(incidImportancia_1.getIncidencia())
                .descripcion("new")
                .build();
        IncidImportancia incidImportancia_2 = new IncidImportancia.IncidImportanciaBuilder(incidenciaIn)
                .importancia((short) 2)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(tokenLuis(), incidImportancia_2).execute().body(), is(2));
        incidImportancia_1 = ENDPOINT.seeIncidImportancia(tokenLuis(), 5L).execute().body().getIncidImportancia();
        // Modificación.
        assertThat(incidImportancia_1.getImportancia(), is((short) 2));
        assertThat(incidImportancia_1.getIncidencia().getDescripcion(), is("new"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_4() throws EntityException, IOException
    {
        // Caso NOT OK: la incidencia no existe; usuario adm y autor.
        Incidencia incidencia = doIncidenciaWithIdDescUsername("pedro@pedro.com", 999L, "new_description", 2L, (short) 21);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .build();

        assertThat(isIncidenciaFound(ENDPOINT.modifyIncidImportancia(tokenPedro(), incidImportancia).execute()), is(false));

        // Caso NOT OK: usuario no adm, ni iniciador. Igual que el anterior.
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .build();
        assertThat(isIncidenciaFound(ENDPOINT.modifyIncidImportancia(tokenLuis(), incidImportancia).execute()), is(false));

        /* Caso NOT OK: la incidencia está cerrada; igual que si no existiera.*/
        incidencia = ENDPOINT.seeIncidsClosedByComu(tokenPaco(), 6L).execute().body().get(0).getIncidencia();
        assertThat(incidencia.getFechaCierre().after(incidencia.getFechaAlta()), is(true));
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 22)
                .build();
        assertThat(isIncidenciaFound(ENDPOINT.modifyIncidImportancia(tokenPaco(), incidImportancia).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_5() throws EntityException, IOException
    {
        /* Caso NOT OK: no existe token.*/
        Incidencia incidencia = doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "new_description", 2L, (short) 21);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 0).build();

        Response<Integer> response = ENDPOINT.modifyIncidImportancia("token_faked", incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_6() throws EntityException, IOException
    {
        // Caso OK: usuario sin perfil administrador no tiene registro previo de incidImportancia.
        // Premisas:
        assertThat(IncidenciaTestUtils.paco_olmo.hasAdministradorAuthority(), is(false));
        IncidImportancia incidImportancia = ENDPOINT.seeIncidImportancia(tokenPaco(), 7L).execute().body().getIncidImportancia();
        Incidencia incidencia = incidImportancia.getIncidencia();
        // IncidImportancia con importancia == 0 : condición de 'no tiene incidImportancia en BD'.
        assertThat(incidImportancia.getImportancia(), is((short) 0));

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(
                new Incidencia.IncidenciaBuilder().copyIncidencia(incidencia).build())
                .importancia((short) 4)
                .build();

        // Devuelve 2 porque es usuario iniciador y entra a moficar incidencia, aunque no haya cambios en ella.
        assertThat(ENDPOINT.modifyIncidImportancia(tokenPaco(), incidImportancia).execute().body(), CoreMatchers.is(2));
        incidImportancia = ENDPOINT.seeIncidImportancia(tokenPaco(), incidencia.getIncidenciaId()).execute().body().getIncidImportancia();
        assertThat(incidImportancia.getImportancia(), is((short) 4));
        assertThat(incidImportancia.getUserComu(), Is.is(IncidenciaTestUtils.paco_olmo));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException, IOException
    {
        // Caso OK: devuelve 2.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();

        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance3").userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        Timestamp fechaPrevNew = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .avances(avances)
                .build();
        assertThat(ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws IOException
    {
        // Caso INCIDENCIA_NOT_FOUND.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance3")
                .userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        resolucion = new Resolucion.ResolucionBuilder(
                new Incidencia.IncidenciaBuilder().copyIncidencia(resolucion.getIncidencia())
                        // Cambiamos el id de la incidencia.
                        .incidenciaId(999L)
                        .build())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();

        Response<Integer> response = ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_3() throws IOException
    {
        // Caso UNAUTHORIZED_TX_TO_USER.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 4L).execute().body();

        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(1111)
                .build();

        Response<Integer> response = ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_4() throws IOException
    {
        // Caso ROLES_NOT_FOUND:
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();

        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(null)
                .costeEstimado(1111)
                .build();

        Response<Integer> response = ENDPOINT.modifyResolucion(tokenPaco(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(ROLES_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_5() throws EntityException, IOException
    {
        // Caso OK: avance con descripción vacía; devuelve 1.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();

        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("").userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();
        assertThat(ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws IOException
    {
        // Caso OK.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.pedro.getUserName(), 1L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 24), IncidenciaTestUtils.pedro);

        IncidComment comment = IncidenciaTestUtils.doComment("newComment", incidUserComu.getIncidencia(), IncidenciaTestUtils.pedro);
        assertThat(ENDPOINT.regIncidComment(tokenPedro(), comment).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws IOException
    {
        // Caso EntityException: USERCOMU_WRONG_INIT.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 5L, IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 24), IncidenciaTestUtils.luis);
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidUserComu.getIncidencia(), IncidenciaTestUtils.pedro);

        Response<Integer> response = ENDPOINT.regIncidComment(tokenPedro(), comment).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_3() throws IOException
    {
        // Caso: la incidencia no existe en BD.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 999L, IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 24), IncidenciaTestUtils.luis);
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidUserComu.getIncidencia(), IncidenciaTestUtils.luis);
        Response<Integer> response = ENDPOINT.regIncidComment(tokenLuis(), comment).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_4() throws EntityException, IOException
    {
        // Caso: incidencia está cerrada.
        Incidencia incidencia = ENDPOINT.seeIncidsClosedByComu(tokenPaco(), 6L).execute().body().get(0).getIncidencia();
        assertThat(isIncidenciaFound(ENDPOINT.seeIncidImportancia(tokenPedro(), incidencia.getIncidenciaId()).
                execute()), is(false));
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidencia, IncidenciaTestUtils.pedro);
        /* Incidencia no encontrada en consulta: está cerrada.*/
        assertThat(isIncidenciaFound(ENDPOINT.regIncidComment(tokenPedro(), comment).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws EntityException, IOException
    {
        // Caso OK: usuario registrado en comunidad. IncidImportancia completa.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "incidencia_6_4", IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 3)
                .build();
        assertThat(ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute().body(), is(2));

        long incidenciaId = ENDPOINT.seeIncidsOpenByComu(tokenLuis(), IncidenciaTestUtils.calle_plazuela_23.getC_Id())
                .execute().body().get(1).getIncidencia().getIncidenciaId();
        IncidImportancia incidImportanciaDB =
                ENDPOINT.seeIncidImportancia(tokenLuis(), incidenciaId).execute().body().getIncidImportancia();
        assertThat(incidImportanciaDB, allOf(
                hasProperty("incidencia", hasProperty("comunidad", Is.is(IncidenciaTestUtils.calle_plazuela_23))),
                hasProperty("importancia", is((short) 3)),
                hasProperty("userComu", allOf(
                        hasProperty("usuario", allOf(
                                hasProperty("uId", Is.is(IncidenciaTestUtils.luis.getuId())),
                                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName())),
                                hasProperty("alias", Is.is(IncidenciaTestUtils.luis.getAlias()))
                        )),
                        hasProperty("comunidad", hasProperty("c_Id", Is.is(IncidenciaTestUtils.calle_plazuela_23.getC_Id())))
                ))
        ));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws EntityException, IOException
    {
        // Caso: falla la restricción usuario está registrado en incidencia.comunidad.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.pedro.getUserName(), "incidencia_6_4", IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .build();

        Response<Integer> response = ENDPOINT.regIncidImportancia(tokenPedro(), incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws EntityException, IOException
    {
        // Caso: no existe la comunidad en la incidencia.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "incidencia_6_4", 999L, (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .build();

        Response<Integer> response = ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_4() throws EntityException, IOException
    {
        // Caso: no existe el usuario que intenta el registro de la incidencia.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "incidencia_6_4", 999L, (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .build();

        Response<Integer> response = ENDPOINT.regIncidImportancia("no_existo", incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_5() throws EntityException, IOException
    {
        // Caso: incidencia ya dada de alta en BD. Registro devuelve '1', en lugar de '2'.
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(IncidenciaTestUtils.luis_plazuelas_10bis)
                .importancia((short) 2).build();
        assertThat(ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_1() throws IOException
    {
        // Caso OK.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 2L, IncidenciaTestUtils.pedro_lafuente.getComunidad().getC_Id(), (short) 22);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.pedro.getUserName(), "resol_incid_2_2", 1111, Instant.now().plus(12, ChronoUnit.DAYS));
        assertThat(ENDPOINT.regResolucion(tokenPedro(), resolucion).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_2() throws IOException
    {
        // Caso: usuario sin funciones administrador.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 5L, IncidenciaTestUtils.luis_plazuela23.getComunidad().getC_Id(), (short) 22);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.luis.getUserName(), "resol_incid_5_4", 22222, Instant.now().plus(12, ChronoUnit.DAYS));
        Response<Integer> response = ENDPOINT.regResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_3() throws IOException
    {
        // Caso: usuarioComunidad no relacionado con comunidad de la incidencia.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 5L, IncidenciaTestUtils.luis_plazuela23.getComunidad().getC_Id(), (short) 22);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.luis.getUserName(), "resol_incid_5_4", 22222, Instant.now().plus(12, ChronoUnit.DAYS));
        Response<Integer> response = ENDPOINT.regResolucion(tokenPedro(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(ROLES_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_4() throws IOException
    {
        // Caso: no existe la incidencia.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.pedro.getUserName(), 999L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 22);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.luis.getUserName(), "resol_incid_5_4", 22222, Instant.now().plus(12, ChronoUnit.DAYS));
        Response<Integer> response = ENDPOINT.regResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_5() throws IOException
    {
        // Caso: resolución duplicada.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 2L, calle_la_fuente_11.getC_Id(), (short) 22);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.pedro.getUserName(), "resol_incid_2_2", 1111, Instant.now().plus(12, ChronoUnit.DAYS));
        assertThat(ENDPOINT.regResolucion(tokenPedro(), resolucion).execute().body(), is(1));

        Response<Integer> response = ENDPOINT.regResolucion(tokenPedro(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(RESOLUCION_DUPLICATE.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_1() throws IOException
    {
        // Caso OK.
        List<IncidComment> comments = ENDPOINT.seeCommentsByIncid(tokenLuis(), 1L).execute().body();
        assertThat(comments.size(), is(2));
        // Diferente usuario misma comunidad.
        comments = ENDPOINT.seeCommentsByIncid(tokenPedro(), 1L).execute().body();
        assertThat(comments.size(), is(2));
        // Diferente incidencia.
        comments = ENDPOINT.seeCommentsByIncid(tokenLuis(), 5L).execute().body();
        assertThat(comments.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_2() throws IOException
    {
        // Usuario no asociado a la comunidad de la incidencia.
        Response<List<IncidComment>> response = ENDPOINT.seeCommentsByIncid(tokenPedro(), 5L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_3() throws IOException
    {
        // No existe la incidencia; existe la comunidad.
        Response<List<IncidComment>> response = ENDPOINT.seeCommentsByIncid(tokenLuis(), 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_4() throws IOException
    {
        // Existe la incidencia; no tiene comentarios.
        List<IncidComment> comments = ENDPOINT.seeCommentsByIncid(tokenLuis(), 3L).execute().body();
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_1() throws IOException
    {
        /* Caso Ok.*/
        IncidAndResolBundle bundle = ENDPOINT.seeIncidImportancia(tokenPedro(), IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()).execute().body();
        IncidImportancia incidImportancia = bundle.getIncidImportancia();
        assertThat(incidImportancia, allOf(
                hasProperty("incidencia", allOf(
                        hasProperty("incidenciaId", is(1L)),
                        hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName())),
                        hasProperty("descripcion", is("incidencia_1_1")),
                        hasProperty("comunidad", is(new Comunidad.ComunidadBuilder().c_id(1L).build())),
                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 41)))
                )),
                hasProperty("userComu", allOf(
                        hasProperty("comunidad", hasProperty("c_Id", is(1L))),
                        hasProperty("usuario", hasProperty("userName", Is.is(IncidenciaTestUtils.pedro.getUserName()))),
                        hasProperty("roles", is(PRESIDENTE.function))
                )),
                hasProperty("importancia", is((short) 2))

        ));
        assertThat(bundle.hasResolucion(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_2() throws IOException
    {
        // 1. No existe incidencia en BD, existe usuario. Es irrelevante la relación usuario_comunidad.
        Response<IncidAndResolBundle> response = ENDPOINT.seeIncidImportancia(tokenPedro(), 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));

        // 2. Existe incidencia, existe usuario, no existe relación usuario_comunidad, ni, por lo tanto, usuario_incidencia.
        response = ENDPOINT.seeIncidImportancia(tokenPedro(), 5L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(ROLES_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_3() throws EntityException, IOException
    {
        // Caso: usuario.comunidad = incidencia.comunidad, pero no hay registro incidImportancia para el usuario.
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        assertThat(incidencia.getComunidad().equals(IncidenciaTestUtils.luis_plazuelas_10bis.getComunidad()), is(true));

        IncidAndResolBundle bundle = ENDPOINT.seeIncidImportancia(tokenLuis(), incidencia.getIncidenciaId()).execute().body();
        IncidImportancia incidImportancia = bundle.getIncidImportancia();
        assertThat(incidImportancia, AllOf.allOf(
                hasProperty("incidencia", is(incidencia)),
                hasProperty("importancia", is((short) 0)),
                hasProperty("fechaAlta", nullValue()),
                hasProperty("userComu", allOf(
                        hasProperty("usuario", allOf(
                                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName())),
                                hasProperty("uId", Is.is(IncidenciaTestUtils.luis.getuId())),
                                hasProperty("alias", Is.is(IncidenciaTestUtils.luis.getAlias()))
                        )),
                        hasProperty("comunidad", hasProperty("c_Id", is(incidencia.getComunidad().getC_Id()))),
                        hasProperty("roles", is(ADMINISTRADOR.function))
                ))
        ));
        assertThat(bundle.hasResolucion(), is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsClosedByComu_1() throws EntityException, InterruptedException, IOException
    {
        List<IncidenciaUser> incidencias = ENDPOINT.seeIncidsClosedByComu(tokenPaco(), IncidenciaTestUtils.calle_olmo_55.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(1));
        assertThat(incidencias.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_6_6")),
                hasProperty("ambitoIncidencia", is(new AmbitoIncidencia((short) 33))),
                hasProperty("importanciaAvg", is(0f)),
                hasProperty("incidenciaId", is(6L)),
                hasProperty("comunidad", Is.is(IncidenciaTestUtils.calle_olmo_55)),
                hasProperty("userName", Is.is(IncidenciaTestUtils.paco.getUserName())),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaCierre", notNullValue())
        ));
        assertThat(incidencias.get(0),
                hasProperty("usuario", allOf(
                        hasProperty("uId", Is.is(IncidenciaTestUtils.paco.getuId())),
                        hasProperty("userName", nullValue()),
                        hasProperty("alias", Is.is(IncidenciaTestUtils.paco.getAlias()))
                ))
        );
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws EntityException, InterruptedException, IOException
    {
        List<IncidenciaUser> incidencias = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(2));
        assertThat(incidencias.get(0).getFechaAltaResolucion(), nullValue());
        assertThat(incidencias.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_1_1")),
                hasProperty("ambitoIncidencia", is(new AmbitoIncidencia((short) 41))),
                hasProperty("importanciaAvg", is(1.5f)),
                hasProperty("incidenciaId", is(1L)),
                hasProperty("comunidad", Is.is(IncidenciaTestUtils.ronda_plazuela_10bis)),
                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName())),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaCierre", nullValue())
        ));
        assertThat(incidencias.get(0),
                hasProperty("usuario", allOf(
                        hasProperty("uId", Is.is(IncidenciaTestUtils.luis.getuId())),
                        hasProperty("userName", nullValue()),
                        hasProperty("alias", Is.is(IncidenciaTestUtils.luis.getAlias()))
                ))
        );
        assertThat(incidencias.get(1).getFechaAltaResolucion(), notNullValue());

        incidencias = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), calle_la_fuente_11.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(2));
        assertThat(incidencias.get(0).getFechaAltaResolucion(), nullValue());
        assertThat(incidencias.get(1).getFechaAltaResolucion(), notNullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2() throws EntityException, InterruptedException, IOException
    {
        // CASO OK: el usuario iniciador no es usuario ya de la aplicación.
        // Premisas.
        List<IncidenciaUser> incidencias = ENDPOINT.seeIncidsOpenByComu(tokenLuis(), IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(2));
        assertThat(incidencias.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_1_1")),
                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName()))
        ));
        assertThat(incidencias.get(0),
                hasProperty("usuario", hasProperty("alias", Is.is(IncidenciaTestUtils.luis.getAlias()))));

        // Borro al usuario iniciador de la icidencia 1.1.
        assertThat(USER_ENDPOINT.deleteUser(tokenLuis()).execute().body(), is(true));
        incidencias = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(2));
        assertThat(incidencias.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_1_1")),
                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName()))
        ));
        // Usuario ahora es null.
        assertThat(incidencias.get(0).getUsuario(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3() throws EntityException, IOException
    {
        // ComunidadId no existe.
        Response<List<IncidenciaUser>> response = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_4() throws IOException
    {
        // Caso: usuario no relacionado con la comunidad.
        Response<List<IncidenciaUser>> response = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), IncidenciaTestUtils.calle_plazuela_23.getC_Id()).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws IOException
    {
        // Caso OK: resolución con avances.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenPedro(), 3L).execute().body();
        assertThat(resolucion, allOf(
                Matchers.hasProperty("userName", CoreMatchers.is(IncidenciaTestUtils.pedro.getUserName())),
                Matchers.hasProperty("descripcion", CoreMatchers.is("plan_resol_3")),
                Matchers.hasProperty("costeEstimado", CoreMatchers.is(11)),
                Matchers.hasProperty("costeFinal", CoreMatchers.is(11)),
                Matchers.hasProperty("moraleja", CoreMatchers.is("moraleja_3")),
                Matchers.hasProperty("incidencia", CoreMatchers.is(new Incidencia.IncidenciaBuilder().incidenciaId(3L).build()))
        ));
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2() throws IOException
    {
        // Caso: la resolución no existe.
        Response<Resolucion> response = ENDPOINT.seeResolucion(tokenLuis(), 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_3() throws IOException
    {
        // Caso: usuario no asociado a la comunidad.
        Response<Resolucion> response = ENDPOINT.seeResolucion(tokenPaco(), 3L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_4() throws IOException
    {
        // Caso: la incidencia no tiene resolución.
        try {
            ENDPOINT.seeResolucion(tokenPedro(), 2L).execute().body();
            fail();
        } catch (EOFException e) {
            assertThat(e.getMessage().contains("End of input at line 1 column 1"), is(true));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1() throws IOException
    {
        // Caso OK.
        List<ImportanciaUser> importanciaUsers = ENDPOINT.seeUserComusImportancia(tokenPedro(), 4L).execute().body();
        assertThat(importanciaUsers.size(), is(2));
        assertThat(importanciaUsers.get(0), allOf(
                hasProperty("userAlias", CoreMatchers.is(IncidenciaTestUtils.luis.getAlias())),
                hasProperty("importancia", CoreMatchers.is((short) 2))
        ));
        assertThat(importanciaUsers.get(1), allOf(
                hasProperty("userAlias", CoreMatchers.is(IncidenciaTestUtils.pedro.getAlias())),
                hasProperty("importancia", CoreMatchers.is((short) 0))
        ));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_2() throws IOException
    {
        // Caso: usuario no pertenece a la comunidad.
        Response<List<ImportanciaUser>> response = ENDPOINT.seeUserComusImportancia(tokenPaco(), 4L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_3() throws IOException
    {
        // Caso: no existe la incidencia.
        Response<List<ImportanciaUser>> response = ENDPOINT.seeUserComusImportancia(tokenPedro(), 999L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

//    ================================== HELPER METHODS ==================================

    String tokenPedro() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader(IncidenciaTestUtils.pedro.getUserName(), IncidenciaTestUtils.pedro.getPassword());
    }

    String tokenLuis() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader(IncidenciaTestUtils.luis.getUserName(), IncidenciaTestUtils.luis.getPassword());
    }

    private String tokenPaco() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader(IncidenciaTestUtils.paco.getUserName(), IncidenciaTestUtils.paco.getPassword());
    }

    private boolean isIncidenciaFound(Response<?> responseEndPoint) throws IOException
    {
        assertThat(responseEndPoint.isSuccessful(), is(false));
        return !retrofitHandler.getErrorBean(responseEndPoint).getMessage().equals(INCIDENCIA_NOT_FOUND.getHttpMessage());
    }
}
