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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.queryrouter.handle.model.QueryHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.CountNumService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JoinTableDataService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;

/**
 * 
 * SQLDataQueryService的实现类
 * 
 * @author luowenlei
 *
 */
@Service("jdbcCountNumServiceImpl")
@Scope("prototype")
public class JdbcCountNumServiceImpl implements CountNumService {
    
    /**
     * JoinTableDataService
     */
    @Resource(name = "jdbcJoinTableDataServiceImpl")
    private JoinTableDataService joinTableDataService;
    
    /**
     * getTotalRecordSize
     * 
     * @param questionModel
     *            questionModel
     * @param QueryExpression
     *            sqlExpression
     * @return DataModel DataModel
     */
    public int getTotalRecordSize(PlaneTableQuestionModel planeQuestionModel,
            QueryHandler newQueryRequest) {
        // 按照tablename, 组织sqlColumnList，以便于按表查询所有的字段信息
        HashMap<String, List<SqlColumn>> tables = new HashMap<String, List<SqlColumn>>();
        SqlExpression sqlExpression = newQueryRequest.getSqlExpression();
        for (SqlColumn sqlColumn : newQueryRequest.getSqlExpression().getQueryMeta().getAllColumns()) {
            if (sqlColumn.getSourceTableName().equals(sqlColumn.getTableName())) {
                // 过滤事实表及退化维
                continue;
            }
            if (sqlColumn.getType() == ColumnType.JOIN) {
                if (tables.get(sqlColumn.getTableName()) == null) {
                    tables.put(sqlColumn.getTableName(), new ArrayList<SqlColumn>());
                }
                tables.get(sqlColumn.getTableName()).add(sqlColumn);
            }
        }
        // 查询jointable中的主表的id信息
        Map<String, List<Object>> values = joinTableDataService.getJoinTableData(
                planeQuestionModel, tables,
                newQueryRequest.getJdbcHandler());
        // set total count sql
        sqlExpression.generateCountSql(planeQuestionModel, sqlExpression.getQueryMeta().getAllColumns(),
                sqlExpression.getNeedColums(), values);
        return newQueryRequest.getJdbcHandler().queryForInt(
                sqlExpression.getCountSqlQuery().toCountSql(),
                sqlExpression.getCountSqlQuery().getWhere().getValues());
        
    }
    
}
