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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.google.common.collect.Maps;

/**
 * 问题模型
 * 
 * @author xiaoming.chen
 *
 */
public class QuestionModel implements Serializable {
    
    /**
     * default generate id
     */
    private static final long serialVersionUID = 1576666141762101245L;
    
    /**
     * axisMetas 轴上的元数据信息
     */
    private Map<AxisType, AxisMeta> axisMetas;
    
    /**
     * queryConditions 查询的条件信息
     */
    private Map<String, MetaCondition> queryConditions;
    
    /**
     * callback 维度的参数
     */
    private Map<String, String> requestParams;
    
    /**
     * sortRecord 排序信息
     */
    private SortRecord sortRecord;
    
    /**
     * 查询的cube的Id
     */
    private String cubeId;
    
    /**
     * dataSourceInfoKey 问题模型对应的数据源信息的key
     */
    private String dataSourceInfoKey;
    
    /**
     * dataSourceInfo 问题模型对应的数据源信息（有数据源的key优先从缓存取）
     */
    private DataSourceInfo dataSourceInfo;
    
    /**
     * useCache 默认使用缓存
     */
    private boolean useCache = true;
    
    /**
     * queryConditionLimit 条件超限异常
     */
    private QueryConditionLimit queryConditionLimit;
    
    /**
     * pageInfo 分页信息
     */
    private PageInfo pageInfo;
    
    /**
     * needSummary 是否需要汇总
     */
    private boolean needSummary = false;
    
    /**
     * isUseIndex 是否使用索引，默认为true
     */
    private boolean isUseIndex = true;
    
    /**
     * 是否过滤空白行
     */
    private boolean filterBlank = false;
    
    /**
     * 查询对象
     */
    public String querySource;
    
    /**
     * 查询Id
     */
    public String queryId;
    
    /**
     * getQueryId
     * 
     * @return the queryId
     */
    public String getQueryId() {
        return queryId;
    }
    
    /**
     * setQueryId
     * 
     * @param queryId
     *            the queryId to set
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
    
    /**
     * getter method for property querySource
     * 
     * @return querySource
     */
    public String getQuerySource() {
        return querySource;
    }
    
    /**
     * setter method for property querySource
     * 
     * @param querySource
     */
    public void setQuerySource(String querySource) {
        this.querySource = querySource;
    }
    
    /**
     * getter method for property axisMetas
     * 
     * @return the axisMetas
     */
    public Map<AxisType, AxisMeta> getAxisMetas() {
        if (axisMetas == null) {
            this.axisMetas = Maps.newHashMap();
        }
        return axisMetas;
    }
    
    /**
     * setter method for property axisMetas
     * 
     * @param axisMetas
     *            the axisMetas to set
     */
    public void setAxisMetas(Map<AxisType, AxisMeta> axisMetas) {
        this.axisMetas = axisMetas;
    }
    
    /**
     * getter method for property queryConditions
     * 
     * @return the queryConditions
     */
    public Map<String, MetaCondition> getQueryConditions() {
        if (this.queryConditions == null) {
            this.queryConditions = new HashMap<String, MetaCondition>();
        }
        return queryConditions;
    }
    
    /**
     * setter method for property queryConditions
     * 
     * @param queryConditions
     *            the queryConditions to set
     */
    public void setQueryConditions(Map<String, MetaCondition> queryConditions) {
        this.queryConditions = queryConditions;
    }
    
    /**
     * getter method for property sortRecord
     * 
     * @return the sortRecord
     */
    public SortRecord getSortRecord() {
        return sortRecord;
    }
    
    /**
     * setter method for property sortRecord
     * 
     * @param sortRecord
     *            the sortRecord to set
     */
    public void setSortRecord(SortRecord sortRecord) {
        this.sortRecord = sortRecord;
    }
    
    /**
     * getter method for property useCache
     * 
     * @return the useCache
     */
    public boolean isUseCache() {
        return useCache;
    }
    
    /**
     * setter method for property useCache
     * 
     * @param useCache
     *            the useCache to set
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    /**
     * getter method for property queryConditionLimit
     * 
     * @return the queryConditionLimit
     */
    public QueryConditionLimit getQueryConditionLimit() {
        if (this.queryConditionLimit == null) {
            this.queryConditionLimit = new QueryConditionLimit();
        }
        return queryConditionLimit;
    }
    
