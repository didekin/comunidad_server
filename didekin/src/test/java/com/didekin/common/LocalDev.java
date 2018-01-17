package com.didekin.common;

import com.didekin.common.springprofile.Profiles;

import org.springframework.context.annotation.Profile;

/**
 * User: pedro@didekin
 * Date: 06/05/16
 * Time: 17:13
 */
@Profile(value = {Profiles.MAIL_PRE, Profiles.NGINX_JETTY_LOCAL})
public interface LocalDev {
}
