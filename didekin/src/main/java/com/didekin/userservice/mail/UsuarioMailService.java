package com.didekin.userservice.mail;

import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ResourceBundle;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.mailBundleName;
import static com.didekin.common.mail.BundleUtil.usuarioMailBundleName;
import static com.didekin.common.mail.MailConstant.mail_from;
import static com.didekin.common.mail.MailKey.BYE;
import static com.didekin.common.mail.MailKey.SALUDO;
import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;
import static java.util.ResourceBundle.getBundle;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 14:58
 */
@Service
public class UsuarioMailService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioMailService.class.getCanonicalName());

    private JavaMailSender mailSender;

    @Autowired
    public UsuarioMailService(JavaMailSender javaMailSender)
    {
        mailSender = javaMailSender;
    }

//  ...................................................................

    public void sendNewPswd(Usuario user, String localeToStr)
    {
        logger.debug("sendNewPswd()");

        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(localeToStr));
        ResourceBundle usuarioBundle = getBundle(usuarioMailBundleName, getLocale(localeToStr));

        SimpleMailMessage mailMsg = new SimpleMailMessage();
        mailMsg.setTo(user.getUserName());
        mailMsg.setSubject(usuarioBundle.getString(SUBJECT.name()));
        mailMsg.setFrom(mail_from);
        mailMsg.setText(mailBundle.getString(SALUDO.name()) + " " + user.getAlias() + ".\n\n" +
                        usuarioBundle.getString(TXT_Password.name()) + user.getPassword() + "\n\n" +
                        usuarioBundle.getString(TXT_CHANGE_Password.name()) + "\n\n" +
                        mailBundle.getString(BYE.name()) + "\n"
        );

        logger.debug("sendNewPswd(): message from = " + mailMsg.getFrom());
        logger.debug("sendNewPswd(): message = \n" + mailMsg.getText());

        mailSender.send(mailMsg);
    }
}
