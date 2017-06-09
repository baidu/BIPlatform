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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JoinTableDataService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.Where;

/**
 * 
 * Description: 查询数据库中需要join的表的数据
 * 
 * @author 罗文磊
 *
 */
@Service("jdbcJoinTableDataServiceImpl")
@Scope("prototype")
public class JdbcJoinTableDataServiceImpl implements JoinTableDataService {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(JdbcJoinTableDataServiceImpl.class);
    
    /**
     * 查询tableName中的jointable的数据id
     * 
     * @param planeTableQuestionModel
     *            所有的字段
     * @param allColums
     *            dataSourceInfo
     * @param tables
     *            key为tablename的结构存储
     * @return Map<String, List<Object>> key为主表中的字段，value为where的id数据
     */
    public Map<String, List<Object>> getJoinTableData(
            PlaneTableQuestionModel planeTableQuestionModel,
            HashMap<String, List<SqlColumn>> tables, JdbcHandler queryHandler) {
        Map<String, List<Object>> result = new HashMap<String, List<Object>>();
        if (MapUtils.isEmpty(tables)) {
            logger.warn("queryId:{} questionModel has no dimtable to select.");
        }
        for (String tableName : tables.keySet()) {
            Where where = new Where(false);
            List<SqlColumn> selectColumns = tables.get(tableName);
            String joinTableFieldId = "";
            String sqlColumnKey = "";
            for (SqlColumn sqlColumn : selectColumns) {
                if (sqlColumn.getJoinTable() == null
                        || CollectionUtils.isEmpty(sqlColumn.getJoinTable().getJoinOnList())
                        || ColumnType.JOIN != sqlColumn.getType()
                        || sqlColumn.getColumnCondition() == null
                        || sqlColumn.getColumnCondition().getColumnConditions() == null
                        || CollectionUtils.isEmpty(sqlColumn.getColumnCondition()
                                .getColumnConditions().getConditionValues())) {
                    continue;
                }
                joinTableFieldId = sqlColumn.getJoinTable().getJoinOnList().get(0)
                        .getJoinTableFieldName();
                sqlColumnKey = QueryMeta.getSqlColumnKey(sqlColumn.getSourceTableName(),
                        sqlColumn.getFactTableFieldName());
                
                where.addTotalWhere(sqlColumn, false);
            }
            if (where.isEmpty()) {
                // 如果搜索为所有条件，则不组织wheresql
                continue;
            }
            String sql = "select " + joinTableFieldId + " from " + tableName + where.getSql();
            List<Map<String, Object>> datas = queryHandler.queryForList(sql, where.getValues());
            result.put(sqlColumnKey, new ArrayList<Object>());
            for (Map<String, Object> data : datas) {
                result.get(sqlColumnKey).add(data.get(joinTableFieldId));
            }
        }
        return result;
    }
}