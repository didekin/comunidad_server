package com.didekin.userservice.mail;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.mail.JavaMailMonitor;
import com.didekinlib.model.usuario.Usuario;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import javax.mail.MessagingException;

import static com.didekin.common.Profiles.MAIL_PRE;
import static com.didekin.common.testutils.Constant.oneComponent_local_EN;
import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static javax.mail.Folder.READ_WRITE;

/**
 * User: pedro@didekin
 * Date: 14/10/15
 * Time: 09:53
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioMailConfigurationPre.class})
@ActiveProfiles({MAIL_PRE})
@Category({LocalDev.class, DbPre.class})
public class UsuarioMailServiceDevPreTest {

    @Autowired
    private UsuarioMailService mailService;

    @Autowired
    private JavaMailMonitor javaMailMonitor;

    private Usuario usuario;

    @Before
    public void setUp() throws MessagingException
    {
        javaMailMonitor.getFolder().open(READ_WRITE);
        javaMailMonitor.expungeFolder();
        usuario = new Usuario.UsuarioBuilder()
                .uId(3L)
                .alias("pedronevado")
                .password("password_new")
                .userName(TO)
                .build();
    }

    @After
    public void clearAfter() throws MessagingException
    {
        javaMailMonitor.closeStoreAndFolder();
    }

    @Test
    public void testPasswordMessage_1() throws MessagingException, InterruptedException, IOException
    {
        mailService.sendNewPswd(usuario, oneComponent_local_ES);
        javaMailMonitor.checkPasswordMessage(usuario, oneComponent_local_ES);
    }

    @Test
    public void testPasswordMessage_2() throws MessagingException, InterruptedException, IOException
    {
        mailService.sendNewPswd(usuario, oneComponent_local_EN);
        javaMailMonitor.checkPasswordMessage(usuario, oneComponent_local_EN);
    }
}