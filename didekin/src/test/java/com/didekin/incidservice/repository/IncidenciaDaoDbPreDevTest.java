package com.didekin.incidservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doComment;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidencia;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithId;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doIncidenciaWithIdDescUsername;
import static com.didekin.incidservice.testutils.IncidenciaTestUtils.doResolucion;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_olmo_55;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco_plazuela23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.model.incidencia.http.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.http.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.http.IncidenciaExceptionMsg.RESOLUCION_DUPLICATE;
import static com.didekinlib.model.incidencia.http.IncidenciaExceptionMsg.RESOLUCION_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static java.sql.Timestamp.from;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 19/11/15
 * Time: 16:10
 */
@SuppressWarnings({"unchecked"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IncidenciaManagerConfiguration.class})
@Profile({NGINX_JETTY_LOCAL, MAIL_PRE})
@Category({LocalDev.class, DbPre.class})
public class IncidenciaDaoDbPreDevTest {

    @Autowired
    private IncidenciaDao incidenciaDao;
    @Autowired
    private UserManagerConnector userManagerConnector;

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws ServiceException, InterruptedException
    {
        assertThat(incidenciaDao.seeIncidenciaById(1L).getFechaCierre(), nullValue());
        Thread.sleep(1000);
        assertThat(incidenciaDao.closeIncidencia(1L), is(1));
        assertThat(incidenciaDao.seeIncidenciaById(1L).getFechaCierre(), notNullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_2() throws ServiceException
    {
        // CASO: incidencia is closed.
        try {
            incidenciaDao.closeIncidencia(5L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIndcidencia_1() throws ServiceException
    {
        // Existe incidencia.
        assertThat(incidenciaDao.deleteIncidencia(1L), is(1));
        // Verificamos que también ha borrado en tabla incidencia_user.
        try {
            incidenciaDao.seeIncidImportanciaByUser(pedro.getUserName(), 1L);
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        // No existe incidencia.
        try {
            incidenciaDao.deleteIncidencia(999L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_2() throws ServiceException
    {
        // Caso NOT OK: la incidencia tiene resolución.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(3L);
        assertThat(incidenciaDao.seeResolucion(incidencia.getIncidenciaId()), notNullValue());

        try {
            incidenciaDao.deleteIncidencia(incidencia.getIncidenciaId());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testGetIncidenciaById() throws ServiceException
    {
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(1L);
        assertThat(incidencia.getIncidenciaId(), is(1L));
        assertThat(incidencia.getDescripcion(), is("incidencia_1"));
        assertThat(incidencia.getAmbitoIncidencia().getAmbitoId(), is((short) 41));
        assertThat(incidencia.getComunidad().getNombreComunidad(), CoreMatchers.is(ronda_plazuela_10bis.getNombreComunidad()));
        assertThat(incidencia.getComunidad().getC_Id(), CoreMatchers.is(ronda_plazuela_10bis.getC_Id()));
        assertThat(incidencia.getFechaAlta().getTime() > 0L, is(true));
        assertThat(incidencia.getFechaCierre(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_IsImportanciaUser()
    {
        assertThat(incidenciaDao.isImportanciaUser(luis.getuId(), 3L), is(0));
        assertThat(incidenciaDao.isImportanciaUser(pedro.getuId(), 3L), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testIsIncidenciaOpen_1()
    {
        assertThat(incidenciaDao.isIncidenciaOpen(1L), is(true));
        incidenciaDao.closeIncidencia(1L);
        assertThat(incidenciaDao.isIncidenciaOpen(1L), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_1() throws ServiceException
    {
        // Premises.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(2L);
        assertThat(incidencia, allOf(
                hasProperty("descripcion", is("incidencia_2")),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 22)))
        ));
        // Data.
        Incidencia incidenciaNew = doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "modified_desc", 2L, (short) 21);
        // Exec and check.
        assertThat(incidenciaDao.modifyIncidencia(incidenciaNew), is(1));
        assertThat(incidenciaDao.seeIncidenciaById(2L), allOf(
                hasProperty("descripcion", is("modified_desc")),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 21)))
        ));

        // Check that the method return 1, although the incidencia has not varied.
        assertThat(incidenciaDao.modifyIncidencia(incidenciaNew), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_2()
    {
        // No existe incidencia en BD.
        Incidencia incidencia = doIncidenciaWithId(null, 999L, 2L, (short) 21);
        try {
            incidenciaDao.modifyIncidencia(incidencia);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }

        // Caso: incidencia cerrada.
        Incidencia incidenciaNew = doIncidenciaWithIdDescUsername("luis@luis.com", 5L, "modified_desc", 4L, (short) 21);
        try {
            incidenciaDao.modifyIncidencia(incidenciaNew);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws ServiceException
    {
        // Caso OK: hay registro previo de incidImportancia.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        // Premisas.
        assertThat(incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 4L).getIncidImportancia().getImportancia(), is((short) 4));
        // Data
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), paco).build())
                .importancia((short) 2)
                .build();
        // Check.
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(1));
        assertThat(incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 4L).getIncidImportancia().getImportancia(), is((short) 2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws ServiceException
    {
        // Caso: incidencia cerrada. No hay modificación. Devuelve 0.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());
        // Data.
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), paco).build())
                .importancia((short) 11)
                .build();
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(0));
    }

    /**
     * Tests sobre fallos en los campos que componen la clave primaria del registro en tabla.
     */
    @Test
    public void testModifyIncidImportancia_3() throws ServiceException
    {
        /* CASO: Incidencia no existe en BD: incidId == 0;*/
        Incidencia incidencia = doIncidencia(luis.getUserName(), "Incidencia_no_BD", 2L, (short) 11);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), pedro).build())
                .build();
        // No lanza excepción: devuelve 0.
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(0));

        // CASO:  Usuario no existe en BD.
        incidencia = doIncidenciaWithId(null, 4L, 4L, (short) 12);
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(),
                new Usuario.UsuarioBuilder()
                        .uId(999L)
                        .build())
                .build();
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(usuarioComunidad)
                .importancia((short) 2)
                .build();
        // No lanza excepción: devuelve 0.
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws ServiceException
    {
        // Caso: resolución con avances. No añadimos ningún avance.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        // Verificamos estado anterior.
        assertThat(resolucion.getCosteEstimado(), is(11));
        assertThat(resolucion.getAvances().size(), is(2));

        // Nuevos datos.
        Timestamp fechaPrevNew = from(now().plus(5, MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .avances(null)
                .build();
        assertThat(incidenciaDao.modifyResolucion(resolucion), is(1));
        resolucion = incidenciaDao.seeResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getCosteEstimado(), is(1111));
        assertThat(Math.abs(resolucion.getFechaPrev().getTime() - fechaPrevNew.getTime()) < 1000, is(true));
        assertThat(resolucion.getAvances().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws ServiceException
    {
        // Caso: la incidencia está cerrada: INCIDENCIA_NOT_FOUND.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());

        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        resolucion = new Resolucion.ResolucionBuilder(incidencia)
                .copyResolucion(resolucion)
                .costeEstimado(1133)
                .build();
        try {
            incidenciaDao.modifyResolucion(resolucion);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_1() throws ServiceException
    {
        // Caso: resolución con 2 avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        // Premisa.
        assertThat(resolucion.getAvances().size(), is(2));

        Avance avance = new Avance.AvanceBuilder()
                .avanceDesc("avance3")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build();
        assertThat(incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance), is(1));
        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(avances.size(), is(3));
        assertThat(avances, hasItem(hasProperty("avanceDesc", is("avance3"))));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_2() throws ServiceException
    {
        // Caso: resolución sin avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        // Premisa.
        assertThat(resolucion.getAvances().size(), is(0));

        Avance avance = new Avance.AvanceBuilder()
                .avanceDesc("avance1")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build();
        assertThat(incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance), is(1));
        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(avances.size(), is(1));
        assertThat(avances, hasItem(hasProperty("avanceDesc", is("avance1"))));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_3() throws ServiceException
    {
        // Caso: no existe la FK incid_id de resolución.
        Resolucion resolucion = new Resolucion.ResolucionBuilder(
                new Incidencia.IncidenciaBuilder()
                        .incidenciaId(999L)
                        .build())
                .descripcion("descResolucion")
                .fechaPrevista(from(now()))
                .userName("user_name")
                .build();

        Avance avance = new Avance.AvanceBuilder().avanceDesc("avanceCrash")
                .author(new Usuario.UsuarioBuilder().userName(resolucion.getUserName()).build())
                .build();
        try {
            incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(RESOLUCION_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws ServiceException
    {
        // Caso OK.
        IncidComment comment = new IncidComment.IncidCommentBuilder()
                .descripcion("comment_1")
                .incidencia(doIncidenciaWithId(null, 2L, 2L, (short) 3))
                .redactor(new Usuario.UsuarioBuilder().uId(3L).build())
                .build();
        assertThat(incidenciaDao.regIncidComment(comment), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws ServiceException
    {
        // No existe la incidencia en BD. Sí existe la comunidad.
        Incidencia incidencia = doIncidenciaWithId(null, 999L, ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = doComment("comment_incidNODb", incidencia, pedro);
        try {
            incidenciaDao.regIncidComment(comment);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_3() throws ServiceException
    {
        // No existe usuarioComunidad en BD; sí existen usuario y comunidad.
        Incidencia incidencia = doIncidenciaWithId(null, 1L, ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = doComment("comment_userComu_NoDb", incidencia, paco);
        try {
            incidenciaDao.regIncidComment(comment);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws ServiceException
    {
        // Premisas: no existe el par incidenciaImportancia-usuario; sí existe la incidencia.
        IncidImportancia incidImportancia;
        try {
            incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 4L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
        // Data.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), juan).build())
                .build();
        // Exec.
        assertThat(incidenciaDao.regIncidImportancia(incidImportancia), is(1));
        // Check data inserted in DB.
        incidImportancia = incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 4L).getIncidImportancia();
        assertThat(incidImportancia.getIncidencia(), is(incidencia));
        assertThat(incidImportancia.getUserComu().getUsuario(), is(juan));
        assertThat(incidImportancia.getImportancia(), is((short) 1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws ServiceException
    {
        // Incidencia no existe en BD.
        Incidencia incidencia = doIncidencia(luis.getUserName(), "Nueva incidencia en Cámaras de vigilancia", 2L, (short) 11);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), pedro)
                        .build())
                .build();
        try {
            incidenciaDao.regIncidImportancia(incidImportancia);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws ServiceException
    {
        // Premisa: usuarioComunidad incongruente con incidencia_comunidad.
        try {
            userManagerConnector.checkUserInComunidad(pedro.getUserName(), 4L);
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
        // Data.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(
                        new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), pedro)
                                .build())
                .importancia((short) 4)
                .build();
        // Check.
        try {
            incidenciaDao.regIncidImportancia(incidImportancia);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_1() throws Exception
    {
        Incidencia incidencia = doIncidencia(juan.getUserName(), "Nueva incidencia en Cámaras de vigilancia", calle_la_fuente_11.getC_Id(), (short) 11);
        assertThat(incidenciaDao.regIncidencia(incidencia) > 0, is(true));

        incidencia = incidenciaDao.getIncidenciasByComu(2L).get(0);
        assertThat(incidencia.getUserName(), is(juan.getUserName()));
        assertThat(incidencia.getComunidad(), is(calle_la_fuente_11));
        assertThat(incidencia.getDescripcion(), is(incidencia.getDescripcion()));
        assertThat(incidencia.getAmbitoIncidencia(), is(incidencia.getAmbitoIncidencia()));
        assertThat(incidencia.getFechaAlta(), notNullValue());
        assertThat(incidencia.getFechaCierre(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_2()
    {
        // No existe comunidadId
        Incidencia incidencia = doIncidencia(juan.getUserName(), "Nueva incidencia en Cámaras de vigilancia", 999L, (short) 11);
        try {
            incidenciaDao.regIncidencia(incidencia);
            fail();
        } catch (SQLException e) {
            assertThat(e.getMessage().contains(ServiceException.COMUNIDAD_FK), is(true));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_3() throws Exception
    {
        // No existe userName: registra la incidencia; no hay restricción de integridad.
        Incidencia incidencia = doIncidencia("no_existo", "Nueva incidencia en Cámaras de vigilancia", calle_la_fuente_11.getC_Id(), (short) 11);
        assertThat(incidenciaDao.regIncidencia(incidencia) > 0, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_1() throws ServiceException
    {
        // Caso OK.
        Incidencia incidencia = doIncidenciaWithId(paco.getUserName(), 4L, paco_plazuela23.getComunidad().getC_Id(), (short) 31);
        Resolucion resolucion = doResolucion(incidencia, paco.getUserName(), "resol_incid_4_4", 111, now().plus(12, DAYS));
        assertThat(incidenciaDao.regResolucion(resolucion), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_2() throws ServiceException
    {
        // Caso: dos resoluciones para una misma incidencia.
        Incidencia incidencia = doIncidenciaWithId(paco.getUserName(), 4L, paco_plazuela23.getComunidad().getC_Id(), (short) 31);
        Resolucion resolucion_1 = doResolucion(incidencia, paco.getUserName(), "resol_incid_4_4_A", 111, now().plus(12, DAYS));
        Resolucion resolucion_2 = doResolucion(incidencia, paco.getUserName(), "resol_incid_4_4_B", 111, now().plus(13, DAYS));
        assertThat(incidenciaDao.regResolucion(resolucion_1), is(1));
        try {
            incidenciaDao.regResolucion(resolucion_2);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(RESOLUCION_DUPLICATE));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_3()
    {
        // Caso: incidencia no existe en BD.
        Incidencia incidencia = doIncidenciaWithId(juan.getUserName(), 999L, calle_plazuela_23.getC_Id(), (short) 31);
        Resolucion resolucion = doResolucion(incidencia, juan.getUserName(), "resol_incid_4_4", 111, now().plus(12, DAYS));
        try {
            incidenciaDao.regResolucion(resolucion);
            fail();
        } catch (ServiceException e) {
            // NO existe la combinación incidencia + comunidad.
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeAvancesByResolucion_1() throws ServiceException
    {
        // Caso: resolución con avances.

        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(3L);
        assertThat(avances.size(), is(2));
        assertThat(avances.get(0), allOf(
                hasProperty("avanceId", is(1L)),
                hasProperty("avanceDesc", is("descripcion_avance_1_3")),
                hasProperty("userName", is(pedro.getUserName())),
                hasProperty("alias", is(pedro.getAlias()))
        ));
        assertThat(avances.get(0).getFechaAlta().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeAvancesByResolucion_2()
    {
        // Caso: resolución sin avances.
        assertThat(incidenciaDao.seeAvancesByResolucion(2L).size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_1()
    {
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(1L);
        assertThat(comments.size(), is(3));

        assertThat(comments.get(0), allOf(
                hasProperty("incidencia", is(
                        new Incidencia.IncidenciaBuilder()
                                .incidenciaId(1L)
                                .comunidad(new Comunidad.ComunidadBuilder().c_id(1L).build())
                                .build())),
                hasProperty("commentId", is(1L)),
                hasProperty("redactor",
                        allOf(
                                is(new Usuario.UsuarioBuilder().uId(3L).build()),
                                hasProperty("userName", is(pedro.getUserName())),
                                hasProperty("alias", is("pedronevado"))
                        )
                ),
                hasProperty("fechaAlta", notNullValue())
        ));

        comments = incidenciaDao.SeeCommentsByIncid(4L);
        assertThat(comments.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_2()
    {
        // La incidencia no existe. La comunidad, sí.
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(999L);
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_SeeIncidenciaById()
    {
        assertThat(incidenciaDao.seeIncidenciaById(1L), allOf(
                hasProperty("incidenciaId", is(1L)),
                hasProperty("comunidad",
                        allOf(
                                hasProperty("c_Id", is(1L)),
                                hasProperty("tipoVia", is(ronda_plazuela_10bis.getTipoVia())),
                                hasProperty("nombreVia", is(ronda_plazuela_10bis.getNombreVia())),
                                hasProperty("numero", is(ronda_plazuela_10bis.getNumero())),
                                hasProperty("sufijoNumero", is(ronda_plazuela_10bis.getSufijoNumero()))
                        )
                ),
                hasProperty("userName", is(luis.getUserName())),
                hasProperty("descripcion", is("incidencia_1")),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 41))),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaCierre", nullValue())
        ));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_1() throws ServiceException
    {
        // Incidencia con resolución. Hay registro incidImportancia para el usuario
        assertThat(incidenciaDao.countResolucionByIncid(3L), is(1));
        // Data.
        IncidAndResolBundle resolBundle = incidenciaDao.seeIncidImportanciaByUser(pedro.getUserName(), 3L);
        // Check.
        assertThat(resolBundle.getIncidImportancia(),
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(3L)),
                                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 46))),
                                        hasProperty("fechaAlta", notNullValue()),
                                        hasProperty("comunidad",
                                                allOf(
                                                        hasProperty("c_Id", is(1L)),
                                                        hasProperty("tipoVia", is(ronda_plazuela_10bis.getTipoVia())),
                                                        hasProperty("nombreVia", is(ronda_plazuela_10bis.getNombreVia())),
                                                        hasProperty("numero", is(ronda_plazuela_10bis.getNumero())),
                                                        hasProperty("sufijoNumero", is(ronda_plazuela_10bis.getSufijoNumero()))
                                                )
                                        )
                                )
                        ),
                        hasProperty("userComu",
                                allOf(
                                        hasProperty("usuario",
                                                allOf(
                                                        hasProperty("userName", is(pedro.getUserName())),
                                                        hasProperty("alias", is(pedro.getAlias())),
                                                        hasProperty("uId", is(pedro.getuId()))
                                                )
                                        ),
                                        hasProperty("comunidad", hasProperty("c_Id", is(ronda_plazuela_10bis.getC_Id()))),
                                        hasProperty("roles", is(ADMINISTRADOR.function))
                                )
                        ),
                        hasProperty("importancia", is((short) 1)),
                        hasProperty("fechaAlta", notNullValue())
                )
        );
        assertThat(resolBundle.hasResolucion(), is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_2() throws ServiceException
    {
        // Caso: existe incidencia; existe usuario, pero no existe el par incidencia_usuario.
        assertThat(incidenciaDao.seeIncidenciaById(4L), notNullValue());
        try {
            incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 4L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        // Caso: no existe la incidencia, existe el usuario.
        try {
            incidenciaDao.seeIncidImportanciaByUser(juan.getUserName(), 999L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        // Caso: incidencia cerrada.
        try {
            incidenciaDao.seeIncidImportanciaByUser(paco.getUserName(), 5L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_3() throws ServiceException
    {
        // Incidencia SIN resolución. Hay registro incidImportancia para el usuario
        assertThat(incidenciaDao.countResolucionByIncid(2L), is(0));
        // Data.
        IncidAndResolBundle resolBundle = incidenciaDao.seeIncidImportanciaByUser(pedro.getUserName(), 2L);
        // Check.
        assertThat(resolBundle.getIncidImportancia(),
                allOf(
                        hasProperty("fechaAlta", notNullValue()),
                        hasProperty("importancia", is((short) 3))
                )
        );
        assertThat(resolBundle.hasResolucion(), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void test_SeeIncidsClosedByComu_1() throws ServiceException
    {
        // Premisas: comunidad con 1 incidencia cerrada; con antigüedad inferior a 2 años.

        // Check: sólo aparece la incidencia con antigüedad < 2 años.
        List<IncidenciaUser> incidencias = incidenciaDao.seeIncidsClosedByComu(calle_plazuela_23.getC_Id());
        assertThat(incidencias.size(), is(1));
        Incidencia incidencia = incidencias.get(0).getIncidencia();
        assertThat(incidencia.getFechaAlta().compareTo(from(now().minus(730, DAYS))) > 0, is(true));
        // Check fields.
        assertThat(incidencia,
                allOf(
                        hasProperty("incidenciaId", is(5L)),
                        hasProperty("comunidad", hasProperty("c_Id", is(calle_plazuela_23.getC_Id()))),
                        hasProperty("userName", is(paco.getUserName())),
                        hasProperty("descripcion", is("incidencia_5")),
                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 3))),
                        hasProperty("importanciaAvg", is(2f)),
                        hasProperty("fechaAlta", notNullValue()),
                        hasProperty("fechaCierre", notNullValue())
                )
        );
        assertThat(incidencias.get(0),
                hasProperty("usuario", allOf
                        (
                                hasProperty("uId", is(paco.getuId())),
                                hasProperty("userName", is(paco.getUserName())),
                                hasProperty("alias", is(paco.getAlias()))
                        )
                )
        );

        // Verificamos que no muestra incidencias con antigüedad > 2 años.
        incidencia = incidenciaDao.seeIncidenciaById(7L);
        assertThat(incidencia, hasProperty("comunidad", hasProperty("c_Id", is(calle_olmo_55.getC_Id()))));
        assertThat(incidencia.getFechaAlta().compareTo(from(now().minus(730, DAYS))) < 0, is(true));
    }

    @Test
    public void test_SeeIncidsClosedByComu_2()
    {
        // CASO NOT OK : si comunidadId no existe, devuelve una lista vacía.
        assertThat(incidenciaDao.seeIncidsClosedByComu(999L).size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1()
    {
        // Caso OK: comunidad con 2 incidencias abiertas.

        List<IncidenciaUser> incidenciaUsers = incidenciaDao.seeIncidsOpenByComu(ronda_plazuela_10bis.getC_Id());
        assertThat(incidenciaUsers.size(), is(2));
        assertThat(incidenciaUsers.get(0).getIncidencia(),
                allOf(
                        hasProperty("incidenciaId", is(1L)),
                        hasProperty("comunidad", hasProperty("c_Id", is(ronda_plazuela_10bis.getC_Id()))),
                        hasProperty("userName", is(luis.getUserName())),
                        hasProperty("descripcion", is("incidencia_1")),
                        hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 41))),
                        hasProperty("importanciaAvg", is(1.5f)),
                        hasProperty("fechaAlta", notNullValue()),
                        hasProperty("fechaCierre", nullValue())

                )
        );
        assertThat(incidenciaUsers.get(0),
                allOf(
                        hasProperty("usuario", allOf
                                (
                                        hasProperty("uId", is(luis.getuId())),
                                        hasProperty("userName", is(luis.getUserName())),
                                        hasProperty("alias", is(luis.getAlias()))
                                )
                        ),
                        hasProperty("fechaAltaResolucion", nullValue())
                )
        );

        // Verificamos fechaAltaResolucion not null en 2ª incidencia.
        assertThat(incidenciaUsers.get(1),
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(3L)),
                                        hasProperty("comunidad", hasProperty("c_Id", is(ronda_plazuela_10bis.getC_Id())))
                                )
                        ),
                        hasProperty("fechaAltaResolucion", notNullValue())
                )
        );
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2()
    {
        // Premisa: the user who initiates the incidencia is no longer a usuarioComunidad, or an user in didekin.
        assertThat(incidenciaDao.seeIncidsOpenByComu(calle_la_fuente_11.getC_Id()).get(0).getUsuario(), is(juan));
        // Delete user in comunidad.
        assertThat(userManagerConnector.deleteUser(juan.getUserName()), is(true));
        // Exec
        IncidenciaUser incidenciaUser = incidenciaDao.seeIncidsOpenByComu(calle_la_fuente_11.getC_Id()).get(0);
        // Check: usuario data are not longer fulfilled, with the exception of userName (taken for the incidencia record).
        assertThat(incidenciaUser,
                allOf(
                        hasProperty("usuario",
                                allOf(
                                        hasProperty("uId", is(0L)),
                                        hasProperty("alias", nullValue()),
                                        hasProperty("userName", is(juan.getUserName()))
                                )
                        ),
                        hasProperty("incidencia", hasProperty("userName", is(juan.getUserName())))
                )
        );
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3()
    {
        // CASO NOT OK : si comunidadId no existe, devuelve una lista vacía.
        assertThat(incidenciaDao.seeIncidsOpenByComu(999L).size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws ServiceException
    {
        /* Caso: resolución con avances.*/
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        assertThat(resolucion, allOf(
                hasProperty("incidencia", hasProperty("incidenciaId", is(3L))),
                hasProperty("userName", is(pedro.getUserName())),
                hasProperty("descripcion", is("plan_resol_3")),
                hasProperty("costeEstimado", is(11)),
                hasProperty("costeFinal", is(11)),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaPrev", notNullValue()),
                hasProperty("moraleja", is("moraleja_3")),
                hasProperty("avances", is(incidenciaDao.seeAvancesByResolucion(3L)))
        ));
        List<Avance> avances = resolucion.getAvances();
        assertThat(avances.size(), is(2));
        assertThat(avances.get(0),
                allOf(
                        hasProperty("avanceDesc", is("descripcion_avance_1_3")),
                        hasProperty("userName", is(pedro.getUserName())),
                        hasProperty("alias", is(pedro.getAlias())),
                        hasProperty("fechaAlta", notNullValue())
                )
        );
        assertThat(avances.get(1),
                allOf(
                        hasProperty("avanceDesc", is("descripcion_avance_2_3")),
                        hasProperty("userName", is(pedro.getUserName())),
                        hasProperty("fechaAlta", notNullValue())
                )
        );
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2() throws ServiceException
    {
        // Premisa: resolucion sin avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        assertThat(resolucion,
                allOf(
                        hasProperty("incidencia",
                                allOf(
                                        hasProperty("incidenciaId", is(5L)),
                                        hasProperty("comunidad", nullValue())  // Difference with the manager.
                                ))
                )
        );
        assertThat(resolucion.getAvances().size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_3()
    {
        /* Caso: incidencia sin resolución.*/
        assertThat(incidenciaDao.seeResolucion(4L), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql", "classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1()
    {
        /* CASOS OK.*/
        List<ImportanciaUser> importanciaUsers = incidenciaDao.seeUserComusImportancia(2L);
        assertThat(importanciaUsers.size(), is(2));
        assertThat(importanciaUsers.get(0), allOf(
                hasProperty("userAlias", is(juan.getAlias())),
                hasProperty("importancia", is((short) 4))
        ));
        assertThat(importanciaUsers.get(1), allOf(
                hasProperty("userAlias", is(pedro.getAlias())),
                hasProperty("importancia", is((short) 3))
        ));

        importanciaUsers = incidenciaDao.seeUserComusImportancia(4L);
        assertThat(importanciaUsers.size(), is(1));
        assertThat(importanciaUsers.get(0), allOf(
                hasProperty("userAlias", is(paco.getAlias())),
                hasProperty("importancia", is((short) 4))
        ));

        // CASO: incidencia no existe.
        try {
            incidenciaDao.seeUserComusImportancia(999L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }
}