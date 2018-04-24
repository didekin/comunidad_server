package com.didekin.incidservice.controller;

import com.didekin.common.AwsPre;
import com.didekin.common.controller.RetrofitConfigurationPre;
import com.didekin.common.springprofile.Profiles;
import com.didekin.incidservice.repository.IncidenciaManagerConfiguration;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:47
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RetrofitConfigurationPre.class,
        IncidenciaManagerConfiguration.class})
@ActiveProfiles({Profiles.NGINX_JETTY_PRE})
@Category({AwsPre.class})
public class IncidenciaControllerAwsTest extends IncidenciaControllerTest {
}