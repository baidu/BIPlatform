package com.baidu.rigel.biplatform.schedule.service;

import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;

/**
 * 调度Service，基于quartz实现
 * 
 * @author majun04
 *
 */
public interface ScheduleService {
    /**
     * 将task加入Schedule服务
     * 
     * @param taskInfo 调度任务信息
     * @return 是否成功执行标识
     */
    public boolean addTaskToSchedule(ScheduleTaskInfo taskInfo);

    /**
     * 在Schedule服务中更新task
     * 
     * @param taskInfo 调度任务信息
     * @return 是否成功执行标识
     */
    public boolean updateTask2Schedule(ScheduleTaskInfo taskInfo);

    /**
     * 从Schedule服务中删除task
     * 
     * @param taskInfo 调度任务信息
     * @return 是否成功执行标识
     */
    public boolean deleteTask4Schedule(ScheduleTaskInfo taskInfo);
}
