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
import static com.didekin.common.mail.MailKey.SALUDO;
import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.userservice.mail.UsuarioMailConfiguration.text_plain_UTF_8;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.TO;
import static com.didekin.userservice.mail.UsuarioMailConfigurationPre.strato_buzon_folder;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;
import static java.lang.System.getenv;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Folder.READ_WRITE;
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

    // ...................... Helpers ......................

    public void extSetUp() throws MessagingException
    {
        if (!folder.isOpen()) {
            folder.open(READ_WRITE);
        }
        expungeFolder();
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

    public Folder getFolder()
    {
        return folder;
    }


    public void extTimedCleanUp() throws MessagingException
    {
        // Cleanup mail folder.
        folder.open(READ_WRITE);
        waitAtMost(20, SECONDS).until(() -> folder.getMessageCount() != 0);
        closeStoreAndFolder();
    }

    // ............... Check helpers ...................

    public void checkPasswordMessage(Usuario usuario, String localeToStr) throws MessagingException, IOException
    {
        ResourceBundle mailBundle = ResourceBundle.getBundle(mailBundleName, getLocale(localeToStr));
        ResourceBundle usuarioBundle = ResourceBundle.getBundle(usuarioMailBundleName, getLocale(localeToStr));

        waitAtMost(15, SECONDS).until(() -> folder.getMessageCount() != 0);

        Message[] messages = folder.getMessages();
        assertThat(messages.length, is(1));
        assertThat(messages[0].getSubject(), is(usuarioBundle.getString(SUBJECT.name())));
        assertThat(messages[0].getContentType(), is(text_plain_UTF_8));

        final String msgContent = (String) messages[0].getContent();
        assertThat(msgContent, allOf(
                containsString(mailBundle.getString(SALUDO.name())),
                containsString(usuario.getPassword()),
                containsString(usuarioBundle.getString(TXT_CHANGE_Password.name())),
                containsString(usuarioBundle.getString(TXT_Password.name()))
        ));

        checkFromTo(messages[0]);
        checkPswd(msgContent, usuario);

        messages[0].setFlag(DELETED, true);
        folder.expunge();
    }

    public String getPswdFromMsg() throws MessagingException, IOException
    {
        waitAtMost(30, SECONDS).until(() -> folder.getMessageCount() != 0);
        // Take the last one in the folder.
        String msgContent = (String) folder.getMessages()[0].getContent();
        return msgContent.split(getDoubleLineSeparatorFromMsg(msgContent))[1].split(":")[1].trim();
    }

    private void checkPswd(String msgContent, Usuario usuario)
    {
        assertThat(msgContent, containsString(usuario.getPassword()));
        String separator = getDoubleLineSeparatorFromMsg(msgContent);
        String password = msgContent.split(separator)[1].split(":")[1].trim();
        assertThat(password, is(usuario.getPassword()));
    }

    private void checkFromTo(Message message) throws MessagingException
    {
        assertThat(((InternetAddress) message.getFrom()[0]).getAddress(), is(getenv("mail_from")));
        assertThat(((InternetAddress) message.getAllRecipients()[0]).getAddress(), is(TO));
    }

    private String getDoubleLineSeparatorFromMsg(String msgContent)
    {
        if (msgContent.contains("\r\n\r\n")) {
            return "\r\n\r\n";
        } else if (msgContent.contains("\r\r")) {
            return "\r\r";
        } else {
            return "\n\n";
        }
    }
}
