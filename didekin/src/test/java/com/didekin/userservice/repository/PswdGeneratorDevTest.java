package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.didekin.userservice.repository.PswdGenerator.GENERATOR_13;
import static com.didekin.userservice.testutils.UsuarioTestUtils.checkGeneratedPassword;

/**
 * User: pedro@didekin
 * Date: 13/10/15
 * Time: 17:45
 */
@Category({DbPre.class, LocalDev.class})
public class PswdGeneratorDevTest {

    @Test
    public void testMakePswd() throws Exception
    {
        String password;
        for (int i = 0; i <= 20000; ++i) {
            password = GENERATOR_13.makePswd();
            checkGeneratedPassword(password);
        }
    }
}