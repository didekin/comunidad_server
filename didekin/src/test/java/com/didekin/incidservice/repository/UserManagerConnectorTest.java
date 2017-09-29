package com.didekin.incidservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.EntityException;
import com.didekin.common.LocalDev;
import com.didekinlib.model.incidencia.dominio.Incidencia;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.didekin.userservice.testutils.UsuarioTestUtils.juan;
import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.paco;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 27/08/17
 * Time: 14:21
 */
public abstract class UserManagerConnectorTest {

    @Autowired
    private UserManagerConnector connector;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CheckAuthorityInComunidad() throws Exception
    {
        assertThat(connector.checkAuthorityInComunidad(pedro.getUserName(), 1L), is(true));
        assertThat(connector.checkAuthorityInComunidad(juan.getUserName(), 2L), is(false));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CheckIncidModificationPower() throws Exception
    {
        // Premises: user with rol 'inq' in DB.
        assertThat(connector.checkAuthorityInComunidad(juan.getUserName(), 2L), is(false));
        // Exec and check: user initiates incidencia.
        assertThat(connector.checkIncidModificationPower(juan.getUserName(),
                new Incidencia.IncidenciaBuilder().incidenciaId(2L).userName(juan.getUserName()).build()),
                is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CheckUserInComunidad() throws Exception
    {
        assertThat(connector.checkUserInComunidad(pedro.getUserName(), 1L), is(true));
        try {
            connector.checkUserInComunidad(pedro.getUserName(), 4L);
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(USERCOMU_WRONG_INIT));
        }
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CompleteUser() throws Exception
    {
        assertThat(connector.completeUser(paco.getUserName()),
                allOf(
                        hasProperty("uId", is(11L)),
                        hasProperty("userName", is(paco.getUserName())),
                        hasProperty("alias", is(paco.getAlias())),
                        hasProperty("password", nullValue())
                )
        );
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_CompleteUserAndComuRoles() throws Exception
    {
        assertThat(connector.completeUserAndComuRoles(luis.getUserName(), 1L),
                allOf(
                        hasProperty("usuario", hasProperty("userName", is("luis@luis.com"))),
                        hasProperty("roles", is("adm,pro"))
                )
        );
    }

    /*  ==============================================  INNER CLASSES =============================================*/

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {IncidenciaManagerConfiguration.class})
    @Category({LocalDev.class})
    public static class UsuarioManagerConnectorDevTest extends UserManagerConnectorTest {
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {IncidenciaManagerConfiguration.class})
    @Category({DbPre.class})
    public static class UsuarioManagerConnectorPreTest extends UserManagerConnectorTest {
    }
}