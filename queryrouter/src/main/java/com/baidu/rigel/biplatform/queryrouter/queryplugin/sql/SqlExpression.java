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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.PlaneTableUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.SqlColumnUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnCondition;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.Join;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QuestionModelTransformationException;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SingleWhere;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SourceSingleWhere;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlConstants;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlQuery;

/**
 * 
 * Description: sql数据组织类
 * 
 * @author 罗文磊
 *
 */
public class SqlExpression implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3151301875582323398L;
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * driver
     */
    private String driver;
    
    /**
     * sqlQuery
     */
    private SqlQuery sqlQuery;
    
    /**
     * sqlQuery
     */
    private SqlQuery countSqlQuery;
    
    /**
     * queryMeta
     */
    private QueryMeta queryMeta;
    
    /**
     * needColums
     */
    private List<SqlColumn> needColums;
    
    /**
     * 是否包含alias
     */
    public boolean hasAlias = false;
    
    /**
     * 事实表名称
     */
    private String tableName;
    
    /**
     * 生成组织sql string,带join语句
     * 
     * @param questionModel
     *            questionModel
     * @param allColums
     *            allColums
     * @param needColums
     *            needColums
     * @return String sql
     */
    public void generateSql(QuestionModel questionModel)
            throws QuestionModelTransformationException {
        PlaneTableQuestionModel planeTableQuestionModel = null;
        try {
            if (questionModel instanceof PlaneTableQuestionModel) {
                planeTableQuestionModel = (PlaneTableQuestionModel) questionModel;
            } else if (questionModel instanceof ConfigQuestionModel) {
                planeTableQuestionModel = PlaneTableUtils
                        .convertConfigQuestionModel2PtQuestionModel((ConfigQuestionModel) questionModel);
            } else {
                throw new QuestionModelTransformationException("can not handle type:"
                        + questionModel.getClass().getName());
            }
            // 检查语法及修正语法
            this.checkParameter(planeTableQuestionModel.getQueryConditions(), queryMeta.getAllColumns(),
                    needColums, false);
            sqlQuery.getSelect().setSelectList(this.getNeedColums());
            
            sqlQuery.getFrom().setSql(generateFromExpression(sqlQuery));
            sqlQuery.getFrom().setTableName(this.getTableName());
            
            sqlQuery.setJoin(generateLeftOuterJoinExpression(
                    planeTableQuestionModel.getQueryConditions(), queryMeta.getAllColumns()));
            
            generateTotalWhereExpression(sqlQuery, queryMeta.getAllColumns(), true);
            this.generateOrderByExpression(sqlQuery, this.hasAlias);
            if (questionModel.getPageInfo() != null) {
                sqlQuery.setPageInfo(questionModel.getPageInfo());
            }
        } catch (Exception e) {
            logger.error("queryId:{} occur sql exception:{}", QueryRouterContext.getQueryId(),
                    e.getMessage());
            throw e;
        }
    }
    
    /**
     * 生成组织sql count string
     * 
     * @param questionModel
     *            questionModel
     * @param allColums
     *            allColums
     * @param needColums
     *            needColums
     * @param whereData
     *            whereData
     * @return String sql
     */
    public void generateCountSql(PlaneTableQuestionModel questionModel,
            Collection<SqlColumn> sqlColumns, List<SqlColumn> needColums,
            Map<String, List<Object>> whereData) throws QuestionModelTransformationException {
        try {
            this.checkParameter(questionModel.getQueryConditions(), queryMeta.getAllColumns(), needColums, false);
            countSqlQuery.getSelect().setSelectList(this.getNeedColums());
            
            countSqlQuery.getFrom().setSql(generateFromExpression(countSqlQuery));
            countSqlQuery.getFrom().setTableName(this.getTableName());
            generateTotalWhereExpressionWithValue(this.countSqlQuery, sqlColumns,
                    SQLConditionType.IN, whereData, false);
            this.generateTotalWhereExpression(countSqlQuery, queryMeta.getAllColumns(), false);
        } catch (Exception e) {
            logger.error("queryId:{} occur sql exception:{}", QueryRouterContext.getQueryId(),
                    e.getMessage());
            throw e;
        }
    }
    
    /**
     * generateNoJoinSql
     *
     * @param needColumns
     * @param sortRecord
     * @param pageInfo
     * @param whereData
     * @param needAgg
     * @param needAlias
     */
    public void generateNoJoinSql(List<SqlColumn> needColumns, SortRecord sortRecord,
            PageInfo pageInfo, Map<String, List<Object>> whereData, boolean needAlias) {
        try {
            sqlQuery.getSelect().setHasAlias(false);
            sqlQuery.getSelect().setSelectList(needColumns);
            
            sqlQuery.getFrom().setSql(generateFromExpression(sqlQuery));
            sqlQuery.getFrom().setTableName(this.getTableName());
            sqlQuery.getWhere().clear();
            generateTotalWhereExpressionWithValue(this.sqlQuery,
                    queryMeta.getAllColumns(), SQLConditionType.IN,
                    whereData, this.hasAlias);
            if (pageInfo != null) {
                sqlQuery.setPageInfo(pageInfo);
            }
        } catch (Exception e) {
            logger.error("queryId:{} occur sql exception:{}", QueryRouterContext.getQueryId(),
                    e.getMessage());
            throw e;
        }
    }
    
    /**
     * 生成组织sql string
     * 
     * @param questionModel
     *            questionModel
     * @param allColums
     *            allColums
     * @param needColums
     *            needColums
     * @return String sql
     */
    public void generateNoJoinSql(QuestionModel questionModel, List<SqlColumn> needColumns,
            Map<String, List<Object>> whereData, boolean needAlias)
            throws QuestionModelTransformationException {
        PlaneTableQuestionModel planeTableQuestionModel = null;
        try {
            if (questionModel instanceof PlaneTableQuestionModel) {
                planeTableQuestionModel = (PlaneTableQuestionModel) questionModel;
            } else if (questionModel instanceof ConfigQuestionModel) {
                planeTableQuestionModel = PlaneTableUtils
                        .convertConfigQuestionModel2PtQuestionModel((ConfigQuestionModel) questionModel);
                
            } else {
                throw new QuestionModelTransformationException("can not handle type:"
                        + questionModel.getClass().getName());
            }
            // 检查语法及修正语法
            this.checkParameter(planeTableQuestionModel.getQueryConditions(),
                    queryMeta.getAllColumns(),
                    needColumns, true);
            this.generateNoJoinSql(needColumns, planeTableQuestionModel.getSortRecord(),
                    planeTableQuestionModel.getPageInfo(), whereData, needAlias);
        } catch (Exception e) {
            logger.error("queryId:{} occur sql exception:{}", QueryRouterContext.getQueryId(),
                    e.getMessage());
            throw e;
        }
    }
    
    /**
     * checkParameter
     *
     * @param questionModel
     * @param allColums
     * @param needColums
     */
    protected void checkParameter(Map<String, MetaCondition> queryConditions,
            Collection<SqlColumn> sqlColumns, List<SqlColumn> needColums, boolean isGenNoJoinSql) {
        if (CollectionUtils.isEmpty(needColums)) {
            throw new QuestionModelTransformationException(
                    "List needColums is empty, there is no SqlColum object available to generate.");
        }
        if (CollectionUtils.isEmpty(sqlColumns)) {
            throw new QuestionModelTransformationException(
                    "Map allColums is empty, there is no SqlColum object available to generate.");
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new QuestionModelTransformationException("cube.getSource() can not be empty");
        }
        String[] tableNames = tableName.split(",");
        if (isGenNoJoinSql) {
            this.setHasAlias(false);
        } else {
            if (tableNames.length > 1) {
                // 为多事实表，如果是，hasAlias需要变为true
                if (this.hasAlias == true) {
                    logger.warn("sql form:{} is a multiFacttable,"
                            + "can not be set hasAlias = false, System change back to 'True'",
                            tableName);
                }
                this.setHasAlias(true);
            } else {
                // 判断是否有join，如果有，hasAlias需要变为true
                Join join = generateLeftOuterJoinExpression(queryConditions, sqlColumns);
                if (CollectionUtils.isNotEmpty(join.getJoinTables())) {
                    logger.warn("sql has Join table:{},"
                            + "can not be set hasAlias = false, System change back to 'True'", join
                            .getJoinTables().get(0));
                    this.setHasAlias(true);
                }
            }
        }
    }
    
    /**
     * 生成from sql语句
     * 
     * @param MiniCube
     *            cube
     * @return String sql
     */
    public String generateFromExpression(SqlQuery sqlQuery)
            throws QuestionModelTransformationException {
        String[] tableNames = tableName.split(",");
        if (tableNames.length == 1) {
            // 为单表
            String from = " from " + tableNames[0] + SqlConstants.SPACE;
            if (hasAlias) {
                from = from + SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.SPACE;
            }
            return from;
        }
        // 下面的代码为处理多表的情况
        StringBuffer sqlFrom = new StringBuffer("");
        
        for (int i = 0; i < tableNames.length; i++) {
            StringBuffer tmpSqlFrom = new StringBuffer(" select * from ");
            tmpSqlFrom.append(tableNames[i]);
            tmpSqlFrom.append(" where 1=1 ");
            SingleWhere singleWhere = null;
            for (SqlColumn sqlColumn : queryMeta.getAllColumns()) {
                // 但事实表中的where
                ColumnCondition columnCondition = sqlColumn.getColumnCondition();
                // 判断是事实表查询
                if (columnCondition != null) {
                    // TODO: 过滤多余的关联ID where
                    if (sqlColumn.getType() == ColumnType.CALLBACK
                            || sqlColumn.getType() == ColumnType.TIME
                            || sqlColumn.getType() == ColumnType.COMMON) {
                        singleWhere = new SourceSingleWhere(sqlColumn, false);
                        sqlQuery.getWhere().addWhere(singleWhere);
                        tmpSqlFrom.append(SqlConstants.AND + singleWhere.getSql());
                    }
                }
            }
            if (i == 0) {
                sqlFrom.append(tmpSqlFrom.toString());
            } else {
                sqlFrom.append(" union all " + tmpSqlFrom.toString());
            }
        }
        return " from (" + sqlFrom.toString() + ")" + SqlConstants.SPACE
                + SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.SPACE;
    }
    
    /**
     * 生成leftouterjoin sql语句
     * 
     * @param HashMap
     *            allColums
     * @param HashMap
     *            needColums
     * 
     * @return Join Join
     */
    public Join generateLeftOuterJoinExpression(Map<String, MetaCondition> queryConditions,
            Collection<SqlColumn> allColums) throws QuestionModelTransformationException {
        Join join = new Join();
        Set<SqlColumn> needJoinColumns = new HashSet<SqlColumn>();
        needJoinColumns.addAll(allColums);
        HashSet<String> joinTables = new HashSet<String>();
        // 添加Where中有字段，而select中没有的sqlcolumns
        for (String key : queryConditions.keySet()) {
            SqlColumn column = queryMeta.getSqlColumnByCubeKey(key);
            if (ColumnType.TIME != column.getType()
            // 如果不为TIME CALLBACK字段
                    && ColumnType.CALLBACK != column.getType()
                    // 如果不为事实表join
                    && !column.getSourceTableName().equals(column.getTableName())) {
                needJoinColumns.add(column);
            }
        }
        for (SqlColumn colum : needJoinColumns) {
            String joinTable = colum.getTableName();
            if (ColumnType.GROUP == colum.getType()) {
                continue;
            }
            if (colum.getSourceTableName().equals(colum.getTableName())) {
                // 可能有退化维的存在，
                continue;
            }
            if (joinTables.contains(joinTable)) {
                // 如果join的dimTableName已在sql中存在，
                continue;
            }
            if (ColumnType.JOIN == colum.getType()) {
                // 如果为JOIN 字段
                if (colum.getJoinTable() != null) {
                    join.getJoinTables().add(colum.getJoinTable());
                    joinTables.add(joinTable);
                }
            }
        }
        return join;
    }
    
    /**
     * 生成generateTotalWhereExpression ,Dimension only where sql语句,如果维度表为事实表，将会过滤
     * 
     * @param ConfigQuestionModel
     *            configQuestionModel
     * @return String sql
     */
    public void generateTotalWhereExpression(SqlQuery sqlQuery, Collection<SqlColumn> sqlColumns,
            boolean includeJoinWhere) {
        for (SqlColumn sqlColumn : sqlColumns) {
            MetaCondition metaCondition = sqlColumn.getColumnCondition();
            if (metaCondition != null && metaCondition instanceof ColumnCondition) {
                if (sqlColumn.getType() == ColumnType.JOIN
                        && SqlColumnUtils.isFacttableColumn(sqlColumn)) {
                    // 退化维
                    sqlQuery.getWhere().addTotalWhere(sqlColumn, hasAlias);
                    continue;
                }
                if (sqlColumn.getType() == ColumnType.CALLBACK
                        || sqlColumn.getType() == ColumnType.TIME
                        || sqlColumn.getType() == ColumnType.COMMON) {
                    sqlQuery.getWhere().addTotalWhere(sqlColumn, hasAlias);
                    continue;
                }
                if (!sqlColumn.getSourceTableName().equals(sqlColumn.getTableName())
                        && includeJoinWhere) {
                    // 获取JOIN where
                    if (sqlColumn.getType() == ColumnType.JOIN) {
                        // 过滤退化维的情况
                        sqlQuery.getWhere().addTotalWhere(sqlColumn, hasAlias);
                        continue;
                    }
                }
            }
        }
    }
    
    /**
     * 生成generateNoJoinTotalWhereExpression
     * 
     * @param SQLConditionType
     *            SQLConditionType
     * @return String sql
     */
    public void generateTotalWhereExpressionWithValue(SqlQuery sqlQuery,
            Collection<SqlColumn> sqlColumns, SQLConditionType sqlConditionType,
            Map<String, List<Object>> whereData, boolean hasAlias) {
        if (MapUtils.isEmpty(whereData)) {
            return;
        }
        for (Entry<String, List<Object>> entry : whereData.entrySet()) {
            SqlColumn sqlColumnOri = queryMeta.getSqlColumn(entry.getKey());
            if (sqlColumnOri != null) {
                
                // 只组织事实表中的where语句
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    // 由于value没有值 添加where 1=2 的语句
                    sqlQuery.getWhere().addTotalWhere(null, hasAlias);
                    return;
                }
                SqlColumn sqlColumn = DeepcopyUtils.deepCopy(sqlColumnOri);
                ColumnCondition columnCondition = SqlColumnUtils.buildColumnCondition(
                        SQLConditionType.IN, entry.getValue());
                
                sqlColumn.setColumnCondition(columnCondition);
                sqlQuery.getWhere().addTotalWhere(sqlColumn, hasAlias);
                
            }
        }
    }
    
    /**
     * 根据sqlcolumn生成sql column name
     * 
     * @param sqlColumn
     *            sqlColumn
     * @return sqlColumn name
     */
    public static String getSqlColumnName(SqlColumn sqlColumn, boolean hasAlias) {
        String columnSqName = "";
        
        if (ColumnType.JOIN == sqlColumn.getType()) {
            if (sqlColumn.getSourceTableName().equals(sqlColumn.getTableName())) {
                // 退化维
                columnSqName = SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.DOT
                        + sqlColumn.getFactTableFieldName();
            } else {
                columnSqName = sqlColumn.getTableName() + SqlConstants.DOT
                        + sqlColumn.getTableFieldName();
            }
        } else if (ColumnType.TIME == sqlColumn.getType()
                || ColumnType.CALLBACK == sqlColumn.getType()) {
            columnSqName = SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.DOT
                    + sqlColumn.getFactTableFieldName();
        } else {
            columnSqName = SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.DOT
                    + sqlColumn.getTableFieldName();
        }
        
        if (!hasAlias) {
            String[] columnSqNames = StringUtils.split(columnSqName, ".");
            columnSqName = columnSqNames[columnSqNames.length - 1];
        }
        return columnSqName;
    }
    
    /**
     * 生成orderby sql语句
     * 
     * @param ConfigQuestionModel
     *            configQuestionModel
     * @param HashMap
     *            allColums
     * @return String orderby sql
     */
    public void generateOrderByExpression(SqlQuery sqlQuery, boolean hasAlias)
            throws QuestionModelTransformationException {
        sqlQuery.getOrderBy().setHasAlias(hasAlias);
        for (SqlColumn sqlColumn : queryMeta.getAllColumns()) {
            if (sqlColumn.getSortRecord() != null) {
                sqlQuery.getOrderBy().getOrderByList().add(sqlColumn);
            }
        }
    }
    
    /**
     * @param driver
     *            jdbc driver
     * @param facttableAlias
     *            facttableAlias facttableAlias
     */
    public SqlExpression(String driver) {
        this.driver = driver;
        sqlQuery = new SqlQuery(driver, this.hasAlias);
        countSqlQuery = new SqlQuery(driver, this.hasAlias);
    }
    
    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     * default generate get tableName
     * 
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * @return the sqlQuery
     */
    public SqlQuery getSqlQuery() {
        return sqlQuery;
    }
    
    /**
     * @return the countSqlQuery
     */
    public SqlQuery getCountSqlQuery() {
        return countSqlQuery;
    }

    /**
     * default generate get queryMeta
     * @return the queryMeta
     */
    public QueryMeta getQueryMeta() {
        return queryMeta;
    }

    /**
     * default generate set queryMeta
     * @param queryMeta the queryMeta to set
     */
    public void setQueryMeta(QueryMeta queryMeta) {
        this.queryMeta = queryMeta;
    }

    /**
     * default generate get hasAlias
     * 
     * @return the hasAlias
     */
    public boolean isHasAlias() {
        return hasAlias;
    }
    
    /**
     * default generate set hasAlias
     * 
     * @param hasAlias
     *            the hasAlias to set
     */
    public void setHasAlias(boolean hasAlias) {
        this.sqlQuery = new SqlQuery(this.driver, hasAlias);
        this.countSqlQuery = new SqlQuery(this.driver, hasAlias);
        this.hasAlias = hasAlias;
    }
    
    /**
     * default generate get needColums
     * 
     * @return the needColums
     */
    public List<SqlColumn> getNeedColums() {
        return needColums;
    }
    
    /**
     * default generate set needColums
     * 
     * @param needColums
     *            the needColums to set
     */
    public void setNeedColums(List<SqlColumn> needColums) {
        this.needColums = needColums;
    }
    
}