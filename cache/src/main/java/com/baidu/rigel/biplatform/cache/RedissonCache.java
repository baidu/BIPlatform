
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

import org.redisson.core.RMap;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

/** 
 *  
 * @author xiaoming.chen
 * @version  2015年3月6日 
 * @since jdk 1.8 or after
 */
public class RedissonCache implements Cache {
    
    
    /** 
     * map
     */
    private RMap<Object, Object> map;

    
    /** 
     * name
     */
    private String name;
    
    
    
    /** 
     * 构造函数
     */
    public RedissonCache(RMap<Object, Object> map, String name) {
        this.map = map;
        this.name = name;
    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#getName() 
     */
    @Override
    public String getName() {
        return this.name;

    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#getNativeCache() 
     */
    @Override
    public Object getNativeCache() {
        return this.map;

    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#get(java.lang.Object) 
     */
    @Override
    public ValueWrapper get(Object key) {
        Object value = this.map.get(key);
        return value == null ? null : new SimpleValueWrapper(value);
    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#get(java.lang.Object, java.lang.Class) 
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        return wrapper == null ? null : (T) wrapper.get();

    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#put(java.lang.Object, java.lang.Object) 
     */
    @Override
    public void put(Object key, Object value) {
        this.map.put(key, value);
    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#putIfAbsent(java.lang.Object, java.lang.Object) 
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper oldValue = get(key);
        if(oldValue == null) {
            this.map.put(key, value);
            return null;
        } else {
            return oldValue;
        }
    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#evict(java.lang.Object) 
     */
    @Override
    public void evict(Object key) {
        this.map.removeAsync(key);
    }

    /*
     * (non-Javadoc) 
     * @see org.springframework.cache.Cache#clear() 
     */
    @Override
    public void clear() {
        this.map.delete();
    }

}

