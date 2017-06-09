package com.baidu.rigel.biplatform.schedule.bo;

import java.io.Serializable;

/**
 * 任务执行对象vo
 * 
 * @author majun04
 *
 */
public class TaskExcuteAction implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6400385852853592485L;

    private ScheduleTaskInfo scheduleTaskInfo = null;

    private TaskActionEnum actionEnum = null;

    private TaskExcuteAction(ScheduleTaskInfo scheduleTaskInfo, TaskActionEnum actionEnum) {
        this.scheduleTaskInfo = scheduleTaskInfo;
        this.actionEnum = actionEnum;
    }

    public static TaskExcuteAction newInstance(ScheduleTaskInfo scheduleTaskInfo, TaskActionEnum actionEnum) {
        return new TaskExcuteAction(scheduleTaskInfo, actionEnum);
    }

    /**
     * @return the scheduleTaskInfo
     */
    public ScheduleTaskInfo getScheduleTaskInfo() {
        return scheduleTaskInfo;
    }

    /**
     * @param scheduleTaskInfo the scheduleTaskInfo to set
     */
    public void setScheduleTaskInfo(ScheduleTaskInfo scheduleTaskInfo) {
        this.scheduleTaskInfo = scheduleTaskInfo;
    }

    /**
     * @return the actionEnum
     */
    public TaskActionEnum getActionEnum() {
        return actionEnum;
    }

    /**
     * @param actionEnum the actionEnum to set
     */
    public void setActionEnum(TaskActionEnum actionEnum) {
        this.actionEnum = actionEnum;
    }

}
