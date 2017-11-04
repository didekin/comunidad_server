package com.didekin.userservice.mail;

import java.util.ListResourceBundle;

import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 16:40
 */
public class UsuarioMailBundle_en extends ListResourceBundle {

    private static final Object[][] contents = new Object[][]{
            {SUBJECT.name(), "New data"},
            {TXT_Password.name(), "Your new password is: "},
            {TXT_CHANGE_Password.name(), "You can change it in: Your user -> Renew password."}
    };

    @Override
    protected Object[][] getContents()
    {
        return contents;
    }
}
