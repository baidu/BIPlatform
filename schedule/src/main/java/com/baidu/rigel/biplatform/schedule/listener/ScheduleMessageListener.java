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
package com.baidu.rigel.biplatform.schedule.listener;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.bo.TaskExcuteAction;
import com.baidu.rigel.biplatform.schedule.service.TaskManagerService;

/**
 * jms消息消费者监听类，主要接收和处理jms消息服务器中所对应的silkroad端发布的报表请求
 * 
 * @author majun04
 *
 */
@Service("scheduleMessageListener")
public class ScheduleMessageListener implements MessageListener {
    /**
     * 日志记录类
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleMessageListener.class);
    @Resource
    private TaskManagerService taskManagerService;

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        ObjectMessage objMsg = (ObjectMessage) message;
        try {
            if (objMsg.getObject() instanceof TaskExcuteAction) {
                TaskExcuteAction taskAction = (TaskExcuteAction) objMsg.getObject();
                ScheduleTaskInfo taskInfo = taskAction.getScheduleTaskInfo();
                LOGGER.info("the reciver message object is :[" + taskAction.getScheduleTaskInfo().toString() + "]");
                switch (taskAction.getActionEnum()) {
                    case ADD:
                        taskManagerService.addTaskToScheduleEngine(taskInfo);
                        break;
                    case UPDATE:
                        taskManagerService.updateTask4ScheduleEngine(taskInfo);
                        break;
                    case DELETE:
                        taskManagerService.deleteTask4ScheduleEngine(taskInfo);
                        break;
                    default:
                        throw new RuntimeException("Unknown action type !!!");
                }
            }
        } catch (JMSException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
