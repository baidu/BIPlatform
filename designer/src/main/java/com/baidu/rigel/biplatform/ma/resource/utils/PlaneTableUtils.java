package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.ma.comm.util.ParamValidateUtils;
import com.google.common.collect.Maps;

/**
 * 平面表工具类
 * 
 * @author yichao.jiang
 *
 */
public class PlaneTableUtils {

    /**
     * 日志记录对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(PlaneTableUtils.class);

    /**
     * 时间粒度参数
     */
    private static final String GRANULARITY = "granularity";

    /**
     * 年正则表达式，匹配2015
     */
    private static final String YEAR_REGEXP = "^[0-9]{4}$";

    /**
     * 季度正则表达式，匹配201501、201504
     */
    private static final String QUARTER_REGEXP = "^[0-9]{4}0[1|4|7]|^[0-9]{4}1[0]$";

    /**
     * 月正则表达式，匹配201501、201505
     */
    private static final String MONTH_REGEXP = "^[0-9]{4}0[0-9]|1[0-2]$";

    /**
     * 周正则表达式，匹配20150626
     */
    private static final String WEEK_REGEXP = "^[0-9]{4}(((0[13578]|(10|12))(0[1-9]|[1-2][0-9]|3[0-1]))|"
            + "(02(0[1-9]|[1-2][0-9]))|((0[469]|11)(0[1-9]|[1-2][0-9]|30)))$";

    /**
     * 日正则表达式，匹配20150626
     */
    private static final String DAY_REGEXP = "^[0-9]{4}(((0[13578]|(10|12))(0[1-9]|[1-2][0-9]|3[0-1]))|"
            + "(02(0[1-9]|[1-2][0-9]))|((0[469]|11)(0[1-9]|[1-2][0-9]|30)))$";

    /**
     * 校验设置的平面表条件值是否合理
     * 
     * @param sqlStr
     * @param value
     * @return
     */
    public static boolean checkSQLCondition(String sqlStr, String value) {
        if (!ParamValidateUtils.check("sqlStr", sqlStr)) {
            return false;
        }
        if (!ParamValidateUtils.check("value", value)) {
            return false;
        }
        SQLConditionType sqlType = SQLConditionType.valueOf(sqlStr);
        String[] tmpValue = value.split(",");
        if (tmpValue == null || tmpValue.length == 0) {
            return false;
        }
        switch (sqlType) {
            case EQ:
            case NOT_EQ:
            case LT:
            case GT:
            case LT_EQ:
            case GT_EQ:
            case LIKE:
                return tmpValue.length == 1;
            case BETWEEN_AND:
                return tmpValue.length == 2;
            case IN:
                return true;
            default:
                return true;
        }
    }

    /**
     * 处理时间条件 handelTimeCondition
     * 
     * @param cube
     * @param requestParams
     * @return
     */
    public static Map<String, Object> handelTimeCondition(Cube cube, Map<String, Object> requestParams) {
        if (!ParamValidateUtils.check("cube", cube)) {
            return Maps.newHashMap();
        }

        if (!ParamValidateUtils.check("requestParams", requestParams)) {
            return Maps.newHashMap();
        }
        Map<String, Object> params = Maps.newHashMap();
        requestParams.forEach((key, value) -> {
            if (value instanceof String) {
                if (isTimeJson((String) value)) {
                    // 对时间特殊处理
                String granularity = getTimeGranularity((String) value);
                String id = getElementIdFromCube(cube, granularity);
                if (id != null) {
                    params.put(id, value);
                } else {
                    params.put(key, value);
                }
            } else {
                params.put(key, value);
            }
        } else {
            params.put(key, value);
        }
    })  ;
        return params;
    }

    /**
     * 判断输入的字符串是否为满足要求的时间字符串 isTimeJson
     * 
     * @param json
     * @return
     */
    public static boolean isTimeJson(String json) {
        return json.contains("start") && json.contains("end") && json.contains("granularity");
    }

