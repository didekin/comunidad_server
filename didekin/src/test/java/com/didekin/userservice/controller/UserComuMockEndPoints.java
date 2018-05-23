package com.didekin.userservice.controller;


import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.didekin.userservice.controller.UserComuMockController.regComu_User_UserComu;
import static com.didekin.userservice.controller.UserComuMockController.regUser_UserComu;
import static com.didekin.userservice.controller.UserComuMockController.user_delete;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_PARAM;

/**
 * User: pedro@didekin
 * Date: 20/11/16
 * Time: 13:33
 */
@SuppressWarnings("unused")
public interface UserComuMockEndPoints {

    @POST(regComu_User_UserComu)
    Call<Boolean> regComuAndUserAndUserComu(@Body UsuarioComunidad usuarioCom);

    @POST(regUser_UserComu)
    Call<Boolean> regUserAndUserComu(@Body UsuarioComunidad userCom);

    @FormUrlEncoded
    @POST(user_delete)
    Call<Boolean> deleteUser(@Field(USER_PARAM) String userName);

    @GET("{mock_path}/{mock2_path}")
    Call<String> tryTokenInterceptor(@Header("Authorization") String accessToken,
                                     @Path("mock_path") String mock_path,
                                     @Path("mock2_path") String mock2_path);
}
