package com.didekin.userservice.repository;

import com.didekin.common.LocalDev;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 17/04/15
 * Time: 13:13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({LocalDev.class})
public class UsuarioAuthServiceDevTest extends UsuarioAuthServiceTest {
}