package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.didekin.incidservice.testutils.IncidenciaTestUtils.calle_plazuela_23;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doComment;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidencia;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithId;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doResolucion;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.juan;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.juan_plazuela23;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.luis;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.paco;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.paco_olmo;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.pedro;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_USER_WRONG_INIT;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:29
 */
@SuppressWarnings("unchecked")
public abstract class IncidenciaManagerTest {

    @Autowired
    private IncidenciaManagerIf incidenciaManager;
    @Autowired
    private IncidenciaDao incidenciaDao;
    @Autowired
    private UsuarioManager usuarioManager;

    private static final String tokenId_1 =
            "eHjO7v0yDv0:APA91bFe9Zzc2wh2F4uk5zr1KWHDQRbP9LQYv1WJ6LvVZ268xO-7B_oK1knt7_opdbUyUImg4ptOwKI-SienVZ0zT2O4ErhDOYc--HPH_qbuXIEfhG5FeQr14wcVEA1g5lPpjaXEfZiE";

    // ======================================  HELPERS ========================================

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_checkIncidenciaOpen()
    {
        assertThat(incidenciaManager.checkIncidenciaOpen(4L), is(true));
        try {
            incidenciaManager.checkIncidenciaOpen(5L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    // ======================================  MANAGER  ========================================

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1()
    {
        // Premise: la incidencia is closed.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());
        try {
            incidenciaManager.closeIncidencia(
                    paco.getUserName(),
                    doResolucion(new Incidencia.IncidenciaBuilder().copyIncidencia(incidencia).build(),
                            paco.getUserName(),
                            "resol_incid_5_4",
                            111,
                            Instant.now().plus(12, DAYS))
            );
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_1() throws EntityException, IOException
    {
        // Premises: usuario iniciador incidencia; no ADM.
        assertThat(juan.getUserName().equals(incidenciaManager.seeIncidenciaById(2L).getUserName()), is(true));
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(juan.getUserName(), 2L), is(false));
        // Exec and check.
        assertThat(incidenciaManager.deleteIncidencia(juan.getUserName(), 2L), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_2() throws EntityException, IOException
    {
        // Premises: no usuario iniciador; no ADM.
        assertThat(juan.getUserName().equals(incidenciaManager.seeIncidenciaById(4L).getUserName()), is(false));
        incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(juan.getUserName(), 4L);
        // Exec and check.
        try {
            incidenciaManager.deleteIncidencia(juan.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testIsIncidenciaWithResolucion_1() throws EntityException, InterruptedException
    {
        assertThat(incidenciaManager.isIncidenciaWithResolucion(4L), is(false));
        assertThat(incidenciaManager.isIncidenciaWithResolucion(3L), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_1() throws EntityException
    {
        // Caso OK: devuelve 1.
        Incidencia incidencia = doIncidenciaWithIdDescUsername(paco.getUserName(), 4L, "desc_mod", calle_plazuela_23.getC_Id(), (short) 12);
        assertThat(incidenciaManager.modifyIncidencia(paco.getUserName(), incidencia), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_2()
    {
        // UserName in incidencia null.
        Incidencia incidencia = doIncidenciaWithId(null, 2L, 2L, (short) 1);

        try {
            incidenciaManager.modifyIncidencia(pedro.getUserName(), incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_USER_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_3() throws EntityException
    {
        // Premisas: usuario no ADM ni iniciador incidencia.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidenciaManager.getUsuarioConnector().checkIncidModificationPower(juan.getUserName(), 4L, incidencia.getUserName()), is(false));
        // Datos.
        incidencia = new Incidencia.IncidenciaBuilder().copyIncidencia(incidencia).descripcion("new_description").build();
        // Exec and check: returns 0.
        assertThat(incidenciaManager.modifyIncidencia(juan.getUserName(), incidencia), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException
    {
        // Premisas: SIN registro previo de incidImportancia; NO usuario ADM.
        IncidAndResolBundle resolBundle = incidenciaManager.seeIncidImportancia(juan.getUserName(), 4L);
        assertThat(resolBundle.getIncidImportancia().getImportancia(), is((short) 0));
        assertThat(resolBundle.getIncidImportancia().getFechaAlta(), nullValue());
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(juan.getUserName(), 4L), is(false));
        // Data.
        Incidencia incidencia = resolBundle.getIncidImportancia().getIncidencia();
        IncidImportancia newIncidImp = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 3).usuarioComunidad(juan_plazuela23).build();
        // Exec: inserta registro incidImportancia.
        assertThat(incidenciaManager.modifyIncidImportancia(juan.getUserName(), newIncidImp), is(1));
        // Check.
        resolBundle = incidenciaManager.seeIncidImportancia(juan.getUserName(), incidencia.getIncidenciaId());
        assertThat(resolBundle.getIncidImportancia().getImportancia(), is((short) 3));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException
    {
        // Premisas: SIN registro previo de incidImportancia; usuario iniciador.
        try {
            assertThat(incidenciaManager.getUsuarioConnector().checkIncidModificationPower(paco.getUserName(), 6L, paco.getUserName()), is(true));
            incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 6L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
        // Data
        IncidImportancia incidImpIn = new IncidImportancia.IncidImportanciaBuilder(incidenciaManager.seeIncidenciaById(6L)).importancia((short) 2).usuarioComunidad(paco_olmo).build();
        // Exec and check: insert incidImportancia and update incidencia (although it hasn't changed).
        assertThat(incidenciaManager.modifyIncidImportancia(paco.getUserName(), incidImpIn), is(2));
        assertThat(incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 6L).getImportancia(), is((short) 2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_3() throws EntityException
    {
        // Premisa: CON registro previo de incidImportancia, sin cambiar nada. Usuario no iniciador ADM.
        IncidImportancia incidImportancia = incidenciaDao.seeIncidImportanciaByUser(pedro.getUserName(), 2L);
        assertThat(incidImportancia.getFechaAlta(), notNullValue());
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(pedro.getUserName(), 2L), is(true));
        // Data: no change.
        IncidImportancia incidImpNew = new IncidImportancia.IncidImportanciaBuilder(incidenciaManager.seeIncidenciaById(2L)).copyIncidImportancia(incidImportancia).build();
        assertThat(incidImpNew.equals(incidImportancia), is(true));
        // Exec and check: update incidImportancia and update incidencia (although they haven't changed).
        assertThat(incidenciaManager.modifyIncidImportancia(pedro.getUserName(), incidImpNew), is(2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException
    {
        // Caso OK: devuelve 2.
        Resolucion resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), 3L);
        // Verificamos estado anterior.
        assertThat(resolucion.getCosteEstimado(), is(11));
        assertThat(resolucion.getAvances().size(), is(2));

        // Nuevos datos, con nuevo avance.
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
        assertThat(incidenciaManager.modifyResolucion(pedro.getUserName(), resolucion), is(2));
        resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getAvances().size(), is(3));
        assertThat(resolucion.getCosteEstimado(), is(1111));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws EntityException
    {
        // Caso OK: devuelve 1; no añadimos avances.
        Resolucion resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), 3L);
        assertThat(resolucion.getAvances().size(), is(2));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(1111)
                .avances(null)
                .build();

        assertThat(incidenciaManager.modifyResolucion(pedro.getUserName(), resolucion), is(1));
        resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getAvances().size(), is(2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_3() throws EntityException
    {
        // Caso not OK: la incidencia está cerrada.
        Resolucion resolucion = incidenciaManager.seeResolucion(paco.getUserName(), 5L);
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());

        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(2232)
                .build();
        try {
            incidenciaManager.modifyResolucion(resolucion.getUserName(), resolucion);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws EntityException
    {
        // Incidencia is closed.
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 1L, ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = doComment("comment_userComu_NoDb", incidencia, pedro);
        incidenciaDao.closeIncidencia(incidencia.getIncidenciaId());
        assertThat(incidenciaManager.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), notNullValue());

        try {
            incidenciaManager.regIncidComment(pedro.getUserName(), comment);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws EntityException
    {
        // Incidencia is open.
        Incidencia incidencia = doIncidenciaWithId(luis.getUserName(), 1L, ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = doComment("comment_userComu_NoDb", incidencia, pedro);
        assertThat(incidenciaManager.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), nullValue());

        assertThat(incidenciaManager.regIncidComment(pedro.getUserName(), comment), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_1() throws Exception
    {
        Incidencia incidencia = doIncidencia(luis.getUserName(), "Incidencia de test", 1L, (short) 24);
        assertThat(incidenciaManager.regIncidencia(incidencia).getIncidenciaId() > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_2() throws Exception
    {
        // Caso: comunidad no existe.
        Incidencia incidencia = doIncidencia(luis.getUserName(), "Incidencia de test", 999L, (short) 24);
        try {
            incidenciaManager.regIncidencia(incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    /**
     * Precondition: gcmToken is NOT NULL and NOT valid in database.
     * Postcondition:
     * - Notification is sent to the GCM server and the following message is received:
     * {"multicast_id":5687883094401283275,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"NotRegistered"}]}.
     * - GcmToken is written to null in database after insertion and communication con GCM service.
     */
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_3() throws Exception
    {
        // Premisas: gcmToken is NOT NULL.
        assertThat(usuarioManager.modifyUserGcmToken(pedro.getUserName(), tokenId_1), is(1));
        assertThat(usuarioManager.getGcmTokensByComunidad(ronda_plazuela_10bis.getC_Id()).size(), is(1));

        Incidencia incidencia = doIncidencia(pedro.getUserName(), "incid_test", ronda_plazuela_10bis.getC_Id(), (short) 24);
        assertThat(incidenciaManager.regIncidencia(incidencia).getDescripcion(), is("incid_test"));
        SECONDS.sleep(10);
        assertThat(usuarioManager.getGcmToken(pedro.getuId()), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws Exception
    {
        // Caso: incidencia cerrada.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre().getTime() < new Date().getTime(), is(true));
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 1).build();
        try {
            incidenciaManager.regIncidImportancia(juan.getUserName(), incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws Exception
    {
        // Caso OK.  TODO: test.
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion() throws EntityException
    {
        // Caso: la incidencia ya está cerrada. Incidencia 5, comunidad 4.
        // Devuelve INCIDENCIA_NOT_FOUND, porque no está entre las incidencias ABIERTAS.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre().getTime() < new Date().getTime(), is(true));
        Resolucion resolucion = doResolucion(incidencia, paco.getUserName(), "resol_incid_5_4", 111, Instant.now().plus(12, DAYS));
        try {
            incidenciaManager.regResolucion(paco.getUserName(), resolucion);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_1()
    {
        // La incidencia no existe. La comunidad, sí.
        try {
            incidenciaManager.seeCommentsByIncid(999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_2() throws EntityException
    {
        // La incidencia existe, no tiene comentarios.
        List<IncidComment> comments = incidenciaManager.seeCommentsByIncid(2L);
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_1() throws EntityException, InterruptedException, SQLException
    {
        // Premise: existe registro de incidImportancia en BD para el usuario.
        assertThat(incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 4L).getFechaAlta(), notNullValue());
        // Exec.
        IncidImportancia incidImportancia = incidenciaManager.seeIncidImportancia(paco.getUserName(), 4L).getIncidImportancia();
        // Check.
        checkCommonIncidImpByUser(incidImportancia, paco, 4L, 4L, "adm");
        assertThat(incidImportancia.getImportancia(), is((short) 4));
        assertThat(incidImportancia.getFechaAlta().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_2() throws EntityException
    {
        // Premisa: la incidencia está cerrada.
        assertThat(incidenciaManager.seeIncidenciaById(5L).getFechaCierre(), notNullValue());
        // Exec.
        try {
            incidenciaManager.seeIncidImportancia(paco.getUserName(), 5L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_3() throws EntityException
    {
        // Caso: no hay registro incidImportancia para el usuario.
        try {
            incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        IncidImportancia incidImportancia = incidenciaManager.seeIncidImportancia(juan.getUserName(), 4L).getIncidImportancia();
        checkCommonIncidImpByUser(incidImportancia, juan, 4L, 4L, "inq");
        assertThat(incidImportancia,
                allOf(
                        hasProperty("importancia", is((short) 0)),
                        hasProperty("fechaAlta", nullValue()),
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("userName", is(paco.getUserName())),
                                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 37))),
                                        hasProperty("fechaAlta", notNullValue()),
                                        hasProperty("fechaCierre", nullValue()),
                                        hasProperty("comunidad",
                                                allOf(
                                                        Matchers.hasProperty("tipoVia", is(calle_plazuela_23.getTipoVia())),
                                                        Matchers.hasProperty("nombreVia", is(calle_plazuela_23.getNombreVia())),
                                                        Matchers.hasProperty("numero", is(calle_plazuela_23.getNumero())),
                                                        Matchers.hasProperty("sufijoNumero", is(calle_plazuela_23.getSufijoNumero()))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Test
    public void test_SeeIncidsClosedByComu() throws Exception
    {
        // TODO: test.
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws EntityException, InterruptedException
    {
        List<IncidenciaUser> incidenciasUser = incidenciaManager.seeIncidsOpenByComu(ronda_plazuela_10bis.getC_Id());
        assertThat(incidenciasUser.size(), is(2));
        assertThat(incidenciasUser.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_1")),
                hasProperty("ambitoIncidencia", is(new AmbitoIncidencia((short) 41))),
                hasProperty("importanciaAvg", is(1.5f)),
                hasProperty("incidenciaId", is(1L)),
                hasProperty("comunidad", Is.is(ronda_plazuela_10bis)),
                hasProperty("userName", Is.is(luis.getUserName())),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaCierre", nullValue())
        ));
        assertThat(incidenciasUser.get(0),
                hasProperty("usuario", allOf(
                        hasProperty("uId", Is.is(luis.getuId())),
                        hasProperty("userName", nullValue()),
                        hasProperty("alias", Is.is(luis.getAlias()))
                ))
        );

        // Segunda incidencia en la misma comunidad.
        assertThat(incidenciasUser.get(1).getIncidencia(),
                allOf(
                        hasProperty("descripcion", is("incidencia_3")),
                        hasProperty("userName", Is.is(pedro.getUserName())),
                        hasProperty("importanciaAvg", is(1f))
                ));
        assertThat(incidenciasUser.get(1), allOf(
                hasProperty("usuario", hasProperty("alias", Is.is(pedro.getAlias()))),
                hasProperty("fechaAltaResolucion", notNullValue()))
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2() throws EntityException
    {
        List<IncidenciaUser> incidenciasUser = incidenciaManager.seeIncidsOpenByComu(IncidenciaTestUtils.calle_la_fuente_11.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_2")));
        assertThat(incidenciasUser.get(0).getIncidencia().getImportanciaAvg(), is((float) 3.5));

        incidenciasUser = incidenciaManager.seeIncidsOpenByComu(calle_plazuela_23.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_4")));
        assertThat(incidenciasUser.get(0).getIncidencia().getImportanciaAvg(), is((float) 4));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3() throws EntityException
    {
        // Caso: no incidencias en la comunidad.
        List<IncidenciaUser> incidenciaUsers = incidenciaManager.seeIncidsOpenByComu(999L);
        assertThat(incidenciaUsers.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws EntityException
    {
        // Caso: OK . TODO:

    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1()
    {
        // CASO: incidencia no existe.
        try {
            incidenciaManager.seeUserComusImportancia(pedro.getUserName(), 999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

//    ================================ Helper methods =======================

    @SuppressWarnings("SameParameterValue")
    private void checkCommonIncidImpByUser(IncidImportancia incidImportancia, Usuario userInSession, long comunidadId, long incidenciaId, String rolInComunidad)
    {
        assertThat(incidImportancia,
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(incidenciaId)),
                                        hasProperty("comunidad", hasProperty("c_Id", is(comunidadId)))
                                )
                        ),
                        hasProperty("userComu",
                                allOf(
                                        hasProperty("usuario",
                                                allOf(
                                                        hasProperty("userName", is(userInSession.getUserName())),
                                                        hasProperty("alias", is(userInSession.getAlias())),
                                                        hasProperty("uId", is(userInSession.getuId()))
                                                )
                                        ),
                                        hasProperty("comunidad", hasProperty("c_Id", is(comunidadId))),
                                        hasProperty("roles", is(rolInComunidad))
                                )
                        )
                )
        );
    }

}