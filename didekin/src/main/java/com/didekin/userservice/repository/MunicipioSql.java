package com.didekin.userservice.repository;

/**
 * User: pedro@didekin
 * Date: 28/05/15
 * Time: 17:52
 */
public enum MunicipioSql {

    BY_PRID_AND_MCD("SELECT * FROM municipio WHERE pr_id = ? AND m_cd = ?"),
    BY_ID("SELECT * FROM municipio WHERE m_id = ?"),;

    String sqlText;

    MunicipioSql(String sqlText)
    {
        this.sqlText = sqlText;
    }

    public String toString()
    {
        return sqlText;
    }
}
