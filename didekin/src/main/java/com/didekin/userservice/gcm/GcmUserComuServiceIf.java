package com.didekin.userservice.gcm;

import com.didekinlib.gcm.model.common.GcmRequestData;
import com.didekinlib.model.common.gcm.GcmToComunidadHelper;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * User: pedro
 * Date: 29/07/16
 * Time: 13:59
 */
public interface GcmUserComuServiceIf {

    void sendGcmMessageToComunidad(GcmToComunidadHelper tokensHelper, GcmRequestData requestData);

    ThreadPoolExecutor getGcmSenderExec();

    ThreadPoolExecutor getGcmUpdaterExec();
}
