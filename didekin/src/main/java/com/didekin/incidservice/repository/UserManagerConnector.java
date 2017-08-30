package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekin.userservice.repository.UsuarioManager;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Stream.of;

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

    boolean checkAuthorityInComunidad(String userName, long comunidadId) throws EntityException
    {
        logger.debug("checkAuthorityInComunidad()");
        return usuarioManager.hasAuthorityAdmInComunidad(userName, comunidadId);
    }

    /**
     * The method checks if a user initiated an incidencia or has the authority 'adm'.
     *
     * @param userNameInSession: user in session.
     * @param comunidadId:       comunidad where the incidencia belongs to.
     * @param incidUserName:     userName authoring the incidencia.
     */
    boolean checkIncidModificationPower(String userNameInSession, long comunidadId, String incidUserName) throws EntityException
    {
        logger.debug("checkIncidModificationPower()");
        return userNameInSession.equals(incidUserName) || checkAuthorityInComunidad(userNameInSession, comunidadId);
    }

    public boolean checkUserInComunidad(String userName, long comunidadId) throws EntityException
    {
        logger.debug("checkUserInComunidad()");
        if (!usuarioManager.isUserInComunidad(userName, comunidadId)) {
            throw new EntityException(USERCOMU_WRONG_INIT);
        }
        return true;
    }

    Usuario completeUser(String userName)
    {
        logger.debug("completeUser()");
        return usuarioManager.completeUser(userName);
    }

    UsuarioComunidad completeWithHighestRol(String userName, Comunidad comunidad)
    {
        logger.debug("completeWithHighestRol()");
        return usuarioManager.completeWithHighestRol(userName, comunidad.getC_Id());
    }

    public String addHighestFunctionalRol(String userName, long comunidadId) throws EntityException
    {
        logger.debug("addHighestFunctionalRol()");
        return usuarioManager.getHighestFunctionalRol(userName, comunidadId);
    }

    UsuarioComunidad getUserComunidad(String userName, long comunidadId)
    {
        logger.debug("getUserNameFromAuthentication()");
        return of(usuarioManager.getUserComuByUserAndComu(userName, comunidadId))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new EntityException(USERCOMU_WRONG_INIT));
    }
}
