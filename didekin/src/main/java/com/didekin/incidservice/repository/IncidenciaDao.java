package com.didekin.incidservice.repository;

import com.didekin.common.repository.EntityException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.didekin.incidservice.repository.IncidenciaSql.CLOSE_INCIDENCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.COUNT_RESOLUCION_BY_INCID;
import static com.didekin.incidservice.repository.IncidenciaSql.DELETE_INCIDENCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.GET_INCID_BY_COMU;
import static com.didekin.incidservice.repository.IncidenciaSql.GET_INCID_BY_PK;
import static com.didekin.incidservice.repository.IncidenciaSql.IS_IMPORTANCIA_USER;
import static com.didekin.incidservice.repository.IncidenciaSql.IS_INCID_OPEN;
import static com.didekin.incidservice.repository.IncidenciaSql.MODIFY_INCIDENCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.MODIFY_INCID_IMPORTANCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.MODIFY_RESOLUCION;
import static com.didekin.incidservice.repository.IncidenciaSql.REG_AVANCE;
import static com.didekin.incidservice.repository.IncidenciaSql.REG_INCID;
import static com.didekin.incidservice.repository.IncidenciaSql.REG_INCID_COMMENT;
import static com.didekin.incidservice.repository.IncidenciaSql.REG_INCID_IMPORTANCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.REG_RESOLUCION;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_AVANCES_BY_RESOLUCION;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_COMMENTS_BY_INCID;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_IMPORTANCIA_BY_USER;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_INCIDS_CLOSED_BY_COMU;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_INCIDS_OPEN_BY_COMU;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_RESOLUCION;
import static com.didekin.incidservice.repository.IncidenciaSql.SEE_USER_COMUS_IMPORTANCIA;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCIDENCIA_NOT_REGISTERED;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.RESOLUCION_DUPLICATE;
import static com.didekinlib.http.incidencia.IncidenciaExceptionMsg.RESOLUCION_NOT_FOUND;
import static com.didekinlib.http.usuario.UsuarioExceptionMsg.USERCOMU_WRONG_INIT;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.stream.Stream.of;

/**
 * User: pedro@didekin
 * Date: 19/11/15
 * Time: 14:02
 */
@Component
public class IncidenciaDao {

    private static final Logger logger = LoggerFactory.getLogger(IncidenciaDao.class.getCanonicalName());

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IncidenciaDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    int closeIncidencia(long incidenciaId) throws EntityException
    {
        logger.debug("closeIncidencia()");
        int rowsUpdated = jdbcTemplate.update(CLOSE_INCIDENCIA.toString(), incidenciaId);
        if (rowsUpdated <= 0) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return rowsUpdated;
    }

    int countResolucionByIncid(long incidenciaId)
    {
        logger.debug("countResolucionByIncid()");
        return jdbcTemplate.queryForObject(COUNT_RESOLUCION_BY_INCID.toString(),
                new Object[]{incidenciaId},
                Integer.class);
    }

    /**
     * The update query controls the existence of a resolucion for the incidencia.
     */
    int deleteIncidencia(long incidenciaId) throws EntityException
    {
        logger.debug("deleteIncidencia()");
        int rowsDeleted = jdbcTemplate.update(DELETE_INCIDENCIA.toString(), incidenciaId);
        if (rowsDeleted <= 0) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return rowsDeleted;
    }

    @SuppressWarnings("SameParameterValue")
    List<Incidencia> getIncidenciasByComu(long comunidadId) throws EntityException
    {
        logger.debug("getIncidenciaByComu()");
        return jdbcTemplate.query(
                GET_INCID_BY_COMU.toString(),
                new Object[]{comunidadId},
                (rs, rowNum) -> new Incidencia.IncidenciaBuilder()
                        .incidenciaId(rs.getLong("incid_id"))
                        .comunidad(new Comunidad.ComunidadBuilder()
                                .c_id(rs.getLong("c_id"))
                                .build())
                        .userName(rs.getString("user_name"))
                        .descripcion(rs.getString("descripcion"))
                        .ambitoIncid(new AmbitoIncidencia(rs.getShort("ambito")))
                        .fechaAlta(rs.getTimestamp("fecha_alta"))
                        .fechaCierre(rs.getTimestamp("fecha_cierre"))
                        .build()
        );
    }

