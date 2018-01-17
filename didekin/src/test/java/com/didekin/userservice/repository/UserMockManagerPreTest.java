package com.didekin.userservice.repository;

import com.didekin.common.DbPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 16:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMockRepoConfiguration.class})
@Category({DbPre.class})
@ActiveProfiles(value = {NGINX_JETTY_PRE})
public class UserMockManagerPreTest extends UserMockManagerTest {
}

