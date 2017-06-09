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
package com.baidu.rigel.biplatform.tesseract.store.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.tesseract.store.meta.StoreMeta;
import com.baidu.rigel.biplatform.tesseract.store.service.MetaSerivce;

/**
 * 
 * 抽像元数据服务
 * 
 * @author lijin
 *
 */
/**
 * TODO
 * 
 * @author lijin
 *
 */
public class AbstractMetaService implements MetaSerivce {
    /**
     * LOOGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetaService.class);
    /**
     * storeManager
     */
    @Resource
    private StoreManager storeManager;
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean saveOrUpdateMetaStore(StoreMeta storeMeta, String dataStoreName) {
        
        if (storeMeta == null || storeMeta.getStoreKey() == null
                || ("").equals(storeMeta.getStoreKey())) {
            LOGGER.warn("can not save StoreMeta with no accurate key:[" + storeMeta + "]");
            return false;
        }
        Cache metaDataStore = this.storeManager.getDataStore(dataStoreName);
        
        List<StoreMeta> storeMetaList = null;
        ValueWrapper storeMetaValue = metaDataStore.get(storeMeta.getStoreKey());
        if (storeMetaValue != null) {
            storeMetaList = (List<StoreMeta>) storeMetaValue.get();
        }
        if (storeMetaList == null) {
            storeMetaList = new ArrayList<StoreMeta>();
        }
//        int idx = 0;
//        boolean update = false;
//        for (; idx < storeMetaList.size(); idx++) {
//            if (storeMetaList.get(idx).equals(storeMeta)) {
//                update = true;
//                break;
//            }
//        }
//        
//        if (update) {
//            storeMetaList.remove(idx);
//        }
        
        if (storeMetaList.contains(storeMeta)) {
            storeMetaList.remove(storeMeta);
        }
        
        storeMetaList.add(storeMeta);
        // 元数据存储时，以: 集群名_产品线_数据源 为KEY存储元数据
        metaDataStore.put(storeMeta.getStoreKey(), storeMetaList);
        LOGGER.info("StoreMeta saved sucess! -- storeKey:[" + storeMeta.getStoreKey() + "]");
        
        return true;
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends StoreMeta> List<T> getStoreMetaListByStoreKey(String dataStoreName,
            String storeKey) {
        Cache metaDataStore = this.storeManager.getDataStore(dataStoreName);
        ValueWrapper metaDataValue = null;
        if (storeKey != null) {
            metaDataValue = metaDataStore.get(storeKey);
        }
        
        List<T> result = null;
        if (metaDataValue != null) {
            result = (List<T>) metaDataValue.get();
        }
        return result;
    }
    
}
