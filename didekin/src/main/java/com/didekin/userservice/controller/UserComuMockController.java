package com.didekin.userservice.controller;

import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.common.repository.EntityException;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UsuarioManagerIf;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.springprofile.Profiles.checkActiveProfiles;
import static com.didekinlib.http.CommonServConstant.FORM_URLENCODED;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.UsuarioServConstant.OPEN;
import static com.didekinlib.http.UsuarioServConstant.USER_PARAM;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: pedro@didekin
 * Date: 21/11/16
 * Time: 09:46
 */
@RestController
@Profile({NGINX_JETTY_LOCAL, NGINX_JETTY_PRE})
public class UserComuMockController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(UserComuMockController.class.getCanonicalName());

    private final UserMockManager userMockManager;
    private final UsuarioManagerIf usuarioManager;
    private static final String mockPath = OPEN + "/mock";
    static final String regComu_User_UserComu = mockPath + "/reg_comu_user_usercomu";
    static final String regUser_UserComu = mockPath + "/reg_user_usercomu";
    static final String user_delete = mockPath + "/user_delete";

    @Autowired
    Environment env;

    @Autowired
    public UserComuMockController(UserMockManager userMockManager, UsuarioManagerIf usuarioManager)
    {
        this.userMockManager = userMockManager;
        this.usuarioManager = usuarioManager;
    }

    @RequestMapping(value = regComu_User_UserComu, method = POST, consumes = MIME_JSON)
    public boolean regComuAndUserAndUserComu(@RequestBody UsuarioComunidad usuarioCom)
            throws EntityException
    {
        logger.debug("regComuAndUserAndUserComu()");
        checkActiveProfiles(env);
        return userMockManager.regComuAndUserAndUserComu(usuarioCom);
    }

    @RequestMapping(value = regUser_UserComu, method = POST, consumes = MIME_JSON)
    public boolean regUserAndUserComu(@RequestBody UsuarioComunidad userComu) throws EntityException
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
}
