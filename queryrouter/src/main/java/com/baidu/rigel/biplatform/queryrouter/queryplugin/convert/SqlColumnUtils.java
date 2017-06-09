/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.queryrouter.queryplugin.convert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.QueryMeasure;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Select;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.Column;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnCondition;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlConstants;
import com.google.common.collect.Lists;

/**
 * QuestionModel to TableData的工具类
 * 
 * @author luowenlei
 *
 */
public class SqlColumnUtils {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(SqlColumnUtils.class);
    
    /**
     * 获取questionModel中需要查询的Columns
     * 
     * @param Map<String, SqlColumn> allColums 元数据信息
     * @param List<String> selecions 选取的字符串信息
     * 
     * @return List needcolumns hashmap
     */
    public static List<SqlColumn> getSqlNeedColumns(
            QueryMeta queryMeta, List<String> selections) {
        List<SqlColumn> needColumns = Lists.newArrayList();
        List<String> calKeys = Lists.newArrayList();
        if (CollectionUtils.isEmpty(queryMeta.getAllColumns())) {
            return needColumns;
        }
        // 获取列元数据
        for (String selectName : selections) {
            SqlColumn sqlColumn = queryMeta.getSqlColumnByCubeKey(selectName);
            if (sqlColumn.getType() == ColumnType.GROUP && !CollectionUtils.isEmpty(sqlColumn.getKeys())) {
                for (String key : sqlColumn.getKeys()) {
                    needColumns.add(queryMeta.getSqlColumnByCubeKey(key));
                }
            } else if (sqlColumn.getOperator() != null
                    && sqlColumn.getOperator().getAggregator() == Aggregator.CALCULATED) {
                calKeys = getRealMeasureKey(sqlColumn.getOperator().getFormula(), 
                        sqlColumn.getTableName(), calKeys);
            } else {
                needColumns.add(queryMeta.getSqlColumnByCubeKey(selectName));
            }
        }
        // 添加计算列的指标数据
        for (String key : calKeys) {
            SqlColumn sqlColumn = queryMeta.getSqlColumn(key);
            if (sqlColumn != null
                    
                    && !needColumns.contains(sqlColumn)) {
                needColumns.add(sqlColumn);
            }
        }
        return needColumns;
    }
    
    /**
     * 获取questionModel中需要查询的Columns
     * 
     * @param Map<String, SqlColumn> allColums 元数据信息
     * @param List<String> selecions 选取的字符串信息
     * 
     * @return List needcolumns hashmap
     */
    public static List<SqlColumn> getAllNeedColumns(
            QueryMeta queryMeta, List<String> selections) {
        List<SqlColumn> needColumns = Lists.newArrayList();
        List<String> calKeys = Lists.newArrayList();
        if (CollectionUtils.isEmpty(queryMeta.getAllColumns())) {
            return needColumns;
        }
        // 获取列元数据
        for (String selectName : selections) {
            SqlColumn sqlColumn = queryMeta.getSqlColumnByCubeKey(selectName);
            if (sqlColumn.getType() == ColumnType.GROUP && !CollectionUtils.isEmpty(sqlColumn.getKeys())) {
                for (String key : sqlColumn.getKeys()) {
                    needColumns.add(queryMeta.getSqlColumnByCubeKey(key));
                }
            } else if (sqlColumn.getOperator() != null
                    && sqlColumn.getOperator().getAggregator() == Aggregator.CALCULATED
                    && sqlColumn.getType() != ColumnType.MEASURE_CALLBACK) {
                calKeys = getRealMeasureKey(sqlColumn.getOperator().getFormula(), 
                        sqlColumn.getTableName(), calKeys);
            } else {
                needColumns.add(queryMeta.getSqlColumnByCubeKey(selectName));
            }
        }
        // 添加计算列的指标数据
        for (String key : calKeys) {
            SqlColumn sqlColumn = queryMeta.getSqlColumn(key);
            if (sqlColumn != null
                    
                    && !needColumns.contains(sqlColumn)) {
                needColumns.add(sqlColumn);
            }
        }
        return needColumns;
    }
    
    /**
     * getRealMeasureKey
     *
     * @return
     */
    public static List<String> getRealMeasureKey(String cal, String tableName, List<String> keyList) {
        if (StringUtils.isEmpty(cal)) {
            return keyList;
        }
        if (CollectionUtils.isEmpty(keyList)) {
            keyList = Lists.newArrayList();
        }
        if (cal.indexOf("${") < 0) {
            return keyList;
        }
        cal = cal.substring(cal.indexOf("${"));
        String[] keys = StringUtils.split(cal, "${");
        for (String m : keys) {
            String key = m.substring(0, m.indexOf("}"));
            if (!keyList.contains(key)) {
                keyList.add(QueryMeta.getSqlColumnKey(tableName, key));
            }
        }
        return keyList;
    }

