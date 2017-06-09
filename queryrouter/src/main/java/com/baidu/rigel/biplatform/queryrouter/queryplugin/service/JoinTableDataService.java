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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;


/**
 * 类JoinTableDataService.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年11月16日 下午2:47:14
 */
public interface JoinTableDataService {
    
    

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
            HashMap<String, List<SqlColumn>> tables, JdbcHandler queryHandler);
}