    int isImportanciaUser(long userId, long incidenciaId)
    {
        logger.debug("isImportanciaUser()");
        return jdbcTemplate.queryForObject(IS_IMPORTANCIA_USER.toString(), new Object[]{userId, incidenciaId}, Integer.class);
    }

    boolean isIncidenciaOpen(long incidenciaId)
    {
        logger.debug("isIncidenciaOpen()");
        int rowsSelected;
        try {
            rowsSelected = jdbcTemplate.queryForObject(IS_INCID_OPEN.toString(),
                    new Object[]{incidenciaId},
                    Integer.class);
        } catch (EmptyResultDataAccessException e) {
            rowsSelected = 0;
        }
        return rowsSelected > 0;
    }

    /**
     * Preconditions:
     * 2. The incidencia is OPEN (checked in sql query).
     * Postconditions:
     * 1. The incidencia is modified in BD.
     *
     * @param incidencia : an Incidencia instance with incidenciaId, descripcion and ambitoId.
     * @return number of rows modified in DB (0 or 1).
     * @throws EntityException INCIDENCIA_NOT_FOUND (or closed).
     */
    int modifyIncidencia(Incidencia incidencia)
    {
        logger.debug("modifyIncidencia()");

        int rowsModified = jdbcTemplate.update(MODIFY_INCIDENCIA.toString(),
                incidencia.getDescripcion(),
                incidencia.getAmbitoIncidencia().getAmbitoId(),
                incidencia.getIncidenciaId());

        if (rowsModified < 1) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return rowsModified;
    }

    /**
     * Control of the state 'incidencia is open' is made in the sql query.
     *
     * @return number of rows updated: 1 without new avance; 2 with new avance.
     * @throws EntityException INCIDENCIA_NOT_FOUND if incidencia is closed or doesn't exist.
     */
    int modifyResolucion(Resolucion resolucion) throws EntityException
    {
        logger.debug("modifyResolucion()");

        return of(resolucion)
                .mapToInt(
                        resolucion1 -> {
                            int updateCount = jdbcTemplate.update(
                                    MODIFY_RESOLUCION.toString(),
                                    resolucion1.getFechaPrev(),
                                    resolucion1.getCosteEstimado(),
                                    resolucion1.getIncidencia().getIncidenciaId()
                            );
                            List<Avance> avances = resolucion1.getAvances();
                            return (avances == null || avances.size() == 0) ? updateCount : (updateCount + regAvance(resolucion1.getIncidencia().getIncidenciaId(), avances.get(0)));
                        }
                ).filter(rowsUpdated -> rowsUpdated > 0)
                .findFirst().orElseThrow(() -> new EntityException(INCIDENCIA_NOT_FOUND));
    }

    /**
     * @return 1 if incidImportancia is updated; 0 if not (incidencia is closed, p.e.)
     */
    int modifyIncidImportancia(IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("modifyIncidImportancia()");
        int updatedRow;
        updatedRow = jdbcTemplate.update(MODIFY_INCID_IMPORTANCIA.toString(),
                incidImportancia.getImportancia(),
                incidImportancia.getIncidencia().getIncidenciaId(),
                incidImportancia.getIncidencia().getComunidad().getC_Id(),
                incidImportancia.getUserComu().getUsuario().getuId());
        return updatedRow;
    }

    int regAvance(final long incidenciaId, final Avance avance) throws EntityException
    {
        logger.debug("regAvance()");
        try {
            return jdbcTemplate.update(REG_AVANCE.toString(),
                    incidenciaId,
                    avance.getAvanceDesc(),
                    avance.getUserName());
        } catch (DataAccessException e) {
            if (e.getMessage().contains("FOREIGN KEY (`incid_id`)")) {
                throw new EntityException(RESOLUCION_NOT_FOUND);
            }
            throw e;
        }
    }

