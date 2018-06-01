package com.didekin.userservice.controller;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.http.HttpHandler;
import com.didekinlib.http.usuario.UsuarioEndPoints;
import com.didekinlib.http.usuariocomunidad.UsuarioComunidadEndPoints;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_PLAZUELA5_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.PASSWORD_WRONG;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UsuarioControllerTest {

    private UsuarioEndPoints USER_ENDPOINT;
    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UsuarioManager usuarioManager;
    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private JavaMailMonitor javaMailMonitor;

    @Before
    public void setUp()
    {
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

//    ===================================== CONTROLLER TESTS =======================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUser() throws IOException
    {
        final String accessToken = userMockManager.insertTokenGetHeaderStr(pedro.getUserName(), pedro.getGcmToken());
        List<UsuarioComunidad> comunidades = USERCOMU_ENDPOINT
                .seeUserComusByUser(accessToken).execute().body();
        assertThat(comunidades.size(), is(3));

        boolean isDeleted = USER_ENDPOINT.deleteUser(accessToken).execute().body();
        // Borra al usuario y las comunidades previamente encontradas en la consulta.
        assertThat(isDeleted, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_1() throws IOException
    {
        Usuario usuarioDB =
                USER_ENDPOINT.getUserData(userMockManager.insertTokenGetHeaderStr(luis.getUserName(), luis.getGcmToken())).execute().body();

        assertThat(usuarioDB.getuId(), is(luis.getuId()));
        assertThat(usuarioDB.getUserName(), is(luis.getUserName()));
        assertThat(usuarioDB.getAlias(), is(luis.getAlias()));
        assertThat(usuarioDB.getGcmToken(), is(luis.getGcmToken()));
        assertThat(usuarioDB.getPassword(), is(nullValue()));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_2() throws IOException
    {
        // We send an invailid token.
        Response<Usuario> response = USER_ENDPOINT.getUserData("faked_token").execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(TOKEN_ENCRYP_DECRYP_ERROR.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_1()
    {
        String userNameOk = pedro.getUserName();
        String userNameWrong = "pedro@wrong.com";
        String passwordOk = "password3";
        String passwordWrong = "passwordWrong";

        USER_ENDPOINT.login(userNameOk, passwordOk, pedro.getGcmToken())
                .test()
                .assertValue(response -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(response.body()));
        USER_ENDPOINT.login(userNameOk, passwordWrong, pedro.getGcmToken())
                .test()
                .assertValue(response -> retrofitHandler.getErrorBean(response).getMessage().equals(PASSWORD_WRONG.getHttpMessage()));
        USER_ENDPOINT.login(userNameWrong, passwordWrong, pedro.getGcmToken()).test()
                .assertValue(response -> retrofitHandler.getErrorBean(response).getMessage().equals(USER_NAME_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_1() throws IOException
    {
        // Change userName.
        Usuario usuarioIn_1 = new Usuario.UsuarioBuilder()
                .userName("new_paco@new.com")
                .uId(paco.getuId())
                .build();

        assertThat(USER_ENDPOINT.modifyUser(
                oneComponent_local_ES,
                userMockManager.insertTokenGetHeaderStr(paco.getUserName(), paco.getGcmToken()),
                usuarioIn_1)
                .execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2() throws IOException
    {
        // Change alias.
        Usuario usuarioIn_1 = new Usuario.UsuarioBuilder()
                .userName(paco.getUserName())
                .alias("newAlias")
                .uId(paco.getuId())
                .build();

        assertThat(USER_ENDPOINT.modifyUser(
                oneComponent_local_ES,
                userMockManager.insertTokenGetHeaderStr(paco.getUserName(), paco.getGcmToken()),
                usuarioIn_1)
                .execute().body(), is(1));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserGcmToken() throws IOException
    {
        final String accessToken = userMockManager.insertTokenGetHeaderStr(luis.getUserName(), luis.getGcmToken());
        assertThat(USER_ENDPOINT.modifyUserGcmToken(accessToken, "GCMtoKen1234X").execute().body(), is(1));
        assertThat(USER_ENDPOINT.getUserData(accessToken).execute().body().getGcmToken(), is("GCMtoKen1234X"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChange() throws IOException, ServiceException
    {
        // Call the controller.
        String newClearPswd = "newPacoPassword";
        assertThat(USER_ENDPOINT.passwordChange(
                userMockManager.insertTokenGetHeaderStr(paco.getUserName(),
                        paco.getGcmToken()),
                newClearPswd).execute().body(),
                is(1));
        // Check.
        assertThat(checkpw(newClearPswd, usuarioManager.getUserDataByName(paco.getUserName()).getPassword()),
                is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordSend() throws MessagingException, IOException, ServiceException
    {
        // Preconditions.
        Usuario usuario = new Usuario.UsuarioBuilder().userName(TO).alias("yo").password("yo_password").build();
        UsuarioComunidad usuarioComunidad =
                new UsuarioComunidad.UserComuBuilder(COMU_LA_PLAZUELA_5, usuario).userComuRest(COMU_PLAZUELA5_JUAN).build();
        assertThat(USERCOMU_ENDPOINT.regComuAndUserAndUserComu(oneComponent_local_ES, usuarioComunidad).execute().body(), is(true));
        // Call the controller.
        assertThat(USER_ENDPOINT.passwordSend(oneComponent_local_ES, usuario.getUserName()).execute().body(), is(true));
        // Cleanup mail folder.
        javaMailMonitor.extTimedCleanUp();
    }

    // ......................... HELPER CLASSES AND METHODS ...............................


    //  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class},
            webEnvironment = RANDOM_PORT)
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class})
    @DirtiesContext
    public static class UsuarioControllerDevTest extends UsuarioControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class},
            webEnvironment = RANDOM_PORT)
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({DbPre.class})
    @DirtiesContext
    public static class UsuarioControllerPreTest extends UsuarioControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            UsuarioRepoConfiguration.class, UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UsuarioControllerAwsTest extends UsuarioControllerTest {
    }
}