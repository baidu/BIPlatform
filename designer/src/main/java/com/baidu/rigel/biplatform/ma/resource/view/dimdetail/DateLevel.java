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
package com.baidu.rigel.biplatform.ma.resource.view.dimdetail;

import java.io.Serializable;

/**
 * 
 * 时间维度定义
 * @author zhongyi
 *
 * 2014-7-31
 */
public class DateLevel implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7505458625873731115L;

    /**
     * 天粒度
     */
    private String day;
    
    /**
     * 周粒度
     */
    private String week;
    
    /**
     * 月粒度
     */
    private String month;
    
    /**
     * 季度粒度
     */
    private String quarter;

    /**
     * get the day
     * @return the day
     */
    public String getDay() {
        return day;
    }

    /**
     * set the day
     * @param day the day to set
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * get the week
     * @return the week
     */
    public String getWeek() {
        return week;
    }

    /**
     * set the week
     * @param week the week to set
     */
    public void setWeek(String week) {
        this.week = week;
    }

    /**
     * get the month
     * @return the month
     */
    public String getMonth() {
        return month;
    }

    /**
     * set the month
     * @param month the month to set
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * get the quarter
     * @return the quarter
     */
    public String getQuarter() {
        return quarter;
    }

    /**
     * set the quarter
     * @param quarter the quarter to set
     */
    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }
}