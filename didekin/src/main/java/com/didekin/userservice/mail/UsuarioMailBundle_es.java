package com.didekin.userservice.mail;

import java.util.ListResourceBundle;

import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;

/**
 * User: pedro@didekin
 * Date: 03/11/2017
 * Time: 18:48
 */
public class UsuarioMailBundle_es extends ListResourceBundle {

    static final String nuevos_datos = "Nuevos datos";
    static final String nueva_contrasegna_tag = "Tu nueva contraseña es: ";
    static final String cambia_contrasegna_in_tag = "Puedes cambiarla en: Tu usuario -> Renovar contraseña.";

    private static final Object[][] contents = new Object[][]{
            {SUBJECT.name(), "Nuevos datos"},
            {TXT_Password.name(), "Tu nueva contraseña es: "},
            {TXT_CHANGE_Password.name(), "Puedes cambiarla en: Tu usuario -> Renovar contraseña."}
    };

    @Override
    protected Object[][] getContents()
    {
        return contents;
    }
}

