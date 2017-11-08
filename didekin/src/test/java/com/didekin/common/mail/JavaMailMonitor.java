package com.didekin.common.mail;

import com.didekinlib.model.usuario.Usuario;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.mailBundleName;
import static com.didekin.common.mail.BundleUtil.usuarioMailBundleName;
import static com.didekin.common.mail.MailConstant.mail_from;
import static com.didekin.common.mail.MailConstant.text_plain_UTF_8;
import static com.didekin.common.mail.MailKey.SALUDO;
import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.strato_buzon_folder;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.mail.Flags.Flag.DELETED;
import static org.awaitility.Awaitility.waitAtMost;
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
        assertThat(store.getFolder(strato_buzon_folder).exists(), is(true));
        folder = store.getFolder(strato_buzon_folder);
    }

    public Folder getFolder()
    {
        return folder;
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

    public void checkPasswordMessage(Usuario usuario, String localeToStr) throws MessagingException, IOException
    {
        ResourceBundle mailBundle = ResourceBundle.getBundle(mailBundleName, getLocale(localeToStr));
        ResourceBundle usuarioBundle = ResourceBundle.getBundle(usuarioMailBundleName, getLocale(localeToStr));

        waitAtMost(12, SECONDS).until(() -> folder.getMessageCount() != 0);

        Message[] messages = folder.getMessages();
        assertThat(messages.length, is(1));
        assertThat(messages[0].getSubject(), is(usuarioBundle.getString(SUBJECT.name())));
        assertThat(messages[0].getContentType(), is(text_plain_UTF_8));
        assertThat(((String) messages[0].getContent()), allOf(
                containsString(mailBundle.getString(SALUDO.name())),
                containsString(usuario.getPassword()),
                containsString(usuarioBundle.getString(TXT_CHANGE_Password.name())),
                containsString(usuarioBundle.getString(TXT_Password.name()))
        ));
        if (usuario.getPassword() != null) {
            assertThat(((String) messages[0].getContent()), containsString(usuario.getPassword()));
        }
        checkFromTo(messages[0]);

        messages[0].setFlag(DELETED, true);
        folder.expunge();
    }

    private void checkFromTo(Message message) throws MessagingException
    {
        assertThat(((InternetAddress) message.getFrom()[0]).getAddress(), is(mail_from));
        assertThat(((InternetAddress) message.getAllRecipients()[0]).getAddress(), is(TO));
    }
}
