package com.didekin.common.repository;

import com.didekinlib.http.exception.ExceptionMsgIf;

/**
 * User: pedro
 * Date: 19/07/15
 * Time: 10:33
 */
public class ServiceException extends RuntimeException {

    public static final String COMUNIDAD_FK = " FOREIGN KEY (`c_id`)";
    public static final String COMUNIDAD_UNIQUE_KEY = "key 'tipo_via'";
    public static final String DUPLICATE_ENTRY = "Duplicate entry";
    public static final String GENERATED_KEY = "getGeneratedKeys()_NO_VALUE";
    public static final String USER_NAME = "user_name";
    public static final String USER_COMU_PK = "key 'PRIMARY'";

    private final ExceptionMsgIf exceptionMsg;

    public ServiceException(ExceptionMsgIf exceptionMsg)
    {
        this.exceptionMsg = exceptionMsg;
    }

    public ExceptionMsgIf getExceptionMsg()
    {
        return exceptionMsg;
    }

}
