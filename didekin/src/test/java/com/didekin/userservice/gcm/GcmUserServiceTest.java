package com.didekin.userservice.gcm;

import com.didekin.common.LocalDev;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekin.userservice.repository.UsuarioRepoConfiguration;
import com.didekinlib.gcm.model.common.GcmResponse;
import com.didekinlib.gcm.model.common.GcmResponse.Result;
import com.didekinlib.gcm.model.incidservice.GcmRequestData;
import com.didekinlib.gcm.retrofit.GcmEndPointImp;
import com.didekinlib.gcm.retrofit.GcmRetrofitHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.didekin.userservice.testutils.UsuarioTestUtils.luis;
import static com.didekin.userservice.testutils.UsuarioTestUtils.ronda_plazuela_10bis;
import static com.didekinlib.http.GsonUtil.objectToJsonStr;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_open_type;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.CoreMatchers.is;

/**
 * User: pedro@didekin
 * Date: 16/08/2018
 * Time: 11:42
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UsuarioRepoConfiguration.class})
@Category({LocalDev.class})
public class GcmUserServiceTest {

    private MockWebServer server;
    private GcmUserService gcmService;

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before()
        {
            server = new MockWebServer();
        }

        @Override
        protected void after()
        {
            try {
                server.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Autowired
    private UsuarioManager usuarioManager;

    @Before
    public void setUp()
    {
        GcmRetrofitHandler retrofitHandler = new GcmRetrofitHandler(server.url("/mock/").toString(), 60);
        GcmEndPointImp endPointImp = new GcmEndPointImp(retrofitHandler);
        gcmService = new GcmUserService(endPointImp, usuarioManager);
    }


    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert_sujetos_a.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:delete_sujetos.sql")
    @Test
    public void test_SendGcmMsgToUserComu()
    {
        // Multicast with only one token.
        final String REGISTRATION_ID_1_B = "luis_gcm_token_after";

        // Mock response.
        GcmResponse gcmResponseIn = new GcmResponse(
                1, 1001L, 1, 0,
                new Result[]{new Result(null, "msg_id_1", REGISTRATION_ID_1_B)}
        );
        String jsonResponse = objectToJsonStr(gcmResponseIn);
        server.enqueue(new MockResponse().setBody(jsonResponse));

        // Precondition:
        Assert.assertThat(luis.getGcmToken(), is("luis_gcm_token"));
        // Exec.
        gcmService.sendGcmMsgToUserComu(new GcmRequestData(incidencia_open_type, ronda_plazuela_10bis.getC_Id()));
        // Check
        waitAtMost(8, SECONDS).until(() -> usuarioManager.getUserData(luis.getUserName()).getGcmToken().equals(REGISTRATION_ID_1_B));
    }
}