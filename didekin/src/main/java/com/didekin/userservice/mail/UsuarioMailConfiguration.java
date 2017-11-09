package com.didekin.userservice.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static com.didekin.common.mail.MailConstant.aws_cred_password;
import static com.didekin.common.mail.MailConstant.aws_cred_username;
import static com.didekin.common.mail.MailConstant.aws_smtp_host;
import static com.didekin.common.mail.MailConstant.aws_smtp_port;
import static com.didekin.common.mail.MailConstant.default_encoding;
import static com.didekin.common.mail.MailConstant.mail_smtp_auth_prop;
import static com.didekin.common.mail.MailConstant.mail_smtp_starttls_enable_prop;
import static com.didekin.common.mail.MailConstant.mail_smtp_starttls_required;
import static com.didekin.common.mail.MailConstant.mail_transport_protocol;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 14:41
 */

@Configuration
public class UsuarioMailConfiguration {

    @Bean
    public JavaMailSender javaMailSender()
    {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailProperties.setProperty(mail_smtp_auth_prop, String.valueOf(true));
        mailProperties.setProperty(mail_smtp_starttls_enable_prop, String.valueOf(true));
        mailProperties.setProperty(mail_smtp_starttls_required, String.valueOf(true));
        mailSender.setJavaMailProperties(mailProperties);
        mailSender.setProtocol(mail_transport_protocol);
        mailSender.setHost(aws_smtp_host);
        mailSender.setPort(aws_smtp_port);
        mailSender.setUsername(aws_cred_username);
        mailSender.setPassword(aws_cred_password);
        mailSender.setDefaultEncoding(default_encoding);
        return mailSender;
    }

    @Bean
    public UsuarioMailServiceIf usuarioMailService()
    {
        return new UsuarioMailService(javaMailSender());
    }
}