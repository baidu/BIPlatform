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
package com.baidu.rigel.biplatform.ma.report.query.chart;

/**
 * XAxisType X轴的类型信息，给前段用作时间优化
 * 
 * @author zhongyi
 *
 */
public enum XAxisType {
    /**
     * 字符串类型
     */
    CATEGORY("category", "字符串"),
    /**
     * 年
     */
    YEAR("year", "yyyy"),
    /**
     * 年月
     */
    MONTH("month", "yyyy-MM"),
    /**
     * 年月日表示的周
     */
    WEEK("week", "yyyy-MM-dd"),
    /**
     * 年季度
     */
    QUARTER("quarter", "yyyy-Qn"),
    /**
     * 年月日
     */
    DATETIME("datetime", "yyyy-MM-dd");
    
    /**
     * 名称
     */
    private String name;
    /**
     * 备注的样式
     */
    private String comment;
    
    /**
     * 初始化
     * 
     * @param name
     *            名称
     * @param comment
     *            注释
     */
    private XAxisType(String name, String comment) {
        this.setName(name);
        this.setComment(comment);
    }
    
    /**
     * parse from name
     * 
     * @param name
     *            名称
     * @return 类型
     */
    public static XAxisType parseXAxisType (String name) {
        if (YEAR.getName().equals(name)) {
            return YEAR;
        } else if (QUARTER.getName().equals(name)) {
            return QUARTER;
        } else if (MONTH.getName().equals(name)) {
            return MONTH;
        } else if (DATETIME.getName().equals(name)) {
            return DATETIME;
        } else if (WEEK.getName().equals(name)) {
            return WEEK;
        } else if (CATEGORY.getName().equals(name)) {
            return CATEGORY;
        } else {
            return CATEGORY;
        }
    }
    
    /**
     * get the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * set the name
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * get the comment
     * 
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * set the comment
     * 
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}