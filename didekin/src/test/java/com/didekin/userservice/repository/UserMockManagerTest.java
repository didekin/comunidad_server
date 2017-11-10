package com.didekin.userservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.mail.MessagingException;

import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 16:23
 * <p>
 * See in UsuarioControllerTest:
 * testDeleteAccessToken_1(),
 * test_deleteAccessTokenByUserName(),
 * test_GetAccessToken(),
 * test_GetAccessTokenByUserName().
 */
public abstract class UserMockManagerTest {

    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private UsuarioManagerIf usuarioManager;

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_1() throws SQLException, EntityException, IOException, MessagingException
    {
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, USER_JUAN, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);
        // Exec.
        assertThat(userMockManager.regComuAndUserAndUserComu(userComu), is(true));
        // Check alta nueva comunidad.
        List<UsuarioComunidad> comunidades = usuarioManager.seeUserComusByUser(USER_JUAN.getUserName());
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(userComu));
        // Check login.
        assertThat(usuarioManager.login(USER_JUAN), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws SQLException, EntityException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.getComunidadById(calle_el_escorial.getC_Id()), is(calle_el_escorial));

        // Nuevo usuarioComunidad.
        UsuarioComunidad newPepe = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build(),
                new Usuario.UsuarioBuilder().copyUsuario(USER_PEPE).userName("newPepe").build(),
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);

        assertThat(userMockManager.regUserAndUserComu(newPepe), is(true));
        assertThat(usuarioManager.getComusByUser(newPepe.getUsuario().getUserName()).get(0), is(calle_el_escorial));
    }
}

