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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.report.query.pivottable.CellData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 图数据输入的工厂类
 * 
 * @author zhongyi
 *
 */
public final class ChartDataInputFactory {
    
     /**
      * ChartDataInputFactory
      */
    private ChartDataInputFactory() {
        
    }
    /**
     * 生成时间趋势线图的图输入对象
     * 
     * @param cellData
     *            数据
     * @param columnNames
     *            列名称
     * @param xNodes
     *            横轴节点
     * @param type
     *            分析类型
     * @return 图输入元数据
     */
    public static ChartDataInput generateTimeTrendChartInput(List<List<CellData>> cellData,
        List<String> columnNames, List<String> xNodes, AnalysisType type, XAxisType xType) {
        
        ChartDataInput input = new ChartDataInput();
        
        List<List<BigDecimal>> pureData = Lists.newArrayList();
        Map<String, DataFormat> formats = Maps.newHashMap();
        int columnSize = cellData.size();
        for (int index = 0; index < columnSize; index++) {
            List<CellData> column = cellData.get(index);
            List<BigDecimal> columnData = Lists.newArrayList();
            String formatExp = null;
            for (CellData unit : column) {
                if (formatExp == null) {
                    formatExp = unit.getFormattedValue();
                }
                columnData.add(unit.getV());
            }
            String columnKey = columnNames.get(index);
            DataFormat format = DataFormat.parseByFormatExp(formatExp);
            formats.put(columnKey, format);
            pureData.add(columnData);
        }
        input.setData(pureData);
        input.setFormats(formats);
        input.setFormat("I,III.DD");
        input.setTitle("时间趋势分析");
        input.setType(type);
        input.setxAxis(xNodes);
        input.setyAxis(columnNames);
        input.setyAxisInfos(getDefaultYAxis(columnNames));
        input.setxAxisType(xType == null ? XAxisType.CATEGORY : xType);
        
        return input;
    }
    
    /**
     * 生成默认的Y轴信息
     * 
     * @param columnNames
     *            列的名称列表
     * @return Y轴对应关心
     */
    protected static Map<String, YAxis> getDefaultYAxis(List<String> columnNames) {
        Map<String, YAxis> yAxisInfos = Maps.newHashMap();
        YAxis yAxis = new YAxis();
        yAxis.setName("left");
        for (String columnName : columnNames) {
            yAxisInfos.put(columnName, yAxis);
        }
        return yAxisInfos;
    }
    
}