package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.RESOLUCION_DUPLICATE;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.RESOLUCION_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
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
@SuppressWarnings("unchecked")
public abstract class IncidenciaDaoTest {

    @Autowired
    private IncidenciaDao incidenciaDao;

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1() throws EntityException, InterruptedException
    {
        assertThat(incidenciaDao.seeIncidenciaById(1L).getFechaCierre(), nullValue());
        Thread.sleep(1000);
        assertThat(incidenciaDao.closeIncidencia(1L), is(1));
        assertThat(incidenciaDao.seeIncidenciaById(1L).getFechaCierre(), notNullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_2() throws EntityException, InterruptedException
    {
        // CASO: incidservice is closed.
        assertThat(incidenciaDao.closeIncidencia(5L), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCountResolucionByIncid_1() throws EntityException, InterruptedException
    {
        assertThat(incidenciaDao.countResolucionByIncid(4L), is(0));

        assertThat(incidenciaDao.countResolucionByIncid(3L), is(1));
        assertThat(incidenciaDao.countResolucionByIncid(5L), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIndcidencia_1() throws EntityException
    {
        // Existe incidservice.
        assertThat(incidenciaDao.deleteIncidencia(1L), is(1));
        // Verificamos que también ha borrado en tabla incidencia_user.
        try {
            incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.pedro.getUserName(), 1L);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        // No existe incidservice.
        try {
            incidenciaDao.deleteIncidencia(999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_2() throws EntityException
    {
        // Caso NOT OK: la incidservice tiene resolución.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(3L);
        assertThat(incidenciaDao.seeResolucion(incidencia.getIncidenciaId()), notNullValue());

        try {
            incidenciaDao.deleteIncidencia(incidencia.getIncidenciaId());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testGetIncidenciaById() throws EntityException
    {
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(1L);
        assertThat(incidencia.getIncidenciaId(), is(1L));
        assertThat(incidencia.getDescripcion(), is("incidencia_1"));
        assertThat(incidencia.getAmbitoIncidencia().getAmbitoId(), is((short) 41));
        assertThat(incidencia.getComunidad().getNombreComunidad(), CoreMatchers.is(IncidenciaTestUtils.ronda_plazuela_10bis.getNombreComunidad()));
        assertThat(incidencia.getComunidad().getC_Id(), CoreMatchers.is(IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()));
        assertThat(incidencia.getFechaAlta().getTime() > 0L, is(true));
        assertThat(incidencia.getFechaCierre(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testIsIncidenciaOpen_1() throws SQLException
    {
        assertThat(incidenciaDao.isIncidenciaOpen(1L), is(true));
        incidenciaDao.closeIncidencia(1L);
        assertThat(incidenciaDao.isIncidenciaOpen(1L), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_1() throws EntityException
    {
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(2L);
        assertThat(incidencia, allOf(
                hasProperty("descripcion", is("incidencia_2")),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 22)))
        ));
        Incidencia incidenciaNew = IncidenciaTestUtils.doIncidenciaWithIdDescUsername("luis@luis.com", 2L, "modified_desc", 2L, (short) 21);
        assertThat(incidenciaDao.modifyIncidencia(incidenciaNew), is(1));
        assertThat(incidenciaDao.seeIncidenciaById(2L), allOf(
                hasProperty("descripcion", is("modified_desc")),
                hasProperty("ambitoIncidencia", hasProperty("ambitoId", is((short) 21))),
                hasProperty("comunidad", is(incidencia.getComunidad())),
                hasProperty("fechaAlta", is(incidencia.getFechaAlta()))
        ));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_2()
    {
        // No existe incidservice en BD. Devuelve '0' filas modificadas.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(null, 999L, 2L, (short) 21);
        assertThat(incidenciaDao.modifyIncidencia(incidencia), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_3() throws EntityException
    {
        // Caso: incidservice cerrada. Devuelve '0' filas modificadas.
        Incidencia incidenciaNew = IncidenciaTestUtils.doIncidenciaWithIdDescUsername("luis@luis.com", 5L, "modified_desc", 4L, (short) 21);
        assertThat(incidenciaDao.modifyIncidencia(incidenciaNew), is(0));
        assertThat(incidenciaDao.seeIncidenciaById(5L).getDescripcion(), is("incidencia_5"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException
    {
        // Caso OK: hay registro previo de incidImportancia.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        // Premisas.
        IncidImportancia incidImportancia = incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.paco.getUserName(), 4L);
        assertThat(incidImportancia.getUserComu().getUsuario(), allOf(notNullValue(), CoreMatchers.is(IncidenciaTestUtils.paco)));
        assertThat(incidImportancia.getImportancia(), is((short) 4));

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.paco).build())
                .importancia((short) 2)
                .build();

        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(1));
        IncidImportancia incidImportanciaDb = incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.paco.getUserName(), 4L);
        assertThat(incidImportanciaDb.getImportancia(), is((short) 2));
        assertThat(incidImportanciaDb.getIncidencia(), is(incidencia));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException
    {
        // Caso: incidservice cerrada. No hay modificación. Devuelve 0.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(5L);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.paco).build())
                .importancia((short) 11)
                .build();
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(0));
        IncidImportancia incidImportanciaDb = incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.paco.getUserName(), 5L);
        assertThat(incidImportanciaDb.getImportancia(), is((short) 2));
    }

    /**
     * Tests sobre fallos en los campos que componen la clave primaria del registro en tabla.
     */
    @Test
    public void testModifyIncidImportancia_3() throws EntityException
    {
        // CASO: Incidencia no existe en BD: incidId == 0;

        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "Incidencia_no_BD", 2L, (short) 11);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.pedro).build())
                .build();

        // No lanza excepción: devuelve 0.
        assertThat(incidenciaDao.modifyIncidImportancia(incidImportancia), is(0));

        // CASO:  Usuario no existe en BD.
        incidencia = IncidenciaTestUtils.doIncidenciaWithId(null, 4L, 4L, (short) 12);
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

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException
    {
        // Caso: resolución con avances. No añadimos ningún avance.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        // Verificamos estado anterior.
        assertThat(resolucion.getCosteEstimado(), is(11));
        assertThat(resolucion.getAvances().size(), is(2));

        // Nuevos datos.
        Timestamp fechaPrevNew = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .fechaPrevista(fechaPrevNew)
                .costeEstimado(1111)
                .build();
        assertThat(incidenciaDao.modifyResolucion(resolucion), is(1));
        resolucion = incidenciaDao.seeResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getCosteEstimado(), is(1111));
        assertThat(Math.abs(resolucion.getFechaPrev().getTime() - fechaPrevNew.getTime()) < 1000, is(true));
        assertThat(resolucion.getAvances().size(), is(2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws EntityException
    {
        // Caso: la incidservice está cerrada. Devuelve 0.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());

        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        resolucion = new Resolucion.ResolucionBuilder(incidencia)
                .copyResolucion(resolucion)
                .costeEstimado(1133)
                .build();
        assertThat(incidenciaDao.modifyResolucion(resolucion), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_1() throws EntityException
    {
        // Caso: resolución con 2 avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        // Premisa.
        assertThat(resolucion.getAvances().size(), is(2));

        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance3").userName(resolucion.getUserName()).build();
        assertThat(incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance), is(1));
        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(avances.size(), is(3));
        assertThat(avances, hasItem(hasProperty("avanceDesc", is("avance3"))));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_2() throws EntityException
    {
        // Caso: resolución sin avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        // Premisa.
        assertThat(resolucion.getAvances().size(), is(0));

        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance1").userName(resolucion.getUserName()).build();
        assertThat(incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance), is(1));
        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(avances.size(), is(1));
        assertThat(avances, hasItem(hasProperty("avanceDesc", is("avance1"))));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegAvance_3() throws EntityException
    {
        // Caso: no existe la FK incid_id de resolución.
        Resolucion resolucion = new Resolucion.ResolucionBuilder(
                new Incidencia.IncidenciaBuilder()
                        .incidenciaId(999L)
                        .build())
                .descripcion("descResolucion")
                .fechaPrevista(Timestamp.from(Instant.now()))
                .userName("user_name")
                .build();

        Avance avance = new Avance.AvanceBuilder().avanceDesc("avanceCrash")
                .userName(resolucion.getUserName())
                .build();
        try {
            incidenciaDao.regAvance(resolucion.getIncidencia().getIncidenciaId(), avance);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(RESOLUCION_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws EntityException
    {
        // Caso OK.
        IncidComment comment = new IncidComment.IncidCommentBuilder()
                .descripcion("comment_1")
                .incidencia(IncidenciaTestUtils.doIncidenciaWithId(null, 2L, 2L, (short) 3))
                .redactor(new Usuario.UsuarioBuilder().uId(3L).build())
                .build();
        assertThat(incidenciaDao.regIncidComment(comment), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_2() throws EntityException
    {
        // No existe la incidservice en BD. Sí existe la comunidad.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(null, 999L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = IncidenciaTestUtils.doComment("comment_incidNODb", incidencia, IncidenciaTestUtils.pedro);
        try {
            incidenciaDao.regIncidComment(comment);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_3() throws EntityException
    {
        // No existe usuarioComunidad en BD; sí existen usuario y comunidad.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(null, 1L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = IncidenciaTestUtils.doComment("comment_userComu_NoDb", incidencia, IncidenciaTestUtils.paco);
        try {
            incidenciaDao.regIncidComment(comment);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws EntityException
    {
        // CASO OK.

        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        // No existe el par incidencia_usuario; sí existe la incidservice.
        IncidImportancia incidImportancia;
        try {
            incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.juan.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 1)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.juan).build())
                .build();

        // Registro el nuevo par incidencia_usuario, y verifico.
        assertThat(incidenciaDao.regIncidImportancia(incidImportancia), is(1));

        incidImportancia = incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.juan.getUserName(), 4L);
        assertThat(incidImportancia.getIncidencia(), is(incidencia));
        assertThat(incidImportancia.getUserComu().getUsuario(), notNullValue());
        assertThat(incidImportancia.getImportancia(), is((short) 1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_2() throws EntityException
    {
        // Incidencia no existe en BD.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "Nueva incidservice en Cámaras de vigilancia", 2L, (short) 11);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 2)
                .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.pedro)
                        .build())
                .build();
        try {
            incidenciaDao.regIncidImportancia(incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_3() throws EntityException
    {
        // UsuarioComunidad incongruente con incidencia_comunidad.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(4L);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(
                        new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), IncidenciaTestUtils.pedro)
                                .build())
                .importancia((short) 4)
                .build();
        try {
            incidenciaDao.regIncidImportancia(incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_1() throws Exception
    {
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.juan.getUserName(), "Nueva incidservice en Cámaras de vigilancia", IncidenciaTestUtils.calle_la_fuente_11.getC_Id(), (short) 11);
        assertThat(incidenciaDao.regIncidencia(incidencia) > 0, is(true));

        incidencia = incidenciaDao.getIncidenciasByComu(2L).get(0);
        assertThat(incidencia.getAmbitoIncidencia(), is(incidencia.getAmbitoIncidencia()));
        assertThat(incidencia.getDescripcion(), is(incidencia.getDescripcion()));
        assertThat(incidencia.getUserName(), CoreMatchers.is(IncidenciaTestUtils.juan.getUserName()));
        assertThat(incidencia.getComunidad(), CoreMatchers.is(IncidenciaTestUtils.calle_la_fuente_11));
        assertThat(incidencia.getFechaAlta().getTime() > 1000, is(true));
        assertThat(incidencia.getFechaCierre(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_2() throws Exception
    {
        // No existe comunidadId
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.juan.getUserName(), "Nueva incidservice en Cámaras de vigilancia", 999L, (short) 11);
        try {
            incidenciaDao.regIncidencia(incidencia);
            fail();
        } catch (SQLException e) {
            assertThat(e.getMessage().contains(EntityException.COMUNIDAD_FK), is(true));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_3() throws Exception
    {
        // No existe userName: registra la incidservice; no hay restricción de integridad.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia("no_existo", "Nueva incidservice en Cámaras de vigilancia", IncidenciaTestUtils.calle_la_fuente_11.getC_Id(), (short) 11);
        assertThat(incidenciaDao.regIncidencia(incidencia) > 0, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_1() throws EntityException
    {
        // Caso OK.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.paco.getUserName(), 4L, IncidenciaTestUtils.paco_plazuela23.getComunidad().getC_Id(), (short) 31);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.paco.getUserName(), "resol_incid_4_4", 111, Instant.now().plus(12, ChronoUnit.DAYS));
        assertThat(incidenciaDao.regResolucion(resolucion), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_2() throws EntityException
    {
        // Caso: dos resoluciones para una misma incidservice.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.paco.getUserName(), 4L, IncidenciaTestUtils.paco_plazuela23.getComunidad().getC_Id(), (short) 31);
        Resolucion resolucion_1 = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.paco.getUserName(), "resol_incid_4_4_A", 111, Instant.now().plus(12, ChronoUnit.DAYS));
        Resolucion resolucion_2 = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.paco.getUserName(), "resol_incid_4_4_B", 111, Instant.now().plus(13, ChronoUnit.DAYS));
        assertThat(incidenciaDao.regResolucion(resolucion_1), is(1));
        try {
            incidenciaDao.regResolucion(resolucion_2);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(RESOLUCION_DUPLICATE));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion_3()
    {
        // Caso: incidservice no existe en BD.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.juan.getUserName(), 999L, IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 31);
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.juan.getUserName(), "resol_incid_4_4", 111, Instant.now().plus(12, ChronoUnit.DAYS));
        try {
            incidenciaDao.regResolucion(resolucion);
            fail();
        } catch (EntityException e) {
            // NO existe la combinación incidservice + comunidad.
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeAvancesByResolucion_1() throws EntityException
    {
        // Caso: resolución con avances.

        List<Avance> avances = incidenciaDao.seeAvancesByResolucion(3L);
        assertThat(avances.size(), is(2));
        assertThat(avances.get(0), allOf(
                hasProperty("avanceId", is(1L)),
                hasProperty("avanceDesc", is("descripcion_avance_1_3")),
                hasProperty("userName", CoreMatchers.is(IncidenciaTestUtils.pedro.getUserName()))
        ));
        assertThat(avances.get(0).getFechaAlta().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeAvancesByResolucion_2()
    {
        // Caso: resolución sin avances.
        assertThat(incidenciaDao.seeAvancesByResolucion(2L).size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
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
                                hasProperty("userName", is("pedro@pedro.com")),
                                hasProperty("alias", is("pedronevado"))
                        )
                ),
                hasProperty("fechaAlta", notNullValue())
        ));

        comments = incidenciaDao.SeeCommentsByIncid(4L);
        assertThat(comments.size(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeCommentsByIncid_2()
    {
        // La incidservice no existe. La comunidad, sí.
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(999L);
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_1() throws SQLException, EntityException
    {
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(1L);

        IncidImportancia incidImportancia = incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.pedro.getUserName(), 1L);
        assertThat(incidImportancia.getIncidencia().getIncidenciaId(), is(incidencia.getIncidenciaId()));
        assertThat(incidImportancia.getUserComu().getUsuario().getuId(), CoreMatchers.is(IncidenciaTestUtils.pedro.getuId()));
        assertThat(incidImportancia.getUserComu().getUsuario().getAlias(), CoreMatchers.is(IncidenciaTestUtils.pedro.getAlias()));
        assertThat(incidImportancia.getUserComu().getUsuario().getUserName(), CoreMatchers.is(IncidenciaTestUtils.pedro.getUserName()));
        assertThat(incidImportancia.getFechaAlta().getTime() > 0L, is(true));
        assertThat(incidImportancia.getImportancia(), is((short) 2));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_2() throws EntityException
    {
        // Caso: existe incidservice; existe usuario, pero no existe el par incidencia_usuario.
        assertThat(incidenciaDao.seeIncidenciaById(4L), notNullValue());
        try {
            incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.juan.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportanciaByUser_3() throws EntityException
    {
        // Caso: no existe la incidservice, existe el usuario.
        try {
            incidenciaDao.seeIncidImportanciaByUser(IncidenciaTestUtils.juan.getUserName(), 999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCID_IMPORTANCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsClosedByComu_1() throws EntityException
    {
        // CASO OK.
        List<IncidenciaUser> incidenciaUsers = incidenciaDao.seeIncidsClosedByComu(IncidenciaTestUtils.calle_plazuela_23.getC_Id());
        assertThat(incidenciaUsers.size(), is(1));
        assertThat(incidenciaUsers.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_5")),
                hasProperty("fechaCierre", notNullValue())
        ));
        // Verificamos antigüedad < 2 años.
        assertThat(incidenciaUsers.get(0).getIncidencia().getFechaAlta().compareTo(Timestamp.from(Instant.now().minus(730, ChronoUnit.DAYS))) > 0, is(true));

        // CASO OK : no obtiene una incidservice que sobrepasa la antigüedad máxima.
        incidenciaUsers = incidenciaDao.seeIncidsClosedByComu(IncidenciaTestUtils.calle_olmo_55.getC_Id());
        assertThat(incidenciaUsers.size(), is(0));
        // Buscamos incidservice y verificamos antigüedad.
        Incidencia incidencia = incidenciaDao.seeIncidenciaById(7L);
        assertThat(incidencia, allOf(
                hasProperty("descripcion", is("incidencia_7_6")),
                hasProperty("fechaCierre", notNullValue())
        ));
        // Verificamos antigüedad > 2 años.
        assertThat(incidencia.getFechaAlta().compareTo(Timestamp.from(Instant.now().minus(730, ChronoUnit.DAYS))) < 0, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1()
    {
        // CASO NOT OK : si comunidadId no existe, devuelve una lista vacía.
        assertThat(incidenciaDao.seeIncidsOpenByComu(999L), notNullValue());
        assertThat(incidenciaDao.seeIncidsOpenByComu(999L).size(), is(0));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2()
    {
        // Caso OK: el usuario iniciador aún continúa como usuario de la aplicación.

        List<IncidenciaUser> incidenciaUsers = incidenciaDao.seeIncidsOpenByComu(IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id());
        assertThat(incidenciaUsers.size(), is(2));
        IncidenciaUser incidenciaUser = incidenciaUsers.get(0);
        assertThat(incidenciaUser.getIncidencia().getIncidenciaId(), is(1L));
        assertThat(incidenciaUser.getIncidencia().getUserName(), CoreMatchers.is(IncidenciaTestUtils.luis.getUserName()));
        assertThat(incidenciaUser.getIncidencia().getComunidad().getC_Id(), CoreMatchers.is(IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()));
        assertThat(incidenciaUser.getIncidencia().getDescripcion(), is("incidencia_1"));
        assertThat(incidenciaUser.getIncidencia().getAmbitoIncidencia().getAmbitoId(), is((short) 41));
        assertThat(incidenciaUser.getIncidencia().getFechaAlta().getTime() > 1000L, is(true));
        assertThat(incidenciaUser.getIncidencia().getFechaCierre(), nullValue());
        assertThat(incidenciaUser.getIncidencia().getImportanciaAvg(), is(1.5f));
        assertThat(incidenciaUser.getUsuario().getuId(), CoreMatchers.is(IncidenciaTestUtils.luis.getuId()));
        // Username null en Usuario: para evitar duplicar el dato con incidservice.userName.
        assertThat(incidenciaUser.getUsuario().getUserName(), nullValue());
        assertThat(incidenciaUser.getUsuario().getAlias(), CoreMatchers.is(IncidenciaTestUtils.luis.getAlias()));
        // Fecha alta resolución nula: no tiene resolución.
        assertThat(incidenciaUser.getFechaAltaResolucion(), nullValue());

        // Fecha alta OK: incidservice abierta con resolución.
        incidenciaUser = incidenciaUsers.get(1);
        assertThat(incidenciaUser.getFechaAltaResolucion(), notNullValue());
        assertThat(incidenciaUser.getFechaAltaResolucion().getTime() > 0L, is(true));

        incidenciaUsers = incidenciaDao.seeIncidsOpenByComu(IncidenciaTestUtils.calle_la_fuente_11.getC_Id());
        assertThat(incidenciaUsers.size(), is(1));
        assertThat(incidenciaUsers.get(0).getFechaAltaResolucion(), nullValue());

        incidenciaUsers = incidenciaDao.seeIncidsOpenByComu(IncidenciaTestUtils.calle_plazuela_23.getC_Id());
        // Hay una incidservice cerrada. La abierta no tiene resolución.
        assertThat(incidenciaUsers.size(), is(1));
        assertThat(incidenciaUsers.get(0).getFechaAltaResolucion(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3() throws SQLException
    {
        // Caso OK: el usuario iniciador ya no es usuario de la aplicación.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia("no_existiré_user", "Incidencia_test", IncidenciaTestUtils.calle_la_fuente_11.getC_Id(), (short) 11);
        assertThat(incidenciaDao.regIncidencia(incidencia) > 1L, is(true));

        List<IncidenciaUser> incidenciaUsers = incidenciaDao.seeIncidsOpenByComu(IncidenciaTestUtils.calle_la_fuente_11.getC_Id());
        assertThat(incidenciaUsers.size(), is(2));
        IncidenciaUser incidenciaUser = incidenciaUsers.get(1);
        assertThat(incidenciaUser.getIncidencia().getUserName(), is(incidencia.getUserName()));
        assertThat(incidenciaUser.getIncidencia().getDescripcion(), is("Incidencia_test"));
        // No tiene registro incidImportancia.
        assertThat(incidenciaUser.getIncidencia().getImportanciaAvg(), is(0f));
        // Usuario null.
        assertThat(incidenciaUser.getUsuario(), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws EntityException
    {
        // Caso: resolución con avances.

        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        assertThat(resolucion, allOf(
                hasProperty("userName", CoreMatchers.is(IncidenciaTestUtils.pedro.getUserName())),
                hasProperty("descripcion", is("plan_resol_3")),
                hasProperty("costeEstimado", is(11)),
                hasProperty("costeFinal", is(11)),
                hasProperty("moraleja", is("moraleja_3")),
                hasProperty("incidencia", is(new Incidencia.IncidenciaBuilder().incidenciaId(3L).build())),
                hasProperty("avances", is(incidenciaDao.seeAvancesByResolucion(3L)))
        ));
        assertThat(resolucion.getFechaAlta().getTime() > 0L, is(true));
        assertThat(resolucion.getFechaPrev().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_2() throws EntityException
    {
        // Caso: resolución sin avances.

        Resolucion resolucion = incidenciaDao.seeResolucion(5L);
        assertThat(resolucion, allOf(
                hasProperty("userName", CoreMatchers.is(IncidenciaTestUtils.paco.getUserName())),
                hasProperty("descripcion", is("plan_resol_5")),
                hasProperty("costeEstimado", is(22)),
                hasProperty("costeFinal", is(23)),
                hasProperty("moraleja", is("moraleja_5")),
                hasProperty("incidencia", is(new Incidencia.IncidenciaBuilder().incidenciaId(5L).build())),
                hasProperty("avances", is(incidenciaDao.seeAvancesByResolucion(5L)))
        ));
        assertThat(resolucion.getAvances().size(), is(0));
        assertThat(resolucion.getFechaAlta().getTime() > 0L, is(true));
        assertThat(resolucion.getFechaPrev().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_3()
    {
        // Caso: incidservice sin resolución.

        try {
            incidenciaDao.seeResolucion(4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(RESOLUCION_NOT_FOUND));
        }
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1()
    {
        // CASOS OK.

        List<ImportanciaUser> importanciaUsers = incidenciaDao.seeUserComusImportancia(2L);
        assertThat(importanciaUsers.size(), is(2));
        assertThat(importanciaUsers.get(0), allOf(
                hasProperty("userAlias", CoreMatchers.is(IncidenciaTestUtils.juan.getAlias())),
                hasProperty("importancia", is((short) 4))
        ));
        assertThat(importanciaUsers.get(1), allOf(
                hasProperty("userAlias", CoreMatchers.is(IncidenciaTestUtils.pedro.getAlias())),
                hasProperty("importancia", is((short) 3))
        ));

        importanciaUsers = incidenciaDao.seeUserComusImportancia(4L);
        assertThat(importanciaUsers.size(), is(1));
        assertThat(importanciaUsers.get(0), allOf(
                hasProperty("userAlias", CoreMatchers.is(IncidenciaTestUtils.paco.getAlias())),
                hasProperty("importancia", is((short) 4))
        ));

        // CASO: incidservice no existe.
        importanciaUsers = incidenciaDao.seeUserComusImportancia(999L);
        assertThat(importanciaUsers, notNullValue());
        assertThat(importanciaUsers.size(), is(0));
    }
}