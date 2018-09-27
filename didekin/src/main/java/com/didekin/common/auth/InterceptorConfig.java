package com.didekin.common.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.didekinlib.http.CommonServConstant.OPEN;

/**
 * User: pedro@didekin
 * Date: 20/05/2018
 * Time: 15:39
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    EncrypTkConsumerBuilder builder;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new AuthInterceptor(builder)).addPathPatterns("/**").excludePathPatterns(OPEN + "/**");
    }
}
