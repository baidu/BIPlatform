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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesDataUnit;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesInputInfo;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesInputInfo.SeriesUnitType;
import com.baidu.rigel.biplatform.ma.report.query.chart.XAxisType;
import com.baidu.rigel.biplatform.ma.report.query.chart.YAxis;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.CellData;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColField;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowHeadField;
import com.baidu.rigel.biplatform.ma.report.service.ChartBuildService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 * 透视表转换成报表图形服务实现
 * 
 * @author zhongyi
 * 
 *         2014-8-14
 */
@Service("chartBuildService")
public class ChartBuildServiceImpl implements ChartBuildService {

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.report.service.ChartBuildService#parseToChart
     * (com.baidu.rigel.biplatform.ma.report.query.pivotTable.PivotTable)
     */
    @Override
    public DIReportChart parseToChart(PivotTable tableResult, Map<String, String> chartType, boolean isTimeChart) {
        return this.helpParse2Chart(tableResult, chartType, isTimeChart, null);
    }

    /**
     * 
     * @param reportChart
     * @return List<BigDecimal>
     */
    private List<BigDecimal> getMaxAndMinValue(DIReportChart reportChart) {
        final List<BigDecimal> tmp = Lists.newArrayList();
        // reportChart.getSeriesData().stream().forEach(data -> {
        // if (data != null) {
        // Collections.addAll(tmp, data.getData());
        // }

        // });
        List<BigDecimal> rs = Lists.newArrayList();
        if (CollectionUtils.isEmpty(reportChart.getSeriesData())) {
            return rs;
        }
        if (reportChart.getSeriesData().get(0) == null) {
            return rs;
        }
        Collections.addAll(tmp, reportChart.getSeriesData().get(0).getData());
        BigDecimal[] tmpArray = tmp.stream().filter(num -> {
            return num != null;
        }).sorted().toArray(BigDecimal[]::new);
        // tmp.clear();
        // Collections.addAll(tmp, tmpArray);
        // Collections.sort(tmp);
        rs = Lists.newArrayList();
        if (tmpArray.length >= 2) {
            rs.add(tmpArray[tmpArray.length - 1]);
            rs.add(tmpArray[0]);
        } else if (tmpArray.length == 1) {
            BigDecimal val = tmpArray[0];
            if (val.compareTo(BigDecimal.ZERO) > 0) {
                rs.add(val);
                rs.add(BigDecimal.ZERO);
            } else {
                rs.add(BigDecimal.ZERO);
                rs.add(val);
            }
        }
        return rs;
    }

