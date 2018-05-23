package com.didekin.auth;


import com.didekinlib.http.usuario.TkParamNames;

import java.util.HashMap;
import java.util.Map;

import static com.didekinlib.http.usuario.TkParamNames.appId;
import static com.didekinlib.http.usuario.TkParamNames.subject;

/**
 * User: pedro@didekin
 * Date: 28/04/2018
 * Time: 13:31
 */
final class TkTestUtil {

    static Map<TkParamNames, Object> getDefaultTestClaims()
    {
        Map<TkParamNames, Object> claimsIn = new HashMap<>(2);
        claimsIn.putIfAbsent(subject, "pedro@didekin.es");
        claimsIn.putIfAbsent(appId, "appId_mock");
        return claimsIn;
    }
}
