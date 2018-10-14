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
import com.didekin.userservice.repository.UserMockRepoConfiguration;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.http.retrofit.HttpHandler;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuario.http.UsuarioEndPoints;
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
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.PASSWORD_WRONG;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.model.usuario.http.UsuarioExceptionMsg.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mindrot.jbcrypt.BCrypt.checkpw;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
@SuppressWarnings("ConstantConditions")
public abstract class UsuarioControllerTest {

    private UsuarioEndPoints USER_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private UsuarioManager usuarioManager;
    @Autowired
    private JavaMailMonitor javaMailMonitor;

    @Before
    public void setUp()
    {
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
    }

//    ===================================== CONTROLLER TESTS =======================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUser()
    {
        final String authHeader = userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName());
        assertThat(authHeader, notNullValue());
        USER_ENDPOINT.deleteUser(authHeader).map(Response::body).test().assertResult(true);
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_1()
    {
        USER_ENDPOINT.getUserData(userMockManager.insertAuthTkGetNewAuthTkStr(luis.getUserName()))
                .map(Response::body)
                .test().assertOf(testObserver -> {
                    Usuario usuarioDB = testObserver.values().get(0);
                    assertThat(usuarioDB.getuId(), is(luis.getuId()));
                    assertThat(usuarioDB.getUserName(), is(luis.getUserName()));
                    assertThat(usuarioDB.getAlias(), is(luis.getAlias()));
                    assertThat(usuarioDB.getGcmToken(), is(luis.getGcmToken()));
                    assertThat(usuarioDB.getTokenAuth(), nullValue());
                    assertThat(usuarioDB.getPassword(), nullValue());
                }
        );
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_2() throws IOException
    {
        // We send an invailid token.
        Response<Usuario> response = USER_ENDPOINT.getUserData("faked_token").test().values().get(0);
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(TOKEN_ENCRYP_DECRYP_ERROR.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_1()
    {
        // Preconditions:
        Usuario usuario1 = usuarioManager.getUserData(luis.getUserName());
        // Exec 1
        String tokenAuth_2 = USER_ENDPOINT.login(pedro.getUserName(), "password5", "gcm_token_login1").blockingGet().body();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(tokenAuth_2), is(true));
        Usuario usuario2 = usuarioManager.getUserData(luis.getUserName());
        assertThat(checkpw(tokenAuth_2, usuario2.getTokenAuth()), is(true));
        assertThat(usuario2.getTokenAuth().equals(usuario1.getTokenAuth()), is(false));
        assertThat(usuario2.getGcmToken().equals(usuario1.getGcmToken()), is(false));
        // Exec 2.
        String tokenAuth_3 = USER_ENDPOINT.login(pedro.getUserName(), "password5", "gcm_token_login2").blockingGet().body();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(tokenAuth_3), is(true));
        assertThat(tokenAuth_3.equals(tokenAuth_2), is(false));
        Usuario usuario3 = usuarioManager.getUserData(luis.getUserName());
        assertThat(checkpw(tokenAuth_3, usuario3.getTokenAuth()), is(true));
        assertThat(usuario3.getTokenAuth().equals(usuario2.getTokenAuth()), is(false));
        assertThat(usuario3.getGcmToken().equals(usuario2.getGcmToken()), is(false));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_2()
    {
        String userNameOk = pedro.getUserName();
        String userNameWrong = "pedro@wrong.com";
        String passwordWrong = "passwordWrong";

        USER_ENDPOINT.login(userNameOk, passwordWrong, pedro.getGcmToken())
                .test()
                .assertValue(response -> retrofitHandler.getErrorBean(response).getMessage().equals(PASSWORD_WRONG.getHttpMessage()));
        USER_ENDPOINT.login(userNameWrong, passwordWrong, pedro.getGcmToken()).test()
                .assertValue(response -> retrofitHandler.getErrorBean(response).getMessage().equals(USER_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_1()
    {
        // Change userName.
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .userName("new_paco@new.com")
                .uId(paco.getuId())
                .build();
        USER_ENDPOINT.modifyUser(oneComponent_local_ES, userMockManager.insertAuthTkGetNewAuthTkStr(paco.getUserName()), usuarioIn)
                .map(Response::body).test().assertValue(1);
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2()
    {
        // Change alias.
        Usuario usuarioIn = new Usuario.UsuarioBuilder()
                .copyUsuario(paco)
                .alias("newAlias")
                .build();
        USER_ENDPOINT.modifyUser(oneComponent_local_ES, userMockManager.insertAuthTkGetNewAuthTkStr(paco.getUserName()), usuarioIn)
                .map(Response::body).test().assertValue(1);
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChange() throws ServiceException
    {
        // Call the controller.
        final String accessTk = userMockManager.insertAuthTkGetNewAuthTkStr(paco.getUserName());
        USER_ENDPOINT.passwordChange(accessTk, paco.getPassword(), "newPacoPassword")
                .test()
                .assertValue(response -> tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(response.body()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordSend() throws MessagingException, ServiceException
    {
        // Preconditions.
        Usuario usuario = new Usuario.UsuarioBuilder().userName(TO).alias("yo").password("yo_password").build();
        UsuarioComunidad usuarioComunidad =
                new UsuarioComunidad.UserComuBuilder(COMU_LA_PLAZUELA_5, usuario).userComuRest(COMU_PLAZUELA5_JUAN).build();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(userMockManager.regComuAndUserAndUserComu(usuarioComunidad)), is(true));
        // Call the controller.
        USER_ENDPOINT.passwordSend(oneComponent_local_ES, usuario.getUserName())
                .map(Response::body)
                .test()
                .assertValue(true);
        // Cleanup mail folder.
        javaMailMonitor.extTimedCleanUp();
    }

    // ......................... HELPER CLASSES AND METHODS ...............................


    //  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class, DbPre.class})
    @DirtiesContext
    public static class UsuarioCtrlerDevTest extends UsuarioControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringBootTest(classes = {RetrofitConfigurationPre.class,
            UserMockRepoConfiguration.class, UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UsuarioCtrlerAwsTest extends UsuarioControllerTest {
    }
}