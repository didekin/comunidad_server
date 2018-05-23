package com.didekin.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.didekinlib.http.usuario.UsuarioServConstant.OPEN;

/**
 * User: pedro@didekin
 * Date: 20/05/2018
 * Time: 15:39
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**").excludePathPatterns(OPEN + "/**");
    }
}
