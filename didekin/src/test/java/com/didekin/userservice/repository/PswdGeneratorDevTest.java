package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static com.didekin.userservice.repository.PswdGenerator.GENERATOR_13;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.PASSWORD;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:45
 */
@Category({DbPre.class, LocalDev.class})
public class PswdGeneratorDevTest {

    private int length = PswdGenerator.symbols.length;
    private List<Character> symbols2 = new ArrayList<>(length);


    @Before
    public void setUp() throws Exception
    {
        for (int i = 0; i < length; i++) {
            symbols2.add(PswdGenerator.symbols[i]);
        }
    }

    @Test
    public void testSymbols() throws Exception
    {
        assertThat(symbols2, not(hasItems('0', 'o', 'O', 'l')));
    }

    @Test
    public void testMakePswd() throws Exception
    {
        String password = GENERATOR_13.makePswd();
        assertThat(password.length(),is(13));
        assertThat(PASSWORD.isPatternOk(password),is(true));
    }
}