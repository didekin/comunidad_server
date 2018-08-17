package com.didekin.userservice.gcm;

import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.gcm.model.common.GcmMulticastRequest;
import com.didekinlib.gcm.model.common.GcmRequest;
import com.didekinlib.gcm.model.common.GcmResponse;
import com.didekinlib.gcm.model.incidservice.GcmRequestData;
import com.didekinlib.gcm.retrofit.GcmEndPointImp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.didekinlib.gcm.model.common.GcmServConstant.GCM_ERROR_CODE;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * User: pedro
 * Date: 28/07/16
 * Time: 18:57
 */
@Service
class GcmUserService implements GcmUserComuServiceIf {

    private static final Logger logger = LoggerFactory.getLogger(GcmUserService.class.getCanonicalName());

    private String didekin_package = "com.didekindroid";
    private String didekin_firebase_project_key =
            "AAAADknoTJQ:APA91bGRihWJup9TYYtKl6LV7d01f5DZJDr5edlwh8KV4fLzq8S20OYyxnqP7Hsj2b4B4zDU0G_jzDH8bOwXGlz77XMFzcPWnEZ8EcDqTbiNTSjDHxuegT2eE8Dsn9YvozF4GIbIHFaJ";
    private String didekin_api_key_header = "key=" + didekin_firebase_project_key;
    private GcmEndPointImp gcmEndPoint;
    private UsuarioManager usuarioService;

    @Autowired
    public GcmUserService(GcmEndPointImp gcmEndPoint, UsuarioManager usuarioService)
    {
        this.gcmEndPoint = gcmEndPoint;
        this.usuarioService = usuarioService;
    }

    public CompletableFuture<Integer> sendGcmMsgToUserComu(final GcmRequestData requestData)
    {
        return supplyAsync(() -> usuarioService.getGcmTokensByComunidad(requestData.getComunidadId()))
                .thenApply(
                        tokens -> new GcmMulticastRequest.Builder(tokens, new GcmRequest.Builder(requestData, didekin_package).build()).build()
                )
                .thenApply(
                        request -> request.getRegistration_ids().length > 0 ?
                                gcmEndPoint.sendMulticastGzip(didekin_api_key_header, request) :
                                new GcmResponse(0, 0, 0, 0, null)
                )
                .thenApply(gcmResponse -> {
                    if (gcmResponse.getTokensToProcess().size() > 0) {
                        return usuarioService.modifyUserGcmTokens(gcmResponse.getTokensToProcess());
                    }
                    return 0;
                })
                .exceptionally(throwable -> {
                    logger.error(throwable.getCause().getMessage());
                    return GCM_ERROR_CODE;
                });
    }
}