package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * sql QueryMeta
 * 
 * @author luowenlei
 *
 */
public class QueryMeta implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6789273423760758962L;
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 以Cube的key为对象存储数据
     */
    private ConcurrentMap<String, SqlColumn> cubeColumns = Maps.newConcurrentMap();
    
    /**
     * 以table+columns的key为对象存储数据
     */
    private ConcurrentMap<String, SqlColumn> sqlColumns = Maps.newConcurrentMap();
    
    /**
     * getSqlColumnByCubeKey
     *
     * @param key
     * @return
     */
    public SqlColumn getSqlColumnByCubeKey(String key) {
        return cubeColumns.get(key);
    }
    
    /**
     * getSqlColumn
     *
     * @param key
     * @return
     */
    public SqlColumn getSqlColumn(String key) {
        return sqlColumns.get(key);
    }
    
    /**
     * getSqlColumn
     *
     * @param tableName
     * @param tableFieldName
     * @return
     */
    public SqlColumn getSqlColumn(String tableName, String tableFieldName) {
        return sqlColumns.get(getSqlColumnKey(tableName, tableFieldName));
    }
    
    /**
     * getAllColumns
     *
     * @return
     */
    public Collection<SqlColumn> getAllColumns() {
        return cubeColumns.values();
    }
    
    /**
     * findSqlColumns
     *
     * @param tableName
     * @param tableFields
     * @return
     */
    public List<SqlColumn> findSqlColumns(String tableName, Collection<String> tableFields) {
        List<SqlColumn> sqlColumns = Lists.newArrayList();
        for (String tableField : tableFields) {
            SqlColumn sqlColumn = this.getSqlColumn(tableName, tableField);
            if (sqlColumn != null) {
                sqlColumns.add(sqlColumn);
            }
        }
        return sqlColumns;
    }
    
    /**
     * findSqlColumns
     *
     * @param cubeSelectionIds
     * @return
     */
    public Collection<SqlColumn> findSqlColumns(List<String> cubeSelectionIds) {
        Collection<SqlColumn> sqlColumns = Lists.newArrayList();
        for (String cubeSelectionId : cubeSelectionIds) {
            SqlColumn sqlColumn = this.getSqlColumn(cubeSelectionId);
            if (sqlColumn != null) {
                sqlColumns.add(sqlColumn);
            }
        }
        return sqlColumns;
    }
    
    /**
     * getAllColumns
     *
     * @return
     */
    public void addSqlColumn(String cubeKey, SqlColumn sqlColumn) {
        if (cubeColumns.get(cubeKey) != null) {
            logger.warn("cubeColumns contents cubekey:{}", cubeKey);
        }
        cubeColumns.put(cubeKey, sqlColumn);
        if (sqlColumn.getType() == ColumnType.COMMON || sqlColumn.getType() == ColumnType.JOIN) {
            String sqlKey = getSqlColumnKey(sqlColumn.getTableName(), sqlColumn.getTableFieldName());
            if (sqlColumns.get(sqlKey) != null) {
                logger.warn("cubeColumns contents sqlkey:{}", sqlKey);
            }
            sqlColumns.put(sqlKey, sqlColumn);
        }
    }
    
    /**
     * getSqlColumnKey
     *
     * @param tableName
     * @param tableFieldName
     * @return
     */
    public static String getSqlColumnKey(String tableName, String tableFieldName) {
        return "[" + tableName + "].[" + tableFieldName + "]";
    }
}
