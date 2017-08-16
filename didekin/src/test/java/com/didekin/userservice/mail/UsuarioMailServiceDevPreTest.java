package com.didekin.userservice.mail;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.Profiles;
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

/**
 * User: pedro@didekin
 * Date: 14/10/15
 * Time: 09:53
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioMailConfigurationPre.class})
@ActiveProfiles({Profiles.MAIL_PRE})
@Category({LocalDev.class, DbPre.class})
public class UsuarioMailServiceDevPreTest {

    @Autowired
    private UsuarioMailService mailService;

    @Autowired
    private JavaMailMonitor javaMailMonitor;

    private Usuario usuario;
    private static final String newPassword = "password_new";

    @Before
    public void setUp() throws MessagingException
    {
        javaMailMonitor.expungeFolder();
        usuario = new Usuario.UsuarioBuilder()
                .uId(3L)
                .alias("pedronevado")
                .userName(UsuarioMailConfigurationPre.TO)
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
        mailService.sendNewPswd(usuario, newPassword);
        Thread.sleep(9000);
        javaMailMonitor.checkPasswordMessage(usuario.getAlias(), newPassword);
    }
}