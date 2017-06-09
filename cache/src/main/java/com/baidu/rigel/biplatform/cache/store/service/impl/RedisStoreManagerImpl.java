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
package com.baidu.rigel.biplatform.cache.store.service.impl;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.cache.RedissonCache;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.redis.config.RedisPoolProperties;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.cache.util.MacAddressUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author xiaoming.chen
 * @version 2015年2月9日
 * @since jdk 1.8 or after
 */
public class RedisStoreManagerImpl implements StoreManager, InitializingBean {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    // private static final String REDIS_LOCK = "RedisLock";

    @Autowired(required = false)
    private Redisson redisson;

    @Autowired(required = false)
    private CacheManager redisCacheManager;

    @Autowired(required = false)
    private RedisPoolProperties redisProperties;

    private String topicKey = TOPICS;

    private String queueKey = EVENT_QUEUE;

    private String lockKey = LOCK_KEY;

    private String cachePrefix = "";

    /**
     * @return the topicKey
     */
    public String getTopicKey() {
        return topicKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.cache.StoreManager#getDataStore(java.lang.String)
     */
    @Override
    public Cache getDataStore(String name) {
        if (redisCacheManager != null) {
            return redisCacheManager.getCache(name);
        }

        RMap<Object, Object> map = redisson.getMap(cachePrefix + "_" + name);
        if (redisProperties.getCacheExpire().containsKey(name)) {
            map.expire(redisProperties.getCacheExpire().get(name), TimeUnit.SECONDS);
        } else if (redisProperties.getDefaultExpire() > 0) {
            map.expire(redisProperties.getDefaultExpire(), TimeUnit.SECONDS);
        }
        return new RedissonCache(map, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.cache.StoreManager#putEvent(java.util.EventObject)
     */
    @Override
    public void putEvent(EventObject event) throws Exception {
        redisson.getQueue(queueKey).add(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.cache.StoreManager#getNextEvent()
     */
    @Override
    public EventObject getNextEvent() throws Exception {
        Object obj = redisson.getQueue(queueKey).poll();
        return obj == null ? null : (EventObject) obj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.cache.StoreManager#postEvent(java.util.EventObject)
     */
    @Override
    public void postEvent(EventObject event) throws Exception {
        // redisson.getTopic(topicKey).publish(event);
        StringBuilder msg = new StringBuilder();
        msg.append(event.getClass().getName());
        msg.append("~~~");
        if (StoreManager.UDF_SETTING.containsKey(StoreManager.SERIALIZER_KEY)) {
            final Function<Object[], Object> function = StoreManager.UDF_SETTING.get(StoreManager.SERIALIZER_KEY);
            msg.append(function.apply(new Object[] { event }).toString());
        } else {
            Gson gson = new GsonBuilder().create();
            msg.append(gson.toJson(event));
        }
        StringRedisTemplate template = (StringRedisTemplate) ApplicationContextHelper.getContext().getBean("template");
        template.convertAndSend(topicKey, msg.toString());
        log.info("post topic into redis key:{},event:{}", topicKey, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.cache.StoreManager#getClusterLock()
     */
    @Override
    public Lock getClusterLock() {
        // return new java.util.concurrent.locks.ReentrantLock ();
        // return redisson.getLock(lockKey);
        return RedisCacheLock.getInstance(lockKey);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        List<String> prefix = new ArrayList<>();
        if (this.redisProperties.isDev()) {
            try {
                prefix.add(MacAddressUtil.getMachineNetworkFlag(null));
            } catch (SocketException | UnknownHostException e) {
                log.warn("get mac add error:{}", e.getMessage());
            }
        }
        if (!StringUtils.isEmpty(this.redisProperties.getClusterPre())) {
            prefix.add(this.redisProperties.getClusterPre());
        }
        if (CollectionUtils.isNotEmpty(prefix)) {
            this.cachePrefix = org.apache.commons.lang.StringUtils.join(prefix, "_");
            log.info("this instance is run with dev mode,current mac :{}", cachePrefix);
            topicKey = cachePrefix + "_" + redisProperties.getTopicName();
            queueKey = cachePrefix + "_" + redisProperties.getEventQueueName();
            lockKey = cachePrefix + "_" + redisProperties.getLockName();
        }
        // redisson.getTopic(topicKey).addListener(new RedisTopicListener());
    }

    @Override
    public Lock getClusterLock(String lockName) {
        // return redisson.getLock(lockName);
        return RedisCacheLock.getInstance(lockName);
    }

}
