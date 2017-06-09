
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

/**
 * 任务执行策略
 * @author yichao.jiang
 * @version 2015年8月9日
 * @since jdk 1.8 or after
 */
public class ExecuteTaskStrategy implements Serializable {

    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 6287277658696586138L;

    /**
     * 小时
     */
    private String hour;

    /**
     * 分钟
     */
    private String minute;

    /**
     * 执行粒度，对应每日、每周、每月、每季和每年
     */
    private String granularity;

    /**
     * 具体粒度下的执行规则，每周(周一、周二等），每月（1日、2日和最后一日等）
     */
    private String detail;

    /** 
     * 获取 hour 
     * @return the hour 
     */
    public String getHour() {
        return hour;
    }

    /** 
     * 设置 hour 
     * @param hour the hour to set 
     */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /** 
     * 获取 minute 
     * @return the minute 
     */
    public String getMinute() {
        return minute;
    }

    /** 
     * 设置 minute 
     * @param minute the minute to set 
     */
    public void setMinute(String minute) {
        this.minute = minute;
    }

    /** 
     * 获取 granularity 
     * @return the granularity 
     */
    public String getGranularity() {
        return granularity;
    }

    /** 
     * 设置 granularity 
     * @param granularity the granularity to set 
     */
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    /** 
     * 获取 detail 
     * @return the detail 
     */
    public String getDetail() {
        return detail;
    }

    /** 
     * 设置 detail 
     * @param detail the detail to set 
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }
    
}