    /**
     * setter method for property queryConditionLimit
     * 
     * @param queryConditionLimit
     *            the queryConditionLimit to set
     */
    public void setQueryConditionLimit(QueryConditionLimit queryConditionLimit) {
        this.queryConditionLimit = queryConditionLimit;
    }
    
    /**
     * getter method for property pageInfo
     * 
     * @return the pageInfo
     */
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    
    /**
     * setter method for property pageInfo
     * 
     * @param pageInfo
     *            the pageInfo to set
     */
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
    
    /**
     * getter method for property needSummary
     * 
     * @return the needSummary
     */
    public boolean isNeedSummary() {
        return needSummary;
    }
    
    /**
     * setter method for property needSummary
     * 
     * @param needSummary
     *            the needSummary to set
     */
    public void setNeedSummary(boolean needSummary) {
        this.needSummary = needSummary;
    }
    
    /**
     * get requestParams
     * 
     * @return the requestParams
     */
    public Map<String, String> getRequestParams() {
        if (this.requestParams == null) {
            this.requestParams = new HashMap<String, String>();
        }
        return requestParams;
    }
    
    /**
     * set requestParams with requestParams
     * 
     * @param requestParams
     *            the requestParams to set
     */
    public void setRequestParams(Map<String, String> requestParams) {
        this.requestParams = requestParams;
    }
    
    /**
     * get cubeId
     * 
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }
    
    /**
     * set cubeId with cubeId
     * 
     * @param cubeId
     *            the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }
    
    /**
     * get dataSourceInfoKey
     * 
     * @return the dataSourceInfoKey
     */
    public String getDataSourceInfoKey() {
        return dataSourceInfoKey;
    }
    
    /**
     * set dataSourceInfoKey with dataSourceInfoKey
     * 
     * @param dataSourceInfoKey
     *            the dataSourceInfoKey to set
     */
    public void setDataSourceInfoKey(String dataSourceInfoKey) {
        this.dataSourceInfoKey = dataSourceInfoKey;
    }
    
    /**
     * get isUseIndex
     * 
     * @return the isUseIndex
     */
    public boolean isUseIndex() {
        return isUseIndex;
    }
    
    /**
     * set isUseIndex with isUseIndex
     * 
     * @param isUseIndex
     *            the isUseIndex to set
     */
    public void setUseIndex(boolean isUseIndex) {
        this.isUseIndex = isUseIndex;
    }
    
    /**
     * @return the filterBlank
     */
    public boolean isFilterBlank() {
        return filterBlank;
    }
    
    /**
     * @param filterBlank
     *            the filterBlank to set
     */
    public void setFilterBlank(boolean filterBlank) {
        this.filterBlank = filterBlank;
    }
    
    /**
     * default generate get dataSourceInfo
     * 
     * @return the dataSourceInfo
     */
    public DataSourceInfo getDataSourceInfo() {
        return dataSourceInfo;
    }
    
    /**
     * default generate set dataSourceInfo
     * 
     * @param dataSourceInfo
     *            the dataSourceInfo to set
     */
    public void setDataSourceInfo(DataSourceInfo dataSourceInfo) {
        this.dataSourceInfo = dataSourceInfo;
    }
    
    // /**
    // * 针对在行或者列上有的维度，如果没有在条件中的话，那么需要在条件中添加一个默认条件的维值
    // */
    // public void completeQueryCondition() {
    // AxisMeta axisMeta = null;
    // AxisType axisType = AxisType.COLUMN;
    // while (axisType != null && (axisMeta = getAxisMetas().get(axisType)) !=
    // null) {
    // if (CollectionUtils.isNotEmpty(axisMeta.getCrossjoinDims())) {
    // for (String dimName : axisMeta.getCrossjoinDims()) {
    // // 如果没有设置条件的话，添加一个默认的空条件进去，防止丢失
    // if (!this.queryConditions.containsKey(dimName)) {
    // this.queryConditions.put(dimName, new DimensionCondition(dimName));
    // }
    // }
    // }
    // if (axisMeta.equals(AxisType.ROW)) {
    // axisType = null;
    // } else {
    // axisType = AxisType.ROW;
    // }
    // }
    // }
    
}