package com.didekin.userservice.repository;

import com.didekin.common.repository.ServiceException;
import com.didekinlib.model.comunidad.Comunidad;
import com.didekinlib.model.comunidad.ComunidadAutonoma;
import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;
import com.didekinlib.model.usuariocomunidad.UsuarioComunidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.didekin.common.repository.ServiceException.GENERATED_KEY;
import static com.didekin.userservice.repository.ComunidadSql.BY_ID;
import static com.didekin.userservice.repository.ComunidadSql.COUNT_BY_USERCOMU;
import static com.didekin.userservice.repository.ComunidadSql.INSERT;
import static com.didekin.userservice.repository.ComunidadSql.INSERT_USUARIO;
import static com.didekin.userservice.repository.ComunidadSql.MODIFY_COMU;
import static com.didekinlib.http.comunidad.ComunidadExceptionMsg.COMUNIDAD_NOT_FOUND;
import static java.sql.Statement.RETURN_GENERATED_KEYS;


/**
 * User: pedro@didekin
 * Date: 18/04/15
 * Time: 10:55
 */
public class ComunidadDao {

    private static final Logger logger = LoggerFactory.getLogger(ComunidadDao.class.getCanonicalName());

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private MunicipioDao municipioDao;

    @Autowired
    public ComunidadDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    //    ============================================================
    //    ............Package-local methods ..........................
    //    ============================================================

    boolean deleteComunidad(Comunidad comunidad) throws ServiceException
    {
        logger.info("::deleteComunidad()");

        int rowsDeleted = jdbcTemplate.update(ComunidadSql.DELETE_BY_ID.toString(), comunidad.getC_Id());
        if (!(rowsDeleted == 1)) {
            throw new ServiceException(COMUNIDAD_NOT_FOUND);
        }
        return Boolean.TRUE;
    }

    boolean existsUserComu(long comunidadId, long userId)
    {
        logger.debug("existsUserComu()");
        int rowsCount = jdbcTemplate.queryForObject(COUNT_BY_USERCOMU.toString(), new Object[]{comunidadId, userId}, Integer.class);
        return (rowsCount > 0);
    }

    Comunidad getComunidadById(long idComunidad) throws ServiceException
    {
        logger.info("::getComunidadById");
        Comunidad comunidad;
        try {
            comunidad = jdbcTemplate.queryForObject(BY_ID.toString(),
                    new ComunidadMapperWithCA(), idComunidad);
        } catch (EmptyResultDataAccessException e) {
            logger.info("::getComunidadById(); the comunidad does not exist.");
            throw new ServiceException(COMUNIDAD_NOT_FOUND);
        }
        return comunidad;
    }

    int getMunicipioId(Municipio municipio)
    {
        logger.info("::getMunicipioId()");
        Municipio municipio1 = municipioDao.getMunicipioByPrIdAndMcd(municipio.getProvincia().getProvinciaId(),
                municipio.getCodInProvincia());
        return municipio1.getmId();
    }

    List<Long> getUsuariosIdFromComunidad(Comunidad comunidad)
    {
        logger.info("::getUsuariosIdFromComunidad()");
        return jdbcTemplate.queryForList(ComunidadSql.USERS_ID_BY_COMU.toString(),
                Long.class, comunidad.getC_Id());
    }

    /**
     * Returns el new PK.
     */
    long insertComunidad(Comunidad comunidad, Connection conn) throws SQLException
    {
        logger.info("insertComunidad()");

        ResultSet rs;
        long pkValue;

        try (PreparedStatement insertComunidad = conn.prepareStatement(INSERT.toString(), RETURN_GENERATED_KEYS)) {

            insertComunidad.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
            insertComunidad.setString(2, comunidad.getTipoVia());
            insertComunidad.setString(3, comunidad.getNombreVia());
            insertComunidad.setShort(4, comunidad.getNumero());
            insertComunidad.setString(5, comunidad.getSufijoNumero());
            insertComunidad.setInt(6, getMunicipioId(comunidad.getMunicipio()));
            insertComunidad.executeUpdate();

            rs = insertComunidad.getGeneratedKeys();
            if (rs.next()) {
                pkValue = rs.getLong(1);
            } else {
                throw new SQLException(GENERATED_KEY);
            }
        }
        return pkValue;
    }

    int insertUsuarioComunidad(UsuarioComunidad usuarioCom, Connection conn) throws SQLException
    {
        logger.info("insertUsuarioComunidad()");

        int rowsInserted;

        try (PreparedStatement insertUsuarioCom = conn.prepareStatement(INSERT_USUARIO.toString())) {
            insertUsuarioCom.setLong(1, usuarioCom.getComunidad().getC_Id());
            insertUsuarioCom.setLong(2, usuarioCom.getUsuario().getuId());
            insertUsuarioCom.setString(3, usuarioCom.getPortal());
            insertUsuarioCom.setString(4, usuarioCom.getEscalera());
            insertUsuarioCom.setString(5, usuarioCom.getPlanta());
            insertUsuarioCom.setString(6, usuarioCom.getPuerta());
            insertUsuarioCom.setString(7, usuarioCom.getRoles());
            rowsInserted = insertUsuarioCom.executeUpdate();
        }
        return rowsInserted;
    }

