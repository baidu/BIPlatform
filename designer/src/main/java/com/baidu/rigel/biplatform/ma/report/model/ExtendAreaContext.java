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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 扩展区域对应上下文：存储当前区域的参数、面包屑、查询结果纪录、下钻／上卷纪录，区域定义信息
 * 
 * @author david.wang
 *
 */
public class ExtendAreaContext implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4565865318696020689L;

    /**
     * 区域id
     */
    private String areaId;

    /**
     * 区域类型
     */
    private ExtendAreaType areaType;

    /**
     * 格式化信息
     */
    private FormatModel formatModel;

    /**
     * 面包屑路径
     */
    private List<Map<String, String>> curBreadCrumPath;

    /**
     * 当前操作下钻条目
     */
    private String drillItemValue;

    /**
     * 报表id
     */
    private String reportId;

    /**
     * 当前上下文包含的参数信息
     */
    private Map<String, Object> params = Maps.newConcurrentMap();

    /**
     * 区域x轴对应的查询条目
     */
    private LinkedHashMap<Item, Object> x = Maps.newLinkedHashMap();

    /**
     * 区域y轴对应的查询条目
     */
    private LinkedHashMap<Item, Object> y = Maps.newLinkedHashMap();

    /**
     * 区域过滤轴对应的查询条目
     */
    private LinkedHashMap<Item, Object> s = Maps.newLinkedHashMap();

    /**
     * 区域备选维度轴对应的条目信息
     */
    private Set<Item> canDim = Sets.newLinkedHashSet();

    /**
     * 区域备选指标轴对应的条目
     */
    private Set<Item> canInd = Sets.newLinkedHashSet();

    /**
     * 
     */
    private Map<String, Item> selectDims = Maps.newHashMap();

    /**
     * 
     */
    private Map<String, Item> selectMeasures = Maps.newHashMap();

    /**
     * 区域默认使用的数据源列表
     */
    private DataSourceInfo defaultDsInfo;

    /**
     * 备用条目、数据源信息对应关系，后续跨库查询使用
     */
    private Map<Item, DataSourceInfo> dsInfo;

    /**
     * 依据扩展区域定义生成的cube，在运行时，此cube不会发生变化
     */
    private Cube cubeDefine;

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

    /**
     * @return the areaType
     */
    public ExtendAreaType getAreaType() {
        return areaType;
    }

    /**
     * @param areaType the areaType to set
     */
    public void setAreaType(ExtendAreaType areaType) {
        this.areaType = areaType;
    }

    /**
     * @return the formatModel
     */
    public FormatModel getFormatModel() {
        return formatModel;
    }

    /**
     * @param formatModel the formatModel to set
     */
    public void setFormatModel(FormatModel formatModel) {
        this.formatModel = formatModel;
    }

    /**
     * @return the curBreadCrumPath
     */
    public List<Map<String, String>> getCurBreadCrumPath() {
        return curBreadCrumPath;
    }

    /**
     * @param curBreadCrumPath the curBreadCrumPath to set
     */
    public void setCurBreadCrumPath(List<Map<String, String>> curBreadCrumPath) {
        this.curBreadCrumPath = curBreadCrumPath;
    }

    /**
     * @return the drillItemValue
     */
    public String getDrillItemValue() {
        return drillItemValue;
    }

    /**
     * @param drillItemValue the drillItemValue to set
     */
    public void setDrillItemValue(String drillItemValue) {
        this.drillItemValue = drillItemValue;
    }

    /**
     * @return the reportId
     */
    public String getReportId() {
        return reportId;
    }

    /**
     * @param reportId the reportId to set
     */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    /**
     * @return the params
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * @return the x
     */
    public LinkedHashMap<Item, Object> getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(LinkedHashMap<Item, Object> x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public LinkedHashMap<Item, Object> getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(LinkedHashMap<Item, Object> y) {
        this.y = y;
    }

    /**
     * @return the s
     */
    public LinkedHashMap<Item, Object> getS() {
        return s;
    }

    /**
     * @param s the s to set
     */
    public void setS(LinkedHashMap<Item, Object> s) {
        this.s = s;
    }

    /**
     * @return the canDim
     */
    public Set<Item> getCanDim() {
        return canDim;
    }

    /**
     * @param canDim the canDim to set
     */
    public void setCanDim(Set<Item> canDim) {
        this.canDim = canDim;
    }

    /**
     * @return the canInd
     */
    public Set<Item> getCanInd() {
        return canInd;
    }

    /**
     * @param canInd the canInd to set
     */
    public void setCanInd(Set<Item> canInd) {
        this.canInd = canInd;
    }

    /**
     * @return the defaultDsInfo
     */
    public DataSourceInfo getDefaultDsInfo() {
        return defaultDsInfo;
    }

    /**
     * @param defaultDsInfo the defaultDsInfo to set
     */
    public void setDefaultDsInfo(DataSourceInfo defaultDsInfo) {
        this.defaultDsInfo = defaultDsInfo;
    }

    /**
     * @return the dsInfo
     */
    public Map<Item, DataSourceInfo> getDsInfo() {
        return dsInfo;
    }

    /**
     * @param dsInfo the dsInfo to set
     */
    public void setDsInfo(Map<Item, DataSourceInfo> dsInfo) {
        this.dsInfo = dsInfo;
    }

    /**
     * @return the cubeDefine
     */
    public Cube getCubeDefine() {
        return cubeDefine;
    }

    /**
     * @param cubeDefine the cubeDefine to set
     */
    public void setCubeDefine(Cube cubeDefine) {
        this.cubeDefine = cubeDefine;
    }

    /**
     * @return the selectDims
     */
    public Map<String, Item> getSelectDims() {
        return selectDims;
    }

    /**
     * @param selectDims the selectDims to set
     */
    public void setSelectDims(Map<String, Item> selectDims) {
        this.selectDims = selectDims;
    }

    /**
     * @return the selectMeasures
     */
    public Map<String, Item> getSelectMeasures() {
        return selectMeasures;
    }

    /**
     * @param selectMeasures the selectMeasures to set
     */
    public void setSelectMeasures(Map<String, Item> selectMeasures) {
        this.selectMeasures = selectMeasures;
    }

}
