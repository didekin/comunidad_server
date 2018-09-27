package com.didekin.userservice.controller;

import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.repository.UsuarioManager;
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

import static com.didekinlib.http.CommonServConstant.ACCEPT_LANGUAGE;
import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.COMUNIDAD_OLDEST_USER;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.COMUNIDAD_WRITE;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.COMUS_BY_USER;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.REG_COMU_AND_USER_AND_USERCOMU;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.REG_COMU_USERCOMU;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.REG_USERCOMU;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.REG_USER_USERCOMU;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.USERCOMUS_BY_COMU;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.USERCOMUS_BY_USER;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.USERCOMU_DELETE;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.USERCOMU_MODIFY;
import static com.didekinlib.model.usuariocomunidad.http.UsuarioComunidadServConstant.USERCOMU_READ;
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

    private final UsuarioManager usuarioManager;

    @Autowired
    public UserComuController(UsuarioManager usuarioManager)
    {
        this.usuarioManager = usuarioManager;
    }

    @RequestMapping(value = USERCOMU_DELETE + "/{comunidadId}", method = DELETE)
    public int deleteUserComu(@RequestHeader("Authorization") String authHeader,
                              @PathVariable long comunidadId) throws ServiceException
    {
        logger.debug("deleteUserComu()");
        return usuarioManager.deleteUserComunidad(
                new UsuarioComunidad.UserComuBuilder(
                        new Comunidad.ComunidadBuilder().c_id(comunidadId).build(),
                        usuarioManager.checkHeaderGetUserData(authHeader)
                ).build()
        );
    }

    @RequestMapping(value = COMUS_BY_USER, method = GET, produces = MIME_JSON)
    public List<Comunidad> getComusByUser(@RequestHeader("Authorization") String authHeader)
    {
        logger.debug("getComusByUser()");
        return usuarioManager.getComusByUser(usuarioManager.checkHeaderGetUserName(authHeader));
    }


    @RequestMapping(value = USERCOMU_READ + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public UsuarioComunidad getUserComuByUserAndComu(@RequestHeader("Authorization") String authHeader, @PathVariable long
            comunidadId) throws ServiceException
    {
        logger.debug("getUserComuByUserAndComu");
        return usuarioManager.getUserComuByUserAndComu(usuarioManager.checkHeaderGetUserName(authHeader), comunidadId);
    }

    @RequestMapping(value = COMUNIDAD_OLDEST_USER + "/{comunidadId}", method = GET)
    public boolean isOldestOrAdmonUserComu(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable long comunidadId) throws ServiceException
    {
        logger.debug("isOldestOrAdmonUserComu()");
        return usuarioManager.checkComuDataModificationPower(
                usuarioManager.checkHeaderGetUserData(authHeader),
                new Comunidad.ComunidadBuilder().c_id(comunidadId).build()
        );
    }

    @RequestMapping(value = COMUNIDAD_WRITE, method = PUT, consumes = MIME_JSON)
    public int modifyComuData(@RequestHeader("Authorization") String authHeader,
                              @RequestBody final Comunidad comunidad)
            throws ServiceException
    {
        logger.info("modifyComuData()");
        return usuarioManager.modifyComuData(
                usuarioManager.checkHeaderGetUserData(authHeader),
                comunidad);
    }

    @RequestMapping(value = USERCOMU_MODIFY, method = PUT, consumes = MIME_JSON)
    public int modifyUserComu(@RequestHeader("Authorization") String authHeader,
                              @RequestBody final UsuarioComunidad userComu) throws ServiceException
    {
        logger.debug("modifyUserComu()");
        return usuarioManager.modifyUserComu
                (
                        new UsuarioComunidad.UserComuBuilder(
                                userComu.getComunidad(),
                                usuarioManager.checkHeaderGetUserData(authHeader)
                        ).userComuRest(userComu)
                                .build()
                );
    }

    @RequestMapping(value = REG_COMU_AND_USER_AND_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regComuAndUserAndUserComu(@RequestHeader(ACCEPT_LANGUAGE) String localeToStr,
                                             @RequestBody UsuarioComunidad usuarioCom)
            throws ServiceException
    {
        logger.debug("regComuAndUserAndUserComu()");
        return usuarioManager.regComuAndUserAndUserComu(usuarioCom, localeToStr);
    }

    @RequestMapping(value = REG_COMU_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regComuAndUserComu(@RequestHeader("Authorization") String accessTk,
                                      @RequestBody UsuarioComunidad usuarioCom) throws ServiceException
    {
        logger.debug("regComuAndUserComu()");

        Usuario usuario = usuarioManager.checkHeaderGetUserData(accessTk);
        UsuarioComunidad usuarioComBis = new UsuarioComunidad.UserComuBuilder(usuarioCom.getComunidad(), usuario)
                .userComuRest(usuarioCom).build();
        return usuarioManager.regComuAndUserComu(usuarioComBis);
    }

    @RequestMapping(value = REG_USER_USERCOMU, method = POST, consumes = MIME_JSON)
    public boolean regUserAndUserComu(@RequestHeader(ACCEPT_LANGUAGE) String localeToStr,
                                      @RequestBody UsuarioComunidad userComu) throws ServiceException
    {
        logger.debug("regUserAndUserComu()");
        return usuarioManager.regUserAndUserComu(userComu, localeToStr);
    }

    @RequestMapping(value = REG_USERCOMU, method = POST, consumes = MIME_JSON)
    public int regUserComu(@RequestHeader("Authorization") String accessTk,
                           @RequestBody UsuarioComunidad usuarioComunidad) throws ServiceException
    {
        logger.debug("regUserComu()");
        Usuario usuario = usuarioManager.checkHeaderGetUserData(accessTk);
        UsuarioComunidad usuarioComBis =
                new UsuarioComunidad.UserComuBuilder(usuarioComunidad.getComunidad(), usuario)
                        .userComuRest(usuarioComunidad)
                        .build();
        return usuarioManager.regUserComu(usuarioComBis);
    }

    @RequestMapping(value = USERCOMUS_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<UsuarioComunidad> seeUserComusByComu(@RequestHeader("Authorization") String authHeader,
                                                     @PathVariable long comunidadId) throws ServiceException
    {
        logger.debug("seeUserComusByComu()");
        return usuarioManager.seeUserComusByComu(usuarioManager.checkHeaderGetUserName(authHeader), comunidadId);
    }

    @RequestMapping(value = USERCOMUS_BY_USER, produces = MIME_JSON, method = GET)
    public List<UsuarioComunidad> seeUserComusByUser(@RequestHeader("Authorization") String authHeader)
            throws ServiceException
    {
        logger.debug("seeUserComusByUser()");
        return usuarioManager.seeUserComusByUser(usuarioManager.checkHeaderGetUserName(authHeader));
    }
}
