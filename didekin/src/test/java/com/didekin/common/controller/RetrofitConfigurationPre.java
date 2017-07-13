package com.didekin.common.controller;

import com.didekin.common.Profiles;
import com.didekinlib.http.retrofit.RetrofitHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Profile(Profiles.NGINX_JETTY_PRE)
@PropertySource({"classpath:/aws_env_test.properties"})
@Configuration
public class RetrofitConfigurationPre {

    @Autowired
    private Environment env;

    private static final int http_timeOut = 60;

    @Bean
    public RetrofitHandler retrofitHandler() throws NoSuchFieldException, IllegalAccessException
    {
        boolean isPRO = env.getProperty("aws.env").equals("PRO");
        String awsUrl;

        if (isPRO){
            awsUrl = RetrofitConfiguration.aws_eb_url;
        } else{
            awsUrl = env.getProperty("aws.eb.url");
        }

        return new RetrofitHandler(
                awsUrl,
                new RetrofitHandler.JksInAppClient(
                        env.getProperty("bks.store.appclient"),
                        System.getenv("jks_appclient_pswd")),
                http_timeOut
        );
    }
}

