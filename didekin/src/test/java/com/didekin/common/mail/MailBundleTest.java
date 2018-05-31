package com.didekin.common.mail;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.mailBundleName;
import static com.didekin.common.mail.MailKey.BYE;
import static com.didekin.common.mail.MailKey.SALUDO;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_EN;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.common.testutils.LocaleConstant.twoComponent_local_EN;
import static com.didekin.common.testutils.LocaleConstant.twoComponent_local_ES;
import static java.util.Locale.getDefault;
import static java.util.ResourceBundle.getBundle;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 14:25
 *
 * Remark: If the specified locale's language, script, country, and variant are all empty strings,
 * then the base name is the only candidate bundle name.
 */
public class MailBundleTest {

    @Test
    public void test_GetBundle_1()
    {
        checkLocalBundle(oneComponent_local_ES, MailBundle_es.class);
        checkLocalBundle(twoComponent_local_ES, MailBundle_es.class);

        checkLocalBundle(oneComponent_local_EN, MailBundle_en.class);
        checkLocalBundle(twoComponent_local_EN, MailBundle_en.class);

        // Default locale: english language, region SPAIN.
        assertThat(getLocale(getDefault().toString()).toString(), is("en_ES"));
        checkLocalBundle("en_US", MailBundle_en.class);

        // Locale without resourceBundle.
        checkLocalBundle("ac_HB", MailBundle_en.class);
        checkLocalBundle("ac", MailBundle_en.class);
        // Locale empty: MailBundle
        checkLocalBundle("", MailBundle.class);
    }

    @Test
    public void test_GetBundle_2()
    {
        // Change default locale.
        Locale.setDefault(new Locale(twoComponent_local_ES));
        assertThat(getLocale(getDefault().toString()).toString(), is(twoComponent_local_ES));

        // Locale without resourceBundle.
        checkLocalBundle("ac_HB", MailBundle.class);
        checkLocalBundle("ac", MailBundle.class);
        checkLocalBundle("", MailBundle.class);
    }

    @Test
    public void test_GetBundle_3()
    {
        // Change default locale.
        Locale.setDefault(new Locale(oneComponent_local_ES));
        assertThat(getLocale(getDefault().toString()).toString(), is(oneComponent_local_ES));

        // Locale without resourceBundle.
        checkLocalBundle("ac_HB", MailBundle_es.class);
        checkLocalBundle("ac", MailBundle_es.class);
        checkLocalBundle("", MailBundle.class);
    }

    @Test
    public void test_GetString_1()
    {
        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_ES));
        assertThat(mailBundle.getString(SALUDO.name()), is("Hola"));
        assertThat(mailBundle.getString(BYE.name()), is("Saludos del equipo de Dídekin"));

        mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_EN));
        assertThat(mailBundle.getString(SALUDO.name()), is("Hello"));
        assertThat(mailBundle.getString(BYE.name()), is("Greetings from Dídekin's team"));
    }

    @Test
    public void test_GetString_2()
    {
        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(""));
        assertThat(mailBundle.getString(SALUDO.name()), is("Hola"));
        assertThat(mailBundle.getString(BYE.name()), is("Saludos del equipo de Dídekin"));
    }

    private <T extends ResourceBundle> void checkLocalBundle(String localToStr, Class<T> resourceBundleClass)
    {
        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(localToStr));
        assertThat(resourceBundleClass.cast(mailBundle), isA(resourceBundleClass));
    }
}