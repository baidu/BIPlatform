

/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.regular.utils;

import com.baidu.rigel.biplatform.ma.regular.report.ExecuteTaskStrategy;

/** 
 * 固定报表调度模块quartz所需的Cron表达式
 * 初步支持日、周、月、季度、年份
 * @author yichao.jiang 
 * @version  2015年8月9日 
 * @since jdk 1.8 or after
 */
public class RegularReportCronExpressionUtils {
    
    /**
     * 最后一天的标识，主要用于月份和季度
     */
    private static final String LAST_DAY = "lastDay";
    
    /**
     * 产生任务执行的cron表达式
     * @param executeStratey
     * @return
     */
    public static String genCronExpression4RegularReport(ExecuteTaskStrategy executeTaskStrategy) {
        if (executeTaskStrategy == null) {
            return null;
        }
        String cronExpression = "* * * * * ? *";
        // 小时
        String hour = executeTaskStrategy.getHour();
        // 分钟
        String minute = executeTaskStrategy.getMinute();
        // 粒度
        String granularity = executeTaskStrategy.getGranularity();
        // 详细
        String detail = executeTaskStrategy.getDetail();
        switch (granularity) {
            case "D":
                cronExpression = "00 " + minute + " " + hour + " * * ? *";
                break;
            case "W":
                if (detail.startsWith("W")) {
                    detail = detail.substring(1, detail.length());
                }
                cronExpression = "00 " + minute + " " + hour + " * * " + detail + " *";
                break;
            case "M":
                if (!LAST_DAY.equals(detail)) {
                    // 指定每月的运行的天
                    if (detail.startsWith("M")) {
                        detail = detail.substring(1, detail.length());
                    }
                    cronExpression = "00 " + minute + " " + hour + " " + detail + " * ? *";
                } else {
                    // 每月最后一天
                    cronExpression = "00 " + minute + " " + hour + " L * ? *";
                }
                break;
            case "Q":
                if (!LAST_DAY.equals(detail)) {
                    // 每季度第一天
                    if (detail.startsWith("Q")) {
                        detail = detail.substring(1, detail.length());
                    }
                    cronExpression = "00 " + minute + " " + hour + " 1 1,4,7,10 ? *";
                } else {
                    // 每季度最后一天
                    cronExpression = "00 " + minute + " " + hour + " L 1,4,7,10 ? *";
                }
                break;
            case "Y":
                if (!LAST_DAY.equals(detail)) {
                    // 每年第一天
                    if (detail.startsWith("Y")) {
                        detail = detail.substring(1, detail.length());
                    }
                    cronExpression = "00 " + minute + " " + hour + " 1 1 ? *";
                } else {
                    // 每年最后一天
                    cronExpression = "00 " + minute + " " + hour + " 31 12 ? *";
                }
                break;
            default:
                throw new UnsupportedOperationException("the current granularity " + granularity 
                        + " is not supported!" );
        }
        return cronExpression;
    }
}

