package com.didekin.common.mail;

import java.util.ListResourceBundle;

/**
 * User: pedro@didekin
 * Date: 06/11/2017
 * Time: 16:07
 */
public class MailBundle extends ListResourceBundle {

    @Override
    protected Object[][] getContents()
    {
        return MailBundle_es.getContentStatic();
    }
}
