package com.baidu.rigel.biplatform.ac.util;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread-local resource holder class, for pass data on the thread.
 * 
 * @author mengran
 */
public abstract class ThreadLocalPlaceholder {

    public static final String ERROR_MSG_KEY = "message";
    
    /**
     * 日志对象
     */
    protected static final Log LOG = LogFactory.getLog(ThreadLocalPlaceholder.class.getName());

    /**
     * 本地线程
     */
    private static final ThreadLocal<Map<Object, Object>> THREADLOCALRESOURCE = new ThreadLocal<Map<Object, Object>>();

    
    /**
     * 返回线程对象
     * 
     * @return
     */
    private static Map<Object, Object> getThreadMap() {

        // Get Current Thread Map
        Map<Object, Object> threadMap = THREADLOCALRESOURCE.get();

        if (threadMap == null) {
            threadMap = new HashMap<Object, Object>();
            THREADLOCALRESOURCE.set(threadMap);
        }

        return threadMap;
    }

    /**
     * Return thread-binding object<br>
     * and the giving key
     * 
     * @param key
     *            key of object
     * @return thread-binding object
     */
    public static Object getProperty(Object key) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        Map<Object, Object> queryMap = getThreadMap();

        LOG.debug("Retrieve Object [" + queryMap.get(key) + "] from thread [" + Thread.currentThread().getName()
                + "] using key[" + key + "].");

        return queryMap.get(key);
    }

    /**
     * Bind the object to thread. Store in to <code>Map</code> object.
     * 
     * @param key
     *            key of object
     * @param target
     *            bind target
     */
    public static void bindProperty(Object key, Object target) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        if (getProperty(key) != null) {
            LOG.debug("Already bind [" + key + "] to thread [" + Thread.currentThread().getName() + "], old value:{"
                    + getProperty(key) + "}, new value:{" + target == null ? "null" : target + "}");
            throw new RuntimeException("Already bind [" + key + "] to thread [" + Thread.currentThread().getName()
                    + "], old value:{" + getProperty(key) + "}, new value:{" + target == null ? "null" : target + "}");
        }

        Map<Object, Object> propertiesMap = getThreadMap();
        // Set value
        propertiesMap.put(key, target);

        LOG.debug("Bound Object [" + target + "] to thread [" + Thread.currentThread().getName() + "]");
    }

    /**
     * Remove the object from thread.
     * 
     * @param key
     *            key of object
     */
    public static void unbindProperty(Object key) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        // Get Current Thread Map
        Map<Object, Object> propertiesMap = getThreadMap();
        if (!propertiesMap.containsKey(key)) {
            LOG.debug("Removed value [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
        }

        propertiesMap.remove(key);

        LOG.debug("Removed key [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
    }

}

