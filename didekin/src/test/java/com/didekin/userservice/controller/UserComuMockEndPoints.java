package com.didekin.userservice.controller;


import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import static com.didekin.userservice.controller.UserComuMockController.regComu_User_UserComu;
import static com.didekin.userservice.controller.UserComuMockController.regUser_UserComu;
import static com.didekinlib.http.CommonServConstant.ACCEPT_LANGUAGE;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_COMU_AND_USER_AND_USERCOMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_USER_USERCOMU;

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
}
