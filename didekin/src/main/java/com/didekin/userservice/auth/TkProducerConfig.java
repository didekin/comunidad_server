package com.didekin.userservice.auth;

import com.didekin.common.auth.TkCommonConfig;
import com.didekin.common.auth.TkKeyServerProviderIf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * User: pedro@didekin
 * Date: 29/05/2018
 * Time: 16:59
 */
@Configuration
@Import(TkCommonConfig.class)
public class TkProducerConfig {

    @Bean
    public EncrypTkProducerBuilder encrypTkProducerBuilder(TkKeyServerProviderIf keyProviderIn)
    {
        return new EncrypTkProducerBuilder(keyProviderIn);
    }
}
