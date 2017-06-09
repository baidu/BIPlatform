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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.meta;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;

/**
 * 
 * Description: 查询数据库中的表的元数据信息
 * 
 * @author 罗文磊
 *
 */
public interface TableMetaService {

    /**
     * 查询所有字段在数据库中的数据类型并添入到sqlColumnMap中
     * 
     * @param sqlColumnMap
     *            所有的字段
     * @param SqlDataSourceInfo
     *            dataSourceInfo
     * @return List<String> 存在的表
     */
    public void generateColumnMeta(
            QueryMeta queryMeta,
            DataSourceInfo dataSourceInfo, JdbcHandler queryHandler);
}