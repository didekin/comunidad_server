package com.didekin.userservice.repository;


import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Connection;
import java.util.List;

import static com.didekin.common.EntityException.DUPLICATE_ENTRY;
import static com.didekin.common.EntityException.USER_NAME;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USER_COMU_NOT_FOUND;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 15:16
 */
@SuppressWarnings("ThrowFromFinallyBlock")
public abstract class UsuarioDaoTest {

    @Autowired
    private UsuarioDao usuarioDao;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteGcmToken()
    {
        assertThat(usuarioDao.modifyUserGcmToken(IncidenciaTestUtils.paco), is(1));
        assertThat(usuarioDao.getUsuarioWithGcmToken(11L).getGcmToken(), CoreMatchers.is(IncidenciaTestUtils.paco.getGcmToken()));
        assertThat(usuarioDao.deleteGcmToken(IncidenciaTestUtils.paco.getGcmToken()), is(1));
        assertThat(usuarioDao.getUsuarioWithGcmToken(11L).getGcmToken(), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test(expected = UsernameNotFoundException.class)
    public void testDeleteUserByName() throws EntityException
    {
        List<UsuarioComunidad> usuarioComunidades = usuarioDao.seeUserComusByUser("pedro@pedro.com");
        assertThat(usuarioComunidades.size(), is(3));
        boolean isDeleted = usuarioDao.deleteUser("pedro@pedro.com");
        assertThat(isDeleted, is(true));
        usuarioDao.getUserByUserName("pedro@pedro.com");
    }

    @Test()
    public void testDeleteUserByNameWrong()
    {
        try {
            usuarioDao.deleteUser("noexiste@noexiste.com");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_1() throws EntityException
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
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }

        // No existe la comunidad.
        usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        comunidad = new Comunidad.ComunidadBuilder().c_id(111L).build();
        try {
            usuarioDao.deleteUserComunidad(new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }

        // No existe ni usuario ni comunidad.
        usuario = new Usuario.UsuarioBuilder().uId(111L).build();
        comunidad = new Comunidad.ComunidadBuilder().c_id(111L).build();
        try {
            usuarioDao.deleteUserComunidad(new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_COMU_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetAllRolesFunctionalUser()
    {
        List<String> roles = usuarioDao.getAllRolesFunctionalUser("pedro@pedro.com");
        assertThat(roles.size(), equalTo(2));
        assertThat(roles, hasItems("adm", "inq"));

        roles = usuarioDao.getAllRolesFunctionalUser("luis@luis.com");
        assertThat(roles.size(), equalTo(3));
        assertThat(roles, hasItems("adm", "pro", "pre"));

        roles = usuarioDao.getAllRolesFunctionalUser("juan@noauth.com");
        assertThat(roles.size(), equalTo(1));
        assertThat(roles, hasItems("inq"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_1()
    {
        List<Comunidad> comunidades = usuarioDao.getComusByUser("juan@noauth.com");
        assertThat(comunidades, CoreMatchers.hasItem(UsuarioTestUtils.COMU_LA_FUENTE));

        comunidades = usuarioDao.getComusByUser("pedro@pedro.com");
        assertThat(comunidades, CoreMatchers.hasItems(UsuarioTestUtils.COMU_LA_PLAZUELA_10bis, UsuarioTestUtils.COMU_LA_FUENTE, UsuarioTestUtils.COMU_EL_ESCORIAL));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComusByUser_2()
    {
        // No existe el usuario en DB.
        List<Comunidad> comunidades = usuarioDao.getComusByUser("noexisto@muerto.com");
        assertThat(comunidades.isEmpty(), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetFuncionRolesArrayByUserComu_1() throws EntityException
    {
        assertThat(usuarioDao.getFuncionRolesArrayByUserComu("luis@luis.com", 1L), is(new String[]{"adm", "pre", "pro"}));
        assertThat(usuarioDao.getFuncionRolesArrayByUserComu("pedro@pedro.com", 3L), is(new String[]{"adm", "inq"}));
        assertThat(usuarioDao.getFuncionRolesArrayByUserComu("juan@noauth.com", 2L), is(new String[]{"inq"}));

    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql"})
    @Test
    public void testGetFuncionRolesArrayByUserComu_2() throws EntityException
    {
        // No se encuentran roles para usuario_comunidad.
        try {
            usuarioDao.getFuncionRolesArrayByUserComu("luis@luis.com", 99L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }

        // No se encuentran roles para usuario_comunidad.
        try {
            usuarioDao.getFuncionRolesArrayByUserComu("noexisto@no.com", 1L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetFuncionRolesStringByUserComu_1() throws EntityException
    {
        assertThat(usuarioDao.getFuncionRolesStringByUserComu("luis@luis.com", 1L), is("adm,pre,pro"));
        assertThat(usuarioDao.getFuncionRolesStringByUserComu("pedro@pedro.com", 3L), is("adm,inq"));
        assertThat(usuarioDao.getFuncionRolesStringByUserComu("juan@noauth.com", 2L), is("inq"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"classpath:delete_sujetos.sql"})
    @Test
    public void testGetFuncionRolesStringByUserComu_2() throws EntityException
    {
        // No se encuentran roles para usuario_comunidad.
        try {
            usuarioDao.getFuncionRolesStringByUserComu("luis@luis.com", 99L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }
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
    public void testGetUsuarioById_1() throws EntityException
    {
        Usuario usuario = usuarioDao.getUsuarioById(5L);
        assertThat(usuario.getUserName(), equalTo("luis@luis.com"));
    }

    @Test(/*expected = EntityException.class*/)
    public void testGetUsuarioById_2()
    {
        try {
            usuarioDao.getUsuarioById(11L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserByUserName_1()
    {
        Usuario usuario = usuarioDao.getUserByUserName("luis@luis.com");
        assertThat(usuario.getUserName(), is("luis@luis.com"));
        assertThat(usuario.getPassword(), is("$2a$10$km0D4Uc5cFV1Gv6aAnoeeu03XNk1i686uqlB2A0BClNtB5A8LucLK"));
        assertThat(usuario.getuId(), is(5L));

        /*assertThat(new BCryptPasswordEncoder().matches(USER_JUAN.getPassword(), usuarioDB.getPassword()), is(true));*/
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testGetUserByUserName_2()
    {
        usuarioDao.getUserByUserName("noexisto@no.com");
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComuByUserAndComu_1() throws EntityException
    {

        // UsuarioComunidad no existe en BD. Devuelve null.
        UsuarioComunidad usuarioComunidad;
        assertThat(usuarioDao.getUserComuByUserAndComu("juan@noauth.com", 1L), nullValue());

        usuarioComunidad = usuarioDao.getUserComuByUserAndComu("pedro@pedro.com", 1L);
        assertThat(usuarioComunidad, notNullValue());
        // Usuario
        Usuario usuario = usuarioComunidad.getUsuario();
        assertThat(usuario.getUserName(), is("pedro@pedro.com"));
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
        usuarioDao.insertUsuario(UsuarioTestUtils.USER_PACO, conn);
        assertThat(UsuarioTestUtils.USER_PACO.getuId() > maxPk, is(true));

        maxPk = usuarioDao.getMaxPk();
        long pkUsuario = usuarioDao.insertUsuario(UsuarioTestUtils.USER_JUAN2, conn);
        assertThat(pkUsuario == maxPk + 1, is(true));
        if (conn != null) {
            conn.close();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test(/*expected = EntityException.class*/)
    public void testInsertUsuario_2() throws Exception
    {
        Connection conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
        usuarioDao.insertUsuario(UsuarioTestUtils.USER_PACO, conn);

        if (conn != null) {
            conn.close();
        }

        try {
            conn = usuarioDao.getJdbcTemplate().getDataSource().getConnection();
            usuarioDao.insertUsuario(UsuarioTestUtils.USER_PACO, conn);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), allOf(containsString(DUPLICATE_ENTRY), containsString(USER_NAME)));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserName() throws EntityException
    {
        // We change username.
        Usuario usuarioDB = usuarioDao.getUserByUserName("juan@noauth.com");
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .alias(usuarioDB.getAlias())
                .userName("new_juan@juan.com")
                .uId(usuarioDB.getuId())
                .build();

        int updatedRow = usuarioDao.modifyUser(usuarioIn);
        assertThat(updatedRow, is(1));
        assertThat(usuarioDao.getUsuarioById(usuarioIn.getuId()).getUserName(), is("new_juan@juan.com"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserAlias() throws EntityException
    {
        // We change alias.
        Usuario usuarioDB = usuarioDao.getUserByUserName("juan@noauth.com");
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .alias("new_alias_juan")
                .uId(usuarioDB.getuId())
                .build();

        int updatedRow = usuarioDao.modifyUserAlias(usuarioIn);
        assertThat(updatedRow, is(1));
        Usuario usuarioDB_2 = usuarioDao.getUsuarioById(usuarioIn.getuId());
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
        List<UsuarioComunidad> userComus = usuarioDao.seeUserComusByUser("paco@paco.com");
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
        Usuario usuarioDb = usuarioDao.getUserByUserName("luis@luis.com");
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .uId(usuarioDb.getuId())
                .password("new_luis_password")
                .build();
        assertThat(usuarioDao.passwordChange(usuarioIn), is(1));
        assertThat(usuarioDao.getUserByUserName("luis@luis.com").getPassword(), is("new_luis_password"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByComu_1()
    {
        //Comunidad 1 con dos usuarios.
        List<UsuarioComunidad> usuariosComu = usuarioDao.seeUserComusByComu(1L);
        assertThat(usuariosComu.size(), is(2));
        assertThat(usuariosComu.get(0).getUsuario().getUserName(), is("luis@luis.com"));
        assertThat(usuariosComu.get(1).getUsuario().getUserName(), is("pedro@pedro.com"));

        // Comunidad 4 con un usuario.
        usuariosComu = usuarioDao.seeUserComusByComu(4L);
        assertThat(usuariosComu.size(), is(1));
        // Datos usuario:
        assertThat(usuariosComu.get(0).getUsuario().getUserName(), is("paco@paco.com"));
        assertThat(usuariosComu.get(0).getUsuario().getAlias(), is("paco"));
        assertThat(usuariosComu.get(0).getUsuario().getuId(), is(11L));
        // Datos comunidad:
        assertThat(usuariosComu.get(0).getComunidad().getC_Id(), is(4L));
        // Datos usuarioComunidad:
        assertThat(usuariosComu.get(0).getPortal(), is("BC"));
        assertThat(usuariosComu.get(0).getEscalera(), is(nullValue()));
        assertThat(usuariosComu.get(0).getPlanta(), is(nullValue()));
        assertThat(usuariosComu.get(0).getPuerta(), is(nullValue()));
        assertThat(usuariosComu.get(0).getRoles(), is("adm,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser_1()
    {
        List<UsuarioComunidad> comunidades = usuarioDao.seeUserComusByUser("luis@luis.com");
        UsuarioComunidad usuarioComunidad = comunidades.get(0);
        // Devuelve los roles en el orden definido en la tabla DB, no en el orden de inserción de los roles.
        assertThat(usuarioComunidad.getRoles(), is("adm,pre,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSeeUserComusByUser_2()
    {
        // Verificación de datos de comunidad.
        List<UsuarioComunidad> userComunidades = usuarioDao.seeUserComusByUser("juan@noauth.com");
        UsuarioComunidad userComuJuan = userComunidades.get(0);
        Comunidad comuLaFuente = userComuJuan.getComunidad();

        assertThat(comuLaFuente.getNombreComunidad(), CoreMatchers.is(UsuarioTestUtils.COMU_LA_FUENTE.getNombreComunidad()));
        assertThat(comuLaFuente.getMunicipio().getNombre(), is("Elda"));
        assertThat(comuLaFuente.getMunicipio().getProvincia().getNombre(), is("Alicante/Alacant"));

        // Verificación datos de UsuarioComunidad.
        userComunidades = usuarioDao.seeUserComusByUser("pedro@pedro.com");

        Usuario userCheck_2 = new Usuario.UsuarioBuilder()
                .userName("pedro@pedro.com")
                .build();

        UsuarioComunidad userComuCheck_2 = new UsuarioComunidad.UserComuBuilder(UsuarioTestUtils.COMU_LA_PLAZUELA_10bis, userCheck_2)
                .portal("Centro")
                .planta("3")
                .puerta("J")
                .roles(ADMINISTRADOR.function)
                .build();

        UsuarioComunidad userComuCheck_3 = new UsuarioComunidad.UserComuBuilder(UsuarioTestUtils.COMU_LA_FUENTE, userCheck_2)
                .portal("A")
                .roles(ADMINISTRADOR.function)
                .build();

        UsuarioComunidad userComuCheck_4 = new UsuarioComunidad.UserComuBuilder(UsuarioTestUtils.COMU_EL_ESCORIAL, userCheck_2)
                .portal("A")
                .roles(ADMINISTRADOR.function.concat(",").concat(INQUILINO.function))
                .build();

        assertThat(userComunidades, hasItems(userComuCheck_2, userComuCheck_3, userComuCheck_4));
    }
}