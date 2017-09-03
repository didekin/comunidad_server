package com.didekin.incidservice.controller;

import com.didekin.common.EntityException;
import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.incidservice.repository.IncidenciaManagerIf;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.IncidServConstant.CLOSE_INCIDENCIA;
import static com.didekinlib.http.IncidServConstant.DELETE_INCID;
import static com.didekinlib.http.IncidServConstant.MOD_INCID_IMPORTANCIA;
import static com.didekinlib.http.IncidServConstant.MOD_RESOLUCION;
import static com.didekinlib.http.IncidServConstant.REG_INCID_COMMENT;
import static com.didekinlib.http.IncidServConstant.REG_INCID_IMPORTANCIA;
import static com.didekinlib.http.IncidServConstant.REG_RESOLUCION;
import static com.didekinlib.http.IncidServConstant.SEE_INCIDS_CLOSED_BY_COMU;
import static com.didekinlib.http.IncidServConstant.SEE_INCIDS_OPEN_BY_COMU;
import static com.didekinlib.http.IncidServConstant.SEE_INCID_COMMENTS;
import static com.didekinlib.http.IncidServConstant.SEE_INCID_IMPORTANCIA;
import static com.didekinlib.http.IncidServConstant.SEE_RESOLUCION;
import static com.didekinlib.http.IncidServConstant.SEE_USERCOMUS_IMPORTANCIA;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


/**
 * User: pedro
 * Date: 02/04/15
 * Time: 11:22
 */
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "unused"})
@RestController
class IncidenciaController extends AppControllerAbstract {

    private static final Logger logger = LoggerFactory.getLogger(IncidenciaController.class.getCanonicalName());

    @Autowired
    private IncidenciaManagerIf incidenciaManager;

    @RequestMapping(value = CLOSE_INCIDENCIA, method = PUT, consumes = MIME_JSON)
    int closeIncidencia(@RequestHeader("Authorization") String accessToken,
                        @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.debug("closeIncidencia()");
        return incidenciaManager.closeIncidencia(getUserNameFromAuthentication(), resolucion);
    }

    @RequestMapping(value = DELETE_INCID + "/{incidenciaId}", method = DELETE)
    public int deleteIncidencia(@RequestHeader("Authorization") String accessToken,
                                @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("deleteIncidencia()");
        return incidenciaManager.deleteIncidencia(getUserNameFromAuthentication(), incidenciaId);
    }

    @RequestMapping(value = MOD_INCID_IMPORTANCIA, method = PUT, consumes = MIME_JSON)
    public int modifyIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                      @RequestBody IncidImportancia incidImportancia) throws EntityException
    {
        logger.error("modifyIncidImportancia()");
        return incidenciaManager.modifyIncidImportancia(getUserNameFromAuthentication(), incidImportancia);
    }

    @RequestMapping(value = MOD_RESOLUCION, method = PUT, consumes = MIME_JSON)
    public int modifyResolucion(@RequestHeader("Authorization") String accessToken,
                                @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.error("modifyResolucion()");
        return incidenciaManager.modifyResolucion(getUserNameFromAuthentication(), resolucion);
    }

    @RequestMapping(value = REG_INCID_COMMENT, method = POST, consumes = MIME_JSON)
    public int regIncidComment(@RequestHeader("Authorization") String accessToken,
                               @RequestBody final IncidComment comment) throws EntityException
    {
        logger.debug("regIncidComment()");
        return incidenciaManager.regIncidComment(getUserNameFromAuthentication(), comment);
    }

    @RequestMapping(value = REG_INCID_IMPORTANCIA, method = RequestMethod.POST, consumes = MIME_JSON)
    public int regIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                   @RequestBody IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("regIncidImportancia()");
        return incidenciaManager.regIncidImportancia(getUserNameFromAuthentication(), incidImportancia);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @RequestMapping(value = REG_RESOLUCION, method = POST, consumes = MIME_JSON)
    public int regResolucion(@RequestHeader("Authorization") String accessToken,
                             @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.debug("regResolucion()");
        return incidenciaManager.regResolucion(getUserNameFromAuthentication(), resolucion);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. The incidencia can be OPEN or CLOSED.
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user doesn't belong to the comunidad of the incidencia.
     * @throws EntityException INCIDENCIA_NOT_FOUND, if the incidencia doesn't exist.
     */
    @RequestMapping(value = SEE_INCID_COMMENTS + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    List<IncidComment> seeCommentsByIncid(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeCommentsByIncid()");
        final Incidencia incidenciaIn = incidenciaManager.seeIncidenciaById(incidenciaId);
        final String userName = getUserNameFromAuthentication();
        incidenciaManager.getUsuarioConnector().checkUserInComunidad(userName, incidenciaIn.getComunidad().getC_Id());

        try {
            return incidenciaManager.seeCommentsByIncid(incidenciaId);
        } catch (EntityException e) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
    }

    @RequestMapping(value = SEE_INCID_IMPORTANCIA + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    public IncidAndResolBundle seeIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                                   @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportanciaByUser");
        return incidenciaManager.seeIncidImportanciaByUser(getUserNameFromAuthentication(), incidenciaId);
    }

    @RequestMapping(value = SEE_INCIDS_CLOSED_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsClosedByComu(@RequestHeader("Authorization") String accessToken,
                                                      @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("seeIncidsClosedByComu()");
        return incidenciaManager.seeIncidsClosedByComu(getUserNameFromAuthentication(), comunidadId);
    }

    @RequestMapping(value = SEE_INCIDS_OPEN_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsOpenByComu(@RequestHeader("Authorization") String accessToken,
                                                    @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("seeIncidsOpenByComu()");
        return incidenciaManager.seeIncidsOpenByComu(getUserNameFromAuthentication(),comunidadId);
    }

    @RequestMapping(value = SEE_RESOLUCION + "/{resolucionId}", produces = MIME_JSON, method = GET)
    public Resolucion seeResolucion(@RequestHeader("Authorization") String accessToken,
                                    @PathVariable long resolucionId) throws EntityException
    {
        logger.debug("seeResolucion()");
        return incidenciaManager.seeResolucion(getUserNameFromAuthentication(), resolucionId);
    }

    @RequestMapping(value = SEE_USERCOMUS_IMPORTANCIA + "/{incidenciaId}", produces = MIME_JSON, method = GET)
    List<ImportanciaUser> seeUserComusImportancia(@RequestHeader("Authorization") String accessToken,
                                                  @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeUserComusImportancia()");
        return incidenciaManager.seeUserComusImportancia(getUserNameFromAuthentication(), incidenciaId);
    }
}