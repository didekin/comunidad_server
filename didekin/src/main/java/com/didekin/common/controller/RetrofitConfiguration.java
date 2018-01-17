package com.didekin.common.controller;


import com.didekinlib.http.retrofit.RetrofitHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Configuration
public class RetrofitConfiguration {

    static final String aws_eb_url = "https://didekin-web-pro.eu-central-1.elasticbeanstalk.com";
    private static final int http_timeOut = 60;

    @Bean
    public RetrofitHandler retrofitHandler()
    {
        return new RetrofitHandler(aws_eb_url, http_timeOut);
    }
}
