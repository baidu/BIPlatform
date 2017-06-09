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
package com.baidu.rigel.biplatform.tesseract.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;

/**
 * 
 * 时间计算工具类
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class TimeUtils {
    
    /**
     * 第一季度开始时间
     */
    private static final String[] QUARTER_BEGIN = {"0101", "0401", "0701", "1001"};
    
    /**
     * 第一季度终止时间
     */
    private static final String[] QUARTER_END = {"0331", "0630", "0930", "1231"};
    
    
    /**
     * 获取before ＋ after ＋ 1 天的时间范围
     * @param before 之前多少天 ，如果表示当前时间，before为0
     * @param after 之后多少天，如果表示当前时间，after为0
     * @return ［before，after］表示的TimeRanage
     */
    public static TimeRangeDetail getDays(int before, int after) {
        Date currentDay = getCurrentDay();
        return getDays(currentDay, before, after);
    }
    
    /**
     * 获取指定时间之前，之后指定的时间的范围
     * @param day 指定时间
     * @param before 之前多少天 ，如果表示当前时间，before为0
     * @param after 之后多少天，如果表示当前时间，after为0
     * @return TimeRange
     */
    public static TimeRangeDetail getDays(Date day, int before, int after) {
        if (day == null) {
            day = getCurrentDay();
        }
        
        if (before < 0) {
            before = 0;
        }
        
        if (after < 0) {
            after = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        // 获取开始时间
        calendar.add(Calendar.DAY_OF_YEAR, 0 - before);
        Date begin = calendar.getTime();
        // 获取结束时间
        calendar.add(Calendar.DAY_OF_YEAR, 0 + before + after);
        Date end = calendar.getTime();
        return new TimeRangeDetail(TimeRangeDetail.toTime(begin),
                TimeRangeDetail.toTime(end));
    }

    /**
     * 获取指定时间weekday的范围,一周指从周一开始
     * @param day 指定时间
     * @return 当前天所在周的范围
     */
    public static TimeRangeDetail getWeekDays(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        return getWeekDays(day, 0, 0);
    }
    
    /**
     * 获取指定时间前几周以及后几周时间的闭区间时间区域。如果before小于0 before 为0 
     * 如果after小于0 after为0，时间范围的开始时间为目标时间的周一，end为目标时间周的周日
     * @param day 指定时间
     * @param before 前几周
     * @param after 后几周
     * @return 
     */
    public static TimeRangeDetail getWeekDays(Date day, int before, int after) {
        if (day == null) {
            day = getCurrentDay();
        }
        
        if (before < 0) {
            before = 0;
        }
        
        if (after < 0) {
            after = 0;
        }
        
        Calendar calendar = Calendar.getInstance();
        
        // 获取一周的第一天 向前找整周天 找到begin的范围
        calendar.setTime(getMondayOfThisWeek(day));
        calendar.add(Calendar.DAY_OF_YEAR, 0 - 7 * before);
        Date start = calendar.getTime();
        
        // 获取一周的最后一天 向后找整周天 找到end范围
        calendar.setTime(getSundayOfThisWeek(day));
        calendar.add(Calendar.DAY_OF_YEAR, 7 * after);
        Date end = calendar.getTime();
        
        return new TimeRangeDetail(TimeRangeDetail.toTime(start), TimeRangeDetail.toTime(end));
    }
    
    /**
     * 获取指定时间所在月份的的时间范围
     * @param day 
     * @return
     */
    public static TimeRangeDetail getMonthDays(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        String firstDay = getFirstDayOfMonth(day);
        String lastDay = getLastDayOfMonth(day);
        return new TimeRangeDetail(firstDay, lastDay);
    }
    
    /**
     * 获取指定时间前几月、后几月时间范围
     * @param day 指定时间
     * @param before 前几月
     * @param after 后几月
     * @return
     */
    public static TimeRangeDetail getMonthDays(Date day, int before, int after) {
        if (day == null) {
            day = getCurrentDay();
        }
        
        if (before < 0) {
            before = 0;
        }
        
        if (after < 0) {
            after = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.add(Calendar.MONTH, 0 - before);
        String begin = getFirstDayOfMonth(calendar.getTime());
        calendar.add(Calendar.MONTH, before + after);
        String end = getLastDayOfMonth(calendar.getTime());
        return new TimeRangeDetail(begin, end);
    }
    
    /**
     * 获取指定时间所在年的时间范围
     * @param day
     * @return
     */
    public static TimeRangeDetail getYearDays(Date day) {
        return getYearDays(day, 0, 0);
    }
    
    /**
     * 获取指定时间前几年、后几年的时间范围
     * @param day
     * @param before 前几年
     * @param after 后几年
     * @return
     */
    public static TimeRangeDetail getYearDays(Date day, int before, int after) {
        if (day == null) {
            day = getCurrentDay();
        }
        
        if (before < 0) {
            before = 0;
        }
        
        if (after < 0) {
            after = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.add(Calendar.YEAR, 0 - before);
        String begin = getFirstDayOfYear(calendar.getTime());
        calendar.add(Calendar.YEAR, before + after);
        String end = getLastDayOfYear(calendar.getTime());
        return new TimeRangeDetail(begin, end);
    }

    /**
     * 获取指定时间所在年的时间范围
     * @param day
     * @return
     */
    public static TimeRangeDetail getQuarterDays(Date day) {
        return getQuarterDays(day, 0, 0);
    }
    
    /**
     * 获取指定时间前几年、后几年的时间范围
     * @param day
     * @param before 前几年
     * @param after 后几年
     * @return
     */
    public static TimeRangeDetail getQuarterDays(Date day, int before, int after) {
        if (day == null) {
            day = getCurrentDay();
        }
        if (before < 0) {
            before = 0;
        }
        if (after < 0) {
            after = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.add(Calendar.MONTH, 0 - 3 * before);
        String begin = getFirstDayOfQuarter(calendar.getTime());
        calendar.add(Calendar.MONTH, 3 * (before + after));
        String end = getLastDayOfQuarter(calendar.getTime());
        return new TimeRangeDetail(begin, end);
    }
    
    /**
     * 
     * 获取指定时间季度的最后一天
     * @param day
     * @return 指定时间所在季度最后一天
     * 
     */
    protected static String getLastDayOfQuarter(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        int quarter = getQuarterOfDay(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        int year = calendar.get(Calendar.YEAR);
        return year + QUARTER_END[quarter];
    }
    
    /**
     * 
     * 获取指定时间季度的第一天
     * @param day
     * @return 指定时间所在季度第一天
     * 
     */
    protected static String getFirstDayOfQuarter(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        int quarter = getQuarterOfDay(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        int year = calendar.get(Calendar.YEAR);
        return year + QUARTER_BEGIN[quarter];
    }
    
    /**
     * 获取指定时间所在季度
     * @param day
     * @return
     */
    private static int getQuarterOfDay(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        int month = calendar.get(Calendar.MONTH);
        return month / 3;
    }
    
    /**
     * 获取指定时间所在年的第一天
     * @param day
     * @return
     */
    protected static String getFirstDayOfYear(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        String timeStr = TimeRangeDetail.toTime(day);
        return timeStr.substring(0, 4) + "0101";
    }
    
    /**
     * 获取指定时间所在年的最后一天
     * @param day
     * @return
     */
    protected static String getLastDayOfYear(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        String timeStr = TimeRangeDetail.toTime(day);
        return timeStr.substring(0, 4) + "1231";
    }
    
    /**
     * 获取每个月的第一天
     * @param day
     * @return
     */
    protected static String getFirstDayOfMonth(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        String timeStr = TimeRangeDetail.toTime(day);
        return timeStr.substring(0, 6) + "01";
    }
    
    /**
     * 获取每个月的最后一天
     * @param day
     * @return
     */
    protected static String getLastDayOfMonth(Date day) {
        if (day == null) {
            day = getCurrentDay();
        }
        String timeStr = TimeRangeDetail.toTime(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime (day);
//        calendar.set(Calendar.YEAR, Integer.parseInt(timeStr.substring(0, 4)));
//        calendar.set(Calendar.MONTH, Integer.parseInt(timeStr.substring(4, 6)));
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return timeStr.substring(0, 6) + String.valueOf(maxDay);
    }
    
    /**
     * 
     * 得到指定时间所在周周一
     * @param day
     * @return
     */
    protected static Date getMondayOfThisWeek(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        calendar.add(Calendar.DATE, -dayOfWeek + 1);
        return calendar.getTime();
    }
    
    /**
     * 
     * 得到指定时间所在周周日
     * @param day
     * @return
     */
    protected static Date getSundayOfThisWeek(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        calendar.add(Calendar.DATE, -dayOfWeek + 7);
        return calendar.getTime();
    }
    
    /**
     * 
     * @return 获取当前日期
     * 
     */
    protected static Date getCurrentDay() {
        return new GregorianCalendar().getTime();
    }

    /**
     * 获取当前年
     * @return
     */
    public static int getCurrentYear() {
        Date day = getCurrentDay();
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取指定时间对于所在年来说应该在第几周
     * @param day yyyyMMdd格式时间字符串
     * @return
     */
    public static int getWeekIndex(String day) throws Exception {
        Date current = getCurrentDay();
        if (!StringUtils.isEmpty(day)) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(TimeRangeDetail.FORMAT_STRING);
            current = sdf.parse(day);
        }
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(current);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
    
}
