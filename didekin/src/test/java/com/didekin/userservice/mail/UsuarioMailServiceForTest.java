package com.didekin.userservice.mail;

import com.didekinlib.model.usuario.Usuario;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import static com.didekin.userservice.mail.UsuarioMailConfiguration.doProperties;
import static com.didekin.userservice.mail.UsuarioMailConfiguration.doSenderSettings;
import static com.didekin.userservice.mail.UsuarioMailService.doPswdMsgFromBundles;

/**
 * User: pedro@didekin
 * Date: 09/11/2017
 * Time: 16:08
 */
@Service
public class UsuarioMailServiceForTest implements UsuarioMailServiceIf {

    private JavaMailSenderImpl mailSender;

    public UsuarioMailServiceForTest()
    {
        mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(doProperties());
        doSenderSettings(mailSender);
        // Cambiamos host:
        mailSender.setHost("email-smtp.eu-west-1.amazonaws.wrong");
    }

    @Override
    public void sendMessage(Usuario user, String localeToStr)
    {
        JavaMailSenderImpl mailSenderTest = mailSender;
        mailSenderTest.setHost("email-smtp.eu-west-1.amazonaws.wrong");
        mailSenderTest.send(doPswdMsgFromBundles(user, localeToStr));
    }
}
