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
package com.baidu.rigel.biplatform.ma.model.service;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;

/**
 * 构建cube
 * 
 * @author zhongyi
 *
 *         2014-7-29
 */
public interface CubeMetaBuildService {
    
    /**
     * 获取所有的表
     * 
     * @param dsId
     *            数据源id
     * @param securityKey 
     * @return 所有的表
     * @throws DataSourceOperationException 
     */
    List<TableInfo> getAllTable(String dsId, String securityKey) throws DataSourceOperationException;
    
    /**
     * 初始化cubetable
     * 
     * @param tableIds
     *            所有指定为cube的表
     * @param regxs
     *            归类的正则表达式
     * @return cubetable
     * @throws DataSourceOperationException 
     */
    List<FactTableMetaDefine> initCubeTables(String dsId,
            List<String> tableIds, List<String> regxs, String securityKey) throws DataSourceOperationException;
    
    /**
     * 
     * initCubeTables
     * @param dsId
     * @param tableIds
     * @param divideTableStrategyVo
     * @param securityKey
     * @return
     * @throws DataSourceOperationException
     */
    List<FactTableMetaDefine> initCubeTables(String dsId, List<String> tableIds, 
            Map<String, DivideTableStrategyVo> divideTableStrategys,
            String securityKey) throws DataSourceOperationException;
}