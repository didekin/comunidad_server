package com.didekin.userservice.repository;


import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.model.entidad.comunidad.Comunidad;
import com.didekinlib.model.relacion.usuariocomunidad.UsuarioComunidad;
import com.didekinlib.model.usuario.Usuario;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.didekin.common.repository.ServiceException.DUPLICATE_ENTRY;
import static com.didekin.common.repository.ServiceException.GCM_TOKEN_KEY;
import static com.didekin.common.repository.ServiceException.USER_NAME;
import static com.didekin.userservice.repository.UsuarioManager.BCRYPT_SALT;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_FUENTE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PACO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_plazuela_23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan_lafuente;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan_plazuela23;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_COMU_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.mindrot.jbcrypt.BCrypt.hashpw;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 15:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({LocalDev.class, DbPre.class})
public class UsuarioDaoDbPreDevTest {

    @Autowired
    private UsuarioDao usuarioDao;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteGcmToken()
    {
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getGcmToken(), is(luis.getGcmToken()));
        assertThat(usuarioDao.deleteGcmToken(luis.getGcmToken()), is(1));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getGcmToken(), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUserByName() throws ServiceException
    {
        List<UsuarioComunidad> usuarioComunidades = usuarioDao.seeUserComusByUser(pedro.getUserName());
        assertThat(usuarioComunidades.size(), is(3));
        boolean isDeleted = usuarioDao.deleteUser(pedro.getUserName());
        assertThat(isDeleted, is(true));
        try {
            usuarioDao.getUserDataByName(pedro.getUserName());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Test()
    public void testDeleteUserByNameWrong()
    {
        try {
            usuarioDao.deleteUser("noexiste@noexiste.com");
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_1() throws ServiceException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        int isDeleted = usuarioDao.deleteUserComunidad(
                new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
        assertThat(isDeleted, is(1));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_2()
    {
        // No existe el usuario.
        Usuario usuario = new Usuario.UsuarioBuilder().uId(111L).build();
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        try {
            usuarioDao.deleteUserComunidad(new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }

        // No existe la comunidad.
        usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        comunidad = new Comunidad.ComunidadBuilder().c_id(111L).build();
        try {
            usuarioDao.deleteUserComunidad(new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }

        // No existe ni usuario ni comunidad.
        usuario = new Usuario.UsuarioBuilder().uId(111L).build();
        comunidad = new Comunidad.ComunidadBuilder().c_id(111L).build();
        try {
            usuarioDao.deleteUserComunidad(new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_1()
    {
        List<Comunidad> comunidades = usuarioDao.getComusByUser("juan@noauth.com");
        assertThat(comunidades, hasItem(COMU_LA_FUENTE));

        comunidades = usuarioDao.getComusByUser(pedro.getUserName());
        assertThat(comunidades, CoreMatchers.hasItems(UsuarioTestUtils.COMU_LA_PLAZUELA_10bis, COMU_LA_FUENTE, UsuarioTestUtils.COMU_EL_ESCORIAL));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_2()
    {
        // No existe el usuario en DB.
        List<Comunidad> comunidades = usuarioDao.getComusByUser("noexisto@muerto.com");
        assertThat(comunidades.isEmpty(), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetMaxPk()
    {
        long maxPk = usuarioDao.getMaxPk();
        //No usuario.
        assertThat(maxPk, is(-1L));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserDataById_1() throws ServiceException
    {
        Usuario usuario = usuarioDao.getUserDataById(luis.getuId());
        UsuarioTestUtils.checkBeanUsuario(usuario, luis, false);
    }

    @Test()
    public void testGetUserDataById_2()
    {
        try {
            usuarioDao.getUserDataById(11L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserDataByName_1()
    {
        Usuario usuario = usuarioDao.getUserDataByName("luis@luis.com");
        UsuarioTestUtils.checkBeanUsuario(usuario, luis, true);
    }

    @Test
    public void testGetUserDataByName_2()
    {
        try {
            usuarioDao.getUserDataByName("noexisto@no.com");
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetUserFullComuByUserAndComu_1() throws ServiceException
    {

        // UsuarioComunidad no existe en BD. Devuelve null.
        UsuarioComunidad usuarioComunidad;
        assertThat(usuarioDao.getUserComuFullByUserAndComu("juan@noauth.com", 1L), nullValue());

        // UsuarioComunidad existe en BD.
        usuarioComunidad = usuarioDao.getUserComuFullByUserAndComu(pedro.getUserName(), 1L);
        assertThat(usuarioComunidad, notNullValue());
        // Usuario
        Usuario usuario = usuarioComunidad.getUsuario();
        assertThat(usuario.getUserName(), is(pedro.getUserName()));
        assertThat(usuario.getAlias(), is("pedronevado"));
        // Comunidad.
        Comunidad comunidad = usuarioComunidad.getEntidad();
        assertThat(comunidad.getDomicilio().getTipoVia(), is("Ronda"));
        assertThat(comunidad.getDomicilio().getNombreVia(), is("de la Plazuela"));
        assertThat(comunidad.getDomicilio().getNumero(), is((short) 10));
        assertThat(comunidad.getDomicilio().getSufijoNumero(), is("bis"));
        assertThat(comunidad.getDomicilio().getMunicipio().getNombre(), is("Motilleja"));
        assertThat(comunidad.getDomicilio().getMunicipio().getCodInProvincia(), is((short) 52));
        assertThat(comunidad.getDomicilio().getMunicipio().getProvincia().getNombre(), is("Albacete"));
        assertThat(comunidad.getDomicilio().getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
        // UsuarioComunidad
        assertThat(usuarioComunidad.getPortal(), is("Centro"));
        assertThat(usuarioComunidad.getEscalera(), nullValue());
        assertThat(usuarioComunidad.getPlanta(), is("3"));
        assertThat(usuarioComunidad.getPuerta(), is("J"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuario_1() throws Exception
    {
        long maxPk = usuarioDao.getMaxPk();

        Connection conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
        usuarioDao.insertUsuario(USER_PACO, conn);
        assertThat(USER_PACO.getuId() > maxPk, is(true));

        maxPk = usuarioDao.getMaxPk();
        long pkUsuario = usuarioDao.insertUsuario(UsuarioTestUtils.USER_LUIS, conn);
        assertThat(pkUsuario == maxPk + 1, is(true));
        if (conn != null) {
            conn.close();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuario_2() throws Exception
    {
        Connection conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
        usuarioDao.insertUsuario(USER_PACO, conn);

        if (conn != null) {
            conn.close();
        }

        tryCheckInsertUser(USER_PACO, conn, USER_NAME);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInsertUsuario_3() throws Exception
    {
        Usuario usuario1 = new Usuario.UsuarioBuilder().copyUsuario(USER_PACO).gcmToken("gcm_token_1").build();
        Usuario usuario2 = new Usuario.UsuarioBuilder().copyUsuario(USER_JUAN).gcmToken("gcm_token_1").build();

        Connection conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
        assertThat(usuarioDao.insertUsuario(usuario1, conn) > 0L, is(true));
        if (conn != null) {
            conn.close();
        }

        tryCheckInsertUser(usuario2, conn, GCM_TOKEN_KEY);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_IsUserInComunidad()
    {
        assertThat(usuarioDao.isUserInComunidad(pedro.getUserName(), 1L)
                        && usuarioDao.isUserInComunidad(pedro.getUserName(), 2L)
                        && usuarioDao.isUserInComunidad(pedro.getUserName(), 3L),
                is(true));
        assertThat(usuarioDao.isUserInComunidad(pedro.getUserName(), 4L), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser() throws ServiceException
    {
        // We change username.
        Usuario usuarioDB = usuarioDao.getUserDataByName("juan@noauth.com");
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .alias(usuarioDB.getAlias())
                .userName("new_juan@juan.com")
                .password(hashpw("new_password", BCRYPT_SALT.get()))
                .uId(usuarioDB.getuId())
                .build();

        int updatedRow = usuarioDao.modifyUser(usuarioIn);
        assertThat(updatedRow, is(1));
        Usuario usuarioDBOut = usuarioDao.getUserDataById(usuarioIn.getuId());
        assertThat(usuarioDBOut.getUserName(), is(usuarioIn.getUserName()));
        assertThat(checkpw("new_password", usuarioDBOut.getPassword()), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserAlias() throws ServiceException
    {
        // We change alias.
        Usuario usuarioDB = usuarioDao.getUserDataByName("juan@noauth.com");
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .alias("new_alias_juan")
                .uId(usuarioDB.getuId())
                .build();

        int updatedRow = usuarioDao.modifyUserAlias(usuarioIn);
        assertThat(updatedRow, is(1));
        Usuario usuarioDB_2 = usuarioDao.getUserDataById(usuarioIn.getuId());
        assertThat(usuarioDB_2.getUserName(), is(usuarioDB.getUserName()));
        assertThat(usuarioDB_2.getAlias(), is(usuarioIn.getAlias()));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserComu()
    {
        UsuarioComunidad uc_1 = UsuarioTestUtils.makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(4L).build(),
                new Usuario.UsuarioBuilder().uId(11L).build(),
                "portal_a", null, "PL-1", "J");

        assertThat(usuarioDao.modifyUserComu(uc_1), is(1));
        List<UsuarioComunidad> userComus = usuarioDao.seeUserComusByUser("paco@paco.com");
        assertThat(userComus.get(0).getPortal(), is(uc_1.getPortal()));
        assertThat(userComus.get(0).getEscalera(), is(uc_1.getEscalera()));
        assertThat(userComus.get(0).getPlanta(), is(uc_1.getPlanta()));
        assertThat(userComus.get(0).getPuerta(), is(uc_1.getPuerta()));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChange()
    {
        Usuario usuarioDb = usuarioDao.getUserDataByName("luis@luis.com");
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .uId(usuarioDb.getuId())
                .password("new_luis_password")
                .build();
        assertThat(usuarioDao.passwordChange(usuarioIn), is(1));
        assertThat(usuarioDao.getUserDataByName("luis@luis.com").getPassword(), is("new_luis_password"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByComu_1()
    {
        // Comunidad 4 con dos usuarios.
        List<UsuarioComunidad> usuariosComu = usuarioDao.seeUserComusByComu(4L);
        assertThat(usuariosComu.size(), is(2));
        // Datos usuario:
        assertThat(usuariosComu.get(1).getUsuario().getUserName(), is("paco@paco.com"));
        assertThat(usuariosComu.get(1).getUsuario().getAlias(), is("paco"));
        assertThat(usuariosComu.get(1).getUsuario().getuId(), is(11L));
        // Datos comunidad:
        assertThat(usuariosComu.get(1).getEntidad().getId(), is(4L));
        // Datos usuarioComunidad:
        assertThat(usuariosComu.get(1).getPortal(), is("BC"));
        assertThat(usuariosComu.get(1).getEscalera(), is(nullValue()));
        assertThat(usuariosComu.get(1).getPlanta(), is(nullValue()));
        assertThat(usuariosComu.get(1).getPuerta(), is(nullValue()));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser()
    {
        // Usuario con 2 comunidades.
        List<UsuarioComunidad> userComunidades = usuarioDao.seeUserComusByUser(juan.getUserName());
        assertThat(userComunidades.size(), is(2));
        assertThat(userComunidades, hasItems(juan_plazuela23, juan_lafuente));
        // Verificación de datos de comunidad.
        assertThat(userComunidades.get(0).getEntidad(),
                allOf(
                        hasProperty("id", is(calle_plazuela_23.getId())),
                        hasProperty("domicilio", allOf
                                (
                                        hasProperty("tipoVia", is(calle_plazuela_23.getDomicilio().getTipoVia())),
                                        hasProperty("nombreVia", is(calle_plazuela_23.getDomicilio().getNombreVia())),
                                        hasProperty("numero", is(calle_plazuela_23.getDomicilio().getNumero())),
                                        hasProperty("sufijoNumero", is(calle_plazuela_23.getDomicilio().getSufijoNumero())),
                                        hasProperty("municipio",
                                                allOf(
                                                        hasProperty("codInProvincia",
                                                                is(calle_plazuela_23.getDomicilio().getMunicipio().getCodInProvincia())),
                                                        hasProperty("nombre",
                                                                is(calle_plazuela_23.getDomicilio().getMunicipio().getNombre())),
                                                        hasProperty("provincia",
                                                                allOf(
                                                                        hasProperty("provinciaId",
                                                                                is(calle_plazuela_23.getDomicilio().getMunicipio().getProvincia().getProvinciaId())),
                                                                        hasProperty("nombre",
                                                                                is(calle_plazuela_23.getDomicilio().getMunicipio().getProvincia().getNombre()))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        // Verificación datos de Usuario.
        assertThat(userComunidades.get(0).getUsuario(),
                allOf(
                        hasProperty("uId", is(juan.getuId())),
                        hasProperty("userName", is(juan.getUserName())),
                        hasProperty("alias", is(juan.getAlias()))
                )
        );
        // Verificación datos de UsuarioComunidad.
        assertThat(userComunidades.get(0),
                allOf(
                        hasProperty("portal", is(juan_plazuela23.getPortal())),
                        hasProperty("planta", is(juan_plazuela23.getPlanta())),
                        hasProperty("escalera", is(juan_plazuela23.getEscalera())),
                        hasProperty("puerta", is(juan_plazuela23.getPuerta()))
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_UpdateTokenAuthByUserName()
    {
        // Premises:
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), notNullValue());
        // Check.
        assertThat(usuarioDao.updateTokenAuthByUserName(luis.getUserName(), "update_luis_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), is("update_luis_tokenAuth"));

        // Premises:
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), nullValue());
        // Check.
        assertThat(usuarioDao.updateTokenAuthByUserName(pedro.getUserName(), "update_pedro_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), is("update_pedro_tokenAuth"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_UpdateTokenAuthById()
    {
        // Premises:
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), nullValue());
        // Check.
        assertThat(usuarioDao.updateUserTokensById(pedro, "update_pedro_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), is("update_pedro_tokenAuth"));
        // Premises:
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), notNullValue());
        // Check.
        assertThat(usuarioDao.updateUserTokensById(luis, "update_luis_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), is("update_luis_tokenAuth"));
        // Check.
        assertThat(usuarioDao.updateUserTokensById(luis, null), is(true));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), nullValue());

        /* Premises: user not in DB.*/
        try {
            usuarioDao.updateUserTokensById(new Usuario.UsuarioBuilder().uId(999L).build(), "fake_token");
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    // ====================================== Helpers ======================================

    private void tryCheckInsertUser(Usuario usuario2, Connection conn, String exceptionKey) throws SQLException
    {
        try {
            conn = requireNonNull(usuarioDao.getJdbcTemplate().getDataSource()).getConnection();
            usuarioDao.insertUsuario(usuario2, conn);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), allOf(containsString(DUPLICATE_ENTRY), containsString(exceptionKey)));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}