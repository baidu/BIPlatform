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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * query meta in row axis
 * 
 * @author xiaoming.chen
 *
 */
public class AxisMeta implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6053293985706748632L;

    /**
     * crossjoinDims 轴上交叉的维度信息
     */
    private List<String> crossjoinDims;

    /**
     * queryMeasures 查询的指标信息，暂时不支持行上放指标 TODO 查询的指标 如果支持自定义指标的话，这里最好不用String类型。1期先这样。后续修改
     */
    private List<String> queryMeasures;
    
    /**
     * 平面表所需的查询字段之间的顺序，该变量仅会在平面表查询过程中使用
     */
    private List<String> queryItemsOrder;

    /**
     * axisType 轴的类型
     */
    private AxisType axisType;

    /**
     * construct with axisType
     * 
     * @param axisType axisType
     */
    public AxisMeta(AxisType axisType) {
        this.axisType = axisType;
    }

    /**
     * 查询的轴的类型
     * 
     * @author xiaoming.chen
     */
    public enum AxisType {
        /**
         * COLUMN 列
         */
        COLUMN,
        /**
         * ROW 行
         */
        ROW,
        /**
         * FILTER 过滤
         */
        FILTER,
        /**
         * OTHER TO BE implement
         */
        OTHER
    }

    /**
     * getter method for property crossjoinDims
     * 
     * @return the crossjoinDims
     */
    public List<String> getCrossjoinDims() {
        if (this.crossjoinDims == null) {
            this.crossjoinDims = new ArrayList<String>();
        }
        return crossjoinDims;
    }

    /**
     * setter method for property crossjoinDims
     * 
     * @param crossjoinDims the crossjoinDims to set
     */
    public void setCrossjoinDims(List<String> crossjoinDims) {
        this.crossjoinDims = crossjoinDims;
    }

    /**
     * getter method for property queryMeasures
     * 
     * @return the queryMeasures
     */
    public List<String> getQueryMeasures() {
        if (this.queryMeasures == null) {
            this.queryMeasures = new ArrayList<String>();
        }
        return queryMeasures;
    }

    /**
     * setter method for property queryMeasures
     * 
     * @param queryMeasures the queryMeasures to set
     */
    public void setQueryMeasures(List<String> queryMeasures) {
        this.queryMeasures = queryMeasures;
    }

    /**
     * getter method for property axisType
     * 
     * @return the axisType
     */
    public AxisType getAxisType() {
        return axisType;
    }

    /**
     * setter method for property axisType
     * 
     * @param axisType the axisType to set
     */
    public void setAxisType(AxisType axisType) {
        this.axisType = axisType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AxisMeta [crossjoinDims=" + crossjoinDims + ", queryMeasures=" + queryMeasures + ", axisType="
                + axisType + ", queryItemOrders=" + queryItemsOrder + "]";
    }

    
    /** 
     * 获取 queryItemsOrder 
     * @return the queryItemsOrder 
     */
    public List<String> getQueryItemsOrder() {
        if (queryItemsOrder == null) {
            queryItemsOrder = Lists.newArrayList();
        }
        return queryItemsOrder;
        
    }

    
    /** 
     * 设置 queryItemsOrder 
     * @param queryItemsOrder the queryItemsOrder to set 
     */
    public void setQueryItemsOrder(List<String> queryItemsOrder) {
        this.queryItemsOrder = queryItemsOrder;
        
    }

}
