package com.didekin.userservice.repository;

import com.didekin.common.repository.RepositoryConfig;
import com.didekin.userservice.mail.UsuarioMailConfiguration;
import com.didekin.userservice.mail.UsuarioMailService;

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
 * <p>
 * UsuarioMailConfiguration is used by UsuarioService to send emails.
 */
@Configuration
@Import(value = {RepositoryConfig.class, UsuarioMailConfiguration.class})
public class UsuarioRepoConfiguration {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final UsuarioMailService mailService;

    @Autowired
    public UsuarioRepoConfiguration(DataSource dataSource, JdbcTemplate jdbcTemplate, UsuarioMailService mailService)
    {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.mailService = mailService;
    }

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
    public UsuarioManagerIf usuarioManager()
    {
        return new UsuarioManager(comunidadDao(jdbcTemplate), usuarioDao(jdbcTemplate), tokenStore(), mailService);
    }
}
