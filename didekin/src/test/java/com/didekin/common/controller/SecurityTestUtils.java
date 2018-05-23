package com.didekin.common.controller;

import com.didekinlib.http.HttpHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Base64;

import retrofit2.Response;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:46
 */
@ContextConfiguration(classes = {RetrofitConfigurationDev.class, RetrofitConfigurationPre.class})
public final class SecurityTestUtils {

    private HttpHandler retrofitHandler;

    // TODO: descomentar y revisar.

    @Autowired
    public SecurityTestUtils(HttpHandler retrofitHandler){
        this.retrofitHandler = retrofitHandler;
    }

    /*public Response<SpringOauthToken> getPasswordUserToken(String userName, String password) throws IOException
    {
        AuthEndPoints endPoints = retrofitHandler.getService(AuthEndPoints.class);
        return endPoints.getPasswordUserToken(
                doAuthBasicHeader(CL_USER),
                userName,
                password,
                PASSWORD_GRANT).execute();
    }

    public String doAuthHeaderFromRemoteToken(String userName, String password) throws IOException
    {
        Response<SpringOauthToken> response = getPasswordUserToken(userName, password);

        if (response.isSuccessful()){
            return doBearerAccessTkHeader(response.body());
        } else {
            return null;
        }
    }

    public String doAuthBasicHeader(final AuthClient cliente)
    {
        final String baseString = cliente.getId() + ":" + cliente.getSecret();

        String base64AuthData = Base64.getEncoder().encodeToString(baseString.getBytes());
        return BASIC_AND_SPACE + base64AuthData;
    }*/
}
