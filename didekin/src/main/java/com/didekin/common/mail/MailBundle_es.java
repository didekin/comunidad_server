package com.didekin.common.mail;

import java.util.ListResourceBundle;

import static com.didekin.common.mail.MailKey.BYE;
import static com.didekin.common.mail.MailKey.SALUDO;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 13:51
 */
public class MailBundle_es extends ListResourceBundle {

    private static final Object[][] contents = new Object[][]{
            {SALUDO.name(), "Hola"},
            {BYE.name(), "Saludos del equipo de Dídekin"}
    };

    static Object[][] getContentStatic()
    {
        return contents;
    }

    @Override
    protected Object[][] getContents()
    {
        return contents;
    }
}
