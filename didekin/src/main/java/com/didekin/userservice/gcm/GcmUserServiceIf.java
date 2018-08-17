package com.didekin.userservice.gcm;

import com.didekinlib.gcm.model.incidservice.GcmRequestData;

import java.util.concurrent.CompletableFuture;

/**
 * User: pedro
 * Date: 29/07/16
 * Time: 13:59
 */
public interface GcmUserServiceIf {

    CompletableFuture<Integer> sendGcmMsgToUserComu(GcmRequestData requestData);
}
