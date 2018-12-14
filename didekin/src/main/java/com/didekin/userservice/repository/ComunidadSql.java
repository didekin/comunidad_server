package com.didekin.userservice.repository;

/**
 * User: pedro@didekin
 * Date: 19/04/15
 * Time: 10:17
 */
public enum ComunidadSql {

    BY_ID("SELECT * FROM comunidades_municipio_vw WHERE c_id = ?"),

    COUNT_BY_USERCOMU("select COUNT(*) from comunidad_miembro " +
            " where c_id = ? " +
            " and u_id = ? "),

    INSERT("INSERT INTO comunidad (c_id, tipo_via, nombre_via, numero, sufijo_numero, m_id) VALUES (?,?,?,?,?,?)"),

    INSERT_USUARIO("INSERT INTO comunidad_miembro (c_id, u_id, portal, escalera, planta, puerta) VALUES (?,?,?,?,?,?)"), /* Con PKs.*/

    DELETE_BY_ID("DELETE FROM comunidad WHERE c_id = ?"),

    MODIFY_COMU("UPDATE comunidad SET " +
            " tipo_via = ?," +
            " nombre_via = ?," +
            " numero = ?," +
            " sufijo_numero = ?," +
            " m_id = ? " +
            " WHERE c_id = ?"),

    // Taking into account everything in comunidad.
    SEARCH_ONE("SELECT * FROM comunidades_municipio_vw" +
            " WHERE tipo_via = ? " +
            " AND nombre_via = ? " +
            " AND numero = ? " +
            " AND pr_id = ? " +
            " AND m_cd = ?"),

    // Not taking into account tipo_via.
    SEARCH_TWO("SELECT * FROM comunidades_municipio_vw" +
            " WHERE nombre_via = ? " +
            " AND numero = ? " +
            " AND pr_id = ? " +
            " AND m_cd = ? " +
            " ORDER BY tipo_via, nombre_via LIMIT 100"),

    // Matching substrings of the nombre_via.
    SEARCH_THREE("SELECT * FROM comunidades_municipio_vw" +
            " WHERE " +
            " (nombre_via = RIGHT(?,length(nombre_via)) " +
            " OR nombre_via = LEFT(?,length(nombre_via)) " +
            " OR nombre_via LIKE ? ) " +
            " AND numero = ? " +
            " AND pr_id = ? " +
            " AND m_cd = ? " +
            " ORDER BY tipo_via, nombre_via LIMIT 100"),

    USERS_ID_BY_COMU("select u_id from comunidad_miembro as cu " +
            " where cu.c_id = ?"),;

    String sqlText;

    ComunidadSql(String sqlText)
    {
        this.sqlText = sqlText;
    }

    @Override
    public String toString()
    {
        return sqlText;
    }
}