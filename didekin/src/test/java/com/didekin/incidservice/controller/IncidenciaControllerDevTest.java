package com.didekin.incidservice.controller;

import com.didekin.Application;
import com.didekin.common.LocalDev;
import com.didekin.common.Profiles;
import com.didekin.common.controller.RetrofitConfigurationDev;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: pedro@didekin
 * Date: 20/11/15
 * Time: 11:47
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class,
        RetrofitConfigurationDev.class})
@ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
@Category({LocalDev.class})
@WebIntegrationTest
@DirtiesContext
public class IncidenciaControllerDevTest extends IncidenciaControllerAutowireTest {
}