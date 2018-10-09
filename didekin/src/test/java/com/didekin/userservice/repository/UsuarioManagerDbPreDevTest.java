package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.userservice.mail.UsuarioMailServiceForTest;
import com.didekin.userservice.testutils.UsuarioTestUtils;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuario.http.AuthHeader;
import com.didekinlib.model.usuario.http.AuthHeaderIf;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.common.testutils.LocaleConstant.twoComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.repository.PswdGenerator.default_password_length;
import static com.didekin.userservice.repository.UsuarioManager.BCRYPT_SALT;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_OTRA;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_PLAZUELA5_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_la_fuente_11;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkBeanUsuario;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkGeneratedPassword;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.doHttpAuthHeader;
import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro_plazuelas_10bis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pepe;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.model.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_DUPLICATE;
import static com.didekinlib.model.comunidad.http.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.PASSWORD_WRONG;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_DATA_NOT_MODIFIED;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_DUPLICATE;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_WRONG_INIT;
import static com.didekinlib.model.usuario.http.UsuarioServConstant.IS_USER_DELETED;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.Rol.PRESIDENTE;
import static com.didekinlib.model.usuariocomunidad.Rol.PROPIETARIO;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.mindrot.jbcrypt.BCrypt.hashpw;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        UserMockRepoConfiguration.class,
        UsuarioMailConfigurationPre.class})
@Category({LocalDev.class, DbPre.class})
@ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
public class UsuarioManagerDbPreDevTest {

