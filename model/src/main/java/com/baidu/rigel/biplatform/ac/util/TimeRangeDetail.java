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
package com.baidu.rigel.biplatform.ac.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

/**
 * 时间范围：比如2014年9月8日到2014年9月14日是一周，那TimeRange将记录start日期为20140908，end时间为20140914
 * 时间范围表示闭区间，为计算与表示方便，起至时间均用字符串表示，对于任何时间粒度的时间值，均表示为一个闭区间，如果表示当前
 * 起至时间一致
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class TimeRangeDetail {
    
    /**
     * 默认时间字符串格式
     */
    public static final String FORMAT_STRING = "yyyyMMdd";
    
    private SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
    /**
     * 开始时间
     */
    private final String start;
    
    /**
     * 中指时间
     */
    private final String end;

    /**
     * 构造函数
     * @param start
     * @param end
     * TimeRange
     */
    public TimeRangeDetail(String start, String end) {
        super();
        this.start = start.replace ("-", "");
        this.end = end.replace ("-", "");
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * 将起至时间转换成日期类型
     * @return 已经转换的开始时间
     * @throws Exception 格式或者字符串内容正确
     */
    public Date getStartTime() throws Exception {
        return getTime(start);
    }
    
    /**
     * 将结束时间转换成日期类型
     * @return 已经转换的时间
     * @throws Exception 格式或者字符串内容正确
     */
    public Date getEndTime() throws Exception {
        return getTime(end);
    }
    
    /**
     * 将给定字符串转换成时间类型
     * @param timeStr 时间字符串，要求yyyyMMdd格式
     * @return 转换成时间类型字符串
     * @throws Exception 格式或者字符串内容正确
     */
    public static Date getTime(String timeStr) throws Exception {
        SimpleDateFormat format = initDateFormat();
        return format.parse(timeStr);
    }
    
    /**
     * 将时间转换为字符串时间
     * @param date 日期
     * @return 日期为空，返回当前日期，否则返回指定日期字符串
     */
    public static String toTime(Date date) {
        if (date == null) {
            date = new GregorianCalendar().getTime();
        }
        SimpleDateFormat format = initDateFormat();
        return format.format(date);
    }
    
    /**
     * 获取当前时间范围内的所有天成员
     * @return 
     */
    public String[] getDays() {
        
        
        if (StringUtils.isBlank(this.start)) {
            throw new IllegalArgumentException("start can not be null for timerange");
        }
        if (StringUtils.isBlank(this.end)) {
            throw new IllegalArgumentException("end can not be null for timerange");
        }
        
        Calendar startCalen = Calendar.getInstance();
        Calendar endCalen = Calendar.getInstance();
        try {
            startCalen.setTime(sdf.parse(start));
            endCalen.setTime(sdf.parse(end));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        List<String> days = Lists.newArrayList();
        do {
            days.add(sdf.format(startCalen.getTime()));
            startCalen.add(Calendar.DAY_OF_MONTH, 1);
        } while (endCalen.equals(startCalen) || endCalen.after(startCalen));
        
        return days.toArray(new String[0]);
    }

    /**
     * 
     * @return
     * 
     */
    private static SimpleDateFormat initDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(FORMAT_STRING);
        return format;
    }

}
