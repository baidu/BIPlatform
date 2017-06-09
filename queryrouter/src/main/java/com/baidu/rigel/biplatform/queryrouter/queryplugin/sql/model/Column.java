package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.model.Operator;

/**
 * 
 * Description: 平面表数据表列元数据信息描述
 * 
 * @author 罗文磊
 *
 */
public class Column implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3151301875582323397L;
    
    /**
     * name
     */
    private String name;
    
    /**
     * tableFieldName
     */
    public String tableFieldName;
    
    /**
     * joinTableFieldName
     */
    private String joinTableFieldName;
    
    /**
     * columnType
     */
    private ColumnType columnType;
    
    /**
     * operator
     */
    private Operator operator;
    
    /**
     * complexOperator
     */
    private String facttableName;
    
    /**
     * complexOperator
     */
    private String facttableColumnName;
    
    /**
     * key
     */
    private String key;
    
    /**
     * caption
     */
    private String caption;
    
    /**
     * tableName
     */
    private String tableName;
    
    /**
     * 保留字段
     */
    private String dbName;
    
    /**
     * join表
     */
    private JoinTable joinTable;
    
    /**
     * group维度时，存放allcolumn key
     */
    private List<String> keys;
    
    /**
     * isRelatedColumn
     */
    private boolean isRelatedColumn = false;

    
    /**
     * default generate get tableFieldName
     * @return the tableFieldName
     */
    public String getTableFieldName() {
        return tableFieldName;
    }

    /**
     * default generate set tableFieldName
     * @param tableFieldName the tableFieldName to set
     */
    public void setTableFieldName(String tableFieldName) {
        this.tableFieldName = tableFieldName;
    }

    /**
     * sortRecord
     */
    private SortRecord sortRecord;
    
    /**
     * default generate get sortRecord
     * @return the sortRecord
     */
    public SortRecord getSortRecord() {
        return sortRecord;
    }

    /**
     * default generate set sortRecord
     * @param sortRecord the sortRecord to set
     */
    public void setSortRecord(SortRecord sortRecord) {
        this.sortRecord = sortRecord;
    }
    
    /**
     * default generate get keys
     * @return the keys
     */
    public List<String> getKeys() {
        return keys;
    }

    /**
     * default generate get isRelatedColumn
     * @return the isRelatedColumn
     */
    public boolean isRelatedColumn() {
        return isRelatedColumn;
    }



    /**
     * default generate set isRelatedColumn
     * @param isRelatedColumn the isRelatedColumn to set
     */
    public void setRelatedColumn(boolean isRelatedColumn) {
        this.isRelatedColumn = isRelatedColumn;
    }



    /**
     * default generate set keys
     * @param keys the keys to set
     */
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    /**
     * default generate get joinTable
     * @return the joinTable
     */
    public JoinTable getJoinTable() {
        return joinTable;
    }

    /**
     * default generate set joinTable
     * @param joinTable the joinTable to set
     */
    public void setJoinTable(JoinTable joinTable) {
        this.joinTable = joinTable;
    }

    /**
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the columnType
     */
    public ColumnType getColumnType() {
        return columnType;
    }
    
    /**
     * @param columnType
     *            the columnType to set
     */
    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }
    
    /**
     * @return the joinTableFieldName
     */
    public String getJoinTableFieldName() {
        return joinTableFieldName;
    }
    
    /**
     * @param joinTableFieldName
     *            the joinTableFieldName to set
     */
    public void setJoinTableFieldName(String joinTableFieldName) {
        this.joinTableFieldName = joinTableFieldName;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the operator
     */
    public Operator operator() {
        return operator;
    }
    
    /**
     * @param operator
     *            the operator to set
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    
    /**
     * @return the facttableName
     */
    public String getFacttableName() {
        return facttableName;
    }
    
    /**
     * @param facttableName
     *            the facttableName to set
     */
    public void setFacttableName(String facttableName) {
        this.facttableName = facttableName;
    }
    
    /**
     * @return the facttableColumnName
     */
    public String getFacttableColumnName() {
        return facttableColumnName;
    }
    
    /**
     * @param facttableColumnName
     *            the facttableColumnName to set
     */
    public void setFacttableColumnName(String facttableColumnName) {
        this.facttableColumnName = facttableColumnName;
    }
    
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * @param caption
     *            the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }
    
    /**
     * @param dbName
     *            the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    
    public Column(String key, String name, String caption, String tableName) {
        super();
        this.key = key;
        this.name = name;
        this.caption = caption;
        this.tableName = tableName;
    }
    
    public Column() {
        super();
    }
}