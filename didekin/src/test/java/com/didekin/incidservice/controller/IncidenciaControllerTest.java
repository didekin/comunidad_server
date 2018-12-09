package com.didekin.incidservice.controller;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.repository.ServiceException;
import com.didekin.common.springprofile.Profiles;
import com.didekin.incidservice.repository.IncidenciaManager;
import com.didekin.incidservice.repository.IncidenciaManagerConfiguration;
import com.didekin.incidservice.repository.UserManagerConnector;
import com.didekinlib.http.retrofit.HttpHandler;
import com.didekinlib.model.relacion.incidencia.dominio.Avance;
import com.didekinlib.model.relacion.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.relacion.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.relacion.incidencia.dominio.IncidComment;
import com.didekinlib.model.relacion.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.relacion.incidencia.dominio.Incidencia;
import com.didekinlib.model.relacion.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.relacion.incidencia.dominio.Resolucion;
import com.didekinlib.model.relacion.incidencia.http.IncidenciaServEndPoints;
import com.didekinlib.model.usuario.Usuario;

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
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doComment;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidencia;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaUser;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithId;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doResolucion;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_olmo_55;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_lafuente;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.model.relacion.incidencia.http.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
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
 * - IncidenciaCtrlerAwsTest: set of test blockingGetd in client mode (server instantiated in AWS) and, therefore,
 * cannot autowire controllers in the tests.
 */

@SuppressWarnings("ConstantConditions")
abstract class IncidenciaControllerTest {

    private IncidenciaServEndPoints ENDPOINT;
    @Autowired
    private IncidenciaManager incidenciaManager;
    @Autowired
    private HttpHandler retrofitHandler;

    @Before
    public void setUp()
    {
        ENDPOINT = retrofitHandler.getService(IncidenciaServEndPoints.class);
    }

