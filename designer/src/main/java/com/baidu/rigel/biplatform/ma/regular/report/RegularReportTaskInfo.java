

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

package com.baidu.rigel.biplatform.ma.regular.report;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.google.common.collect.Sets;

/** 
 * 固定报表任务，继承自任务调度的任务信息
 * @author yichao.jiang 
 * @version  2015年8月4日 
 * @since jdk 1.8 or after
 */
public class RegularReportTaskInfo extends ScheduleTaskInfo implements Serializable {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 2521055236432999999L;
     
    /**
     * 记录该固定报表信息所属的报表id
     */
    private String reportId;
    
    /**
     * 固定报表对应的参数信息，定义为Set，方便前端处理
     */
    private Set<RegularReportParam> params = Sets.newHashSet();
        
    /**
     * 任务执行失败时的最多重试次数
     */
    private String maxRetryTimesIfTaskFail;
    

    /**
     * 任务执行失败时的邮件接收人
     */
    private List<String> mailReceiversIfTaskFail;
    
    /**
     * 是否立即运行
     */
    private boolean isRunNow;
    
    /**
     * 任务执行策略
     */
    private ExecuteTaskStrategy executeStrategy;
    
    /**
     * TODO 运行信息
     */
    private RegularTaskExecuteStatus executeStatus = RegularTaskExecuteStatus.RUNNING;
    
    /** 
     * 获取 reportId 
     * @return the reportId 
     */
    public String getReportId() {
        return reportId;
    }
    
    /** 
     * 设置 reportId 
     * @param reportId the reportId to set 
     */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    /** 
     * 获取 params 
     * @return the params 
     */
    public Set<RegularReportParam> getParams() {
        return params;
    }

    /** 
     * 设置 params 
     * @param params the params to set 
     */
    public void setParams(Set<RegularReportParam> params) {
        this.params = params;
    }

    /** 
     * 获取 maxRetryTimesIfTaskFail 
     * @return the maxRetryTimesIfTaskFail 
     */
    public String getMaxRetryTimesIfTaskFail() {
        return maxRetryTimesIfTaskFail;
    }

    /** 
     * 设置 maxRetryTimesIfTaskFail 
     * @param maxRetryTimesIfTaskFail the maxRetryTimesIfTaskFail to set 
     */
    public void setMaxRetryTimesIfTaskFail(String maxRetryTimesIfTaskFail) {
        this.maxRetryTimesIfTaskFail = maxRetryTimesIfTaskFail;
    }

    /** 
     * 获取 mailReceiversIfTaskFail 
     * @return the mailReceiversIfTaskFail 
     */
    public List<String> getMailReceiversIfTaskFail() {
        return mailReceiversIfTaskFail;
    }

    /** 
     * 设置 mailReceiversIfTaskFail 
     * @param mailReceiversIfTaskFail the mailReceiversIfTaskFail to set 
     */
    public void setMailReceiversIfTaskFail(List<String> mailReceiversIfTaskFail) {
        this.mailReceiversIfTaskFail = mailReceiversIfTaskFail;
    }

    
    /** 
     * 获取 isRunNow 
     * @return the isRunNow 
     */
    public boolean getIsRunNow() {
        return isRunNow;
    }

    /** 
     * 设置 isRunNow 
     * @param isRunNow the isRunNow to set 
     */
    public void setIsRunNow(boolean isRunNow) {
        this.isRunNow = isRunNow;
    }

    
    /** 
     * 获取 executeStrategy 
     * @return the executeStrategy 
     */
    public ExecuteTaskStrategy getExecuteStrategy() {
        return executeStrategy;
    }

    /** 
     * 设置 executeStrategy 
     * @param executeStrategy the executeStrategy to set 
     */
    public void setExecuteStrategy(ExecuteTaskStrategy executeStrategy) {
        this.executeStrategy = executeStrategy;
    }

    
    /** 
     * 获取 status 
     * @return the status 
     */
    public RegularTaskExecuteStatus getExecuteStatus() {
        return executeStatus;
    }

    /** 
     * 设置 status 
     * @param status the status to set 
     */
    public void setExecuteStatus(RegularTaskExecuteStatus executeStatus) {
        this.executeStatus = executeStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "taskId: " + getTaskId() + ", taskName: " + getTaskName() + ", cronExpression: " + getCronExpression() 
                + ", params: " + params + ", executeTaskAction: " + getExcuteAction();
    }
}

