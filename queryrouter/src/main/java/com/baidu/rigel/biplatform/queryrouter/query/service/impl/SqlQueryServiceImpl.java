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
package com.baidu.rigel.biplatform.queryrouter.query.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SqlQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryService;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.DataModelConvertService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.SqlColumnUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.utils.QueryHandlerBuilder;
import com.google.common.collect.Lists;

/**
 * sql鏌ヨ鐨勬彃浠�
 * 
 * @author luowenlei
 *
 */
@Service("sqlQueryService")
@Scope("prototype")
public class SqlQueryServiceImpl implements QueryService {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(SqlQueryServiceImpl.class);
    
    /**
     * dataModelConvertService
     */
    @Resource(name = "dataModelConvertService")
    private DataModelConvertService dataModelConvertService;
    
//    /**
//     * cacheManagerService
//     */
//    @Resource(name = "cacheManagerservice")
//    private CacheManagerService cacheManagerService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.querybus.queryplugin.QueryPlugin#query(com
     * .baidu.rigel.biplatform.ac.query.model.QuestionModel)
     */
    @Override
    public DataModel query(QuestionModel questionModel, QueryContext queryContext) throws Exception {
        try {
            DataModel dataModel = null;
            SqlQuestionModel sqlQuestionModel = (SqlQuestionModel) questionModel;
            if (sqlQuestionModel.getDataSourceInfo().getDataSourceType() == DataSourceType.ASYN) {
                dataModel = new DataModel();
            } else {
                JdbcHandler jdbcHandler = QueryHandlerBuilder.buildJdbcHandler(sqlQuestionModel
                        .getDataSourceInfo());
                jdbcHandler.initJdbcTemplate(sqlQuestionModel.getDataSourceInfo());
                List<Map<String, Object>> rowBasedList = jdbcHandler.queryForList(
                        sqlQuestionModel.getSql(), Lists.newArrayList());
                // convert data to datamodel
                dataModel = dataModelConvertService.convert(
                        SqlColumnUtils.getNeedSqlColumns(sqlQuestionModel.getSql()), rowBasedList);
            }
            return dataModel;
        } catch (Exception e) {
            throw e;
        }
        
    }
    

}
