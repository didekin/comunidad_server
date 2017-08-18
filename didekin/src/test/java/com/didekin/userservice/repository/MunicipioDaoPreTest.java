package com.didekin.userservice.repository;

import com.didekin.common.DbPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 19/04/15
 * Time: 11:18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({DbPre.class})
public class MunicipioDaoPreTest extends MunicipioDaoTest {
}