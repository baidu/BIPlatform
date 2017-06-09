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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.service.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.PropertiesFileUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.AggregateCompute;
import com.baidu.rigel.biplatform.queryrouter.query.vo.Meta;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultSet;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.QueryMeasure;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection.DataSourceException;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection.DataSourcePoolService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection.SqlDataSourceWrap;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * 处理Jdbc sql query请求
 * 
 * @author luowenlei
 *
 */
@Service("jdbcHandlerImpl")
@Scope("prototype")
public class JdbcHandlerImpl implements JdbcHandler {
    
    /**
     * PROPERY_MAX_RESULT_SIZE
     */
    private static final String PROPERY_MAX_RESULT_SIZE = "queryrouter.result.memory.max.size";
    
    /**
     * PROPERY_SERVER_FILE_NAME
     */
    private static final String PROPERY_SERVER_FILE_NAME = "application";
    
    /**
     * meroryMaxSize,内存存放的最大的记录条数，默认50万
     */
    private static int memoryMaxSize = 500000;
    
    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * jdbcTemplate
     */
    private JdbcTemplate jdbcTemplate = null;
    
    /**
     * dataSourcePoolService
     */
    @Resource(name = "localDataSourcePoolServiceImpl")
    private DataSourcePoolService dataSourcePoolService;
    
    /**
     * dataSourceInfo
     */
    private DataSourceInfo dataSourceInfo;
    
    /**
     * 
     * initJdbcTemplate
     * 
     * @param dataSource
     *            dataSource
     */
    @Override
    public synchronized void initJdbcTemplate(DataSourceInfo dataSourceInfo) {
        long begin = System.currentTimeMillis();
        try {
            DataSource dataSource = (SqlDataSourceWrap) this.dataSourcePoolService
                    .getDataSourceByKey(dataSourceInfo);
            
            if (this.getJdbcTemplate() == null
                    || !this.getJdbcTemplate().getDataSource().equals(dataSource)) {
                this.setJdbcTemplate(new JdbcTemplate(dataSource));
                logger.info("queryId:{} initJdbcTemplate cost:"
                        + (System.currentTimeMillis() - begin) + "ms",
                        QueryRouterContext.getQueryId());
            }
            String maxSize = PropertiesFileUtils.getPropertiesKey(PROPERY_SERVER_FILE_NAME,
                    PROPERY_MAX_RESULT_SIZE);
            if (!StringUtils.isEmpty(maxSize)) {
                memoryMaxSize = Integer.valueOf(maxSize).intValue();
            }
            this.dataSourceInfo = dataSourceInfo;
        } catch (DataSourceException e) {
            e.printStackTrace();
            logger.error("getDataSource error:" + e.getCause().getMessage());
        }
    }
    
