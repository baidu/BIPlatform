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
package com.baidu.rigel.biplatform.tesseract.dataquery.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.IndexDataResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;

/**
 * 
 * DataQueryService
 * 
 * @author lijin
 *
 */
public interface DataQueryService {
    /**
     * 
     * queryForListWithSql
     * 
     * @param sql
     *            sql
     * @param dataSource
     *            dataSource
     * @return List<Map<String,Object>>
     */
    List<Map<String, Object>> queryForListWithSql(String sql, DataSource dataSource);
    
    /**
     * 
     * 给定数据源、sql查询对像、limit第一个参数、limit第二个参数得到DocumentsForIndex
     * 
     * @param sqlQuery
     *            sql查询对像
     * @param dataSource
     *            数据源
     * @param limitStart
     *            limit第一个参数
     * @param limitEnd
     *            limit第二个参数
     * @return IndexDataResultSet
     * @throws IOException 
     */
    IndexDataResultSet queryForDocListWithSQLQuery(SqlQuery sqlQuery, DataSource dataSource,
        long limitStart, long limitEnd) throws IOException;
    
    
    /**
     * 
     * @param sqlQuery
     * @param dataSource
     * @param limitStart
     * @param limitEnd
     * @param queryRequest
     * @return
     */
    SearchIndexResultSet queryForListWithSQLQueryAndGroupBy(SqlQuery sqlQuery, DataSource dataSource,
            long limitStart, long limitEnd, QueryRequest queryRequest);
    
    /**
     * 
     * 跟据sql查询得到long值 ，用来查 count值
     * 
     * @param sql
     *            查询sql
     * @param dataSource
     *            数据源
     * @return long
     */
    long queryForLongWithSql(String sql, DataSource dataSource);
    
    /**
     * getter method for property jdbcTemplate
     * 
     * @param dataSource
     *            获取某数据源下的jdbcTemplate
     * @return the jdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate(DataSource dataSource);
    
}
