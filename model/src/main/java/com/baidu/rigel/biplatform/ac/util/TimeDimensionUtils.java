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


/**
 * 时间维度工具类
 * 
 * @author xiaoming.chen
 *
 */
public class TimeDimensionUtils {

    /**
     * LOG
     */
//    private static Logger LOG = LoggerFactory.getLogger(TimeDimensionUtils.class);
//
//    /**
//     * DEFAULT_SIMPLE_DATEFORMAT yyyy-MM-dd
//     */
//    public static SimpleDateFormat DEFAULT_SIMPLE_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据时间和对应的格式
     * 
     * @param timeStr 时间字符串
     * @param timeType 转换成的时间类型
     * @param timeFormat 时间字符串对应的格式，如果为空，用时间类型对应的格式
     * @return timeMember 生成的时间member
     * @throws ParseException 日期字符串parse成日期对象失败
     */
//    public static MiniCubeMember createTimeMember(String timeStr, TimeType timeType, String timeFormat)
//            throws ParseException {
//        if (StringUtils.isBlank(timeStr) && timeType == null) {
//            throw new IllegalArgumentException("timeStr is blank or timeType is null, timeStr:" + timeStr
//                    + " timeType:" + timeType);
//        }
//
//        Calendar calendar = null;
//        String dateFormat = StringUtils.isBlank(timeFormat) ? timeType.getFormat() : timeFormat;
//        if (dateFormat.toUpperCase().contains("QN")) {
//            int year = Integer.parseInt(timeStr.substring(0, 4));
//            int quarter = Integer.parseInt(timeStr.substring(5));
//            // 在Calendar中，月份比实际的数字小1
//            int month = (quarter - 1) * 3;
//            calendar = new GregorianCalendar(year, month, 1);
//        } else {
//            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//            Date date = sdf.parse(timeStr);
//            calendar = Calendar.getInstance();
//            calendar.setTime(date);
//        }
//
//        int quarter = calendar.get(Calendar.MONTH) / 3 + 1;
//        String caption = null;
//        MiniCubeLevel level = new MiniCubeLevel("level_" + timeType);
//
//        if (timeType.equals(TimeType.TimeYear)) {
//            caption = calendar.get(Calendar.YEAR) + "";
//            level.setType(LevelType.TIME_YEARS);
//        } else if (timeType.equals(TimeType.TimeQuarter)) {
//            caption = calendar.get(Calendar.YEAR) + "_Q" + quarter;
//            level.setType(LevelType.TIME_QUARTERS);
//        } else if (timeType.equals(TimeType.TimeMonth)) {
//            caption = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1);
//            level.setType(LevelType.TIME_MONTHS);
//        } else if (timeType.equals(TimeType.TimeWeekly)) {
//            caption = calendar.get(Calendar.YEAR) + "_W" + calendar.get(Calendar.WEEK_OF_YEAR);
//            level.setType(LevelType.TIME_WEEKS);
//        } else if (timeType.equals(TimeType.TimeDay)) {
//            caption = DEFAULT_SIMPLE_DATEFORMAT.format(calendar.getTime());
//            level.setType(LevelType.TIME_DAYS);
//        }
//
//        MiniCubeMember member = new MiniCubeMember(timeStr);
//        member.setCaption(caption);
//        member.setVisible(true);
//        member.setLevel(level);
//        LOG.info("time member:" + member);
//        return member;
//    }

