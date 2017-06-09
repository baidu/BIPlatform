

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

package com.baidu.rigel.biplatform.ma.regular.service;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;

/** 
 * 固定报表任务队列消费者消息监听器 
 * @author yichao.jiang 
 * @version  2015年8月3日 
 * @since jdk 1.8 or after
 */
@Service("regularReportTaskConsumerMessageListener")
public class RegularReportTaskConsumerMessageListener implements MessageListener {

    /**
     * 日志记录
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportTaskConsumerMessageListener.class);
    
    /**
     * 任务执行服务
     */
    @Resource
    private RegularReportExecuteTaskService regularReportExecuteTaskService;
    
    /**
     * {@inheritDoc}
     * <br> 处理消息队列中的任务
     */
    @Override
    public void onMessage(Message msg) {
        ObjectMessage objMsg = (ObjectMessage) msg;
        try {
            if (objMsg.getObject() instanceof RegularReportTaskInfo) {
                RegularReportTaskInfo task = (RegularReportTaskInfo) objMsg.getObject();
                LOG.info("the task { " + task + "} was received by listener");
                // 校验任务id和报表id是否为空
                if (StringUtils.isEmpty(task.getTaskId()) || StringUtils.isEmpty(task.getReportId()) 
                        || StringUtils.isEmpty(task.getProductLineName())) {
                    throw new RuntimeException("the task { " + task 
                            + "} 's task id or report or productLine or id is empty");
                }
                // 执行任务
                regularReportExecuteTaskService.executeTask(task.getReportId(), 
                        task.getTaskId(), task.getProductLineName());
            }
        } catch (JMSException | RuntimeException e) {
            LOG.error("regular report jms task queue listener happened exception!", e.fillInStackTrace());  
        } 
    }

}