    int regIncidComment(IncidComment comment) throws EntityException
    {
        logger.debug("regIncidComment()");
        int insertedRow;
        try {
            insertedRow = jdbcTemplate.update(REG_INCID_COMMENT.toString(),
                    comment.getIncidencia().getIncidenciaId(),
                    comment.getIncidencia().getComunidad().getC_Id(),
                    comment.getRedactor().getuId(),
                    comment.getDescripcion());
        } catch (DataAccessException de) {
            doCatchIncidenciaUserIntegrity(de);
            throw de;
        }
        return insertedRow;
    }

    /**
     * @return incidenciaId.
     */
    long regIncidencia(Incidencia incidencia) throws SQLException, EntityException
    {
        logger.debug("regIncidencia()");
        ResultSet rs;
        long incidenciaPk;

        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement regIncidencia = conn.prepareStatement(REG_INCID.toString(), RETURN_GENERATED_KEYS)) {

            regIncidencia.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
            regIncidencia.setLong(2, incidencia.getComunidad().getC_Id());
            regIncidencia.setString(3, incidencia.getUserName());
            regIncidencia.setString(4, incidencia.getDescripcion());
            regIncidencia.setShort(5, incidencia.getAmbitoIncidencia().getAmbitoId());
            regIncidencia.executeUpdate();

            rs = regIncidencia.getGeneratedKeys();
            if (rs.next()) {
                incidenciaPk = rs.getLong(1);
            } else {
                throw new EntityException(INCIDENCIA_NOT_REGISTERED);
            }
        }
        return incidenciaPk;
    }

    int regIncidImportancia(IncidImportancia incidImportancia) throws EntityException
    {
        logger.debug("regIncidImportancia()");

        int rowInserted;
        try {
            rowInserted = jdbcTemplate.update(REG_INCID_IMPORTANCIA.toString(),
                    incidImportancia.getIncidencia().getIncidenciaId(),
                    incidImportancia.getUserComu().getComunidad().getC_Id(),
                    incidImportancia.getUserComu().getUsuario().getuId(),
                    incidImportancia.getImportancia());
        } catch (DataAccessException de) {
            doCatchIncidenciaUserIntegrity(de);
            throw de;
        }
        return rowInserted;
    }

    int regResolucion(Resolucion resolucion) throws EntityException
    {
        logger.debug("regResolucion()");
        int insertRow;
        try {
            insertRow = jdbcTemplate.update(REG_RESOLUCION.toString(),
                    resolucion.getIncidencia().getIncidenciaId(),
                    resolucion.getUserName(),
                    resolucion.getDescripcion(),
                    resolucion.getCosteEstimado(),
                    resolucion.getFechaPrev());
        } catch (DataAccessException e) {
            doCatchDuplicateResolucionForIncid(e);
            doCatchIncidenciaUserIntegrity(e);
            throw e;
        }
        return insertRow;
    }

    List<Avance> seeAvancesByResolucion(long resolucionId)
    {
        logger.debug("seeAvancesByResolucion()");
        return jdbcTemplate.query(SEE_AVANCES_BY_RESOLUCION.toString(), new Object[]{resolucionId}, new AvanceMapper());
    }

    List<IncidComment> SeeCommentsByIncid(long incidenciaId)
    {
        logger.debug("seeCommentsByIncid()");
        return jdbcTemplate.query(SEE_COMMENTS_BY_INCID.toString(),
                new Object[]{incidenciaId},
                new IncidCommentMapper());
    }

    /**
     * This method queries the incidencia_comunidad_view exclusively; therefore data pertaining to the user level,
     * such as the importancia_avg are not available in the returned object.
     * <p>
     * Postconditions:
     * 1. An Incidencia instance is returned with:
     * - incidenciaId.
     * - comunidad.c_Id.
     * - comunidad.tipoVia.
     * - comunidad.nombreVia
     * - comunidad.numero.
     * - comunidad.sufijoNumero
     * - userName
     * - descripcion
     * - fechaAlta
     * - fechaCierre (NULL if it's open).
     */
    Incidencia seeIncidenciaById(long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidenciaById()");
        Incidencia incidencia;

        try {
            incidencia = jdbcTemplate.queryForObject(
                    GET_INCID_BY_PK.toString(),
                    (rs, rowNum) -> new Incidencia.IncidenciaBuilder()
                            .incidenciaId(rs.getLong("incid_id"))
                            .comunidad(new Comunidad.ComunidadBuilder()
                                    .c_id(rs.getLong("c_id"))
                                    .tipoVia(rs.getString("tipo_via"))
                                    .nombreVia(rs.getString("nombre_via"))
                                    .numero(rs.getShort("numero"))
                                    .sufijoNumero(rs.getString("sufijo_numero"))
                                    .build())
                            .userName(rs.getString("user_name"))
                            .descripcion(rs.getString("descripcion"))
                            .ambitoIncid(new AmbitoIncidencia(rs.getShort("ambito")))
                            .fechaAlta(rs.getTimestamp("fecha_alta"))
                            .fechaCierre(rs.getTimestamp("fecha_cierre"))
                            .build(),
                    incidenciaId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
        return incidencia;
    }

    /**
     * Preconditions:
     * 1. The incidencia is open.
     * Postconditions:
     *
     * @return an IncidAndResolBundle instance is returned with:
     * - incidImportancia.incidencia.incidenciaId.
     * - incidImportancia.incidencia.userName (user who registered the incidencia).
     * - incidImportancia.incidencia.descripcion.
     * - incidImportancia.incidencia.ambito.ambitoId.
     * - incidImportancia.incidencia.fechaAlta.
     * - incidImportancia.incidencia.comunidad.c_Id.
     * - incidImportancia.incidencia.comunidad.tipoVia.
     * - incidImportancia.incidencia.comunidad.nombreVia.
     * - incidImportancia.incidencia.comunidad.numero.
     * - incidImportancia.incidencia.comunidad.sufijoNumero.
     * - incidImportancia.usuarioComunidad.usuario.uId.
     * - incidImportancia.usuarioComunidad.usuario.userName (user who ranked the importancia in this instance-record).
     * - incidImportancia.usuarioComunidad.usuario.alias.
     * - incidImportancia.usuarioComunidad.roles.
     * - incidImportancia.importancia.
     * - incidImportancia.fechaAlta
     * - hasResolucion (from fechaAltaResolucion == null?).
     * @throws EntityException INCID_IMPORTANCIA_NOT_FOUND, if there isn't incidImportancia record for the user or the incidencia is closed.
     */
    IncidAndResolBundle seeIncidImportanciaByUser(String userName, long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportanciaByUser()");

        try {
            return jdbcTemplate.queryForObject(
                    SEE_IMPORTANCIA_BY_USER.toString(),
                    new Object[]{userName, incidenciaId},
                    (rs, rowNum) -> doIncidImpResolView(rs));
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(INCID_IMPORTANCIA_NOT_FOUND);
        }
    }

    /**
     * Postconditions:
     * A list of the closed incidencias in the comunidad, with fechaAlta NOT OLDER than 2 years,
     * is returned as a list of IncidenciaUser instances, with the following fields:
     * - incidencia.incidenciaId.
     * - incidencia.comunidad.c_id.
     * - incidencia.userName (user who registered the incidencia).
     * - incidencia.descripcion.
     * - incidencia.ambito.ambitoId.
     * - incidencia.importanciaAvg.
     * - incidencia.fechaAlta.
     * - incidencia.fechaCierre (not null, by definition).
     * - usuario.uId. (user who registered the incidencia)
     * - usuario.userName.
     * - usuario.alias.
     */
    List<IncidenciaUser> seeIncidsClosedByComu(long comunidadId)
    {
        logger.debug("seeIncidsClosedByComu()");
        return jdbcTemplate.query(SEE_INCIDS_CLOSED_BY_COMU.toString(), new Object[]{comunidadId}, (rs, rowNum) -> doBasicIncidenciaUser(rs));
    }

    /**
     * Postconditions:
     * 1. A list of the open incidencias in the comunidad, as produced by
     * {@link #seeIncidsClosedByComu(long comunidadId) seeIncidsClosedByComu method}
     * plus:
     * - incidenciaUser.fechaAltaResolucion.
     */
    List<IncidenciaUser> seeIncidsOpenByComu(long comunidadId)
    {
        logger.debug("seeIncidsOpenByComu()");
        return jdbcTemplate.query(SEE_INCIDS_OPEN_BY_COMU.toString(),
                new Object[]{comunidadId},
                (rs, rowNum) -> {
                    final IncidenciaUser incidenciaUserIn = doBasicIncidenciaUser(rs);
                    return new IncidenciaUser.IncidenciaUserBuilder(incidenciaUserIn.getIncidencia())
                            .copyIncidUser(incidenciaUserIn)
                            .fechaAltaResolucion(rs.getTimestamp("res_fecha_alta"))
                            .build();
                }
        );
    }

    /**
     * @return 1. a resolucion instance is returned with:
     * - incidencia.incidencia_id
     * - userName.
     * - descripcion.
     * - costeEstimado.
     * - costeFinal.
     * - fechaAlta.
     * - fechaPrevista.
     * - moraleja.
     * - avances (avance.avanceDesc, avance.userName, avance.fechaAlta)
     * 2. null, if the resolucion does not exist.
     */
    Resolucion seeResolucion(final long incidenciaId) throws EntityException
    {
        logger.debug("seeResolucion()");

        try {
            return jdbcTemplate.queryForObject(SEE_RESOLUCION.toString(),
                    new Object[]{incidenciaId},
                    (rs, rowNum) -> new Resolucion.ResolucionBuilder(
                            new Incidencia.IncidenciaBuilder()
                                    .incidenciaId(rs.getLong("incid_id"))
                                    .build())
                            .userName(rs.getString("user_name"))
                            .descripcion(rs.getString("plan"))
                            .costeEstimado(rs.getInt("coste_estimado"))
                            .costeReal(rs.getInt("coste"))
                            .fechaAlta(rs.getTimestamp("fecha_alta"))
                            .fechaPrevista(rs.getTimestamp("fecha_prevista"))
                            .moraleja(rs.getString("moraleja"))
                            .avances(seeAvancesByResolucion(incidenciaId))
                            .build());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * @return list of ImportanciaUser instances with:
     * - userAlias
     * - importancia
     * @throws EntityException INCIDENCIA_NOT_FOUND.
     */
    @SuppressWarnings("SimplifyStreamApiCallChains")
    List<ImportanciaUser> seeUserComusImportancia(long incidenciaId)
    {
        logger.debug("seeUserComusImportancia()");
        return of(
                jdbcTemplate.query(SEE_USER_COMUS_IMPORTANCIA.toString(),
                        new Object[]{incidenciaId},
                        (rs, rowNum) -> new ImportanciaUser(rs.getString("alias"), rs.getShort("importancia")))
        ).findFirst()
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new EntityException(INCIDENCIA_NOT_FOUND));
    }

//    =========================== MAPPERS ==================================

    private static class IncidCommentMapper implements RowMapper<IncidComment> {

        @Override
        public IncidComment mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return new IncidComment.IncidCommentBuilder()
                    .commentId(rs.getLong("comment_id"))
                    .descripcion(rs.getString("descripcion"))
                    .incidencia(
                            new Incidencia.IncidenciaBuilder()
                                    .incidenciaId(rs.getLong("incid_id"))
                                    .comunidad(
                                            new Comunidad.ComunidadBuilder()
                                                    .c_id(rs.getLong("c_id"))
                                                    .build()
                                    )
                                    .build()
                    )
                    .redactor(doUsuarioFromDb(rs))
                    .fechaAlta(rs.getTimestamp("fecha_alta"))
                    .build();
        }
    }

    private static class AvanceMapper implements RowMapper<Avance> {
        @Override
        public Avance mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return new Avance.AvanceBuilder()
                    .avanceId(rs.getLong("avance_id"))
                    .avanceDesc(rs.getString("descripcion"))
                    .author(
                            new Usuario.UsuarioBuilder()
                                    .userName(rs.getString("user_name"))
                                    .alias(rs.getString("alias"))
                                    .build())
                    .fechaAlta(rs.getTimestamp("fecha_alta"))
                    .build();
        }
    }

    //    =========================== HELPERS ==================================

    private static IncidenciaUser doBasicIncidenciaUser(ResultSet rs) throws SQLException
    {
        final Incidencia incidencia = new Incidencia.IncidenciaBuilder()
                .incidenciaId(rs.getLong("incid_id"))
                .comunidad(new Comunidad.ComunidadBuilder()
                        .c_id(rs.getLong("c_id")).build())
                .userName(rs.getString("user_name"))
                .descripcion(rs.getString("descripcion"))
                .ambitoIncid(new AmbitoIncidencia(rs.getShort("ambito")))
                .importanciaAvg(rs.getFloat("importancia_avg"))
                .fechaAlta(rs.getTimestamp("fecha_alta"))
                .fechaCierre(rs.getTimestamp("fecha_cierre"))
                .build();

        final Usuario usuario = new Usuario.UsuarioBuilder()
                .uId(rs.getLong("u_id"))
                .userName(rs.getString("user_name"))
                .alias(rs.getString("alias"))
                .build();

        return new IncidenciaUser.IncidenciaUserBuilder(incidencia)
                .usuario(usuario)
                .build();
    }

    private void doCatchIncidenciaUserIntegrity(DataAccessException de) throws EntityException
    {
        if (de.getMessage().contains("FOREIGN KEY (`c_id`, `u_id`)")) {
            logger.error(de.getMessage());
            throw new EntityException(USERCOMU_WRONG_INIT);
        }
        if (de.getMessage().contains("FOREIGN KEY (`incid_id`)")) {
            logger.error(de.getMessage());
            throw new EntityException(INCIDENCIA_NOT_FOUND);
        }
    }

    private void doCatchDuplicateResolucionForIncid(DataAccessException e) throws EntityException
    {
        if (e.getMessage().contains(EntityException.DUPLICATE_ENTRY) && e.getMessage().contains("key 'PRIMARY'")) {
            logger.error(e.getMessage());
            throw new EntityException(RESOLUCION_DUPLICATE);
        }
    }

    private static IncidAndResolBundle doIncidImpResolView(ResultSet rs) throws SQLException
    {
        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                .c_id(rs.getLong("c_id"))
                .tipoVia(rs.getString("comunidad_tipo_via"))
                .nombreVia(rs.getString("comunidad_nombre_via"))
                .numero(rs.getShort("comunidad_numero"))
                .sufijoNumero(rs.getString("comunidad_sufijo"))
                .build();

        IncidImportancia incidImportancia = new IncidImportancia.IncidImportanciaBuilder(
                new Incidencia.IncidenciaBuilder()
                        .incidenciaId(rs.getLong("incid_id"))
                        .comunidad(comunidad)
                        .userName(rs.getString("incid_user_initiator"))
                        .descripcion(rs.getString("descripcion"))
                        .ambitoIncid(new AmbitoIncidencia(rs.getShort("ambito")))
                        .fechaAlta(rs.getTimestamp("fecha_alta_incidencia"))
                        .build())
                .usuarioComunidad(
                        new UsuarioComunidad.UserComuBuilder(
                                comunidad,
                                doUsuarioFromDb(rs))
                                .roles(rs.getString("roles"))
                                .build())
                .importancia(rs.getShort("importancia"))
                .fechaAlta(rs.getTimestamp("fecha_alta"))
                .build();

        return new IncidAndResolBundle(incidImportancia, rs.getTimestamp("fecha_alta_resolucion") != null);
    }

    private static Usuario doUsuarioFromDb(ResultSet rs) throws SQLException
    {
        return new Usuario.UsuarioBuilder()
                .uId(rs.getLong("u_id"))
                .userName(rs.getString("user_name"))
                .alias(rs.getString("alias"))
                .build();
    }
}