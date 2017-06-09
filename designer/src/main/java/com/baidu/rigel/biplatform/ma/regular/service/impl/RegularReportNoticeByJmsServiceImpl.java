

/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.regular.service.impl;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportNoticeByJmsService;

/** 
 * 固定报表任务队列通知服务 
 * @author yichao.jiang 
 * @version  2015年8月3日 
 * @since jdk 1.8 or after
 */
@Service("regularReportNoticeByJmsService")
public class RegularReportNoticeByJmsServiceImpl implements RegularReportNoticeByJmsService {
    
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportNoticeByJmsServiceImpl.class);
    
    /**
     * jmsTemplate
     */
    @Resource(name = "jmsTemplate")
    private JmsTemplate jmsTemplate = null;

    /**
     * regularReportTaskQueue
     */
    @Resource(name = "regularReportTaskQueue")
    private ActiveMQQueue regularReportTaskQueue = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void putTaskIntoJmsQueue(RegularReportTaskInfo task) {  
        jmsTemplate.send(regularReportTaskQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                LOG.info("the task {" + task + "} is put into jms queue");
                return session.createObjectMessage(task);
            }
        });
    }
}

