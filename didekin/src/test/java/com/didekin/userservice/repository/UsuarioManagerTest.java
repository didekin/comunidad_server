package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekin.userservice.mail.UsuarioMailServiceForTest;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.gcm.model.common.GcmTokensHolder;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.common.testutils.Constant.twoComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.repository.PswdGenerator.default_password_length;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_OTRA;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_PLAZUELA5_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PACO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkGeneratedPassword;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pepe;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_DUPLICATE;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
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
 * <p>
 * See in UsuarioControllerTest:
 * testDeleteAccessToken_1(),
 * test_deleteAccessTokenByUserName(),
 * test_GetAccessToken(),
 * test_GetAccessTokenByUserName().
 */
public abstract class UsuarioManagerTest {

    @Autowired
    private UsuarioManagerIf usuarioManager;
    @Autowired
    private JavaMailMonitor javaMailMonitor;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private ComunidadDao comunidadDao;
    @Autowired
    UsuarioMailServiceForTest mailServiceForTest;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUser_1() throws EntityException
    {
        Usuario usuario = usuarioManager.completeUser(USER_PACO.getUserName());
        assertThat(usuario, allOf(
                hasProperty("uId", is(11L)),
                hasProperty("userName", is(USER_PACO.getUserName())),
                hasProperty("alias", is(USER_PACO.getAlias())),
                hasProperty("password", nullValue())
        ));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUser_2() throws EntityException
    {
        try {
            usuarioManager.completeUser("no_existo");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CompleteWithUserComuRoles()
    {
        assertThat(usuarioManager.completeWithUserComuRoles(luis.getUserName(), 1L).getRoles(), is("adm,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_deleteAccessTokenByUserName()
    {
        // No token in database: no delete.
        assertThat(usuarioManager.getAccessTokenByUserName(luis.getUserName()).isPresent(), is(false));
        assertThat(usuarioManager.deleteAccessTokenByUserName(luis.getUserName()), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUser_1() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().userName("user@user.com").alias("alias_user")
                .password("password_user").build();
        UsuarioComunidad usuarioComunidad = makeUsuarioComunidad(COMU_REAL, usuario, "portal", "esc", "plantaX",
                "door", PROPIETARIO.function);

        boolean isRegOk = usuarioManager.regComuAndUserAndUserComu(usuarioComunidad, oneComponent_local_ES);
        assertThat(isRegOk, is(true));

        boolean isDeleted = usuarioManager.deleteUser(usuario.getUserName());
        assertThat(isDeleted, is(true));
    }

    @Test
    public void testDeleteUser_2()
    {
        // El usuario no existe en BD.
        Usuario usuario = new Usuario.UsuarioBuilder().userName("user@user.com").alias("alias_user")
                .password("password_user").build();
        try {
            usuarioManager.deleteUser(usuario.getUserName());
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUserAndComunidades() throws EntityException
    {
        int comuDeleted = usuarioManager.deleteUserAndComunidades(pedro.getUserName());
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_1() throws EntityException
    {
        // La comunidad tiene 2 usuarios. El usuario tiene 3 comunidades.
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).userName(pedro.getUserName()).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();

        int isDeleted = usuarioManager.deleteUserComunidad(usuarioComunidad);
        assertThat(isDeleted, is(1));

        Comunidad comunidadDb = comunidadDao.getComunidadById(comunidad.getC_Id());
        assertThat(comunidadDb.getC_Id(), is(comunidad.getC_Id()));

        // La comunidad tiene 1 usuario.
        comunidad = new Comunidad.ComunidadBuilder().c_id(3L).build();
        usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();
        isDeleted = usuarioManager.deleteUserComunidad(usuarioComunidad);
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
        assertThat(usuarioManager.deleteUserComunidad(pedro_plazuelas_10bis), is(1));
        // Preconditions.
        assertThat(usuarioManager.getComusByUser(luis.getUserName()).size(), is(1));
        assertThat(usuarioManager.seeUserComusByComu(ronda_plazuela_10bis.getC_Id()).size(), is(1));
        // Exec and check.
        assertThat(usuarioManager.deleteUserComunidad(luis_plazuelas_10bis), is(IS_USER_DELETED));
        try {
            comunidadDao.getComunidadById(ronda_plazuela_10bis.getC_Id());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        UsuarioTestUtils.checkUserNotFound(luis.getUserName(), usuarioManager);

        // El usuario tiene tres comunidades. La comunidad tiene 1 usuario.
        // Preconditions.
        assertThat(usuarioManager.getComusByUser(pedro.getUserName()).size() > 1, is(true));
        assertThat(usuarioManager.seeUserComusByComu(calle_el_escorial.getC_Id()).size(), is(1));
        // Exec.
        assertThat(usuarioManager.deleteUserComunidad(pedro_escorial), is(1));
        // Check.
        try {
            comunidadDao.getComunidadById(3L);
            fail("NO existe la comunidad");
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        try {
            assertThat(usuarioManager.getUserByUserName(pedro.getUserName()), notNullValue());
        } catch (EntityException e) {
            fail();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_3() throws EntityException
    {
        // Usuario con una comunidad y comunidad con dos usuarios.
        assertThat(usuarioManager.getComusByUser(luis.getUserName()).size(), is(1));
        assertThat(usuarioManager.seeUserComusByComu(ronda_plazuela_10bis.getC_Id()).size(), is(2));
        // Exec.
        assertThat(usuarioManager.deleteUserComunidad(luis_plazuelas_10bis), is(IS_USER_DELETED));
        // Check.
        assertThat(usuarioManager.getComunidadById(ronda_plazuela_10bis.getC_Id()), is(ronda_plazuela_10bis));
        try {
            usuarioManager.getUserByUserName(luis.getUserName());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComunidadById() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        assertThat(usuarioManager.getComunidadById(usuario, 2L), is(new Comunidad.ComunidadBuilder().c_id(2L).build()));

        try {
            // No hay vínculo usuario-comunidad; ambos existen.
            usuarioManager.getComunidadById(usuario, 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }

        try {
            // No existe el usuario.
            usuarioManager.getComunidadById(new Usuario.UsuarioBuilder().uId(99L).build(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetGcmToken()
    {
        // Insertamos token en usuario.
        Usuario usuario = doUsuario(pedro.getUserName(), "pedro_gcm_token");
        assertThat(usuarioManager.modifyUserGcmToken(usuario), is(1));
        // Exec and check.
        assertThat(usuarioManager.getGcmToken(pedro.getuId()), is("pedro_gcm_token"));

        // Caso null.
        assertThat(usuarioManager.getGcmToken(luis.getuId()), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetGcmTokensByComunidad() throws EntityException
    {
        // Insertamos tokens en tres usuarios.
        Usuario usuario = doUsuario(pedro.getUserName(), "pedro_gcm_token");
        assertThat(usuarioManager.modifyUserGcmToken(usuario), is(1));
        usuario = doUsuario(luis.getUserName(), "luis_gcm_token");
        assertThat(usuarioManager.modifyUserGcmToken(usuario), is(1));
        usuario = doUsuario(juan.getUserName(), "juan_gcm_token");
        assertThat(usuarioManager.modifyUserGcmToken(usuario), is(1));

        List<String> tokens = usuarioManager.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(1L).build().getC_Id());
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("pedro_gcm_token", "luis_gcm_token"));

        tokens = usuarioManager.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(2L).build().getC_Id());
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("pedro_gcm_token", "juan_gcm_token"));

        tokens = usuarioManager.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(3L).build().getC_Id());
        assertThat(tokens.size(), is(1));
        assertThat(tokens, hasItems("pedro_gcm_token"));

        // Caso: comunidad con usuario sin token.
        tokens = usuarioManager.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(4L).build().getC_Id());
        assertThat(tokens, notNullValue());
        assertThat(tokens.size(), is(1));
        assertThat(tokens, hasItems("juan_gcm_token"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_getRolesSecurity()
    {
        List<String> authorities = usuarioManager.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName(pedro.getUserName()).build());

        assertThat(authorities.size(), is(2));
        assertThat(authorities, hasItems(ADMINISTRADOR.authority, INQUILINO.authority));

        authorities = usuarioManager.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName("luis@luis.com").build());
        assertThat(authorities.size(), is(2));
        assertThat(authorities, hasItems(PRESIDENTE.authority, PROPIETARIO.authority));

        authorities = usuarioManager.getRolesSecurity(
                new Usuario.UsuarioBuilder().userName("juan@noauth.com").build());
        assertThat(authorities.size(), is(1));
        assertThat(authorities, hasItem(INQUILINO.authority));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComuByUserAndComu() throws EntityException
    {
        // UsuarioComunidad en BD.
        assertThat(usuarioManager.getUserComuByUserAndComu(pedro.getUserName(), 1L), is(pedro_plazuelas_10bis));
        // No existe la combinación (usario, comunidad); existe la comunidad.
        assertThat(usuarioManager.getUserComuByUserAndComu("paco@paco.com", 1L), nullValue());
        // No existe la comunidad.
        assertThat(usuarioManager.getUserComuByUserAndComu("paco@paco.com", 111L), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserByUserName_1() throws EntityException
    {
        usuarioManager.regComuAndUserAndUserComu(makeUsuarioComunidad(
                COMU_LA_PLAZUELA_5, pedro, "portal1", "EE", "3", null, INQUILINO.function), oneComponent_local_ES);
        Usuario usuario = usuarioManager.getUserByUserName(pedro.getUserName());
        assertThat(usuario.getUserName(), is(pedro.getUserName()));
        assertThat(usuario.getuId() > 0L, is(true));
        assertThat(usuario.getAlias(), is(pedro.getAlias()));
        assertThat(usuario.getPassword().isEmpty(), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestUserComuId_1() throws EntityException
    {
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(3L).build(), 1L), is(true));
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(5L).build(), 1L), is(false));
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(7L).build(), 2L), is(true));
    }

    @Test
    public void testIsOldestUserComuId_2() throws EntityException
    {
        // Caso: la comunidad no existe en BD.
        Usuario pedroUser = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("password3").build();
        try {
            assertThat(usuarioManager.isOldestUserComu(pedroUser, 999L), is(true));
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
        Usuario pedroOk = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("password3").build();
        Usuario pedroWrongUserName = new Usuario.UsuarioBuilder().userName("pedro@wrong.com").password("password3")
                .build();
        Usuario pedroWrongPassword = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("passwordWrong")
                .build();

        assertThat(usuarioManager.login(pedroOk), is(true));
        assertThat(usuarioManager.login(pedroWrongPassword), is(false));

        try {
            assertThat(usuarioManager.login(pedroWrongUserName), is(true));
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testMakeNewPassword_1() throws EntityException, UnsupportedEncodingException
    {
        String newPassword = usuarioManager.makeNewPassword();
        checkGeneratedPassword(newPassword, default_password_length);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws EntityException
    {
        // Caso 1: usuario ADM, no 'oldest' in comunidad.
        Comunidad comunidadChanged = new Comunidad.ComunidadBuilder().copyComunidadNonNullValues(ronda_plazuela_10bis).sufijoNumero("Tris").build();
        assertThat(usuarioManager.checkComuDataModificationPower(luis, ronda_plazuela_10bis), is(true));
        assertThat(usuarioManager.modifyComuData(luis, comunidadChanged), is(1));
        // Caso 2: usuario 'oldest' in comunidad, no ADM.
        comunidadChanged = new Comunidad.ComunidadBuilder().copyComunidadNonNullValues(calle_la_fuente_11).sufijoNumero("QAC").build();
        assertThat(usuarioManager.checkComuDataModificationPower(juan, calle_la_fuente_11), is(true));
        assertThat(usuarioManager.modifyComuData(juan, comunidadChanged), is(1));

        // Caso 3: usuario no ADM, no 'oldest' in comunidad.
        UsuarioComunidad userComu = makeUsuarioComunidad(
                calle_el_escorial,
                luis,
                "portalB", "escB", "plantaZ", "door31", INQUILINO.function);
        assertThat(usuarioManager.regUserComu(userComu), is(1));
        assertThat(usuarioManager.checkComuDataModificationPower(luis, calle_el_escorial), is(false));

        comunidadChanged = new Comunidad.ComunidadBuilder().copyComunidadNonNullValues(calle_el_escorial).sufijoNumero("TRAS").build();
        try {
            usuarioManager.modifyComuData(luis, comunidadChanged);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }
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

        assertThat(usuarioManager.modifyUser(usuarioIn, "juan@noauth.com", twoComponent_local_ES), is(1));
        assertThat(usuarioManager.getUserByUserName("juan@noauth.com").getAlias(), is("new_juan_alias"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2() throws EntityException, IOException, MessagingException
    {
        Usuario oldUser = usuarioManager.getUserByUserName("juan@noauth.com");
        // We change userName.
        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .uId(7L)
                .userName(TO)
                .build();

        assertThat(usuarioManager.modifyUser(usuarioNew, "juan@noauth.com", oneComponent_local_ES), is(1));
        // Check new data in DB.
        assertThat(usuarioManager.getUserByUserName(TO), allOf(
                hasProperty("uId", equalTo(7L)),
                hasProperty("alias", is("juan_no_auth")))
        );
        // Login changed.
        try {
            usuarioManager.login(oldUser);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
        checkPswdSentAndLogin(usuarioNew);
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
            usuarioManager.modifyUser(usuarioNew, "juan@noauth.com", twoComponent_local_ES);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_DATA_NOT_MODIFIED));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_ModifyUserGcmToken_1() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().copyUsuario(usuarioManager.completeUser("juan@noauth.com")).gcmToken("GCMtoKen1234X").build();
        // Verificamos la premisa.
        assertThat(usuarioManager.getGcmToken(usuario.getuId()), nullValue());
        // Insertamos token.
        assertThat(usuarioManager.modifyUserGcmToken(usuario.getUserName(), usuario.getGcmToken()), is(1));
        // Verificamos.
        assertThat(usuarioManager.getGcmToken(usuario.getuId()), is("GCMtoKen1234X"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_ModifyUserGcmToken_2() throws EntityException
    {
        // Caso: no existe usuario.
        try {
            usuarioManager.modifyUserGcmToken("noexist_user", "GCMtoKen1234X");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_ModifyUserGcmTokens()
    {
        assertThat(usuarioManager.modifyUserGcmToken(pedro), is(1));
        assertThat(usuarioManager.modifyUserGcmToken(luis), is(1));
        assertThat(usuarioManager.modifyUserGcmToken(juan), is(1));

        List<GcmTokensHolder> holders = new ArrayList<>(3);
        holders.add(new GcmTokensHolder(null, luis.getGcmToken()));
        holders.add(new GcmTokensHolder(null, pedro.getGcmToken()));
        holders.add(new GcmTokensHolder("new_juan_token", juan.getGcmToken()));
        assertThat(usuarioManager.modifyUserGcmTokens(holders), is(3));

        assertThat(usuarioDao.getUsuarioWithGcmToken(luis.getuId()).getGcmToken(), nullValue());
        assertThat(usuarioDao.getUsuarioWithGcmToken(pedro.getuId()).getGcmToken(), nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordChangeWithName_1() throws EntityException
    {
        assertThat(usuarioManager.passwordChangeWithName(luis.getUserName(), "new_luis_password"), is(1));
        assertThat(new BCryptPasswordEncoder().matches("new_luis_password", usuarioDao.getUsuarioById(luis.getuId()).getPassword()),
                is(true));
        assertThat(usuarioManager.login(new Usuario.UsuarioBuilder().copyUsuario(luis).password("new_luis_password").build()), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordChangeWithName_2() throws EntityException
    {
        // Precondition: no existe usuario.
        Usuario newPepe = new Usuario.UsuarioBuilder().copyUsuario(pepe).uId(999L).build();
        try {
            usuarioManager.passwordChangeWithName(newPepe.getUserName(), "newPassword");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordSend() throws Exception
    {
        // Preconditions: dirección email válida.
        Usuario userDroid = new Usuario.UsuarioBuilder().copyUsuario(pedro).userName(TO).build();
        UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, userDroid).userComuRest(pedro_escorial).build();
        assertThat(usuarioManager.regComuAndUserAndUserComu(userComu, twoComponent_local_ES), is(true));
        // Check password generated and sent.
        javaMailMonitor.extSetUp();
        String password = javaMailMonitor.getPswdFromMsg();
        // Check login.
        Usuario userReg = new Usuario.UsuarioBuilder().copyUsuario(usuarioManager.getUserByUserName(TO)).password(password).build();
        assertThat(usuarioManager.login(userReg), is(true));

        // Exec test.
        assertThat(usuarioManager.passwordSend(userReg.getUserName(), oneComponent_local_ES), is(true));
        assertThat(usuarioManager.login(userReg), is(false));

        // Check password generated and sent.
        javaMailMonitor.expungeFolder();
        password = javaMailMonitor.getPswdFromMsg();
        checkGeneratedPassword(password, default_password_length);
        // Check login.
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(userReg).password(password).build();
        assertThat(usuarioManager.login(userIn), is(true));
        // Cleaning and closing.
        javaMailMonitor.closeStoreAndFolder();
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordSend_mailFail()
    {
        // Preconditions: dirección email válida.
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .copyUsuario(usuarioManager.getUserByUserName(pedro.getUserName()))
                .password(pedro.getPassword()).build();
        // Exec
        try {
            usuarioManager.passwordSend(usuarioIn.getUserName(), oneComponent_local_ES, mailServiceForTest);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // Login not changed.
        assertThat(usuarioManager.login(usuarioIn), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_1() throws EntityException, IOException, MessagingException
    {
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(USER_JUAN).userName(TO).password(null).build();
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, userIn, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);
        // Exec.
        assertThat(usuarioManager.regComuAndUserAndUserComu(userComu, oneComponent_local_ES), is(true));
        checkPswdSentAndLogin(userIn);
        // Check alta nueva comunidad.
        List<UsuarioComunidad> comunidades = usuarioManager.seeUserComusByUser(userIn.getUserName());
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(userComu));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_2() throws EntityException
    {
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(USER_JUAN).userName(TO).password(null).build();
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, userIn, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);
        // Exec.
        try {
            usuarioManager.regComuAndUserAndUserComu(userComu, oneComponent_local_ES, mailServiceForTest);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // No new data in database.
        UsuarioTestUtils.checkUserNotFound(userIn.getUserName(), usuarioManager);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_3() throws EntityException
    {
        boolean isInserted_1 = usuarioManager.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN, oneComponent_local_ES);
        assertThat(isInserted_1, is(true));
        try {
            usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, twoComponent_local_ES);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_4() throws EntityException
    {
        boolean isInserted_1 = usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, oneComponent_local_ES);
        assertThat(isInserted_1, is(true));
        try {
            usuarioManager.regComuAndUserAndUserComu(COMU_REAL_PEPE, oneComponent_local_ES);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu_1() throws EntityException
    {
        // Preconditions: there is DB a user registered in a comunidad: juan en comunidades 2 y 4.
        assertThat(usuarioManager.getComusByUser(juan.getUserName()).size(), is(2));

        UsuarioComunidad usuarioComunidad2 = makeUsuarioComunidad(COMU_OTRA, juan, "AB", "ESC", "11", "puert",
                ADMINISTRADOR.function);

        boolean isRegOk = usuarioManager.regComuAndUserComu(usuarioComunidad2);
        assertThat(isRegOk, is(true));

        List<UsuarioComunidad> comunidades = usuarioManager.seeUserComusByUser(juan.getUserName());
        assertThat(comunidades.size(), is(3));
        assertThat(comunidades, hasItem(usuarioComunidad2));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu_2() throws EntityException
    {
        // Preconditions: there is DB a user registered in a comunidad.
        assertThat(usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, twoComponent_local_ES), is(true));
        try {
            usuarioManager.regComuAndUserComu(COMU_REAL_PEPE);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws EntityException, MessagingException, IOException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.getComunidadById(calle_el_escorial.getC_Id()), is(calle_el_escorial));

        // Nuevo usuarioComunidad.
        UsuarioComunidad newPepe = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build(),
                new Usuario.UsuarioBuilder().copyUsuario(USER_PEPE).userName(TO).build(),
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);

        assertThat(usuarioManager.regUserAndUserComu(newPepe, oneComponent_local_ES), is(true));
        checkPswdSentAndLogin(newPepe.getUsuario());

        // Check alta en comunidad.
        assertThat(usuarioManager.getComusByUser(TO).get(0), is(calle_el_escorial));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_2() throws EntityException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.getComunidadById(calle_el_escorial.getC_Id()), is(calle_el_escorial));
        // Nuevo usuarioComunidad.
        UsuarioComunidad newPepe = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build(),
                new Usuario.UsuarioBuilder().copyUsuario(USER_PEPE).userName(TO).build(),
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);

        try {
            usuarioManager.regUserAndUserComu(newPepe, oneComponent_local_ES, mailServiceForTest);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // No new data in database.
        UsuarioTestUtils.checkUserNotFound(TO, usuarioManager);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_3() throws EntityException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN, twoComponent_local_ES), is(true));
        try {
            usuarioManager.regUserAndUserComu(COMU_REAL_JUAN, oneComponent_local_ES);
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidades_1() throws EntityException
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
        usuarioManager.regComuAndUserAndUserComu(usuarioComunidad_1, oneComponent_local_ES);
        Usuario userWithPk = usuarioManager.getUserByUserName(USER_JUAN.getUserName());

        // Primera búsqueda.
        List<Comunidad> comunidades = usuarioManager.searchComunidades(comunidad);
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
        usuarioManager.regComuAndUserComu(usuarioComunidad_2);

        // Segunda búsqueda.
        comunidades = usuarioManager.searchComunidades(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(comunidad2));

        // Caso 3: una comunidad con coincidencia completa. Se aplica searchOne.
        final UsuarioComunidad usuarioComunidad_3 = makeUsuarioComunidad(comunidad, userWithPk, null,
                null, "planta3", "doorA", ADMINISTRADOR.function);
        usuarioManager.regComuAndUserComu(usuarioComunidad_3);

        // Tercera búsqueda.
        comunidades = usuarioManager.searchComunidades(comunidad);
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(comunidad));
    }

    // ======================================== CHECKERS ========================================

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CheckComuDataModificationPower()
    {
        // No oldest, but adm.
        assertThat(usuarioManager.checkComuDataModificationPower(
                new Usuario.UsuarioBuilder().copyUsuario(luis).build(), new Comunidad.ComunidadBuilder().c_id(1L).build()
        ), is(true));
        // Usuario oldest no adm.
        assertThat(usuarioManager.checkComuDataModificationPower(
                new Usuario.UsuarioBuilder().copyUsuario(juan).build(), new Comunidad.ComunidadBuilder().c_id(2L).build()
        ), is(true));
    }

    // ======================================== HELPERS ========================================

    private Usuario doUsuario(String userName, String gcmToken) throws EntityException
    {
        return new Usuario.UsuarioBuilder().uId(usuarioManager.completeUser(userName).getuId()).gcmToken
                (gcmToken).build();
    }

    private void checkPswdSentAndLogin(Usuario newUsuario) throws MessagingException, IOException
    {
        // Check password generated and sent.
        javaMailMonitor.extSetUp();
        String password = javaMailMonitor.getPswdFromMsg();
        checkGeneratedPassword(password, default_password_length);
        // Cleaning and closing.
        javaMailMonitor.closeStoreAndFolder();
        // Check login.
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(newUsuario).password(password).build();
        assertThat(usuarioManager.login(userIn), is(true));
    }
}

