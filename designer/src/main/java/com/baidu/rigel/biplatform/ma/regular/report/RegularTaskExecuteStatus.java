

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

/** 
 * 固定报表任务执行状态
 * @author yichao.jiang 
 * @version  2015年8月10日 
 * @since jdk 1.8 or after
 */
public enum RegularTaskExecuteStatus {
    
    /**
     * 运行中
     */
    RUNNING("运行中"),
    
    /**
     * 启动
     */
    START("启动"),
    
    /**
     * 停止
     */
    STOP("停止");
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 构造函数
     */
    RegularTaskExecuteStatus(String status) {
        this.status = status;
    }
    
    /** 
     * 获取 status 
     * @return the status 
     */
    public String getStatus() {
        return status;
    }
    
    /** 
     * 设置 status 
     * @param status the status to set 
     */
    public void setStatus(String status) {
        this.status = status;
    }
}

