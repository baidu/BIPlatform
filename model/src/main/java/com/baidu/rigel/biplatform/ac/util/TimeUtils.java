package com.baidu.rigel.biplatform.ac.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.google.common.collect.Maps;

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
    
    private static final Map<String, TimeType> TIME_TYPE_MAP = Maps.newHashMap();
    
    static {
        TIME_TYPE_MAP.put("M", TimeType.TimeMonth);
        TIME_TYPE_MAP.put("D", TimeType.TimeDay);
        TIME_TYPE_MAP.put("Y", TimeType.TimeYear);
        TIME_TYPE_MAP.put("Q", TimeType.TimeQuarter);
        TIME_TYPE_MAP.put("W", TimeType.TimeWeekly);
    }
    
    /**
     * 跟据传入的粒度获取对应的时间类型
     * @param gSymbol
     * @return TimeType
     */
    public static TimeType getTimeTypeWithGranularitySymbol(String gSymbol) {
        if (!StringUtils.isEmpty(gSymbol)) {
            return TIME_TYPE_MAP.get(gSymbol);
        }
        return null;
    }

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
     * 获取指定时间所在季度的时间范围
     * @param day
     * @return
     */
    public static TimeRangeDetail getQuarterDays(Date day) {
        return getQuarterDays(day, 0, 0);
    }
    
    /**
     * 获取指定时间前几季度、后几季度的时间范围
     * @param day
     * @param before 前几季度
     * @param after 后几季度
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
        calendar.setTime(day);        
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
    
    /**
     * 
     * @param start  前端传入的起始日期
     * @param end    前端传入的结束日期
     * @param timeType 时间维度类型
     * @return 返回具体以日为粒度的时间格式 20141107
     */
    public static Map<String, String> getTimeCondition(String start, String end, TimeType timeType) {
        return singleTimeContidion(start, end, timeType);
    }
    
    /**
     * 单一时间条件
     * @param start  前端传入的起始日期
     * @param end    前端传入的结束日期
     * @param timeType 时间维度类型
     * @return 返回具体以日为粒度的时间格式 20141107
     */
    protected static Map<String, String> singleTimeContidion(String start,
            String end, TimeType timeType) {
        Map<String, String> result = new HashMap<String, String>();
        String startYear = start.substring(0, 4); // 起始年份
        String endYear = end.substring(0, 4); // 截止年份
        Calendar cal = Calendar.getInstance();
        switch (timeType) {
            case TimeYear:
                start = startYear + "0101"; // 起始年份的第一天
                end = endYear + "1231"; // 截止年份的最后一天
                break;
            case TimeHalfYear:
                break;
            case TimeQuarter:
                // 处理YYYYQ1,YYYYQ2,YYYYQ3,YYYYQ4场景
                // 将这类格式数据转换成具体的日期
                String startQuarter = start.substring(5);
                String startDate = QUARTER_BEGIN[Integer.valueOf(startQuarter) - 1];
                start = startYear + startDate;
                String endQuarter = end.substring(5);
                  // 取end的那个季的最后一天
                String endDate = QUARTER_END[Integer.valueOf(endQuarter) - 1];
                end = endYear + endDate;
                break;
            case TimeMonth:
                // 处理YYYYMM场景
                cal.clear();
                String startMonth = start.substring(4);
                start = startYear + startMonth + "01";
                String endMonth = end.substring(4);
                cal.set(Calendar.YEAR, Integer.valueOf(endYear));
                cal.set(Calendar.MONTH, Integer.valueOf(endMonth) - 1); 
                // 取end的那个月的最后一天
                end = endYear + endMonth
                                 + cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
            case TimeWeekly:
                // 处理YYYYMMDD场景
                cal.clear();
                cal.set(Calendar.YEAR, Integer.valueOf(startYear));
                cal.set(Calendar.MONTH, Integer.valueOf(start.substring(4, 6)) - 1);
                // end 为什么+6天？
                cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(start.substring(6)));
                cal.add(Calendar.DAY_OF_MONTH, 6);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                String day = "" + dayOfMonth;
                if (dayOfMonth < 10) {
                    day = "0" + day;
                }
                int month = cal.get(Calendar.MONTH) + 1;
                if (month < 10) {
                    end = cal.get(Calendar.YEAR) + "0" + month + day;
                } else {
                    end = cal.get(Calendar.YEAR) + "" + month + day;
                }
                break;
            case TimeDay:
                break;
            case TimeHour:
                break;
            case TimeMinute:
                break;
            case TimeSecond:
                break;
            default:
                break;
        }
        end = checkEndDateAfterNow(end);
        result.put("start", start);
        result.put("end", end);
        return result;
    }
    
    /**
     * 检查截止日期是否已经超过当前日期
     * @param end 截止日期
     * @return
     */
    protected static String checkEndDateAfterNow(String end) {
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.YEAR, Integer.valueOf(end.substring(0,4)));
        calEnd.set(Calendar.MONTH, Integer.valueOf(end.substring(4,6))-1);
        calEnd.set(Calendar.DAY_OF_MONTH, Integer.valueOf(end.substring(6)));
        
        Calendar calNow = Calendar.getInstance();
        if (calNow.before(calEnd)) {
            int dayOfMonth = calNow.get(Calendar.DAY_OF_MONTH);
            String day = "" + dayOfMonth;
            if (dayOfMonth < 10) {
                day = "0" + day;
            }
            int month = calNow.get(Calendar.MONTH) + 1;
            if (month < 10 ) {
                end = "" + calNow.get(Calendar.YEAR) + "0" + month + day;             
            } else {
                end = "" + calNow.get(Calendar.YEAR) + month + day; 
            }
        }
        return end;
    }
}
