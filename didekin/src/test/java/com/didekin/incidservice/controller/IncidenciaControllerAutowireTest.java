package com.didekin.incidservice.controller;

import com.didekin.common.EntityException;
import com.didekin.incidservice.testutils.IncidenciaTestUtils;
import com.didekin.userservice.controller.UserComuController;
import com.didekin.userservice.controller.UsuarioController;
import com.didekin.userservice.gcm.GcmUserComuServiceIf;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import retrofit2.Response;

import static com.didekin.incidservice.testutils.IncidenciaTestUtils.pedro;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.ROLES_NOT_FOUND;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * User: pedro@didekin
 * Date: 01/05/16
 * Time: 20:07
 */
@SuppressWarnings({"unchecked"})
abstract class IncidenciaControllerAutowireTest extends IncidenciaControllerTest {

    @Autowired
    private
    UsuarioController usuarioController;
    @Autowired
    private
    UserComuController userComuController;

    @Autowired
    private
    IncidenciaController incidenciaController;

    @Autowired
    GcmUserComuServiceIf gcmUserComuServiceIf;

    @Autowired
    private
    IncidenciaController.IncidControllerChecker helper;

    private static final String tokenId_1 =
            "eHjO7v0yDv0:APA91bFe9Zzc2wh2F4uk5zr1KWHDQRbP9LQYv1WJ6LvVZ268xO-7B_oK1knt7_opdbUyUImg4ptOwKI-SienVZ0zT2O4ErhDOYc--HPH_qbuXIEfhG5FeQr14wcVEA1g5lPpjaXEfZiE";

    @Test
    public void testBefore()
    {
        assertThat(incidenciaController, notNullValue());
        assertThat(usuarioController, notNullValue());
    }

//  ==============================================================================================

    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_incidencia_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testDeleteIncidencia_A() throws EntityException, IOException
    {
        /* Caso: UNAUTHORIZED_TX_TO_USER;*/
        // Verificamos las premisas: no tiene authority administrador; es el usuario que dio el alta.
        assertThat(userComuController.hasAuthorityAdmInComunidad(IncidenciaTestUtils.luis.getUserName(), IncidenciaTestUtils.calle_plazuela_23.getC_Id()), is(false)); /*incidenciaService.seeIncidenciaById(5L).getUserName()*/
        assertThat(IncidenciaTestUtils.luis.getUserName().equals(ENDPOINT.seeIncidImportancia(tokenLuis(), 5L).execute().body().getIncidImportancia().getIncidencia().getUserName()), is(true));

        Response<Integer> response = ENDPOINT.deleteIncidencia(tokenLuis(), 5L).execute();
        assertThat(response.isSuccessful(), is(false));
        assertThat(retrofitHandler.getErrorBean(response).getMessage(), is(UNAUTHORIZED_TX_TO_USER.getHttpMessage()));
    }

    /**
     * Precondition: gcmToken is NOT NULL and NOT valid in database.
     * Postcondition:
     * - Notification is sent to the GCM server and the following message is received:
     * {"multicast_id":5687883094401283275,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"NotRegistered"}]}.
     * - GcmToken is written to null in database after insertion and communication con GCM service.
     */
    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_b.sql")
    @Sql(executionPhase = AFTER_TEST_METHOD,
            scripts = {"classpath:delete_sujetos.sql", "classpath:delete_incidencia.sql"})
    @Test
    public void testRegIncidImportancia_A() throws Exception
    {
        // Caso: importancia == 0; gcmToken is NOT NULL and NOT valid in database.

        // Premisa.
        assertThat(USER_ENDPOINT.modifyUserGcmToken(tokenPedro(), tokenId_1).execute().body(), is(1));
        assertThat(userComuController.getGcmTokensByComunidad(IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id()).size(), is(1));
        assertThat(USER_ENDPOINT.getGcmToken(tokenPedro()).execute().body().getToken(), is(tokenId_1));

        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(pedro.getUserName(), "incid_test", IncidenciaTestUtils.ronda_plazuela_10bis.getC_Id(), (short) 24);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .importancia((short) 0)
                .build();
        assertThat(ENDPOINT.regIncidImportancia(tokenPedro(), incidImportancia).execute().body(), is(2));
        SECONDS.sleep(10);
        assertThat(USER_ENDPOINT.getGcmToken(tokenPedro()).execute().body().getToken(), nullValue());
    }

    //  ================================== TEST HELPER METHODS ==================================
    @Test
    public void testHasModificationPower()
    {
        // Paremeter userName == controller.getUserNameFromAuthentication().

        Incidencia incidencia = IncidenciaTestUtils.doIncidencia(pedro.getUserName(), "incidservice", 2L, (short) 11);
        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .usuarioComunidad(IncidenciaTestUtils.pedro_lafuente)
                .importancia((short) 2)
                .build();

        // Caso 1: userName no existe.
        try {
            helper.checkIncidModificationPower("no_existo",
                    incidImportancia.getIncidencia().getComunidad().getC_Id(),
                    incidImportancia.getIncidencia().getUserName());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }

        // Caso 2: comunidad no existe.
        incidencia = IncidenciaTestUtils.doIncidencia(pedro.getUserName(), "incidservice", 999L, (short) 11);
        UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(incidencia.getComunidad(), pedro).build();
        incidImportancia = new IncidImportancia.IncidImportanciaBuilder(incidencia)
                .copyIncidImportancia(incidImportancia)
                .usuarioComunidad(userComu)
                .build();
        try {
            helper.checkIncidModificationPower(pedro.getUserName(),
                    incidImportancia.getIncidencia().getComunidad().getC_Id(),
                    incidImportancia.getIncidencia().getUserName());
            fail();
        } catch (EntityException e) {
            assertThat(e.getExceptionMsg(), is(ROLES_NOT_FOUND));
        }
    }
}
