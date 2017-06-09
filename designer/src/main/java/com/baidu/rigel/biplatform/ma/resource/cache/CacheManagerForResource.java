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
package com.baidu.rigel.biplatform.ma.resource.cache;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;

/**
 * REST层的cacheManager
 * 
 * @author zhongyi
 *
 *         2014-7-30
 */
@Service("cacheManagerForReource")
public class CacheManagerForResource {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(CacheManagerForResource.class);
    
    /**
     * cache manager
     */
    @Resource 
    private StoreManager storeManager = null;
    
    /**
     * 
     * @return
     */
    protected Cache getCache() {
        return storeManager.getDataStore ("bi_platform");
    }
    
    /**
     * 
     * @param key
     * @return
     */
    public Object getFromCache(String key) {
        logger.info("query obj from cache with key: " + key);
        Cache cache = getCache();
        if (cache == null) {
            return null;
        }
        ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            logger.info("can not get obj from cache with key : " + key);
            return null;
        }
        Object value = wrapper.get();
        if (value == null) {
            logger.info("can not get obj from cache with key : " + key);
            return null;
        }
        return value;
    }
    
    /**
     * 
     * @param key
     * @throws Exception
     */
    public void setToCache(String key, Object value) throws CacheOperationException {
        logger.info("set obj to cache with key: " + key);
        Cache cache = getCache();
        if (!StringUtils.hasText(key)) {
            logger.error("Key is empty when setToCache Operation. ");
            throw new CacheOperationException("Key is empty when setToCache Operation. ");
        }
        cache.put(key, value);
    }
    
    /**
     * 
     * @param key
     * @throws Exception
     */
    public void deleteFromCache(String key) throws CacheOperationException {
        logger.info("delete obj from cache with key: " + key);
        Cache cache = getCache();
        if (!StringUtils.hasText(key)) {
            throw new CacheOperationException("No such key in cache. "); 
        }
        cache.evict(key);
    }
    
//    /**
//     * 
//     * @param cacheManager
//     */
//    protected void setCacheManager(CacheManager cacheManager) {
//        this.cacheManager = cacheManager;
//    }

    /**
     * 
     * @param key
     */
    public void removeFromCache(String key) {
        this.getCache().evict(key);
    }
}