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
package com.baidu.rigel.biplatform.tesseract.datasource.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourceManager;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourceManagerFactory;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourceWrap;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;

/**
 * 数据源信息实现
 * 
 * @author xiaoming.chen
 *
 */
@Service
public class DataSourcePoolServiceImpl implements DataSourcePoolService {

    @Resource
    private StoreManager storeManager;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.datasource.DataSourceInfoPoolService#initDataSourceInfo(com.baidu.rigel.
     * biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public void initDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException {
        Cache dsInfoCache = storeManager.getDataStore(DATASOURCEINFO_POOL_CACHE_NAME);
        // 将数据源信息cache起来，在需要的时候加载数据源信息
        dsInfoCache.put(dataSourceInfo.getDataSourceKey(), dataSourceInfo);
        // 实际不会马上初始化数据源，只会在需要时候才加载数据源到内存

    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService
     * #initDataSourceInfoList(java.util.List)
     */
    @Override
    public void initDataSourceInfoList(List<DataSourceInfo> dataSourceInfoList)
            throws DataSourceException {
        if (!CollectionUtils.isEmpty(dataSourceInfoList)) {
            for (DataSourceInfo dataSourceInfo : dataSourceInfoList) {
                initDataSourceInfo(dataSourceInfo);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.datasource.DataSourceInfoPoolService#updateDataSourceInfo(com.baidu.rigel
     * .biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public void updateDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException {
        // 先删除，在初始化,后续需要对数据源信息进行判断以后再判断
        destroyDataSourceInfo(dataSourceInfo);
        initDataSourceInfo(dataSourceInfo);
        LOG.info("save new datasource info cache");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.datasource.DataSourceInfoPoolService#destroyDataSourceInfo(com.baidu.rigel
     * .biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public boolean destroyDataSourceInfo(DataSourceInfo dataSourceInfo) throws DataSourceException {
        DataSourceManager dataSourceManager = DataSourceManagerFactory.getDataSourceManagerInstance(dataSourceInfo);
        // 先从内存中移除数据源对象
        dataSourceManager.removeDataSource(dataSourceInfo.getDataSourceKey());
        LOG.info("remove datasource from mem datasource pool");
        Cache dsInfoCache = storeManager.getDataStore(DATASOURCEINFO_POOL_CACHE_NAME);
        // 在从cache中移除数据源信息
        dsInfoCache.evict(dataSourceInfo.getDataSourceKey());
        LOG.info("remove datasource info from cache:{}",dsInfoCache);
        return true;
    }

    @Override
    public DataSourceInfo getDataSourceInfo(String dataSourceInfoKey) {
        if (StringUtils.isBlank(dataSourceInfoKey)) {
            throw new IllegalArgumentException("can not found datasourceinfo by blank datasourceinfo key");
        }
        Cache dsInfoCache = storeManager.getDataStore(DATASOURCEINFO_POOL_CACHE_NAME);
        return StoreManager.getFromCache(dsInfoCache, dataSourceInfoKey, DataSourceInfo.class);
    }

    @Override
    public DataSourceWrap getDataSourceByKey(String key) throws DataSourceException {
        DataSourceInfo dataSourceInfo = getDataSourceInfo(key);
        return getDataSourceByKey(dataSourceInfo);
    }

    @Override
    public DataSourceWrap getDataSourceByKey(DataSourceInfo dataSourceInfo) throws DataSourceException {
        long curr = System.currentTimeMillis();
        DataSourceManager dataSourceManager = DataSourceManagerFactory.getDataSourceManagerInstance(dataSourceInfo);
        LOG.info("getDataSource cost:" + (System.currentTimeMillis() - curr)
                + " to getDataSourceManagerInstance");
        curr = System.currentTimeMillis();
        // 每次调用init，init自动判断是否初始化过
        dataSourceManager.initDataSource(dataSourceInfo);
        LOG.info("getDataSource cost:" + (System.currentTimeMillis() - curr)
                + " to dataSourceManager.initDataSource");
        curr = System.currentTimeMillis();
        DataSourceWrap result = dataSourceManager.getDataSourceByKey(dataSourceInfo.getDataSourceKey());
        LOG.info("getDataSource cost:" + (System.currentTimeMillis() - curr)
                + " to dataSourceManager.getDataSourceByKey");
        return result;
    }

}
