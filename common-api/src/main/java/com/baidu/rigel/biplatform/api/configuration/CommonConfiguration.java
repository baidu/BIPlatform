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
package com.baidu.rigel.biplatform.api.configuration;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Designer项目springbean的Configuration定义类
 * 
 * @author majun04
 *
 */
@Configuration
@EnableConfigurationProperties
public class CommonConfiguration {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonConfiguration.class);
    /**
     * jms消息服务器url
     */
    @Value("${biplatform.ma.jms.jmsServerUrl}")
    private String jmsServerUrl;

    /**
     * jmsconnectionFactory实例
     * 
     * @return 返回被spring代理的connectionFactory实例bean
     */
    @Bean(name = "connectionFactory")
    public ConnectionFactory singleConnectionFactory() {
        SingleConnectionFactory connectionFactory = new SingleConnectionFactory();
        connectionFactory.setTargetConnectionFactory(pooledConnectionFactory());
        return connectionFactory;
    }

    /**
     * 采用activeMQ实现的ConnectionFactory实例对象
     * 
     * @return 返回采用activeMQ实现的ConnectionFactory实例对象
     */
    @Bean(name = "activeMQConnectionFactory")
    public ConnectionFactory activeMQConnectionFactory() {
        LOG.info("the activeMQConnection url is :[" + jmsServerUrl + "]");
        ConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(jmsServerUrl);
        return activeMQConnectionFactory;
    }

    /**
     * spring代理的pooledConnectionFactory实例对象
     * 
     * @return 返回spring代理的pooledConnectionFactory实例对象
     */
    @Bean(name = "pooledConnectionFactory")
    public PooledConnectionFactory pooledConnectionFactory() {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setMaxConnections(10);
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory());
        return pooledConnectionFactory;
    }

    /**
     * jmsTemplate
     * 
     * @return 返回jmsTemplate
     */
    @Bean(name = "jmsTemplate")
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(singleConnectionFactory());
        return jmsTemplate;
    }

    /**
     * 生成点对点的activeMQQueue对象
     * 
     * @return 返回ActiveMQTopic对象
     */
    @Bean(name = "scheduleTaskTopic")
    public ActiveMQTopic scheduleTaskTopic() {
        ActiveMQTopic queueTopic = new ActiveMQTopic("scheduleTaskTopic");
        return queueTopic;
    }

    /**
     * 生成点对点的activeMQQueue对象
     * 
     * @return 返回activeMQQueue对象
     */
    @Bean(name = "publishReportQueueDestination")
    public ActiveMQQueue activeMQQueue() {
        ActiveMQQueue queueDestination = new ActiveMQQueue("publishReportQueue");
        return queueDestination;
    }
    
    /**
     * 固定报表任务接收队列
     * @return
     */
    @Bean(name = "regularReportTaskQueue") 
    public ActiveMQQueue regularReportTaskQueue() {
        ActiveMQQueue taskQueue = new ActiveMQQueue("regularReportTaskQueue");
        return taskQueue;
    }
}
