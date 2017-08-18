package com.didekin.userservice.repository;

import com.didekin.common.DbPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 15:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({DbPre.class})
public class UsuarioDaoPreTest extends UsuarioDaoTest {
}