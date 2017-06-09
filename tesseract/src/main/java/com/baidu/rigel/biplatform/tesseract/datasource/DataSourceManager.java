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
/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.datasource;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;

/**
 * 管理数据源接口
 * 
 * @author xiaoming.chen
 * 
 */
public interface DataSourceManager {
    
    /**
     * 根据数据源的ID返回可用数据源
     * 
     * @param key 数据源的KEY
     * @return 可用数据源
     * @throws DataSourceException  can not found datasource by key
     * @throws IllegalArgumentException key is null
     */
    DataSourceWrap getDataSourceByKey(String key) throws DataSourceException;
    
    /**
     * 根据数据源信息初始化数据源
     * 
     * @param dataSourceInfo
     * @throws DataSourceException  init datasource error or dataSource has init yet
     * @throws IllegalArgumentException dataSourceInfo is invalid 
     */
    void initDataSource(DataSourceInfo dataSourceInfo) throws DataSourceException;
    
    /**
     * remove dynamicDataSource by key
     * @param key dynamicDataSource key
     * @throws IllegalArgumentException key is null
     */
    void removeDataSource(String key);
    
    
    /** 
     * updateDataSource
     * @param dataSourceInfo
     * @throws DataSourceException
     */
    void updateDataSource(DataSourceInfo dataSourceInfo) throws DataSourceException;
}
