package com.didekin.common.auth;


import com.didekin.userservice.auth.TkParamNames;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.userservice.auth.TkParamNames.appId;
import static com.didekin.userservice.auth.TkParamNames.audience;
import static com.didekin.userservice.auth.TkParamNames.expiration;
import static com.didekin.userservice.auth.TkParamNames.issuer;
import static com.didekin.userservice.auth.TkParamNames.subject;
import static com.didekinlib.model.common.dominio.ValidDataPatterns.EMAIL;
import static java.util.Collections.singletonList;

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
    // Time to expire: 90 days.
    static final int TK_VALIDITY_DAYS = 90;
    // Time to expire in seconds.
    static final long TK_VALIDITY_SECONDS = TK_VALIDITY_DAYS * 24 * 60 * 60;
    // Message for invariants.
    static final String invalid_claim_values = " No valid claim list of values ";
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
                doExpirationDate(NumericDate.fromSeconds(Instant.now().getEpochSecond()))
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

    @Profile(value = {NGINX_JETTY_LOCAL, NGINX_JETTY_PRE})
    public static TkAuthClaims doClaimsFromMap(Map<TkParamNames, ?> initClaims)
    {
        TkAuthClaims claims = new TkAuthClaims(initClaims);
        defaultClaimMap.forEach(claims.claimsMap::putIfAbsent);
        claims.checkTokenInvariants();
        return claims;
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

    /**
     * @param expirationNumDate is measured in seconds.
     */
    static long doExpirationDate(NumericDate expirationNumDate)
    {
        expirationNumDate.addSeconds(TK_VALIDITY_SECONDS);
        // jose4j requires NumericDate be converted to long for serialization. It assumes the unit is SECONDS.
        return expirationNumDate.getValue();
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
        return Instant.ofEpochSecond(getExpirationNumDate().getValue());
    }

    NumericDate getExpirationNumDate()
    {
        return NumericDate.fromSeconds((Long) claimsMap.get(expiration));
    }
}
