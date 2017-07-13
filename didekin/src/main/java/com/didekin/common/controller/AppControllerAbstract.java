package com.didekin.common.controller;

import com.didekin.common.EntityException;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.http.ErrorBean;
import com.didekinlib.model.exception.ExceptionMsgIf;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: pedro@didekin
 * Date: 18/11/15
 * Time: 15:21
 */
public abstract class AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(AppControllerAbstract.class.getCanonicalName());

    @ExceptionHandler({EntityException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST) // The specific status is in the errorBean.
    public ErrorBean entityExceptionHandling(EntityException e)
    {
        logger.error("entityExceptionHandling(): " + e.getExceptionMsg().getHttpMessage());
        ExceptionMsgIf exceptionMsg = e.getExceptionMsg();
        return new ErrorBean(exceptionMsg.getHttpMessage(), exceptionMsg.getHttpStatus());
    }

    protected String getUserNameFromAuthentication()
    {
        logger.debug("getUserFromAuthentication()");
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
                .getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }

    protected Usuario getUserFromDb(UsuarioServiceIf usuarioService) throws EntityException
    {
        logger.debug("getUserFromDb()");
        return usuarioService.getUserByUserName(getUserNameFromAuthentication());
    }
}
