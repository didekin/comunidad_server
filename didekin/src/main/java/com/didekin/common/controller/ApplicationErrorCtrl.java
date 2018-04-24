package com.didekin.common.controller;


import com.didekinlib.http.exception.ErrorBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static com.didekinlib.http.CommonServConstant.ERROR;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;


/**
 * User: pedro@didekin
 * Date: 31/07/15
 * Time: 12:00
 * Controller to take care of the url  /error.
 */
@RestController
public class ApplicationErrorCtrl implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationErrorCtrl.class.getCanonicalName());

    private final ErrorAttributes errorAttributes;

    @Autowired
    public ApplicationErrorCtrl(@SuppressWarnings("SpringJavaAutowiringInspection") ErrorAttributes errorAttributes)
    {
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath()
    {
        return ERROR;
    }

    @RequestMapping(value = ERROR, produces = MIME_JSON)
    public ErrorBean handleErrors(HttpServletRequest request)
    {
        logger.info("handleErrors()");

        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> errorMap = errorAttributes.getErrorAttributes((WebRequest) requestAttributes, false);
        logger.error(errorMap.get("error") + "Http status: " + errorMap.get("status"));
        return new ErrorBean((String) errorMap.get("error"), (Integer) errorMap.get("status"));
    }
}