    /**
     * 判断该id对应的维度是否为时间维度 isTimeDim
     * 
     * @param cube
     * @param elementId
     * @return
     */
    public static boolean isTimeDim(Cube cube, String elementId) {
        if (!ParamValidateUtils.check("cube", cube)) {
            return false;
        }
        if (!ParamValidateUtils.check("elementId", elementId)) {
            return false;
        }

        // 获取维度信息
        Map<String, Dimension> dimensions = cube.getDimensions();
        if (dimensions == null || dimensions.size() == 0) {
            return false;
        }
        Dimension dim = dimensions.get(elementId);
        if (dim == null) {
            return false;
        }
        return dim.isTimeDimension();
    }

    /**
     * 将普通数值转为规范的时间JSON字符串 convert2TimeJson
     * 
     * @param value
     * @param requestParams
     * @return
     */
    public static String convert2TimeJson(String value, Map<String, Object> requestParams, TimeDimension timeDim) {
        
        if (!ParamValidateUtils.check("value", value)) {
            return null;
        }
        if (!ParamValidateUtils.check("requestParams", requestParams)) {
            return null;
        }
        if (!requestParams.containsKey(GRANULARITY)) {
            if (timeDim == null) {
                return null;
            }
            requestParams.put(GRANULARITY, timeDim.getDataTimeType().getGranularity());
        }
        // 获取时间粒度参数
        String granularity = (String) requestParams.get(GRANULARITY);
        return cov2StandTime(value, granularity);
    }

    /**
     * 判断某个粒度下的时间字符串是否满足需求 isStandardTime
     * 
     * @param time
     * @return
     */
    private static boolean isStandardTime(String time, String granularity) {
        switch (granularity) {
            case "Y":
                return matchRegexp(time, YEAR_REGEXP);
            case "Q":
                return matchRegexp(time, QUARTER_REGEXP);
            case "M":
                return matchRegexp(time, MONTH_REGEXP);
            case "W":
                return matchRegexp(time, WEEK_REGEXP);
            case "D":
                return matchRegexp(time, DAY_REGEXP);
            default:
                throw new RuntimeException("the time granularity : " + granularity + "is wrong");
        }
    }

    /**
     * 测试时间是否符合正则表达式 matchRegexp
     * 
     * @param time
     * @param regex
     * @return
     */
    private static boolean matchRegexp(String time, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(time);
        return matcher.matches();
    }

    /**
     * 
     * cov2StandTime
     * 
     * @param time
     * @param granularity
     * @return
     * @throws JSONException
     */
    private static String cov2StandTime(String timeValue, String granularity) {
        // TODO 目前仅支持单选
        String timeStr = "{'start':'%s','end':'%s','granularity':'%s'}";
        Map<String, String> time = null;
        switch (granularity) {
        // 年
            case "Y":
                time = TimeUtils.getTimeCondition(timeValue, timeValue, TimeType.TimeYear);
                break;
            // 季度
            case "Q":
                String year = timeValue.substring(0, 4);
                String month = timeValue.substring(4, 6);
                int monthInt = Integer.valueOf(month);
                String quarter = year + "Q" + (monthInt/3 + 1); 
                time = TimeUtils.getTimeCondition(quarter, quarter, TimeType.TimeQuarter);
                break;
            // 月份
            case "M":
                time = TimeUtils.getTimeCondition(timeValue, timeValue, TimeType.TimeMonth);
                break;
            // 星期
            case "W":
                time = TimeUtils.getTimeCondition(timeValue, timeValue, TimeType.TimeWeekly);
                break;
            // 天
            case "D":
                time = TimeUtils.getTimeCondition(timeValue, timeValue, TimeType.TimeDay);
                break;
            default:
                break;
        }
        String start = time.get("start");
        String end = time.get("end");
        return String.format(timeStr, start, end, granularity);
    }

