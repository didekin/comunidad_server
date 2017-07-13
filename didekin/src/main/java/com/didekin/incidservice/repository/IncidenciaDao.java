package com.didekin.incidservice.repository;

import com.didekin.common.EntityException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.incidencia.dominio.AmbitoIncidencia;
import com.didekinlib.model.incidencia.dominio.Avance;
import com.didekinlib.model.incidencia.dominio.ImportanciaUser;
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
import java.sql.Statement;
import java.util.List;

import static com.didekin.incidservice.repository.IncidenciaSql.CLOSE_INCIDENCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.COUNT_RESOLUCION_BY_INCID;
import static com.didekin.incidservice.repository.IncidenciaSql.DELETE_INCIDENCIA;
import static com.didekin.incidservice.repository.IncidenciaSql.GET_INCID_BY_COMU;
import static com.didekin.incidservice.repository.IncidenciaSql.GET_INCID_BY_PK;
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
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCIDENCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.INCID_IMPORTANCIA_NOT_FOUND;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.RESOLUCION_DUPLICATE;
import static com.didekinlib.model.incidencia.dominio.IncidenciaExceptionMsg.RESOLUCION_NOT_FOUND;
import static com.didekinlib.model.usuariocomunidad.UsuarioComunidadExceptionMsg.USERCOMU_WRONG_INIT;

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

    int closeIncidencia(long incidenciaId)
    {
        logger.debug("closeIncidencia()");
        return jdbcTemplate.update(CLOSE_INCIDENCIA.toString(), incidenciaId);
    }

    int countResolucionByIncid(long incidenciaId)
    {
        logger.debug("countResolucionByIncid()");
        return jdbcTemplate.queryForObject(COUNT_RESOLUCION_BY_INCID.toString(),
                new Object[]{incidenciaId},
                Integer.class);
    }

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

    int modifyIncidencia(Incidencia incidencia)
    {
        logger.debug("modifyIncidencia()");
        return jdbcTemplate.update(MODIFY_INCIDENCIA.toString(),
                incidencia.getDescripcion(),
                incidencia.getAmbitoIncidencia().getAmbitoId(),
                incidencia.getIncidenciaId());
    }

    int modifyResolucion(Resolucion resolucion)
    {
        logger.debug("modifyResolucion()");
        return jdbcTemplate.update(MODIFY_RESOLUCION.toString(),
                resolucion.getFechaPrev(),
                resolucion.getCosteEstimado(),
                resolucion.getIncidencia().getIncidenciaId());
    }

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

    long regIncidencia(Incidencia incidencia) throws SQLException
    {
        logger.debug("regIncidencia()");
        ResultSet rs;
        long incidenciaPk;

        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement regIncidencia = conn.prepareStatement(REG_INCID.toString(), Statement.RETURN_GENERATED_KEYS)) {

            regIncidencia.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
            regIncidencia.setLong(2, incidencia.getComunidad().getC_Id());
            regIncidencia.setString(3, incidencia.getUserName());
            regIncidencia.setString(4, incidencia.getDescripcion());
            regIncidencia.setShort(5, incidencia.getAmbitoIncidencia().getAmbitoId());
            regIncidencia.setNull(6, JDBCType.TIMESTAMP.getVendorTypeNumber());
            regIncidencia.executeUpdate();

            rs = regIncidencia.getGeneratedKeys();
            if (rs.next()) {
                incidenciaPk = rs.getLong(1);
            } else {
                throw new SQLException(EntityException.GENERATED_KEY);
            }
        }

        return incidenciaPk;
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

    IncidImportancia seeIncidImportanciaByUser(String userName, long incidenciaId) throws EntityException
    {
        logger.debug("seeIncidImportancia()");
        IncidImportancia incidImportancia;
        try {
            incidImportancia = jdbcTemplate.queryForObject(
                    SEE_IMPORTANCIA_BY_USER.toString(),
                    new Object[]{userName, incidenciaId},
                    (rs, rowNum) -> {
                        Comunidad comunidad = new Comunidad.ComunidadBuilder()
                                .c_id(rs.getLong("c_id"))
                                .build();

                        return new IncidImportancia.IncidImportanciaBuilder(
                                new Incidencia.IncidenciaBuilder()
                                        .incidenciaId(rs.getLong("incid_id"))
                                        .comunidad(comunidad)
                                        .build())
                                .usuarioComunidad(
                                        new UsuarioComunidad.UserComuBuilder(
                                                comunidad,
                                                new Usuario.UsuarioBuilder()
                                                        .uId(rs.getLong("u_id"))
                                                        .userName(rs.getString("user_name"))
                                                        .alias(rs.getString("alias"))
                                                        .build())
                                                .build())
                                .importancia(rs.getShort("importancia"))
                                .fechaAlta(rs.getTimestamp("fecha_alta"))
                                .build();
                    });
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(INCID_IMPORTANCIA_NOT_FOUND);
        }
        return incidImportancia;
    }

    List<IncidenciaUser> seeIncidsClosedByComu(long comunidadId)
    {
        logger.debug("seeIncidsClosedByComu()");
        return jdbcTemplate.query(SEE_INCIDS_CLOSED_BY_COMU.toString(), new Object[]{comunidadId}, (rs, rowNum) -> doBasicIncidenciaUser(rs));
    }

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

    Resolucion seeResolucion(final long resolucionId) throws EntityException
    {
        logger.debug("seeResolucion()");

        try {
            return jdbcTemplate.queryForObject(SEE_RESOLUCION.toString(),
                    new Object[]{resolucionId},
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
                            .avances(seeAvancesByResolucion(resolucionId))
                            .build());
        } catch (EmptyResultDataAccessException e) {
            throw new EntityException(RESOLUCION_NOT_FOUND);
        }
    }

    List<ImportanciaUser> seeUserComusImportancia(long incidenciaId)
    {
        logger.debug("seeUserComusImportancia()");
        return jdbcTemplate.query(SEE_USER_COMUS_IMPORTANCIA.toString(),
                new Object[]{incidenciaId},
                (rs, rowNum) -> new ImportanciaUser(rs.getString("alias"), rs.getShort("importancia"))
        );
    }

//    =========================== HELPERS ==================================

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
                    .redactor(
                            new Usuario.UsuarioBuilder()
                                    .uId(rs.getLong("u_id"))
                                    .userName(rs.getString("user_name"))
                                    .alias(rs.getString("alias"))
                                    .build()
                    )
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
                    .userName(rs.getString("user_name"))
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

        Usuario usuario = null;

        if (rs.getString("alias") != null) {
            usuario = new Usuario.UsuarioBuilder()
                    .uId(rs.getLong("u_id"))
                    .alias(rs.getString("alias"))
                    .build();
        }

        return new IncidenciaUser.IncidenciaUserBuilder(incidencia)
                .usuario(usuario)
                .build();
    }
}