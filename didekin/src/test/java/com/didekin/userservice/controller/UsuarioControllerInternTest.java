package com.didekin.userservice.controller;

import com.didekin.Application;
import com.didekin.common.DbPre;
import com.didekin.common.EntityException;
import com.didekin.common.LocalDev;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekin.userservice.mail.UsuarioMailConfigurationPre;
import com.didekinlib.model.usuario.Usuario;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.common.Profiles.MAIL_PRE;
import static com.didekin.common.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PACO;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UsuarioControllerInternTest {

    @Autowired
    private
    UsuarioController usuarioController;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUser_1() throws EntityException
    {
        Usuario usuario = usuarioController.completeUser(USER_PACO.getUserName());
        assertThat(usuario, allOf(
                hasProperty("uId", is(11L)),
                hasProperty("userName", is(USER_PACO.getUserName())),
                hasProperty("alias", is(USER_PACO.getAlias())),
                hasProperty("password", nullValue())
        ));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUser_2() throws EntityException
    {
        try {
            usuarioController.completeUser("no_existo");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInternalModifyUserGcmToken_1() throws EntityException
    {
        Usuario usuario = new Usuario.UsuarioBuilder().copyUsuario(usuarioController.completeUser("juan@noauth.com")).gcmToken("GCMtoKen1234X").build();
        // Verificamos la premisa.
        assertThat(usuarioController.getGcmToken(usuario.getuId()), nullValue());
        // Insertamos token.
        assertThat(usuarioController.internalModifyUserGcmToken(usuario.getUserName(), usuario.getGcmToken()), is(1));
        // Verificamos.
        assertThat(usuarioController.getGcmToken(usuario.getuId()), is("GCMtoKen1234X"));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testInternalModifyUserGcmToken_2() throws EntityException
    {
        // Caso: no existe usuario.
        try {
            usuarioController.internalModifyUserGcmToken("noexist_user", "GCMtoKen1234X");
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }
    }

//   ================================================  INNER  CLASSES  ==================================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({LocalDev.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UsuarioControllerInternDevTest extends UsuarioControllerInternTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class,
            UsuarioMailConfigurationPre.class})
    @ActiveProfiles(value = {NGINX_JETTY_LOCAL, MAIL_PRE})
    @Category({DbPre.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UsuarioControllerInternPreTest extends UsuarioControllerInternTest {
    }
}