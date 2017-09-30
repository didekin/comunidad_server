package com.didekin.incidservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.incidservice.repository.IncidenciaManagerIf;
import com.didekin.incidservice.repository.UserManagerConnector;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.http.retrofit.IncidenciaServEndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithId;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doResolucion;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_lafuente;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
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

@SuppressWarnings({"unchecked", "Duplicates"})
abstract class IncidenciaControllerTest {

    private IncidenciaServEndPoints ENDPOINT;
    @Autowired
    private IncidenciaManagerIf incidenciaManager;
    @Autowired
    private RetrofitHandler retrofitHandler;
    @Autowired
    private UserManagerConnector connector;

    @Before
    public void setUp() throws Exception
    {
        ENDPOINT = retrofitHandler.getService(IncidenciaServEndPoints.class);
    }

//  ==============================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws EntityException, IOException
    {
        // Caso OK: cierra la incidencia sin añadir avance.
        // Premisas.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        assertThat(incidencia.getFechaCierre(), nullValue());
        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(null)
                .build();
        // Devuelve 2: no modifica avances.
        assertThat(ENDPOINT.closeIncidencia(tokenLuis(), resolucion).execute().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_1() throws IOException
    {
        // Caso OK: existe incidencia.
        assertThat(ENDPOINT.deleteIncidencia(tokenPedro(), 2L).execute().body(), is(1));
        /* Caso: no existe incidencia (es la incidencia borrada).*/
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_3() throws IOException
    {
        // Caso: incidencia con resolución abierta.
        assertThat(isIncidenciaFound(ENDPOINT.deleteIncidencia(tokenLuis(), 3L).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException, IOException
    {
        // Caso OK: usuario 'adm', con incidImportancia NO registrada, modifica incidencia e inserta importancia.
        // Premisas.
        assertThat(luis_plazuelas_10bis.hasAdministradorAuthority(), is(true));
        IncidImportancia incidImportancia0 = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia();
        // No hay registro: fechaAlta == null.
        assertThat(incidImportancia0.getFechaAlta(), nullValue());
        assertThat(incidImportancia0.getImportancia(), is((short) 0));
        // Data
        incidImportancia0 = new IncidImportancia.IncidImportanciaBuilder(
                new Incidencia.IncidenciaBuilder().copyIncidencia(incidImportancia0.getIncidencia()).descripcion("new_3_1").build())
                .copyIncidImportancia(incidImportancia0)
                .importancia((short) 1)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(tokenLuis(), incidImportancia0).execute().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException, IOException
    {
        /* Caso NOT OK: no existe token.*/
        Incidencia incidencia = doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "new_description", 2L, (short) 21);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 0).build();

        Response<Integer> response = ENDPOINT.modifyIncidImportancia("token_faked", incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException, IOException
    {
        // Caso OK: modifica resolucion y añade un avance: devuelve 2.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        // Nuevos datos.
        List<Avance> avances = new ArrayList<>(1);
        avances.add(new Avance.AvanceBuilder()
                .avanceDesc("avance3")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build());
        Timestamp fechaPrevNew = Timestamp.from(now().plus(5, ChronoUnit.MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .avances(avances)
                .build();
        assertThat(ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws IOException
    {
        // Caso UNAUTHORIZED_TX_TO_USER: usuario no ADM.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 4L).execute().body();
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(luis.getUserName(), calle_la_fuente_11.getC_Id()), is(false));
        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(1111)
                .build();

        Response<Integer> response = ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_3() throws IOException
    {
        // Caso USERCOMU_WRONG_INIT: el usuario no está asociado a la comunidad.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();
        try {
            incidenciaManager.getUsuarioConnector().checkUserInComunidad(paco.getUserName(), resolucion.getComunidadId());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(null)
                .costeEstimado(1111)
                .build();
        // Check.
        Response<Integer> response = ENDPOINT.modifyResolucion(tokenPaco(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_4() throws EntityException, IOException
    {
        // Caso OK: avance con descripción vacía; devuelve 1.
        Resolucion resolucion = ENDPOINT.seeResolucion(tokenLuis(), 3L).execute().body();

        // Nuevos datos.
        List<Avance> avances = new ArrayList<>(1);
        avances.add(new Avance.AvanceBuilder()
                .avanceDesc("").author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build());
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();
        assertThat(ENDPOINT.modifyResolucion(tokenLuis(), resolucion).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws IOException
    {
        // Caso OK.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                doIncidenciaWithId(pedro.getUserName(), 1L, ronda_plazuela_10bis.getC_Id(), (short) 24), pedro);

        IncidComment comment = IncidenciaTestUtils.doComment("newComment", incidUserComu.getIncidencia(), pedro);
        assertThat(ENDPOINT.regIncidComment(tokenPedro(), comment).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws IOException
    {
        // Caso EntityException: USERCOMU_WRONG_INIT.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                doIncidenciaWithId(luis.getUserName(), 5L, calle_plazuela_23.getC_Id(), (short) 24), luis);
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidUserComu.getIncidencia(), pedro);

        Response<Integer> response = ENDPOINT.regIncidComment(tokenPedro(), comment).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_3() throws IOException
    {
        // Caso: la incidencia no existe en BD.
        IncidenciaUser incidUserComu = IncidenciaTestUtils.doIncidenciaUser(
                doIncidenciaWithId(luis.getUserName(), 999L, calle_plazuela_23.getC_Id(), (short) 24), luis);
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidUserComu.getIncidencia(), luis);
        Response<Integer> response = ENDPOINT.regIncidComment(tokenLuis(), comment).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_4() throws EntityException, IOException
    {
        // Caso: incidencia está cerrada.
        Incidencia incidencia = ENDPOINT.seeIncidsClosedByComu(tokenPaco(), 6L).execute().body().get(0).getIncidencia();
        assertThat(isIncidenciaFound(ENDPOINT.seeIncidImportancia(tokenPedro(), incidencia.getIncidenciaId()).
                execute()), is(false));
        IncidComment comment = IncidenciaTestUtils.doComment("Comment_DESC", incidencia, pedro);
        /* Incidencia no encontrada en consulta: está cerrada.*/
        assertThat(isIncidenciaFound(ENDPOINT.regIncidComment(tokenPedro(), comment).execute()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws EntityException, IOException
    {
        // Caso OK: usuario NO adm registrado en comunidad. No existe registro previo de incidencia.
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(luis.getUserName(), 4L), is(false));
        // Data.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(luis.getUserName(), "incidencia_6_4", calle_plazuela_23.getC_Id(), (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 3)
                .build();
        // Exec and check: inserta incidencia e incidenciImportancia.
        assertThat(ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws EntityException, IOException
    {
        // Caso: no existe la comunidad asociada la incidencia.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(luis.getUserName(), "incidencia_6_4", 999L, (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .build();

        Response<Integer> response = ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws EntityException, IOException
    {
        // Caso: incidencia ya dada de alta en BD. Registro devuelve '1', en lugar de '2'.
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(tokenLuis(), 3L).execute().body().getIncidImportancia().getIncidencia();
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(luis_plazuelas_10bis)
                .importancia((short) 2).build();
        assertThat(ENDPOINT.regIncidImportancia(tokenLuis(), incidImportancia).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_1() throws IOException
    {
        // Caso OK.
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 2L, pedro_lafuente.getComunidad().getC_Id(), (short) 22);
        Resolucion resolucion = doResolucion(incidencia, pedro.getUserName(), "resol_incid_2_2", 1111, now().plus(12, DAYS));
        assertThat(ENDPOINT.regResolucion(tokenPedro(), resolucion).execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_2() throws IOException
    {
        // Caso: usuario sin funciones administrador.
        // Preconditions
        assertThat(connector.checkAuthorityInComunidad(luis.getUserName(), calle_plazuela_23.getC_Id()), is(false));
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 5L, calle_plazuela_23.getC_Id(), (short) 22);
        Resolucion resolucion = doResolucion(incidencia, luis.getUserName(), "resol_incid_5_4", 22222, now().plus(12, DAYS));
        Response<Integer> response = ENDPOINT.regResolucion(tokenLuis(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_3() throws IOException
    {
        // Caso: usuarioComunidad no relacionado con comunidad de la incidencia.
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 5L, calle_plazuela_23.getC_Id(), (short) 22);
        Resolucion resolucion = doResolucion(incidencia, luis.getUserName(), "resol_incid_5_4", 22222, now().plus(12, DAYS));
        Response<Integer> response = ENDPOINT.regResolucion(tokenPedro(), resolucion).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_1() throws IOException
    {
        /* Caso Ok.*/
        IncidAndResolBundle bundle = ENDPOINT.seeIncidImportancia(tokenPedro(), ronda_plazuela_10bis.getC_Id()).execute().body();
        IncidImportancia incidImportancia = bundle.getIncidImportancia();
        assertThat(incidImportancia, hasProperty("importancia", is((short) 2)));
        assertThat(bundle.hasResolucion(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsClosedByComu_1() throws EntityException, InterruptedException, IOException
    {
        List<IncidenciaUser> incidencias = ENDPOINT.seeIncidsClosedByComu(tokenPaco(), UsuarioTestUtils.calle_olmo_55.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws EntityException, InterruptedException, IOException
    {
        // Caso OK: comunidad con 2 incidencias abiertas.
        List<IncidenciaUser> incidencias = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), ronda_plazuela_10bis.getC_Id()).execute().body();
        assertThat(incidencias.size(), is(2));
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_4() throws IOException
    {
        // Caso: usuario no relacionado con la comunidad.
        Response<List<IncidenciaUser>> response = ENDPOINT.seeIncidsOpenByComu(tokenPedro(), calle_plazuela_23.getC_Id()).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws IOException
    {
        // Caso OK: resolución con avances.
        assertThat(ENDPOINT.seeResolucion(tokenPedro(), 3L).execute().body().getAvances().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2() throws IOException
    {
        // Caso: usuario no asociado a la comunidad.
        Response<Resolucion> response = ENDPOINT.seeResolucion(tokenPaco(), 3L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1() throws IOException
    {
        // Caso OK.
        assertThat(ENDPOINT.seeUserComusImportancia(tokenPedro(), 4L).execute().body().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
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

//    ================================== HELPER METHODS ==================================

    private String tokenPedro() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(pedro.getUserName(), pedro.getPassword());
    }

    private String tokenLuis() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(luis.getUserName(), luis.getPassword());
    }

    private String tokenPaco() throws IOException
    {
        return new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(UsuarioTestUtils.paco.getUserName(), UsuarioTestUtils.paco.getPassword());
    }

    private boolean isIncidenciaFound(Response<?> responseEndPoint) throws IOException
    {
        assertThat(responseEndPoint.isSuccessful(), is(false));
        return !retrofitHandler.getErrorBean(responseEndPoint).getMessage().equals(INCIDENCIA_NOT_FOUND.getHttpMessage());
    }
}
