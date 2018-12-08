package com.didekin.userservice.repository;

import static com.didekin.userservice.repository.UsuarioTables.USUARIO_COMUNIDAD_TB;
import static com.didekin.userservice.repository.UsuarioTables.USUARIO_TB;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 14:21
 */
enum UsuarioSql {

    COMUS_BY_USER("select c_id, tipo_via, nombre_via, numero, sufijo_numero, " +
            " portal, escalera, planta, puerta, " +
            " m_cd, m_nombre, " +
            " pr_id, pr_nombre" +
            " from usuarios_comunidades_view " +
            " where user_name = ? " +
            " ORDER BY pr_id, m_cd, nombre_via"),

    DELETE_GCM_TOKEN("UPDATE usuario SET " +
            " gcm_token = NULL " +
            " WHERE gcm_token = ?"),

    DELETE_BY_NAME("DELETE FROM usuario WHERE user_name = ?"),

    DELETE_USER_COMUNIDAD("DELETE FROM usuario_comunidad " +
            " WHERE c_id = ? AND u_id = ?"),

    GCM_TOKENS_BY_COMUNIDAD("SELECT gcm_token " +
            "FROM usuario AS u " +
            "INNER JOIN usuario_comunidad AS cu " +
            "INNER JOIN comunidad AS c " +
            "ON u.u_id = cu.u_id " +
            "AND cu.c_id = c.c_id " +
            "WHERE c.c_id = ? " +
            "AND gcm_token IS NOT NULL " +
            "ORDER BY gcm_token"),

    INSERT("INSERT INTO usuario(u_id, alias, password, user_name, gcm_token) values (?,?,?,?,?)"),

    IS_USER_IN_COMUNIDAD("SELECT COUNT(*) FROM " + USUARIO_TB +
            " INNER JOIN " + USUARIO_COMUNIDAD_TB +
            " USING (u_id) " +
            " WHERE user_name = ? AND c_id = ?"),

    MAX_PK("SELECT MAX(u_id) AS u_id FROM usuario"),

    MODIFY_USER("UPDATE usuario SET " +
            " alias = ?," +
            " user_name = ?, " +
            " password = ? " +
            " WHERE u_id = ?"),

    MODIFY_USER_ALIAS("UPDATE usuario SET " +
            " alias = ? " +
            " WHERE u_id = ?"),

    MODIFY_GCM_TOKEN_BY_TOKEN("UPDATE usuario SET " +
            " gcm_token = ? " +
            " WHERE gcm_token = ?"),

    MODIFY_USERCOMU("UPDATE usuario_comunidad SET " +
            " portal = ?," +
            " escalera = ?," +
            " planta = ?," +
            " puerta = ? " +
            " WHERE c_id = ? AND u_id = ?"),

    NEW_PASSWORD("UPDATE usuario SET " +
            " password = ? " +
            " WHERE u_id = ?"),

    OLDEST_USER_COMU("SELECT u_id " +
            " FROM usuario_comunidad " +
            " WHERE c_id = ? " +
            " ORDER BY fecha_alta ASC LIMIT 1"),

    PK("u_id"),

    ROLES_ALL_FUNC("SELECT DISTINCT cu.roles " +
            " FROM usuario as u INNER JOIN usuario_comunidad AS cu " +
            " ON u.u_id = cu.u_id " +
            " WHERE u.user_name = ?"),

    UPDATE_TOKEN_AUTH_BY_NAME("UPDATE usuario SET " +
            " token_auth = ? " +
            " WHERE user_name = ?"),

    UPDATE_TOKENS_GCM_AUTH_BY_ID("UPDATE usuario SET " +
            " gcm_token = ?, " +
            " token_auth = ? " +
            " WHERE u_id = ?"),

    USER_BY_EMAIL("SELECT u_id, alias, user_name, gcm_token, password, token_auth " +
            " FROM usuario as u WHERE u.user_name = ?"),

    USER_BY_ID("SELECT u_id, alias, user_name, gcm_token, password, token_auth " +
            " FROM usuario WHERE u_id = ?"),

    USERCOMU_BY_COMU("select * from usuarios_comunidades_view" +
            " where user_name = ? AND c_id = ? "),

    USERCOMUS_BY_COMU("select c_id, u_id, user_name, alias," +
            " portal, escalera, planta, puerta " +
            " from usuarios_comunidades_view" +
            " where c_id = ? " +
            " ORDER BY alias"),

    USERCOMU_BY_EMAIL("SELECT u.u_id, u.alias, u.user_name, cu.c_id, cu.roles " +
            " FROM usuario as u INNER JOIN usuario_comunidad AS cu " +
            " USING (u_id) " +
            " WHERE u.user_name = ? AND cu.c_id = ?"),

    USERCOMUS_BY_USER("select * from usuarios_comunidades_view" +
            " where user_name = ? " +
            " ORDER BY pr_id, m_cd, nombre_via, tipo_via"),
    ;

    String sqlText;

    UsuarioSql(String sqlText)
    {
        this.sqlText = sqlText;
    }

    public String toString()
    {
        return sqlText;
    }
}