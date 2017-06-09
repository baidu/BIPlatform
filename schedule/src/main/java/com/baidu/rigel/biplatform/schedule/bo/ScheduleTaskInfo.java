package com.baidu.rigel.biplatform.schedule.bo;

import java.io.Serializable;

/**
 * 调度引擎要执行调度时所需的任务信息
 * 
 * @author majun04
 *
 */
public class ScheduleTaskInfo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1170805453898693447L;
    /**
     * 任务名称（描述）
     */
    private String taskName;
    /**
     * 任务id，做存储用
     */
    private String taskId;
    /**
     * 任务调度表达式，cron类型
     */
    private String cronExpression;
    /**
     * 要执行的动作（多数为httpclietn要执行的action地址）
     */
    private String excuteAction;
    /**
     * 任务所属产品线名称
     */
    private String productLineName;

    /**
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return the taskId
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @param taskId the taskId to set
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * @return the cronExpression
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * @param cronExpression the cronExpression to set
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    /**
     * @return the excuteAction
     */
    public String getExcuteAction() {
        return excuteAction;
    }

    /**
     * @param excuteAction the excuteAction to set
     */
    public void setExcuteAction(String excuteAction) {
        this.excuteAction = excuteAction;
    }

    /**
     * @return the productLineName
     */
    public String getProductLineName() {
        return productLineName;
    }

    /**
     * @param productLineName the productLineName to set
     */
    public void setProductLineName(String productLineName) {
        this.productLineName = productLineName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ScheduleTaskInfo [taskName=" + taskName + ", cronExpression=" + cronExpression + ", excuteAction="
                + excuteAction + ", productLineName=" + productLineName + "]";
    }

}
