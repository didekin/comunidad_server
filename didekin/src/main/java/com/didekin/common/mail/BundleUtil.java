package com.didekin.common.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * User: pedro@didekin
 * Date: 03/11/2017
 * Time: 19:24
 */
public final class BundleUtil {

    public static final String mailBundleName = "com.didekin.common.mail.MailBundle";
    public static final String usuarioMailBundleName = "com.didekin.userservice.mail.UsuarioMailBundle";

    private BundleUtil()
    {
    }

    public static Locale getLocale(String localToStr)
    {
        String[] localeItems = localToStr.split("_");
        String lang = "", country = "", variant = "";

        switch (localeItems.length) {
            case 3:
                variant = localeItems[2];
            case 2:
                country = localeItems[1];
            case 1:
                lang = localeItems[0];
                break;
            default:
                break;
        }
        return new Locale(lang, country, variant);
    }
}
