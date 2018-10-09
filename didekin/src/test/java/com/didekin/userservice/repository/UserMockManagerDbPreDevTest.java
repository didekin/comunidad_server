package com.didekin.userservice.repository;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuario.http.AuthHeader;
import com.didekinlib.model.usuario.http.AuthHeaderIf;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.COMU_REAL;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_JUAN;
import static com.didekin.userservice.testutils.UsuarioTestUtils.USER_PEPE;
import static com.didekin.userservice.testutils.UsuarioTestUtils.calle_el_escorial;
import static com.didekin.userservice.testutils.UsuarioTestUtils.makeUsuarioComunidad;
import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.tkEncrypted_direct_symmetricKey_REGEX;
import static com.didekinlib.model.usuariocomunidad.Rol.ADMINISTRADOR;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mindrot.jbcrypt.BCrypt.checkpw;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UserMockRepoConfiguration.class})
@Category({LocalDev.class, DbPre.class})
@ActiveProfiles(value = {NGINX_JETTY_LOCAL})
public class UserMockManagerDbPreDevTest {

    @Autowired
    private UserMockManager userMockManager;
    @Autowired
    private UsuarioManager usuarioManager;

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_DoInsertAuthHeader()
    {
        String httpAuthHeaderIn = userMockManager.insertAuthTkGetNewAuthTkStr(pedro.getUserName());
        // Check values from authHeader obtained from DB.
        AuthHeaderIf httpHeaderFromDb = new AuthHeader.AuthHeaderBuilder(httpAuthHeaderIn).build();
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(httpHeaderFromDb.getToken()), is(true));
        assertThat(checkpw(httpHeaderFromDb.getToken(), usuarioManager.getUserData(pedro.getUserName()).getTokenAuth()), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegComuAndUserAndUserComu_1() throws ServiceException
    {
        UsuarioComunidad userComu = makeUsuarioComunidad(COMU_REAL, USER_JUAN, "portal", "esc", "1",
                "door", ADMINISTRADOR.function);
        // Exec.
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(userMockManager.regComuAndUserAndUserComu(userComu)), is(true));
        // Check alta nueva comunidad.
        List<UsuarioComunidad> comunidades = usuarioManager.seeUserComusByUser(USER_JUAN.getUserName());
        assertThat(comunidades.size(), is(1));
        assertThat(comunidades, hasItem(userComu));
        // Check login.
        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(usuarioManager.login(USER_JUAN)), is(true));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testRegUserAndUserComu_1() throws ServiceException
    {
        // Preconditions: there is a comunidad associated to other users.
        assertThat(usuarioManager.getComunidadById(calle_el_escorial.getC_Id()), is(calle_el_escorial));

        // Nuevo usuarioComunidad.
        UsuarioComunidad newPepe = makeUsuarioComunidad(
                new Comunidad.ComunidadBuilder().c_id(calle_el_escorial.getC_Id()).build(),
                new Usuario.UsuarioBuilder().copyUsuario(USER_PEPE).userName("new@pepe.com").build(),
                "portalB", "escB", "plantaZ", "door31", ADMINISTRADOR.function);

        assertThat(tkEncrypted_direct_symmetricKey_REGEX.isPatternOk(userMockManager.regUserAndUserComu(newPepe)), is(true));
        assertThat(usuarioManager.getComusByUser(newPepe.getUsuario().getUserName()).get(0), is(calle_el_escorial));
    }
}

