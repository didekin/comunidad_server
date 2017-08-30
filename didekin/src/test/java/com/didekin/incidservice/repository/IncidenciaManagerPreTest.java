package com.didekin.incidservice.repository;

import com.didekin.common.DbPre;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IncidenciaManagerConfiguration.class})
@Category({DbPre.class})
public class IncidenciaManagerPreTest extends IncidenciaManagerTest {
}