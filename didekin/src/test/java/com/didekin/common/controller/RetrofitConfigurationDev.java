package com.didekin.common.controller;

import com.didekin.common.springprofile.Profiles;
import com.didekinlib.http.HttpHandler;
import com.didekinlib.http.HttpHandler.JksInAppClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Profile(Profiles.NGINX_JETTY_LOCAL)
@Configuration
public class RetrofitConfigurationDev {

    private static final String jetty_local_URL =  "https://didekinspring.pagekite.me/";
    private static final String local_jks_appclient = "/Users/pedro/keystores/didekin_web_local/didekin_web_local_jks";
    private static final String local_jks_appclient_pswd = "octubre_5_2016_dev_jks";
    private static final int http_timeOut = 120;

    @Bean
    public HttpHandler retrofitHandler()
    {
        return new HttpHandler(jetty_local_URL, new JksInAppClient(local_jks_appclient, local_jks_appclient_pswd),http_timeOut);
    }
}