    /**
     * 将时间对象转成指定的时间Member
     * 
     * @param calendar 时间
     * @param timeType 转换成的时间Member的类型
     * @param timeFormat 时间格式
     * @return 时间Member
     */
//    public static MiniCubeMember createTimeMember(Calendar calendar, TimeType timeType, String timeFormat) {
//        // 如果没有设置时间格式，那么用时间类型默认格式
//        String resultTimeFormat = StringUtils.isBlank(timeFormat) ? timeType.getFormat() : timeFormat;
//        int quarter = calendar.get(Calendar.MONTH) / 3 + 1;
//        String name = "";
//        String caption = "";
//        if (resultTimeFormat.toUpperCase().endsWith("QN")) {
//            name = timeFormat.toUpperCase().replace(TimeType.TimeYear.getFormat(), calendar.get(Calendar.YEAR) + "");
//            name = name.toUpperCase().replace("N", quarter + "");
//        } else if (resultTimeFormat.toUpperCase().endsWith("WN")) {
//            name = timeFormat.toUpperCase().replace(TimeType.TimeYear.getFormat(), calendar.get(Calendar.YEAR) + "");
//            name = name.toUpperCase().replace("N", calendar.get(Calendar.WEEK_OF_YEAR) + "");
//        } else {
//            SimpleDateFormat sdf = new SimpleDateFormat(resultTimeFormat);
//            name = sdf.format(calendar.getTime());
//        }
//        MiniCubeLevel level = new MiniCubeLevel("level_" + timeType);
//
//        if (timeType.equals(TimeType.TimeYear)) {
//            caption = calendar.get(Calendar.YEAR) + "";
//            level.setType(LevelType.TIME_YEARS);
//        } else if (timeType.equals(TimeType.TimeQuarter)) {
//            caption = calendar.get(Calendar.YEAR) + "_Q" + quarter;
//            level.setType(LevelType.TIME_QUARTERS);
//        } else if (timeType.equals(TimeType.TimeMonth)) {
//            caption = calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1);
//            level.setType(LevelType.TIME_MONTHS);
//        } else if (timeType.equals(TimeType.TimeWeekly)) {
//            caption = calendar.get(Calendar.YEAR) + "_W" + calendar.get(Calendar.WEEK_OF_YEAR);
//            level.setType(LevelType.TIME_WEEKS);
//        } else if (timeType.equals(TimeType.TimeDay)) {
//            caption = DEFAULT_SIMPLE_DATEFORMAT.format(calendar.getTime());
//            level.setType(LevelType.TIME_DAYS);
//        }
//
//        MiniCubeMember member = new MiniCubeMember(name);
//        member.setCaption(caption);
//        member.setVisible(true);
//        member.setLevel(level);
//        return member;
//    }

    // /**
    // * 将时间的UniqueName转换成的数组转换成timeMember
    // *
    // * @param cube 查询维度所在的cube
    // * @param names 时间uniqueName解析后的name数组
    // * @return 根据时间UniqueName转换的时间member
    // * @throws MiniCubeQueryException 时间维度转换失败
    // */
    // public static MiniCubeMember processTimeMember(Cube cube, String[] names) throws MiniCubeQueryException {
    // // TODO cube中可能有多个时间维度，这个方法需要重新实现
    // Dimension targetDim = (TimeDimension) cube.getDimensions().get(TimeDimension.DEFAULT_TIME_DIMENSION_NAME);
    // if (targetDim == null) {
    // throw new MiniCubeQueryException("can not found default time dimension 'Time' in cube:" + cube);
    // }
    // MiniCubeMember result = null;
    // TimeDimension timeDim = (TimeDimension) targetDim;
    // if (names[0].equals(TimeDimension.DEFAULT_TIME_YEAR_DIMENSION_NAME)) {
    // int length = names.length;
    // TimeType timeType = null;
    // String name = names[names.length - 1];
    // switch (length) {
    // case 2:
    // timeType = TimeType.TimeYear;
    // break;
    // case 3:
    // timeType = TimeType.TimeQuarter;
    // break;
    // case 4:
    // timeType = TimeType.TimeMonth;
    // break;
    // default:
    // timeType = TimeType.TimeDay;
    // break;
    // }
    // try {
    // result = TimeDimensionUtils.createTimeMember(name, timeType, timeDim.getTimeFormat());
    // } catch (ParseException e) {
    // LOG.warn("parse time error,expect format:" + timeDim.getTimeFormat() + " actual date:" + name, e);
    // throw new IllegalArgumentException("parse time error,expect format:" + timeDim.getTimeFormat()
    // + " actual date:" + name, e);
    // }
    // return result;
    //
    // } else if (names[0].equals(TimeDimension.DEFAULT_TIME_WEEK_DIMENSION_NAME)) {
    // int length = names.length;
    // TimeType timeType = null;
    // String name = names[names.length - 1];
    // switch (length) {
    // case 2:
    // timeType = TimeType.TimeWeekly;
    // break;
    // case 3:
    // timeType = TimeType.TimeDay;
    // break;
    // default:
    // timeType = TimeType.TimeDay;
    // break;
    // }
    // try {
    // result = TimeDimensionUtils.createTimeMember(name, timeType, timeDim.getTimeFormat());
    // } catch (ParseException e) {
    // LOG.warn("parse time error,expect format:" + timeDim.getTimeFormat() + " actual date:" + name, e);
    // throw new IllegalArgumentException("parse time error,expect format:" + timeDim.getTimeFormat()
    // + " actual date:" + name, e);
    // }
    // return result;
    // } else {
    // throw new MiniCubeQueryException("can not found default time dimension 'Time' in cube:" + cube);
    // }
    //
    // }

}
