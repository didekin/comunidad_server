package com.didekin.userservice.mail;

import com.didekin.common.mail.MailConstants;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    public void sendNewPswd(Usuario user, String newPswd)
    {
        logger.debug("sendNewPswd()");

        SimpleMailMessage mailMsg = new SimpleMailMessage();
        mailMsg.setTo(user.getUserName());
        mailMsg.setSubject(UsuarioMailConstants.SUBJECT_1);
        mailMsg.setFrom(MailConstants.mail_from);
        mailMsg.setText(
                UsuarioMailConstants.SALUDO + " " + user.getAlias() + ".\n\n" +
                        UsuarioMailConstants.TXT_CONTRASEÑA + newPswd + "\n\n" +
                        UsuarioMailConstants.TXT_CHANGE_CONTRASEÑA_1 + "\n" +
                        UsuarioMailConstants.TXT_CHANGE_CONTRASEÑA_2 + "\n\n" +
                        UsuarioMailConstants.BY + "\n"
        );

        logger.debug("sendNewPswd(): message from = " + mailMsg.getFrom());
        logger.debug("sendNewPswd(): message = " + mailMsg.getText());

        mailSender.send(mailMsg);
    }
}
