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

    static final String new_data = "New data";
    static final String new_password_tag = "Your new password is: ";
    static final String change_password_in_tag = "You can change it in: Your user -> Renew password.";
    private static final Object[][] contents = new Object[][]{
            {SUBJECT.name(), new_data},
            {TXT_Password.name(), new_password_tag},
            {TXT_CHANGE_Password.name(), change_password_in_tag}
    };

    @Override
    protected Object[][] getContents()
    {
        return contents;
    }
}
