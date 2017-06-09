package com.baidu.rigel.biplatform.schedule.control;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.bo.TaskActionEnum;
import com.baidu.rigel.biplatform.schedule.bo.TaskExcuteAction;
import com.baidu.rigel.biplatform.schedule.utils.ScheduleHelper;

/**
 * 调度引擎向外提供的web版api接口
 * 
 * @author majun04
 *
 */
@RestController
@RequestMapping("/schedule/task")
public class TaskManagerControl {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManagerControl.class);

    /**
     * jmsTemplate
     */
    @Resource(name = "jmsTemplate")
    private JmsTemplate jmsTemplate = null;

    /**
     * queueDestination
     */
    @Resource(name = "scheduleTaskTopic")
    private ActiveMQTopic scheduleTaskTopic = null;

    /**
     * 保存调度任务
     * 
     * @param taskInfo 任务信息
     * @param request 请求request对象
     * @return 返回指定格式的ResponseResult对象
     */
    @RequestMapping(value = "/saveTask")
    public ResponseResult saveTask(@ModelAttribute ScheduleTaskInfo taskInfo, HttpServletRequest request) {
        String taskId = ScheduleHelper.generateTaskId(taskInfo);
        taskInfo.setTaskId(taskId);
        sendJmsTopic(taskInfo, TaskActionEnum.ADD);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("taskId", taskInfo.getTaskId());
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(map);
        return responseResult;
    }

    /**
     * 更新调度任务
     * 
     * @param taskInfo 任务信息
     * @param request 请求request对象
     * @return 返回指定格式的ResponseResult对象
     */
    @RequestMapping(value = "/updateTask")
    public ResponseResult updateTask(@ModelAttribute ScheduleTaskInfo taskInfo, HttpServletRequest request) {
        sendJmsTopic(taskInfo, TaskActionEnum.UPDATE);
        ResponseResult responseResult = new ResponseResult();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("taskId", taskInfo.getTaskId());
        responseResult.setData(map);
        return responseResult;

    }

    /**
     * 删除调度任务
     * 
     * @param taskInfo 任务信息
     * @param request 请求request对象
     * @return 返回指定格式的ResponseResult对象
     */
    @RequestMapping(value = "/deleteTask")
    public ResponseResult deleteTask(@ModelAttribute ScheduleTaskInfo taskInfo, HttpServletRequest request) {
        sendJmsTopic(taskInfo, TaskActionEnum.DELETE);
        ResponseResult responseResult = new ResponseResult();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("taskId", taskInfo.getTaskId());
        responseResult.setData(map);
        return responseResult;

    }

    /**
     * 给jms服务发送task操作消息
     * 
     * @param taskInfo
     * @param action
     */
    private void sendJmsTopic(ScheduleTaskInfo taskInfo, TaskActionEnum action) {
        jmsTemplate.send(scheduleTaskTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                LOG.debug("begin send jms objectMessage.... the taskInfo is :[" + taskInfo.toString() + "]");
                return session.createObjectMessage(TaskExcuteAction.newInstance(taskInfo, action));
            }
        });
    }
}
