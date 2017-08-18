package com.didekin.userservice.repository;

import com.didekin.common.LocalDev;

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
@Category({LocalDev.class})
public class UsuarioDaoDevTest extends UsuarioDaoTest {
}