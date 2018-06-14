package com.didekin.common.auth;

import com.didekin.common.repository.ServiceException;
import com.didekinlib.http.usuario.AuthHeader;
import com.didekinlib.http.usuario.AuthHeaderIf;
import com.google.gson.JsonSyntaxException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.didekin.common.auth.TkAuthClaims.getDefaultClaim;
import static com.didekin.userservice.auth.TkParamNames.appId;
import static com.didekin.userservice.auth.TkParamNames.audience;
import static com.didekin.userservice.auth.TkParamNames.issuer;
import static com.didekinlib.http.usuario.TkValidaPatterns.closed_paths_REGEX;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.BAD_REQUEST;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.UNAUTHORIZED;
import static com.didekinlib.http.usuario.UsuarioServConstant.AUTH_HEADER;

/**
 * User: pedro@didekin
 * Date: 20/05/2018
 * Time: 15:36
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class.getCanonicalName());
    private final EncrypTkConsumerBuilder consumerBuilder;

    @Autowired
    public AuthInterceptor(EncrypTkConsumerBuilder builderIn)
    {
        consumerBuilder = builderIn;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || authHeader.isEmpty()) {
            // We exclude initial '/' in path.
            if (closed_paths_REGEX.isPatternOk(request.getRequestURI().substring(1))) {
                // Requests with paths in the 'closed' area should have authHeader.
                throw new ServiceException(BAD_REQUEST);
            }
            return true;
        }

        try {
            AuthHeaderIf headerIn = new AuthHeader.AuthHeaderBuilder(authHeader).build();
            JwtClaims claims = consumerBuilder.defaultInit(headerIn.getToken()).build().getClaims();
            if (!headerIn.getAppID().equals(claims.getClaimValue(appId.getName()))
                    || !claims.getAudience().equals(getDefaultClaim(audience))
                    || !claims.getIssuer().equals(getDefaultClaim(issuer))) {
                throw new ServiceException(UNAUTHORIZED);
            }
            return true;
        } catch (JsonSyntaxException | MalformedClaimException | IllegalArgumentException e) {
            logger.debug(e.getMessage());
            throw new ServiceException(TOKEN_ENCRYP_DECRYP_ERROR);
        }
    }

    public EncrypTkConsumerBuilder getConsumerBuilder()
    {
        return consumerBuilder;
    }
}
