package com.didekin.userservice.repository;

import com.didekin.auth.EncrypTkProducerBuilder;
import com.didekin.auth.TkConfiguration;
import com.didekin.common.repository.RepositoryConfig;
import com.didekin.userservice.mail.UsuarioMailConfiguration;
import com.didekin.userservice.mail.UsuarioMailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * User: pedro
 * Date: 26/03/15
 * Time: 10:53
 * <p>
 * UsuarioMailConfiguration is used by UsuarioService to send emails.
 */
@Configuration
@Import(value = {RepositoryConfig.class, UsuarioMailConfiguration.class, TkConfiguration.class})
public class UsuarioRepoConfiguration {

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioMailService mailService;
    private final EncrypTkProducerBuilder producerBuilder;

    @Autowired
    public UsuarioRepoConfiguration(JdbcTemplate jdbcTemplate, UsuarioMailService mailService, EncrypTkProducerBuilder producerBuilderIn)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.mailService = mailService;
        this.producerBuilder = producerBuilderIn;
    }

    @Bean
    public MunicipioDao municipioDao()
    {
        return new MunicipioDao(jdbcTemplate);
    }

    @Bean
    public UsuarioDao usuarioDao()
    {
        return new UsuarioDao(jdbcTemplate);
    }

    @Bean
    public ComunidadDao comunidadDao()
    {
        return new ComunidadDao(jdbcTemplate);
    }

    @Bean
    public UsuarioManager usuarioManager()
    {
        return new UsuarioManager(comunidadDao(), usuarioDao(), mailService, producerBuilder);
    }
}
