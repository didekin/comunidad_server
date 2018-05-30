package com.didekin.userservice.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.springprofile.Profiles.checkActiveProfiles;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 */
@Configuration
@Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL})
@Import(value = {UsuarioRepoConfiguration.class})
public class UserMockRepoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(UserMockRepoConfiguration.class.getName());

    @Autowired
    Environment env;

    @Bean
    public UserMockManager userMockManager(UsuarioManager usuarioManager)
    {
        logger.debug("userMockManager()");
        checkActiveProfiles(env);
        return new UserMockManager(usuarioManager);
    }
}
