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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;

/**
 * 数据源信息池，保证每个数据源初始化成功以后保持数据源信息方便调用
 * 
 * @author xiaoming.chen
 *
 */
public interface DataSourcePoolService {

    /**
     * DATASOURCEINFO_POOL_CACHE_NAME
     */
    String DATASOURCEINFO_POOL_CACHE_NAME = "dataSourceInfoPool";

    /**
     * LOG
     */
    Logger LOG = LoggerFactory.getLogger(DataSourcePoolService.class);

    /**
     * 初始化数据源信息，包括数据源信息存储和建立数据源
     * 
     * @param dataSourceInfo 数据源信息
     * @throws DataSourceException 初始化失败
     * @throws IllegalArgumentException 数据源信息校验失败
     */
    void initDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException;

    /**
     * 更新数据源信息（找到原来的数据源先销毁再重建，不涉及缓存操作，缓存操作需单独操作）
     * 
     * @param dataSourceInfo 数据源信息
     * @throws DataSourceException 更新失败
     * @throws IllegalArgumentException 数据源信息校验失败
     */
    void updateDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException;

    /**
     * 根据数据源信息的KEY获取数据源
     * 
     * @param dataSourceInfoKey 数据源信息的KEY
     * @return 数据源信息
     * @throws DataSourceException 找不到数据源
     * @throws IllegalArgumentException 数据源信息校验失败
     */
    DataSourceInfo getDataSourceInfo(String dataSourceInfoKey);

    /**
     * 根据数据源的ID返回可用数据源
     * 
     * @param key 数据源的KEY
     * @return 可用数据源
     * @throws DataSourceException can not found datasource by key
     * @throws IllegalArgumentException key is null
     */
    DataSourceWrap getDataSourceByKey(String key) throws DataSourceException;

    /**
     * 根据数据源信息返回可用数据源
     * 
     * @param dataSourceInfo 数据源信息
     * @return 可用数据源
     * @throws DataSourceException can not found datasource by key
     * @throws IllegalArgumentException key is null
     */
    DataSourceWrap getDataSourceByKey(DataSourceInfo dataSourceInfo) throws DataSourceException;

    /**
     * 销毁数据源
     * 
     * @param dataSourceInfo 销毁的数据源信息
     * @return 销毁结果
     * @throws DataSourceException 参数异常
     */
    boolean destroyDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException;

    /**
     * 销毁数据源
     * 
     * @param dataSourceInfoKey 销毁的数据源的KEY
     * @throws DataSourceException 参数异常
     */
    default boolean destroyDataSourceInfo(String dataSourceInfoKey) throws DataSourceException {
        DataSourceInfo dataSourceInfo = null;
        try {
            dataSourceInfo = getDataSourceInfo(dataSourceInfoKey);
        } catch (IllegalArgumentException e) {
            LOG.warn("can not get dataSource info by key:" + dataSourceInfoKey, e);
            return false;
        }
        return destroyDataSourceInfo(dataSourceInfo);
    }

}
