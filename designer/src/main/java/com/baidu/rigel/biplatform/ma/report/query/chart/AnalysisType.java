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

/**
 * 分析类型
 * 
 * @author zhongyi
 * @since 2013.12.17
 * @version 1.0.0.0
 */
public enum AnalysisType {
    /**
     * 时间趋势分析
     */
    TIME_TREND("timetrend", ChartShowType.LINE);
    
    /**
     * 分析的名称
     */
    private String name;
    
    /**
     * 图形类型
     */
    private ChartShowType showType;
    
    /**
     * 初始化
     * 
     * @param name
     *            名称
     * @param showType
     *            展示类型
     */
    private AnalysisType(String name, ChartShowType showType) {
        this.setName(name);
        this.setShowType(showType);
    }
    
    /**
     * get the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * set the name
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * get the showType
     * 
     * @return the showType
     */
    public ChartShowType getShowType() {
        return showType;
    }
    
    /**
     * set the showType
     * 
     * @param showType
     *            the showType to set
     */
    public void setShowType(ChartShowType showType) {
        this.showType = showType;
    }
}