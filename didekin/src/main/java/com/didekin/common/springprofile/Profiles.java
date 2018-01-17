package com.didekin.common.springprofile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * User: pedro@didekin
 * Date: 06/05/16
 * Time: 16:35
 */
public final class Profiles {

    private static final Logger logger = LoggerFactory.getLogger(Profiles.class.getCanonicalName());

    /**
     * This variable is hard-coded in didekindroid/terminal/env_init.sh (to start didekin_web application when testing in local).
     */
    public static final String NGINX_JETTY_LOCAL = "nginx-jetty-local";
    /**
     * This variable is hard-coded in didekin_web/terminal/aws_pre/.ebextensions/env-var.config.
     */
    public static final String NGINX_JETTY_PRE = "nginx-jetty-pre";
    public static final String MAIL_PRE = "mail-pre";

    private Profiles()
    {
    }

    public static void checkActiveProfiles(Environment environment)
    {
        logger.debug("checkActiveProfiles()");
        for (final String profileName : environment.getActiveProfiles()) {
            logger.debug(profileName);
            assertTrue(profileName.equals(NGINX_JETTY_LOCAL)
                            || profileName.equals(NGINX_JETTY_PRE)
                            || profileName.equals(MAIL_PRE),
                    "Wrong profile active");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertTrue(boolean assertion, String message)
    {
        if (!assertion) {
            throw new AssertionError(message);
        }
    }
}


