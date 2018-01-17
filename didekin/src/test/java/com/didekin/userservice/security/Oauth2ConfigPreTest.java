package com.didekin.userservice.security;

import com.didekin.Application;
import com.didekin.common.DbPre;
import com.didekin.common.springprofile.Profiles;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.common.controller.RetrofitConfigurationDev;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class,
        RetrofitConfigurationDev.class,
        UsuarioMailConfigurationPre.class})
@ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
@Category({DbPre.class})
@WebIntegrationTest
@DirtiesContext
public class Oauth2ConfigPreTest extends OauthConfigTest {
}