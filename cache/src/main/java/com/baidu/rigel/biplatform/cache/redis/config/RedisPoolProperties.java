
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
package com.baidu.rigel.biplatform.cache.redis.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.baidu.rigel.biplatform.cache.StoreManager;

import redis.clients.jedis.Protocol;


/** 
 *  
 * @author xiaoming.chen
 * @version  2015年1月29日 
 * @since jdk 1.8 or after
 */
@ConfigurationProperties(prefix = "config.redis")
public class RedisPoolProperties extends RedisProperties {
    
    
    
    
    private RedisPoolProperties.Pool poolConfig;
    
    
    private boolean usePool = true;
    
    
    /** 
     * dev 是否是开发模式，开发模式的话，默认的KEY前面都会加上mac地址
     */
    private boolean dev = false;
    
    
    /** 
     * defaultExpire 默认超时时间
     */
    private long defaultExpire = -1;
    
    private Map<String, Long> cacheExpire = new HashMap<>();
    
    private String topicName = StoreManager.TOPICS;
    
    private String eventQueueName = StoreManager.EVENT_QUEUE;
    
    private String lockName = "lock";
    
    
    /** 
     * clusterPre 集群的前缀标识
     */
    private String clusterPre;
    
//    /** 
//     * redisServers 多个redis server的配置信息，多个redis server配置格式为  host1:port1:pass,host2:port2
//     * 如果多个服务器密码一直，可以配置一个，其它不写就可以了。
//     */
//    private String redisServers;
    /**
     * Pool properties.
     */
    public static class Pool{

        private int timeout = Protocol.DEFAULT_TIMEOUT;
        
        /**
         * if maxIdle == 0, ObjectPool has 0 size pool
         */
        private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
        
        /**
         * 
         */
        private long maxWait = GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

        private boolean testOnBorrow = GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW;
        
        private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

        private int maxActive = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;

        private boolean testOnReturn = GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN;

        private boolean testWhileIdle = GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;

        private long timeBetweenEvictionRunsMillis = GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

        private int numTestsPerEvictionRun =  GenericObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

        private long minEvictableIdleTimeMillis = GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

        private long softMinEvictableIdleTimeMillis = GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

        private boolean lifo = GenericObjectPoolConfig.DEFAULT_LIFO;
        
        private boolean testOnCreate = GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE;
        

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        /** 
         * 获取 timeout 
         * @return the timeout 
         */
        public int getTimeout() {
        
            return timeout;
        }

        /** 
         * 设置 timeout 
         * @param timeout the timeout to set 
         */
        public void setTimeout(int timeout) {
        
            this.timeout = timeout;
        }

        /** 
         * 获取 testOnBorrow 
         * @return the testOnBorrow 
         */
        public boolean isTestOnBorrow() {
        
            return testOnBorrow;
        }

        /** 
         * 设置 testOnBorrow 
         * @param testOnBorrow the testOnBorrow to set 
         */
        public void setTestOnBorrow(boolean testOnBorrow) {
        
            this.testOnBorrow = testOnBorrow;
        }

        /** 
         * 获取 testOnReturn 
         * @return the testOnReturn 
         */
        public boolean isTestOnReturn() {
        
            return testOnReturn;
        }

        /** 
         * 设置 testOnReturn 
         * @param testOnReturn the testOnReturn to set 
         */
        public void setTestOnReturn(boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        /** 
         * 获取 testWhileIdle 
         * @return the testWhileIdle 
         */
        public boolean isTestWhileIdle() {
        
            return testWhileIdle;
        }

        /** 
         * 设置 testWhileIdle 
         * @param testWhileIdle the testWhileIdle to set 
         */
        public void setTestWhileIdle(boolean testWhileIdle) {
        
            this.testWhileIdle = testWhileIdle;
        }

        /** 
         * 获取 timeBetweenEvictionRunsMillis 
         * @return the timeBetweenEvictionRunsMillis 
         */
        public long getTimeBetweenEvictionRunsMillis() {
        
            return timeBetweenEvictionRunsMillis;
        }

        /** 
         * 设置 timeBetweenEvictionRunsMillis 
         * @param timeBetweenEvictionRunsMillis the timeBetweenEvictionRunsMillis to set 
         */
        public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        }

        /** 
         * 获取 numTestsPerEvictionRun 
         * @return the numTestsPerEvictionRun 
         */
        public int getNumTestsPerEvictionRun() {
        
            return numTestsPerEvictionRun;
        }

        /** 
         * 设置 numTestsPerEvictionRun 
         * @param numTestsPerEvictionRun the numTestsPerEvictionRun to set 
         */
        public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        
            this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        }

        /** 
         * 获取 minEvictableIdleTimeMillis 
         * @return the minEvictableIdleTimeMillis 
         */
        public long getMinEvictableIdleTimeMillis() {
        
            return minEvictableIdleTimeMillis;
        }

