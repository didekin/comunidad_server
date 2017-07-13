package com.didekin.userservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.userservice.repository.UsuarioServiceIf;
import com.didekinlib.model.comunidad.Comunidad;

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
import static com.didekinlib.http.ComunidadServConstant.COMUNIDAD_READ;
import static com.didekinlib.http.ComunidadServConstant.COMUNIDAD_SEARCH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: pedro@didekin
 * Date: 21/11/16
 * Time: 09:46
 */
@SuppressWarnings("UnusedParameters")
@RestController
public class ComunidadController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(ComunidadController.class.getCanonicalName());
    private final UsuarioServiceIf usuarioService;

    @Autowired
    public ComunidadController(UsuarioServiceIf usuarioService)
    {
        this.usuarioService = usuarioService;
    }

    @RequestMapping(value = COMUNIDAD_READ + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public Comunidad getComuData(@RequestHeader("Authorization") String accessToken, @PathVariable long comunidadId)
            throws EntityException
    {
        logger.debug("getComunidadById()");
        return usuarioService.getComunidadById(getUserFromDb(usuarioService), comunidadId);
    }

    @RequestMapping(value = COMUNIDAD_SEARCH, method = POST, consumes = MIME_JSON, produces = MIME_JSON)
    public List<Comunidad> searchComunidades(@RequestBody Comunidad comunidad)
    {
        logger.debug("searchComunidadOne()");
        return usuarioService.searchComunidades(comunidad);
    }
}
