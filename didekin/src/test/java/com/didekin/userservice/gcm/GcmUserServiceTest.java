package com.didekin.userservice.gcm;

import com.didekin.common.LocalDev;
import com.didekin.common.auth.TkCommonConfig;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekin.userservice.repository.UserMockRepoConfiguration;
import com.didekin.userservice.repository.UsuarioManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static org.junit.Assert.*;

/**
 * User: pedro@didekin
 * Date: 16/08/2018
 * Time: 11:42
 */
public abstract class GcmUserServiceTest {

    @Autowired
    private UsuarioManager usuarioManager;

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void test_SendGcmMsgToUserComu()
    {
    }

    // ================================= INNER CLASSES ======================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {UserMockRepoConfiguration.class,
            UsuarioMailConfigurationPre.class,
            TkCommonConfig.class})
    @Category({LocalDev.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    public static class GcmUserServiceDevTest extends GcmUserServiceTest{

    }
}