    private UserManagerConnector getUserConnector()
    {
        return incidenciaManager.getUsuarioConnector();
    }

//  ==============================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws ServiceException
    {
        // Caso OK: cierra la incidencia sin añadir avance.
        // Premisas.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        Resolucion resolucion = ENDPOINT.seeResolucion(accessToken, 3L).blockingGet().body();
        Incidencia incidencia = ENDPOINT.seeIncidImportancia(accessToken, 3L).blockingGet().body().getIncidImportancia().getIncidencia();
        assertThat(incidencia.getFechaCierre(), nullValue());
        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(null)
                .build();
        // Devuelve 2: no modifica avances.
        assertThat(ENDPOINT.closeIncidencia(accessToken, resolucion).blockingGet().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_2() throws ServiceException, IOException
    {
        Resolucion resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), 4L);
        assertThat(incidenciaManager.closeIncidencia(pedro.getUserName(), resolucion), is(2));
        // Premisas: incidencia ya cerrada.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(pedro.getUserName());
        Response<Integer> response = ENDPOINT.closeIncidencia(accessToken, resolucion).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_1() throws IOException
    {
        // Caso OK: existe incidencia.
        assertThat(ENDPOINT.deleteIncidencia(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                2L)
                .blockingGet().body(), is(1));
        /* Caso: no existe incidencia (es la incidencia borrada).*/
        assertThat(isIncidenciaFound(ENDPOINT.deleteIncidencia(
                getUserConnector().insertTokenGetHeaderStr(luis.getUserName()),
                2L)
                .blockingGet()), is(false));
    }

    @Test
    public void testDeleteIncidencia_2() throws IOException
    {
        // Caso: token inválido.
        Response<Integer> response = ENDPOINT.deleteIncidencia("token_faked", 2L).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(TOKEN_ENCRYP_DECRYP_ERROR.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_3() throws IOException
    {
        // Caso: incidencia con resolución abierta.
        assertThat(isIncidenciaFound(
                ENDPOINT.deleteIncidencia(
                        getUserConnector().insertTokenGetHeaderStr(luis.getUserName()),
                        3L)
                        .blockingGet()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws ServiceException
    {
        // Caso OK: usuario 'adm', con incidImportancia NO registrada, modifica incidencia e inserta importancia.
        // Premisas.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        IncidImportancia incidImportancia0 = ENDPOINT.seeIncidImportancia(accessToken, 3L).blockingGet().body().getIncidImportancia();
        // No hay registro: fechaAlta == null.
        assertThat(incidImportancia0.getFechaAlta(), nullValue());
        assertThat(incidImportancia0.getImportancia(), is((short) 0));
        // Data
        incidImportancia0 = new IncidImportancia.IncidImportanciaBuilder(
                new Incidencia.IncidenciaBuilder().copyIncidencia(incidImportancia0.getIncidencia()).descripcion("new_3_1").build())
                .copyIncidImportancia(incidImportancia0)
                .importancia((short) 1)
                .build();

        assertThat(ENDPOINT.modifyIncidImportancia(accessToken, incidImportancia0).blockingGet().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws ServiceException, IOException
    {
        /* Caso NOT OK: token inválido.*/
        Incidencia incidencia =
                doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "new_description", 2L, (short) 21);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 0).build();

        Response<Integer> response = ENDPOINT.modifyIncidImportancia("token_faked", incidImportancia).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(TOKEN_ENCRYP_DECRYP_ERROR.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws ServiceException
    {
        // Caso OK: modifica resolucion y añade un avance: devuelve 2.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        Resolucion resolucion = ENDPOINT.seeResolucion(accessToken, 3L).blockingGet().body();// Nuevos datos.
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
        assertThat(ENDPOINT.modifyResolucion(accessToken, resolucion).blockingGet().body(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_4() throws ServiceException
    {
        // Caso OK: avance con descripción vacía; devuelve 1.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        Resolucion resolucion = ENDPOINT.seeResolucion(accessToken, 3L).blockingGet().body();

        // Nuevos datos.
        List<Avance> avances = new ArrayList<>(1);
        avances.add(new Avance.AvanceBuilder()
                .avanceDesc("").author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build());
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();
        assertThat(ENDPOINT.modifyResolucion(accessToken, resolucion).blockingGet().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1()
    {
        // Caso OK.
        IncidenciaUser incidUserComu =
                doIncidenciaUser(doIncidenciaWithId(pedro.getUserName(), 1L, ronda_plazuela_10bis.getId(), (short) 24), pedro);

        IncidComment comment = doComment("newComment", incidUserComu.getIncidencia(), pedro);
        assertThat(ENDPOINT.regIncidComment(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                comment)
                .blockingGet().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws IOException
    {
        // Caso ServiceException: USERCOMU_WRONG_INIT.
        IncidenciaUser incidUserComu =
                doIncidenciaUser(doIncidenciaWithId(luis.getUserName(), 5L, calle_plazuela_23.getId(), (short) 24), luis);
        IncidComment comment = doComment("Comment_DESC", incidUserComu.getIncidencia(), pedro);

        Response<Integer> response = ENDPOINT.regIncidComment(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                comment)
                .blockingGet();
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
        IncidenciaUser incidUserComu = doIncidenciaUser(
                doIncidenciaWithId(luis.getUserName(), 999L, calle_plazuela_23.getId(), (short) 24), luis);
        IncidComment comment = doComment("Comment_DESC", incidUserComu.getIncidencia(), luis);
        Response<Integer> response = ENDPOINT.regIncidComment(
                getUserConnector().insertTokenGetHeaderStr(luis.getUserName()),
                comment)
                .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_4() throws ServiceException
    {
        // Caso: incidencia está cerrada.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(paco.getUserName());
        Incidencia incidencia = ENDPOINT.seeIncidsClosedByComu(accessToken, 6L).blockingGet().body().get(0).getIncidencia();
        IncidComment comment = doComment("Comment_DESC", incidencia, pedro);
        assertThat(ENDPOINT.regIncidComment(accessToken, comment).blockingGet().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws ServiceException
    {
        // Caso OK: usuario NO adm registrado en comunidad. No existe registro previo de incidencia.
        assertThat(getUserConnector().checkAuthorityInComunidad(luis.getUserName(), 4L), is(false));
        // Data.
        Incidencia incidencia = doIncidencia(luis.getUserName(), "incidencia_6_4", calle_plazuela_23.getId(), (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 3)
                .build();
        // Exec and check: inserta incidencia e incidenciImportancia.
        assertThat(
                ENDPOINT.regIncidImportancia(getUserConnector().insertTokenGetHeaderStr(luis.getUserName()),
                        incidImportancia).blockingGet().body(),
                is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws ServiceException, IOException
    {
        // Caso: no existe la comunidad asociada la incidencia.
        Incidencia incidencia = doIncidencia(luis.getUserName(), "incidencia_6_4", 999L, (short) 14);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .build();

        Response<Integer> response =
                ENDPOINT.regIncidImportancia(
                        getUserConnector().insertTokenGetHeaderStr(luis.getUserName()),
                        incidImportancia
                ).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws ServiceException
    {
        // Caso: incidencia ya dada de alta en BD. Registro devuelve '1', en lugar de '2'.
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        Incidencia incidencia = ENDPOINT
                .seeIncidImportancia(accessToken, 3L)
                .blockingGet().body().getIncidImportancia().getIncidencia();
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(luis_plazuelas_10bis)
                .importancia((short) 2).build();
        assertThat(ENDPOINT.regIncidImportancia(accessToken, incidImportancia).blockingGet().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_1()
    {
        // Caso OK.
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 2L, pedro_lafuente.getEntidad().getId(), (short) 22);
        Resolucion resolucion = doResolucion(incidencia, pedro.getUserName(),
                "resol_incid_2_2",
                1111,
                now().plus(12, DAYS));
        assertThat(ENDPOINT.
                        regResolucion(getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()), resolucion)
                        .blockingGet().body(),
                is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_1()
    {
        // Caso OK.
        final String tokenLuis = getUserConnector().insertTokenGetHeaderStr(luis.getUserName());
        List<IncidComment> comments = ENDPOINT.seeCommentsByIncid(tokenLuis, 1L).blockingGet().body();
        assertThat(comments.size(), is(2));
        // Diferente usuario misma comunidad.
        comments =
                ENDPOINT.seeCommentsByIncid(getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()), 1L)
                        .blockingGet().body();
        assertThat(comments.size(), is(2));
        // Diferente incidencia.
        comments = ENDPOINT.seeCommentsByIncid(tokenLuis, 5L).blockingGet().body();
        assertThat(comments.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_2() throws IOException
    {
        // Usuario no asociado a la comunidad de la incidencia.
        Response<List<IncidComment>> response =
                ENDPOINT.seeCommentsByIncid(getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()), 5L)
                        .blockingGet();
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
        Response<List<IncidComment>> response =
                ENDPOINT.seeCommentsByIncid(getUserConnector().insertTokenGetHeaderStr(luis.getUserName()), 999L)
                        .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_4()
    {
        // Existe la incidencia; no tiene comentarios.
        List<IncidComment> comments =
                ENDPOINT.seeCommentsByIncid(getUserConnector().insertTokenGetHeaderStr(luis.getUserName()), 3L)
                        .blockingGet().body();
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_1()
    {
        /* Caso Ok.*/
        IncidAndResolBundle bundle =
                ENDPOINT.
                        seeIncidImportancia(
                                getUserConnector()
                                        .insertTokenGetHeaderStr(pedro.getUserName()), ronda_plazuela_10bis.getId()
                        )
                        .blockingGet().body();
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
        final String accessToken = getUserConnector().insertTokenGetHeaderStr(pedro.getUserName());
        Response<IncidAndResolBundle> response =
                ENDPOINT.seeIncidImportancia(accessToken, 999L).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(INCIDENCIA_NOT_FOUND.getHttpMessage()));

        // 2. Existe incidencia, existe usuario, no existe relación usuario_comunidad, ni, por lo tanto, usuario_incidencia.
        response = ENDPOINT.seeIncidImportancia(accessToken, 5L).blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsClosedByComu_1() throws ServiceException
    {
        List<IncidenciaUser> incidencias =
                ENDPOINT.seeIncidsClosedByComu(
                        getUserConnector().insertTokenGetHeaderStr(paco.getUserName()), calle_olmo_55.getId()
                ).blockingGet().body();
        assertThat(incidencias.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsClosedByComu_2() throws ServiceException
    {
        List<IncidenciaUser> incidencias =
                ENDPOINT.seeIncidsClosedByComu(
                        getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()), calle_el_escorial.getId()
                ).blockingGet().body();
        assertThat(incidencias.size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws ServiceException
    {
        // Caso OK: comunidad con 2 incidencias abiertas.
        List<IncidenciaUser> incidencias =
                ENDPOINT.seeIncidsOpenByComu(
                        getUserConnector()
                                .insertTokenGetHeaderStr(pedro.getUserName()), ronda_plazuela_10bis.getId()
                ).blockingGet().body();
        assertThat(incidencias.size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3() throws ServiceException, IOException
    {
        // ComunidadId no existe.
        Response<List<IncidenciaUser>> response = ENDPOINT.seeIncidsOpenByComu(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                999L)
                .blockingGet();
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
        Response<List<IncidenciaUser>> response = ENDPOINT.seeIncidsOpenByComu(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                calle_plazuela_23.getId())
                .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1()
    {
        // Caso OK: resolución con avances.
        assertThat(ENDPOINT.seeResolucion(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                3L)
                .blockingGet().body().getAvances().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2()
    {
        // Caso OK: no hay resolución.
        assertThat(ENDPOINT
                        .seeResolucion(getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()), 1L)
                        .blockingGet()
                        .body(),
                nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_3() throws IOException
    {
        // Caso: usuario no asociado a la comunidad.
        Response<Resolucion> response = ENDPOINT.seeResolucion(
                getUserConnector().insertTokenGetHeaderStr(paco.getUserName()),
                3L)
                .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1()
    {
        // Caso OK.
        assertThat(ENDPOINT.seeUserComusImportancia(
                getUserConnector().insertTokenGetHeaderStr(pedro.getUserName()),
                4L)
                .blockingGet().body().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_b.sql", "classpath:insert_incidencia_b.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_2() throws IOException
    {
        // Caso: usuario no pertenece a la comunidad.
        Response<List<ImportanciaUser>> response = ENDPOINT.seeUserComusImportancia(
                getUserConnector().insertTokenGetHeaderStr(paco.getUserName()),
                4L)
                .blockingGet();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USERCOMU_WRONG_INIT.getHttpMessage()));
    }

//    ================================== HELPER METHODS ==================================

    private boolean isIncidenciaFound(Response<?> responseEndPoint) throws IOException
    {
        assertThat(responseEndPoint.isSuccessful(), is(false));
        return !retrofitHandler.getErrorBean(responseEndPoint).getMessage().equals(INCIDENCIA_NOT_FOUND.getHttpMessage());
    }

//    ================================== INNER CLASSES ==================================

    /**
     * User: pedro@didekin
     * Date: 20/11/15
     * Time: 11:47
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            IncidenciaManagerConfiguration.class})
    @ActiveProfiles({Profiles.NGINX_JETTY_PRE})
    @Category({AwsPre.class})
    public static class IncidenciaCtrlerAwsTest extends IncidenciaControllerTest {
    }

    /**
     * User: pedro@didekin
     * Date: 20/11/15
     * Time: 11:47
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class, RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class, DbPre.class})
    @DirtiesContext
    public static class IncidenciaCtrlerDbPreDevTest extends IncidenciaControllerTest {
    }
}
