package com.didekin.userservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.userservice.gcm.GcmUserComuServiceIf;
import com.didekin.userservice.mail.UsuarioMailService;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.http.ErrorBean;
import com.didekinlib.model.usuario.GcmTokenWrapper;
import com.didekinlib.model.usuario.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.didekinlib.http.CommonServConstant.FORM_URLENCODED;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.UsuarioServConstant.ACCESS_TOKEN_DELETE;
import static com.didekinlib.http.UsuarioServConstant.GCM_TOKEN_PARAM;
import static com.didekinlib.http.UsuarioServConstant.LOGIN;
import static com.didekinlib.http.UsuarioServConstant.PASSWORD_MODIFY;
import static com.didekinlib.http.UsuarioServConstant.PASSWORD_SEND;
import static com.didekinlib.http.UsuarioServConstant.PSWD_PARAM;
import static com.didekinlib.http.UsuarioServConstant.USER_DELETE;
import static com.didekinlib.http.UsuarioServConstant.USER_PARAM;
import static com.didekinlib.http.UsuarioServConstant.USER_READ;
import static com.didekinlib.http.UsuarioServConstant.USER_READ_GCM_TOKEN;
import static com.didekinlib.http.UsuarioServConstant.USER_WRITE;
import static com.didekinlib.http.UsuarioServConstant.USER_WRITE_GCM_TOKEN;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.PASSWORD_NOT_SENT;
import static com.didekinlib.model.usuario.UsuarioExceptionMsg.USER_NAME_NOT_FOUND;
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

    private final UsuarioMailService usuarioMailService;

    private final UsuarioServiceIf usuarioService;

    @Autowired
    public UsuarioController(UsuarioMailService usuarioMailService, GcmUserComuServiceIf gcmUserComuServiceIf, UsuarioServiceIf usuarioService)
    {
        this.usuarioMailService = usuarioMailService;
        this.usuarioService = usuarioService;
    }

    @RequestMapping(value = ACCESS_TOKEN_DELETE + "/{oldTk}", method = DELETE)
    public boolean deleteAccessToken(@RequestHeader("Authorization") String accessToken,
                                     @PathVariable String oldTk) throws EntityException
    {
        logger.debug("deleteAccessToken()");
        return usuarioService.deleteAccessToken(oldTk);
    }

    @RequestMapping(value = USER_DELETE, method = DELETE, produces = MIME_JSON)
    public boolean deleteUser(@RequestHeader("Authorization") String accessToken) throws EntityException
    {
        logger.debug("deleteUser()");
        return usuarioService.deleteUser(getUserNameFromAuthentication());
    }

    @RequestMapping(value = USER_READ_GCM_TOKEN, method = GET, produces = MIME_JSON)
    public GcmTokenWrapper getGcmToken(@RequestHeader("Authorization") String accessToken) throws EntityException
    {
        logger.debug("getGcmToken()");
        return new GcmTokenWrapper(getGcmToken(getUserFromDb(usuarioService).getuId()));
    }

    @RequestMapping(value = USER_READ, method = GET, produces = MIME_JSON)
    public Usuario getUserData(@RequestHeader("Authorization") String accessToken) throws EntityException
    {
        logger.debug("getUserData()");
        Usuario usuarioDb = getUserFromDb(usuarioService);
        return new Usuario.UsuarioBuilder()
                .userName(usuarioDb.getUserName())
                .alias(usuarioDb.getAlias())
                .uId(usuarioDb.getuId()).build();
    }

    @RequestMapping(value = LOGIN, method = POST, consumes = FORM_URLENCODED)
    public boolean login(@RequestParam(USER_PARAM) String userName, @RequestParam(PSWD_PARAM) String password)
            throws EntityException
    {
        logger.debug("login()");
        return usuarioService.login(new Usuario.UsuarioBuilder().userName(userName).password(password).build());
    }

    @RequestMapping(value = USER_WRITE_GCM_TOKEN, method = POST, consumes = FORM_URLENCODED)
    public int modifyUserGcmTokens(@RequestHeader("Authorization") String accessToken,
                                   @RequestParam(GCM_TOKEN_PARAM) final String gcmToken) throws EntityException
    {
        logger.debug("modifyUserGcmToken()");
        return internalModifyUserGcmToken(getUserNameFromAuthentication(), gcmToken);
    }

    @RequestMapping(value = USER_WRITE, method = PUT, consumes = MIME_JSON)
    public int modifyUser(@RequestHeader("Authorization") String accessToken, @RequestBody final Usuario newUsuario)
            throws EntityException
    {
        logger.debug("modifyUser()");
        return usuarioService.modifyUser(newUsuario, getUserNameFromAuthentication());
    }

    @RequestMapping(value = PASSWORD_MODIFY, method = POST, consumes = FORM_URLENCODED)
    public int passwordChange(@RequestHeader("Authorization") String accessToken,
                              @RequestParam(PSWD_PARAM) String newPassword) throws EntityException
    {
        logger.debug("passwordChangeWithName()");
        return usuarioService.passwordChangeWithName(getUserNameFromAuthentication(), newPassword);
        // TODO: notificar por mail que el password ha sido cambiado.
    }

    @RequestMapping(value = PASSWORD_SEND, method = POST, consumes = FORM_URLENCODED)
    public boolean passwordSend(@RequestParam(USER_PARAM) String userName) throws EntityException, MailException
    {
        logger.debug("passwordSend()");
        Usuario usuario = usuarioService.getUserByUserName(userName);
        final String newPswd = usuarioService.makeNewPassword(usuario);
        if (!newPswd.isEmpty()){
            try{
                usuarioMailService.sendNewPswd(usuario, usuarioService.makeNewPassword(usuario));  // TODO: hacer asíncrono con Observable.
            }catch (MailException e){
                throw new EntityException(PASSWORD_NOT_SENT);
            }
        }
        return usuarioService.passwordChangeWithUser(usuario, newPswd) == 1;
    }

//    ............................ HANDLING EXCEPTIONS ................................

    @ExceptionHandler({UsernameNotFoundException.class})
    public ErrorBean userNameExceptionHandling(EntityException e) throws EntityException
    {
        logger.info("userNameExceptionHandling()");
        throw new EntityException(USER_NAME_NOT_FOUND);
    }

    //  =========================== METHODS FOR INTERNAL SERVICES =========================

    public Usuario completeUser(String userName) throws EntityException
    {
        logger.debug("completeUser()");
        return usuarioService.completeUser(userName);
    }

    String getGcmToken(long usuarioId)
    {
        logger.debug("getGcmToken(Usuario usuario)");
        return usuarioService.getGcmToken(usuarioId);
    }

    int internalModifyUserGcmToken(String userName, String gcmToken) throws EntityException
    {
        logger.debug("modifyUserGcmToken(String gcmToken)");
        Usuario usuario = new Usuario.UsuarioBuilder().uId(completeUser(userName).getuId()).gcmToken(gcmToken).build();
        return usuarioService.modifyUserGcmToken(usuario);
    }
}
