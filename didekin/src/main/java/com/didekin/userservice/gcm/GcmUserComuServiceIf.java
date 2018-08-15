package com.didekin.userservice.gcm;

import com.didekinlib.gcm.model.common.GcmRequestData;
import com.didekinlib.model.common.gcm.GcmToComunidadHelper;

/**
 * User: pedro
 * Date: 29/07/16
 * Time: 13:59
 */
public interface GcmUserComuServiceIf {

    void sendGcmMsgToUserComu(GcmToComunidadHelper tokensHelper, GcmRequestData requestData);
}
