package com.didekin.userservice.security;

import com.didekin.common.AwsPre;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.repository.RepositoryConfig;
import com.didekin.common.springprofile.Profiles;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 24/04/15
 * Time: 16:22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RetrofitConfigurationPre.class,
        RepositoryConfig.class})
@ActiveProfiles(value = {Profiles.NGINX_JETTY_PRE})
@Category({AwsPre.class})
public class Oauth2ConfigAwsTest extends OauthConfigTest {
}