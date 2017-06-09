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
package com.baidu.rigel.biplatform.ma.report.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * liteOlap区域的对象
 * 
 * @author zhongyi
 *
 */
public class LiteOlapExtendArea extends ExtendArea {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4715184148329394854L;
    
    /**
     * 选择区ID
     */
    private String selectionAreaId;
    
    /**
     * tableAreaId
     */
    private String tableAreaId;
    
    /**
     * chartAreaId
     */
    private String chartAreaId;
    
    /**
     * 候选维度
     */
    private Map<String, Item> candDims;
    
    /**
     * 候选指标
     */
    private Map<String, Item> candInds;

    /**
     * @return the candDims
     */
    public Map<String, Item> getCandDims() {
        if (candDims == null) {
            candDims = new LinkedHashMap<String, Item>();
        }
        return candDims;
    }

    /**
     * @param candDims the candDims to set
     */
    public void setCandDims(Map<String, Item> candDims) {
        this.candDims = candDims;
    }

    /**
     * @return the candInds
     */
    public Map<String, Item> getCandInds() {
        if (candInds == null) {
            candInds = new LinkedHashMap<String, Item>();
        }
        return candInds;
    }

    /**
     * @param candInds the candInds to set
     */
    public void setCandInds(Map<String, Item> candInds) {
        this.candInds = candInds;
    }

    /**
     * @return the tableAreaId
     */
    public String getTableAreaId() {
        return tableAreaId;
    }

    /**
     * @param tableAreaId the tableAreaId to set
     */
    public void setTableAreaId(String tableAreaId) {
        this.tableAreaId = tableAreaId;
    }

    /**
     * @return the chartAreaId
     */
    public String getChartAreaId() {
        return chartAreaId;
    }

    /**
     * @param chartAreaId the chartAreaId to set
     */
    public void setChartAreaId(String chartAreaId) {
        this.chartAreaId = chartAreaId;
    }
    
    /**
     * 
     * @param item
     */
    public void addCandDim(Item item) {
        this.getCandDims().put(item.getOlapElementId(), item);
    }
    
    /**
     * 
     * @param itemId
     */
    public void removeCandDim(String itemId) {
        this.getCandDims().remove(itemId);
    }
    
    /**
     * 
     * @param item
     */
    public void addCandInd(Item item) {
        this.getCandInds().put(item.getOlapElementId(), item);
    }
    
    /**
     * 
     * @param itemId
     */
    public void removeCandInd(String itemId) {
        this.getCandInds().remove(itemId);
    }

    /**
     * @return the selectionAreaId
     */
    public String getSelectionAreaId() {
        return selectionAreaId;
    }

    /**
     * @param selectionAreaId the selectionAreaId to set
     */
    public void setSelectionAreaId(String selectionAreaId) {
        this.selectionAreaId = selectionAreaId;
    }
    
    @Override
    public Item getItem(String itemId) {
        Item result = this.getLogicModel().getItem(itemId);
        if (result != null) {
            return result;
        }
        for (Item item : this.candDims.values()) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        for (Item item : this.candInds.values()) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }
}
