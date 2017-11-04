package com.didekin.userservice.mail;

import org.junit.Test;

import java.util.ResourceBundle;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.usuarioMailBundleName;
import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.common.testutils.Constant.oneComponent_local_EN;
import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_CHANGE_Password;
import static com.didekin.userservice.mail.UsuarioMailKey.TXT_Password;
import static java.util.ResourceBundle.getBundle;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 04/11/2017
 * Time: 14:25
 */
public class UsuarioMailBundleTest {

    @Test
    public void test_GetBundle() throws Exception
    {
        ResourceBundle usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_ES));
        assertThat(UsuarioMailBundle_es.class.cast(usuarioBundle), isA(UsuarioMailBundle_es.class));

        usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_EN));
        assertThat(UsuarioMailBundle_en.class.cast(usuarioBundle), isA(UsuarioMailBundle_en.class));
    }

    @Test
    public void test_GetString()
    {
        ResourceBundle usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_ES));
        assertThat(usuarioBundle.getString(SUBJECT.name()), is("Nuevos datos"));

        usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_EN));
        assertThat(usuarioBundle.getString(TXT_Password.name()), is("Your new password is: "));
        assertThat(usuarioBundle.getString(TXT_CHANGE_Password.name()), is("You can change it in Your user -> Renew password."));
    }
}