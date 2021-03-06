package com.didekin.userservice.controller;

import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.springprofile.Profiles.checkActiveProfiles;
import static com.didekinlib.http.CommonServConstant.FORM_URLENCODED;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.model.usuario.http.TkValidaPatterns.closed_paths_REGEX;
import static com.didekinlib.model.usuario.http.UserMockEndPoints.regComu_User_UserComu;
import static com.didekinlib.model.usuario.http.UserMockEndPoints.regUser_UserComu;
import static com.didekinlib.model.usuario.http.UserMockEndPoints.user_delete;
import static com.didekinlib.model.usuario.http.UsuarioServConstant.USER_PARAM;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: pedro@didekin
 * Date: 21/11/16
 * Time: 09:46
 *
 * This class allows for certain 'tuned' methods for use in tests in the client apps.
 * It implements methods in UserComuMockEndPoints.
 */
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "unused"})
@RestController
@Profile({NGINX_JETTY_LOCAL, NGINX_JETTY_PRE})
public class UserComuMockController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(UserComuMockController.class.getCanonicalName());

    // Messages for interceptor tests.
    public static final String CLOSED_AREA_MSG = "IS_CLOSED";
    public static final String OPEN_AREA_MSG = "IS_OPEN";

    private final UserMockManager userMockManager;
    private final UsuarioManager usuarioManager;

    @Autowired
    Environment env;

    @Autowired
    public UserComuMockController(UserMockManager userMockManager, UsuarioManager usuarioManager)
    {
        this.userMockManager = userMockManager;
        this.usuarioManager = usuarioManager;
    }

    @RequestMapping(value = regComu_User_UserComu, method = POST, consumes = MIME_JSON)
    public String regComuAndUserAndUserComu(@RequestBody UsuarioComunidad usuarioCom)
            throws ServiceException
    {
        logger.debug("regComuAndUserAndUserComu()");
        checkActiveProfiles(env);
        return userMockManager.regComuAndUserAndUserComu(usuarioCom);
    }

    @RequestMapping(value = regUser_UserComu, method = POST, consumes = MIME_JSON)
    public String regUserAndUserComu(@RequestBody UsuarioComunidad userComu) throws ServiceException
    {
        logger.debug("regUserAndUserComu()");
        checkActiveProfiles(env);
        return userMockManager.regUserAndUserComu(userComu);
    }

    @RequestMapping(value = user_delete, method = POST, consumes = FORM_URLENCODED)
    public boolean deleteUser(@RequestParam(USER_PARAM) String userName)
    {
        logger.debug("deleteUser()");
        checkActiveProfiles(env);
        return usuarioManager.deleteUser(userName);
    }

    /**
     * Mock implementation for the case when AuthInterceptor returns true.
     */
    @RequestMapping(value = "{mock_path}/{mock2_path}", method = GET)
    public String tryTokenInterceptor(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable String mock_path,
                                      @PathVariable String mock2_path)
    {
        logger.debug("tryTokenInterceptor()");
        checkActiveProfiles(env);
        if (closed_paths_REGEX.isPatternOk(mock_path)) {
            return CLOSED_AREA_MSG;
        } else {
            return OPEN_AREA_MSG;
        }
    }
}
