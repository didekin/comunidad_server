package com.didekin.common;

import org.springframework.context.annotation.Profile;

/**
 * User: pedro@didekin
 * Date: 06/05/16
 * Time: 17:14
 */
@Profile(value = {Profiles.MAIL_PRE, Profiles.NGINX_JETTY_LOCAL})
public interface DbPre {
}
