package com.didekin.userservice.gcm;

import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.gcm.retrofit.GcmEndPointImp;
import com.didekinlib.gcm.retrofit.GcmRetrofitHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 */
@Configuration
@Import(UsuarioRepoConfiguration.class)
public class GcmConfiguration {

    // Firebase gcm URL.
    private static final String FCM_HOST_PORT = "https://fcm.googleapis.com";

    @Autowired
    private UsuarioServiceIf usuarioService;

    @Bean
    public GcmRetrofitHandler gcmRetrofitHandler()
    {
        return new GcmRetrofitHandler(FCM_HOST_PORT, 30);
    }

    @Bean
    public GcmEndPointImp gcmEndPointImp()
    {
        return new GcmEndPointImp(gcmRetrofitHandler());
    }

    @Bean
    public GcmUserComuServiceIf gcmUserComuServiceIf()
    {
        return new GcmUserComuService(gcmEndPointImp(), usuarioService);
    }
}
