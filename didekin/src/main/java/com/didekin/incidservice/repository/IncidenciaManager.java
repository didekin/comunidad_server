package com.didekin.incidservice.repository;


import com.didekin.common.EntityException;
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

import static com.didekinlib.http.GenericExceptionMsg.UNAUTHORIZED_TX_TO_USER;
import static com.didekinlib.model.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_REGISTERED;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_USER_WRONG_INIT;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_WRONG_INIT;
import static com.didekinlib.model.incidencia.dominio.Resolucion.doResolucionModifiedWithNewAvance;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_closed_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.incidencia_open_type;
import static com.didekinlib.model.incidencia.gcm.GcmKeyValueIncidData.resolucion_open_type;
import static java.util.stream.Stream.of;

/**
 * User: pedro@didekin
 * Date: 20/04/15
 * Time: 15:41
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
class IncidenciaManager implements IncidenciaManagerIf {

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

    //    ============================================================
    //    .......... IncidenciaManagerIf .......
    //    ============================================================

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad.
     * 2. The incidencia is open.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers, she is not the incidencia's initiator or the incidencia is closed, an exception is thrown.
     * 2. The incidencia and all its incidenciaUsers are deleted.
     *
     * @return number of registers accessed and, perhaps, modified.
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         UNAUTHORIZED_TX_TO_USER.
     */
    @Override
    public int closeIncidencia(final String userName, Resolucion resolucion) throws EntityException
    {
        logger.debug("closeIncidencia()");

        Resolucion resolucionFull = of(seeIncidenciaById(resolucion.getIncidencia().getIncidenciaId()))
                .map(incidencia -> new Resolucion.ResolucionBuilder(incidencia).copyResolucion(resolucion).buildAsFk())
                .filter(resolucionStr -> usuarioConnector.checkAuthorityInComunidad(userName, resolucionStr.getComunidadId()))
                .findFirst().orElseThrow(() -> new EntityException(UNAUTHORIZED_TX_TO_USER));

        int rowsUpdated =
                modifyResolucion(userName, resolucion) + incidenciaDao.closeIncidencia(resolucion.getIncidencia().getIncidenciaId());

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_closed_type, resolucionFull.getComunidadId());
        gcmUserComuService.sendGcmMessageToComunidad(resolucionFull, requestData);

        return rowsUpdated;
    }

    /**
     * Preconditions:
     * 1. The user has adm functions in the comunidad.
     * 2. The incidencia hasn't got a resolucion open.
     * Postconditions:
     * 1. If the user hasn't got now the necessary powers or a resolucion is open, an exception is thrown.
     * 2. The incidencia and all its incidenciaUsers are deleted.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         UNAUTHORIZED_TX_TO_USER.
     */
    @Override
    public int deleteIncidencia(String userNameInSession, long incidenciaId) throws EntityException
    {
        logger.debug("deleteIncidencia()");
        return of(seeIncidenciaById(incidenciaId))
                .filter(incidencia -> usuarioConnector.checkIncidModificationPower(userNameInSession, incidencia.getComunidad().getC_Id(), incidencia.getUserName()))
                .map(incidencia -> incidenciaDao.deleteIncidencia(incidencia.getIncidenciaId()))
                .findFirst().orElseThrow(() -> new EntityException(UNAUTHORIZED_TX_TO_USER));
    }

    @Override
    public boolean isIncidenciaWithResolucion(long incidenciaId)
    {
        logger.debug("isIncidenciaWithResolucion()");
        return incidenciaDao.countResolucionByIncid(incidenciaId) == 1;
    }

    /**
     * Preconditions:
     * 1. The user has powers to modify the incidencia: he/she is the user who initiates the incidencia or has adm function rol.
     * 2. The incidencia is OPEN.
     * Postconditions:
     * 1. The incidencia is modified in BD.
     *
     * @param userNameInSession : user trying the modification.
     * @param incidencia        : an Incidencia instance with incidenciaId, descripcion and ambitoId.
     * @return number of rows modified in DB (0 or 1).
     * @throws EntityException INCIDENCIA_NOT_FOUND (or closed),
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         INCIDENCIA_USER_WRONG_INIT.
     */
    @Override
    public int modifyIncidencia(String userNameInSession, final Incidencia incidencia) throws EntityException
    {
        logger.debug("modifyIncidencia()");

        if (incidencia == null || incidencia.getUserName() == null) {
            throw new EntityException(INCIDENCIA_USER_WRONG_INIT);
        }
        return of(incidencia)
                .filter(incidenciaIn ->
                        usuarioConnector.checkIncidModificationPower(userNameInSession, incidenciaIn.getComunidadId(), incidenciaIn.getUserName()))
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
     *
     * @param incidImportancia : an IncidImportancia instance with incidencia and importancia fields fulfilled (importancia default initialization == 0).
     * @return number of rows modified in DB: 1 or 2 if incidImportancia.importancia is also updated.
     * @throws EntityException ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist),
     *                         INCIDENCIA_NOT_FOUND (if the incidencia is closed or it doesn't exist),
     *                         INCID_IMPORTANCIA_WRONG_INIT (if incidImportancia.importancia is wrongly initialized).
     */
    @Override
    public int modifyIncidImportancia(String userNameInSession, IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("modifyIncidImportancia()");

        // Modificamos incidencia.
        int rowsIncidMod = modifyIncidencia(userNameInSession, incidImportancia.getIncidencia());

        // Registramos o modificamos incidImportancia: registramos si modificación devuelve entero < 1.
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
                        incidImpIn -> incidenciaDao.modifyIncidImportancia(incidImpIn) < 1 ? regIncidImportancia(userNameInSession, incidImpIn) : 1
                ).findFirst().orElseThrow(() -> new EntityException(INCID_IMPORTANCIA_WRONG_INIT));  // Exception related to filter pass 0.

        return rowsIncidMod + rowsIncidImpMod;
    }

    /**
     * Preconditions:
     * 1. There exists an incidencia OPEN to which the resolución is assigned.
     * Postconditions:
     * 1. The resolución is updated and a new avance is inserted in the DB, if that is the case.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND,
     *                         UNAUTHORIZED_TX_TO_USER,
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist).
     */
    @Override
    public int modifyResolucion(String userName, Resolucion resolucion) throws EntityException
    {
        logger.debug("modifyResolucion()");

        return of(resolucion)
                .filter(resolucion1 -> usuarioConnector.checkAuthorityInComunidad(userName, resolucion1.getComunidadId()))
                .map(resolucion1 -> (
                                resolucion1.getAvances() != null && resolucion1.getAvances().size() == 1 && !resolucion1.getAvances().get(0).getAvanceDesc().isEmpty() ?
                                        doResolucionModifiedWithNewAvance(resolucion1, userName) :
                                        resolucion1
                        )
                ).map(incidenciaDao::modifyResolucion).findFirst().orElseThrow(() -> new EntityException(UNAUTHORIZED_TX_TO_USER));
    }

    @Override
    public int regIncidComment(String userName, final IncidComment comment) throws EntityException
    {
        logger.debug("regIncidComment()");
        return of(comment)
                .filter(commentIn -> checkIncidenciaOpen(comment.getIncidencia().getIncidenciaId()))
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
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia. It may have a gcm token in data base.
     * Postconditions:
     * 1. the incidencia is inserted in the DB, if necessary.
     * 2. A data message is sent to the GCM server if the incidencia is inserted.
     * 3. If a gcm token is returned from the gcm server call, it is updated or deleted in DB.
     * 4. If inserted, rowsUpdated counter is increased.
     *
     * @param incidencia : an Incidencia instance with userName.
     * @return Incidencia: the incidencia inserted or the one already in BD.
     * @throws EntityException INCIDENCIA_NOT_REGISTERED (if a SQLException is thrown).
     */
    @Override
    public Incidencia regIncidencia(Incidencia incidencia) throws EntityException
    {
        logger.debug("regIncidencia()");

        // Si la incidencia ya está registrada y abierta, la devolvemos; si no, lanzamos una excepción 'incidencia not found'.
        if (incidencia.getIncidenciaId() > 0L && checkIncidenciaOpen(incidencia.getIncidenciaId())) {
            return incidencia;
        }

        return of(incidencia)
                .filter(incidencia1 -> incidencia.getIncidenciaId() == 0L)
                .map(incidencia1 -> {
                    long pK;
                    try {
                        pK = incidenciaDao.regIncidencia(incidencia1);
                    } catch (SQLException e) {
                        if (e.getMessage().contains(EntityException.COMUNIDAD_FK)) {
                            throw new EntityException(COMUNIDAD_NOT_FOUND);
                        }
                        throw new EntityException(INCIDENCIA_NOT_REGISTERED);
                    }
                    return new Incidencia.IncidenciaBuilder()
                            .copyIncidencia(incidencia1)
                            .incidenciaId(pK)
                            .build();
                })
                .peek(incidencia1 -> {
                    GcmIncidRequestData requestData = new GcmIncidRequestData(incidencia_open_type, incidencia1.getComunidadId());
                    gcmUserComuService.sendGcmMessageToComunidad(incidencia1, requestData);
                }).findFirst().get();
    }

    public int regIncidImportancia(String userName, IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("regIncidImportancia()");
        long incidenciaIdInit = incidImportancia.getIncidencia().getIncidenciaId();

        return of(incidImportancia.getIncidencia())
                .filter(incidenciaIn -> usuarioConnector.checkUserInComunidad(userName, incidenciaIn.getComunidadId()))
                .map(incidenciaIn -> regIncidencia(
                        new Incidencia.IncidenciaBuilder()
                                .copyIncidencia(incidenciaIn)
                                .userName(userName)
                                .build()))
                .map(incidenciaOut -> incidenciaDao.regIncidImportancia(
                        new IncidImportancia.IncidImportanciaBuilder(regIncidencia(incidenciaOut))
                                .usuarioComunidad(usuarioConnector.getUserComunidad(userName, incidenciaOut.getComunidadId()))
                                .importancia(incidImportancia.getImportancia())
                                .build()
                        )
                )
                .map(rowsUpdated -> (incidenciaIdInit == 0L) ? ++rowsUpdated : rowsUpdated)
                .findFirst().get();
    }

    @Override
    public int regResolucion(String userName, Resolucion resolucion) throws EntityException
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
                .findFirst().orElseThrow(() -> new EntityException(UNAUTHORIZED_TX_TO_USER));

        // Asynchronous GCM notification.
        GcmIncidRequestData requestData = new GcmIncidRequestData(resolucion_open_type, resolucion.getComunidadId());
        gcmUserComuService.sendGcmMessageToComunidad(resolucion, requestData);

        return rowsUpdated;
    }

    @Override
    public List<IncidComment> seeCommentsByIncid(long incidenciaId) throws EntityException
    {
        logger.debug("seeCommentsByIncid()");
        List<IncidComment> comments = incidenciaDao.SeeCommentsByIncid(incidenciaId);
        if (comments == null || comments.isEmpty()) {
            // Check if the incidencia exists.
            seeIncidenciaById(incidenciaId);
        }
        return comments;
    }

    @Override
    public Incidencia seeIncidenciaById(long incidenciaId) throws EntityException
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
     * 1. An IncidAndResolBundle instance is returned with the data and highest rol of the user in session:
     * - incidencia.incidenciaId.
     * - incidencia.comunidad.c_Id.
     * - usuarioComunidad.usuario.userName (user in session).
     * - usuarioComunidad.usuario.alias.
     * - usuarioComunidad.usuario.uId.
     * - usuarioComunidad.comunidad.c_Id.
     * - usuarioComunidad.roles (highest rol).
     * - incidImportancia.importancia.
     * - incidImportancia.fechaAlta
     * <p>
     * 2. If the user hasn't registered an incidImportancia record previously, the incidenciaResolBundle contains also:
     * - incidImportancia.importancia == 0.
     * - incidImportancia.fechaAlta == null.
     * Plus:
     * - incidencia.userName (user who registered the incidencia).
     * - incidencia.ambito.ambitoId.
     * - incidencia.fechaAlta.
     * - incidencia.fechaCierre.
     * - incidencia.comunidad.tipoVia.
     * - incidencia.comunidad.nombreVia.
     * - incidencia.comunidad.numero.
     * - incidencia.comunidad.sufijoNumero.
     *
     * @throws EntityException INCIDENCIA_NOT_FOUND (or not open)
     *                         ROLES_NOT_FOUND (if the relationship usuario_comunidad doesn't exist).
     */
    @Override
    public IncidAndResolBundle seeIncidImportancia(final String userNameInSession, final long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportancia()");

        IncidImportancia incidImportancia = of(incidenciaId)
                .filter(this::checkIncidenciaOpen) // If none, we throw at the end INCIDENCIA_NOT_FOUND.
                .map(incidenciaIdIn -> {
                    try {
                        return incidenciaDao.seeIncidImportanciaByUser(userNameInSession, incidenciaIdIn);
                    } catch (EntityException e) {
                        if (e.getExceptionMsg().equals(INCID_IMPORTANCIA_NOT_FOUND)) {
                            return new IncidImportancia.IncidImportanciaBuilder(seeIncidenciaById(incidenciaIdIn))   // INCIDENCIA_NOT_FOUND
                                    .build();
                        }
                        throw e;
                    }
                })
                .map(incidImpIn -> incidImpIn.getUserComu() != null ? incidImpIn : new IncidImportancia.IncidImportanciaBuilder(incidImpIn.getIncidencia())
                        .copyIncidImportancia(incidImpIn)
                        .usuarioComunidad(
                                new UsuarioComunidad.UserComuBuilder(incidImpIn.getIncidencia().getComunidad(), getUsuarioConnector().completeUser(userNameInSession))
                                        .build())
                        .build()
                ).findFirst().orElseThrow(() -> new EntityException(INCIDENCIA_NOT_FOUND));

        return of(incidImportancia)
                .map(incidImpOut -> {
                    Usuario usuarioIn = incidImpOut.getUserComu().getUsuario();
                    Comunidad comunidadIn = incidImpOut.getIncidencia().getComunidad();
                    Incidencia incidenciaIn = incidImpOut.getIncidencia();
                    return new IncidAndResolBundle(
                            new IncidImportancia.IncidImportanciaBuilder(incidenciaIn)
                                    .importancia(incidImpOut.getImportancia())
                                    .usuarioComunidad(new UsuarioComunidad.UserComuBuilder(comunidadIn, usuarioIn)
                                            .userComuRest(incidImpOut.getUserComu())
                                            .roles(getUsuarioConnector().addHighestFunctionalRol(usuarioIn.getUserName(), comunidadIn.getC_Id()))
                                            .build()
                                    )
                                    .fechaAlta(incidImpOut.getFechaAlta())
                                    .build(),
                            isIncidenciaWithResolucion(incidenciaIn.getIncidenciaId())
                    );
                }).findFirst().get();
    }

    @Override
    public List<IncidenciaUser> seeIncidsClosedByComu(long comunidadId)
    {
        logger.debug("seeIncidsClosedByComu()");
        return incidenciaDao.seeIncidsClosedByComu(comunidadId);
    }

    @Override
    public List<IncidenciaUser> seeIncidsOpenByComu(long comunidadId)
    {
        logger.debug("seeIncidsOpenByComu()");
        return incidenciaDao.seeIncidsOpenByComu(comunidadId);
    }

    /**
     * Preconditions:
     * 1. The user is registered in the comunidad of the incidencia.
     * 2. The incidencia can be OPEN or CLOSED.
     * Postconditions:
     *
     * @return resolucion in BD, with comunidad initialized in the incidencia field.
     * @throws EntityException USERCOMU_WRONG_INIT, if the user is not associated to the comunidad.
     * @throws EntityException INCIDENCIA_NOT_FOUND or RESOLUCION_NOT_FOUND, if the resolucionId (incidenciaId) doesn't exist.
     */
    @Override
    public Resolucion seeResolucion(String userName, long resolucionId) throws EntityException  // TODO: testar las dos excepciones.
    {
        logger.debug("seeResolucion()");

        final Incidencia incidencia = seeIncidenciaById(resolucionId);  // INCIDENCIA_NOT_FOUND exception.
        getUsuarioConnector().checkUserInComunidad(userName, incidencia.getComunidadId()); // USERCOMU_WRONG_INIT exception.

        return of(resolucionId)
                .map(incidenciaDao::seeResolucion)  // RESOLUCION_NOT_FOUND exception.
                .map(resolucionIn -> new Resolucion.ResolucionBuilder(
                        new Incidencia.IncidenciaBuilder()
                                .copyIncidencia(incidencia)
                                .comunidad(
                                        new Comunidad.ComunidadBuilder().c_id(incidencia.getComunidadId())
                                                .build())
                                .build())
                        .copyResolucion(resolucionIn)
                        .build())
                .findFirst().get();
    }

    @Override
    public List<ImportanciaUser> seeUserComusImportancia(final String userName, long incidenciaId) throws EntityException
    {
        logger.debug("seeUserComusImportancia()");
        return of(incidenciaId)
                .filter(incidenciaPk -> usuarioConnector.checkUserInComunidad(userName, seeIncidenciaById(incidenciaPk).getComunidad().getC_Id()))
                .map(incidenciaPk -> incidenciaDao.seeUserComusImportancia(incidenciaId))
                .findFirst().get();
    }

    /* ================================ HELPERS ===================================*/

    @Override
    public UserManagerConnector getUsuarioConnector()
    {
        return usuarioConnector;
    }

    @Override
    public boolean checkIncidenciaOpen(long incidenciaId)
    {
        logger.debug("checkIncidenciaOpen()");
        if (!incidenciaDao.isIncidenciaOpen(incidenciaId)) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return true;
    }
}
