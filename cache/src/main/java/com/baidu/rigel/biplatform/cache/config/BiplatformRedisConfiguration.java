/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.rigel.biplatform.cache.config;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections.CollectionUtils;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.codec.SerializationCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.redis.RedisProperties.Sentinel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import redis.clients.jedis.JedisPoolConfig;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.redis.config.HazelcastProperties;
import com.baidu.rigel.biplatform.cache.redis.config.RedisPoolProperties;
import com.baidu.rigel.biplatform.cache.redis.listener.RedisQueueListener;
import com.baidu.rigel.biplatform.cache.store.service.HazelcastNoticePort;
import com.baidu.rigel.biplatform.cache.store.service.HazelcastQueueItemListener;
import com.baidu.rigel.biplatform.cache.store.service.LocalEventListenerThread;
import com.baidu.rigel.biplatform.cache.store.service.impl.HazelcastStoreManager;
import com.baidu.rigel.biplatform.cache.store.service.impl.MessageReceiver;
import com.baidu.rigel.biplatform.cache.store.service.impl.RedisStoreManagerImpl;
import com.baidu.rigel.biplatform.cache.util.MacAddressUtil;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's Redis support.
 * 
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Christian Dupuis
 */
@Configuration
@ConditionalOnClass({ Redisson.class})
@EnableConfigurationProperties
public class BiplatformRedisConfiguration {
    
    private static Logger log = LoggerFactory.getLogger(BiplatformRedisConfiguration.class);


    @Bean
    @ConditionalOnMissingBean
    RedisPoolProperties redisPoolProperties() {
        return new RedisPoolProperties();
    }

    /**
     * Base class for Redis configurations.
     */
    protected static abstract class AbstractRedisConfiguration {

        @Autowired
        protected RedisPoolProperties properties;
        
        @Autowired(required=false)
        protected RedisSentinelConfiguration sentinelConfiguration;

        protected final Config getConfig() {
            Config config = new Config();
            config.setCodec(new SerializationCodec());
            if(properties.getSentinel() != null) {
                return createSentinelServerConfig(config, properties.getSentinel());
            } else {
                config.useSingleServer()
                .setAddress(properties.getHost())
                .setConnectionPoolSize(properties.getPoolConfig().getMaxActive())
                .setDatabase(properties.getDatabase())
                .setPassword(properties.getPassword())
                .setTimeout(properties.getPoolConfig().getTimeout());
                return config;
            }
        }
        
        
        private Config createSentinelServerConfig(Config config, Sentinel sentinel) {
            config.useSentinelConnection()
            .setMasterName(sentinel.getMaster())
            .addSentinelAddress(StringUtils.commaDelimitedListToStringArray(sentinel.getNodes()))
            .setDatabase(properties.getDatabase())
            .setPassword(properties.getPassword())
            .setMasterConnectionPoolSize(properties.getPoolConfig().getMaxActive())
            .setTimeout(properties.getPoolConfig().getTimeout());
            return config;
        }
        
        
        protected final JedisConnectionFactory applyProperties(
                JedisConnectionFactory factory) {
            factory.setHostName(this.properties.getHost());
            factory.setPort(this.properties.getPort());
            if (this.properties.getPassword() != null) {
                factory.setPassword(this.properties.getPassword());
            }
            factory.setDatabase(this.properties.getDatabase());
            return factory;
        }

        protected final RedisSentinelConfiguration getSentinelConfig() {
            if (this.sentinelConfiguration != null) {
                return this.sentinelConfiguration;
            }
            Sentinel sentinelProperties = this.properties.getSentinel();
            if (sentinelProperties != null) {
                RedisSentinelConfiguration config = new RedisSentinelConfiguration();
                config.master(sentinelProperties.getMaster());
                config.setSentinels(createSentinels(sentinelProperties));
                
                
                return config;
            }
            return null;
        }

        private List<RedisNode> createSentinels(Sentinel sentinel) {
            List<RedisNode> sentinels = new ArrayList<RedisNode>();
            String nodes = sentinel.getNodes();
            for (String node : StringUtils.commaDelimitedListToStringArray(nodes)) {
                try {
                    String[] parts = StringUtils.split(node, ":");
                    Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                    sentinels.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
                }
                catch (RuntimeException ex) {
                    throw new IllegalStateException("Invalid redis sentinel "
                            + "property '" + node + "'", ex);
                }
            }
            return sentinels;
        }


    }
    
    protected static class ClusterRedisCachePrefix implements RedisCachePrefix {
        
        private List<String> prefix;

        private final RedisSerializer<String> serializer = new StringRedisSerializer();
        private final String delimiter;
        
        /** 
         * 构造函数
         */
        public ClusterRedisCachePrefix(String delimiter,List<String> prefix) {
            this.prefix = prefix;
            if(StringUtils.isEmpty(delimiter)) {
                this.delimiter = ":";
            }else {
                this.delimiter = delimiter;
            }
        }


        /** 
         * 构造函数
         */
        public ClusterRedisCachePrefix(List<String> prefix) {
            this.prefix = prefix;
            this.delimiter = ":";
        }


        /** 
         * 获取 prefix 
         * @return the prefix 
         */
        public List<String> getPrefix() {
        
            return prefix;
        }


        /** 
         * 设置 prefix 
         * @param prefix the prefix to set 
         */
        public void setPrefix(List<String> prefix) {
        
            this.prefix = prefix;
        }


        @Override
        public byte[] prefix(String cacheName) {
            StringBuilder sb = new StringBuilder();
            if(CollectionUtils.isNotEmpty(prefix)) {
                sb.append(org.apache.commons.lang.StringUtils.join(prefix, delimiter)).append(delimiter);
            }
            sb.append(cacheName).append(delimiter);
            return serializer.serialize(sb.toString());
            
        }
        
        
    }

