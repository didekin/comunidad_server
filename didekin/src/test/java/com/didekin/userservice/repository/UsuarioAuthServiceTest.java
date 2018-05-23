package com.didekin.userservice.repository;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collection;

import static com.didekin.userservice.testutils.UsuarioTestUtils.pedro;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.ROLES_NOT_FOUND;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: pedro@didekin
 * Date: 17/04/15
 * Time: 13:13
 */
public abstract class UsuarioAuthServiceTest {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private UsuarioManager usuarioManager;

    private UsuarioAuthService authService;     // TODO: descomentar y revisar.

    /*@SuppressWarnings("unchecked")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLoadUserByUsernameOk()
    {
        authService = new UsuarioAuthService(usuarioManager);
        assertThat(authService, notNullValue());

        UserDetails userDetails = authService.loadUserByUsername("luis@luis.com");
        assertThat(userDetails.getAuthorities().size(), is(2));

        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();
        assertThat(authorities, hasItems(new SimpleGrantedAuthority("admon"), new SimpleGrantedAuthority("user")));

        userDetails = authService.loadUserByUsername(pedro.getUserName());
        authorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();
        assertThat(userDetails.getAuthorities().size(), is(2));
        assertThat(authorities, hasItems(new SimpleGrantedAuthority("admon"), new SimpleGrantedAuthority("user")));

        userDetails = authService.loadUserByUsername("juan@noauth.com");
        authorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();
        assertThat(userDetails.getAuthorities().size(), is(1));
        assertThat(authorities, hasItem(new SimpleGrantedAuthority("user")));
    }

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testLoadUserByUsernameError1()
    {

        authService = new UsuarioAuthService(usuarioManager);

        try {
            authService.loadUserByUsername("pepe@noauth.com");
            Assert.fail("");
        } catch (UsernameNotFoundException ue) {
            assertThat(ue.getMessage(), containsString(ROLES_NOT_FOUND.toString()));
        }
    }

    @Test
    public void testLoadUserByUsernameError2()
    {
        authService = new UsuarioAuthService(usuarioManager);
        try {
            authService.loadUserByUsername("noexisto@fail.com");
            Assert.fail("");
        } catch (UsernameNotFoundException ue) {
            assertThat(ue.getMessage(), is(USER_NAME_NOT_FOUND.toString()));
        }
    }*/
}