    int insertUsuarioComunidad(UsuarioComunidad usuarioComunidad)
    {
        logger.info("insertUsuarioComunidad(usuarioComunidad)");
        int rowInserted;

        rowInserted = jdbcTemplate.update(
                INSERT_USUARIO.toString(),
                usuarioComunidad.getComunidad().getC_Id(),
                usuarioComunidad.getUsuario().getuId(),
                usuarioComunidad.getPortal(),
                usuarioComunidad.getEscalera(),
                usuarioComunidad.getPlanta(),
                usuarioComunidad.getPuerta(),
                usuarioComunidad.getRoles()
        );
        return rowInserted;
    }

    int modifyComuData(Comunidad comunidad)
    {
        logger.debug("modifyComuData()");

        return jdbcTemplate.update(MODIFY_COMU.toString(),
                comunidad.getTipoVia(),
                comunidad.getNombreVia(),
                comunidad.getNumero(),
                comunidad.getSufijoNumero(),
                getMunicipioId(comunidad.getMunicipio()),
                comunidad.getC_Id());
    }

    List<Comunidad> searchComunidadOne(Comunidad comunidad)
    {
        logger.info("::searchComunidadOne()");

        return jdbcTemplate.query(ComunidadSql.SEARCH_ONE.toString(), new ComunidadMapper(),
                comunidad.getTipoVia(),
                comunidad.getNombreVia(),
                comunidad.getNumero(),
                comunidad.getMunicipio().getProvincia().getProvinciaId(),
                comunidad.getMunicipio().getCodInProvincia());
    }

    List<Comunidad> searchComunidadTwo(Comunidad comunidad)
    {
        logger.info("::searchComunidadTwo()");
        return jdbcTemplate.query(ComunidadSql.SEARCH_TWO.toString(), new ComunidadMapper(),
                comunidad.getNombreVia(),
                comunidad.getNumero(),
                comunidad.getMunicipio().getProvincia().getProvinciaId(),
                comunidad.getMunicipio().getCodInProvincia());
    }

    List<Comunidad> searchComunidadThree(Comunidad comunidad)
    {
        logger.info("::searchComunidadThree()");
        String param3_4 = comunidad.getNombreVia();
        String param5 = "%" + comunidad.getNombreVia() + "%";

        return jdbcTemplate.query(ComunidadSql.SEARCH_THREE.toString(), new ComunidadMapper(),
                param3_4,
                param3_4,
                param5,
                comunidad.getNumero(),
                comunidad.getMunicipio().getProvincia().getProvinciaId(),
                comunidad.getMunicipio().getCodInProvincia());
    }

    // ............. HELPER CLASSES ...............

    static class ComunidadMapper implements RowMapper<Comunidad> {
        @Override
        public Comunidad mapRow(ResultSet resultSet, int i) throws SQLException
        {
            return new Comunidad.ComunidadBuilder()
                    .c_id(resultSet.getLong("c_Id"))
                    .tipoVia(resultSet.getString("tipo_via"))
                    .nombreVia(resultSet.getString("nombre_via"))
                    .numero(resultSet.getShort("numero"))
                    .sufijoNumero(resultSet.getString("sufijo_numero"))
                    .fechaAlta(resultSet.getTimestamp("fecha_alta"))
                    .municipio(
                            new Municipio(
                                    resultSet.getShort("m_cd"),
                                    resultSet.getString("m_nombre"),
                                    new Provincia(
                                            resultSet.getShort("pr_id"),
                                            resultSet.getString("pr_nombre")
                                    )
                            )
                    )
                    .build();
        }
    }

    private static class ComunidadMapperWithCA implements RowMapper<Comunidad> {

        @Override
        public Comunidad mapRow(ResultSet resultSet, int i) throws SQLException
        {
            return new Comunidad.ComunidadBuilder()
                    .c_id(resultSet.getLong("c_Id"))
                    .tipoVia(resultSet.getString("tipo_via"))
                    .nombreVia(resultSet.getString("nombre_via"))
                    .numero(resultSet.getShort("numero"))
                    .sufijoNumero(resultSet.getString("sufijo_numero"))
                    .fechaAlta(resultSet.getTimestamp("fecha_alta"))
                    .municipio(
                            new Municipio(
                                    resultSet.getShort("m_cd"),
                                    resultSet.getString("m_nombre"),
                                    new Provincia(
                                            new ComunidadAutonoma(resultSet.getShort("ca_id")),
                                            resultSet.getShort("pr_id"),
                                            resultSet.getString("pr_nombre")
                                    )
                            )
                    )
                    .build();
        }
    }
}