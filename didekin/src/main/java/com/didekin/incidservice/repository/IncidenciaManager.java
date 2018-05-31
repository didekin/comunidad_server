package com.didekin.incidservice.repository;


import com.didekin.common.repository.ServiceException;
import com.didekin.userservice.gcm.GcmUserComuServiceIf;
import com.didekinlib.gcm.model.incidservice.GcmIncidRequestData;
import com.didekinlib.model.comunidad.Comunidad;
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
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_NOT_REGISTERED;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_USER_WRONG_INIT;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.incidencia.dominio.Resolucion.doResolucionModifiedWithNewAvance;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_closed_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_open_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.resolucion_open_type;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
public class IncidenciaManager {

    private static final Logger logger = LoggerFactory.getLogger(IncidenciaManager.class.getCanonicalName());

    private final IncidenciaDao incidenciaDao;
    @Autowired
    private GcmUserComuServiceIf gcmUserComuService;
    @Autowired
    private UserManagerConnector usuarioConnector;

    @Autowired
    public IncidenciaManager(IncidenciaDao incidenciaDao)
    {
        this.incidenciaDao = incidenciaDao;
    }

    public UserManagerConnector getUsuarioConnector()
    {
        return usuarioConnector;
    }

    //    ============================================================
    //    .......... IncidenciaManagerIf .......
    //    ============================================================

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad.
     * 2. The incidencia is open.
     * 3. The incidencia has already an open resolucion.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers, she is not the incidencia's initiator or the incidencia is closed, an exception is thrown.
     * 2. The incidencia and all its incidenciaUsers are deleted.
     *
     * @return number of registers accessed and, perhaps, modified.
     * @throws ServiceException INCIDENCIA_NOT_FOUND,
     *                          USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist),
     *                          UNAUTHORIZED_TX_TO_USER.
     */
    public int closeIncidencia(final String userName, Resolucion resolucion) throws ServiceException
    {
        logger.debug("closeIncidencia()");

        Resolucion resolucionFull = of(seeIncidenciaById(resolucion.getIncidencia().getIncidenciaId()))
                .map(incidencia -> new Resolucion.ResolucionBuilder(incidencia).copyResolucion(resolucion).buildAsFk())
                .filter(resolucionStr -> usuarioConnector.checkAuthorityInComunidad(userName, resolucionStr.getComunidadId()))
                .findFirst().orElseThrow(() -> new ServiceException(UNAUTHORIZED_TX_TO_USER));

        int rowsUpdated =
                modifyResolucion(userName, resolucion) + incidenciaDao.closeIncidencia(resolucion.getIncidencia().getIncidenciaId());

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_closed_type, resolucionFull.getComunidadId());
        gcmUserComuService.sendGcmMessageToComunidad(resolucionFull, requestData);

        return rowsUpdated;
    }

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad or she is the initiator user.
     * 2. The incidencia hasn't got a resolucion open.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers or a resolucion is open, an exception is thrown.
     * 2. The incidencia and all its incidenciaUsers are deleted.
     *
     * @return number of rows deleted (1).
     * @throws ServiceException INCIDENCIA_NOT_FOUND,
     *                          USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist),
     *                          UNAUTHORIZED_TX_TO_USER.
     */
    public int deleteIncidencia(String userNameInSession, long incidenciaId) throws ServiceException
    {
        logger.debug("deleteIncidencia()");
        return of(seeIncidenciaById(incidenciaId))
                .filter(incidencia -> usuarioConnector.checkIncidModificationPower(userNameInSession, incidencia))
                .map(incidencia -> incidenciaDao.deleteIncidencia(incidencia.getIncidenciaId()))
                .findFirst().orElseThrow(() -> new ServiceException(UNAUTHORIZED_TX_TO_USER));
    }

    /**
     * Preconditions:
     * 1. Incidencia.incidenciaId > 0 and Incidencia.userName != null.
     * 2. The user has powers to modify the incidencia: he/she is the user who initiates the incidencia or has adm function rol.
     * Postconditions:
     * 1. The incidencia is modified in BD.
     *
     * @param userNameInSession : user trying the modification.
     * @param incidencia        : an Incidencia instance with incidenciaId, descripcion and ambitoId.
     * @return number of rows modified in DB (0 or 1).
     * @throws ServiceException INCIDENCIA_NOT_FOUND (or closed),
     *                          USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist),
     *                          INCIDENCIA_USER_WRONG_INIT.
     */
    int modifyIncidencia(String userNameInSession, final Incidencia incidencia) throws ServiceException
    {
        logger.debug("modifyIncidencia()");

        if (incidencia == null || incidencia.getIncidenciaId() <= 0 || incidencia.getUserName() == null) {
            throw new ServiceException(INCIDENCIA_USER_WRONG_INIT);
        }
        return of(incidencia)
                .filter(incidenciaIn ->
                        usuarioConnector.checkIncidModificationPower(userNameInSession, incidenciaIn))
                .mapToInt(incidenciaDao::modifyIncidencia)
                .findFirst().orElse(0);
    }

    /**
     * Preconditions:
     * 1. The incidImportancia.incidencia is open.
     * 2. The user is registered in the comunidad of the incidencia.
     * Postconditions:
     * 1. The incidencia is modified if user 'adm' or author.
     * 2. The incidImportancia record is updated, if it already exists, or it is created if not.
     * 3. The incidImportancia record is updated if importancia > 0.
     *
     * @param incidImportancia : an IncidImportancia instance with incidencia and importancia fields fulfilled (importancia default initialization == 0).
     * @return number of rows modified in DB: 1 or 2 if incidImportancia.importancia is also updated.
     * @throws ServiceException USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist),
     *                          INCIDENCIA_NOT_FOUND (if the incidencia is closed or it doesn't exist).
     */
    public int modifyIncidImportancia(String userNameInSession, IncidImportancia incidImportancia) throws ServiceException
    {
        logger.debug("modifyIncidImportancia()");

        // Checking invariants.
        checkIncidenciaOpen(incidImportancia.getIncidencia().getIncidenciaId());
        getUsuarioConnector().checkUserInComunidad(userNameInSession, incidImportancia.getIncidencia().getComunidadId());

        // Modificamos incidencia.
        int rowsIncidMod = modifyIncidencia(userNameInSession, incidImportancia.getIncidencia());

        // Registramos o modificamos incidImportancia.
        int rowsIncidImpMod = of(incidImportancia)
                .filter(incidImpIn -> incidImpIn.getImportancia() > 0)
                .map(incidImpIn -> new IncidImportancia.IncidImportanciaBuilder(incidImpIn.getIncidencia())
                        .usuarioComunidad(
                                new UsuarioComunidad.UserComuBuilder(
                                        incidImpIn.getIncidencia().getComunidad(),
                                        new Usuario.UsuarioBuilder()
                                                .copyUsuario(usuarioConnector.completeUser(userNameInSession))
                                                .build()
                                ).build()
                        )
                        .importancia(incidImpIn.getImportancia())
                        .build()
                ).mapToInt(
                        // Registramos si modificación devuelve entero < 1
                        incidImpIn -> incidenciaDao.modifyIncidImportancia(incidImpIn) < 1 ? regIncidImportancia(userNameInSession, incidImpIn) : 1
                ).findFirst().orElse(0);

        return rowsIncidMod + rowsIncidImpMod;
    }

    /**
     * Preconditions:
     * 1. There exists an incidencia OPEN to which the resolución is assigned.
     * 2. Only these fields can be modified:
     * - resolucion.fechaPrevista.
     * - resolucion.costeEstimado.
     * - resolucion.avances.
     * <p>
     * Postconditions:
     * 1. The resolución is updated and a new avance is inserted in the DB, if that is the case.
     *
     * @throws ServiceException INCIDENCIA_NOT_FOUND,
     *                          UNAUTHORIZED_TX_TO_USER,
     *                          USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist).
     */
    public int modifyResolucion(String userName, Resolucion resolucion) throws ServiceException
    {
        logger.debug("modifyResolucion()");

        return of(resolucion)
                .filter(resolucion1 -> usuarioConnector.checkAuthorityInComunidad(userName, resolucion1.getComunidadId()))
                .map(resolucion1 -> doResolucionModifiedWithNewAvance(resolucion1, userName))
                .map(incidenciaDao::modifyResolucion).findFirst().orElseThrow(() -> new ServiceException(UNAUTHORIZED_TX_TO_USER));
    }

    /**
     * Preconditions:
     * 1. There exists an incidencia OPEN or CLOSED to which the comment is to be associated.
     * 2. The user is associated to the comunidad to which belongs the incidencia.
     * Postconditions:
     * 1. The comment is inserted in the DB.
     *
     * @throws ServiceException USER_NAME_NOT_FOUND,
     *                          INCIDENCIA_NOT_FOUND.
     */
    public int regIncidComment(String userName, final IncidComment comment) throws ServiceException
    {
        logger.debug("regIncidComment()");
        return of(comment)
//                .filter(commentIn -> checkIncidenciaOpen(comment.getIncidencia().getIncidenciaId()))
                .filter(commentIn -> usuarioConnector.checkUserInComunidad(userName, commentIn.getIncidencia().getComunidadId()))
                .mapToInt(commentIn -> incidenciaDao.regIncidComment(
                        new IncidComment.IncidCommentBuilder()
                                .copyComment(commentIn)
                                .redactor(usuarioConnector.completeUser(userName))
                                .build()
                        )
                )
                .findFirst().getAsInt();
    }


    /**
     * Postconditions:
     * 1. the incidencia is inserted in the DB, if necessary.
     * 2. A data message is sent to the GCM server if the incidencia is inserted.
     * 3. If a gcm token is returned from the gcm server call, it is updated or deleted in DB.
     * 4. If inserted, rowsUpdated counter is increased.
     *
     * @param incidencia : an Incidencia instance with userName.
     * @return Incidencia: the incidencia inserted or the one already in BD.
     * @throws ServiceException INCIDENCIA_NOT_REGISTERED (if a SQLException is thrown).
     *                          INCIDENCIA_NOT_FOUND if the incidencia is closed.
     */
    Incidencia regIncidencia(Incidencia incidencia) throws ServiceException
    {
        logger.debug("regIncidencia()");

        return of(incidencia)
                .filter(incidencia1 -> incidencia.getIncidenciaId() == 0L)
                .map(incidencia1 -> {
                    long pK;
                    try {
                        pK = incidenciaDao.regIncidencia(incidencia1);
                    } catch (SQLException e) {
                        throw new ServiceException(INCIDENCIA_NOT_REGISTERED);
                    }
                    return new Incidencia.IncidenciaBuilder()
                            .copyIncidencia(incidencia1)
                            .incidenciaId(pK)
                            .build();
                })
                .peek(incidencia1 -> {
                    GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_open_type, incidencia1.getComunidadId());
                    gcmUserComuService.sendGcmMessageToComunidad(incidencia1, requestData);
                }).findFirst().orElse(incidencia);  // Return original parameter if incidenciaId != 0
    }

    /**
     * The method persists an incidencia, if not already persisted, and a new usuario_incidencia_importancia relationship.
     * <p>
     * Preconditions:
     * 1. The user is associated to the comunidad to which belongs the incidencia.
     * 2. The incidencia is open.
     * <p>
     * Postconditions:
     * 1. The incidencia is persisted, if necessary.
     * 2. An IncidImportancia instance is persisted for the user, with default importancia value (0), if
     * not provided an explicit one.
     *
     * @return number of rows inserted: it should be 2.
     * @throws ServiceException USER_NAME_NOT_FOUND,
     *                          INCIDENCIA_NOT_REGISTERED (see regIncidencia() below),
     *                          USERCOMU_WRONG_INIT.
     */
    public int regIncidImportancia(String userName, IncidImportancia incidImportancia) throws ServiceException
    {
        logger.debug("regIncidImportancia()");

        Incidencia incidencia = incidImportancia.getIncidencia();

        return of(incidencia)
                .filter(incidenciaIn -> usuarioConnector.checkUserInComunidad(userName, incidenciaIn.getComunidadId()))
                .map(incidenciaIn -> {
                    // Si la incidencia ya está registrada y abierta, la devolvemos.
                    if (incidenciaIn.getIncidenciaId() > 0L && checkIncidenciaOpen(incidenciaIn.getIncidenciaId())) { // INCIDENCIA_NOT_FOUND.
                        return incidenciaIn;
                    }
                    return regIncidencia(
                            new Incidencia.IncidenciaBuilder()
                                    .copyIncidencia(incidenciaIn)
                                    .userName(userName)
                                    .build());
                })
                .map(incidenciaOut -> incidenciaDao.regIncidImportancia(
                        new IncidImportancia.IncidImportanciaBuilder(regIncidencia(incidenciaOut))
                                .usuarioComunidad(usuarioConnector.completeUserAndComuRoles(userName, incidenciaOut.getComunidadId()))
                                .importancia(incidImportancia.getImportancia())
                                .build()
                        )
                )
                .map(rowsUpdated -> (incidencia.getIncidenciaId() == 0L) ? ++rowsUpdated : rowsUpdated)
                .findFirst().get();
    }

    /**
     * Preconditions:
     * 1. The user has functional role adm in the comunidad of the incidencia.
     * 2. The incidencia is open.
     * Postconditions:
     * 1. The resolucion is persisted in DB.
     * 2. A notification is sent to the users in the comunidad.
     *
     * @throws ServiceException INCIDENCIA_NOT_FOUND if the incidencia doesn't exist or is closed.
     *                          USERCOMU_WRONG_INIT (if the FK restriction incidencia.usuarioComunidad is violated).
     *                          RESOLUCION_DUPLICATE.
     */
    public int regResolucion(String userName, Resolucion resolucion) throws ServiceException
    {
        logger.debug("regResolucion()");

        int rowsUpdated = of(resolucion)
                .map(Resolucion::getIncidencia)
                .filter(incidencia -> usuarioConnector.checkAuthorityInComunidad(userName, incidencia.getComunidadId()))
                .filter(incidencia -> checkIncidenciaOpen(incidencia.getIncidenciaId()))
                .map(incidencia -> new Resolucion.ResolucionBuilder(incidencia)
                        .copyResolucion(resolucion)
                        .userName(userName)
                        .build())
                .map(incidenciaDao::regResolucion)
                .findFirst().orElseThrow(() -> new ServiceException(UNAUTHORIZED_TX_TO_USER));

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(resolucion_open_type, resolucion.getComunidadId());
        gcmUserComuService.sendGcmMessageToComunidad(resolucion, requestData);

        return rowsUpdated;
    }

    public List<IncidComment> seeCommentsByIncid(long incidenciaId) throws ServiceException
    {
        logger.debug("seeCommentsByIncid()");
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(incidenciaId);
        if (comments == null || comments.isEmpty()) {
            // Check if the incidencia exists.
            seeIncidenciaById(incidenciaId);
        }
        return comments;
    }

    public Incidencia seeIncidenciaById(long incidenciaId) throws ServiceException
    {
        logger.debug("seeIncidenciaById()");
        return incidenciaDao.seeIncidenciaById(incidenciaId);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. incidenciaId corresponds to an OPEN incidencia.
     * <p>
     * Postconditions:
     * 1. An IncidAndResolBundle instance is returned with an IncidImportancia instance as produced by
     * {@link IncidenciaDao#seeIncidImportanciaByUser(String userName, long incidenciaId) IncidenciaDao.seeIncidImportanciaByUser method}.
     * 2. If the user hasn't registered an incidImportancia record previously, an incidenciaResolBundle is composed by this method with:
     * - a fully initialized incidencia, comunidad and usuarioComunidad.
     * - incidImportancia.importancia == 0.
     * - incidImportancia.fechaAlta == null.
     * - hasResolucion = state in table of resolucion.
     *
     * @throws ServiceException INCIDENCIA_NOT_FOUND (or not open)
     *                          USERCOMU_WRONG_INIT (if the relationship usuario_comunidad doesn't exist).
     */
    public IncidAndResolBundle seeIncidImportanciaByUser(final String userNameInSession, final long incidenciaId) throws ServiceException
    {
        logger.debug("seeIncidImportanciaByUser()");

        return of(incidenciaId)
                .filter(this::checkIncidenciaOpen) // If none, we throw at the end INCIDENCIA_NOT_FOUND.
                .filter(incidenciaIdIn -> checkIncidImportanciaInDb(userNameInSession, incidenciaIdIn)) // Evita exception en incidenciaDao.seeIncidImportanciaByUser().
                .map(incidenciaIdIn -> incidenciaDao.seeIncidImportanciaByUser(userNameInSession, incidenciaIdIn)) // Here it is verified user in comunidad.
                .findFirst()
                .orElseGet(() -> {
                            Incidencia incidenciaIn = seeIncidenciaById(incidenciaId);
                            UsuarioComunidad usuarioComunidad = getUsuarioConnector().completeUserAndComuRoles(userNameInSession, incidenciaIn.getComunidadId());
                            return new IncidAndResolBundle(
                                    new IncidImportancia.IncidImportanciaBuilder(incidenciaIn)
                                            .usuarioComunidad(
                                                    new UsuarioComunidad.UserComuBuilder(
                                                            new Comunidad.ComunidadBuilder()
                                                                    .copyComunidadNonNullValues(incidenciaIn.getComunidad())
                                                                    .build(),
                                                            new Usuario.UsuarioBuilder()
                                                                    .copyUsuario(usuarioComunidad.getUsuario())
                                                                    .build())
                                                            .roles(usuarioComunidad.getRoles())
                                                            .build()
                                            ).build(),
                                    incidenciaDao.countResolucionByIncid(incidenciaId) > 0
                            );
                        }
                );
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * Postconditions:
     * 1. A list of the closed incidencias in the comunidad, as produced by
     * {@link IncidenciaDao#seeIncidsClosedByComu(long comunidadId) IncidenciaDao.seeIncidsClosedByComu method}
     *
     * @throws ServiceException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad or
     *                          the incidencia doesn't exist.
     */
    public List<IncidenciaUser> seeIncidsClosedByComu(String userNameInSession, long comunidadId)
    {
        logger.debug("seeIncidsClosedByComu()");
        return Stream.of(comunidadId)
                .filter(comunidadIdIn -> getUsuarioConnector().checkUserInComunidad(userNameInSession, comunidadIdIn))
                .map(incidenciaDao::seeIncidsClosedByComu)
                .findFirst().get();
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * Postconditions:
     * 1. A list of the open incidencias in the comunidad, as produced by
     * {@link IncidenciaDao#seeIncidsOpenByComu(long comunidadId) IncidenciaDao.seeIncidsOpenByComu method}
     *
     * @throws ServiceException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad or
     *                          the incidencia doesn't exist.
     */
    public List<IncidenciaUser> seeIncidsOpenByComu(String userNameInSession, long comunidadId)
    {
        logger.debug("seeIncidsOpenByComu()");
        return Stream.of(comunidadId)
                .filter(comunidadIdIn -> getUsuarioConnector().checkUserInComunidad(userNameInSession, comunidadIdIn))
                .map(incidenciaDao::seeIncidsOpenByComu)
                .findFirst().get();
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. The incidencia can be OPEN or CLOSED.
     *
     * @return 1. A resolucion instance is returned as produced by
     * {@link IncidenciaDao#seeResolucion(long resolucionId) IncidenciaDao.seeResolucion method}
     * plus:
     * - incidencia.comunidad.c_Id
     * 2. null, there does not exists the resolucion.
     * @throws ServiceException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad.
     * @throws ServiceException INCIDENCIA_NOT_FOUND if the incidenciaId (same as resolucionId) doesn't exist.
     */
    public Resolucion seeResolucion(String userName, long incidenciaId) throws ServiceException
    {
        logger.debug("seeResolucion()");

        final Incidencia incidencia = seeIncidenciaById(incidenciaId);  // INCIDENCIA_NOT_FOUND exception.
        getUsuarioConnector().checkUserInComunidad(userName, incidencia.getComunidadId()); // USERCOMU_WRONG_INIT exception.

        return of(incidenciaId)
                .map(incidenciaDao::seeResolucion)
                .flatMap(resolucionIn -> resolucionIn != null ? of(resolucionIn) : empty())
                .map(resolucionIn -> new Resolucion.ResolucionBuilder(
                        new Incidencia.IncidenciaBuilder()
                                .copyIncidencia(incidencia)
                                .comunidad(
                                        new Comunidad.ComunidadBuilder().c_id(incidencia.getComunidadId())
                                                .build())
                                .build())
                        .copyResolucion(resolucionIn)
                        .build())
                .findFirst()
                .orElse(null);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. The incidencia can be OPEN or CLOSED.
     * Postconditions:
     * 1. A list as produced by
     * {@link IncidenciaDao#seeUserComusImportancia(long incidenciaId) IncidenciaDao.seeUserComusImportancia method}
     *
     * @throws ServiceException INCIDENCIA_NOT_FOUND, if the incidenciaId doesn't exist.
     * @throws ServiceException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad.
     */
    public List<ImportanciaUser> seeUserComusImportancia(final String userName, long incidenciaId) throws ServiceException
    {
        logger.debug("seeUserComusImportancia()");
        return of(incidenciaId)
                .filter(incidenciaPk -> usuarioConnector.checkUserInComunidad(userName, seeIncidenciaById(incidenciaPk).getComunidad().getC_Id()))
                .map(incidenciaPk -> incidenciaDao.seeUserComusImportancia(incidenciaId))
                .findFirst().get();
    }

    /* ================================ HELPERS ===================================*/

    boolean checkIncidenciaOpen(long incidenciaId)
    {
        logger.debug("checkIncidenciaOpen()");
        if (!incidenciaDao.isIncidenciaOpen(incidenciaId)) {
            throw new ServiceException(INCIDENCIA_NOT_FOUND);
        }
        return true;
    }

    boolean checkIncidImportanciaInDb(String userNameInSession, long incidenciaId)
    {
        logger.debug("checkIncidImportanciaInDb()");
        return incidenciaDao.isImportanciaUser(usuarioConnector.completeUser(userNameInSession).getuId(), incidenciaId) == 1;
    }
}
