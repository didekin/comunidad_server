package com.didekin.userservice.security;

import com.didekinlib.http.HttpHandler;
import com.didekinlib.http.usuario.UsuarioEndPoints;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static com.didekin.userservice.repository.UsuarioManager.BCRYPT_SALT;
import static org.mindrot.jbcrypt.BCrypt.hashpw;

/**
 * User: pedro@didekin
 * Date: 05/05/16
 * Time: 16:09
 */
public abstract class OauthConfigTest {

    private UsuarioEndPoints USER_ENDPOINT;

    @Autowired
    private HttpHandler retrofitHandler;

    @Before
    public void setUp()
    {
        USER_ENDPOINT = retrofitHandler.getService(UsuarioEndPoints.class);
    }

    @SuppressWarnings("unused")
    public void printEncriptPswd()
    {
        String ps1 = "password3";
        String ps2 = "password5";
        String ps3 = "password7";
        System.out.println("One: " + hashpw(ps1, BCRYPT_SALT));
        System.out.println("Two: " + hashpw(ps2, BCRYPT_SALT));
        System.out.println("Three: " + hashpw(ps3, BCRYPT_SALT));
    }

    @Test
    public void testDoAuthBasicHeader()     // TODO: descomentar y revisar.
    {
        /*String encodedHeader = new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER);
        assertThat(encodedHeader, equalTo("Basic dXNlcjo="));*/
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetToken_1() throws IOException  // TODO: descomentar y revisar.
    {
        /*SpringOauthToken tokenResp = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        assertThat(tokenResp, notNullValue());
        assertThat(tokenResp.getTokenType(), is("bearer"));

        // Refresh token.
        long refreshTkValiditySeconds =
                (tokenResp.getRefreshToken().getExpiration().getTime() - new Date().getTime()) / 1000;
        long diff = abs(refreshTkValiditySeconds - REFRESHTK_VALIDITY_SECONDS);
        assertThat(diff < 60, is(true));*/
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetToken_2() throws IOException   // TODO: descomentar y revisar.
    {
        // This is a reiterative test of keeping the same token while userName and password don't change.

        /*SpringOauthToken token_1 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        String accessToken_1 = token_1.getValue();
        String refreshToken_1 = token_1.getRefreshToken().getValue();

        SpringOauthToken token_2 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        String accessToken_2 = token_2.getValue();
        String refreshToken_2 = token_2.getRefreshToken().getValue();
        assertThat(accessToken_2, is(accessToken_1));
        assertThat(refreshToken_2, is(refreshToken_1));

        SpringOauthToken token_3 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        String accessToken_3 = token_3.getValue();
        String refreshToken_3 = token_3.getRefreshToken().getValue();
        assertThat(accessToken_3, is(accessToken_1));
        assertThat(refreshToken_3, is(refreshToken_1));*/
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetTokenNoRoles() throws IOException   // TODO: descomentar y revisar.
    {
        // Si el usuario no tiene ningún rol en la tabla comunidad_usuario, devuelve una excepción.
        /*Response<SpringOauthToken> response = new SecurityTestUtils(retrofitHandler).getPasswordUserToken("juan@noauth.com", "password7");
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(BAD_REQUEST.getHttpMessage()));*/
    }

    @Test
    public void testGetTokenUserNotExist() throws IOException     // TODO: descomentar y revisar.
    {
        // No user in BD.
        /*Response<SpringOauthToken> response = new SecurityTestUtils(retrofitHandler).getPasswordUserToken("noexisto@noexisto.com", "passwordNo");
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(BAD_REQUEST.getHttpMessage()));*/
    }

    @Test
    public void testNotFound() throws IOException   // TODO: descomentar y revisar.
    {
        /*Response<ErrorBean> response = OAUTH_ENDPOINT.getNotFoundMsg().execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(NOT_FOUND.getHttpMessage()));*/
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRefreshTokenGrant_1() throws IOException    // TODO: descomentar y revisar.
    {
        /*SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        String accessToken1 = token.getValue();
        String refreshToken1 = token.getRefreshToken().getValue();

        SpringOauthToken newToken =
                OAUTH_ENDPOINT.getRefreshUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER), refreshToken1, REFRESH_TOKEN_GRANT).execute().body();
        String newAccessTk = newToken.getValue();
        String newRefreshTk = newToken.getRefreshToken().getValue();
        assertThat(newAccessTk != null && newRefreshTk != null, is(true));
        // AccesToken is different.
        assertThat(newAccessTk, not(accessToken1));
        // The current mail in OauthConfig is for not reusing the refresh token.
        assertThat(newRefreshTk, not(is(refreshToken1)));*/
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRefreshTokenGrant_2() throws IOException     // TODO: descomentar y revisar.
    {
        /*SpringOauthToken token = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        String refreshToken1 = token.getRefreshToken().getValue();

        // We manipulate the refreshToken.
        String refreshFakeTk = refreshToken1.concat("fake");
        Response<SpringOauthToken> response = OAUTH_ENDPOINT.getRefreshUserToken(new SecurityTestUtils(retrofitHandler).doAuthBasicHeader(CL_USER), refreshFakeTk, REFRESH_TOKEN_GRANT).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(BAD_REQUEST.getHttpMessage()));*/
    }

    /**
     * If we try two times to get an access token on password credentials, the refresh token in table becomes null, and the
     * retrieval of an access token based on refresh token credential fails.
     */
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRefreshTokenGrant_3() throws IOException    // TODO: descomentar y revisar.
    {
        /*SpringOauthToken token_1 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        assertThat(token_1.getRefreshToken().getValue(), notNullValue());

        SpringOauthToken token_2 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        assertThat(token_2.getRefreshToken().getValue(), notNullValue());

        Response<SpringOauthToken> response = OAUTH_ENDPOINT.getRefreshUserToken(new SecurityTestUtils(retrofitHandler)
                .doAuthBasicHeader(CL_USER), token_2.getRefreshToken().getValue(), REFRESH_TOKEN_GRANT).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(BAD_REQUEST.getHttpMessage()));*/
    }


    /**
     * We test the obvious: if we delete first of all the token in the database, the error pattern of the previous test doesn't apply.
     */
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRefreshTokenGrant_4() throws IOException    // TODO: descomentar y revisar.
    {
        /*SpringOauthToken token_1 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        assertThat(token_1.getRefreshToken().getValue(), notNullValue());

        assertThat(USER_ENDPOINT.deleteAccessToken(doBearerAccessTkHeader(token_1), token_1.getValue
                ()).execute().body(), is(true));

        SpringOauthToken token_2 = new SecurityTestUtils(retrofitHandler).getPasswordUserToken(pedro.getUserName(), "password3").body();
        assertThat(token_2.getRefreshToken().getValue(), notNullValue());

        SpringOauthToken token_3 = OAUTH_ENDPOINT.getRefreshUserToken(new SecurityTestUtils(retrofitHandler)
                .doAuthBasicHeader(CL_USER), token_2.getRefreshToken().getValue(), REFRESH_TOKEN_GRANT).execute().body();
        assertThat(token_3.getRefreshToken(), notNullValue());*/
    }
}
