package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doComment;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidencia;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithId;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doResolucion;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_olmo_55;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan_plazuela23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco_olmo;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_lafuente;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_REGISTERED;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_USER_WRONG_INIT;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_CheckIncidImportanciaByUserInDb() throws Exception
    {
        assertThat(incidenciaManager.checkIncidImportanciaInDb(juan.getUserName(), 5L), is(false));
        assertThat(incidenciaManager.checkIncidImportanciaInDb(paco.getUserName(), 5L), is(true));
    }

    // ======================================  MANAGER  ========================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws EntityException, IOException
    {
        // Caso OK: añadimos un avance a una resolución con avances, y cerramos la incidencia.
        // Premisas.
        Resolucion resolucion = incidenciaManager.seeResolucion(luis.getUserName(), 3L);
        assertThat(resolucion.getAvances().size(), is(2));
        final Incidencia incidencia = incidenciaManager.seeIncidenciaById(resolucion.getIncidencia().getIncidenciaId());
        assertThat(incidencia.getFechaCierre(), nullValue());
        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder()
                .avanceDesc("avance3")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        Timestamp fechaPrevNew = Timestamp.from(Instant.now().plus(5, MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .avances(avances)
                .build();
        // Exec and check: return 3 (incidencia, resolucion y avance).
        assertThat(incidenciaManager.closeIncidencia(luis.getUserName(), resolucion), is(3));
        assertThat(incidenciaManager.seeResolucion(luis.getUserName(), 3L).getAvances().size(), is(3));
        assertThat(incidenciaManager.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), notNullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_2() throws EntityException, IOException
    {
        // Caso OK: cierra la incidencia sin añadir avance.
        // Premisas.
        Resolucion resolucion = incidenciaManager.seeResolucion(luis.getUserName(), 3L);
        final Incidencia incidencia = incidenciaManager.seeIncidenciaById(resolucion.getIncidencia().getIncidenciaId());
        assertThat(incidencia.getFechaCierre(), nullValue());
        // Nuevos datos.
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .avances(null)
                .build();
        // Devuelve 2: no modifica avances.
        assertThat(incidenciaManager.closeIncidencia(luis.getUserName(), resolucion), is(2));
        assertThat(incidenciaManager.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), notNullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_3()
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_1() throws EntityException
    {
        // Caso OK: usuario iniciador y adm modifica incidencia.
        // Premises.
        assertThat(incidenciaManager.seeIncidenciaById(4L).getUserName(), is(paco.getUserName()));
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(paco.getUserName(), 4L), is(true));
        // Data
        Incidencia incidencia = doIncidenciaWithIdDescUsername(paco.getUserName(), 4L, "desc_mod", calle_plazuela_23.getC_Id(), (short) 12);
        // Excec and check.
        assertThat(((IncidenciaManager) incidenciaManager).modifyIncidencia(paco.getUserName(), incidencia), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_2()
    {
        // UserName in incidencia is null.
        Incidencia incidencia = doIncidenciaWithId(null, 2L, 2L, (short) 1);
        try {
            ((IncidenciaManager) incidenciaManager).modifyIncidencia(pedro.getUserName(), incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_USER_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_3() throws EntityException
    {
        // Premisas: usuario no ADM ni iniciador incidencia.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(5L);
        assertThat(incidenciaManager.getUsuarioConnector().checkIncidModificationPower(juan.getUserName(), incidencia), is(false));
        // Datos.
        incidencia = new Incidencia.IncidenciaBuilder().copyIncidencia(incidencia).descripcion("new_description").build();
        // Exec and check: returns 0.
        assertThat(((IncidenciaManager) incidenciaManager).modifyIncidencia(juan.getUserName(), incidencia), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException
    {
        // Premisas: SIN registro previo de incidImportancia; NO usuario ADM, ni iniciador; existe registro de incidencia.
        IncidAndResolBundle resolBundle = incidenciaManager.seeIncidImportanciaByUser(juan.getUserName(), 4L);
        assertThat(resolBundle.getIncidImportancia().getImportancia(), is((short) 0));
        assertThat(resolBundle.getIncidImportancia().getFechaAlta(), nullValue());
        Incidencia incidencia = resolBundle.getIncidImportancia().getIncidencia();
        assertThat(incidenciaManager.getUsuarioConnector().checkIncidModificationPower(juan.getUserName(), incidencia), is(false));
        /* Data.*/
        IncidImportancia newIncidImp = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 3).usuarioComunidad(juan_plazuela23).build();
        // Exec: inserta registro incidImportancia.
        assertThat(incidenciaManager.modifyIncidImportancia(juan.getUserName(), newIncidImp), is(1));
        // Check.
        resolBundle = incidenciaManager.seeIncidImportanciaByUser(juan.getUserName(), incidencia.getIncidenciaId());
        assertThat(resolBundle.getIncidImportancia().getImportancia(), is((short) 3));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException
    {
        // Premisas: usuario iniciador ( -> existe registro previo de incidImportancia); no ADM.
        final IncidImportancia incidImportancia = incidenciaManager.seeIncidImportanciaByUser(paco.getUserName(), 6L).getIncidImportancia();
        Incidencia incidencia = incidImportancia.getIncidencia();
        assertThat(incidencia.getComunidad(), is(calle_olmo_55));
        assertThat(incidencia.getUserName(), is(paco.getUserName()));
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(paco.getUserName(), calle_olmo_55.getC_Id()), is(false));
        assertThat(incidImportancia.getImportancia(), is((short) 2));
        // Data
        IncidImportancia incidImpIn = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 1).usuarioComunidad(paco_olmo).build();
        // Exec and check: insert incidImportancia and update incidencia (although it hasn't changed).
        assertThat(incidenciaManager.modifyIncidImportancia(paco.getUserName(), incidImpIn), is(2));
        assertThat(incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 6L).getIncidImportancia().getImportancia(), is((short) 1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_3() throws EntityException
    {
        // Premisa: CON registro previo de incidImportancia, sin cambiar nada. Usuario no iniciador, función ADM.
        IncidImportancia incidImportancia = incidenciaDao.seeIncidImportanciaByUser(pedro.getUserName(), 2L).getIncidImportancia();
        assertThat(incidImportancia.getFechaAlta(), notNullValue());
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(pedro.getUserName(), 2L), is(true));
        // Data: no change.
        IncidImportancia incidImpNew = new IncidImportancia.IncidImportanciaBuilder(incidenciaManager.seeIncidenciaById(2L)).copyIncidImportancia(incidImportancia).build();
        assertThat(incidImpNew.equals(incidImportancia), is(true));
        // Exec and check: update incidImportancia and update incidencia (although they haven't changed).
        assertThat(incidenciaManager.modifyIncidImportancia(pedro.getUserName(), incidImpNew), is(2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_4() throws EntityException
    {
        // Premisa: incidencia is closed. Usuario inicidador.
        Incidencia incidencia = incidenciaManager.seeIncidenciaById(7L);
        assertThat(incidencia.getFechaCierre(), notNullValue());
        assertThat(incidenciaManager.getUsuarioConnector().checkIncidModificationPower(paco.getUserName(), incidencia), is(true));
        // Data:
        IncidImportancia incidNew = new IncidImportancia.IncidImportanciaBuilder(incidencia).usuarioComunidad(paco_olmo).importancia((short) 1).build();
        // Exec and check
        try {
            incidenciaManager.modifyIncidImportancia(paco.getUserName(), incidNew);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }

        // Premisa: usuario not associated to comunidad.
        try {
            incidenciaManager.getUsuarioConnector().checkUserInComunidad(pedro.getUserName(), calle_olmo_55.getC_Id());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
        // Data:
        incidencia = incidenciaManager.seeIncidenciaById(6L);
        assertThat(incidencia.getComunidadId(), is(calle_olmo_55.getC_Id()));
        incidNew = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(calle_olmo_55, pedro).roles("pro").build())
                .importancia((short) 1).build();
        // Exec and check
        try {
            incidenciaManager.modifyIncidImportancia(pedro.getUserName(), incidNew);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }

        // Premisa: importancia <= 0.
        incidNew = new IncidImportancia.IncidImportanciaBuilder(incidencia).usuarioComunidad(paco_olmo).importancia((short) 0).build();
        // Exec and check: inserta incidencia e incidenciaImportancia. Devuelve 1, porque modifica incidencia aunque no haya variación.
        assertThat(incidenciaManager.modifyIncidImportancia(paco.getUserName(), incidNew), is(1));
        // Verificamos que si importancia > 0, el método devolvería 2.
        incidNew = new IncidImportancia.IncidImportanciaBuilder(incidencia).usuarioComunidad(paco_olmo).importancia((short) 2).build();
        assertThat(incidenciaManager.modifyIncidImportancia(paco.getUserName(), incidNew), is(2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException
    {
        // Caso OK: resolucion con 2 avances; se modifica resolucion.coste estimado y se añade avance: devuelve 2.
        Resolucion resolucion = incidenciaManager.seeResolucion(pedro.getUserName(), 3L);
        // Verificamos estado anterior.
        assertThat(resolucion.getCosteEstimado(), is(11));
        assertThat(resolucion.getAvances().size(), is(2));

        // Nuevos datos, con nuevo avance.
        Avance avance = new Avance.AvanceBuilder()
                .avanceDesc("avance3")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        Timestamp fechaPrevNew = Timestamp.from(Instant.now().plus(5, MINUTES));
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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
        assertThat(((IncidenciaManager) incidenciaManager).regIncidencia(incidencia).getIncidenciaId() > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_2() throws Exception
    {
        // Caso: comunidad no existe.  doIncidenciaWithId(String userName, long incidenciaId, long comunidadId, short ambitoId)
        Incidencia incidencia = doIncidencia(luis.getUserName(), "Incidencia de test", 999L, (short) 24);
        try {
            ((IncidenciaManager) incidenciaManager).regIncidencia(incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_REGISTERED));
        }

        /* Caso: incidenciaId > 0.*/
        final Incidencia incidenciaNew = doIncidenciaWithId(luis.getUserName(), 2L, 2L, (short) 24);
        assertThat(((IncidenciaManager) incidenciaManager).regIncidencia(incidenciaNew), is(incidenciaNew));
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
        assertThat(((IncidenciaManager) incidenciaManager).regIncidencia(incidencia).getDescripcion(), is("incid_test"));
        SECONDS.sleep(10);
        assertThat(usuarioManager.getGcmToken(pedro.getuId()), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

        // Caso: usuario no asociado a la comunidad.
        // Premisa:
        try {
            incidenciaManager.getUsuarioConnector().checkUserInComunidad(paco.getUserName(), 1L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
        // Data.
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidenciaManager.seeIncidenciaById(3L)).importancia((short) 1).build();
        // Exec and check.
        try {
            incidenciaManager.regIncidImportancia(paco.getUserName(), incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws Exception
    {
        // Premisas: no existe incidencia;  iniciador no es usuario ADM.
        assertThat(incidenciaManager.getUsuarioConnector().checkAuthorityInComunidad(juan.getUserName(), 2L), is(false));
        Incidencia incidencia = doIncidencia(juan.getUserName(), "Nueva incidencia en Cámaras de vigilancia", 2L, (short) 11);
        // Data
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).usuarioComunidad(pedro_lafuente).importancia((short) 4).build();
        // Exec and check: se insertan incidencia e incidenciaImportancia.
        assertThat(incidenciaManager.regIncidImportancia(pedro.getUserName(), incidImportancia), is(2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws Exception
    {
        // Premisas: existe incidencia;  no existe incidenciaImportancia para usuario ADM.
        final Incidencia incidencia = incidenciaManager.seeIncidenciaById(3L);
        assertThat(incidencia, notNullValue());
        assertThat(incidenciaManager.seeIncidImportanciaByUser(luis.getUserName(), 3L),
                allOf(
                        hasProperty("incidImportancia",
                                allOf(
                                        hasProperty("importancia", is((short) 0)),
                                        hasProperty("fechaAlta", nullValue())
                                )
                        )
                )
        );
        // Data
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).usuarioComunidad(luis_plazuelas_10bis).importancia((short) 2).build();
        // Exec and check: se inserta incidenciaImportancia.
        assertThat(incidenciaManager.regIncidImportancia(luis.getUserName(), incidImportancia), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_1() throws EntityException, InterruptedException, SQLException
    {
        // Premise: existe registro de incidImportancia en BD para el usuario; incidencia sin resolución iniciada.
        assertThat(incidenciaDao.countResolucionByIncid(4L), is(0));
        // Exec.
        IncidAndResolBundle resolBundleOut = incidenciaManager.seeIncidImportanciaByUser(paco.getUserName(), 4L);
        // Check.
        assertThat(resolBundleOut.getIncidImportancia().getFechaAlta(), notNullValue());
        assertThat(resolBundleOut.getIncidImportancia().getImportancia() > 0, is(true));
        assertThat(resolBundleOut.hasResolucion(), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_2() throws EntityException
    {
        // Premisa: la incidencia está cerrada.
        assertThat(incidenciaManager.seeIncidenciaById(5L).getFechaCierre(), notNullValue());
        // Exec.
        try {
            incidenciaManager.seeIncidImportanciaByUser(paco.getUserName(), 5L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_3() throws EntityException
    {
        // Caso: no hay registro incidImportancia para el usuario; existen usuario-comunidad e incidencia-comunidad; NO resolución asociada.
        try {
            incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
        assertThat(incidenciaDao.countResolucionByIncid(4L), is(0));
        // Data.
        IncidAndResolBundle resolBundle = incidenciaManager.seeIncidImportanciaByUser(juan.getUserName(), 4L);
        // Check.
        assertThat(resolBundle.getIncidImportancia(),
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(4L)),
                                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 37))),
                                        hasProperty("fechaAlta", notNullValue()),
                                        hasProperty("comunidad",
                                                allOf(
                                                        hasProperty("c_Id", is(calle_plazuela_23.getC_Id())),
                                                        hasProperty("tipoVia", is(calle_plazuela_23.getTipoVia())),
                                                        hasProperty("nombreVia", is(calle_plazuela_23.getNombreVia())),
                                                        hasProperty("numero", is(calle_plazuela_23.getNumero())),
                                                        hasProperty("sufijoNumero", is(calle_plazuela_23.getSufijoNumero()))
                                                )
                                        )
                                )
                        ),
                        hasProperty("userComu",
                                allOf(
                                        hasProperty("usuario",
                                                allOf(
                                                        hasProperty("userName", is(juan.getUserName())),
                                                        hasProperty("alias", is(juan.getAlias())),
                                                        hasProperty("uId", is(juan.getuId()))
                                                )
                                        ),
                                        hasProperty("comunidad", hasProperty("c_Id", is(calle_plazuela_23.getC_Id()))),
                                        hasProperty("roles", is(INQUILINO.function))
                                )
                        ),
                        hasProperty("importancia", is((short) 0)),
                        hasProperty("fechaAlta", nullValue())
                )
        );
        assertThat(resolBundle.hasResolucion(), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_4() throws EntityException, InterruptedException, SQLException
    {
        // Caso: no hay registro incidImportancia para el usuario; existen usuario-comunidad e incidencia-comunidad; SÍ resolución asociada.
        try {
            incidenciaDao.seeIncidImportanciaByUser(luis.getUserName(), 3L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
        assertThat(incidenciaDao.countResolucionByIncid(3L), is(1));
        // Data.
        IncidAndResolBundle resolBundle = incidenciaManager.seeIncidImportanciaByUser(luis.getUserName(), 3L);
        // Check.
        assertThat(resolBundle.getIncidImportancia(),
                allOf(
                        hasProperty("importancia", is((short) 0)),
                        hasProperty("fechaAlta", nullValue())
                )
        );
        assertThat(resolBundle.hasResolucion(), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_SeeIncidsClosedByComu_1() throws Exception
    {
        // Premisa: comunidad sin incidencias cerradas.
        assertThat(incidenciaManager.seeIncidsClosedByComu(pedro.getUserName(), 2L).size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_SeeIncidsClosedByComu_2() throws Exception
    {
        // Premisa: comunidad sin incidencias cerradas.
        assertThat(incidenciaManager.seeIncidsClosedByComu(pedro.getUserName(), 2L).size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_SeeIncidsClosedByComu_3() throws Exception
    {
        // Premisa: no existe la comunidad.
        try {
            incidenciaManager.seeIncidsClosedByComu("noexisto@no.com", 2L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }

        // Premisa: no existe el usuario.
        try {
            incidenciaManager.seeIncidsClosedByComu(juan.getUserName(), 999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws EntityException
    {
        // Premisas: una única incidencia en la comunidad; abierta.
        List<IncidenciaUser> incidenciasUser = incidenciaManager.seeIncidsOpenByComu(pedro.getUserName(), calle_la_fuente_11.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_2")));

        // Premisas: dos incidencias en la comunidad; una abierta, otra cerrada.
        incidenciasUser = incidenciaManager.seeIncidsOpenByComu(paco.getUserName(), calle_plazuela_23.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_4")));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2() throws EntityException
    {
        // Caso: no incidencias en la comunidad.
        assertThat(incidenciaManager.deleteIncidencia(juan.getUserName(), 2L), is(1));
        List<IncidenciaUser> incidenciaUsers = incidenciaManager.seeIncidsOpenByComu(juan.getUserName(), calle_la_fuente_11.getC_Id());
        assertThat(incidenciaUsers.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws EntityException
    {
        // Premisa: resolucion sin avances.
        Resolucion resolucion = incidenciaManager.seeResolucion(paco.getUserName(), 5L);
        assertThat(resolucion,
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(5L)),
                                        hasProperty("comunidad", hasProperty("c_Id", is(4L)))   // Difference with the DAO.
                                ))
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2() throws EntityException
    {
        // Premisa: incidencia sin resolucion.
        assertThat(incidenciaDao.seeResolucion(2L), nullValue());
        // Exec.
        assertThat(incidenciaManager.seeResolucion(juan.getUserName(), 2L), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
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

}