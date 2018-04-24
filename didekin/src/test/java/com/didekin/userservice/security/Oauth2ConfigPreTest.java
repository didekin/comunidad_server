package com.didekin.userservice.security;

import com.didekin.Application;
import com.didekin.common.DbPre;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.springprofile.Profiles;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class,
        RetrofitConfigurationDev.class,
        UsuarioMailConfigurationPre.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
@Category({DbPre.class})
@DirtiesContext
public class Oauth2ConfigPreTest extends OauthConfigTest {
}