    /**
     * 通过sql查询数据库中的数据
     * 
     * @param sqlQuery
     *            sql
     * @param dataSourceInfo
     *            dataSourceInfo
     * @return List<Map<String, Object>> formd tableresult data
     */
    public List<Map<String, Object>> queryForList(String sql, List<Object> whereValues) {
        long begin = System.currentTimeMillis();
        List<Map<String, Object>> result = null;
        try {
            logger.info("queryId:{} sql: {}", QueryRouterContext.getQueryId(),
                    this.toPrintString(sql, whereValues));
            result = this.jdbcTemplate.queryForList(sql, whereValues.toArray());
        } catch (Exception e) {
            logger.error("queryId:{} select sql error:{}", QueryRouterContext.getQueryId(), e
                    .getCause().getMessage());
            throw e;
        } finally {
            logger.info("queryId:{} select sql cost:{} ms resultsize:{}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - begin,
                    result == null ? null : result.size());
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler
     * #queryForMeta(java.lang.String, java.util.List)
     */
    @Override
    public Map<String, String> queryForMeta(String tableName) {
        long begin = System.currentTimeMillis();
        int count = 0;
        Map<String, String> result = Maps.newConcurrentMap();
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getJdbcTemplate().getDataSource().getConnection();
            DatabaseMetaData dbMetaData = conn.getMetaData();
            if (StringUtils.isNotEmpty(tableName)) {
                String allTable = "%";
                
                try {
                    rs = dbMetaData.getColumns(null, null, tableName, allTable);
                } catch (Exception e) {
                    logger.warn("queryId:{} can not get Columns", QueryRouterContext.getQueryId());
                }
                while (rs != null && rs.next()) {
                    try {
                        result.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
                    } catch (Exception e) {
                        logger.warn("queryId:{} can not get Columns Meta.", QueryRouterContext.getQueryId());
                    }
                    count ++;
                }
            }
        } catch (SQLException e1) {
            logger.error("queryId:{} select meta error:{}", QueryRouterContext.getQueryId(), e1
                    .getCause().getMessage());
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeConnection(conn);
            logger.info("queryId:{} select meta cost:{} ms resultsize:{}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - begin, count);
        }
        return result;
    }
    
    /**
     * queryForInt
     * 
     * @param sql
     *            sql
     * @param whereValues
     *            whereValues
     * @param dataSourceInfo
     *            dataSourceInfo
     * @return int count
     */
    public int queryForInt(String sql, List<Object> whereValues) {
        long begin = System.currentTimeMillis();
        Map<String, Object> result = null;
        int count = 0;
        try {
            logger.info("queryId:{} count sql: {}", QueryRouterContext.getQueryId(),
                    this.toPrintString(sql, whereValues));
            result = this.jdbcTemplate.queryForMap(sql, whereValues.toArray());
            count = Integer.valueOf(result.values().toArray()[0].toString()).intValue();
        } catch (Exception e) {
            logger.error("queryId:{} select sql error:{}", QueryRouterContext.getQueryId(), e
                    .getCause().getMessage());
            throw e;
        } finally {
            logger.info("queryId:{} select count sql cost:{} ms, result: {}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - begin, count);
        }
        return count;
    }
    
    /**
     * querySqlList
     *
     * @param sqlQuery
     * @param groupByList
     * @param dataSourceInfo
     * @return
     */
    public SearchIndexResultSet querySqlList(SqlQuery sqlQuery, List<SqlColumn> groupByList) {
        // 此方法目前只能使用 preparesql = false
        sqlQuery.getWhere().setGeneratePrepareSql(false);
        long begin = System.currentTimeMillis();
        List<String> selectListOrder = Lists.newArrayList();
        for (SqlColumn sqlColumn : sqlQuery.getSelect().getSelectList()) {
            selectListOrder.add(sqlColumn.getName());
        }
        List<String> groupByListStr = Lists.newArrayList();
        for (SqlColumn sqlColumn : groupByList) {
            groupByListStr.add(sqlColumn.getName());
        }
        Meta meta = new Meta(selectListOrder.toArray(new String[0]));
        SearchIndexResultSet resultSet = new SearchIndexResultSet(meta, 1000000);
        
        final List<String> selectListOrderf = Lists.newArrayList(selectListOrder);
        jdbcTemplate.query(new PreparedStatementCreator() {
            
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String sql = sqlQuery.toSql();
                logger.info("queryId:{} sql: {}", QueryRouterContext.getQueryId(), sql);
                PreparedStatement pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                if (con.getMetaData().getDriverName().toLowerCase().contains("mysql")) {
                    pstmt.setFetchSize(Integer.MIN_VALUE);
                }
                return pstmt;
            }
        }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                List<Object> fieldValues = new ArrayList<Object>();
                String groupBy = "";
                for (String select : selectListOrderf) {
                    fieldValues.add(rs.getObject(select));
                    if (groupByListStr != null && groupByListStr.contains(select)) {
                        groupBy += rs.getString(select) + ",";
                    }
                }
                
                SearchIndexResultRecord record = new SearchIndexResultRecord(fieldValues
                        .toArray(new Serializable[0]), groupBy);
                resultSet.addRecord(record);
            }
        });
        logger.info("queryId:{} select sql cost:{} ms resultsize:{}",
                QueryRouterContext.getQueryId(), System.currentTimeMillis() - begin,
                resultSet == null ? null : resultSet.size());
        ;
        return resultSet;
    }
    
