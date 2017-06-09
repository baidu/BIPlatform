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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * HighCharts图表对象
 * 
 * @author zhongyi
 *
 */
public class DIReportChart implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 3417297142845383674L;
    
    /**
     * title
     */
    private String title;
    
    /**
     * subTitle
     */
    private String subTitle;
    
    /**
     * xAxisType
     */
    private String xAxisType;
    
    /**
     * xAxisCategories
     */
    private String[] xAxisCategories;
    
    /**
     * 坐标轴描述信息
     */
    private ChartRenderInfo render;
    
    /**
     * yAxises
     */
    private List<YAxis> yAxises;
    
    /**
     * seriesData
     */
    private List<SeriesDataUnit> seriesData;
    
    /**
     * 
     */
    private String[] allMeasures;
    
    /**
     * defaultMeasuers
     */
    private String[] defaultMeasures;
    
    /**
     * allDims
     */
    private String[] allDims;
    
    /**
     * defaultDims
     */
    private String[] defaultDims;
    
    /**
     * 分析指标中的最大值
     */
    private BigDecimal maxValue = BigDecimal.valueOf(100);
    
    /**
     * 分析指标中的最小值
     */
    private BigDecimal minValue = BigDecimal.ZERO;
    
    /**
     * topN 设置方式
     */
    private String topType;
    
    /**
     * topN的记录条数
     */
    private int recordSize = 0;
    
    /**
     * topN的默认指标id
     */
    private String topedMeasureId;
    
    /**
     * 图形引用的所有指标列表
     */
    private Map<String, String> measureMap = Maps.newHashMap();
    
    /**
     * areaId
     */
    private String areaId;
    
    /**
     * dimMap
     */
    private Map<String, String> dimMap;
    
    private Map<String, Object> appearance;
    
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubTitle() {
        return subTitle;
    }
    
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
    
    public String getxAxisType() {
        return xAxisType;
    }
    
    public void setxAxisType(String xAxisType) {
        this.xAxisType = xAxisType;
    }
    
    public String[] getxAxisCategories() {
        return xAxisCategories;
    }
    
    public void setxAxisCategories(String[] xAxisCategories) {
        this.xAxisCategories = xAxisCategories;
    }
    
    public List<SeriesDataUnit> getSeriesData() {
        return seriesData;
    }
    
    public void setSeriesData(List<SeriesDataUnit> seriesData) {
        this.seriesData = seriesData;
    }
    
    public List<YAxis> getyAxises() {
        return yAxises;
    }
    
    public void setyAxises(List<YAxis> yAxises) {
        this.yAxises = yAxises;
    }

    /**
     * @return the allMeasures
     */
    public String[] getAllMeasures() {
        return allMeasures;
    }

    /**
     * @param allMeasures the allMeasures to set
     */
    public void setAllMeasures(String[] allMeasures) {
        this.allMeasures = allMeasures;
    }

    /**
     * @return the defaultMeasuers
     */
    public String[] getDefaultMeasures() {
        return defaultMeasures;
    }

    /**
     * @param defaultMeasuers the defaultMeasuers to set
     */
    public void setDefaultMeasures(String[] defaultMeasuers) {
        this.defaultMeasures = defaultMeasuers;
    }

    /**
     * @return the allDims
     */
    public String[] getAllDims() {
        return allDims;
    }

    /**
     * @param allDims the allDims to set
     */
    public void setAllDims(String[] allDims) {
        this.allDims = allDims;
    }

    /**
     * @return the defaultDims
     */
    public String[] getDefaultDims() {
        return defaultDims;
    }

    /**
     * @param defaultDims the defaultDims to set
     */
    public void setDefaultDims(String[] defaultDims) {
        this.defaultDims = defaultDims;
    }

    /**
     * @return the maxValue
     */
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * @return the minValue
     */
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the topType
     */
    public String getTopType() {
        if (topType == null) {
            topType = "NONE";
        }
        return topType;
    }

    /**
     * @param topType the topType to set
     */
    public void setTopType(String topType) {
        this.topType = topType;
    }

    /**
     * @return the recordSize
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * @param recordSize the recordSize to set
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /**
     * @return the topedMeasureId
     */
    public String getTopedMeasureId() {
        return topedMeasureId;
    }

    /**
     * @param topedMeasureId the topedMeasureId to set
     */
    public void setTopedMeasureId(String topedMeasureId) {
        this.topedMeasureId = topedMeasureId;
    }

    /**
     * @return the measureMap
     */
    public Map<String, String> getMeasureMap() {
        if (this.measureMap == null) {
            this.measureMap = Maps.newHashMap();
        }
        return measureMap;
    }

    /**
     * @param measureMap the measureMap to set
     */
    public void setMeasureMap(Map<String, String> measureMap) {
        this.measureMap = measureMap;
    }

    /**
     * @return the areaId
     */
    public String getAreaId() {
        return areaId;
    }

    /**
     * @param areaId the areaId to set
     */
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public void setDimMap(Map<String, String> dimMap) {
        this.dimMap =dimMap;
    }

    /**
     * @return the dimMap
     */
    public Map<String, String> getDimMap() {
        if (this.dimMap == null) {
            this.dimMap = Maps.newHashMap();
        }
        return dimMap;
    }

    /**
     * @return the render
     */
    public ChartRenderInfo getRender() {
        if (this.render == null) {
            this.render = new ChartRenderInfo ();
        }
        return render;
    }

    /**
     * @param render the render to set
     */
    public void setRender(ChartRenderInfo render) {
        this.render = render;
    }

    /**
     * @return the appearance
     */
    public Map<String, Object> getAppearance() {
        return appearance;
    }

    /**
     * @param appearance the appearance to set
     */
    public void setAppearance(Map<String, Object> appearance) {
        this.appearance = appearance;
    }


}
