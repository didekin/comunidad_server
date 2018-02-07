package com.didekin.incidservice.repository;

import com.didekin.common.repository.EntityException;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;

/**
 * User: pedro@didekin
 * Date: 27/08/17
 * Time: 14:14
 */
@Service
public class UserManagerConnector {

    private static final Logger logger = LoggerFactory.getLogger(UserManagerConnector.class.getCanonicalName());

    private final UsuarioManager usuarioManager;

    @Autowired
    public UserManagerConnector(UsuarioManager usuarioManager)
    {
        this.usuarioManager = usuarioManager;
    }

    public boolean checkAuthorityInComunidad(String userName, long comunidadId) throws EntityException
    {
        logger.debug("checkAuthorityInComunidad()");
        return usuarioManager.completeWithUserComuRoles(userName, comunidadId).hasAdministradorAuthority();
    }

    /**
     * The method checks if a user initiated an incidencia or has the authority 'adm'.
     *  @param userNameInSession : user in session.
     * @param incidencia : incidencia to be modified.
     */
    boolean checkIncidModificationPower(String userNameInSession, Incidencia incidencia) throws EntityException
    {
        logger.debug("checkIncidModificationPower()");
        return userNameInSession.equals(incidencia.getUserName()) || checkAuthorityInComunidad(userNameInSession, incidencia.getComunidadId());
    }

    public boolean checkUserInComunidad(String userName, long comunidadId) throws EntityException
    {
        logger.debug("checkUserInComunidad()");
        if (!usuarioManager.getUsuarioDao().isUserInComunidad(userName, comunidadId)) {
            throw new EntityException(USERCOMU_WRONG_INIT);
        }
        return true;
    }

    Usuario completeUser(String userName)
    {
        logger.debug("completeUser()");
        return usuarioManager.completeUser(userName);
    }

    UsuarioComunidad completeUserAndComuRoles(String userName, long comunidadId)
    {
        logger.debug("completeWithUserComuRoles()");
        return usuarioManager.completeWithUserComuRoles(userName, comunidadId);
    }
}