    /**
     * Redis pooled connection configuration.
     */
    @Configuration
    protected static class RedisPooledConnectionConfiguration extends
            AbstractRedisConfiguration {

        
        @Bean
        @ConditionalOnProperty(prefix = "config.redis", name = "active", havingValue = "true")
        public Redisson redisson(){
            
            return Redisson.create(getConfig());
        }
        
        @Bean (name = "redisConnectionFactory")
        @ConditionalOnProperty(prefix = "config.redis", name = "active", havingValue = "true")
        public RedisConnectionFactory redisConnectionFactory()
                throws UnknownHostException {
            return applyProperties(createJedisConnectionFactory());
        }
        
        
        
        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        public RedisTemplate<Object, Object> redisTemplate(
                RedisConnectionFactory redisConnectionFactory)
                throws UnknownHostException {
            RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }
        
        @Bean(name="redisCacheManager")
        @ConditionalOnBean(RedisTemplate.class)
        public CacheManager redisCacheManager(RedisTemplate<Object, Object> template) {
            RedisCacheManager redisCacheManager = new RedisCacheManager(template);
            redisCacheManager.setDefaultExpiration(this.properties.getDefaultExpire());
            List<String> prefix = new ArrayList<>();
            prefix.add("jedis");
            if(this.properties.isDev()) {
                try {
                    prefix.add(MacAddressUtil.getMachineNetworkFlag(null));
                } catch (SocketException | UnknownHostException e) {
                    log.warn("get mac add error:{}", e.getMessage());
                }
            }
            if(!StringUtils.isEmpty(this.properties.getClusterPre())) {
                prefix.add(this.properties.getClusterPre());
            }
            redisCacheManager.setUsePrefix(true);
            redisCacheManager.setExpires(this.properties.getCacheExpire());
            
            redisCacheManager.setCachePrefix(new ClusterRedisCachePrefix(prefix));
            
            return redisCacheManager;
        }
        
        @Bean(name="redisStoreManager")
        @ConditionalOnBean(Redisson.class)
        public StoreManager redisStoreManager() {
            return new RedisStoreManagerImpl();
        }
        
        @Bean
        @ConditionalOnBean(Redisson.class)
        public RedisQueueListener redisQueueListener() {
            return new RedisQueueListener();
        }
        
        @Bean
        @ConditionalOnMissingBean(name = "hazelcastStoreManager")
        public HazelcastProperties hazelcastProperties() {
            return new HazelcastProperties();
        }
        
        @Bean(name="hazelcastStoreManager")
        @ConditionalOnMissingBean(name = "redisStoreManager")
        public StoreManager hazelcastStoreManager(HazelcastProperties properties) {
            return new HazelcastStoreManager(properties);
        }
        
        @Bean
        @ConditionalOnBean(name="hazelcastStoreManager")
        public HazelcastNoticePort hazelcastNoticePort() {
            return new HazelcastNoticePort();
        }
        
        @Bean
        @ConditionalOnBean(name="hazelcastStoreManager")
        public HazelcastQueueItemListener hazelcastQueueItemListener() {
            return new HazelcastQueueItemListener();
        }
        
        @Bean
        @ConditionalOnBean(name="hazelcastStoreManager")
        public LocalEventListenerThread localEventListenerThread() {
            return new LocalEventListenerThread();
        }

        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        RedisMessageListenerContainer container (RedisConnectionFactory factory, MessageListenerAdapter adapter) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer ();
            container.setConnectionFactory (factory);
            final String topicKey = ((RedisStoreManagerImpl) redisStoreManager ()).getTopicKey();
            container.addMessageListener (adapter, new PatternTopic(topicKey));
            return container;
        }
        
        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        MessageListenerAdapter listenerAdapter (MessageReceiver receiver) {
            return new MessageListenerAdapter (receiver, "receiveMessage");
        }
        
        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        MessageReceiver receiver (CountDownLatch latch) {
            return new MessageReceiver(latch);
        }
        
        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        CountDownLatch latch() {
            return new CountDownLatch(1);
        }

        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate (connectionFactory);
        }
        
        private JedisConnectionFactory createJedisConnectionFactory() {
            JedisConnectionFactory factory = null;
            if (this.properties.getPoolConfig() != null) {
                factory = new JedisConnectionFactory(getSentinelConfig(), jedisPoolConfig());
            }
            factory = new JedisConnectionFactory(getSentinelConfig());
            factory.setUsePool(this.properties.isUsePool());
            factory.setPassword(this.properties.getPassword());
            return factory;
        }

        private JedisPoolConfig jedisPoolConfig() {
            JedisPoolConfig config = new JedisPoolConfig();
            RedisPoolProperties.Pool props = this.properties.getPoolConfig();
            config.setMaxTotal(props.getMaxActive());
            config.setMaxIdle(props.getMaxIdle());
            config.setMinIdle(props.getMinIdle());
            config.setMaxWaitMillis(props.getMaxWait());
            config.setTestOnBorrow(props.isTestOnBorrow());
            config.setTestOnCreate(props.isTestOnCreate());
            config.setTestOnReturn(props.isTestOnReturn());
            config.setTestWhileIdle(props.isTestWhileIdle());
            config.setTimeBetweenEvictionRunsMillis(props.getTimeBetweenEvictionRunsMillis());
            config.setMinEvictableIdleTimeMillis(props.getMinEvictableIdleTimeMillis());
            config.setNumTestsPerEvictionRun(props.getNumTestsPerEvictionRun());
            config.setSoftMinEvictableIdleTimeMillis(props.getSoftMinEvictableIdleTimeMillis());
            config.setLifo(props.isLifo());
            return config;
        }

    }



}
