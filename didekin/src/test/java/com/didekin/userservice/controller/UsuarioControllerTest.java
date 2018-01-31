package com.didekin.userservice.controller;

import com.didekin.Application;
import com.didekin.common.AwsPre;
import com.didekin.common.DbPre;
import com.didekin.common.repository.EntityException;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.controller.SecurityTestUtils;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.userservice.repository.UsuarioManagerIf;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.http.HttpHandler;
import com.didekinlib.http.auth.AuthEndPoints;
import com.didekinlib.http.auth.SpringOauthToken;
import com.didekinlib.http.usuario.UsuarioEndPoints;
import com.didekinlib.http.usuariocomunidad.UsuarioComunidadEndPoints;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import retrofit2.Response;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_LA_PLAZUELA_5;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_PLAZUELA5_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.getUserData;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPaco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.tokenPedro;
import static com.didekinlib.http.GenericExceptionMsg.BAD_REQUEST;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.auth.AuthClient.CL_USER;
import static com.didekinlib.http.auth.AuthClient.doBearerAccessTkHeader;
import static com.didekinlib.http.auth.AuthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.auth.AuthConstant.REFRESH_TOKEN_GRANT;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
@SuppressWarnings("Duplicates")
public abstract class UsuarioControllerTest {

    private UsuarioEndPoints USER_ENDPOINT;
    private UsuarioComunidadEndPoints USERCOMU_ENDPOINT;
    private AuthEndPoints OAUTH_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;
    @Autowired
    private UsuarioManagerIf usuarioManager;
    @Autowired
    private JavaMailMonitor javaMailMonitor;

    @Before
    public void setUp()
    {
        OAUTH_ENDPOINT = retrofitHandler.getService(AuthEndPoints.class);
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
        USERCOMU_ENDPOINT = retrofitHandler.getService(UsuarioComunidadEndPoints.class);
    }

    //    ==============================  USERSERVICE TESTS ========================================

    /* UserService tests requiring http client. */

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteAccessToken_1() throws EntityException, IOException
    {
        // We test just the SujetosService method.
        assertThat(usuarioManager.deleteAccessToken(tokenPedro(retrofitHandler)), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_deleteAccessTokenByUserName_1() throws Exception
    {
        SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(luis.getUserName(), luis.getPassword()).body();
        assertThat(usuarioManager.deleteAccessTokenByUserName(luis.getUserName()), is(true));
        assertThat(usuarioManager.getAccessToken(token.getValue()), nullValue());
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetAccessToken() throws Exception
    {
        SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), pedro.getPassword()).body();
        assertThat(usuarioManager.getAccessToken(token.getValue()).getValue(), is(token.getValue()));
        assertThat(usuarioManager.getAccessToken(token.getValue()).getRefreshToken().getValue(), is(token.getRefreshToken().getValue()));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_GetAccessTokenByUserName() throws Exception
    {
        // No existe usuario in BD.
        assertThat(usuarioManager.getAccessTokenByUserName("noexisto@no.com").isPresent(), is(false));

        // Existe usuario.
        SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), pedro.getPassword()).body();
        Optional<OAuth2AccessToken> oAuth2AccessToken = usuarioManager.getAccessTokenByUserName(pedro.getUserName());
        assertThat(oAuth2AccessToken.isPresent(), is(true));
        assertThat(oAuth2AccessToken.get().getValue(), is(token.getValue()));
    }

//    ===================================== CONTROLLER TESTS =======================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testDeleteAccessToken() throws IOException
    {
        SpringOauthToken token_1 = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                pedro.getUserName(),
                "password3",
                PASSWORD_GRANT).execute().body();
        boolean isDeleted = USER_ENDPOINT.deleteAccessToken(doBearerAccessTkHeader(token_1), token_1.getValue
                ()).execute().body();
        assertThat(isDeleted, is(true));

