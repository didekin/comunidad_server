package com.didekin.common.controller;


import com.didekinlib.http.HttpHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Configuration
public class RetrofitConfiguration {

    private static final String aws_eb_url = "https://didekin-web-pro.eu-central-1.elasticbeanstalk.com";
    private static final int http_timeOut = 60;

    @Bean
    public HttpHandler retrofitHandler()
    {
        return new HttpHandler.HttpHandlerBuilder(aws_eb_url).timeOutSec(http_timeOut).build();
    }
}
