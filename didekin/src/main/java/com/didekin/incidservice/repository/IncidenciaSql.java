package com.didekin.incidservice.repository;

/**
 * User: pedro
 * Date: 31/03/15
 * Time: 14:21
 */
enum IncidenciaSql {

    CLOSE_INCIDENCIA("UPDATE " + IncidTables.INCID_COMU_TB + " SET fecha_cierre = NOW() " +
            " WHERE incid_id = ? and fecha_cierre IS NULL"),

    COUNT_RESOLUCION_BY_INCID("select count(*) from " + IncidTables.INCID_RESOLUCION_TB + " where incid_id = ?"),

    DELETE_INCIDENCIA("DELETE ic FROM " + IncidTables.INCID_COMU_TB + " AS ic " +
            " left join " + IncidTables.INCID_RESOLUCION_TB + " AS ir ON ic.incid_id = ir.incid_id" +
            " WHERE ir.incid_id IS NULL AND  ic.incid_id = ?"),

    GET_INCID_BY_COMU("SELECT * FROM " + IncidTables.INCID_COMU_TB + " WHERE c_id = ?"),

    GET_INCID_BY_PK("select ic.* " +
            "from " + IncidTables.INCID_COMU_VIEW + " AS ic " +
            "where ic.incid_id = ? "),

    IS_INCID_OPEN("SELECT incid_id FROM " + IncidTables.INCID_COMU_TB +
            " WHERE incid_id = ? AND fecha_cierre IS NULL"),

    MODIFY_INCID_IMPORTANCIA("UPDATE " + IncidTables.INCID_IMPORTANCIA_TB + " as ii " +
            " left join " + IncidTables.INCID_COMU_TB + " as ic ON ii.incid_id = ic.incid_id" +
            " SET ii.importancia = ? " +
            " WHERE ii.incid_id = ? AND ii.c_id = ? AND ii.u_id = ? AND ic.fecha_cierre IS NULL"),

    MODIFY_INCIDENCIA("UPDATE " + IncidTables.INCID_COMU_TB + " SET descripcion = ?," +
            " ambito = ?" +
            " WHERE incid_id = ? AND fecha_cierre IS NULL"),

    MODIFY_RESOLUCION("UPDATE " + IncidTables.INCID_RESOLUCION_TB + " as ir " +
            " left join " + IncidTables.INCID_COMU_TB + " as ic ON ir.incid_id = ic.incid_id " +
            " SET ir.fecha_prevista = ?, ir.coste_estimado = ? " +
            " WHERE ir.incid_id = ? AND ic.fecha_cierre IS NULL"),

    REG_AVANCE("insert into " + IncidTables.INCID_RESOLUCION_AVANCE_TB + " (incid_id, descripcion, user_name) VALUES (?,?,?)"),

    REG_INCID("insert into " + IncidTables.INCID_COMU_TB + " (incid_id, c_id, user_name, descripcion, ambito, fecha_alta) VALUES (?,?,?,?,?,?)"),

    REG_INCID_COMMENT("INSERT INTO " + IncidTables.INCID_COMMENT_TB +
            " (incid_id, c_id, u_id, descripcion) " +
            "VALUES (?,?,?,?)"),

    REG_INCID_IMPORTANCIA("INSERT INTO " + IncidTables.INCID_IMPORTANCIA_TB +
            " (incid_id, c_id, u_id, importancia) VALUES (?,?,?,?)"),

    REG_RESOLUCION("INSERT INTO " + IncidTables.INCID_RESOLUCION_TB +
            " (incid_id, user_name, plan, coste_estimado, fecha_prevista) " +
            " VALUES (?,?,?,?,?)"),

    SEE_AVANCES_BY_RESOLUCION("select * from " + IncidTables.INCID_RESOLUCION_AVANCE_TB +
            " where incid_id = ? order by fecha_alta"),

    SEE_COMMENTS_BY_INCID("select comment_id, descripcion, fecha_alta," +
            " incid_id, c_id, " +
            " u_id, user_name, alias " +
            " from " + IncidTables.INCID_COMMENT_VIEW + " where incid_id = ? " +
            " order by fecha_alta"),

    SEE_IMPORTANCIA_BY_USER("select * from " + IncidTables.INCID_IMPORTANCIA_USER_VIEW +
            " where user_name = ? and incid_id = ?"),

    SEE_INCIDS_CLOSED_BY_COMU("select " +
            " i.*," +
            " ivg.importancia_avg " +
            " from " + IncidTables.INCID_USER_ALTA_VIEW + " AS i " +
            " LEFT join " + IncidTables.INCID_AVG_VIEW + " AS ivg " +
            " on i.incid_id = ivg.incid_id " +
            " where i.c_id = ? and i.fecha_cierre IS NOT NULL and i.fecha_alta > DATE_SUB(NOW(), INTERVAL 2 YEAR) " +
            " order by i.fecha_alta"),

    SEE_INCIDS_OPEN_BY_COMU("select " +
            " i.*, " +
            " ivg.importancia_avg, " +
            " re.fecha_alta AS 'res_fecha_alta' " +
            " from " + IncidTables.INCID_USER_ALTA_VIEW + " AS i " +
            " LEFT join " + IncidTables.INCID_RESOLUCION_TB + " AS re " +
            " USING (incid_id) " +
            " LEFT join " + IncidTables.INCID_AVG_VIEW + " AS ivg " +
            " USING (incid_id) " +
            " where i.c_id = ? and i.fecha_cierre IS NULL " +
            " order by i.fecha_alta"),

    SEE_RESOLUCION("select * from " + IncidTables.INCID_RESOLUCION_TB + " where incid_id = ?"),

    SEE_USER_COMUS_IMPORTANCIA("select alias, importancia from " + IncidTables.INCID_IMPORTANCIA_USER_VIEW +
            " where incid_id = ? ORDER by alias"),;

    String sqlText;

    IncidenciaSql(String sqlText)
    {
        this.sqlText = sqlText;
    }

    public String toString()
    {
        return sqlText;
    }
}