        Response<Usuario> response = USER_ENDPOINT.getUserData(doBearerAccessTkHeader(token_1)).execute();
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
    public void testDeleteUser() throws IOException
    {
        List<UsuarioComunidad> comunidades = USERCOMU_ENDPOINT.seeUserComusByUser(tokenPedro(retrofitHandler)).execute().body();
        assertThat(comunidades.size(), is(3));

        boolean isDeleted = USER_ENDPOINT.deleteUser(tokenPedro(retrofitHandler)).execute().body();
        // Borra al usuario y las comunidades previamente encontradas en la consulta.
        assertThat(isDeleted, is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_1() throws IOException
    {
        Usuario usuarioDB = getUserData(luis, USER_ENDPOINT, retrofitHandler);

        assertThat(usuarioDB.getuId(), is(luis.getuId()));
        assertThat(usuarioDB.getUserName(), is(luis.getUserName()));
        assertThat(usuarioDB.getAlias(), is(luis.getAlias()));
        assertThat(usuarioDB.getPassword(), is(nullValue()));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserData_2() throws IOException
    {
        // We send an invailid token.
        Response<Usuario> response = USER_ENDPOINT.getUserData(
                doBearerAccessTkHeader(
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

        String userNameOk = pedro.getUserName();
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

        assertThat(USER_ENDPOINT.modifyUser(oneComponent_local_ES, tokenPaco(retrofitHandler), usuarioIn_1).execute().body(), is(1));
        // Check that access token has been deleted. We try with both userNames.
        assertThat(usuarioManager.getAccessTokenByUserName(paco.getUserName()).isPresent(), is(false));
        assertThat(usuarioManager.getAccessTokenByUserName("new_paco@new.com").isPresent(), is(false));
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

        assertThat(USER_ENDPOINT.modifyUser(oneComponent_local_ES, tokenPaco(retrofitHandler), usuarioIn_1).execute().body(), is(1));
        // Check wiht usuarioManager that access token has not been deleted.
        assertThat(usuarioManager.getAccessTokenByUserName(paco.getUserName()).isPresent(), is(true));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testModifyUserGcmToken() throws IOException
    {
        String tokenLuis = new SecurityTestUtils(retrofitHandler).doAuthHeaderFromRemoteToken(luis.getUserName(), luis.getPassword());
        assertThat(USER_ENDPOINT.modifyUserGcmToken(tokenLuis, "GCMtoKen1234X").execute().body(),
                is(1));
        assertThat(USER_ENDPOINT.getGcmToken(tokenLuis).execute().body().getToken(), is("GCMtoKen1234X"));
    }

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordChange() throws IOException, EntityException
    {
        // Preconditions: user is registered with an access token.
        SpringOauthToken accessToken = getTokenAndCheckDb(paco.getUserName(), paco.getPassword());
        assertThat(usuarioManager.getAccessTokenByUserName(paco.getUserName()).isPresent(), is(true));
        // Call the controller.
        String newClearPswd = "newPacoPassword";
        assertThat(USER_ENDPOINT.passwordChange(doBearerAccessTkHeader(accessToken), newClearPswd).execute().body(),
                is(1));
        // Check.
        assertThat(new BCryptPasswordEncoder()
                        .matches(newClearPswd, usuarioManager.getUserByUserName(paco.getUserName()).getPassword()),
                is(true));
        // Check for deletion of oauth token.
        assertThat(usuarioManager.getAccessTokenByUserName(paco.getUserName()).isPresent(), is(false));
    }

    @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testPasswordSend() throws MessagingException, IOException, EntityException
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

    // ......................... HELPER CLASSES AND METHODS ...............................

    private SpringOauthToken getTokenAndCheckDb(String userName, String password) throws IOException
    {
        SpringOauthToken token_1 = OAUTH_ENDPOINT.getPasswordUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER),
                userName,
                password,
                PASSWORD_GRANT).execute().body();
        assertThat(usuarioManager.getAccessTokenByUserName(userName).isPresent(), is(true));
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
            UsuarioRepoConfiguration.class, UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_PRE, MAIL_PRE})
    @Category({AwsPre.class})
    public static class UsuarioControllerAwsTest extends UsuarioControllerTest {
    }
}