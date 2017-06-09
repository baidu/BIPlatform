

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

package com.baidu.rigel.biplatform.ma.divide.table.service;

import java.util.Map;

/** 
 * 按时间分表工具类
 * @author yichao.jiang 
 * @version  2015年6月17日 
 * @since jdk 1.8 or after
 */
public class TimeDivideTableUtils {

    /**
     * 判断当前上下文中是否含有时间
     * getTimeJsonFromContext
     * @param context
     * @return
     */
    public static String getTimeJsonFromContext(Map<String, Object> context) {
        if (context == null) {
            return null;
        }
        
        for (String key : context.keySet()) {
            Object value = context.get(key);
            if (value instanceof String) {
                String valueStr = (String) value;
                if (valueStr.contains("start") && valueStr.contains("end") && valueStr.contains("granularity")) {
                    return valueStr;
                }
            }
        }
        return null;
    }
    
//    /**
//     * 获取时间条件下的所有天、月、年对应的字符串
//     * getAllDetailDays
//     * @param tablePrefx 事实表前缀
//     * @param timeJson
//     * @param divideTableGranularity 分表粒度
//     * @return
//     */
//    public static String getAllFactTableNames(String tablePrefix, String timeJson, String divideTableStrategy) {
//        // 参数校验
//        if (!ParamValidateUtils.check("timeJson", timeJson)) {
//            return null;
//        }
//        // 处理时间参数，获取对应的起始时间和截止时间
//        return getTimeRange(tablePrefix, timeJson, divideTableStrategy);
//    }
//    
//    /**
//     * 获取对应时间粒度下的起始时间
//     * getNewTimeRange
//     * @param timeJson
//     * @return
//     */
//    private static String getTimeRange(String tablePrefix, String timeJson, String divideTableStrategy) {
//        String start;
//        String end;
//        String result = null;
//        try {
//            JSONObject json = new JSONObject(timeJson);
//            start = json.getString("start").replace("-", "");
//            end = json.getString("end").replace("-", "");
//            // 获取起始日期，对应时间分表策略粒度下的具体日期
//            result = getAllTimeTables(start, end, tablePrefix, divideTableStrategy);
//        } catch (Exception e) {
//            throw new RuntimeException("exception happend when get detail time!");
//        }
//        return result;
//    }
//    
//    /**
//     * 获取起始日期，对应时间分表策略粒度下的具体日期
//     * getAllTime
//     * @param start
//     * @param end
//     * @param tablePrefix 
//     * @param divideTableStrategy
//     * @return
//     */
//    private static String getAllTimeTables(String start, String end, String tablePrefix, String divideTableStrategy) {
//        TimeRangeDetail range = new TimeRangeDetail(start, end);
//        String[] days = range.getDays();
//        Set<String> tableNames = Sets.newTreeSet();
//        StringBuilder stringBuilder = new StringBuilder();
//        switch (divideTableStrategy) {
//            // 按年分表
//            case "yyyy":
//                for (int i = 0; i < days.length; i++) {
//                    tableNames.add(tablePrefix + days[i].substring(0, 4));
//                } 
//                break;
//            // 按月分表
//            case "yyyyMM":
//                for (int i = 0; i < days.length; i++) {
//                    tableNames.add(tablePrefix + days[i].substring(0, 6));
//                }
//                break;
//            // 按日分表
//            case "yyyyMMdd":
//                for (int i = 0; i < days.length; i++) {
//                    tableNames.add(tablePrefix + days[i]);
//                }
//                break;
//            default:
//                throw new RuntimeException("the current time strategy is " + divideTableStrategy + ", and now we don't support!");
//        }
//        // 将所有表名遍历，以逗号分隔
//        tableNames.forEach(value -> {
//            stringBuilder.append(value + ",");
//        });
//        // 替换最后一个逗号
//        stringBuilder.replace(stringBuilder.length()-1, stringBuilder.length(), "");
//        return stringBuilder.toString();
//    }
}

