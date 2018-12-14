package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.model.entidad.Domicilio;
import com.didekinlib.model.entidad.Municipio;
import com.didekinlib.model.entidad.Provincia;
import com.didekinlib.model.entidad.comunidad.Comunidad;
import com.didekinlib.model.relacion.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.usuario.Usuario;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.util.List;

import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_10;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekinlib.model.entidad.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static java.util.Objects.requireNonNull;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({LocalDev.class, DbPre.class})
public class ComunidadDaoDbPreDevTest {

    @Autowired
    private ComunidadDao comunidadDao;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private UsuarioManager sujetosService;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test(expected = ServiceException.class)
    public void testDeleteComunidadOk() throws ServiceException
    {
        Comunidad comunidad = comunidadDao.getComunidadById(3L);
        boolean isDeleted = comunidadDao.deleteComunidad(comunidad);
        assertThat(isDeleted, is(true));
        // Throw an exception if the comunidad does not exist.
        comunidad = comunidadDao.getComunidadById(3L);
        assertThat(comunidad, nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteComunidadWrong()
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(11L).build();
        // The comunidad does not exist.
        try {
            comunidadDao.deleteComunidad(comunidad);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComunidadByPk() throws ServiceException
    {
        Comunidad comunidad = comunidadDao.getComunidadById(3L);
        assertThat(comunidad.getDomicilio().getNombreVia(), is("de El Escorial"));
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
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
        long pkComunidad;
        try (Connection conn = requireNonNull(comunidadDao.getJdbcTemplate().getDataSource()).getConnection()) {
            assertThat(COMU_LA_PLAZUELA_10.getId(), is(0L));
            pkComunidad = comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn);
        }
        assertThat(pkComunidad > 0, is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertComunidad_2() throws Exception
    {
        Connection conn = requireNonNull(comunidadDao.getJdbcTemplate().getDataSource()).getConnection();
        comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn);
        // Admite alta duplicada.
        assertThat(comunidadDao.insertComunidad(COMU_LA_PLAZUELA_10, conn) > 0, is(true));
        if (conn != null) {
            conn.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuarioComunidad() throws ServiceException
    {
        // Necesito userName, no sólo uId, como en el caso de la comunidad.

        UsuarioComunidad usuarioCom = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, paco)
                .portal("portal")
                .escalera("esc")
                .planta("1")
                .puerta("door")
                .build();

        int rowInserted = comunidadDao.insertUsuarioComunidad(usuarioCom);
        assertThat(rowInserted, is(1));
        List<UsuarioComunidad> usuariosComunidad = usuarioDao.seeUserComusByUser(paco.getUserName());
        // Check.
        assertThat(usuariosComunidad.get(2),
                allOf(
                        hasProperty("portal", is(usuarioCom.getPortal())),
                        hasProperty("escalera", is(usuarioCom.getEscalera())),
                        hasProperty("planta", is(usuarioCom.getPlanta())),
                        hasProperty("puerta", is(usuarioCom.getPuerta())),
                        hasProperty("entidad", is(calle_el_escorial)),
                        hasProperty("usuario", is(paco))
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuarioComunidad_3() throws ServiceException
    {
        boolean insertedRow = sujetosService.regComuAndUserAndUserComu(COMU_REAL_PEPE, oneComponent_local_ES);
        assertThat(insertedRow, is(true));

        Comunidad comunidad = usuarioDao.getComusByUser(USER_PEPE.getUserName()).get(0);
        Usuario usuario = usuarioDao.getUserDataByName(USER_PEPE.getUserName());
        UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(comunidad, usuario)
                .userComuRest(COMU_REAL_PEPE)
                .build();
        try {
            comunidadDao.insertUsuarioComunidad(userComu);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), allOf(containsString(ServiceException.DUPLICATE_ENTRY), containsString(ServiceException.USER_COMU_PK)));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws ServiceException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(4L)
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("nuevo_tipo")
                        .nombreVia("nuevo_nombre_via")
                        .numero((short) 22)
                        .municipio(new Municipio((short) 2, new Provincia((short) 13)))
                        .build())
                .build();
        assertThat(comunidadDao.modifyComuData(comunidad), is(1));
        Comunidad comunidadDb = comunidadDao.getComunidadById(4L);
        assertThat(comunidadDb, hasProperty("domicilio", allOf(
                hasProperty("nombreVia", equalTo(comunidad.getDomicilio().getNombreVia())),
                hasProperty("tipoVia", equalTo(comunidad.getDomicilio().getTipoVia())),
                hasProperty("numero", equalTo(comunidad.getDomicilio().getNumero())),
                hasProperty("sufijoNumero", equalTo(comunidad.getDomicilio().getSufijoNumero())),
                hasProperty("municipio", equalTo(comunidad.getDomicilio().getMunicipio()))
        )));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testSearchComuidad_1()
    {
        // Existe comunidad en DB.
        List<Comunidad> comunidades = comunidadDao.searchComunidadOne(COMU_LA_PLAZUELA_10);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(COMU_LA_PLAZUELA_10))); // difieren en el sufijo número.
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getCodInProvincia(), is((short) 52));
    }

    @Test()
    public void testSearchComuidad_2()
    {
        // NO existe comunidad en DB.
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Ronda")
                        .nombreVia("no existe")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();
        List<Comunidad> comunidades = comunidadDao.searchComunidadOne(comunidad);
        assertThat(comunidades, notNullValue());
        assertThat(comunidades.size(), is(0));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_3()
    {
        // NO consideramos tipo_via en la búsqueda. Resto de datos coincide con entrada en DB.

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Travesía")
                        .nombreVia("de la Plazuela")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadTwo(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el sufijo número.
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
        assertThat(comunidades.get(0).getDomicilio().getNumero(), is((short) 10));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        assertThat(comunidades.get(0).getDomicilio().getMunicipio().getCodInProvincia(), is((short) 52));

        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda")); // Y no Travesía.
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_4()
    {
        // NO consideramos tipo_via en la búsqueda.
        /*select name from metal where name like '%param'; */

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("Plazuela")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_5()
    {
        /*select name from metal where name = LEFT('namexxx',length(name));*/

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Travesía")
                        .nombreVia("de la Plazuela Nueva")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_6()
    {
        /*select name from metal where name like 'param%';*/
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("de la Plazu")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_7()
    {
        /*select name from metal where name = RIGHT('xxxname',length(name));*/
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("Atajo de la Plazuela")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_8()
    {
        /* select name from metal where name like '%param%';*/

        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("Plazu")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, not(hasItem(comunidad))); // difieren en el nombre_via.
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Plazuela"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidad_9() throws ServiceException
    {
        /*select name from metal where name = RIGHT('xxxname',length(name));*/
        // Diferencia con test_7: datos de password encriptados en DB.

        final Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Calle")
                        .nombreVia("de la Mujer de la Plazuela")
                        .numero((short) 10)
                        .sufijoNumero("Bis")
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        UsuarioComunidad userComu = makeUsuarioComunidad(comunidad, juan, "portal1", "esc2", "planta3", "puerta12");
        boolean rowInserted = sujetosService.regComuAndUserComu(userComu);
        assertThat(rowInserted, is(true));

        // Datos de comunidad de búsqueda.
        Comunidad comunidadSearch = new Comunidad.ComunidadBuilder()
                .domicilio(new Domicilio.DomicilioBuilder()
                        .tipoVia("Ronda")
                        .nombreVia("de la Plazuela")
                        .numero((short) 10)
                        .municipio(new Municipio((short) 52, new Provincia((short) 2)))
                        .build())
                .build();

        List<Comunidad> comunidades = comunidadDao.searchComunidadThree(comunidadSearch);
        assertThat(comunidades.size(), is(2));
        assertThat(comunidades.get(0).getDomicilio().getTipoVia(), is("Calle"));
        assertThat(comunidades.get(0).getDomicilio().getNombreVia(), is("de la Mujer de la Plazuela"));
        assertThat(comunidades.get(1).getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidades.get(1).getDomicilio().getNombreVia(), is("de la Plazuela"));
    }
}