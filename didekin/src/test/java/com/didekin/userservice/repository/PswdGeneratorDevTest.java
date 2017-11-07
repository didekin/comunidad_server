package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.userservice.repository.PswdGenerator.AsciiInterval;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.letter_lower;
import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.letter_upper_1;
import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.letter_upper_2;
import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.number;
import static com.didekin.userservice.repository.PswdGenerator.AsciiInterval.underscore;
import static com.didekin.userservice.repository.PswdGenerator.default_password_length;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkGeneratedPassword;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:45
 */
@Category({DbPre.class, LocalDev.class})
public class PswdGeneratorDevTest {

    @Test
    public void testMakePswd_1() throws Exception
    {
        String password;
        for (int i = 0; i <= 20000; ++i) {
            password = new PswdGenerator().makePassword();
            checkGeneratedPassword(password, default_password_length);
        }
    }

    @Test
    public void testMakePswd_2() throws Exception
    {
        String password;
        for (int i = 0; i <= 200; ++i) {
            password = new PswdGenerator(10).makePassword();
            checkGeneratedPassword(password, 10);
        }
    }

    @Test
    public void test_IsInside() throws Exception
    {
        assertThat(number.isInside(49), is(true));
        assertThat(number.isInside(57), is(true));
        assertThat(number.isInside(54), is(true));
        assertThat(number.isInside(58), is(false));

        assertThat(underscore.isInside(95), is(true));
        assertThat(underscore.isInside(96), is(false));
    }

    @Test
    public void test_AddAsciiToList() throws Exception
    {
        AtomicInteger expectedListSize = new AtomicInteger(0);
        List<Integer> asciiList = new ArrayList<>(60);

        addCheckList(expectedListSize, asciiList, number);
        addCheckList(expectedListSize, asciiList, letter_upper_1);
        addCheckList(expectedListSize, asciiList, letter_upper_2);
        addCheckList(expectedListSize, asciiList, underscore);
        addCheckList(expectedListSize, asciiList, letter_lower);

        assertThat(AsciiInterval.asciiList.size(), is(expectedListSize.get()));
    }

    private void addCheckList(AtomicInteger expectedListSize, List<Integer> asciiList, AsciiInterval asciiInterval)
    {
        asciiInterval.addAsciiToList(asciiList);
        expectedListSize.addAndGet(asciiInterval.asciiFinal - asciiInterval.asciiInitial + 1);
        assertThat(asciiList.size(), is(expectedListSize.get()));
    }
}