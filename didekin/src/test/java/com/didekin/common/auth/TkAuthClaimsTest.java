package com.didekin.common.auth;

import com.didekin.common.DbPre;
import com.didekin.common.LocalDev;
import com.didekin.userservice.auth.TkParamNames;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

import static com.didekin.common.auth.TkAuthClaims.TK_VALIDITY_DAYS;
import static com.didekin.common.auth.TkAuthClaims.TK_VALIDITY_SECONDS;
import static com.didekin.common.auth.TkAuthClaims.default_audience_value;
import static com.didekin.common.auth.TkAuthClaims.default_issuer_value;
import static com.didekin.common.auth.TkAuthClaims.doClaimsFromMap;
import static com.didekin.common.auth.TkAuthClaims.doDefaultAuthClaims;
import static com.didekin.common.auth.TkAuthClaims.doExpirationDate;
import static com.didekin.userservice.auth.TkParamNames.appId;
import static com.didekin.userservice.auth.TkParamNames.audience;
import static com.didekin.userservice.auth.TkParamNames.expiration;
import static com.didekin.userservice.auth.TkParamNames.issuer;
import static com.didekin.userservice.auth.TkParamNames.subject;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.jose4j.jwt.NumericDate.fromSeconds;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: pedro@didekin
 * Date: 10/05/2018
 * Time: 12:51
 */
@Category({LocalDev.class})
public class TkAuthClaimsTest {

    private Map<TkParamNames, Object> claimsMap;

    @Test
    public void test_doDefaultAuthClaims_1() throws MalformedClaimException
    {
        initMap();
        claimsMap.put(issuer, "mock_issuer");
        claimsMap.put(audience, "mock_audience");
        TkAuthClaims claims = doDefaultAuthClaims(claimsMap);
        checkMap(claims);
        assertThat(claims.getJwtClaimsFromMap().getIssuer(), is("mock_issuer"));
        assertThat(claims.getJwtClaimsFromMap().getAudience(), is(singletonList("mock_audience")));

        initMap();
        claims = doDefaultAuthClaims(claimsMap);
        checkMap(claims);
        assertThat(claims.getJwtClaimsFromMap().getAudience(), is(default_audience_value));
        assertThat(claims.getJwtClaimsFromMap().getIssuer(), is(default_issuer_value));
    }

    @Test
    public void test_doDefaultAuthClaims_2()
    {
        try {
            doDefaultAuthClaims(null);
            fail();
        } catch (IllegalArgumentException e) {
            // First exception is related to email/user.
            assertThat(e.getMessage(), containsString(subject.getName()));
        }
    }

    @Test
    public void test_doDefaultAuthClaims_3() throws MalformedClaimException
    {
        TkAuthClaims claims = doDefaultAuthClaims("user@name.com", "appId_abcde");
        assertThat(claims.getJwtClaimsFromMap().getSubject(), is("user@name.com"));
        assertThat(claims.getJwtClaimsFromMap().getClaimValue(appId.getName()), is("appId_abcde"));
        checkMap(claims);
    }

    @Test
    public void test_DoClaimsFromMap()
    {
        initMap();
        claimsMap.put(
                expiration,
                fromSeconds((now().minus(10, SECONDS)).getEpochSecond()).getValue());
        assertThat(doClaimsFromMap(claimsMap).getExpirationInstant().isBefore(now()), is(true));
    }

    @Test
    public void test_GetJwtClaimsFromMap() throws MalformedClaimException
    {
        initMap();
        checkMap(doDefaultAuthClaims(claimsMap));
    }

    // ============================ Tests of helper methods =========================

    @Test
    public void test_doExpirationNumDate()
    {
        long expireDateLong = doExpirationDate(fromSeconds(now().getEpochSecond()));
        long nowLong = now().getEpochSecond();
        long timeDiff = (expireDateLong - nowLong);
        assertThat(timeDiff <= TK_VALIDITY_SECONDS && timeDiff >= (TK_VALIDITY_SECONDS - 1), is(true));
    }

    @Test
    public void test_getExpirationNumDate()
    {
        initMap();
        TkAuthClaims authClaims = doDefaultAuthClaims(claimsMap);
        assertThat(authClaims.getExpirationNumDate() != null, is(true));
        assertThat(authClaims.getExpirationInstant().getEpochSecond(), is(authClaims.getExpirationNumDate().getValue()));
    }

    @Test
    public void test_checkTokenInvariants()
    {
        initMap();
        claimsMap.remove(appId);
        try {
            doDefaultAuthClaims(claimsMap);
            fail();
        } catch (IllegalArgumentException e) {
            // First exception is related to appId: email correct.
            assertThat(e.getMessage(), containsString(appId.getName()));
        }
    }

    // ============================ Helper methods =========================

    private void initMap()
    {
        claimsMap = new HashMap<>(1);
        claimsMap.putIfAbsent(subject, "pedro@didekin.es");
        claimsMap.putIfAbsent(appId, "appId_mock");
    }

    public static void checkMap(TkAuthClaims authClaims) throws MalformedClaimException
    {
        assertThat(authClaims.checkTokenInvariants(), is(true));
        long daysToExpiration = now().until(authClaims.getExpirationInstant(), DAYS);
        assertThat(daysToExpiration <= TK_VALIDITY_DAYS && daysToExpiration >= (TK_VALIDITY_DAYS - 1), is(true));

        JwtClaims jwtClaims = authClaims.getJwtClaimsFromMap();
        assertThat(jwtClaims.getClaimNames(),
                allOf(
                        hasItem(subject.getName()),
                        hasItem(issuer.getName()),
                        hasItem(expiration.getName()),
                        hasItem(appId.getName()),
                        hasItem(audience.getName())
                )
        );
        assertThat(jwtClaims.getExpirationTime() != null, is(true));
    }
}