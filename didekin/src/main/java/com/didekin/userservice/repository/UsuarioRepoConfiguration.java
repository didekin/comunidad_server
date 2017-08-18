package com.didekin.userservice.repository;

import com.didekin.common.repository.RepositoryConfig;
import com.didekin.userservice.mail.UsuarioMailConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 */
@Configuration
@Import(value={RepositoryConfig.class, UsuarioMailConfiguration.class})
public class UsuarioRepoConfiguration {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public JdbcTokenStore tokenStore()
    {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public MunicipioDao municipioDao(JdbcTemplate jdbcTemplate)
    {
        return new MunicipioDao(jdbcTemplate);
    }

    @Bean
    public UsuarioDao usuarioDao(JdbcTemplate jdbcTemplate)
    {
        return new UsuarioDao(jdbcTemplate);
    }

    @Bean
    public ComunidadDao comunidadDao(JdbcTemplate jdbcTemplate)
    {
        return new ComunidadDao(jdbcTemplate);
    }

    @Bean
    public UsuarioServiceIf sujetosService()
    {
        return new UsuarioService(comunidadDao(jdbcTemplate), usuarioDao(jdbcTemplate));
    }
}
