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
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * ChartMetaData
 * @author zhongyi
 *
 */
public class ChartMetaData implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = -6440344208428624352L;
    
    /**
     * title
     */
    private String title;
    
    /**
     * subTitle
     */
    private String subTitle;
    
    /**
     * xAxis
     */
    private List<Item> xAxis;
    
    /**
     * seriesSet
     */
    private List<SeriesInputInfo> seriesSet;
    
    /**
     * filters
     */
    private List<Item> filters;
    
    /**
     * yAxises
     */
    private Map<String, YAxis> yAxises;
    
    /**
     * 
     * @return
     */
    public static ChartMetaData generateEmptyChartMetaData() {
        ChartMetaData chart = new ChartMetaData();
        chart.setFilters(Lists.<Item>newArrayList());
        chart.setXAxis(Lists.<Item>newArrayList());
        chart.setSeriesSet(Lists.<SeriesInputInfo>newArrayList());
        SeriesInputInfo seriesUnit = SeriesInputInfo.generateEmptySeriesInputInfo(chart
                .getDefaultYAxis());
        chart.getSeriesSet().add(seriesUnit);
        initYAxis(chart);
        return chart;
    }
    
    /**
     * 
     * @param chart
     */
    public static void initYAxis(ChartMetaData chart) {
        chart.setYAxises(Maps.<String, YAxis>newHashMap());
        YAxis yAxisLeft = new YAxis();
        yAxisLeft.setName("left");
        // yAxis_left.setUnitName("元");
        yAxisLeft.setUnitName(" ");
        YAxis yAxisRight = new YAxis();
        yAxisRight.setName("right");
        // yAxis_right.setUnitName("个");
        yAxisRight.setUnitName(" ");
        chart.getYAxises().put(yAxisLeft.getName(), yAxisLeft);
        chart.getYAxises().put(yAxisRight.getName(), yAxisRight);
    }
    
    /**
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 
     * @return
     */
    public String getSubTitle() {
        return subTitle;
    }
    
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
    
    public List<Item> getXAxis() {
        return xAxis;
    }
    
    public void setXAxis(List<Item> xAxis) {
        this.xAxis = xAxis;
    }
    
    public List<SeriesInputInfo> getSeriesSet() {
        return seriesSet;
    }
    
    public void setSeriesSet(List<SeriesInputInfo> seriesSet) {
        this.seriesSet = seriesSet;
    }
    
    public List<Item> getFilters() {
        return filters;
    }
    
    public void setFilters(List<Item> filters) {
        this.filters = filters;
    }
    
    public Map<String, YAxis> getYAxises() {
        if (yAxises == null) {
            initYAxis(this);
        }
        return yAxises;
    }
    
    public String getDefaultYAxis() {
        Map<String, YAxis> yAxises = getYAxises();
        if (yAxises.values().iterator().hasNext()) {
            return yAxises.values().iterator().next().getName();
        }
        return "";
    }
    
    public void setYAxises(Map<String, YAxis> yAxises) {
        this.yAxises = yAxises;
    }
    
}