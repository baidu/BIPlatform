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
package com.baidu.rigel.biplatform.cache;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 
 * 数据存储服务接口
 * 
 * @author lijin
 *
 */
public interface StoreManager {
    
    
    /** 
     * TOPICS
     */
    public static final String TOPICS = "topics";
    
    public static final String LOCK_KEY = "lockName";

    

    
    /** 
     * EVENT_QUEUE
     */
    public static final String EVENT_QUEUE = "eventQueue";
    /**
     * String 当前的storeManager
     */
    String CURRENT_STORE_MANAGER = "CURRENT_STORE_MANAGER";
    
    public static final String SERIALIZER_KEY = "serializer";
    
    public static final String DESERIALIZER_KEY = "deserializer";
    
    final Map<String, Function<Object[], Object>> UDF_SETTING = new HashMap<String, Function<Object[], Object>>();
    
    
    /**
     * 
     * 获取存储区
     * 
     * @param name
     *            存储区名称
     * @return Cache
     */
    Cache getDataStore(String name);
    
    /**
     * 从cache中根据KEY获取对象并转换成指定类型
     * 
     * @param cache
     *            存数据的cache
     * @param key
     *            对象的key
     * @param clazz
     *            转换的类型
     * @return 结果
     * @throws IllegalArgumentException
     *             参数异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromCache(Cache cache, String key, Class<T> clazz) {
        if (cache == null || StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("illegal params ,cache:" + cache + " key:" + key);
        }
        ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper != null) {
            return (T) valueWrapper.get();
        }
        return null;
    }
    
    /**
     * 
     * 将事件加入队列中
     * 
     * @param event
     *            事件
     * @throws Exception
     *             可能抛出异常
     */
    void putEvent(EventObject event) throws Exception;
    
    /**
     * 
     * 从队列中取出当前的事件 注意，该方法是阻塞的，需要使用单独线程
     * 
     * @return EventObject
     * @throws Exception
     */
    EventObject getNextEvent() throws Exception;
    
    /**
     * 
     * postEvent
     * 
     * @param event
     *            要发布的事件
     * @throws Exception
     *             异常
     */
    void postEvent(EventObject event) throws Exception;
    
    /**
     * 
     * 拿到集群锁
     * @return Lock
     */
    Lock getClusterLock();
    
    
    Lock getClusterLock(String lockName);
    
    /**
     * 用户自定义序列化、反序列化器注册
     * @param f
     */
    static void addUdfSerializerSetting (Function<Object[], Object> f) {
        UDF_SETTING.put (SERIALIZER_KEY, f);
    }
    
    
    /**
     * 用户自定义序列化、反序列化器注册
     * @param f
     */
    static void addUdfDeSerializerSetting (Function<Object[],  Object> f) {
        UDF_SETTING.put (DESERIALIZER_KEY, f);
    }
    
}
