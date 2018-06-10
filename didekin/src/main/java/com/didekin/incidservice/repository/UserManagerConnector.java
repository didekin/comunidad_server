package com.didekin.incidservice.repository;

import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.repository.UserMockManager;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import static com.didekin.common.springprofile.Profiles.MAIL_PRE;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_LOCAL;
import static com.didekin.common.springprofile.Profiles.NGINX_JETTY_PRE;
import static com.didekin.common.springprofile.Profiles.checkActiveProfiles;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;

/**
 * User: pedro@didekin
 * Date: 27/08/17
 * Time: 14:14
 * <p>
 * This class groups the dependencies of incidencia's controller and manager on user(comu) service.
 * In the future, the UsuarioManager instance should be replaced by an http endpoint.
 */
@Service
public class UserManagerConnector {

    private static final Logger logger = LoggerFactory.getLogger(UserManagerConnector.class.getCanonicalName());
    private final UsuarioManager usuarioManager;
    private final UserMockManager userMockManager;

    @Autowired
    Environment env;

    @Autowired
    public UserManagerConnector(UsuarioManager usuarioManager)
    {
        this.usuarioManager = usuarioManager;
        userMockManager= new UserMockManager(usuarioManager);
    }

    public boolean checkAuthorityInComunidad(String userName, long comunidadId) throws ServiceException
    {
        logger.debug("checkAuthorityInComunidad()");
        return usuarioManager.completeWithUserComuRoles(userName, comunidadId).hasAdministradorAuthority();
    }

    public String checkHeaderGetUserName(String httpHeaderIn)
    {
        return usuarioManager.checkHeaderGetUserName(httpHeaderIn);
    }

    /**
     * The method checks if a user initiated an incidencia or has the authority 'adm'.
     *
     * @param userNameInSession : user in session.
     * @param incidencia        : incidencia to be modified.
     */
    boolean checkIncidModificationPower(String userNameInSession, Incidencia incidencia) throws ServiceException
    {
        logger.debug("checkIncidModificationPower()");
        return userNameInSession.equals(incidencia.getUserName()) || checkAuthorityInComunidad(userNameInSession, incidencia.getComunidadId());
    }

    public boolean checkUserInComunidad(String userName, long comunidadId) throws ServiceException
    {
        logger.debug("checkUserInComunidad()");
        if (!usuarioManager.isUserInComunidad(userName, comunidadId)) {
            throw new ServiceException(USERCOMU_WRONG_INIT);
        }
        return true;
    }

    Usuario completeUser(String userName)
    {
        logger.debug("completeUser()");
        return usuarioManager.getUserData(userName);
    }

    UsuarioComunidad completeUserAndComuRoles(String userName, long comunidadId)
    {
        logger.debug("completeWithUserComuRoles()");
        return usuarioManager.completeWithUserComuRoles(userName, comunidadId);
    }

    /**
     * Only for tests.
     */
    @Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL, MAIL_PRE})
    boolean deleteUser(String userName)
    {
        logger.debug("deleteUser()");
        checkActiveProfiles(env);
        return usuarioManager.deleteUser(userName);
    }

    // ==================================  FOR TESTS =================================

    @Profile({NGINX_JETTY_PRE, NGINX_JETTY_LOCAL})
    public String insertTokenGetHeaderStr(String userName, String appIDIn){
        checkActiveProfiles(env);
        return userMockManager.insertAuthTkGetNewAuthTkStr(userName, appIDIn);
    }
}
