package com.didekin.userservice.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static java.lang.System.getenv;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 14:41
 */

@Configuration
public class UsuarioMailConfiguration {

    public static final String text_plain_UTF_8 = "text/plain; charset=UTF-8";

    static void doSenderSettings(JavaMailSenderImpl mailSender)
    {
        mailSender.setProtocol("smtp");
        mailSender.setHost("email-smtp.eu-west-1.amazonaws.com");
        mailSender.setPort(2587);
        mailSender.setUsername(getenv("aws_cred_username"));
        mailSender.setPassword(getenv("aws_cred_password"));
        mailSender.setDefaultEncoding("UTF-8");
    }

    static Properties doProperties()
    {
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.smtp.auth", String.valueOf(true));
        mailProperties.setProperty("mail.smtp.starttls.enable", String.valueOf(true));
        mailProperties.setProperty("mail.smtp.starttls.required", String.valueOf(true));
        return mailProperties;
    }

    @Bean
    public JavaMailSender javaMailSender()
    {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(doProperties());
        doSenderSettings(mailSender);
        return mailSender;
    }

    @Bean
    public UsuarioMailService usuarioMailService()
    {
        return new UsuarioMailService(javaMailSender());
    }
}