package com.didekin.userservice.repository;

import com.didekinlib.model.comunidad.Municipio;
import com.didekinlib.model.comunidad.Provincia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: pedro@didekin
 * Date: 28/05/15
 * Time: 17:49
 */
public class MunicipioDao {

    private static final Logger logger = LoggerFactory.getLogger(MunicipioDao.class.getCanonicalName());

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MunicipioDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    Municipio getMunicipioByPrIdAndMcd(short provinciaId, short municipioCodeInProv)
    {
        logger.info("::getMunicipioByPrIdAndMcd()");

        return jdbcTemplate
                .queryForObject(MunicipioSql.BY_PRID_AND_MCD.toString(), new MunicipioMapper(), provinciaId,
                        municipioCodeInProv);
    }

    Municipio getMunicipioById(int municipioId)
    {
        logger.info("::getMuncipioById()");
        return jdbcTemplate.queryForObject(MunicipioSql.BY_ID.toString(), new MunicipioMapper(),
                municipioId);
    }


    // ............. HELPER CLASSES ...............

    private static class MunicipioMapper implements RowMapper<Municipio> {

        @Override
        public Municipio mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return new Municipio(
                    rs.getInt("m_id"),
                    rs.getShort("m_cd"),
                    rs.getString("nombre"),
                    new Provincia(rs.getShort("pr_id")));
        }
    }
}
