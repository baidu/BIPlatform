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
 * 图形展示的类型
 * 
 * @author zhongyi
 *
 */
public enum ChartShowType {
    /**
     * 饼图
     */
    PIE("pie"),
    /**
     * 折线
     */
    LINE("line"),
    /**
     * 柱状
     */
    BAR("bar"),
    /**
     * 烧杯
     */
    BEAKER("beaker"),
    /**
     * 没有
     */
    EMPTY("");
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 初始化
     * 
     * @param name
     *            名称
     */
    private ChartShowType(String name) {
        this.name = name;
    }
    
    /**
     * get the name
     * 
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 得到图形展现类型
     * 
     * @param type
     *            类型名称
     * @return 类型对象
     */
    public static ChartShowType parseSeriesUnitType(String type) {
        if (type.equals("pie")) {
            return PIE;
        } else if (type.equals("line")) {
            return LINE;
        } else if (type.equals("bar")) {
            return BAR;
        } else if (type.equals("beaker")) {
            return BEAKER;
        } else {
            return EMPTY;
        }
    }
}