    @Autowired
    private UsuarioManager usuarioManager;
    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private JavaMailMonitor javaMailMonitor;
    @Autowired
    UsuarioMailServiceForTest mailServiceForTest;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CompleteWithUserComuRoles()
    {
        assertThat(usuarioManager.completeWithUserComuRoles(luis.getUserName(), 1L).getRoles(), is("adm,pro"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUser_1() throws ServiceException
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
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteUserAndComunidades() throws ServiceException
    {
        int comuDeleted = usuarioManager.deleteUserAndComunidades(pedro.getUserName());
        assertThat(comuDeleted, is(3));

        assertThat(usuarioManager.comunidadDao.getComunidadById(1L).getC_Id(), is(1L));
        assertThat(usuarioManager.comunidadDao.getComunidadById(2L).getC_Id(), is(2L));
        try {
            usuarioManager.comunidadDao.getComunidadById(3L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        // Check usuario borrado.
        try {
            usuarioManager.getUserData(pedro.getUserName());
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
        // La comunidad tiene 2 usuarios. El usuario tiene 3 comunidades.
        Comunidad comunidad = new Comunidad.ComunidadBuilder().c_id(1L).build();
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).userName(pedro.getUserName()).build();
        UsuarioComunidad usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();

        int isDeleted = usuarioManager.deleteUserComunidad(usuarioComunidad);
        assertThat(isDeleted, is(1));

        Comunidad comunidadDb = usuarioManager.comunidadDao.getComunidadById(comunidad.getC_Id());
        assertThat(comunidadDb.getC_Id(), is(comunidad.getC_Id()));

        // La comunidad tiene 1 usuario.
        comunidad = new Comunidad.ComunidadBuilder().c_id(3L).build();
        usuarioComunidad = new UsuarioComunidad.UserComuBuilder(comunidad, usuario).build();
        isDeleted = usuarioManager.deleteUserComunidad(usuarioComunidad);
        assertThat(isDeleted, is(1));
        try {
            usuarioManager.comunidadDao.getComunidadById(comunidad.getC_Id());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
            assertThat(e.getExceptionMsg().getHttpMessage(), is(COMUNIDAD_NOT_FOUND.getHttpMessage()));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_2() throws ServiceException
    {
        // El usuario tiene una comunidad. La comunidad tiene un usuario.
        assertThat(usuarioManager.deleteUserComunidad(pedro_plazuelas_10bis), is(1));
        // Preconditions.
        assertThat(usuarioManager.getComusByUser(luis.getUserName()).size(), is(1));
        assertThat(usuarioManager.seeUserComusByComu(luis.getUserName(), ronda_plazuela_10bis.getC_Id()).size(), is(1));
        // Exec and check.
        assertThat(usuarioManager.deleteUserComunidad(luis_plazuelas_10bis), is(IS_USER_DELETED));
        try {
            usuarioManager.comunidadDao.getComunidadById(ronda_plazuela_10bis.getC_Id());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        UsuarioTestUtils.checkUserNotFound(luis.getUserName(), usuarioManager);

        // El usuario tiene tres comunidades. La comunidad tiene 1 usuario.
        // Preconditions.
        assertThat(usuarioManager.getComusByUser(pedro.getUserName()).size() > 1, is(true));
        assertThat(usuarioManager.seeUserComusByComu(pedro.getUserName(), calle_el_escorial.getC_Id()).size(), is(1));
        // Exec.
        assertThat(usuarioManager.deleteUserComunidad(pedro_escorial), is(1));
        // Check.
        try {
            usuarioManager.comunidadDao.getComunidadById(3L);
            fail("NO existe la comunidad");
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
        try {
            assertThat(usuarioManager.getUserData(pedro.getUserName()), notNullValue());
        } catch (ServiceException e) {
            fail();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUserComunidad_3() throws ServiceException
    {
        // Usuario con una comunidad y comunidad con dos usuarios.
        assertThat(usuarioManager.getComusByUser(luis.getUserName()).size(), is(1));
        assertThat(usuarioManager.seeUserComusByComu(luis.getUserName(), ronda_plazuela_10bis.getC_Id()).size(), is(2));
        // Exec.
        assertThat(usuarioManager.deleteUserComunidad(luis_plazuelas_10bis), is(IS_USER_DELETED));
        // Check.
        assertThat(usuarioManager.getComunidadById(ronda_plazuela_10bis.getC_Id()), is(ronda_plazuela_10bis));
        try {
            usuarioManager.getUserData(luis.getUserName());
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetComunidadById() throws ServiceException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().uId(3L).build();
        assertThat(usuarioManager.getComunidadById(usuario, 2L), is(new Comunidad.ComunidadBuilder().c_id(2L).build()));

        try {
            // No hay vínculo usuario-comunidad; ambos existen.
            usuarioManager.getComunidadById(usuario, 4L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }

        try {
            // No existe el usuario.
            usuarioManager.getComunidadById(new Usuario.UsuarioBuilder().uId(99L).build(), 4L);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetGcmTokensByComunidad() throws ServiceException
    {
        List<String> tokens = usuarioManager.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(1L).build().getC_Id());
        assertThat(tokens.size(), is(1));
        assertThat(tokens, hasItem(luis.getGcmToken()));
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
    public void testGetUserComuByUserAndComu() throws ServiceException
    {
        // UsuarioComunidad en BD.
        assertThat(usuarioManager.getUserComuByUserAndComu(pedro.getUserName(), 1L), is(pedro_plazuelas_10bis));

        // No existe la combinación (usario, comunidad); existe la comunidad.
        assertThat(usuarioManager.getUserComuByUserAndComu("paco@paco.com", 1L), nullValue());

        // No existe la comunidad.
        try {
            usuarioManager.getUserComuByUserAndComu("paco@paco.com", 111L);
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_getUserData_1() throws ServiceException
    {
        // Although luis has authToken initialized, it is not updated in DB.
        assertThat(luis.getTokenAuth(), notNullValue());
        // Exec.
        usuarioManager.regComuAndUserAndUserComu(makeUsuarioComunidad(
                COMU_LA_PLAZUELA_5, luis, "portal1", "EE", "3", null, INQUILINO.function), oneComponent_local_ES);
        // Check.
        checkBeanUsuario(
                usuarioManager.getUserData(luis.getUserName()),
                usuarioManager.usuarioDao.getUserDataByName(luis.getUserName()),
                true);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsOldestUserComuId_1() throws ServiceException
    {
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(3L).build(), 1L), is(true));
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(5L).build(), 1L), is(false));
        assertThat(usuarioManager.isOldestUserComu(new Usuario.UsuarioBuilder().uId(7L).build(), 2L), is(true));
    }

    @Test
    public void testIsOldestUserComuId_2() throws ServiceException
    {
        // Caso: la comunidad no existe en BD.
        Usuario pedroUser = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("password3").build();
        try {
            assertThat(usuarioManager.isOldestUserComu(pedroUser, 999L), is(true));
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_1() throws ServiceException
    {
        Usuario pedroOk = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("password3").build();
        Usuario pedroWrongUserName = new Usuario.UsuarioBuilder().userName("pedro@wrong.com").password("password3")
                .build();
        Usuario pedroWrongPassword = new Usuario.UsuarioBuilder().userName(pedro.getUserName()).password("passwordWrong")
                .build();
        Usuario pedroInvalidUserName = new Usuario.UsuarioBuilder().userName("pedro_invalid_name").password("password4")
                .build();

        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(pedroOk)), is(true));

        try {
            usuarioManager.login(pedroWrongPassword);
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(PASSWORD_WRONG));
        }

        try {
            usuarioManager.login(pedroWrongUserName);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }

        try {
            usuarioManager.login(pedroInvalidUserName);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testMakeNewPassword_1() throws ServiceException
    {
        String newPassword = usuarioManager.makeNewPassword();
        checkGeneratedPassword(newPassword, default_password_length);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyComuData() throws ServiceException
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
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_1() throws ServiceException
    {
        // Preconditions.
        final Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .uId(7L)
                .userName(juan.getUserName())
                .password(juan.getPassword())
                .alias("new_juan_alias")
                .build();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(usuarioIn)), is(true));
        // We change alias ONLY.
        assertThat(usuarioManager.modifyUser(usuarioIn, "juan@noauth.com", twoComponent_local_ES), is(1));
        assertThat(usuarioManager.getUserData("juan@noauth.com").getAlias(), is("new_juan_alias"));
        // Login has not changed.
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(usuarioIn)), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2() throws ServiceException, IOException, MessagingException
    {
        Usuario oldUser = usuarioManager.getUserData("juan@noauth.com");
        // We change userName.
        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .uId(7L)
                .userName(TO)
                .build();

        //noinspection SynchronizeOnNonFinalField
        synchronized (javaMailMonitor) {
            javaMailMonitor.extSetUp();
            assertThat(usuarioManager.modifyUser(usuarioNew, "juan@noauth.com", oneComponent_local_ES), is(1));
            // Check new data in DB.
            assertThat(usuarioManager.getUserData(TO), allOf(
                    hasProperty("uId", equalTo(7L)),
                    hasProperty("alias", is("juan_no_auth")))
            );
            // Login changed.
            try {
                usuarioManager.login(oldUser);
                fail();
            } catch (ServiceException e) {
                assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
            }
            checkPswdSentAndLogin(usuarioNew);
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_3() throws ServiceException
    {
        // UserName and alias null.
        final Usuario usuarioNew = new Usuario.UsuarioBuilder()
                .uId(7L)
                .build();
        try {
            usuarioManager.modifyUser(usuarioNew, "juan@noauth.com", twoComponent_local_ES);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_DATA_NOT_MODIFIED));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordChange_1() throws ServiceException
    {
        String newAuthToken = usuarioManager.passwordChange(luis.getUserName(), luis.getPassword(), "new_luis_password");
        // Check on returned new authToken.
        assertThat(newAuthToken, allOf(notNullValue(), not(is(luis.getTokenAuth()))));
        Usuario newLuis = usuarioManager.getUserData(luis.getUserName());
        assertThat(checkpw(newAuthToken, newLuis.getTokenAuth()), is(true));
        // Check on updated new password.
        assertThat(checkpw("new_luis_password", newLuis.getPassword()), is(true));
        // Check login with new password.
        assertThat(usuarioManager.login(new Usuario.UsuarioBuilder().copyUsuario(luis).password("new_luis_password").build()),
                notNullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordChange_2() throws ServiceException
    {
        // Precondition: no existe usuario.
        Usuario oldFakedUser = new Usuario.UsuarioBuilder().copyUsuario(pepe).uId(999L).build();
        try {
            usuarioManager.passwordChange(oldFakedUser.getUserName(), oldFakedUser.getPassword(), "newPassword");
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_NOT_FOUND));
        }

        // Precondition: old password wrong.
        oldFakedUser = new Usuario.UsuarioBuilder().copyUsuario(paco).password("password_wrong").build();
        try {
            usuarioManager.passwordChange(oldFakedUser.getUserName(), oldFakedUser.getPassword(), "newPassword");
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_WRONG));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordSend_1() throws Exception
    {
        // Preconditions:
        Usuario userDroid = new Usuario.UsuarioBuilder().copyUsuario(pedro).userName(TO).build();
        UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(calle_el_escorial, userDroid).userComuRest(pedro_escorial).build();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(userMockManager.regComuAndUserAndUserComu(userComu)), is(true));
        // Check login.
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(userDroid)), is(true));

        // Exec test.
        //noinspection SynchronizeOnNonFinalField
        synchronized (javaMailMonitor) {
            javaMailMonitor.extSetUp();
            assertThat(usuarioManager.passwordSend(usuarioManager.getUserData(TO).getUserName(), oneComponent_local_ES), is(true));
            // Login no válido una vez generado el nuevo password.
            try {
                usuarioManager.login(userDroid);
                fail();
            } catch (ServiceException se) {
                assertThat(se.getExceptionMsg(), is(PASSWORD_WRONG));
            }

            // Check password generated and sent.
            String password = javaMailMonitor.getPswdFromMsg();
            checkGeneratedPassword(password, default_password_length);
            // Check login with new password.
            Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(userDroid).password(password).build();
            assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(userIn)), is(true));
            // Cleaning and closing.
            javaMailMonitor.closeStoreAndFolder();
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_passwordSend_2()
    {
        // Preconditions: dirección email válida.
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .copyUsuario(usuarioManager.getUserData(pedro.getUserName()))
                .password(pedro.getPassword()).build();
        // Exec
        try {
            usuarioManager.passwordSend(usuarioIn.getUserName(), oneComponent_local_ES, mailServiceForTest);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // Login not changed.
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(usuarioIn)), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_1() throws ServiceException, IOException, MessagingException
    {
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(USER_JUAN).userName(TO).password(null).build();
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, userIn, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);
        // Exec.
        //noinspection SynchronizeOnNonFinalField
        synchronized (javaMailMonitor) {
            javaMailMonitor.extSetUp();
            assertThat(usuarioManager.regComuAndUserAndUserComu(userComu, oneComponent_local_ES), is(true));
            checkPswdSentAndLogin(userIn);
        }
        // Check alta nueva comunidad.
        List<UsuarioComunidad> comunidades = usuarioManager.seeUserComusByUser(userIn.getUserName());
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(userComu));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_2() throws ServiceException
    {
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(USER_JUAN).userName(TO).password(null).build();
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, userIn, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);

        // Exec: mailServiceForTest is wrongly configured.
        try {
            usuarioManager.regComuAndUserAndUserComu(userComu, oneComponent_local_ES, mailServiceForTest);
            fail();
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // No new data in database.
        UsuarioTestUtils.checkUserNotFound(userIn.getUserName(), usuarioManager);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_3() throws ServiceException
    {
        boolean isInserted_1 = usuarioManager.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN, oneComponent_local_ES);
        assertThat(isInserted_1, is(true));
        try {
            usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, twoComponent_local_ES);
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_4() throws ServiceException
    {
        boolean isInserted_1 = usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, oneComponent_local_ES);
        assertThat(isInserted_1, is(true));
        try {
            usuarioManager.regComuAndUserAndUserComu(COMU_REAL_PEPE, oneComponent_local_ES);
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserComu_1() throws ServiceException
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
    public void testRegComuAndUserComu_2() throws ServiceException
    {
        // Preconditions: there is DB a user registered in a comunidad.
        assertThat(usuarioManager.regComuAndUserAndUserComu(COMU_REAL_JUAN, twoComponent_local_ES), is(true));
        try {
            usuarioManager.regComuAndUserComu(COMU_REAL_PEPE);
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(COMUNIDAD_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws ServiceException, MessagingException, IOException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.getComunidadById(calle_el_escorial.getC_Id()), is(calle_el_escorial));

        // Nuevo usuarioComunidad.
        UsuarioComunidad newPepe = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build(),
                new Usuario.UsuarioBuilder().copyUsuario(USER_PEPE).userName(TO).build(),
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);

        //noinspection SynchronizeOnNonFinalField
        synchronized (javaMailMonitor) {
            javaMailMonitor.extSetUp();
            assertThat(usuarioManager.regUserAndUserComu(newPepe, oneComponent_local_ES), is(true));
            checkPswdSentAndLogin(newPepe.getUsuario());
        }

        // Check alta en comunidad.
        assertThat(usuarioManager.getComusByUser(TO).get(0), is(calle_el_escorial));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_2() throws ServiceException
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
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(PASSWORD_NOT_SENT));
        }
        // No new data in database.
        UsuarioTestUtils.checkUserNotFound(TO, usuarioManager);
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_3() throws ServiceException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN, twoComponent_local_ES), is(true));
        try {
            usuarioManager.regUserAndUserComu(COMU_REAL_JUAN, oneComponent_local_ES);
        } catch (ServiceException e) {
            assertThat(e.getExceptionMsg(), is(USER_DUPLICATE));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSearchComunidades_1() throws ServiceException
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
        Usuario userWithPk = usuarioManager.getUserData(USER_JUAN.getUserName());

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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_SeeUserComusByComu()
    {
        // Premise: user in comunidad.
        assertThat(usuarioManager.isUserInComunidad(pedro.getUserName(), calle_la_fuente_11.getC_Id()), is(true));
        // Check.
        assertThat(usuarioManager.seeUserComusByComu(pedro.getUserName(), calle_la_fuente_11.getC_Id()).size(), is(2));

        // Premise: user not in comunidad.
        assertThat(usuarioManager.isUserInComunidad(luis.getUserName(), calle_la_fuente_11.getC_Id()), is(false));
        try {
            usuarioManager.seeUserComusByComu(luis.getUserName(), calle_la_fuente_11.getC_Id());
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(UNAUTHORIZED_TX_TO_USER));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_UpdateTokenAuthInDb()
    {
        // Premises: user in DB.
        String authToken = usuarioManager.updateTokenAuthInDb(pedro);
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(authToken), is(true));
        assertThat(checkpw(authToken, usuarioManager.getUserData(pedro.getUserName()).getTokenAuth()), is(true));

        // Premises: user not in DB.
        try {
            usuarioManager.updateTokenAuthInDb(new Usuario.UsuarioBuilder().uId(9999).userName("fake@user.com").gcmToken("fake.gcm_token").build());
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(USER_NOT_FOUND));
        }
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

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_checkHeaderGetUserName()
    {
        String httpAuthHeader = doHttpAuthHeader(pedro, usuarioManager.producerBuilder);
        AuthHeaderIf headerIn = new AuthHeader.AuthHeaderBuilder().tokenFromJsonBase64Header(httpAuthHeader).build();
        // Premises: token in BD.
        assertThat(usuarioManager.updateTokenAuthInDb(pedro, headerIn.getToken()), notNullValue());
        // Exec, check.
        assertThat(usuarioManager.checkHeaderGetUserName(httpAuthHeader), is(pedro.getUserName()));

        // Premises: token are not the same.
        AuthHeaderIf headerDb = doAuthHeader(pedro, usuarioManager.producerBuilder);
        assertThat(headerIn.getToken().equals(headerDb.getToken()), is(false));
        assertThat(usuarioManager.updateTokenAuthInDb(pedro, headerDb.getToken()), notNullValue());
        // Exec, check.
        try {
            usuarioManager.checkHeaderGetUserName(httpAuthHeader);
            fail();
        } catch (ServiceException se) {
            assertThat(se.getExceptionMsg(), is(UNAUTHORIZED));
        }
    }

    // ======================================== TESTS of HELPERS ========================================

    @Test
    public void testEncryptedPsw_1()
    {
        String password = "password11";
        String encodePsw = hashpw(password, BCRYPT_SALT.get());
        assertThat(checkpw(password, encodePsw), is(true));
        assertThat(encodePsw.length(), is(60));
        // Check that password hash is not deterministic.
        String encodePswBis = hashpw(password, BCRYPT_SALT.get());
        assertThat(encodePsw.equals(encodePswBis), is(false));
    }

    @Test
    public void testEncryptedPsw_2()
    {
        String tokenToHash = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0" +
                "." +
                "._L86WbOFHY-3g0E2EXejJg" +
                ".UB1tHZZq0TYFTZKPVZXY83GRxHz770Aq7BuMCEbNnaSC5cVNOLEOgBQrOQVJmVL-9Ke9KRSwuq7MmVcA2EB_0xRBr_YbzmMWbpUcTQUFtE5OZOFiCsxL5Yn0gA_DDLZboivpoSqndQRP-44mWVkM1A" +
                ". RIvTWRrsyoJ1mpl8vUhQDQ";
        String encodToken = hashpw(tokenToHash, BCRYPT_SALT.get());
        assertThat(checkpw(tokenToHash, encodToken), is(true));
        assertThat(encodToken.length(), is(60));
    }

    // ======================================== HELPERS ========================================

    private void checkPswdSentAndLogin(Usuario newUsuario) throws MessagingException, IOException
    {
        // Check password generated and sent.
        String password = javaMailMonitor.getPswdFromMsg();
        checkGeneratedPassword(password, default_password_length);
        // Cleaning and closing.
        javaMailMonitor.closeStoreAndFolder();
        // Check login.
        Usuario userIn = new Usuario.UsuarioBuilder().copyUsuario(newUsuario).password(password).build();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(userIn)), is(true));
    }
}

