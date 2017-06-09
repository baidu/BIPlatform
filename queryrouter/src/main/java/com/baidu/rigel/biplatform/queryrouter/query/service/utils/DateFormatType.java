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
package com.baidu.rigel.biplatform.queryrouter.query.service.utils;

import java.text.SimpleDateFormat;

/**
 * 
 * DateFormatType 日期格式类型
 * 
 * @author lijin
 *
 */
public enum DateFormatType {
    /**
     * DATE_FORMAT_YYYY_MM_DD
     */
    DATE_FORMAT_YYYY_MM_DD("yyyy-MM-dd"),
    /**
     * DATE_FORMAT_YYYYMMDD
     */
    DATE_FORMAT_YYYYMMDD("yyyyMMdd");
    
    /**
     * 格式串
     */
    private String dateFormatStr;
    /**
     * formatter
     */
    private SimpleDateFormat formatter;
    
    /**
     * DateFormatType 构造函数
     * @param dateFormatStr 格式串
     */
    private DateFormatType(String dateFormatStr) {
        this.dateFormatStr = dateFormatStr;
        formatter = new SimpleDateFormat(this.dateFormatStr);
    }
    
    /**
     * getter method for property dateFormatStr
     * 
     * @return the dateFormatStr
     */
    public final String getDateFormatStr() {
        return dateFormatStr;
    }
    
    /**
     * getter method for property formatter
     * 
     * @return the formatter
     */
    public final SimpleDateFormat getFormatter() {
        return formatter;
    }
    
}
