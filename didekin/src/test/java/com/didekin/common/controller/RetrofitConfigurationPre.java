package com.didekin.common.controller;

import com.didekinlib.http.HttpHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;

/**
 * User: pedro@didekin
 * Date: 20/04/16
 * Time: 16:33
 */
@Profile(NGINX_JETTY_PRE)
@Configuration
public class RetrofitConfigurationPre {

    private static final String jetty_awspre_URL = "https://didekin-web-pre.eu-central-1.elasticbeanstalk.com";
    private static final String jetty_awspre_jks_appclient = "/Users/pedro/keystores/didekin_web_pre/didekin_web_pre_jks";
    private static final String jetty_awspre_jks_appclient_pswd = "erbutco_1_6102_pre_jks";
    private static final int http_timeOut = 90;

    @Bean
    public HttpHandler retrofitHandler()
    {
        return new HttpHandler.HttpHandlerBuilder(jetty_awspre_URL)
                .okHttpClient(http_timeOut, new JksInAppClient(jetty_awspre_jks_appclient, jetty_awspre_jks_appclient_pswd))
                .build();
    }
}

