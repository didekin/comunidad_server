package com.didekin.userservice.repository;

import com.didekin.common.repository.EntityException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_10;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 19/04/15
 * Time: 11:18
 */
@SuppressWarnings({"ThrowFromFinallyBlock", "Duplicates"})
public abstract class ComunidadDaoTest {

    @Autowired
    private ComunidadDao comunidadDao;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private UsuarioManagerIf sujetosService;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test(expected = EntityException.class)
    public void testDeleteComunidadOk() throws EntityException
    {
        Comunidad comunidad = comunidadDao.getComunidadById(3L);
        boolean isDeleted = comunidadDao.deleteComunidad(comunidad);
        assertThat(isDeleted, is(true));
        // Throw an exception if the comunidad does not exist.
        comunidad = comunidadDao.getComunidadById(3L);
        assertThat(comunidad, nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteComunidadWrong()
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(11L).build();
        // The comunidad does not exist.
        try {
            comunidadDao.deleteComunidad(comunidad);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testExistsUserComu()
    {
        assertThat(comunidadDao.existsUserComu(1L, 3L), is(true));
        // No existe el par, pero sí cada elemento.
        assertThat(comunidadDao.existsUserComu(1L, 7L), is(false));
        // No existe ni la comuidad, ni el usuario.
        assertThat(comunidadDao.existsUserComu(111L, 333L), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComunidadByPk() throws EntityException
    {
        Comunidad comunidad = comunidadDao.getComunidadById(3L);
        assertThat(comunidad.getNombreVia(), is("de El Escorial"));
    }

    /* Assume municipio table is populated.*/
    @Test
    public void testGetMunicipioId()
    {
            /*Municipio(short cdInProvincia, short provinciaId)*/
        Municipio municipio = new Municipio((short) 3, new Provincia((short) 13));
        int municipioId = comunidadDao.getMunicipioId(municipio);
        assertThat(municipioId > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testGetUsuariosIdByComunidad()
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        List<Long> usuariosId = comunidadDao.getUsuariosIdFromComunidad(comunidad);
        assertThat(usuariosId.size(), is(2));
        assertThat(usuariosId, hasItems(3L, 5L));

        comunidad = new Comunidad.ComunidadBuilder().c_id(3L).build();
        usuariosId = comunidadDao.getUsuariosIdFromComunidad(comunidad);
        assertThat(usuariosId.size(), is(1));
        assertThat(usuariosId, hasItems(3L));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertComunidad_1() throws Exception
    {
        Connection conn = null;
        long pkComunidad;
        try {
            conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
            assertThat(COMU_LA_PLAZUELA_10.getC_Id(), is(0L));
            pkComunidad = comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        assertThat(pkComunidad > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertComunidad_2() throws Exception
    {
        Connection conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
        comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn);
        if (conn != null) {
            conn.close();
        }

        try {
            conn = comunidadDao.getJdbcTemplate().getDataSource().getConnection();
            comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), allOf(containsString(EntityException.DUPLICATE_ENTRY), containsString(EntityException.COMUNIDAD_UNIQUE_KEY)));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuarioComunidad() throws EntityException
    {
        // Necesito userName, no sólo uId, como en el caso de la comunidad.

        UsuarioComunidad usuarioCom = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, paco)
                .portal("portal")
                .escalera("esc")
                .planta("1")
                .puerta("door")
                .roles(INQUILINO.function.concat(",").concat(PRESIDENTE.function))
                .build();

        int rowInserted = comunidadDao.insertUsuarioComunidad(usuarioCom);
        assertThat(rowInserted, is(1));
        List<UsuarioComunidad> usuariosComunidad = usuarioDao.seeUserComusByUser(paco.getUserName());
        // Check.
        assertThat(usuariosComunidad.get(2),
                allOf(
                        hasProperty("roles", is("pre,inq")),
                        hasProperty("portal", is(usuarioCom.getPortal())),
                        hasProperty("escalera", is(usuarioCom.getEscalera())),
                        hasProperty("planta", is(usuarioCom.getPlanta())),
                        hasProperty("puerta", is(usuarioCom.getPuerta())),
                        hasProperty("comunidad", is(calle_el_escorial)),
                        hasProperty("usuario", is(paco))
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuarioComunidad_3() throws EntityException
    {
        boolean insertedRow = sujetosService.regComuAndUserAndUserComu(COMU_REAL_PEPE, oneComponent_local_ES);
        assertThat(insertedRow, is(true));

        Comunidad comunidad = usuarioDao.getComusByUser(USER_PEPE.getUserName()).get(0);
        Usuario usuario = usuarioDao.getUserByUserName(USER_PEPE.getUserName());
        UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                .userComuRest(COMU_REAL_PEPE)
                .build();
        try {
            comunidadDao.insertUsuarioComunidad(userComu);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), allOf(containsString(EntityException.DUPLICATE_ENTRY), containsString(EntityException.USER_COMU_PK)));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws EntityException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(4L).tipoVia("nuevo_tipo").nombreVia
                ("nuevo_nombre_via").numero((short) 22).municipio(new Municipio((short) 2, new Provincia((short) 13)))
                .build();
        assertThat(comunidadDao.modifyComuData(comunidad), is(1));
        Comunidad comunidadDb = comunidadDao.getComunidadById(4L);
        assertThat(comunidadDb, allOf(
                hasProperty("nombreVia", equalTo(comunidad.getNombreVia())),
                hasProperty("tipoVia", equalTo(comunidad.getTipoVia())),
                hasProperty("numero", equalTo(comunidad.getNumero())),
                hasProperty("sufijoNumero", equalTo(comunidad.getSufijoNumero())),
                hasProperty("municipio", equalTo(comunidad.getMunicipio()))
        ));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testSearchComuidad_1()
    {
        // Existe comunidad en DB.
        List<Comunidad> comunidades = comunidadDao.searchComunidadOne(COMU_LA_PLAZUELA_10);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(COMU_LA_PLAZUELA_10))); // difieren en el sufijo número.
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getMunicipio().getCodInProvincia(), is((short) 52));
    }

    @Test()
    public void testSearchComuidad_2()
    {
        // NO existe comunidad en DB.
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Ronda")
                .nombreVia("no existe")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();
        List<Comunidad> comunidades = comunidadDao.searchComunidadOne(comunidad);
        assertThat(comunidades, notNullValue());
        assertThat(comunidades.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_3()
    {
        // NO consideramos tipo_via en la búsqueda. Resto de datos coincide con entrada en DB.

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Travesía")
                .nombreVia("de la Plazuela")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadTwo(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el sufijo número.
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getMunicipio().getCodInProvincia(), is((short) 52));

        assertThat(comunidades.get(0).getTipoVia(), is("Ronda")); // Y no Travesía.
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_4()
    {
        // NO consideramos tipo_via en la búsqueda.
        /*select name from metal where name like '%param'; */

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("Plazuela")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_5()
    {
        /*select name from metal where name = LEFT('namexxx',length(name));*/

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Travesía")
                .nombreVia("de la Plazuela Nueva")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_6()
    {
        /*select name from metal where name like 'param%';*/
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("de la Plazu")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_7()
    {
        /*select name from metal where name = RIGHT('xxxname',length(name));*/
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("Atajo de la Plazuela")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_8()
    {
        /* select name from metal where name like '%param%';*/

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("Plazu")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_9() throws SQLException, EntityException
    {
        /*select name from metal where name = RIGHT('xxxname',length(name));*/
        // Diferencia con test_7: datos de password encriptados en DB.

        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("de la Mujer de la Plazuela")
                .numero((short) 10)
                .sufijoNumero("Bis")
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, juan, "portal1", "esc2", "planta3", "puerta12",
                PROPIETARIO.function);
        boolean rowInserted = sujetosService.regComuAndUserComu(userComu);
        assertThat(rowInserted, is(true));

        // Datos de comunidad de búsqueda.
        Comunidad comunidadSearch = new Comunidad.ComunidadBuilder()
                .tipoVia("Ronda")
                .nombreVia("de la Plazuela")
                .numero((short) 10)
                .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidadSearch);
        assertThat(comunidades.size(), is(2));
        assertThat(comunidades.get(0).getTipoVia(), is("Calle"));
        assertThat(comunidades.get(0).getNombreVia(), is("de la Mujer de la Plazuela"));
        assertThat(comunidades.get(1).getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(1).getNombreVia(), is("de la Plazuela"));
    }
}