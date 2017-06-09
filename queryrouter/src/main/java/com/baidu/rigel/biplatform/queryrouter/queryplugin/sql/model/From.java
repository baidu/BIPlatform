package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

/**
 * sql From
 * 
 * @author luowenlei
 *
 */
public class From extends SqlSegment {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8882672890101351785L;
    
    /**
     * tableName
     */
    private String tableName;

    /**
     * default generate get tableName
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * default generate set tableName
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public From(boolean hasAlias) {
        super(hasAlias);
        // TODO Auto-generated constructor stub
    }

}
