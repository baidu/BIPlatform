package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;

import org.springframework.util.StringUtils;

/**
 * sql Segment
 * 
 * @author luowenlei
 *
 */
public class SqlSegment implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8185627906626742508L;

    /**
     * sql
     */
    private String sql;
    
    /**
     * hasAlias
     */
    private boolean hasAlias;

    /**
     * @param hasAlias
     */
    public SqlSegment(boolean hasAlias) {
        this.hasAlias = hasAlias;
    }
    
    /**
     * @param hasAlias
     */
    public SqlSegment() {
        this.hasAlias = false;
    }
    
    /**
     * getSql
     * 
     * @return the sql
     */
    public String getSql() {
        if (sql == null) {
            sql = "";
        }
        return sql;
    }

    /**
     * setSql
     * 
     * @param sql
     *            the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * default generate get hasAlias
     * @return the hasAlias
     */
    public boolean isHasAlias() {
        return hasAlias;
    }

    /**
     * default generate set hasAlias
     * @param hasAlias the hasAlias to set
     */
    public void setHasAlias(boolean hasAlias) {
        this.hasAlias = hasAlias;
    }
    
    /**
     * getFormatSql
     *
     * @param sql
     * @return
     */
    public String getFormatSql(String sql) {
        sql = StringUtils.replace(sql, "  ", " ");
        sql = StringUtils.replace(sql, "  ", " ");
        return sql;
    }
    
    /**
     * clear
     *
     */
    public void clear() {
        this.sql = "";
    }
}
