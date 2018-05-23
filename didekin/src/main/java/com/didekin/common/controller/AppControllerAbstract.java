package com.didekin.common.controller;

import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.repository.UsuarioManagerIf;
import com.didekinlib.http.exception.ErrorBean;
import com.didekinlib.http.exception.ExceptionMsgIf;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: pedro@didekin
 * Date: 18/11/15
 * Time: 15:21
 */
public abstract class AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(AppControllerAbstract.class.getCanonicalName());

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST) // The specific status is in the errorBean.
    public ErrorBean entityExceptionHandling(ServiceException e)
    {
        logger.error("entityExceptionHandling(): " + e.getExceptionMsg().getHttpMessage());
        ExceptionMsgIf exceptionMsg = e.getExceptionMsg();
        return new ErrorBean(exceptionMsg.getHttpMessage(), exceptionMsg.getHttpStatus());
    }

    protected String getUserNameFromAuthentication()   // TODO: suprimir o modificar.
    {
        logger.debug("getUserFromAuthentication()");
        /*OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
                .getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();*/
        return null;
    }

    protected Usuario getUserFromDb(UsuarioManagerIf usuarioService) throws ServiceException  // TODO: modificar.
    {
        logger.debug("getUserFromDb()");
        return usuarioService.getUserByUserName(getUserNameFromAuthentication());
    }
}
