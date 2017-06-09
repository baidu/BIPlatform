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
package com.baidu.rigel.biplatform.tesseract.store.service;

import java.util.List;

import com.baidu.rigel.biplatform.tesseract.store.meta.StoreMeta;

/**
 * 
 * 元数据Service接口
 * 
 * @author lijin
 *
 */
public interface MetaSerivce {
    
    /**
     * 
     * 保存或更新元数据到存储区中
     * 
     * @param metaStore
     *            元数据信息
     * @param dataStoreName
     *            存储区名称
     * @return boolean 保存成功返回 true，否则false
     */
    boolean saveOrUpdateMetaStore(StoreMeta metaStore, String dataStoreName);
    
    /**
     * 
     * 获取dataStore
     * 
     * @param dataStoreName
     *            dataStoreName
     * @return Cache
     */
  //  Cache getDataStore(String dataStoreName);
    
    /**
     * 
     * 获取storeMeta列表,并转换成对应的子类返回
     * 
     * @param dataStoreName
     *            存储区名称
     * @param storeKey
     *            存储key
     * @param <T> 返回值的类型
     * @return <T extends StoreMeta> List<T>
     */
    <T extends StoreMeta> List<T> getStoreMetaListByStoreKey(String dataStoreName, String storeKey);
    
}
