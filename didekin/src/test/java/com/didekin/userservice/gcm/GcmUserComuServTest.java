package com.didekin.userservice.gcm;

import com.didekin.common.EntityException;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static com.didekin.ThreadPoolConstants.KEEP_ALIVE_MILLISEC;
import static com.didekin.ThreadPoolConstants.MAX_THREADS_GCM;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: pedro
 * Date: 29/07/16
 * Time: 12:18
 */
public abstract class GcmUserComuServTest {

    @Autowired
    UsuarioServiceIf usuarioService;

    abstract GcmUserComuServiceIf getGcmService();

    //  ========================= TEST METHODS ==========================

    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void testGetGcmTokensByComunidad() throws EntityException
    {
        // Insertamos tokens en tres usuarios.
        Usuario usuario = doUsuario("pedro@pedro.com", "pedro_gcm_token");
        assertThat(usuarioService.modifyUserGcmToken(usuario), is(1));
        usuario = doUsuario("luis@luis.com", "luis_gcm_token");
        assertThat(usuarioService.modifyUserGcmToken(usuario), is(1));
        usuario = doUsuario("juan@noauth.com", "juan_gcm_token");
        assertThat(usuarioService.modifyUserGcmToken(usuario), is(1));

        List<String> tokens = usuarioService.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(1L).build().getC_Id());
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("pedro_gcm_token", "luis_gcm_token"));

        tokens = usuarioService.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(2L).build().getC_Id());
        assertThat(tokens.size(), is(2));
        assertThat(tokens, hasItems("pedro_gcm_token", "juan_gcm_token"));

        tokens = usuarioService.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(3L).build().getC_Id());
        assertThat(tokens.size(), is(1));
        assertThat(tokens, hasItems("pedro_gcm_token"));

        // Caso: comunidad con usuario sin token.
        tokens = usuarioService.getGcmTokensByComunidad(new Comunidad.ComunidadBuilder().c_id(4L).build().getC_Id());
        assertThat(tokens, notNullValue());
        assertThat(tokens.size(), is(0));
    }

    @Test
    public void testGcmServiceConfig_1()
    {
        ThreadPoolExecutor poolSenderGcm = getGcmService().getGcmSenderExec();
        assertThat(poolSenderGcm.allowsCoreThreadTimeOut(), is(true));
        assertThat(poolSenderGcm.getCorePoolSize(), is(MAX_THREADS_GCM));
        assertThat(poolSenderGcm.getMaximumPoolSize(), is(MAX_THREADS_GCM));
        assertThat(poolSenderGcm.getPoolSize(), is(0));
        assertThat(poolSenderGcm.getKeepAliveTime(MILLISECONDS), is(KEEP_ALIVE_MILLISEC));
    }

    @Test
    public void testGcmServiceConfig_2()
    {
        ThreadPoolExecutor poolUpdaterGcm = getGcmService().getGcmUpdaterExec();
        assertThat(poolUpdaterGcm.allowsCoreThreadTimeOut(), is(true));
        assertThat(poolUpdaterGcm.getCorePoolSize(), is(0));
        assertThat(poolUpdaterGcm.getMaximumPoolSize(), is(Integer.MAX_VALUE));
        assertThat(poolUpdaterGcm.getPoolSize(), is(0));
        assertThat(poolUpdaterGcm.getKeepAliveTime(MILLISECONDS), is(KEEP_ALIVE_MILLISEC));
    }

//  ========================= HELPER METHODS ==========================

    private Usuario doUsuario(String userName, String gcmToken) throws EntityException
    {
        return new Usuario.UsuarioBuilder().uId(usuarioService.completeUser(userName).getuId()).gcmToken
                (gcmToken).build();
    }
}