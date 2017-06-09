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
package com.baidu.rigel.biplatform.ma.report.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;

/**
 * 
 * 图形构建服务
 * @author zhongyi
 *
 *         2014-8-14
 */
public interface ChartBuildService {
    
    /**
     * 
     * @param tableResult
     * @return
     */
    DIReportChart parseToChart(PivotTable tableResult, Map<String, String> chartType, boolean isTimeChart);
    
    /**
     * 针对lite-olap的chart做特殊转换
     * @param tableResult
     * @param chartType
     * @param isTimeChart
     * @param context
     * @return
     */
    DIReportChart parseToLiteOlapChart(PivotTable tableResult, Map<String, String> chartType, boolean isTimeChart, 
            Map<String, Object> context);
}