    /**
     * getAllColumns
     *
     * @param planeTableQuestionModel
     * @return
     */
    public static QueryMeta getAllColumns(PlaneTableQuestionModel planeTableQuestionModel) {
        return getAllColumns(planeTableQuestionModel.getMetaMap(),
                planeTableQuestionModel.getSource(), planeTableQuestionModel.getQueryConditions());
    }
    
    /**
     * 获取指标及维度中所有的字段信息Formcube
     * 
     * @param cube
     *            cube
     * @return HashMap allcolumns hashmap
     */
    public static QueryMeta getAllColumns(
            Map<String, Column> metaMap, String source, Map<String, MetaCondition> queryConditions) {
        QueryMeta queryMeta = new QueryMeta();
        if (CollectionUtils.isEmpty(metaMap)) { 
            return queryMeta;
        }

        for (Entry<String, Column> entry : metaMap.entrySet()) {
            String k = entry.getKey();
            Column v = entry.getValue();
            SqlColumn sqlColumn = new SqlColumn();
            sqlColumn.setName(v.getName());
            sqlColumn.setOperator(v.getOperator());
            sqlColumn.setTableFieldName(v.getTableFieldName());
            sqlColumn.setTableName(v.getTableName());
            sqlColumn.setSourceTableName(source);
            sqlColumn.setFactTableFieldName(v.getFacttableColumnName());
            sqlColumn.setCaption(v.getCaption());
            sqlColumn.setJoinTable(v.getJoinTable());
            sqlColumn.setType(v.getColumnType());
            sqlColumn.setColumnKey(k);
            sqlColumn.setKeys(v.getKeys());
            sqlColumn.setSortRecord(v.getSortRecord());
            sqlColumn.setRelatedColumn(v.isRelatedColumn());
            if (MapUtils.isNotEmpty(queryConditions) && queryConditions.get(k) != null) {
                sqlColumn.setColumnCondition((ColumnCondition) queryConditions.get(k));
            }
            queryMeta.addSqlColumn(k, sqlColumn);
        }
        boolean hasAlias = needAlias(source, queryMeta.getAllColumns());
        setSqlColumnsSqlUniqueName(source, queryMeta.getAllColumns(), hasAlias, "");
        return queryMeta;
    }
    
