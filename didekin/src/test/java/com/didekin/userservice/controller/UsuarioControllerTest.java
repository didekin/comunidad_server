package com.didekin.userservice.controller;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.EntityException;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.userservice.repository.ServOneRepoConfiguration;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.http.oauth2.SpringOauthToken;
import com.didekinlib.http.retrofit.Oauth2EndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;
import com.didekinlib.http.retrofit.UsuarioComunidadEndPoints;
import com.didekinlib.http.retrofit.UsuarioEndPoints;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import retrofit2.Response;

import static com.didekin.common.Profiles.MAIL_PRE;
import static com.didekin.common.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_PLAZUELA5_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.getUserData;
import static com.didekin.userservice.testutils.UsuarioTestUtils.insertUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPedro;
import static com.didekinlib.http.GenericExceptionMsg.BAD_REQUEST;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthConstant.REFRESH_TOKEN_GRANT;
import static com.didekinlib.http.oauth2.OauthTokenHelper.HELPER;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
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
    private Oauth2EndPoints OAUTH_ENDPOINT;

    @Autowired
    private RetrofitHandler retrofitHandler;
    @Autowired
    private UsuarioServiceIf sujetosService;
    @Autowired
    private JavaMailMonitor javaMailMonitor;

    @Before
    public void setUp() throws Exception
    {
        OAUTH_ENDPOINT = retrofitHandler.getService(Oauth2EndPoints.class);
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

    @After
    public void clear()
    {
    }

    //    ==============================  USERSERVICE TESTS ========================================
          /* UserService tests requiring http client. */

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void deleteAccessTokenByUserName_1() throws Exception
    {
        SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(luis.getUserName(), luis.getPassword()).body();
        assertThat(sujetosService.deleteAccessTokenByUserName(luis.getUserName()), is(true));
        assertThat(sujetosService.getAccessToken(token.getValue()), nullValue());
    }

//    ===================================== CONTROLLER TESTS =======================================

    @Test
    public void testBefore()
    {
        assertThat(sujetosService, notNullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteAccessToken_1() throws EntityException, IOException
    {
        // We test just the SujetosService method.
        assertThat(sujetosService.deleteAccessToken(tokenPedro(retrofitHandler)), is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteAccessToken_2() throws IOException
    {
        SpringOauthToken token_1 = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                "pedro@pedro.com",
                "password3",
                PASSWORD_GRANT).execute().body();
        boolean isDeleted = USER_ENDPOINT.deleteAccessToken(HELPER.doBearerAccessTkHeader(token_1), token_1.getValue
                ()).execute().body();
        assertThat(isDeleted, is(true));

        Response<Usuario> response = USER_ENDPOINT.getUserData(HELPER.doBearerAccessTkHeader(token_1)).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));

        Response<SpringOauthToken> responseB = OAUTH_ENDPOINT.getRefreshUserToken(
                new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                token_1.getRefreshToken().getValue(),
                REFRESH_TOKEN_GRANT
        ).execute();
        assertThat(responseB.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(responseB).getMessage(), is(BAD_REQUEST.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test()
    public void testDeleteUser_1() throws IOException
    {
        List<UsuarioComunidad> comunidades = USERCOMU_ENDPOINT.seeUserComusByUser(tokenPedro(retrofitHandler)).execute().body();
        assertThat(comunidades.size(), is(3));

        boolean isDeleted = USER_ENDPOINT.deleteUser(tokenPedro(retrofitHandler)).execute().body();
        // Borra al usuario y las comunidades previamente encontradas en la consulta.
        assertThat(isDeleted, is(true));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_1() throws IOException
    {
        insertUsuarioComunidad(COMU_REAL_JUAN, USERCOMU_ENDPOINT, USER_ENDPOINT, retrofitHandler);
        Usuario usuarioDB = getUserData(USER_JUAN, USER_ENDPOINT, retrofitHandler);

        assertThat(usuarioDB.getuId() > 0, is(true));
        assertThat(usuarioDB.getUserName(), is("juan@juan.us"));
        assertThat(usuarioDB.getAlias(), is("juan"));
        assertThat(usuarioDB.getPassword(), nullValue());

    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_2() throws IOException
    {
        // We send an invailid token.
        Response<Usuario> response = USER_ENDPOINT.getUserData(
                HELPER.doBearerAccessTkHeader(
                        new SpringOauthToken("fake_token", null, null, new SpringOauthToken.OauthToken("faked_token_refresh", null), null)
                )).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_1() throws IOException
    {

        String userNameOk = "pedro@pedro.com";
        String userNameWrong = "pedro@wrong.com";
        String passwordOk = "password3";
        String passwordWrong = "passwordWrong";

        assertThat(USER_ENDPOINT.login(userNameOk, passwordOk).execute().body(), is(true));
        assertThat(USER_ENDPOINT.login(userNameOk, passwordWrong).execute().body(), is(false));

        Response<Boolean> response = USER_ENDPOINT.login(userNameWrong, passwordOk).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USER_NAME_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLogin_2() throws IOException
    {
        String userNameWrong = "user@notfound.com";
        String passwordWrong = "password_ok";

        Response<Boolean> response = USER_ENDPOINT.login(userNameWrong, passwordWrong).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(USER_NAME_NOT_FOUND.getHttpMessage()));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_1() throws IOException
    {
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_JUAN).execute().body();
        SpringOauthToken token_1 = getTokenAndCheckDb(USER_JUAN.getUserName(), USER_JUAN.getPassword());
        Usuario usuarioDb_1 = USER_ENDPOINT.getUserData(HELPER.doBearerAccessTkHeader(token_1)).execute().body();

        // Change userName.
        Usuario usuarioIn_1 = new Usuario.UsuarioBuilder()
                .userName("new_juan@new.com")
                .uId(usuarioDb_1.getuId())
                .build();

        assertThat(USER_ENDPOINT.modifyUser(HELPER.doBearerAccessTkHeader(token_1), usuarioIn_1).execute().body(), is(1));
        // Check wiht usuarioService that access token has been deleted. We try with both userNames.
        assertThat(sujetosService.getAccessTokenByUserName("new_juan@new.com").isPresent(), is(false));
        assertThat(sujetosService.getAccessTokenByUserName(USER_JUAN.getUserName()).isPresent(), is(false));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_2() throws IOException
    {
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_JUAN).execute().body();
        SpringOauthToken token_1 =
                getTokenAndCheckDb(USER_JUAN.getUserName(), USER_JUAN.getPassword());
        Usuario usuarioDb_1 = USER_ENDPOINT.getUserData(HELPER.doBearerAccessTkHeader(token_1)).execute().body();

        // Change alias.
        Usuario usuarioIn_1 = new Usuario.UsuarioBuilder()
                .alias(usuarioDb_1.getAlias())
                .uId(usuarioDb_1.getuId())
                .build();

        assertThat(USER_ENDPOINT.modifyUser(HELPER.doBearerAccessTkHeader(token_1), usuarioIn_1).execute().body(), is(1));
        // Check wiht usuarioService that access token has not been deleted.
        assertThat(sujetosService.getAccessTokenByUserName(USER_JUAN.getUserName()).isPresent(), is(true));
        // Authentication by refreshToken continues to work.
        assertThat(
                OAUTH_ENDPOINT.getRefreshUserToken(new SecurityTestUtils(retrofitHandler)
                        .doAuthBasicHeader(CL_USER), token_1.getRefreshToken().getValue(), REFRESH_TOKEN_GRANT)
                        .execute().body(), notNullValue()
        );
    }

    /**
     * We test how change of userName invalidates accessToken.
     */
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUser_3() throws IOException, EntityException
    {
        USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_JUAN).execute().body();
        SpringOauthToken token_1 = getTokenAndCheckDb(USER_JUAN.getUserName(), USER_JUAN.getPassword());
        // Change userName with usuarioDao.
        Usuario usuarioIn_1 = new Usuario.UsuarioBuilder()
                .copyUsuario(sujetosService.getUserByUserName(USER_JUAN.getUserName()))
                .userName("new_juan@new.com")
                .build();
        assertThat(sujetosService.getUsuarioDao().modifyUser(usuarioIn_1), is(1));

        // Check accessToken has not been deleted.
        assertThat(sujetosService.getAccessTokenByUserName("new_juan@new.com").isPresent(), is(true));
        // But authentication by refreshToken throws an exception.
        Response<SpringOauthToken> response = OAUTH_ENDPOINT.getRefreshUserToken(
                new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER), token_1.getRefreshToken().getValue(), REFRESH_TOKEN_GRANT).execute();
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED.getHttpMessage()));  // USER_NAME_NOT_FOUND.
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserGcmToken() throws IOException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().uId(5L).userName("luis@luis.com").password("password5").build();
        String tokenLuis = new SecurityTestUtils(retrofitHandler).getBearerAccessTokenHeader(usuario.getUserName(), usuario.getPassword());
        assertThat(USER_ENDPOINT.modifyUserGcmToken(tokenLuis, "GCMtoKen1234X").execute().body(),
                is(1));
        assertThat(USER_ENDPOINT.getGcmToken(tokenLuis).execute().body().getToken(), is("GCMtoKen1234X"));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChange_1() throws IOException, InterruptedException, EntityException
    {
        // Preconditions: user is registered with an access token.
        assertThat(USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_REAL_JUAN).execute().body(), is(true));
        SpringOauthToken accessToken = getTokenAndCheckDb(USER_JUAN.getUserName(), USER_JUAN.getPassword());
        assertThat(sujetosService.getAccessTokenByUserName(USER_JUAN.getUserName()).isPresent(), is(true));

        // Call the controller.
        String newClearPswd = "new_luis_password";
        assertThat(USER_ENDPOINT.passwordChange(HELPER.doBearerAccessTkHeader(accessToken), newClearPswd).execute().body(),
                is(1));

        // Check.
        assertThat(new BCryptPasswordEncoder()
                        .matches(newClearPswd, sujetosService.getUserByUserName(USER_JUAN.getUserName()).getPassword()),
                is(true));
        // Check for deletion of oauth token.
        assertThat(sujetosService.getAccessTokenByUserName(USER_JUAN.getUserName()).isPresent(), CoreMatchers.is(false));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordSend_1() throws MessagingException, IOException, EntityException, InterruptedException
    {
        javaMailMonitor.expungeFolder();
        final String oldPassword = "yo_password";

        // Preconditions.
        Usuario usuario = new Usuario.UsuarioBuilder().userName(TO).alias("yo").password(oldPassword).build();
        UsuarioComunidad usuarioComunidad =
                new UsuarioComunidad.UserComuBuilder(COMU_LA_PLAZUELA_5, usuario).userComuRest(COMU_PLAZUELA5_JUAN).build();
        assertThat(USERCOMU_ENDPOINT.regComuAndUserAndUserComu(usuarioComunidad).execute().body(), is(true));

        // Call the controller.
        assertThat(USER_ENDPOINT.passwordSend(usuario.getUserName()).execute().body(), is(true));

        // Check mail.
        Thread.sleep(9000);
        javaMailMonitor.checkPasswordMessage(usuario.getAlias(), null);
        // Login data have changed
        assertThat(sujetosService.login(usuario), is(false));
        // Cleaning and closing.
        javaMailMonitor.closeStoreAndFolder();
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordSend_2() throws MessagingException, IOException, EntityException, InterruptedException
    {
        // Preconditions.
        assertThat(USERCOMU_ENDPOINT.regComuAndUserAndUserComu(COMU_PLAZUELA5_JUAN).execute().body(), is(true));
        getTokenAndCheckDb(USER_JUAN.getUserName(), USER_JUAN.getPassword());
        // Invalid email.
        Response<Boolean> isPswdSent = USER_ENDPOINT.passwordSend(USER_JUAN.getUserName()).execute();
        assertThat(retrofitHandler.getErrorBean(isPswdSent).getMessage(), is(PASSWORD_NOT_SENT.getHttpMessage()));
        // Login data haven't changed
        assertThat(sujetosService.login(USER_JUAN), is(true));
    }

    // ......................... TESTS OF HELPER METHODS ..................................

    @Test
    public void testEncryptedPsw()
    {
        String password = "password11";
        String encodePsw = new BCryptPasswordEncoder().encode(password);
        assertThat(new BCryptPasswordEncoder().matches(password, encodePsw), is(true));
        String encodePswBis = new BCryptPasswordEncoder().encode(password);
        assertThat(encodePsw.equals(encodePswBis), is(false));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testSignUp() throws IOException, EntityException
    {
        Usuario usuarioDB = insertUsuarioComunidad(COMU_REAL_JUAN, USERCOMU_ENDPOINT, USER_ENDPOINT, retrofitHandler);

        assertThat(usuarioDB.getuId() > 0, is(true));
        assertThat(usuarioDB.getUserName(), is(USER_JUAN.getUserName()));
        assertThat(usuarioDB.getAlias(), is(USER_JUAN.getAlias()));
        assertThat(new BCryptPasswordEncoder().matches(
                USER_JUAN.getPassword(),
                sujetosService.getUserByUserName(USER_JUAN.getUserName()).getPassword()),
                is(true));
    }

    // ......................... HELPER CLASSES AND METHODS ...............................

    private SpringOauthToken getTokenAndCheckDb(String userName, String password) throws IOException
    {
        SpringOauthToken token_1 = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                userName,
                password,
                PASSWORD_GRANT).execute().body();
        assertThat(sujetosService.getAccessTokenByUserName(userName).isPresent(), is(true));
        return token_1;
    }

    //  ==============================================  INNER CLASSES =============================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UsuarioControllerDevTest extends UsuarioControllerTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({DbPre.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UsuarioControllerPreTest extends UsuarioControllerTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {RetrofitConfigurationPre.class,
            ServOneRepoConfiguration.class, UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UsuarioControllerAwsTest extends UsuarioControllerTest {
    }
}