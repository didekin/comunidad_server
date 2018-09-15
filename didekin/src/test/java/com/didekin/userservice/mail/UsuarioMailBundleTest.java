package com.didekin.userservice.mail;

import com.didekin.common.LocalDev;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ResourceBundle;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.mail.BundleUtil.usuarioMailBundleName;
import static com.didekin.common.mail.MailKey.SUBJECT;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_EN;
import static com.didekin.common.testutils.LocaleConstant.oneComponent_local_ES;
import static com.didekin.userservice.mail.UsuarioMailBundle_en.change_password_in_tag;
import static com.didekin.userservice.mail.UsuarioMailBundle_en.new_data;
import static com.didekin.userservice.mail.UsuarioMailBundle_en.new_password_tag;
import static com.didekin.userservice.mail.UsuarioMailBundle_es.cambia_contrasegna_in_tag;
import static com.didekin.userservice.mail.UsuarioMailBundle_es.nueva_contrasegna_tag;
import static com.didekin.userservice.mail.UsuarioMailBundle_es.nuevos_datos;
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
@Category({LocalDev.class})
public class UsuarioMailBundleTest {

    @Test
    public void test_GetBundle()
    {
        ResourceBundle usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_ES));
        assertThat((UsuarioMailBundle_es) usuarioBundle, isA(UsuarioMailBundle_es.class));

        usuarioBundle = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_EN));
        assertThat((UsuarioMailBundle_en) usuarioBundle, isA(UsuarioMailBundle_en.class));
    }

    @Test
    public void test_GetString()
    {
        ResourceBundle bundle_es = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_ES));
        assertThat(bundle_es.getString(SUBJECT.name()), is(nuevos_datos));
        assertThat(bundle_es.getString(TXT_Password.name()), is(nueva_contrasegna_tag));
        assertThat(bundle_es.getString(TXT_CHANGE_Password.name()), is(cambia_contrasegna_in_tag));

        ResourceBundle bundle_en = getBundle(usuarioMailBundleName, getLocale(oneComponent_local_EN));
        assertThat(bundle_en.getString(SUBJECT.name()), is(new_data));
        assertThat(bundle_en.getString(TXT_Password.name()), is(new_password_tag));
        assertThat(bundle_en.getString(TXT_CHANGE_Password.name()), is(change_password_in_tag));
    }
}