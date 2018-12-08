package com.didekin.incidservice.repository;

import com.didekin.common.repository.RepositoryConfig;
import com.didekin.userservice.auth.TkProducerConfig;
import com.didekin.userservice.gcm.GcmConfiguration;
import com.didekin.userservice.repository.UsuarioManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 */
@Configuration
@Import(value = {RepositoryConfig.class, GcmConfiguration.class, TkProducerConfig.class})
public class IncidenciaManagerConfiguration {

    @Bean
    public IncidenciaDao incidenciaDao(JdbcTemplate jdbcTemplate)
    {
        return new IncidenciaDao(jdbcTemplate);
    }

    @Bean
    public UserManagerConnector userManagerConnector(UsuarioManager usuarioManager)
    {
        return new UserManagerConnector(usuarioManager);
    }

    /*@Bean
    public IncidenciaManager incidenciaManager(IncidenciaDao incidenciaDao)
    {
        return new IncidenciaManager(incidenciaDao);
    }*/
}
