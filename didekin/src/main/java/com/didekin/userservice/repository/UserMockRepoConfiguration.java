package com.didekin.userservice.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static com.didekin.common.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.Profiles.NGINX_JETTY_PRE;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 * <p>
 * UsuarioMailConfiguration is used by UsuarioService to send emails.
 */
@Configuration
@Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL})
@Import(value = {UsuarioRepoConfiguration.class})
public class UserMockRepoConfiguration {

    @Autowired
    private ComunidadDao comunidadDao;
    @Autowired
    private UsuarioDao usuarioDao;

    @Bean
    public UserMockManager userMockManager()
    {
        return new UserMockManager(comunidadDao, usuarioDao);
    }
}
