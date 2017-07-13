package com.didekin.incidservice.repository;

import com.didekin.common.repository.RepositoryConfig;

import org.springframework.beans.factory.annotation.Autowired;
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
@Import(value = {RepositoryConfig.class})
public class IncidenciaRepoConfiguration {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    public IncidenciaDao incidenciaDao(JdbcTemplate jdbcTemplate)
    {
        return new IncidenciaDao(jdbcTemplate);
    }

    @Bean
    public IncidenciaRepoServiceIf incidenciaRepoService(IncidenciaDao incidenciaDao)
    {
        return new IncidenciaRepoService(incidenciaDao);
    }
}
