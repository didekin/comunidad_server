package com.didekin.userservice.controller;

import com.didekin.Application;
import com.didekin.common.DbPre;
import com.didekin.common.EntityException;
import com.didekin.common.LocalDev;
import com.didekin.common.Profiles;
import com.didekin.common.controller.RetrofitConfigurationDev;
import com.didekinlib.model.usuariocomunidad.Rol;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.hamcrest.CoreMatchers;
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

import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.Rol.INQUILINO;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 02/04/15
 * Time: 12:12
 */
public abstract class UserComuControllerInternTest {

    @Autowired
    private UserComuController userComuController;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUserWithHighestRol_1() throws EntityException
    {
        UsuarioComunidad userComu = userComuController.completeWithHighestRol("luis@luis.com", 1L);
        assertThat(userComu, allOf(
                hasProperty("comunidad", hasProperty("c_Id", is(1L))),
                hasProperty("usuario", hasProperty("userName", is("luis@luis.com"))),
                hasProperty("roles", is(Rol.ADMINISTRADOR.function))
        ));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testCompleteUserWithHighestRol_2()
    {
        // Caso: no existe usuario.
        try {
            userComuController.completeWithHighestRol("no_existo", 1L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USER_NAME_NOT_FOUND));
        }

        // Caso: no existe comunidad.
        try {
            userComuController.completeWithHighestRol("luis@luis.com", 999L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }

        // Caso: existen usuario y comunidad, pero no hay relación entre ambos.
        try {
            userComuController.completeWithHighestRol("luis@luis.com", 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetUserComunidadChecker_1()
    {
        UsuarioComunidad userComu = userComuController.getUserComunidadChecker("juan@noauth.com", 2L);
        assertThat(userComu, allOf(
                notNullValue(),
                hasProperty("roles", CoreMatchers.is(INQUILINO.function))
        ));

        userComu = userComuController.getUserComunidadChecker("juan@noauth.com", 3L);
        assertThat(userComu, nullValue());
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testHasFunctionAdmInComunidad() throws EntityException
    {
        assertThat(userComuController.hasAuthorityAdmInComunidad("pedro@pedro.com", 1L), is(true));
        assertThat(userComuController.hasAuthorityAdmInComunidad("juan@noauth.com", 2L), is(false));
        assertThat(userComuController.hasAuthorityAdmInComunidad("luis@luis.com", 1L), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testIsUserInComunidad()
    {
        // No existe relación entre usuario y comunidad.
        assertThat(userComuController.isUserInComunidad("pedro@pedro.com", 4L), is(false));
        // Sí existe.
        assertThat(userComuController.isUserInComunidad("pedro@pedro.com", 1L), is(true));
        // La comunidad no existe.
        assertThat(userComuController.isUserInComunidad("pedro@pedro.com", 111L), is(false));
        // Ni el usuario, ni la comunidad existen.
        assertThat(userComuController.isUserInComunidad("noexisto@no.com", 111L), is(false));
    }

    //   ================================================  INNER  CLASSES  ==================================================

    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({LocalDev.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UserComuControllerInternDevTest extends UserComuControllerInternTest {
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = {Application.class,
            RetrofitConfigurationDev.class})
    @ActiveProfiles(value = {Profiles.NGINX_JETTY_LOCAL, Profiles.MAIL_PRE})
    @Category({DbPre.class})
    @WebIntegrationTest
    @DirtiesContext
    public static class UserComuControllerInternPreTest extends UserComuControllerInternTest {
    }
}