package com.didekin.userservice.mail;

import com.didekin.common.mail.JavaMailMonitor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 14:41
 * We use in pre-production tests the production configuration for sending messages.
 * We add a monitor for the mail box in Strato for didekindroid@didekin.es.
 */

@Configuration
@Profile({MAIL_PRE})
@Import(UsuarioMailConfiguration.class)
public class UsuarioMailConfigurationPre {

    private static final String strato_imap_protocol = "imap";
    private static final String strato_imap_host = "imap.strato.com";
    private static final String strato_buzon_password = "aDh-9ZQ-Qzw-AJt";
    private static final String strato_buzon_user = "didekindroid@didekin.es";
    public static final String strato_buzon_folder = "Inbox";
    public static final String TO = strato_buzon_user;

    @Bean
    public JavaMailMonitor javaMailMonitor() throws MessagingException
    {
        Properties props = System.getProperties();
        Session session = Session.getInstance(props, null);
        session.setDebug(true);
        Store store = session.getStore(strato_imap_protocol);
        store.connect(strato_imap_host, strato_buzon_user, strato_buzon_password);
        return new JavaMailMonitor(store);
    }

    @Bean
    public UsuarioMailServiceForTest usuarioMailServiceForTest()
    {
        return new UsuarioMailServiceForTest();
    }
}