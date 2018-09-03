package com.didekin.userservice.repository;


import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

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
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_COMU_NOT_FOUND;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
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
@SuppressWarnings({"ThrowFromFinallyBlock", "ConstantConditions"})
public abstract class UsuarioDaoTest {

    @Autowired
    private UsuarioDao usuarioDao;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteGcmToken()
    {
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getGcmToken(), is(luis.getGcmToken()));
        assertThat(usuarioDao.deleteGcmToken(luis.getGcmToken()), is(1));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getGcmToken(), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUserByName() throws ServiceException
    {
        List<UsuarioComunidad> usuarioComunidades = usuarioDao.seeUserComusByUser(pedro.getUserName()); // TODO: fail.
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetAllRolesFunctionalUser()
    {
        List<String> roles = usuarioDao.getAllRolesFunctionalUser(pedro.getUserName());
        assertThat(roles.size(), equalTo(2));
        assertThat(roles, hasItems("adm", "inq"));

        roles = usuarioDao.getAllRolesFunctionalUser("luis@luis.com");
        assertThat(roles.size(), equalTo(2));
        assertThat(roles, hasItems("adm", "pro"));

        roles = usuarioDao.getAllRolesFunctionalUser("juan@noauth.com");
        assertThat(roles.size(), equalTo(1));
        assertThat(roles, hasItems("inq"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_1()
    {
        List<Comunidad> comunidades = usuarioDao.getComusByUser("juan@noauth.com");   // TODO: fail.
        assertThat(comunidades, hasItem(COMU_LA_FUENTE));

        comunidades = usuarioDao.getComusByUser(pedro.getUserName());
        assertThat(comunidades, CoreMatchers.hasItems(UsuarioTestUtils.COMU_LA_PLAZUELA_10bis, COMU_LA_FUENTE, UsuarioTestUtils.COMU_EL_ESCORIAL));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_2()
    {
        // No existe el usuario en DB.
        List<Comunidad> comunidades = usuarioDao.getComusByUser("noexisto@muerto.com");   // TODO: fail.
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetUserComuRolesByUserName()
    {
        assertThat(usuarioDao.getUserComuRolesByUserName(pedro.getUserName(), 2L),
                allOf(
                        hasProperty("usuario",
                                allOf(
                                        hasProperty("uId", is(3L)),
                                        hasProperty("userName", is(pedro.getUserName())),
                                        hasProperty("alias", is(pedro.getAlias()))
                                )
                        ),
                        hasProperty("comunidad", hasProperty("c_Id", is(2L))),
                        hasProperty("roles", is("adm,inq"))
                )
        );

        try {
            usuarioDao.getUserComuRolesByUserName(pedro.getUserName(), 4L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetUserFullComuByUserAndComu_1() throws ServiceException
    {

        // UsuarioComunidad no existe en BD. Devuelve null.
        UsuarioComunidad usuarioComunidad;
        assertThat(usuarioDao.getUserComuFullByUserAndComu("juan@noauth.com", 1L), nullValue());   // TODO: fail.

        // UsuarioComunidad existe en BD.
        usuarioComunidad = usuarioDao.getUserComuFullByUserAndComu(pedro.getUserName(), 1L);
        assertThat(usuarioComunidad, notNullValue());
        // Usuario
        Usuario usuario = usuarioComunidad.getUsuario();
        assertThat(usuario.getUserName(), is(pedro.getUserName()));
        assertThat(usuario.getAlias(), is("pedronevado"));
        // Comunidad.
        Comunidad comunidad = usuarioComunidad.getComunidad();
        assertThat(comunidad.getTipoVia(), is("Ronda"));
        assertThat(comunidad.getNombreVia(), is("de la Plazuela"));
        assertThat(comunidad.getNumero(), is((short) 10));
        assertThat(comunidad.getSufijoNumero(), is("bis"));
        assertThat(comunidad.getMunicipio().getNombre(), is("Motilleja"));
        assertThat(comunidad.getMunicipio().getCodInProvincia(), is((short) 52));
        assertThat(comunidad.getMunicipio().getProvincia().getNombre(), is("Albacete"));
        assertThat(comunidad.getMunicipio().getProvincia().getProvinciaId(), is((short) 2));
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

        Connection conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
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
        Connection conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
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

        Connection conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
        assertThat(usuarioDao.insertUsuario(usuario1, conn) > 0L, is(true));
        if (conn != null) {
            conn.close();
        }

        tryCheckInsertUser(usuario2, conn, GCM_TOKEN_KEY);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserComu()
    {
        UsuarioComunidad uc_1 = UsuarioTestUtils.makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(4L).build(),
                new Usuario.UsuarioBuilder().uId(11L).build(),
                "portal_a", null, "PL-1", "J", "adm,pro,pre");

        assertThat(usuarioDao.modifyUserComu(uc_1), is(1));
        List<UsuarioComunidad> userComus = usuarioDao.seeUserComusByUser("paco@paco.com");   // TODO: fail.
        assertThat(userComus.get(0).getPortal(), is(uc_1.getPortal()));
        assertThat(userComus.get(0).getEscalera(), is(uc_1.getEscalera()));
        assertThat(userComus.get(0).getPlanta(), is(uc_1.getPlanta()));
        assertThat(userComus.get(0).getPuerta(), is(uc_1.getPuerta()));
        assertThat(userComus.get(0).getRoles(), is("adm,pre,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByComu_1()
    {
        // Comunidad 4 con dos usuarios.
        List<UsuarioComunidad> usuariosComu = usuarioDao.seeUserComusByComu(4L);    // TODO: fail.
        assertThat(usuariosComu.size(), is(2));
        // Datos usuario:
        assertThat(usuariosComu.get(1).getUsuario().getUserName(), is("paco@paco.com"));
        assertThat(usuariosComu.get(1).getUsuario().getAlias(), is("paco"));
        assertThat(usuariosComu.get(1).getUsuario().getuId(), is(11L));
        // Datos comunidad:
        assertThat(usuariosComu.get(1).getComunidad().getC_Id(), is(4L));
        // Datos usuarioComunidad:
        assertThat(usuariosComu.get(1).getPortal(), is("BC"));
        assertThat(usuariosComu.get(1).getEscalera(), is(nullValue()));
        assertThat(usuariosComu.get(1).getPlanta(), is(nullValue()));
        assertThat(usuariosComu.get(1).getPuerta(), is(nullValue()));
        assertThat(usuariosComu.get(1).getRoles(), is("adm,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:insert_sujetos_a.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser()
    {
        // Usuario con 2 comunidades.
        List<UsuarioComunidad> userComunidades = usuarioDao.seeUserComusByUser(juan.getUserName());   // TODO: fail.
        assertThat(userComunidades.size(), is(2));
        assertThat(userComunidades, hasItems(juan_plazuela23, juan_lafuente));
        // Verificación de datos de comunidad.
        assertThat(userComunidades.get(0).getComunidad(),
                allOf(
                        hasProperty("c_Id", is(calle_plazuela_23.getC_Id())),
                        hasProperty("tipoVia", is(calle_plazuela_23.getTipoVia())),
                        hasProperty("nombreVia", is(calle_plazuela_23.getNombreVia())),
                        hasProperty("numero", is(calle_plazuela_23.getNumero())),
                        hasProperty("sufijoNumero", is(calle_plazuela_23.getSufijoNumero())),
                        hasProperty("municipio",
                                allOf(
                                        hasProperty("codInProvincia", is(calle_plazuela_23.getMunicipio().getCodInProvincia())),
                                        hasProperty("nombre", is(calle_plazuela_23.getMunicipio().getNombre())),
                                        hasProperty("provincia",
                                                allOf(
                                                        hasProperty("provinciaId", is(calle_plazuela_23.getMunicipio().getProvincia().getProvinciaId())),
                                                        hasProperty("nombre", is(calle_plazuela_23.getMunicipio().getProvincia().getNombre()))
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
                        hasProperty("puerta", is(juan_plazuela23.getPuerta())),
                        hasProperty("roles", is(juan_plazuela23.getRoles()))
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_UpdateTokenAuthById()
    {
        // Premises:
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), nullValue());
        // Check.
        assertThat(usuarioDao.updateTokenAuthById(pedro.getuId(), "update_pedro_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(pedro.getuId()).getTokenAuth(), is("update_pedro_tokenAuth"));
        // Premises:
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), notNullValue());
        // Check.
        assertThat(usuarioDao.updateTokenAuthById(luis.getuId(), "update_luis_tokenAuth"), is(true));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), is("update_luis_tokenAuth"));
        // Check.
        assertThat(usuarioDao.updateTokenAuthById(luis.getuId(), null), is(true));
        assertThat(usuarioDao.getUserDataById(luis.getuId()).getTokenAuth(), nullValue());

        /* Premises: user not in DB.*/
        try {
            usuarioDao.updateTokenAuthById(999L, "fake_token");
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    // ====================================== Helpers ======================================

    private void tryCheckInsertUser(Usuario usuario2, Connection conn, String exceptionKey) throws SQLException
    {
        try {
            conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
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

    // ======================================  INNER CLASSES ======================================

    /**
     * User: pedro
     * Date: 31/03/15
     * Time: 15:16
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
    @Category({LocalDev.class})
    public static class UsuarioDaoDevTest extends UsuarioDaoTest {
    }

    /**
     * User: pedro
     * Date: 31/03/15
     * Time: 15:16
     */
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
    @Category({DbPre.class})
    public static class UsuarioDaoPreTest extends UsuarioDaoTest {
    }
}