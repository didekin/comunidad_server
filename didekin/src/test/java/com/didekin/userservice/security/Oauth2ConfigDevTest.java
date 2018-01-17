package com.didekin.userservice.security;

import com.didekin.Application;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class,
        RetrofitConfigurationDev.class,
        UsuarioMailConfigurationPre.class})
@ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
@Category({LocalDev.class})
@WebIntegrationTest
@DirtiesContext
public class Oauth2ConfigDevTest extends OauthConfigTest {
}