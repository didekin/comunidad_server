package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.Profiles.MAIL_PRE;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 16:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class, UsuarioMailConfigurationPre.class})
@Category({DbPre.class})
@ActiveProfiles(value = {MAIL_PRE})
public class UsuarioManagerPreTest extends UsuarioManagerTest {
}

