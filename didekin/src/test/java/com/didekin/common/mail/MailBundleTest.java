package com.didekin.common.mail;

import org.junit.Test;

import java.util.ResourceBundle;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.mailBundleName;
import static com.didekin.common.mail.MailKey.BYE;
import static com.didekin.common.mail.MailKey.SALUDO;
import static com.didekin.common.testutils.Constant.oneComponent_local_EN;
import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.common.testutils.Constant.twoComponent_local_EN;
import static com.didekin.common.testutils.Constant.twoComponent_local_ES;
import static java.util.ResourceBundle.getBundle;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 14:25
 */
public class MailBundleTest {

    @Test
    public void test_GetBundle() throws Exception
    {
        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_ES));
        assertThat(MailBundle_es.class.cast(mailBundle), isA(MailBundle_es.class));
        mailBundle = getBundle(mailBundleName, getLocale(twoComponent_local_ES));
        assertThat(MailBundle_es.class.cast(mailBundle), isA(MailBundle_es.class));

        mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_EN));
        assertThat(MailBundle_en.class.cast(mailBundle), isA(MailBundle_en.class));
        mailBundle = getBundle(mailBundleName, getLocale(twoComponent_local_EN));
        assertThat(MailBundle_en.class.cast(mailBundle), isA(MailBundle_en.class));
    }

    @Test
    public void test_GetString(){
        ResourceBundle mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_ES));
        assertThat(mailBundle.getString(SALUDO.name()), is("Hola"));
        assertThat(mailBundle.getString(BYE.name()), is("Saludos del equipo de Dídekin"));

        mailBundle = getBundle(mailBundleName, getLocale(oneComponent_local_EN));
        assertThat(mailBundle.getString(SALUDO.name()), is("Hello"));
        assertThat(mailBundle.getString(BYE.name()), is("Greetings from Dídekin's team"));
    }
}