package com.didekin.userservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.userservice.repository.UsuarioManagerIf;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.UsuarioComunidadServConstant.COMUNIDAD_OLDEST_USER;
import static com.didekinlib.http.UsuarioComunidadServConstant.COMUNIDAD_WRITE;
import static com.didekinlib.http.UsuarioComunidadServConstant.COMUS_BY_USER;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_COMU_AND_USER_AND_USERCOMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_COMU_USERCOMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_USERCOMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.REG_USER_USERCOMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.USERCOMUS_BY_COMU;
import static com.didekinlib.http.UsuarioComunidadServConstant.USERCOMUS_BY_USER;
import static com.didekinlib.http.UsuarioComunidadServConstant.USERCOMU_DELETE;
import static com.didekinlib.http.UsuarioComunidadServConstant.USERCOMU_MODIFY;
import static com.didekinlib.http.UsuarioComunidadServConstant.USERCOMU_READ;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * User: pedro@didekin
 * Date: 21/11/16
 * Time: 09:46
 */
@SuppressWarnings("unused")
@RestController
public class UserComuController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(UserComuController.class.getCanonicalName());

    private final UsuarioManagerIf usuarioManager;

    @Autowired
    public UserComuController(UsuarioManagerIf usuarioManager)
    {
        this.usuarioManager = usuarioManager;
    }

    @RequestMapping(value = USERCOMU_DELETE + "/{comunidadId}", method = DELETE)
    public int deleteUserComu(@RequestHeader("Authorization") String accessToken,
                              @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("deleteUserComu()");
        return usuarioManager.deleteUserComunidad(
                new UsuarioComunidad
                        .UserComuBuilder(
                        new Comunidad.ComunidadBuilder().c_id(comunidadId).build(),
                        getUserFromDb(usuarioManager))
                        .build()
        );
    }

    @RequestMapping(value = COMUS_BY_USER, method = GET, produces = MIME_JSON)
    public List<Comunidad> getComusByUser(@RequestHeader("Authorization") String accessToken)
    {
        logger.debug("getComusByUser()");
        return usuarioManager.getComusByUser(getUserNameFromAuthentication());
    }


    @RequestMapping(value = USERCOMU_READ + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public UsuarioComunidad getUserComuByUserAndComu(@RequestHeader("Authorization") String accessToken, @PathVariable long
            comunidadId) throws EntityException
    {
        logger.debug("getUserComuByUserAndComu");
        return usuarioManager.getUserComuByUserAndComu(getUserNameFromAuthentication(), comunidadId);
    }

    @RequestMapping(value = COMUNIDAD_OLDEST_USER + "/{comunidadId}", method = GET)
    public boolean isOldestOrAdmonUserComu(@RequestHeader("Authorization") String accessToken,
                                           @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("isOldestOrAdmonUserComu()");
        return usuarioManager.isOldestUserComu(getUserFromDb(usuarioManager), comunidadId)
                || usuarioManager.completeWithUserComuRoles(getUserNameFromAuthentication(), comunidadId).hasAdministradorAuthority();
    }

    @RequestMapping(value = COMUNIDAD_WRITE, method = PUT, consumes = MIME_JSON)
    public int modifyComuData(@RequestHeader("Authorization") String accessToken,
                              @RequestBody final Comunidad comunidad)
            throws EntityException
    {
        logger.info("modifyComuData()");
        return usuarioManager.modifyComuData(getUserFromDb(usuarioManager), comunidad);
    }

    @RequestMapping(value = USERCOMU_MODIFY, method = PUT, consumes = MIME_JSON)
    public int modifyUserComu(@RequestHeader("Authorization") String accessToken,
                              @RequestBody final UsuarioComunidad userComu) throws EntityException
    {
        logger.debug("modifyUserComu()");
        return usuarioManager.modifyUserComu(new UsuarioComunidad
                .UserComuBuilder(userComu.getComunidad(), getUserFromDb(usuarioManager)) // agika
                .userComuRest(userComu)
                .build()
        );
    }

    @RequestMapping(value = REG_COMU_AND_USER_AND_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regComuAndUserAndUserComu(@RequestBody UsuarioComunidad usuarioCom)
            throws EntityException
    {
        logger.debug("regComuAndUserAndUserComu()");
        return usuarioManager.regComuAndUserAndUserComu(usuarioCom);
        // TODO: hay que controlar que no se dan de alta dos administradores o dos presidentes.
        // TODO: si la comunidad ya existe y el userComu no, hacer un regUserAndUserComu.
        // TODO: si el userComu existe y la comunidad no, hacer un regComuAndUserComu.
        // TODO: si existen ambos, pero el userComu no pertenece a la comunidad, hacer un RegUserComu.

        // TODO: algo similar hay que hacer en el resto de acciones de registro.
    }

    @RequestMapping(value = REG_COMU_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regComuAndUserComu(@RequestHeader("Authorization") String headerAccessToken,
                                      @RequestBody UsuarioComunidad usuarioCom) throws EntityException
    {
        logger.debug("regComuAndUserComu()");

        Usuario usuario = getUserFromDb(usuarioManager);
        UsuarioComunidad usuarioComBis = new UsuarioComunidad.UserComuBuilder(usuarioCom.getComunidad(), usuario)
                .userComuRest(usuarioCom).build();
        return usuarioManager.regComuAndUserComu(usuarioComBis);
    }

    @RequestMapping(value = REG_USER_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regUserAndUserComu(@RequestBody UsuarioComunidad userComu) throws EntityException
    {
        logger.debug("regUserAndUserComu()");
        return usuarioManager.regUserAndUserComu(userComu);
//         TODO: notificación de alta de usuario al resto de la comunidad.
    }

    @RequestMapping(value = REG_USERCOMU, method = POST, consumes = MIME_JSON)
    public int regUserComu(@RequestHeader("Authorization") String headerAccessToken,
                           @RequestBody UsuarioComunidad usuarioComunidad) throws EntityException
    {
        logger.debug("regUserComu()");
        Usuario usuario = getUserFromDb(usuarioManager);
        UsuarioComunidad usuarioComBis = new UsuarioComunidad.UserComuBuilder(usuarioComunidad.getComunidad(), usuario)
                .userComuRest(usuarioComunidad).build();
        return usuarioManager.regUserComu(usuarioComBis);
//         TODO: notificación de alta de usuario al resto de la comunidad.
    }

    @RequestMapping(value = USERCOMUS_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<UsuarioComunidad> seeUserComusByComu(@RequestHeader("Authorization") String headerAccessToken,
                                                     @PathVariable long comunidadId)
    {
        logger.debug("seeUserComusByComu()");
        return usuarioManager.seeUserComusByComu(comunidadId);
    }

    @RequestMapping(value = USERCOMUS_BY_USER, produces = MIME_JSON, method = GET)
    public List<UsuarioComunidad> seeUserComusByUser(@RequestHeader("Authorization") String accessToken)
            throws EntityException
    {
        logger.debug("seeUserComusByUser()");
        return usuarioManager.seeUserComusByUser(getUserFromDb(usuarioManager).getUserName());
    }
}
