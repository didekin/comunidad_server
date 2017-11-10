package com.didekin.userservice.mail;

import com.didekinlib.model.usuario.Usuario;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import static com.didekin.userservice.mail.UsuarioMailService.doPswdMsgFromBundles;

/**
 * User: pedro@didekin
 * Date: 09/11/2017
 * Time: 16:08
 */
@Service
public class UsuarioMailServiceForTest implements UsuarioMailServiceIf {

    private JavaMailSender mailSender;

    public UsuarioMailServiceForTest(JavaMailSender javaMailSender)
    {
        mailSender = javaMailSender;
    }

    @Override
    public void sendMessage(Usuario user, String localeToStr)
    {
        JavaMailSenderImpl mailSenderTest = (JavaMailSenderImpl) mailSender;
        mailSenderTest.setHost("email-smtp.eu-west-1.amazonaws.wrong");
        mailSenderTest.send(doPswdMsgFromBundles(user, localeToStr));
    }
}