    /**
     * 获取时间字符串中的时间粒度信息 getTimeGranularity
     * 
     * @param timeJson
     * @return
     */
    private static String getTimeGranularity(String timeJson) {
        String granularity = "";
        try {
            JSONObject json = new JSONObject(timeJson);
            granularity = json.getString("granularity");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return granularity;
    }

    /**
     * 获取cube中对应时间粒度下的element的id信息 getElementIdFromCube
     * 
     * @param cube
     * @param granularity
     * @return
     */
    private static String getElementIdFromCube(Cube cube, String granularity) {
        Map<String, Dimension> dimensions = cube.getDimensions();
        if (dimensions.size() == 0) {
            return null;
        }
        for (String key : dimensions.keySet()) {
            Dimension dimension = dimensions.get(key);
            // 如果是时间维度
            if (dimension.isTimeDimension()) {
                TimeType timeType = getTimeType4Granularity(granularity);
                TimeDimension timeDim = (TimeDimension) dimension;
                if (timeDim.getDataTimeType() == timeType) {
                    return timeDim.getId();
                }
            }
        }
        return null;
    }

    /**
     * 获取对应日期粒度下的日期类型 getTimeFormat4Granuarity
     * 
     * @return
     */
    private static TimeType getTimeType4Granularity(String granularity) {
        switch (granularity) {
            case "Y":
                return TimeType.TimeYear;
            case "Q":
                return TimeType.TimeQuarter;
            case "M":
                return TimeType.TimeMonth;
            case "W":
                return TimeType.TimeWeekly;
            case "D":
                return TimeType.TimeDay;
            default:
                throw new UnsupportedOperationException("暂不支持此时间类型");
        }
    }

    /**
     * 平面表中对时间条件的特殊处理
     * 
     * @param timeJson
     * @return 处理后的，起始日期和截止日期之间的所有天
     */
    public static String handleTimeCondition(String timeJson) {
        if (!ParamValidateUtils.check("timeJson", timeJson)) {
            return timeJson;
        }
        if (timeJson.contains("start") && timeJson.contains("end") && timeJson.contains("granularity")) {
            // 对时间JSON串进行处理
            return getTimeCondition(timeJson);
        }
        return timeJson;
    }

    /**
     * 对时间JSON串进行处理，获取起始日期和截止日期之间的所有天
     * 
     * @param timeValue
     * @return 起始日期和截止日期之间的所有天
     */
    private static String getTimeCondition(String timeValue) {
        String start;
        String end;
        String result = null;
        try {
            JSONObject json = new JSONObject(timeValue);
            start = json.getString("start").replace("-", "");
            end = json.getString("end").replace("-", "");
            String granularity = json.getString("granularity");
            // 保证开始时间小于结束时间
            if (start.compareTo(end) > 0) {
                String tmp = start;
                start = end;
                end = tmp;
            }
            Map<String, String> time = null;
            switch (granularity) {
            // 年
                case "Y":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeYear);
                    break;
                // 季度
                case "Q":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeQuarter);
                    break;
                // 月份
                case "M":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeMonth);
                    break;
                // 星期
                case "W":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeWeekly);
                    break;
                // 天
                case "D":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeDay);
                    break;
                default:
                    break;
            }
            start = time.get("start");
            end = time.get("end");
            LOG.info("the planeTable time condition :start time is [" + start + "], end time is [" + end + "], and "
                    + "the granularity is " + granularity);
            // 获取具体的日期天
            result = getDetailTimeCondition(start, end);
        } catch (Exception e) {
            LOG.debug("the input time format is wrong" + timeValue, e);
        }
        return result;
    }

    /**
     * 获取起始日期和截止日期之间的所有天，之间用逗号分隔
     * 
     * @param startTime 起始日期
     * @param endTime 截止日期
     * @return
     */
    private static String getDetailTimeCondition(String startTime, String endTime) {
        TimeRangeDetail range = new TimeRangeDetail(startTime, endTime);
        StringBuilder message = new StringBuilder();
        String[] days = range.getDays();
        for (int i = 0; i < days.length - 1; i++) {
            message.append(days[i] + ",");
        }
        message.append(days[days.length - 1]);
        return message.toString();
    }
}
