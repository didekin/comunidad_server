package com.didekin.incidservice.controller;

import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.common.repository.ServiceException;
import com.didekin.incidservice.repository.IncidenciaManager;
import com.didekin.incidservice.repository.UserManagerConnector;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
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
import static com.didekinlib.http.incidencia.IncidServConstant.CLOSE_INCIDENCIA;
import static com.didekinlib.http.incidencia.IncidServConstant.DELETE_INCID;
import static com.didekinlib.http.incidencia.IncidServConstant.MOD_INCID_IMPORTANCIA;
import static com.didekinlib.http.incidencia.IncidServConstant.MOD_RESOLUCION;
import static com.didekinlib.http.incidencia.IncidServConstant.REG_INCID_COMMENT;
import static com.didekinlib.http.incidencia.IncidServConstant.REG_INCID_IMPORTANCIA;
import static com.didekinlib.http.incidencia.IncidServConstant.REG_RESOLUCION;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_INCIDS_CLOSED_BY_COMU;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_INCIDS_OPEN_BY_COMU;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_INCID_COMMENTS;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_INCID_IMPORTANCIA;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_RESOLUCION;
import static com.didekinlib.http.incidencia.IncidServConstant.SEE_USERCOMUS_IMPORTANCIA;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
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

    private final IncidenciaManager incidenciaManager;

    @Autowired
    public IncidenciaController(IncidenciaManager incidenciaManager, UserManagerConnector userConnector)
    {
        this.incidenciaManager = incidenciaManager;
    }

    private UserManagerConnector getUsuarioConnector()
    {
        return incidenciaManager.getUsuarioConnector();
    }

    @RequestMapping(value = CLOSE_INCIDENCIA, method = PUT, consumes = MIME_JSON)
    int closeIncidencia(@RequestHeader("Authorization") String accessToken,
                        @RequestBody Resolucion resolucion) throws ServiceException
    {
        logger.debug("closeIncidencia()");
        return incidenciaManager.closeIncidencia(getUsuarioConnector().checkHeaderGetUserName(accessToken), resolucion);
    }

    @RequestMapping(value = DELETE_INCID + "/{incidenciaId}", method = DELETE)
    public int deleteIncidencia(@RequestHeader("Authorization") String accessToken,
                                @PathVariable long incidenciaId) throws ServiceException
    {
        logger.debug("deleteIncidencia()");
        return incidenciaManager.deleteIncidencia(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidenciaId);
    }

    @RequestMapping(value = MOD_INCID_IMPORTANCIA, method = PUT, consumes = MIME_JSON)
    public int modifyIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                      @RequestBody IncidImportancia incidImportancia) throws ServiceException
    {
        logger.error("modifyIncidImportancia()");
        return incidenciaManager.modifyIncidImportancia(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidImportancia);
    }

    @RequestMapping(value = MOD_RESOLUCION, method = PUT, consumes = MIME_JSON)
    public int modifyResolucion(@RequestHeader("Authorization") String accessToken,
                                @RequestBody Resolucion resolucion) throws ServiceException
    {
        logger.error("modifyResolucion()");
        return incidenciaManager.modifyResolucion(getUsuarioConnector().checkHeaderGetUserName(accessToken), resolucion);
    }

    @RequestMapping(value = REG_INCID_COMMENT, method = POST, consumes = MIME_JSON)
    public int regIncidComment(@RequestHeader("Authorization") String accessToken,
                               @RequestBody final IncidComment comment) throws ServiceException
    {
        logger.debug("regIncidComment()");
        return incidenciaManager.regIncidComment(getUsuarioConnector().checkHeaderGetUserName(accessToken), comment);
    }

    @RequestMapping(value = REG_INCID_IMPORTANCIA, method = RequestMethod.POST, consumes = MIME_JSON)
    public int regIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                   @RequestBody IncidImportancia incidImportancia) throws ServiceException
    {
        logger.debug("regIncidImportancia()");
        return incidenciaManager.regIncidImportancia(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidImportancia);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @RequestMapping(value = REG_RESOLUCION, method = POST, consumes = MIME_JSON)
    public int regResolucion(@RequestHeader("Authorization") String accessToken,
                             @RequestBody Resolucion resolucion) throws ServiceException
    {
        logger.debug("regResolucion()");
        return incidenciaManager.regResolucion(getUsuarioConnector().checkHeaderGetUserName(accessToken), resolucion);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. The incidencia can be OPEN or CLOSED.
     *
     * @throws ServiceException USERCOMU_WRONG_INIT, if the user doesn't belong to the comunidad of the incidencia.
     * @throws ServiceException INCIDENCIA_NOT_FOUND, if the incidencia doesn't exist.
     */
    @RequestMapping(value = SEE_INCID_COMMENTS + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    List<IncidComment> seeCommentsByIncid(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable long incidenciaId) throws ServiceException
    {
        logger.debug("seeCommentsByIncid()");
        incidenciaManager.getUsuarioConnector().checkUserInComunidad(
                getUsuarioConnector().checkHeaderGetUserName(accessToken),
                incidenciaManager.seeIncidenciaById(incidenciaId).getComunidad().getC_Id()
        );

        try {
            return incidenciaManager.seeCommentsByIncid(incidenciaId);
        } catch (ServiceException e) {
            throw new ServiceException(INCIDENCIA_NOT_FOUND);
        }
    }

    @RequestMapping(value = SEE_INCID_IMPORTANCIA + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    public IncidAndResolBundle seeIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                                   @PathVariable long incidenciaId) throws ServiceException
    {
        logger.debug("seeIncidImportanciaByUser");
        return incidenciaManager.seeIncidImportanciaByUser(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidenciaId);
    }

    @RequestMapping(value = SEE_INCIDS_CLOSED_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsClosedByComu(@RequestHeader("Authorization") String accessToken,
                                                      @PathVariable long comunidadId) throws ServiceException
    {
        logger.debug("seeIncidsClosedByComu()");
        return incidenciaManager.seeIncidsClosedByComu(getUsuarioConnector().checkHeaderGetUserName(accessToken), comunidadId);
    }

    @RequestMapping(value = SEE_INCIDS_OPEN_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsOpenByComu(@RequestHeader("Authorization") String accessToken,
                                                    @PathVariable long comunidadId) throws ServiceException
    {
        logger.debug("seeIncidsOpenByComu()");
        return incidenciaManager.seeIncidsOpenByComu(getUsuarioConnector().checkHeaderGetUserName(accessToken), comunidadId);
    }

    @RequestMapping(value = SEE_RESOLUCION + "/{incidenciaId}", produces = MIME_JSON, method = GET)
    public Resolucion seeResolucion(@RequestHeader("Authorization") String accessToken,
                                    @PathVariable long incidenciaId) throws ServiceException
    {
        logger.debug("seeResolucion()");
        return incidenciaManager.seeResolucion(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidenciaId);
    }

    @RequestMapping(value = SEE_USERCOMUS_IMPORTANCIA + "/{incidenciaId}", produces = MIME_JSON, method = GET)
    List<ImportanciaUser> seeUserComusImportancia(@RequestHeader("Authorization") String accessToken,
                                                  @PathVariable long incidenciaId) throws ServiceException
    {
        logger.debug("seeUserComusImportancia()");
        return incidenciaManager.seeUserComusImportancia(getUsuarioConnector().checkHeaderGetUserName(accessToken), incidenciaId);
    }
}