        /** 
         * 设置 minEvictableIdleTimeMillis 
         * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set 
         */
        public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        }

        /** 
         * 获取 softMinEvictableIdleTimeMillis 
         * @return the softMinEvictableIdleTimeMillis 
         */
        public long getSoftMinEvictableIdleTimeMillis() {
        
            return softMinEvictableIdleTimeMillis;
        }

        /** 
         * 设置 softMinEvictableIdleTimeMillis 
         * @param softMinEvictableIdleTimeMillis the softMinEvictableIdleTimeMillis to set 
         */
        public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        
            this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
        }

        /** 
         * 获取 lifo 
         * @return the lifo 
         */
        public boolean isLifo() {
        
            return lifo;
        }

        /** 
         * 设置 lifo 
         * @param lifo the lifo to set 
         */
        public void setLifo(boolean lifo) {
        
            this.lifo = lifo;
        }

        /** 
         * 设置 maxWait 
         * @param maxWait the maxWait to set 
         */
        public void setMaxWait(long maxWait) {
        
            this.maxWait = maxWait;
        }

        /** 
         * 获取 maxWait 
         * @return the maxWait 
         */
        public long getMaxWait() {
        
            return maxWait;
        }

        /** 
         * 获取 testOnCreate 
         * @return the testOnCreate 
         */
        public boolean isTestOnCreate() {
        
            return testOnCreate;
        }

        /** 
         * 设置 testOnCreate 
         * @param testOnCreate the testOnCreate to set 
         */
        public void setTestOnCreate(boolean testOnCreate) {
        
            this.testOnCreate = testOnCreate;
        }
    }
    

    /** 
     * 获取 poolConfig 
     * @return the poolConfig 
     */
    public RedisPoolProperties.Pool getPoolConfig() {
    
        return poolConfig;
    }

    /** 
     * 设置 poolConfig 
     * @param poolConfig the poolConfig to set 
     */
    public void setPoolConfig(RedisPoolProperties.Pool poolConfig) {
    
        this.poolConfig = poolConfig;
    }




    /** 
     * 获取 usePool 
     * @return the usePool 
     */
    public boolean isUsePool() {
    
        return usePool;
    }

    /** 
     * 设置 usePool 
     * @param usePool the usePool to set 
     */
    public void setUsePool(boolean usePool) {
    
        this.usePool = usePool;
    }

    /** 
     * 获取 dev 
     * @return the dev 
     */
    public boolean isDev() {
    
        return dev;
    }

    /** 
     * 设置 dev 
     * @param dev the dev to set 
     */
    public void setDev(boolean dev) {
    
        this.dev = dev;
    }

    /** 
     * 获取 defaultExpire 
     * @return the defaultExpire 
     */
    public long getDefaultExpire() {
    
        return defaultExpire;
    }

    /** 
     * 设置 defaultExpire 
     * @param defaultExpire the defaultExpire to set 
     */
    public void setDefaultExpire(long defaultExpire) {
    
        this.defaultExpire = defaultExpire;
    }

    /** 
     * 获取 clusterPre 
     * @return the clusterPre 
     */
    public String getClusterPre() {
    
        return clusterPre;
    }

    /** 
     * 设置 clusterPre 
     * @param clusterPre the clusterPre to set 
     */
    public void setClusterPre(String clusterPre) {
    
        this.clusterPre = clusterPre;
    }

    /** 
     * 获取 cacheExpire 
     * @return the cacheExpire 
     */
    public Map<String, Long> getCacheExpire() {
    
        return cacheExpire;
    }

    /** 
     * 设置 cacheExpire 
     * @param cacheExpire the cacheExpire to set 
     */
    public void setCacheExpire(Map<String, Long> cacheExpire) {
    
        this.cacheExpire = cacheExpire;
    }

    /** 
     * 获取 topicName 
     * @return the topicName 
     */
    public String getTopicName() {
    
        return topicName;
    }

    /** 
     * 设置 topicName 
     * @param topicName the topicName to set 
     */
    public void setTopicName(String topicName) {
    
        this.topicName = topicName;
    }

    /** 
     * 获取 eventQueueName 
     * @return the eventQueueName 
     */
    public String getEventQueueName() {
    
        return eventQueueName;
    }

    /** 
     * 设置 eventQueueName 
     * @param eventQueueName the eventQueueName to set 
     */
    public void setEventQueueName(String eventQueueName) {
    
        this.eventQueueName = eventQueueName;
    }

    /** 
     * 获取 lockName 
     * @return the lockName 
     */
    public String getLockName() {
    
        return lockName;
    }

    /** 
     * 设置 lockName 
     * @param lockName the lockName to set 
     */
    public void setLockName(String lockName) {
    
        this.lockName = lockName;
    }
    
}

