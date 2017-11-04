package com.didekin.common.mail;

import java.util.ListResourceBundle;

import static com.didekin.common.mail.MailKey.BYE;
import static com.didekin.common.mail.MailKey.SALUDO;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 14:17
 */
public class MailBundle_en extends ListResourceBundle {

    private static final Object[][] contents = new Object[][]{
            {SALUDO.name(), "Hello"},
            {BYE.name(), "Greetings from Dídekin's team"}
    };

    @Override
    protected Object[][] getContents()
    {
        return contents;
    }
}
