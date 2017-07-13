package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.didekin.userservice.repository.PswdGenerator.LENGTH;
import static com.didekin.userservice.testutils.UsuarioTestUtils.*;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_DUPLICATE;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_DATA_NOT_MODIFIED;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_DUPLICATE;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 16:23
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public abstract class UsuarioServiceTest {

    @Autowired
    private UsuarioServiceIf usuarioService;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private ComunidadDao comunidadDao;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void deleteAccessTokenByUserName_1() throws Exception
    {
        // No token in database: no delete.
        assertThat(usuarioService.deleteAccessTokenByUserName(luis.getUserName()), Matchers.is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_1() throws EntityException
    {
        // La comunidad tiene 2 usuarios. El usuario tiene 3 comunidades.
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).userName("pedro@pedro.com").build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();

        int isDeleted = usuarioService.deleteUserComunidad(usuarioComunidad);
        assertThat(isDeleted, is(1));

        Comunidad comunidadDb = comunidadDao.getComunidadById(comunidad.getC_Id());
        assertThat(comunidadDb.getC_Id(), is(comunidad.getC_Id()));

        // La comunidad tiene 1 usuario.
        comunidad = new Comunidad.ComunidadBuilder().c_id(3L).build();
        usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();
        isDeleted = usuarioService.deleteUserComunidad(usuarioComunidad);
        assertThat(isDeleted, is(1));
        try {
            comunidadDao.getComunidadById(comunidad.getC_Id());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
            assertThat(e.getExceptionMsg().getHttpMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_2() throws EntityException
    {
        // El usuario tiene una comunidad. La comunidad tiene un usuario.

        UsuarioComunidad uc_1 = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(4L).build(),
                new Usuario.UsuarioBuilder().uId(11L).userName("paco@paco.com").build(),
                null, null, null, null, null);
        assertThat(usuarioService.deleteUserComunidad(uc_1), is(IS_USER_DELETED));

        try {
            comunidadDao.getComunidadById(4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }

        try {
            usuarioService.getUserByUserName("paco@paco.com");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }

        // El usuario tiene tres comunidades. La comunidad tiene 1 usuario.

        UsuarioComunidad uc_2 = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(3L).build(),
                new Usuario.UsuarioBuilder().uId(3L).userName("pedro@pedro.com").build(),
                null, null, null, null, null);
        assertThat(usuarioService.deleteUserComunidad(uc_2), is(1));

        try {
            comunidadDao.getComunidadById(3L);
            fail("NO existe la comunidad");
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }

        try {
            assertThat(usuarioService.getUserByUserName("pedro@pedro.com"), notNullValue());
        } catch (EntityException e) {
            fail();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUserAndComunidades() throws EntityException
    {
        int comuDeleted = usuarioService.deleteUserAndComunidades("pedro@pedro.com");
        assertThat(comuDeleted, is(3));

        assertThat(comunidadDao.getComunidadById(1L).getC_Id(), is(1L));
        assertThat(comunidadDao.getComunidadById(2L).getC_Id(), is(2L));
        try {
            comunidadDao.getComunidadById(3L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
            assertThat(e.getExceptionMsg().getHttpMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUser_1() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().userName("user@user.com").alias("alias_user")
                .password("password_user").build();
        UsuarioComunidad usuarioComunidad = makeUsuarioComunidad(COMU_REAL, usuario, "portal", "esc", "plantaX",
                "door", PROPIETARIO.function);

        boolean isRegOk = usuarioService.regComuAndUserAndUserComu(usuarioComunidad);
        assertThat(isRegOk, is(true));

        boolean isDeleted = usuarioService.deleteUser(usuario.getUserName());
        assertThat(isDeleted, is(true));
    }

    @Test
    public void testDeleteUser_2()
    {
        // El usuario no existe en BD.
        Usuario usuario = new Usuario.UsuarioBuilder().userName("user@user.com").alias("alias_user")
                .password("password_user").build();
        try {
            usuarioService.deleteUser(usuario.getUserName());
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetAuthoritiesByUser()
    {

        List<String> authorities = usuarioService.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName("pedro@pedro.com").build());

        assertThat(authorities.size(), is(2));
        assertThat(authorities, hasItems(ADMINISTRADOR.authority, INQUILINO.authority));

        authorities = usuarioService.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName("luis@luis.com").build());
        assertThat(authorities.size(), is(2));
        assertThat(authorities, hasItems(PRESIDENTE.authority, PROPIETARIO.authority));

        authorities = usuarioService.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName("juan@noauth.com").build());
        assertThat(authorities.size(), is(1));
        assertThat(authorities, hasItem(INQUILINO.authority));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComunidadById() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        assertThat(usuarioService.getComunidadById(usuario, 2L), is(new Comunidad.ComunidadBuilder().c_id(2L).build()));

        try {
            // No hay vínculo usuario-comunidad; ambos existen.
            usuarioService.getComunidadById(usuario, 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }

        try {
            // No existe el usuario.
            usuarioService.getComunidadById(new Usuario.UsuarioBuilder().uId(99L).build(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetHighestRolFunction() throws EntityException
    {
        assertThat(usuarioService.getHighestFunctionalRol("pedro@pedro.com", 3L), is(ADMINISTRADOR.function));
        assertThat(usuarioService.getHighestFunctionalRol("luis@luis.com", 1L), is(ADMINISTRADOR.function));
        assertThat(usuarioService.getHighestFunctionalRol("juan@noauth.com", 2L), is(INQUILINO.function));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserByUserName_1() throws EntityException
    {
        usuarioService.regComuAndUserAndUserComu(makeUsuarioComunidad(COMU_LA_PLAZUELA_5, pedro, "portal1", "EE", "3", null, INQUILINO.function));
        Usuario usuario = usuarioService.getUserByUserName(pedro.getUserName());
        assertThat(usuario.getUserName(), is(pedro.getUserName()));
        assertThat(usuario.getuId() > 0L, is(true));
        assertThat(usuario.getAlias(), is(pedro.getAlias()));
        assertThat(new BCryptPasswordEncoder().matches(pedro.getPassword(), usuario.getPassword()), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComuByUserAndComu() throws EntityException
    {
        // No existe la combinación (usario, comunidad); existe la comunidad.
        assertThat(usuarioService.getUserComuByUserAndComu("paco@paco.com", 1L), nullValue());

        // No existe la comunidad.
        try {
            usuarioService.getUserComuByUserAndComu("paco@paco.com", 111L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestUserComuId_1() throws InterruptedException, EntityException
    {
        assertThat(usuarioService.isOldestUserComu(new Usuario.UsuarioBuilder().uId(3L).build(), 1L), is(true));
        assertThat(usuarioService.isOldestUserComu(new Usuario.UsuarioBuilder().uId(5L).build(), 1L), is(false));
        assertThat(usuarioService.isOldestUserComu(new Usuario.UsuarioBuilder().uId(7L).build(), 2L), is(true));
    }

    @Test
    public void testIsOldestUserComuId_2() throws InterruptedException, EntityException
    {
        // Caso: la comunidad no existe en BD.
        Usuario pedro = new Usuario.UsuarioBuilder().userName("pedro@pedro.com").password("password3").build();
        try {
            assertThat(usuarioService.isOldestUserComu(pedro, 999L), is(true));
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_1() throws EntityException
    {
        Usuario pedroOk = new Usuario.UsuarioBuilder().userName("pedro@pedro.com").password("password3").build();
        Usuario pedroWrongUserName = new Usuario.UsuarioBuilder().userName("pedro@wrong.com").password("password3")
                .build();
        Usuario pedroWrongPassword = new Usuario.UsuarioBuilder().userName("pedro@pedro.com").password("passwordWrong")
                .build();

        assertThat(usuarioService.login(pedroOk), is(true));
        assertThat(usuarioService.login(pedroWrongPassword), is(false));

        try {
            assertThat(usuarioService.login(pedroWrongUserName), is(true));
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testMakeNewPassword() throws EntityException
    {
        String newPassword = usuarioService.makeNewPassword(pedro);
        assertThat((short) newPassword.length(), is(LENGTH));
        String passwordBD = usuarioDao.getUserByUserName(pedro.getUserName()).getPassword();
        assertThat(new BCryptPasswordEncoder().matches(newPassword, passwordBD), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws InterruptedException, EntityException
    {
        Usuario u1 = new Usuario.UsuarioBuilder().userName("pedro@pedro.com").password("paswword1").alias
                ("alias1").build();
        Usuario u2 = new Usuario.UsuarioBuilder().userName("luis@luis.com").password("paswword2").alias
                ("alias2").build();
        Comunidad c1 = new Comunidad.ComunidadBuilder().tipoVia("tipoV1").nombreVia("nombreV1")
                .municipio(new Municipio((short) 13, new Provincia((short) 3))).build();
        Comunidad c3 = new Comunidad.ComunidadBuilder().tipoVia("tipoV3").nombreVia("nombreV3")
                .municipio(new Municipio((short) 13, new Provincia((short) 3))).build();

        UsuarioComunidad uc13 = makeUsuarioComunidad(c3, u1, null, null, null, null, PROPIETARIO.function);
        UsuarioComunidad uc21 = makeUsuarioComunidad(c1, u2, null, null, null, null, PROPIETARIO.function);

        usuarioService.regComuAndUserAndUserComu(uc13);
        Thread.sleep(1000);
        usuarioService.regComuAndUserAndUserComu(uc21);
        Thread.sleep(1000);

        u1 = usuarioService.getUserByUserName(u1.getUserName());
        u2 = usuarioService.getUserByUserName(u2.getUserName());
        c3 = usuarioService.getComusByUser(u1.getUserName()).get(0);
        c1 = usuarioService.getComusByUser(u2.getUserName()).get(0);

        UsuarioComunidad uc11 = makeUsuarioComunidad(c1, u1, null, null, null, null, PROPIETARIO.function);
        UsuarioComunidad uc23 = makeUsuarioComunidad(c3, u2, null, null, null, null, PROPIETARIO.function);

        usuarioService.regUserComu(uc11);
        Thread.sleep(1000);
        usuarioService.regUserComu(uc23);
        Thread.sleep(1000);

        Comunidad c11 = new Comunidad.ComunidadBuilder().c_id(c1.getC_Id()).tipoVia("tipoV11").nombreVia("nombreV11")
                .municipio(new Municipio((short) 14, new Provincia((short) 3))).build();
        Comunidad c33 = new Comunidad.ComunidadBuilder().c_id(c3.getC_Id()).tipoVia("tipoV33").nombreVia("nombreV33")
                .municipio(new Municipio((short) 14, new Provincia((short) 3))).build();

        assertThat(usuarioService.modifyComuData(u1, c33), is(1));
        assertThat(usuarioService.modifyComuData(u2, c11), is(1));

        try {
            usuarioService.modifyComuData(u1, c11);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }

        try {
            usuarioService.modifyComuData(u2, c33);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserGcmTokens()
    {
        assertThat(usuarioService.modifyUserGcmToken(pedro), is(1));
        assertThat(usuarioService.modifyUserGcmToken(luis), is(1));
        assertThat(usuarioService.modifyUserGcmToken(IncidenciaTestUtils.juan), is(1));

        List<GcmTokensHolder> holders = new ArrayList<>(3);
        holders.add(new GcmTokensHolder(null, luis.getGcmToken()));
        holders.add(new GcmTokensHolder(null, pedro.getGcmToken()));
        holders.add(new GcmTokensHolder("new_juan_token", IncidenciaTestUtils.juan.getGcmToken()));
        assertThat(usuarioService.modifyUserGcmTokens(holders), is(3));

        assertThat(usuarioDao.getUsuarioWithGcmToken(luis.getuId()).getGcmToken(), nullValue());
        assertThat(usuarioDao.getUsuarioWithGcmToken(pedro.getuId()).getGcmToken(), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_1() throws EntityException
    {
        // We change alias ONLY.
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .uId(7L)
                .alias("new_juan_alias")
                .build();

        assertThat(usuarioService.modifyUser(usuarioIn, "juan@noauth.com"), is(1));
        assertThat(usuarioService.getUserByUserName("juan@noauth.com").getAlias(), is("new_juan_alias"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2() throws EntityException
    {
        // We change userName.
        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .uId(7L)
                .userName("new_juan@juan.com")
                .build();

        assertThat(usuarioService.modifyUser(usuarioNew, "juan@noauth.com"), is(1));
        assertThat(usuarioService.getUserByUserName("new_juan@juan.com"), allOf(
                hasProperty("uId", equalTo(7L)),
                hasProperty("alias", is("juan_no_auth")))
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_3() throws EntityException
    {
        // UserName and alias null.
        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .uId(7L)
                .build();
        try {
            usuarioService.modifyUser(usuarioNew, "juan@noauth.com");
        } catch (EntityException e){
            assertThat(e.getExceptionMsg(), is(USER_DATA_NOT_MODIFIED));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChangeWithName() throws EntityException
    {
        Usuario luis = usuarioService.getUserByUserName("luis@luis.com");
        String newClearPswd = "new_luis_password";
        assertThat(usuarioService.passwordChangeWithName(luis.getUserName(), newClearPswd), is(1));
        assertThat(new BCryptPasswordEncoder().matches(newClearPswd, usuarioDao.getUsuarioById(luis.getuId()).getPassword()),
                is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChangeWithUser()
    {
        try {
            usuarioService.passwordChangeWithUser(new Usuario.UsuarioBuilder()
                    .userName("noexisto@no.com").password("noexisto").build(), "newPassword");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_DATA_NOT_MODIFIED));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_1() throws SQLException, EntityException
    {
        UsuarioComunidad usuarioCom = makeUsuarioComunidad(COMU_REAL, USER_JUAN, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);

        boolean insertedOk = usuarioService.regComuAndUserAndUserComu(usuarioCom);
        assertThat(insertedOk, is(true));

        List<UsuarioComunidad> comunidades = usuarioService.seeUserComusByUser(USER_JUAN.getUserName());
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(usuarioCom));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_2() throws SQLException, EntityException
    {
        boolean isInserted_1 = usuarioService.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN);
        assertThat(isInserted_1, is(true));
        try {
            usuarioService.regComuAndUserAndUserComu(COMU_REAL_JUAN);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_3() throws SQLException, EntityException
    {
        boolean isInserted_1 = usuarioService.regComuAndUserAndUserComu(COMU_REAL_JUAN);
        assertThat(isInserted_1, is(true));
        try {
            usuarioService.regComuAndUserAndUserComu(COMU_REAL_PEPE);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    /* En este test no hay codificación del password porque sortea el método del controller que lo hace. */
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu_1() throws SQLException, EntityException
    {
        // Preconditions: there is DB a user registered in a comunidad.
        UsuarioComunidad usuarioComunidad1 = makeUsuarioComunidad(COMU_REAL, USER_JUAN, "portal", "esc", "1", "door",
                INQUILINO.function);
        usuarioService.regComuAndUserAndUserComu(usuarioComunidad1);
        Usuario usuarioWithPk = usuarioService.getUserByUserName(USER_JUAN.getUserName());

        UsuarioComunidad usuarioComunidad2 = makeUsuarioComunidad(COMU_OTRA, usuarioWithPk, "AB", "ESC", "11", "puert",
                ADMINISTRADOR.function);

        boolean isRegOk = usuarioService.regComuAndUserComu(usuarioComunidad2);
        assertThat(isRegOk, is(true));

        List<UsuarioComunidad> comunidades = usuarioService.seeUserComusByUser(USER_JUAN.getUserName());
        assertThat(comunidades.size(), is(2));
        assertThat(comunidades, hasItems(usuarioComunidad1, usuarioComunidad2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu_2() throws EntityException
    {
        // Preconditions: there is DB a user registered in a comunidad.
        boolean isInserted_1 = usuarioService.regComuAndUserAndUserComu(COMU_REAL_JUAN);
        assertThat(isInserted_1, is(true));
        try {
            usuarioService.regComuAndUserComu(COMU_REAL_PEPE);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws SQLException, EntityException
    {
        // Preconditions: there is a comunidad associated to other users.
        usuarioService.regComuAndUserAndUserComu(COMU_TRAV_PLAZUELA_PEPE);
        // We get the id of the comunidad.
        long comunidadId = usuarioService.getComusByUser(USER_PEPE.getUserName()).get(0).getC_Id();

        // Nuevo usuarioComunidad.
        UsuarioComunidad userComu = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(comunidadId).build(),
                USER_JUAN2,
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);
        boolean isRegistered = usuarioService.regUserAndUserComu(userComu);

        assertThat(isRegistered, is(true));
        long comunidadId_2 = usuarioService.getComusByUser(USER_JUAN2.getUserName()).get(0).getC_Id();
        assertThat(comunidadId_2, is(comunidadId));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_2() throws EntityException
    {
        // Preconditions: there is a comunidad associated to other users.
        boolean isInserted_1 = usuarioService.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN);
        assertThat(isInserted_1, is(true));
        try {
            usuarioService.regUserAndUserComu(COMU_REAL_JUAN);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidades_1() throws SQLException, EntityException
    {
        // Criterio de búsqueda. La mantenemos constante.
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .tipoVia("Ronda")
                .nombreVia("de la Plazuela")
                .numero((short) 5)
                .municipio(new Municipio((short) 2, new Provincia((short) 27)))
                .build();

        // Caso 1: no hay coincidencia ni en tipoVia, ni en nombreVia. Se aplica comunidadDao.searchThree.
        final Comunidad comunidad1 = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("de la Mujer de la Plazuela")
                .numero((short) 5)
                .sufijoNumero("Bis")
                .municipio(new Municipio((short) 2, new Provincia((short) 27)))
                .build();

        final UsuarioComunidad usuarioComunidad_1 = makeUsuarioComunidad(comunidad1, USER_JUAN, "portal", "esc",
                "plantaX", "door12", PROPIETARIO.function);
        usuarioService.regComuAndUserAndUserComu(usuarioComunidad_1);
        Usuario userWithPk = usuarioService.getUserByUserName(USER_JUAN.getUserName());

        // Primera búsqueda.
        List<Comunidad> comunidades = usuarioService.searchComunidades(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(comunidad1));

        // Caso 2: una comunidad con coincidencia en nombre, no en tipoVia. Se aplica searchTwo.
        Comunidad comunidad2 = new Comunidad.ComunidadBuilder()
                .tipoVia("Calle")
                .nombreVia("de la Plazuela")
                .numero((short) 5)
                .municipio(new Municipio((short) 2, new Provincia((short) 27)))
                .build();

        final UsuarioComunidad usuarioComunidad_2 = makeUsuarioComunidad(comunidad2, userWithPk, null,
                null, "planta3", "doorA", ADMINISTRADOR.function);
        usuarioService.regComuAndUserComu(usuarioComunidad_2);

        // Segunda búsqueda.
        comunidades = usuarioService.searchComunidades(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(comunidad2));

        // Caso 3: una comunidad con coincidencia completa. Se aplica searchOne.
        final UsuarioComunidad usuarioComunidad_3 = makeUsuarioComunidad(comunidad, userWithPk, null,
                null, "planta3", "doorA", ADMINISTRADOR.function);
        usuarioService.regComuAndUserComu(usuarioComunidad_3);

        // Tercera búsqueda.
        comunidades = usuarioService.searchComunidades(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(comunidad));
    }
}

