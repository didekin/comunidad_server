package com.didekin.common.controller;

import com.didekin.common.repository.ServiceException;
import com.didekinlib.http.exception.ErrorBean;
import com.didekinlib.http.exception.ExceptionMsgIf;

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
@SuppressWarnings("unused")
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
}
