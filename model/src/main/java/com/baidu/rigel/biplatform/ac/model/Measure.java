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
/**
 * 
 */
package com.baidu.rigel.biplatform.ac.model;

import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 指标：作为多维分析模型中特殊的维度，通常对应的成员均是数字类型。 指标的主要作用是从量化的角度反映一个或者几个属性（维度）对整体分析结果的影响程度。
 * 比如：要统计同一广告在不同页面中的点击次数从而来为精准投放广告提供必要的决策支持。这一业务模型中 页面、广告构成了统计点次数的属性，而点击率就是从量化角度上来体现不同属性的情况下， 投放效果的好坏。
 * 
 * @author xiaoming.chen
 *
 */
@JsonIgnoreProperties
public interface Measure extends OlapElement {

    /**
     * 指标的UniqueName开头
     */
    public static final String MEASURE_DIMENSION_NAME = "Measure";

    /**
     * 指标的汇总方式
     * 
     * @return 指标汇总方式
     */
    Aggregator getAggregator();

    /**
     * 事实表中指标定义
     * 
     * @return 如果是普通指标 返回事实表中列名，如果是计算指标，返回表达式 如果是自定义指标 返回定义信息
     */
    String getDefine();

    /**
     * 获取指标类型
     * 
     * @return 获取指标类型
     */
    MeasureType getType();

    @JsonIgnore
    @Override
    public default String getUniqueName() {
        return MetaNameUtil.generateMeasureUniqueName(getName());
    }

}
