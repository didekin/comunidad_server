package com.didekin.userservice.gcm;

import com.didekinlib.gcm.model.common.GcmRequestData;
import com.didekinlib.model.common.gcm.GcmToComunidadHelper;

import java.util.concurrent.CompletableFuture;

/**
 * User: pedro
 * Date: 29/07/16
 * Time: 13:59
 */
public interface GcmUserComuServiceIf {

    CompletableFuture<Integer> sendGcmMsgToUserComu(GcmToComunidadHelper tokensHelper, GcmRequestData requestData);
}
