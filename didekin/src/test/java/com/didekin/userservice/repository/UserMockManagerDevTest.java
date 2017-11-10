package com.didekin.userservice.repository;

import com.didekin.common.LocalDev;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.Profiles.MAIL_PRE;
import static com.didekin.common.Profiles.NGINX_JETTY_LOCAL;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 16:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMockRepoConfiguration.class})
@Category({LocalDev.class})
@ActiveProfiles(value = {NGINX_JETTY_LOCAL})
public class UserMockManagerDevTest extends UserMockManagerTest {
}