    /**
     * querySqlList
     *
     * @param sqlQuery
     * @param groupByList
     * @param dataSourceInfo
     * @return
     */
    public SearchIndexResultSet querySqlListWithAgg(SqlQuery sqlQuery,
            List<SqlColumn> groupByList, List<QueryMeasure> queryMeasures) {
        
        // 此方法目前只能使用 preparesql = false
        sqlQuery.getWhere().setGeneratePrepareSql(false);
        long begin = System.currentTimeMillis();

        List<String> groupByListStr = Lists.newArrayList();
        for (SqlColumn sqlColumn : groupByList) {
            groupByListStr.add(sqlColumn.getName());
        }
        
        List<String> selectListOrder = Lists.newArrayList();
        for (SqlColumn sqlColumn : sqlQuery.getSelect().getSelectList()) {
            selectListOrder.add(sqlColumn.getName());
        }
        Meta meta = new Meta(selectListOrder.toArray(new String[0]));
        SearchIndexResultSet resultSet = new SearchIndexResultSet(meta, 1000000);
        SearchIndexResultSet resultSetTemp = new SearchIndexResultSet(meta, 1000000);
        final List<Integer> aggInfo = Lists.newArrayList();
        jdbcTemplate.query(new PreparedStatementCreator() {
            
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String sql = sqlQuery.toSql();
                logger.info("queryId:{} sql: {}", QueryRouterContext.getQueryId(), sql);
                PreparedStatement pstmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                if (con.getMetaData().getDriverName().toLowerCase().contains("mysql")) {
                    pstmt.setFetchSize(Integer.MIN_VALUE);
                }
                return pstmt;
            }
        }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                List<Object> fieldValues = new ArrayList<Object>();
                String groupBy = "";
                for (String select : selectListOrder) {
                    fieldValues.add(rs.getObject(select));
                    if (groupByListStr != null && groupByListStr.contains(select)) {
                        groupBy += rs.getString(select) + ",";
                    }
                }
                
                SearchIndexResultRecord record = new SearchIndexResultRecord(fieldValues
                        .toArray(new Serializable[0]), groupBy);
                resultSetTemp.addRecord(record);
                if (resultSetTemp.getDataList().size() > memoryMaxSize) {
                    // 第一次计算：sum  count 聚集计算，distinct count 行值分堆
                    resultSet.addAll(
                            AggregateCompute.aggregateWithoutDc(
                                    resultSetTemp.getDataList(), groupByListStr.size(), queryMeasures));
                    aggInfo.add(resultSet.size());
                    resultSetTemp.clear(meta, 1000000);
                    System.gc();
                }
            }
        });
        
        // 计算剩余数据
        resultSet.addAll(
                AggregateCompute.aggregateWithoutDc(
                        resultSetTemp.getDataList(), groupByListStr.size(), queryMeasures));
        aggInfo.add(resultSet.size());
        System.gc();
        
        // 计算distinct count || count hashSet size
        List<QueryMeasure> queryMeasuresDp = DeepcopyUtils.deepCopy(queryMeasures);
        if (this.contentsCountOperator(queryMeasures)) {
            queryMeasuresDp.forEach(measure -> {
                if (measure.getAggregator().equals(Aggregator.COUNT)) {
                    measure.setAggregator(Aggregator.SUM);
                }
            });
        }
        
        // 最终计算
        resultSet.setDataList(AggregateCompute.aggregate(
                resultSet.getDataList(), groupByListStr.size(), queryMeasuresDp));
        
        logger.info("queryId:{} select sql cost:{} ms resultsize:{} aggTimes:{} afterMemSizePerAgg:{}",
                QueryRouterContext.getQueryId(), System.currentTimeMillis() - begin,
                resultSet == null ? null : resultSet.size(), aggInfo.size(), this.convertListToString(aggInfo));
        return resultSet;
    }
    
    /**
     * contentsDistinctOperator
     *
     * @param queryMeasures
     * @return
     */
    private boolean contentsCountOperator(List<QueryMeasure> queryMeasures) {
        for (QueryMeasure queryMeasure : queryMeasures) {
            if (queryMeasure.getAggregator() == Aggregator.DISTINCT_COUNT
                    || queryMeasure.getAggregator() == Aggregator.COUNT) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * convertListToString
     *
     * @param list
     * @return
     */
    private String convertListToString(List<Integer> list) {
        String r = "";
        for (Object o : list) {
            r = r + o.toString() + ",";
        }
        return  "[" + r + "]";
    }
    /**
     * toPrintString
     * 
     * @param sql
     *            sql
     * @param objects
     *            objects
     * @return sql String
     */
    public String toPrintString(String sql, List<Object> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return sql;
        }
        String printSql = new String(sql);
        int valuesCount = 0;
        if (!StringUtils.isEmpty(printSql)) {
            for (Object value : objects) {
                valuesCount++;
                if (value instanceof String) {
                    printSql = StringUtils.replaceOnce(printSql, "?", "'" + value.toString() + "'");
                } else {
                    printSql = StringUtils.replaceOnce(printSql, "?", value.toString());
                }
                if (valuesCount > 2000) {
                    return printSql;
                }
            }
            return printSql;
        } else {
            return "";
        }
    }
    
    @Override
    public DataSourceInfo getDataSourceInfo() {
        // TODO Auto-generated method stub
        return this.dataSourceInfo;
    }
    
    public DataSourceInfo setDataSourceInfo(DataSourceInfo dataSourceInfo) {
        return this.dataSourceInfo = dataSourceInfo;
    }
    
    /**
     * default generate get jdbcTemplate
     * 
     * @return the jdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    
    /**
     * default generate set jdbcTemplate
     * 
     * @param jdbcTemplate
     *            the jdbcTemplate to set
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
