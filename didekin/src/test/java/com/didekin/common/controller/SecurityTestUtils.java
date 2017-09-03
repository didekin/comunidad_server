package com.didekin.common.controller;

import com.didekinlib.http.oauth2.OauthClient;
import com.didekinlib.http.oauth2.SpringOauthToken;
import com.didekinlib.http.retrofit.Oauth2EndPoints;
import com.didekinlib.http.retrofit.RetrofitHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Base64;

import retrofit2.Response;

import static com.didekinlib.http.oauth2.OauthClient.CL_USER;
import static com.didekinlib.http.oauth2.OauthConstant.PASSWORD_GRANT;
import static com.didekinlib.http.oauth2.OauthTokenHelper.BASIC_AND_SPACE;
import static com.didekinlib.http.oauth2.OauthTokenHelper.HELPER;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:46
 */
@ContextConfiguration(classes = {RetrofitConfigurationDev.class, RetrofitConfigurationPre.class})
public final class SecurityTestUtils {

    private RetrofitHandler retrofitHandler;

    @Autowired
    public SecurityTestUtils(RetrofitHandler retrofitHandler){
        this.retrofitHandler = retrofitHandler;
    }

    public Response<SpringOauthToken> getPasswordUserToken(String userName, String password) throws IOException
    {
        Oauth2EndPoints endPoints = retrofitHandler.getService(Oauth2EndPoints.class);
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
            return HELPER.doBearerAccessTkHeader(response.body());
        } else {
            return null;
        }
    }

    public String doAuthBasicHeader(final OauthClient cliente)
    {
        final String baseString = cliente.getId() + ":" + cliente.getSecret();

        String base64AuthData = Base64.getEncoder().encodeToString(baseString.getBytes());
        return BASIC_AND_SPACE + base64AuthData;
    }
}
