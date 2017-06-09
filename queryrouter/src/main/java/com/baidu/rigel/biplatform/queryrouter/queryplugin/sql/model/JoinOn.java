package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

/**
 * sql JoinOn
 * 
 * @author luowenlei
 *
 */
public class JoinOn extends SqlSegment {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6180677386102168725L;
    
    /**
     * joinTableName
     */
    private String joinTableName;
    
    /**
     * joinTableFieldName
     */
    private String joinTableFieldName;

    /**
     * facttableName
     */
    private String facttableName;
    
    /**
     * complexOperator
     */
    private String facttableColumnName;

    
    
    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment#getSql()
     */
    @Override
    public String getSql() {
        StringBuffer sql = new StringBuffer("");
        sql.append(SqlConstants.SPACE);
        sql.append(SqlConstants.AND);
        sql.append(SqlConstants.SPACE);
        sql.append(joinTableName);
        sql.append(SqlConstants.DOT);
        sql.append(joinTableFieldName);
        sql.append(SqlConstants.SPACE);
        sql.append(SqlConstants.EQUALS);
        sql.append(SqlConstants.SPACE);
        sql.append(facttableName);
        sql.append(SqlConstants.DOT);
        sql.append(facttableColumnName);
        sql.append(SqlConstants.SPACE);
        return this.getFormatSql(sql.toString());
    }

    /**
     * default generate get joinTableName
     * @return the joinTableName
     */
    public String getJoinTableName() {
        return joinTableName;
    }

    /**
     * default generate set joinTableName
     * @param joinTableName the joinTableName to set
     */
    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    /**
     * default generate get facttableName
     * @return the facttableName
     */
    public String getFacttableName() {
        return facttableName;
    }

    /**
     * default generate set facttableName
     * @param facttableName the facttableName to set
     */
    public void setFacttableName(String facttableName) {
        this.facttableName = facttableName;
    }

    /**
     * default generate get joinTableFieldName
     * @return the joinTableFieldName
     */
    public String getJoinTableFieldName() {
        return joinTableFieldName;
    }

    /**
     * default generate set joinTableFieldName
     * @param joinTableFieldName the joinTableFieldName to set
     */
    public void setJoinTableFieldName(String joinTableFieldName) {
        this.joinTableFieldName = joinTableFieldName;
    }

    /**
     * default generate get facttableColumnName
     * @return the facttableColumnName
     */
    public String getFacttableColumnName() {
        return facttableColumnName;
    }

    /**
     * default generate set facttableColumnName
     * @param facttableColumnName the facttableColumnName to set
     */
    public void setFacttableColumnName(String facttableColumnName) {
        this.facttableColumnName = facttableColumnName;
    }
}