    /**
     * 
     * @param pTable
     * @return
     */
    private String[] getXAxisCategories(PivotTable pTable, boolean isTimeDimOnXAxis) {
        if (pTable.getRowDefine() == null || pTable.getRowDefine().size() == 0) {
            return new String[0];
        }
        List<String> categories = Lists.newArrayList();
        for (int i = 0; i < pTable.getRowDefine().size(); i++) {
            RowDefine row = pTable.getRowDefine().get(i);
            if (StringUtils.isEmpty(row.getShowXAxis())) {
                continue;
            }
            /**
             * showXAxis will be used as x axis for chart
             */
            if (isTimeDimOnXAxis) {
                String dateStr = row.getShowXAxis();
                SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat sfTarget = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sf.parse(dateStr);
                    dateStr = sfTarget.format(date);
                } catch (ParseException e) {
                    /**
                     * TODO 格式不对忽略怎么样？
                     */

                }
                categories.add(dateStr);
            } else {
                categories.add(row.getShowXAxis());
            }
        }
        return categories.toArray(new String[0]);
    }

    /**
     * 
     * @param seriesInput
     * @param pTable
     * @param isTimeChart
     * @param chartType
     * @return
     */
    private List<SeriesDataUnit> getSeriesUnitsByInputUnit(List<SeriesInputInfo> seriesInput, PivotTable pTable,
            Map<String, String> chartType, boolean isTimeChart) {
        return this.helpGetSeriesUnitsByInputUnit(seriesInput, pTable, chartType, isTimeChart, null);
    }
    
    /**
     * 针对lite-olap的chart产生序列数据
     * @param seriesInput
     * @param pTable
     * @param chartType
     * @param isTimeChart
     * @param context
     * @return
     */
    private List<SeriesDataUnit> getSeriesUnitsByInputUnit(List<SeriesInputInfo> seriesInput, PivotTable pTable,
            Map<String, String> chartType, boolean isTimeChart, Map<String, Object> context) {
        return this.helpGetSeriesUnitsByInputUnit(seriesInput, pTable, chartType, isTimeChart, context);
    }

    /**
     * 协助产生图形的序列数据
     * @param seriesInput
     * @param pTable
     * @param chartType
     * @param isTimeChart
     * @param context
     * @return
     */
    private List<SeriesDataUnit> helpGetSeriesUnitsByInputUnit(List<SeriesInputInfo> seriesInput, PivotTable pTable,
            Map<String, String> chartType, boolean isTimeChart, Map<String, Object> context) {
        List<SeriesDataUnit> units = Lists.newArrayList();
        
        // 获取colDefins
        List<ColDefine> columnDefs = pTable.getColDefine();
        // 寻找需要包含的value信息
        Set<String> includeColDefines = Sets.newHashSet();
        if (context != null && context.size() != 0) {
            // 对参数的value进行存储
            Set<String> paramValues = Sets.newHashSet();
            context.forEach((k, v) ->{
                if (v instanceof String) {
                    paramValues.add((String) v);
                }
            });
            // 获取colFields
            List<List<ColField>> colFields = pTable.getColFields();
            colFields.forEach(listValue1 -> {
                listValue1.forEach(listValue2 -> {
                    String uniqueName = listValue2.getUniqName().replace("}", "");
                    uniqueName = uniqueName.replace("{", "");
                    if (paramValues != null && paramValues.contains(uniqueName)) {
                        includeColDefines.add(listValue2.getV());
                    }
                });
            });
        }
        for (int i = 0; i < columnDefs.size(); i++) {
            boolean exclude = false;
            ColDefine col = columnDefs.get(i);
            // TODO the showName should be put in generateSeriesBranch method as
            // third parameter.
            SeriesInputInfo info = null;
            if (isTimeChart) {
                info = seriesInput.get(0);
                info.setType(SeriesUnitType.LINE);
                if (context != null && context.size() != 0) {
                    for (String str : includeColDefines) {
                        // 对于不应该包含的列予以清除，并跳过当次循环
                        if (!col.getCaption().contains(str)) {
                            exclude = true;
                            break;
                        }
                    }
                    if (exclude) {
                        continue;
                    }
                }
            } else {
                info = seriesInput.get(i);
                String tmp = chartType.get(col.getUniqueName());
                if (tmp == null) {
                    info.setType(SeriesUnitType.COLUMN);
                } else {
                    info.setType(SeriesUnitType.valueOf(tmp.toUpperCase()));
                }
            }
            SeriesDataUnit branchData = null;
            if (info.getType() == SeriesUnitType.MAP) {
                List<RowHeadField> rowHeadFields = pTable.getRowHeadFields().get(0);
                branchData = generateSeriesBranch(pTable, col, info, i, rowHeadFields);
            } else {
                branchData = generateSeriesBranch(pTable, col, info, i);
            }
            units.add(branchData);
        }
        return units;
    }
            
    /**
     * 
     * @param pTable
     * @param col
     * @param info
     * @param i
     * @param rowHeadFields
     * @return SeriesDataUnit
     */
    private SeriesDataUnit generateSeriesBranch(PivotTable pTable, ColDefine col, SeriesInputInfo info, int i,
            List<RowHeadField> rowHeadFields) {
        if (pTable.getDataSourceColumnBased() == null || pTable.getDataSourceColumnBased().size() <= i) {
            return null;
        }
        List<CellData> columnData = pTable.getDataSourceColumnBased().get(i);
        SeriesDataUnit seriesUnit = new SeriesDataUnit();
        seriesUnit.setData(getDataFromCells(columnData));
        seriesUnit.setName(col.getCaption());
        seriesUnit.setType(info.getType().getName());
        seriesUnit.setFormat(col.getFormat());
        String[] measuerNames = MetaNameUtil.parseUnique2NameArray(col.getUniqueName());
        seriesUnit.setyAxisName(measuerNames[measuerNames.length - 1]);
        return seriesUnit;
    }

    // private String[][] genDataCaptions(List<RowHeadField> rowHeadFields) {
    // return rowHeadFields.stream().map(headField -> {
    // return new String[]{headField.getV(), headField.getUniqueName()};
    // }).toArray(String[][] :: new);
    // }

    /**
     * 
     * @param pTable
     * @param columnUniqName
     * @param showName
     * @param type
     * @param format
     * @param yAxisName
     * @return
     */
    private SeriesDataUnit generateSeriesBranch(PivotTable pTable, ColDefine col, SeriesInputInfo info, int i) {

        if (pTable.getDataSourceColumnBased() == null || pTable.getDataSourceColumnBased().size() <= i) {
            return null;
        }
        List<CellData> columnData = pTable.getDataSourceColumnBased().get(i);
        SeriesDataUnit seriesUnit = new SeriesDataUnit();
        seriesUnit.setData(getDataFromCells(columnData));
        seriesUnit.setName(col.getCaption());
        String[] measuerNames = MetaNameUtil.parseUnique2NameArray(col.getUniqueName());
        seriesUnit.setyAxisName(measuerNames[measuerNames.length - 1]);
        seriesUnit.setType(info.getType().getName());
        seriesUnit.setFormat(col.getFormat());
        // seriesUnit.setyAxisName(info.getyAxisName());
        return seriesUnit;
    }

    /**
     * 
     * @param columnData
     * @return
     */
    private BigDecimal[] getDataFromCells(List<CellData> columnData) {

        BigDecimal[] result = new BigDecimal[columnData.size()];
        for (int i = 0; i < columnData.size(); i++) {
            result[i] = columnData.get(i).getV();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DIReportChart parseToLiteOlapChart(PivotTable tableResult, Map<String, String> chartType, boolean isTimeChart,
            Map<String, Object> context) {
        return this.helpParse2Chart(tableResult, chartType, isTimeChart, context);
    }
    
    /**
     * 辅助将pivotTable转为DIReportChart
     * @param tableResult
     * @param chartType
     * @param isTimeChart
     * @param context
     * @return
     */
    private DIReportChart helpParse2Chart(PivotTable tableResult, Map<String, String> chartType, boolean isTimeChart,
            Map<String, Object> context) {
        DIReportChart reportChart = new DIReportChart();
        // reportChart.setTitle("趋势图");
        // reportChart.setSubTitle("");
        reportChart.setSeriesData(Lists.<SeriesDataUnit> newArrayList());
        // for(int i=0; i<tableResult.getColDefine().size(); i++){
        // SeriesInputInfo seriesInput = chartMeta.getSeriesSet().get(i);
        // if(!dataSets.containsKey(String.valueOf(i))){
        // continue;
        // }
        List<SeriesInputInfo> seriesInputs = Lists.newArrayList();
        for (int i = 0; i < chartType.size(); ++i) {
            SeriesInputInfo seriesInput = new SeriesInputInfo();
            // ColDefine define = tableResult.getColDefine().get(i);
            // seriesInput.setName(define.getShowAxis());
            // seriesInput.setyAxisName(define.getShowAxis());
            seriesInputs.add(seriesInput);
        }
        // for(String type : chartType) {
        // if (isTimeChart) {
        // seriesInput.setType(SeriesUnitType.LINE);
        // } else {
        // seriesInput.setType(SeriesUnitType.valueOf(type));
        // }
        // seriesInput.setyAxisName(type);
        //
        // }
        List<SeriesDataUnit> seriesUnits;
        if (context != null && context.size() != 0) {
            // 针对lite-olap的chart
            seriesUnits = getSeriesUnitsByInputUnit(seriesInputs, tableResult, chartType, isTimeChart, context);
        } else {
            // 针对普通的chart
            seriesUnits = getSeriesUnitsByInputUnit(seriesInputs, tableResult, chartType, isTimeChart);
        }
        reportChart.getSeriesData().addAll(seriesUnits);
        // }
        // ChartMetaData chartMeta = new ChartMetaData();
        reportChart.setyAxises(Lists.<YAxis> newArrayList());
        YAxis yAxis = new YAxis(); // chartMeta.getYAxises().get("test_axis");
        yAxis.setName("纵轴");
        yAxis.setUnitName("单位");
        reportChart.getyAxises().add(yAxis);

        /**
         * use the x axis from query result from first series.
         */
        reportChart.setxAxisCategories(getXAxisCategories(tableResult, isTimeChart));
        if (isTimeChart) {
            reportChart.setxAxisType(XAxisType.DATETIME.getName());
        } else {
            reportChart.setxAxisType(XAxisType.CATEGORY.getName());
        }
        List<BigDecimal> maxAndMinValue = getMaxAndMinValue(reportChart);
        if (maxAndMinValue != null && maxAndMinValue.size() >= 2) {
            reportChart.setMaxValue(maxAndMinValue.get(0));
            reportChart.setMinValue(maxAndMinValue.get(1));
        }
        return reportChart;
    }

}