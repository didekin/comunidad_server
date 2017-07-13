package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
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
public abstract class IncidenciaRepoServiceTest {

    @Autowired
    private IncidenciaRepoServiceIf incidenciaService;

    @Autowired
    private IncidenciaDao incidenciaDao;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testInitialization()
    {
        assertThat(incidenciaDao, is(notNullValue()));
        assertThat(incidenciaService, notNullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testCloseIncidencia_1()
    {
        // Caso NOT OK: la incidservice is closed.
        try {
            incidenciaService.closeIncidencia(5L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testIsIncidenciaWithResolucion_1() throws EntityException, InterruptedException
    {
        assertThat(incidenciaService.isIncidenciaWithResolucion(4L), is(false));
        assertThat(incidenciaService.isIncidenciaWithResolucion(3L), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_1() throws EntityException
    {
        // Caso OK: devuelve 1.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithIdDescUsername(IncidenciaTestUtils.paco.getUserName(), 4L, "desc_mod", IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 12);
        assertThat(incidenciaService.modifyIncidencia(incidencia), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_2()
    {
        // Datos no existentes en BD.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.pedro.getUserName(), 999L, 999L, (short) 21);

        try {
            incidenciaService.modifyIncidencia(incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidencia_3() throws EntityException
    {
        // Caso: incidservice cerrada.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithIdDescUsername(IncidenciaTestUtils.paco.getUserName(), 5L, "desc_mod", IncidenciaTestUtils.calle_plazuela_23.getC_Id(), (short) 12);
        try {
            incidenciaService.modifyIncidencia(incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_1() throws EntityException
    {
        // Caso OK: SIN registro previo de incidImportancia.
        // Premisas.
        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.paco.getUserName(), 6L);
        assertThat(incidImportancia.getUserComu(), nullValue());
        assertThat(incidImportancia.getImportancia(), is((short) 0));
        Incidencia incidencia = incidImportancia.getIncidencia();
        assertThat(incidencia.getIncidenciaId(), is(6L));

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 3).usuarioComunidad(IncidenciaTestUtils.paco_olmo).build();
        assertThat(incidenciaService.modifyIncidImportancia(incidImportancia), CoreMatchers.is(1));
        incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.paco.getUserName(), incidencia.getIncidenciaId());
        assertThat(incidImportancia.getImportancia(), is((short) 3));
        assertThat(incidImportancia.getUserComu(), CoreMatchers.is(IncidenciaTestUtils.paco_olmo));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_2() throws EntityException
    {
        // Caso: SIN registro previo de incidImportancia con incidservice cerrada.
        // Premisas.
        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.juan.getUserName(), 5L);
        assertThat(incidImportancia.getUserComu(), nullValue());
        assertThat(incidImportancia.getImportancia(), is((short) 0));
        Incidencia incidencia = incidImportancia.getIncidencia();
        assertThat(incidencia.getFechaCierre(), notNullValue());

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 3)
                .usuarioComunidad(IncidenciaTestUtils.juan_plazuela23)
                .build();

        try {
            incidenciaService.modifyIncidImportancia(incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyIncidImportancia_3() throws EntityException
    {
        // Caso: CON registro previo de incidImportancia, sin cambiar nada.
        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.paco.getUserName(), 4L);
        assertThat(incidImportancia.getUserComu(), CoreMatchers.is(IncidenciaTestUtils.paco_plazuela23));
        assertThat(incidImportancia.getImportancia(), is((short) 4));
        Incidencia incidencia = incidImportancia.getIncidencia();

        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 4).usuarioComunidad(IncidenciaTestUtils.paco_plazuela23).build();
        assertThat(incidenciaService.modifyIncidImportancia(incidImportancia), CoreMatchers.is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_1() throws EntityException
    {
        // Caso OK: devuelve 2.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        // Verificamos estado anterior.
        assertThat(resolucion.getCosteEstimado(), CoreMatchers.is(11));
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(2));

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
        assertThat(incidenciaService.modifyResolucion(resolucion), is(2));
        resolucion = incidenciaService.seeResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(3));
        assertThat(resolucion.getCosteEstimado(), CoreMatchers.is(1111));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_2() throws EntityException
    {
        // Caso OK: devuelve 1; no añadimos avances.
        Resolucion resolucion = incidenciaDao.seeResolucion(3L);
        assertThat(resolucion.getAvances().size(), is(2));
        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(1111)
                .build();

        assertThat(incidenciaService.modifyResolucion(resolucion), is(1));
        resolucion = incidenciaService.seeResolucion(resolucion.getIncidencia().getIncidenciaId());
        assertThat(resolucion.getAvances().size(), CoreMatchers.is(2));
        assertThat(resolucion.getCosteEstimado(), CoreMatchers.is(1111));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_3() throws EntityException
    {
        // Caso not OK: la incidservice está cerrada.
        Resolucion resolucion = incidenciaService.seeResolucion(5L);
        Incidencia incidencia = incidenciaService.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre(), notNullValue());

        resolucion = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .costeEstimado(2232)
                .build();
        try {
            incidenciaService.modifyResolucion(resolucion);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testModifyResolucion_4() throws EntityException
    {
        // Caso NOT OK: la incidservice no existe.

        Resolucion resolucion = incidenciaService.seeResolucion(3L);

        // Nuevos datos.
        Avance avance = new Avance.AvanceBuilder().avanceDesc("avance3").userName(resolucion.getUserName()).build();
        List<Avance> avances = new ArrayList<>(1);
        avances.add(avance);
        resolucion = new Resolucion.ResolucionBuilder(new Incidencia.IncidenciaBuilder().incidenciaId(999L).build())
                .copyResolucion(resolucion)
                .avances(avances)
                .build();
        try {
            incidenciaService.modifyResolucion(resolucion);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), CoreMatchers.is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidComment_1() throws EntityException
    {
        // Incidencia is closed.
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 1L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = IncidenciaTestUtils.doComment("comment_userComu_NoDb", incidencia, IncidenciaTestUtils.pedro);
        incidenciaDao.closeIncidencia(incidencia.getIncidenciaId());
        assertThat(incidenciaService.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), notNullValue());

        try {
            incidenciaService.regIncidComment(comment);
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
        Incidencia incidencia = IncidenciaTestUtils.doIncidenciaWithId(IncidenciaTestUtils.luis.getUserName(), 1L, IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 28);
        IncidComment comment = IncidenciaTestUtils.doComment("comment_userComu_NoDb", incidencia, IncidenciaTestUtils.pedro);
        assertThat(incidenciaService.seeIncidenciaById(incidencia.getIncidenciaId()).getFechaCierre(), nullValue());

        assertThat(incidenciaService.regIncidComment(comment), is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_1() throws Exception
    {
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "Incidencia de test", 1L, (short) 24);
        assertThat(incidenciaService.regIncidencia(incidencia) > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidencia_2() throws Exception
    {
        // Caso: comunidad no existe.
        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(IncidenciaTestUtils.luis.getUserName(), "Incidencia de test", 999L, (short) 24);
        try {
            incidenciaService.regIncidencia(incidencia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_1() throws Exception
    {
        // Caso: incidservice cerrada.
        Incidencia incidencia = incidenciaService.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre().getTime() < new Date().getTime(), is(true));
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia).importancia((short) 1).build();
        try {
            incidenciaService.regIncidImportancia(incidImportancia);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegResolucion() throws EntityException
    {
        // Caso: la incidservice ya está cerrada. Incidencia 5, comunidad 4.
        // Devuelve INCIDENCIA_NOT_FOUND, porque no está entre las incidencias ABIERTAS.
        Incidencia incidencia = incidenciaService.seeIncidenciaById(5L);
        assertThat(incidencia.getFechaCierre().getTime() < new Date().getTime(), is(true));
        Resolucion resolucion = IncidenciaTestUtils.doResolucion(incidencia, IncidenciaTestUtils.paco.getUserName(), "resol_incid_5_4", 111, Instant.now().plus(12, ChronoUnit.DAYS));
        try {
            incidenciaService.regResolucion(resolucion);
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
        // La incidservice no existe. La comunidad, sí.
        try {
            incidenciaService.seeCommentsByIncid(999L);
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
        // La incidservice existe, no tiene comentarios.
        List<IncidComment> comments = incidenciaService.seeCommentsByIncid(2L);
        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_1() throws EntityException, InterruptedException, SQLException
    {
        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.paco.getUserName(), 4L);
        assertThat(incidImportancia.getIncidencia().getIncidenciaId(), is(4L));
        assertThat(incidImportancia.getIncidencia().getComunidad().getC_Id(), is(4L));
        assertThat(incidImportancia.getUserComu().getComunidad().getC_Id(), is(4L));
        assertThat(incidImportancia.getUserComu().getUsuario().getuId(), Is.is(IncidenciaTestUtils.paco.getuId()));
        assertThat(incidImportancia.getUserComu().getUsuario().getUserName(), Is.is(IncidenciaTestUtils.paco.getUserName()));
        assertThat(incidImportancia.getUserComu().getUsuario().getAlias(), Is.is(IncidenciaTestUtils.paco.getAlias()));
        assertThat(incidImportancia.getImportancia(), is((short) 4));
        assertThat(incidImportancia.getFechaAlta().getTime() > 0L, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidImportancia_2() throws EntityException
    {
        // Caso: no existe la incidservice; existe el usuario.
        try {
            incidenciaService.seeIncidImportancia(IncidenciaTestUtils.paco.getUserName(), 999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }

        // Caso: no existe el usuario; existe la incidencia.
        Incidencia incidencia = incidenciaService.seeIncidenciaById(4L);
        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia("noexisto_user", 4L);
        assertThat(incidImportancia, allOf(
                hasProperty("userComu", nullValue()),
                hasProperty("importancia", is((short) 0)),
                hasProperty("fechaAlta", nullValue()),
                hasProperty("incidencia", allOf(
                        hasProperty("fechaAlta", notNullValue()),
                        hasProperty("descripcion", is(incidencia.getDescripcion())),
                        hasProperty("userName", is(incidencia.getUserName()))
                ))
        ));

        // Caso: no existe el usuario, ni la incidencia.
        try {
            incidenciaService.seeIncidImportancia("noexisto_user", 999L);
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
        // Caso: usuario.comunidad = incidencia.comunidad, pero no hay registro incidImportancia para el usuario.
        // Incidencia 5, juan_no_auth.
        Incidencia incidencia = incidenciaService.seeIncidenciaById(5L);
        assertThat(incidencia.getComunidad().equals(IncidenciaTestUtils.juan_plazuela23.getComunidad()), is(true));

        IncidImportancia incidImportancia = incidenciaService.seeIncidImportancia(IncidenciaTestUtils.juan.getUserName(), incidencia.getIncidenciaId());
        assertThat(incidImportancia, allOf(
                hasProperty("incidencia", is(incidencia)),
                hasProperty("importancia", is((short) 0)),
                hasProperty("userComu", nullValue())
        ));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_1() throws EntityException, InterruptedException
    {
        List<IncidenciaUser> incidenciasUser = incidenciaService.seeIncidsOpenByComu(IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id());
        assertThat(incidenciasUser.size(), is(2));
        assertThat(incidenciasUser.get(0).getIncidencia(), allOf(
                hasProperty("descripcion", is("incidencia_1")),
                hasProperty("ambitoIncidencia", is(new AmbitoIncidencia((short) 41))),
                hasProperty("importanciaAvg", is(1.5f)),
                hasProperty("incidenciaId", is(1L)),
                hasProperty("comunidad", Is.is(IncidenciaTestUtils.ronda_plazuela_10bis)),
                hasProperty("userName", Is.is(IncidenciaTestUtils.luis.getUserName())),
                hasProperty("fechaAlta", notNullValue()),
                hasProperty("fechaCierre", nullValue())
        ));
        assertThat(incidenciasUser.get(0),
                hasProperty("usuario", allOf(
                        hasProperty("uId", Is.is(IncidenciaTestUtils.luis.getuId())),
                        hasProperty("userName", nullValue()),
                        hasProperty("alias", Is.is(IncidenciaTestUtils.luis.getAlias()))
                ))
        );

        // Segunda incidservice en la misma comunidad.
        assertThat(incidenciasUser.get(1).getIncidencia(),
                allOf(
                        hasProperty("descripcion", is("incidencia_3")),
                        hasProperty("userName", Is.is(IncidenciaTestUtils.pedro.getUserName())),
                        hasProperty("importanciaAvg", is(1f))
                ));
        assertThat(incidenciasUser.get(1), allOf(
                hasProperty("usuario", hasProperty("alias", Is.is(IncidenciaTestUtils.pedro.getAlias()))),
                hasProperty("fechaAltaResolucion", notNullValue()))
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_2() throws EntityException
    {
        List<IncidenciaUser> incidenciasUser = incidenciaService.seeIncidsOpenByComu(IncidenciaTestUtils.calle_la_fuente_11.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_2")));
        assertThat(incidenciasUser.get(0).getIncidencia().getImportanciaAvg(), CoreMatchers.is((float) 3.5));

        incidenciasUser = incidenciaService.seeIncidsOpenByComu(IncidenciaTestUtils.calle_plazuela_23.getC_Id());
        assertThat(incidenciasUser.size(), is(1));
        assertThat(incidenciasUser.get(0).getIncidencia(), hasProperty("descripcion", is("incidencia_4")));
        assertThat(incidenciasUser.get(0).getIncidencia().getImportanciaAvg(), CoreMatchers.is((float) 4));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeIncidsOpenByComu_3() throws EntityException
    {
        // Caso: no incidencias en la comunidad.
        List<IncidenciaUser> incidenciaUsers = incidenciaService.seeIncidsOpenByComu(999L);
        assertThat(incidenciaUsers.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_incidencia_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeResolucion_1() throws EntityException
    {
        // Caso: NO hay resolución para la incidservice.
        assertThat(incidenciaService.seeResolucion(4L), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_a.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testSeeUserComusImportancia_1()
    {
        // CASO: incidservice no existe.
        try {
            incidenciaService.seeUserComusImportancia(999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(INCIDENCIA_NOT_FOUND));
        }
    }

//    ================================ Helper methods =======================

}