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
 * 数据格式化类型
 * 
 * @author zhongyi
 *
 */
public enum DataFormat {
    /**
     * ID
     */
    ID("id", "III"),
    /**
     * INTEGER
     */
    INTEGER("integer", "I,III"),
    /**
     * FLOAT
     */
    FLOAT("float", "I,III.DD"),
    /**
     * PERCENTAGE
     */
    PERCENTAGE("percentage", "I,III.DD%");
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 样式
     */
    private String format;
    
    /**
     * 初始化
     * 
     * @param name
     *            名称
     * @param format
     *            样式
     */
    private DataFormat(String name, String format) {
        this.name = name;
        this.format = format;
    }
    
    /**
     * 得到格式
     * 
     * @param format
     *            格式名称
     * @return 格式对象
     */
    public static DataFormat parseByFormatExp(String format) {
        if (ID.getFormat().equals(format)) {
            return ID;
        } else if (INTEGER.getFormat().equals(format)) {
            return INTEGER;
        } else if (PERCENTAGE.getFormat().equals(format)) {
            return PERCENTAGE;
        } else if (FLOAT.getFormat().equals(format)) {
            return FLOAT;
        } else {
            return null;
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
     * get the format
     * 
     * @return the format
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * set the format
     * 
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }
}