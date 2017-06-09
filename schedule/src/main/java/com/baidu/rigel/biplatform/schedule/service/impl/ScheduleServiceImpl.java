package com.baidu.rigel.biplatform.schedule.service.impl;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.constant.ScheduleConstant;
import com.baidu.rigel.biplatform.schedule.job.HttpClientExcuteJob;
import com.baidu.rigel.biplatform.schedule.service.ScheduleService;

/**
 * scheduleService实现
 * 
 * @author majun04
 *
 */
@Service("scheduleService")
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Resource(name = "schedulerBean")
    private Scheduler quartzScheduler;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.ScheduleService#addTaskToSchedule(com.baidu.rigel.biplatform.schedule
     * .bo.ScheduleTaskInfo)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean addTaskToSchedule(ScheduleTaskInfo taskInfo) {
        boolean flag = true;
        JobDataMap newJobDataMap = new JobDataMap();
        newJobDataMap.put(ScheduleConstant.EXCUTE_ACTION_KEY, taskInfo.getExcuteAction());
        newJobDataMap.put(ScheduleConstant.TASK_ID, taskInfo.getTaskId());
        newJobDataMap.put(ScheduleConstant.PRODUCT_LINE_NAME, taskInfo.getProductLineName());
        newJobDataMap.put(ScheduleConstant.SCHEDULE_TASK_OBJ_KEY, taskInfo);
        JobDetail jobDetail =
                JobBuilder.newJob(HttpClientExcuteJob.class).withIdentity(generateJobKey(taskInfo))
                        .withDescription(taskInfo.getTaskName()).usingJobData(newJobDataMap).build();
        ScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(taskInfo.getCronExpression());
        Trigger trigger =
                TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(generateTriggerKey(taskInfo))
                        .withSchedule(scheduleBuilder).build();
        try {
            quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.ScheduleService#deleteTask4Schedule(com.baidu.rigel.biplatform.schedule
     * .bo.ScheduleTaskInfo)
     */
    public boolean deleteTask4Schedule(ScheduleTaskInfo taskInfo) {
        boolean flag = true;
        JobKey jobKey = generateJobKey(taskInfo);
        try {
            quartzScheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.schedule.service.ScheduleService#updateTask2Schedule(com.baidu.rigel.biplatform.schedule
     * .bo.ScheduleTaskInfo)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean updateTask2Schedule(ScheduleTaskInfo taskInfo) {
        boolean flag = true;
        JobKey jobKey = generateJobKey(taskInfo);
        JobDetail jobDetail = null;
        try {
            jobDetail = quartzScheduler.getJobDetail(jobKey);
        } catch (SchedulerException e1) {
            throw new RuntimeException(e1.getMessage(), e1);
        }
        jobDetail.getJobDataMap().put(ScheduleConstant.EXCUTE_ACTION_KEY, taskInfo.getExcuteAction());
        ScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(taskInfo.getCronExpression());
        TriggerKey triggerKey = generateTriggerKey(taskInfo);
        Trigger newTrigger =
                TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(triggerKey).withSchedule(scheduleBuilder)
                        .build();
        try {
            quartzScheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    /**
     * 生成JobKey对象
     * 
     * @param taskInfo 任务对象
     * @return 返回JobKey
     */
    private JobKey generateJobKey(ScheduleTaskInfo taskInfo) {
        return JobKey.jobKey(taskInfo.getTaskName() + "--" + taskInfo.getTaskId());
    }

    /**
     * 生成TriggerKey对象
     * 
     * @param taskInfo 任务对象
     * @return 返回TriggerKey
     */
    private TriggerKey generateTriggerKey(ScheduleTaskInfo taskInfo) {
        return TriggerKey.triggerKey(taskInfo.getTaskName() + "--" + taskInfo.getTaskId());
    }
}