    /**
     * 自动识别是否用alias
     *
     * @param sourceTable
     * @param allColumns
     * @return
     */
    public static boolean needAlias(String searchTableName, Collection<SqlColumn> allColumns) {
        // handle single facttable case
        boolean hasJoinTables = false;
        String sourceTableName = null;
        for (SqlColumn v : allColumns) {
            if (v.getType() == ColumnType.JOIN
                    && v.getJoinTable() != null
                    && !v.getJoinTable().getJoinOnList().isEmpty()) {
                sourceTableName = v.getSourceTableName();
                hasJoinTables = true;
                break;
            }
        }
        boolean singleFacttable = !searchTableName.contains(",");
        boolean isSearchSource = searchTableName.equals(sourceTableName);
        if (singleFacttable && hasJoinTables && isSearchSource) {
            return true;
        } else if (!singleFacttable) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * resetAllColumnsSqlUniqueName
     *
     * @param source
     * @param allColumns
     * @return
     */
    public static void setSqlColumnsSqlUniqueName(String source,
            Collection<SqlColumn> sqlColumns, boolean hasAlias, String aliasPlus) {
        if (!hasAlias) {
            logger.info("queryId:{} set properties of 'sqlUniqueColumn' to FactTableFieldName",
                    QueryRouterContext.getQueryId());
        }
        if (aliasPlus == null) {
            aliasPlus = "";
        }
        for (SqlColumn sqlColumn : sqlColumns) {
            if (ColumnType.JOIN == sqlColumn.getType()) {
                if (sqlColumn.getTableName().equals(sqlColumn.getSourceTableName())) {
                    // 退化维
                    if (hasAlias) {
                        sqlColumn.setSqlUniqueColumn(SqlConstants.SOURCE_TABLE_ALIAS_NAME
                                + sqlColumn.getFactTableFieldName());
                    } else {
                        sqlColumn.setSqlUniqueColumn(sqlColumn.getFactTableFieldName() + aliasPlus);
                    }
                } else {
                    // 正常维度
                    if (hasAlias) {
                        sqlColumn
                                .setSqlUniqueColumn(sqlColumn.getTableName() + sqlColumn.getName());
                    } else {
                        sqlColumn.setSqlUniqueColumn(sqlColumn.getName() + aliasPlus);
                    }
                }
            } else {
                if (hasAlias) {
                    sqlColumn.setSqlUniqueColumn(SqlConstants.SOURCE_TABLE_ALIAS_NAME
                            + sqlColumn.getName());
                } else {
                    sqlColumn.setSqlUniqueColumn(sqlColumn.getTableFieldName() + aliasPlus);
                }
            }
        }
    }
    
    /**
     * 将needcolumns 转换成 非join 的事实表的字段。
     *
     * @param sqlColumn
     * @param needAlias
     * @return
     */
    public static List<SqlColumn> getFacttableColumns(QueryMeta queryMeta, Select select,
            String tableName, boolean needAgg, boolean needAlias) {
        List<SqlColumn> factTableColums = Lists.newArrayList();
        for (String needSqlColumnName : select.getQueryProperties()) {
            SqlColumn needSqlColumn = queryMeta.getSqlColumn(tableName, needSqlColumnName);
            factTableColums.add(needSqlColumn);
        }
        for (QueryMeasure queryMeasure : select.getQueryMeasures()) {
            SqlColumn needSqlColumn = queryMeta.getSqlColumn(tableName, queryMeasure.getProperties());
            SqlColumn noJoinColumnsCopy = DeepcopyUtils.deepCopy(needSqlColumn);
            if (!needAgg) {
                setNoAggSqlColumn(noJoinColumnsCopy);
            }
            factTableColums.add(noJoinColumnsCopy);
        }
        return factTableColums;
    }
    
    /**
     * buildColumnCondition
     *
     * @param allColumnMapKey
     * @param type
     * @param values
     * @return
     */
    public static ColumnCondition buildColumnCondition(SQLConditionType type, List<?> values) {
        ColumnCondition columnCondition = new ColumnCondition();
        SQLCondition sqlCondition = new SQLCondition();
        // 目前维度只有in的情况。
        sqlCondition.setCondition(type);
        List<String> vals = Lists.newArrayList();
        for (Object o : values) {
            vals.add(o.toString());
        }
        sqlCondition.setConditionValues(vals);
        columnCondition.setColumnConditions(sqlCondition);
        return columnCondition;
    }
    
    /**
     * isFacttableColumn
     *
     * @param sqlColumn
     * @return
     */
    public static boolean isFacttableColumn(SqlColumn sqlColumn) {
        return sqlColumn.getSourceTableName().equals(sqlColumn.getTableName())
                || sqlColumn.getType() == ColumnType.CALLBACK;
    }
    
    /**
     * getNeedSqlColumns
     *
     * @param sql
     * @return
     */
    public static List<SqlColumn> getNeedSqlColumns(String sql) {
        List<SqlColumn> list = Lists.newArrayList();
        List<String> selectList = getColumnsFromSql(sql);
        for (String select : selectList) {
            SqlColumn sqlColumn = new SqlColumn();
            sqlColumn.setColumnKey(select);
            sqlColumn.setName(select);
            sqlColumn.setDataType("");
            sqlColumn.setSqlUniqueColumn(select);
            sqlColumn.setType(ColumnType.COMMON);
            sqlColumn.setTableName("");
            sqlColumn.setCaption(select);
            list.add(sqlColumn);
        }
        return list;
    }
    
    /**
     * getColumnsFromSql
     *
     * @param sql
     * @return
     */
    public static List<String> getColumnsFromSql(String sql) {
        List<String> columns = Lists.newArrayList();
        sql = sql.toLowerCase();
        String select = StringUtils.substring(sql, sql.indexOf("select") + 6, sql.indexOf("from"));
        String[] selects = StringUtils.split(select, SqlConstants.COMMA);
        for (String s : selects) {
            int start = s.indexOf(" as ");
            if (start < 0) {
                start = 0;
            } else {
                start = start + 4;
            }
            columns.add(StringUtils.trim(StringUtils.substring(s, start, s.length())));
        }
        return columns;
    }
    
    /*
     * setNoAggSqlColumn
     *
     * @param sqlColumn
     */
    protected static void setNoAggSqlColumn(SqlColumn sqlColumn) {
        if (sqlColumn == null
                || sqlColumn.getOperator() == null) {
            return;
        }
        sqlColumn.getOperator().setAggregator(Aggregator.NONE);
        sqlColumn.getOperator().setFormula(null);
    }
}
