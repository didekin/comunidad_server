package com.didekin.incidservice.controller;

import com.didekin.Application;
import com.didekin.common.DbPre;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.common.springprofile.Profiles;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:47
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class,
        RetrofitConfigurationDev.class})
@ActiveProfiles({Profiles.NGINX_JETTY_LOCAL})
@Category({DbPre.class})

@DirtiesContext
public class IncidenciaControllerPreTest extends IncidenciaControllerTest {

}