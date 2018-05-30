package com.didekin.common.auth;


import com.didekinlib.http.usuario.TkParamNames;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didekinlib.http.usuario.TkParamNames.appId;
import static com.didekinlib.http.usuario.TkParamNames.audience;
import static com.didekinlib.http.usuario.TkParamNames.expiration;
import static com.didekinlib.http.usuario.TkParamNames.issuer;
import static com.didekinlib.http.usuario.TkParamNames.subject;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.EMAIL;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.singletonList;
import static org.jose4j.jwt.NumericDate.fromMilliseconds;

/**
 * User: pedro@didekin
 * Date: 09/05/2018
 * Time: 15:46
 * <p>
 * Creating process:
 * * -- Create a JWT Claims set containing the desired claims.
 * * -- Create a JOSE Header containing the desired set of Header Parameters.
 * * -- Create a JWS/JWE using the Message as the JWS payload or JWE plaintext.
 */
public class TkAuthClaims {

    // Default claims map.
    private static final Map<TkParamNames, Object> defaultClaimMap;
    static final List<String> default_audience_value = singletonList("didekin_web");
    static final String default_issuer_value = "didekin_auth";
    // 1440 horas (60 días).
    static final long TK_VALIDITY_SECONDS = 1440 * 60 * 60;
    // Message for invariants.
    private static final String invalid_claim_values = " No valid claim list of values ";
    // Claims map.
    private final Map<TkParamNames, Object> claimsMap;

    static {
        defaultClaimMap = new HashMap<>(2);
        defaultClaimMap.put(issuer, default_issuer_value);
        defaultClaimMap.put(audience, default_audience_value);
    }

    public static Object getDefaultClaim(TkParamNames defaultClaimName)
    {
        return defaultClaimMap.get(defaultClaimName);
    }

    private TkAuthClaims(Map<TkParamNames, ?> claimsMapIn)
    {
        claimsMap = new HashMap<>(3);
        if (claimsMapIn != null && !claimsMapIn.isEmpty()) {
            claimsMap.putAll(claimsMapIn);
        }
    }

    public static TkAuthClaims doDefaultAuthClaims(Map<TkParamNames, ?> initClaims)
    {
        TkAuthClaims claims = new TkAuthClaims(initClaims);
        claims.claimsMap.put(
                expiration,
                doExpirationDate(fromMilliseconds(Instant.now().toEpochMilli()))
        );
        defaultClaimMap.forEach(claims.claimsMap::putIfAbsent);
        claims.checkTokenInvariants();
        return claims;
    }

    public static TkAuthClaims doDefaultAuthClaims(String userName, String appId)
    {
        Map<TkParamNames, String> initClaims = new HashMap<>(2);
        initClaims.put(TkParamNames.subject, userName);
        initClaims.put(TkParamNames.appId, appId);
        return doDefaultAuthClaims(initClaims);
    }

    public Object getAuthClaim(TkParamNames paramName)
    {
        return claimsMap.get(paramName);
    }

    public JwtClaims getJwtClaimsFromMap()
    {
        JwtClaims claims = new JwtClaims();
        claimsMap.forEach((appClaimName, claimValue) -> claims.setClaim(appClaimName.getName(), claimValue));
        return claims;
    }

    // ============================ Helper methods =========================

    static long doExpirationDate(NumericDate expirationNumDate)
    {
        expirationNumDate.addSeconds(TK_VALIDITY_SECONDS);
        // jose4j requires NumericDate be converted to long for serialization.
        return expirationNumDate.getValueInMillis();
    }

    boolean checkTokenInvariants()
    {
        if (!claimsMap.containsKey(subject) || !EMAIL.isPatternOk((String) claimsMap.get(subject))) {
            throw new IllegalArgumentException(this.getClass().getName() + invalid_claim_values + ": " + subject.getName());
        }
        if (!claimsMap.containsKey(appId)) {
            throw new IllegalArgumentException(this.getClass().getName() + invalid_claim_values + ": " + appId.getName());
        }
        return true;
    }

    Instant getExpirationInstant()
    {
        return ofEpochMilli(getExpirationNumDate().getValueInMillis());
    }

    NumericDate getExpirationNumDate()
    {
        return fromMilliseconds((Long) claimsMap.get(expiration));
    }
}
