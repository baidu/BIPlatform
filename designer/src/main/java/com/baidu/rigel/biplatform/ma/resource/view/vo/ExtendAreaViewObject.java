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
package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * 扩展区域映射定义
 * @author david.wang
 * @version 1.0.0.1
 */
public class ExtendAreaViewObject implements Serializable {

    /**
     * ExtendAreaViewObject.java -- long
     * description:
     */
    private static final long serialVersionUID = -1370745808935500586L;
    
    /**
     * 过滤轴
     */
    private List<ItemViewObject> xAxis;
    
    /**
     * 过滤轴
     */
    private List<ItemViewObject> yAxis;
    
    /**
     * 过滤轴
     */
    private List<ItemViewObject> sAxis;

    /**
     * 候选维度
     */
    private List<ItemViewObject> candDims;
    
    /**
     * 候选指标
     */
    private List<ItemViewObject> candInds;
    
    /**
     * @return the candDims
     */
    public List<ItemViewObject> getCandDims() {
        return candDims;
    }

    /**
     * @param candDims the candDims to set
     */
    public void setCandDims(List<ItemViewObject> candDims) {
        this.candDims = candDims;
    }

    /**
     * @return the candInds
     */
    public List<ItemViewObject> getCandInds() {
        return candInds;
    }

    /**
     * @param candInds the candInds to set
     */
    public void setCandInds(List<ItemViewObject> candInds) {
        this.candInds = candInds;
    }

    /**
     * @return the xAxis
     */
    public List<ItemViewObject> getxAxis() {
        return xAxis;
    }

    /**
     * @param xAxis the xAxis to set
     */
    public void setxAxis(List<ItemViewObject> xAxis) {
        this.xAxis = xAxis;
    }

    /**
     * @return the yAxis
     */
    public List<ItemViewObject> getyAxis() {
        return yAxis;
    }

    /**
     * @param yAxis the yAxis to set
     */
    public void setyAxis(List<ItemViewObject> yAxis) {
        this.yAxis = yAxis;
    }

    /**
     * @return the sAxis
     */
    public List<ItemViewObject> getsAxis() {
        return sAxis;
    }

    /**
     * @param sAxis the sAxis to set
     */
    public void setsAxis(List<ItemViewObject> sAxis) {
        this.sAxis = sAxis;
    }
    
    
}
