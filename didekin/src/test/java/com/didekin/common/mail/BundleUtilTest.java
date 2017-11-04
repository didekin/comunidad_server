package com.didekin.common.mail;

import org.junit.Test;

import java.util.Locale;

import static com.didekin.common.mail.BundleUtil.getLocale;
import static com.didekin.common.testutils.Constant.oneComponent_local_ES;
import static com.didekin.common.testutils.Constant.twoComponent_local_ES;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 03/11/2017
 * Time: 19:32
 */
public class BundleUtilTest {

    @Test
    public void test_GetLocale() throws Exception
    {
        assertThat(getLocale(twoComponent_local_ES), is(new Locale("es", "ES")));
        assertThat(getLocale(twoComponent_local_ES).toString(), is("es_ES"));
        assertThat(getLocale(oneComponent_local_ES), is(new Locale("es")));
        assertThat(getLocale(oneComponent_local_ES).toString(), is("es"));

        assertThat(getLocale("en_US_al").toString(), is("en_US_al"));
    }
}