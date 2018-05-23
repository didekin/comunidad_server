package com.didekin.auth;

import com.didekin.auth.EncryptedTkConsumer.EncrypTkConsumerBuilder;
import com.didekin.common.repository.ServiceException;
import com.didekinlib.http.usuario.AuthHeader;
import com.google.gson.JsonSyntaxException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.didekinlib.http.usuario.TkParamNames.appId;
import static com.didekinlib.http.usuario.TkValidaPatterns.closed_paths_REGEX;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.BAD_REQUEST;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.TOKEN_ENCRYP_DECRYP_ERROR;
import static com.didekinlib.http.usuario.UsuarioServConstant.AUTH_HEADER;

/**
 * User: pedro@didekin
 * Date: 20/05/2018
 * Time: 15:36
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private final EncrypTkConsumerBuilder builder;

    public AuthInterceptor(EncrypTkConsumerBuilder builderIn)
    {
       builder = builderIn;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || authHeader.isEmpty()) {
            // We exclude initial '/' in path.
            if (closed_paths_REGEX.isPatternOk(request.getRequestURI().substring(1))) {
                throw new ServiceException(BAD_REQUEST);
            }
            return true;
        }

        try {
            AuthHeader headerIn = new AuthHeader.AuthHeaderBuilder(authHeader).build();
            JwtClaims claims = builder.defaultInit(headerIn.getToken()).build().getClaims();
            return (headerIn.getAppID().equals(claims.getClaimValue(appId.getName()))) && headerIn.getUserName().equals(claims.getSubject());
        } catch (JsonSyntaxException | MalformedClaimException e) {
            throw new ServiceException(TOKEN_ENCRYP_DECRYP_ERROR);
        }
    }

    EncrypTkConsumerBuilder getBuilder()
    {
        return builder;
    }
}
