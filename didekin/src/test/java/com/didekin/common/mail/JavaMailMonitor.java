package com.didekin.common.mail;

import com.didekin.userservice.mail.UsuarioMailConfigurationPre;

import org.hamcrest.CoreMatchers;

import java.io.IOException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import static com.didekin.common.mail.MailConstants.TEXT_PLAIN_UTF_8;
import static com.didekin.common.mail.MailConstants.mail_from;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.strato_buzon_folder;
import static com.didekin.userservice.mail.UsuarioMailConstants.SALUDO;
import static com.didekin.userservice.mail.UsuarioMailConstants.SUBJECT_1;
import static com.didekin.userservice.mail.UsuarioMailConstants.TXT_CHANGE_CONTRASEÑA_1;
import static com.didekin.userservice.mail.UsuarioMailConstants.TXT_CHANGE_CONTRASEÑA_2;
import static com.didekin.userservice.mail.UsuarioMailConstants.TXT_CONTRASEÑA;
import static com.google.common.base.Preconditions.checkState;
import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Folder.READ_WRITE;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 13/06/16
 * Time: 13:29
 * <p>
 * Utility class for tests only.
 */
public class JavaMailMonitor {

    private final Store store;
    private final Folder folder;

    public JavaMailMonitor(Store store) throws MessagingException
    {
        this.store = store;
        checkState(store.getFolder(strato_buzon_folder).exists());
        folder = store.getFolder(strato_buzon_folder);
        folder.open(READ_WRITE);
        checkState(folder.isOpen());
    }

    public void checkPasswordMessage(String userAlias, String newPassword) throws MessagingException, IOException
    {
        Message[] messages = folder.getMessages();

        assertThat(messages.length, is(1));
        assertThat(messages[0].getSubject(), is(SUBJECT_1));
        assertThat(messages[0].getContentType(), is(TEXT_PLAIN_UTF_8));
        assertThat(((String) messages[0].getContent()), allOf(
                containsString(SALUDO),
                containsString(userAlias),
                containsString(TXT_CHANGE_CONTRASEÑA_1),
                containsString(TXT_CHANGE_CONTRASEÑA_2),
                containsString(TXT_CONTRASEÑA)
        ));
        if (newPassword != null) {
            assertThat(((String) messages[0].getContent()), containsString(newPassword));
        }
        assertThat(((InternetAddress) messages[0].getFrom()[0]).getAddress(), is(mail_from));
        assertThat(((InternetAddress) messages[0].getAllRecipients()[0]).getAddress(), CoreMatchers.is(UsuarioMailConfigurationPre.TO));

        messages[0].setFlag(DELETED, true);
        folder.expunge();
    }

    public void closeStoreAndFolder() throws MessagingException
    {
        expungeFolder();

        if (folder.isOpen()) {
            folder.close(true);
        }
        if (store.isConnected()) {
            store.close();
        }
    }

    public void expungeFolder() throws MessagingException
    {
        Message[] messages = folder.getMessages();
        for (Message message : messages) {
            message.setFlag(DELETED, true);
        }

        folder.expunge();
    }
}
