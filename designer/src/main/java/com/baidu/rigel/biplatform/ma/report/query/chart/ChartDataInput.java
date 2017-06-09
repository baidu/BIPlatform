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

/**
 * 图的数据输入对象
 * 
 * @author zhongyi
 *
 */
public class ChartDataInput implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8025756903037364805L;
    
    /**
     * 数据
     */
    private List<List<BigDecimal>> data;
    /**
     * 横轴节点
     */
    private List<String> xAxis;
    
    /**
     * 图的横轴类型
     */
    private XAxisType xAxisType;
    
    /**
     * 每条记录的名称
     */
    private List<String> yAxis;
    /**
     * 分析类型
     */
    private AnalysisType type;
    /**
     * 标题
     */
    private String title;
    /**
     * 每条记录对应的Y轴信息
     */
    private Map<String, YAxis> yAxisInfos;
    /**
     * 每条记录的格式信息
     */
    private Map<String, DataFormat> formats;
    
    /**
     * 数据格式
     */
    private String format;
    
    /**
     * get the data
     * 
     * @return the data
     */
    public List<List<BigDecimal>> getData() {
        return data;
    }
    
    /**
     * set the data
     * 
     * @param data
     *            the data to set
     */
    public void setData(List<List<BigDecimal>> data) {
        this.data = data;
    }
    
    /**
     * get the xAxis
     * 
     * @return the xAxis
     */
    public List<String> getxAxis() {
        return xAxis;
    }
    
    /**
     * set the xAxis
     * 
     * @param xAxis
     *            the xAxis to set
     */
    public void setxAxis(List<String> xAxis) {
        this.xAxis = xAxis;
    }
    
    /**
     * get the yAxis
     * 
     * @return the yAxis
     */
    public List<String> getyAxis() {
        return yAxis;
    }
    
    /**
     * set the yAxis
     * 
     * @param yAxis
     *            the yAxis to set
     */
    public void setyAxis(List<String> yAxis) {
        this.yAxis = yAxis;
    }
    
    /**
     * get the type
     * 
     * @return the type
     */
    public AnalysisType getType() {
        return type;
    }
    
    /**
     * set the type
     * 
     * @param type
     *            the type to set
     */
    public void setType(AnalysisType type) {
        this.type = type;
    }
    
    /**
     * get the title
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * set the title
     * 
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * get the yAxisInfos
     * 
     * @return the yAxisInfos
     */
    public Map<String, YAxis> getyAxisInfos() {
        return yAxisInfos;
    }
    
    /**
     * set the yAxisInfos
     * 
     * @param yAxisInfos
     *            the yAxisInfos to set
     */
    public void setyAxisInfos(Map<String, YAxis> yAxisInfos) {
        this.yAxisInfos = yAxisInfos;
    }
    
    /**
     * get the formats
     * 
     * @return the formats
     */
    public Map<String, DataFormat> getFormats() {
        return formats;
    }
    
    /**
     * set the formats
     * 
     * @param formats
     *            the formats to set
     */
    public void setFormats(Map<String, DataFormat> formats) {
        this.formats = formats;
    }
    
    /**
     * get the xAxisType
     * 
     * @return the xAxisType
     */
    public XAxisType getxAxisType() {
        return xAxisType;
    }
    
    /**
     * set the xAxisType
     * 
     * @param xAxisType
     *            the xAxisType to set
     */
    public void setxAxisType(XAxisType xAxisType) {
        this.xAxisType = xAxisType;
    }
    
    /**
     * default generate get format
     * 
     * @return the format
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * default generate format param set method
     * 
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }
}