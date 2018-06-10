package com.didekin.common.auth;


import com.didekin.userservice.auth.TkParamNames;

import org.jose4j.jwx.JsonWebStructure;

import java.util.HashMap;
import java.util.Map;

import static com.didekin.userservice.auth.TkParamNames.algorithm_ce;
import static com.didekin.userservice.auth.TkParamNames.algorithm_cek;
import static org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
import static org.jose4j.jwe.KeyManagementAlgorithmIdentifiers.DIRECT;

/**
 * Example of header with public key:
 * {
 * "alg":"ES256",
 * "jwk": {
 * "kty":"EC",
 * "crv":"P-256",
 * "x":"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU",
 * "y":"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0",
 * "use":"sig",
 * "kid":"Dynamic_1"
 * },
 * "kid":"Dynamic_1"
 * }
 */
public class TkHeaders {

    private static final Map<TkParamNames, String> defaultHeaderMap;

    static {
        defaultHeaderMap = new HashMap<>(2);
        defaultHeaderMap.put(algorithm_cek, DIRECT);
        defaultHeaderMap.put(algorithm_ce, AES_128_CBC_HMAC_SHA_256);
    }

    public static String getDefaultHeader(TkParamNames defaultHeaderName)
    {
        return defaultHeaderMap.get(defaultHeaderName);
    }

    private final Map<TkParamNames, String> headerMap;

    private TkHeaders(Map<TkParamNames, String> headerMapIn)
    {
        headerMap = new HashMap<>(2);
        if (headerMapIn != null && !headerMapIn.isEmpty()) {
            headerMap.putAll(headerMapIn);
        }
    }

    static TkHeaders doHeadersSymmetricKey(Map<TkParamNames, String> initHeaderMap)
    {
        TkHeaders headers = new TkHeaders(initHeaderMap);
        defaultHeaderMap.forEach(headers.headerMap::putIfAbsent);
        return headers;
    }

    public static TkHeaders doHeadersSymmetricKey()
    {
        return doHeadersSymmetricKey(null);
    }

    public <T extends JsonWebStructure> void putHeadersIn(T jsonWebStructure)
    {
        headerMap.forEach((headerName, headerValue) -> jsonWebStructure.setHeader(headerName.getName(), headerValue));
    }

    String getTokenHeader(TkParamNames paramName)
    {
        return headerMap.get(paramName);
    }
}
