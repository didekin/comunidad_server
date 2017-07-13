package com.didekin.incidservice.controller;
import com.didekin.userservice.gcm.GcmUserComuServiceIf;
import com.didekin.common.EntityException;
import com.didekin.common.controller.AppControllerAbstract;
import com.didekin.incidservice.repository.IncidenciaRepoServiceIf;
import com.didekin.userservice.controller.UserComuController;
import com.didekin.userservice.controller.UsuarioController;
import com.didekinlib.gcm.model.incidservice.GcmIncidRequestData;
import com.didekinlib.model.common.dominio.WrapperCounter;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
import com.didekinlib.model.incidencia.dominio.IncidAndResolBundle;
import com.didekinlib.model.incidencia.dominio.IncidComment;
import com.didekinlib.model.incidencia.dominio.IncidImportancia;
import com.didekinlib.model.incidencia.dominio.Incidencia;
import com.didekinlib.model.incidencia.dominio.IncidenciaUser;
import com.didekinlib.model.incidencia.dominio.Resolucion;
import com.didekinlib.model.usuario.Usuario;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.didekinlib.http.CommonServConstant.MIME_JSON;
import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
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
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_closed_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_open_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.resolucion_open_type;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;
import static com.google.common.base.Preconditions.checkArgument;
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
    private UsuarioController usuarioController;
    @Autowired
    private UserComuController userComuController;
    @Autowired
    private GcmUserComuServiceIf gcmUserComuServiceIf;
    @Autowired
    private IncidenciaRepoServiceIf incidenciaRepoService;
    @Autowired
    private IncidControllerChecker checker;

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad.
     * 2. The incidservice is open.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers or the incidservice is closed, an exception is thrown.
     * 2. The incidservice and all its incidenciaUsers are deleted.
     *
     * @return number of registers accessed and, perhaps, modified.
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         UNAUTHORIZED_TX_TO_USER.
     */
    @RequestMapping(value = CLOSE_INCIDENCIA, method = PUT, consumes = MIME_JSON)
    int closeIncidencia(@RequestHeader("Authorization") String accessToken,
                        @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.debug("closeIncidencia()");
        String userName = getUserNameFromAuthentication();
        Incidencia incidencia = incidenciaRepoService.seeIncidenciaById(resolucion.getIncidencia().getIncidenciaId());
        Resolucion resolucionIn = new Resolucion.ResolucionBuilder(incidencia).copyResolucion(resolucion).buildAsFk();
        // En este método verificamos autoridad 'adm' en la comunidad.
        int rowsModified = modifyResolucionInt(resolucionIn, userName)
                + incidenciaRepoService.closeIncidencia(resolucionIn.getIncidencia().getIncidenciaId());

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_closed_type, resolucionIn.getComunidadId());
        gcmUserComuServiceIf.sendGcmMessageToComunidad(resolucionIn, requestData);

        return rowsModified;
    }

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad.
     * 2. The incidservice hasn't got a resolucion open.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers or a resolucion is open, an exception is thrown.
     * 2. The incidservice and all its incidenciaUsers are deleted.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         UNAUTHORIZED_TX_TO_USER.
     */
    @RequestMapping(value = DELETE_INCID + "/{incidenciaId}", method = DELETE)
    public int deleteIncidencia(@RequestHeader("Authorization") String accessToken,
                                @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("deleteIncidencia()");
        String userName = getUserNameFromAuthentication();
        Incidencia incidencia = incidenciaRepoService.seeIncidenciaById(incidenciaId);
        checker.checkAuthorityInComunidad(userName, incidencia.getComunidad().getC_Id());
        return incidenciaRepoService.deleteIncidencia(incidenciaId);
    }

    /**
     * Preconditions:
     * 1. The incidImportancia.incidencia is open.
     * 2. The user is registered in the comunidad of the incidencia.
     * Postconditions:
     * 1. The incidencia is modified if it is open and user 'adm' or author.
     * 2. The incidImportancia record is updated, if it already exists, or it is created if not.
     *
     * @param incidImportancia : an IncidImportancia instance with incidencia and importancia fields fulfilled (importancia default initialization == 0).
     *
     * @return number of rows modified in DB: 1 or 2 if incidImportancia.importancia is also updated.
     *
     * @throws EntityException ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         INCIDENCIA_NOT_FOUND (if the incidservice is closed or it doesn't exist - usuario adm or author).
     */
    @RequestMapping(value = MOD_INCID_IMPORTANCIA, method = PUT, consumes = MIME_JSON)
    public int modifyIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                      @RequestBody IncidImportancia incidImportancia) throws EntityException
    {
        logger.error("modifyIncidImportancia()");
        final String userNameIncid = incidImportancia.getIncidencia().getUserName();
        // User name of incidservice's author.
        checkArgument(userNameIncid != null);
        // User in session.
        final String userName = getUserNameFromAuthentication();
        final Comunidad comunidad = incidImportancia.getIncidencia().getComunidad();

        int modifiedRows = 0;

        // Modificamos la incidencia.
        if (checker.checkIncidModificationPower(userName, comunidad.getC_Id(), userNameIncid)) {
            modifiedRows += modifyIncidencia(incidImportancia.getIncidencia());
        }

        // Modificamos (o registramos, si no existe registro previo) la importancia.
        if (incidImportancia.getImportancia() > 0) {
            final UsuarioComunidad userComu = new UsuarioComunidad.UserComuBuilder(
                    comunidad,
                    new Usuario.UsuarioBuilder().copyUsuario(usuarioController.completeUser(userName)).build())
                    .build();

            final IncidImportancia incidImportanciaIn = new IncidImportancia.IncidImportanciaBuilder(incidImportancia.getIncidencia())
                    .usuarioComunidad(userComu)
                    .importancia(incidImportancia.getImportancia())
                    .build();
            modifiedRows += incidenciaRepoService.modifyIncidImportancia(incidImportanciaIn);
        }
        return modifiedRows;
    }

    /**
     * Preconditions:
     * 1. There exists an incidservice OPEN to which the resolución is assigned.
     * Postconditions:
     * 1. The resolución is updated and a new avance is inserted in the DB, if that is the case.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         UNAUTHORIZED_TX_TO_USER,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist).
     */
    @RequestMapping(value = MOD_RESOLUCION, method = PUT, consumes = MIME_JSON)
    public int modifyResolucion(@RequestHeader("Authorization") String accessToken,
                                @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.error("modifyResolucion()");
        checkArgument(resolucion.getAvances().size() <= 1);
        String userName = getUserNameFromAuthentication();
        return modifyResolucionInt(resolucion, userName);
    }

    /**
     * Preconditions:
     * 1. There exists an incidservice OPEN to which the comment is to be associated.
     * 2. The user is associated to the comunidad to which belongs the incidservice.
     * Postconditions:
     * 1. The comment is inserted in the DB.
     *
     * @throws EntityException USER_NAME_NOT_FOUND,
     *                         INCIDENCIA_NOT_FOUND, if the incidservice is closed (fechaCierre != null).
     */
    @RequestMapping(value = REG_INCID_COMMENT, method = POST, consumes = MIME_JSON)
    public int regIncidComment(@RequestHeader("Authorization") String accessToken,
                               @RequestBody final IncidComment comment) throws EntityException
    {
        logger.debug("regIncidComment()");
        checker.checkIncidenciaOpen(comment.getIncidencia().getIncidenciaId());

        final Usuario user = usuarioController.completeUser(getUserNameFromAuthentication());
        final IncidComment commentIn = new IncidComment.IncidCommentBuilder()
                .copyComment(comment)
                .redactor(user)
                .build();
        return incidenciaRepoService.regIncidComment(commentIn);
    }

    /**
     * The method persists an incidservice and a new usuario_incidencia_importancia relationship.
     * <p>
     * Preconditions:
     * 1. The user is associated to the comunidad to which belongs the incidservice.
     * 2. The incidImportancia.incidservice is open.
     * <p>
     * Postconditions:
     * 1. The incidservice is persisted.
     * 2. An IncidImportancia instance is persisted for the user, with default importancia value (0), if
     * not provided an explicit one.
     *
     * @return number of rows inserted: it should be 2.
     * @throws EntityException USER_NAME_NOT_FOUND,
     *                         INCIDENCIA_NOT_REGISTERED (see regIncidencia() below),
     *                         USERCOMU_WRONG_INIT.
     */
    @RequestMapping(value = REG_INCID_IMPORTANCIA, method = RequestMethod.POST, consumes = MIME_JSON)
    public int regIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                   @RequestBody IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("regIncidImportancia()");

        final String userName = getUserNameFromAuthentication();
        final Comunidad comunidadIn = incidImportancia.getIncidencia().getComunidad();
        // Obtenemos datos de usuarioComunidad y los verificamos antes de insertar incidservice o incidImportancia.
        final UsuarioComunidad userComu = checker.getUserComunidadChecker(userName, comunidadIn.getC_Id());
        if (userComu == null) {
            throw new EntityException(USERCOMU_WRONG_INIT);
        }

        final Incidencia incidenciaIn = new Incidencia.IncidenciaBuilder()
                .copyIncidencia(incidImportancia.getIncidencia())
                .userName(userName)
                .build();

        // No controlamos si importancia > 0. Registramos incidImportancia con importancia = 0, cuando viene valor por defecto.
        WrapperCounter counter = new WrapperCounter(0L);
        IncidImportancia incidImportanciaIn = new IncidImportancia.IncidImportanciaBuilder(
                regIncidencia(incidenciaIn, counter))
                .usuarioComunidad(userComu)
                .importancia(incidImportancia.getImportancia())
                .build();
        counter.addCounter(incidenciaRepoService.regIncidImportancia(incidImportanciaIn));
        return (int) counter.getCounter();
    }


    /**
     * Preconditions:
     * 1. The user has functional role adm in the comunidad of the incidservice.
     * 2. The incidservice is open.
     * Postconditions:
     * 1. The resolucion is persisted in DB.
     * 2. A notification is sent to the users in the comunidad.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND if the incidservice doesn't exist or is closed.
     *                         USERCOMU_WRONG_INIT (if the FK restriction incidservice.usuarioComunidad is violated).
     *                         RESOLUCION_DUPLICATE.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    @RequestMapping(value = REG_RESOLUCION, method = POST, consumes = MIME_JSON)
    public int regResolucion(@RequestHeader("Authorization") String accessToken,
                             @RequestBody Resolucion resolucion) throws EntityException
    {
        logger.debug("regResolucion()");
        String userName = getUserNameFromAuthentication();
        final Incidencia incidenciaIn = resolucion.getIncidencia();
        checker.checkAuthorityInComunidad(userName, incidenciaIn.getComunidad().getC_Id());
        checker.checkIncidenciaOpen(resolucion.getIncidencia().getIncidenciaId());

        final Resolucion resolucionIn = new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .copyResolucion(resolucion)
                .userName(userName)
                .build();

        final int insertResolucion = incidenciaRepoService.regResolucion(resolucionIn);

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(resolucion_open_type, resolucion.getComunidadId());
        gcmUserComuServiceIf.sendGcmMessageToComunidad(resolucion, requestData);

        return insertResolucion;
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * 2. The incidservice can be OPEN or CLOSED.
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user doesn't belong to the comunidad of the incidservice.
     * @throws EntityException INCIDENCIA_NOT_FOUND, if the incidservice doesn't exist.
     */
    @RequestMapping(value = SEE_INCID_COMMENTS + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    List<IncidComment> seeCommentsByIncid(@RequestHeader("Authorization") String accessToken,
                                          @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeCommentsByIncid()");
        final Incidencia incidenciaIn = incidenciaRepoService.seeIncidenciaById(incidenciaId);
        final String userName = getUserNameFromAuthentication();
        checker.checkUserInComunidad(userName, incidenciaIn.getComunidad().getC_Id());

        try {
            return incidenciaRepoService.seeCommentsByIncid(incidenciaId);
        } catch (EntityException e) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * 2. incidenciaId corresponds to an OPEN incidencia.
     * <p>
     * Postconditions:
     * 1. An IncidenciaUserComu instance is returned with the user's data and the highest rol in the comunidad:
     * - incidencia.incidenciaId.
     * - incidencia.userName (user who registered the incidservice).
     * - incidencia.ambito.ambitoId.
     * - incidencia.fechaAlta.
     * - incidencia.fechaCierre.
     * - incidencia.comunidad.c_Id.
     * - incidencia.comunidad.tipoVia.
     * - incidencia.comunidad.nombreVia.
     * - incidencia.comunidad.numero.
     * - incidencia.comunidad.sufijoNumero.
     * - usuarioComunidad.usuario.userName (user in session).
     * - usuarioComunidad.usuario.alias.
     * - usuarioComunidad.usuario.uId.
     * - usuarioComunidad.comunidad.c_Id.
     * - usuarioComunidad.roles (highest rol).
     * - incidImportancia.importancia.
     * - incidImportancia.fechaAlta
     * <p>
     * 2. If the user hasn't registered an incidImportancia record:
     * - incidImportancia.importancia == 0.
     * - incidImportancia.fechaAlta == null.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND (or not open)
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist).
     */
    @RequestMapping(value = SEE_INCID_IMPORTANCIA + "/{incidenciaId}", method = GET, produces = MIME_JSON)
    public IncidAndResolBundle seeIncidImportancia(@RequestHeader("Authorization") String accessToken,
                                                   @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportancia");

        final Incidencia incidencia = incidenciaRepoService.seeIncidenciaById(incidenciaId);
        checker.checkIncidenciaOpen(incidenciaId);

        // Lo necesito para el objeto devuelto. También verifica la pertenencia del usuario a la comunidad.
        final UsuarioComunidad userComu =
                userComuController.completeWithHighestRol(getUserNameFromAuthentication(), incidencia.getComunidad().getC_Id());
        final IncidImportancia incidImportancia =
                incidenciaRepoService.seeIncidImportancia(userComu.getUsuario().getUserName(), incidenciaId);
        final boolean resolucionFlag = incidenciaRepoService.isIncidenciaWithResolucion(incidenciaId);
        return new IncidAndResolBundle(
                new IncidImportancia.IncidImportanciaBuilder(incidencia)
                        .importancia(incidImportancia.getImportancia())
                        .usuarioComunidad(userComu)
                        .fechaAlta(incidImportancia.getFechaAlta())
                        .build(),
                resolucionFlag);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * Postconditions:
     * A list of the closed incidencias in the comunidad, with fechaAlta NOT OLDER than 2 years,
     * are returned as a list of IncidenciaUser instances, with
     * the following fields:
     * - incidservice.incidenciaId.
     * - incidservice.comunidad.c_id.
     * - incidservice.userName (user who registered the incidservice).
     * - incidservice.descripcion.
     * - incidservice.ambito.ambitoId.
     * - incidservice.importanciaAvg.
     * - incidservice.fechaAlta.
     * - incidservice.fechaCierre (not null, by definition).
     * - usuario.uId. (user who registered the incidservice)
     * - usuario.alias (user who registered the incidservice).
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad or
     * the incidservice doesn't exist.
     */
    @RequestMapping(value = SEE_INCIDS_CLOSED_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsClosedByComu(@RequestHeader("Authorization") String accessToken,
                                                      @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("seeIncidsClosedByComu()");
        checker.checkUserInComunidad(getUserNameFromAuthentication(), comunidadId);
        return incidenciaRepoService.seeIncidsClosedByComu(comunidadId);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * Postconditions:
     * A list of the open incidencias in the comunidad are returned as a list of IncidenciaUser instances, with
     * the following fields:
     * - incidservice.incidenciaId.
     * - incidservice.comunidad.c_id.
     * - incidservice.userName (user who registered the incidservice).
     * - incidservice.descripcion.
     * - incidservice.ambito.ambitoId.
     * - incidservice.importanciaAvg.
     * - incidservice.fechaAlta.
     * - incidservice.fechaCierre (null, by definition).
     * - usuario.uId. (user who registered the incidservice)
     * - usuario.alias (user who registered the incidservice).
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad or the incidservice doesn't exist.
     */
    @RequestMapping(value = SEE_INCIDS_OPEN_BY_COMU + "/{comunidadId}", method = GET, produces = MIME_JSON)
    public List<IncidenciaUser> seeIncidsOpenByComu(@RequestHeader("Authorization") String accessToken,
                                                    @PathVariable long comunidadId) throws EntityException
    {
        logger.debug("seeIncidsOpenByComu()");
        checker.checkUserInComunidad(getUserNameFromAuthentication(), comunidadId);
        return incidenciaRepoService.seeIncidsOpenByComu(comunidadId);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * 2. The incidservice can be OPEN or CLOSED.
     * Postconditions:
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad.
     * @throws EntityException INCIDENCIA_NOT_FOUND, if the resolucionId (incidenciaId) doesn't exist.
     * @return null if there is not resolucion in BD.
     */
    @RequestMapping(value = SEE_RESOLUCION + "/{resolucionId}", produces = MIME_JSON, method = GET)
    public Resolucion seeResolucion(@RequestHeader("Authorization") String accessToken,
                                    @PathVariable long resolucionId) throws EntityException
    {
        logger.debug("seeResolucion()");
        final Incidencia incidencia = incidenciaRepoService.seeIncidenciaById(resolucionId);
        checker.checkUserInComunidad(getUserNameFromAuthentication(), incidencia.getComunidad().getC_Id());
        Resolucion resolucionBd = incidenciaRepoService.seeResolucion(resolucionId);
        if (resolucionBd != null) {
            return new Resolucion.ResolucionBuilder(
                    new Incidencia.IncidenciaBuilder()
                            .copyIncidencia(incidencia)
                            .comunidad(
                                    new Comunidad.ComunidadBuilder().c_id(incidencia.getComunidadId())
                                            .build())
                            .build())
                    .copyResolucion(resolucionBd)
                    .build();
        } else {
            return resolucionBd;
        }
    }


    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice.
     * 2. The incidservice can be OPEN or CLOSED.
     * Postconditions:
     *
     * @throws EntityException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad.
     * @throws EntityException INCIDENCIA_NOT_FOUND, if the incidenciaId doesn't exist.
     */
    @RequestMapping(value = SEE_USERCOMUS_IMPORTANCIA + "/{incidenciaId}", produces = MIME_JSON, method = GET)
    List<ImportanciaUser> seeUserComusImportancia(@RequestHeader("Authorization") String accessToken,
                                                  @PathVariable long incidenciaId) throws EntityException
    {
        logger.debug("seeUserComusImportancia()");
        checker.checkUserInComunidad(getUserNameFromAuthentication(), incidenciaRepoService.seeIncidenciaById(incidenciaId).getComunidad().getC_Id());
        return incidenciaRepoService.seeUserComusImportancia(incidenciaId);
    }

//  ================================ API METHODS FOR INTERNAL CALLS ================================

    /**
     * Preconditions:
     * 1. The user has powers to modify the incidservice: he/she is the user who initiates the incidservice or has adm function rol.
     * 2. The incidservice is OPEN.
     * Postconditions:
     * 1. The incidservice is modified in BD.
     *
     * @param incidencia : an Incidencia instance with incidenciaId, descripcion and ambitoId.
     * @return number of rows modified in DB (it should be 1).
     * @throws EntityException INCIDENCIA_NOT_FOUND (or closed).
     */
    private int modifyIncidencia(Incidencia incidencia) throws EntityException
    {
        logger.debug("modifyIncidencia()");
        return incidenciaRepoService.modifyIncidencia(incidencia);
    }

    /**
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         UNAUTHORIZED_TX_TO_USER,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist).
     */
    private int modifyResolucionInt(final Resolucion resolucion, String userName) throws EntityException
    {
        checker.checkAuthorityInComunidad(userName, resolucion.getComunidadId());

        if (resolucion.getAvances().size() > 0) {
            return incidenciaRepoService.modifyResolucion(doResolucionWithAvance(resolucion, userName));
        }
        return incidenciaRepoService.modifyResolucion(resolucion);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidservice. It may have a gcm token in data base.
     * Postconditions:
     * 1. the incidservice is inserted in the DB, if necessary.
     * 2. A data message is sent to the GCM server.
     * 3. If a gcm token is returned gcm server call, it is updated or deleted in DB.
     *
     * @param incidenciaIn : an Incidencia instance with userName.
     * @return Incidencia the one inserted or the already in BD.
     * @throws EntityException INCIDENCIA_NOT_REGISTERED (if a SQLException is thrown, when registering
     *                         the incidservice in the 'service').
     */
    @SuppressWarnings("WeakerAccess")
    Incidencia regIncidencia(final Incidencia incidenciaIn, WrapperCounter rowsInsertCount) throws EntityException
    {
        logger.debug("regIncidencia()");
        long incidenciaPK = incidenciaIn.getIncidenciaId();
        final Incidencia incidenciaOut;

        // Incidencia sin PK: no existen en BD.
        if (incidenciaPK <= 0) {
            incidenciaPK = incidenciaRepoService.regIncidencia(incidenciaIn);
            rowsInsertCount.addCounter();
            // Actualizamos la FK para incidImportancia.
            incidenciaOut = new Incidencia.IncidenciaBuilder()
                    .copyIncidencia(incidenciaIn)
                    .incidenciaId(incidenciaPK)
                    .build();
        } else {
            checker.checkIncidenciaOpen(incidenciaIn.getIncidenciaId());
            incidenciaOut = incidenciaIn;
        }

        GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_open_type, incidenciaOut.getComunidadId());
        gcmUserComuServiceIf.sendGcmMessageToComunidad(incidenciaOut, requestData);

        return incidenciaOut;
    }

//  ===================================  HELPER METHODS =======================================

    private Resolucion doResolucionWithAvance(Resolucion resolucion, String userName)
    {
        List<Avance> avances = new ArrayList<>(1);
        avances.add(new Avance.AvanceBuilder()
                .copyAvance(resolucion.getAvances().get(0))
                .userName(userName)
                .build());

        return new Resolucion.ResolucionBuilder(resolucion.getIncidencia())
                .fechaPrevista(resolucion.getFechaPrev())
                .costeEstimado(resolucion.getCosteEstimado())
                .avances(avances)
                .buildAsFk();
    }

//    ============================================================
//    ..................... INNER CLASSES  .......................
//    ============================================================

    @Bean
    public IncidControllerChecker incidControllerChecker()
    {
        return new IncidControllerChecker();
    }

    /**
     * User: pedro@didekin
     * Date: 17/03/16
     * Time: 09:30
     */
    class IncidControllerChecker {

        void checkAuthorityInComunidad(String userName, long comunidadId) throws EntityException
        {
            logger.debug("checkAuthorityInComunidad()");
            if (!userComuController.hasAuthorityAdmInComunidad(userName, comunidadId)) {
                throw new EntityException(UNAUTHORIZED_TX_TO_USER);
            }
        }

        /**
         * The method checks if a user initiated an incidservice or has the authority 'adm'.
         *
         * @param userName:      user in session.
         * @param comunidadId:   comunidad where the incidservice belongs to.
         * @param incidUserName: userName authoring the incidservice.
         */
        boolean checkIncidModificationPower(String userName, long comunidadId, String incidUserName) throws EntityException
        {
            logger.debug("checkIncidModificationPower()");
            return userComuController.hasAuthorityAdmInComunidad(userName, comunidadId) || userName.equals(incidUserName);
        }

        void checkIncidenciaOpen(long incidenciaId) throws EntityException
        {
            logger.debug("checkIncidenciaOpen()");
            incidenciaRepoService.checkIncidenciaOpen(incidenciaId);
        }

        void checkUserInComunidad(String userName, long comunidadId) throws EntityException
        {
            logger.debug("checkUserInComunidad()");
            if (!userComuController.isUserInComunidad(userName, comunidadId)) {
                throw new EntityException(USERCOMU_WRONG_INIT);
            }
        }

        UsuarioComunidad getUserComunidadChecker(String userName, long comunidadId)
        {
            logger.debug("getUserNameFromAuthentication()");
            return userComuController.getUserComunidadChecker(userName, comunidadId);
        }
    }
}