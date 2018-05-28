package com.didekin.userservice.controller;

import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.gcm.GcmUserComuServiceIf;
import com.didekin.userservice.mail.UsuarioMailService;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.http.usuario.AuthHeader.AuthHeaderBuilder;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.didekinlib.http.CommonServConstant.ACCEPT_LANGUAGE;
import static com.didekinlib.http.CommonServConstant.FORM_URLENCODED;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.usuario.UsuarioServConstant.APP_ID_PARAM;
import static com.didekinlib.http.usuario.UsuarioServConstant.LOGIN;
import static com.didekinlib.http.usuario.UsuarioServConstant.PASSWORD_MODIFY;
import static com.didekinlib.http.usuario.UsuarioServConstant.PASSWORD_SEND;
import static com.didekinlib.http.usuario.UsuarioServConstant.PSWD_PARAM;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_DELETE;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_PARAM;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_READ;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_WRITE;
import static com.didekinlib.http.usuario.UsuarioServConstant.USER_WRITE_GCM_TOKEN;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


/**
 * User: pedro
 * Date: 02/04/15
 * Time: 11:22
 */
@SuppressWarnings({"UnusedParameters"})
@RestController
public class UsuarioController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class.getCanonicalName());

    private final UsuarioManager usuarioManager;

    @Autowired
    public UsuarioController(UsuarioMailService usuarioMailService, GcmUserComuServiceIf gcmUserComuServiceIf, UsuarioManager usuarioManager)
    {
        this.usuarioManager = usuarioManager;
    }

    @RequestMapping(value = USER_DELETE, method = DELETE, produces = MIME_JSON)
    public boolean deleteUser(@RequestHeader("Authorization") String accessToken) throws ServiceException
    {
        logger.debug("deleteUser()");
        return usuarioManager.deleteUser(new AuthHeaderBuilder(accessToken).build().getUserName());
    }

    @RequestMapping(value = USER_READ, method = GET, produces = MIME_JSON)
    public Usuario getUserData(@RequestHeader("Authorization") String accessToken) throws ServiceException
    {
        logger.debug("getUserData()");
        Usuario usuarioDb = usuarioManager.getUserDataByName(new AuthHeaderBuilder(accessToken).build().getUserName());
        return new Usuario.UsuarioBuilder()
                .userName(usuarioDb.getUserName())
                .alias(usuarioDb.getAlias())
                .gcmToken(usuarioDb.getGcmToken())
                .uId(usuarioDb.getuId()).build();
    }

    @RequestMapping(value = LOGIN, method = POST, consumes = FORM_URLENCODED)
    public String login(@RequestParam(USER_PARAM) String userName,
                        @RequestParam(PSWD_PARAM) String password,
                        @RequestParam(APP_ID_PARAM) String appID) throws ServiceException
    {
        logger.debug("login()");
        return usuarioManager.login(new Usuario.UsuarioBuilder().userName(userName).password(password).gcmToken(appID).build());
    }

    @RequestMapping(value = USER_WRITE_GCM_TOKEN, method = POST, consumes = FORM_URLENCODED)
    public int modifyUserGcmTokens(@RequestHeader("Authorization") String accessToken,
                                   @RequestParam(APP_ID_PARAM) final String gcmToken) throws ServiceException
    {
        logger.debug("modifyUserGcmToken()");
        return usuarioManager.modifyUserGcmToken(new AuthHeaderBuilder(accessToken).build().getUserName(), gcmToken);
    }

    @RequestMapping(value = USER_WRITE, method = PUT, consumes = MIME_JSON)
    public int modifyUser(@RequestHeader(ACCEPT_LANGUAGE) String localeToStr,
                          @RequestHeader("Authorization") String accessToken,
                          @RequestBody final Usuario newUsuario)
            throws ServiceException
    {
        logger.debug("modifyUser()");
        return usuarioManager.modifyUser(newUsuario, new AuthHeaderBuilder(accessToken).build().getUserName(), localeToStr);
    }

    @RequestMapping(value = PASSWORD_MODIFY, method = POST, consumes = FORM_URLENCODED)
    public int passwordChange(@RequestHeader("Authorization") String accessToken,
                              @RequestParam(PSWD_PARAM) String newPassword) throws ServiceException
    {
        logger.debug("passwordChange()");
        return usuarioManager.passwordChange(new AuthHeaderBuilder(accessToken).build().getUserName(), newPassword);
    }

    @RequestMapping(value = PASSWORD_SEND, method = POST, consumes = FORM_URLENCODED)
    public boolean passwordSend(@RequestHeader(ACCEPT_LANGUAGE) String localeToStr,
                                @RequestParam(USER_PARAM) String userName) throws ServiceException, MailException
    {
        logger.debug("passwordSend()");
        return usuarioManager.passwordSend(userName, localeToStr);
    }
}
