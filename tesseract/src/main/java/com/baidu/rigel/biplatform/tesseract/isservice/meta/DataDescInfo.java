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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;

/**
 * 
 * 数据描述信息
 * 
 * @author lijin
 *
 */
public class DataDescInfo implements Serializable {
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = -5648266665662656146L;
    /**
     * 数据源名称
     */
    private String sourceName;
    /**
     * 数据源所属产品线
     */
    private String productLine;
    /**
     * 物理表
     */
    private List<String> tableNameList;
    /**
     * 是否分表
     */
    private boolean isSplitTable;
    
    /**
     * 物理表，如果该物理表是分表的，则存储表前纵+正则；如果是普通表，就存表的名；
     */
    private String tableName;
    
    /**
     * 当前索引的最大数据ID（在事实表中的ID值） 在更新数据时，取>maxDataId的数据
     */
    private Map<String, BigDecimal> maxDataIdMap;
    
    /**
     * 事实表的ID字段
     */
    private String idStr ;
    
    /**
     * 
     * TODO getSourceName
     * 
     * @return sourceName
     */
    public String getSourceName() {
        return sourceName;
    }
    
    /**
     * 
     * TODO setSourceName
     * 
     * @param sourceName
     *            数据源名称
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    /**
     * 
     * TODO getProductLine
     * 
     * @return productLine
     */
    public String getProductLine() {
        return productLine;
    }
    
    /**
     * 
     * TODO setProductLine
     * 
     * @param productLine
     *            产品线信息
     */
    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }
    
    /**
     * 
     * TODO getTableNameList
     * 
     * @return tableNameList
     */
    public List<String> getTableNameList() {
        return tableNameList;
    }
    
    /**
     * 
     * TODO setTableNameList
     * 
     * @param tableNameList
     *            物理表
     */
    public void setTableNameList(List<String> tableNameList) {
        this.tableNameList = tableNameList;
    }
    
    /**
     * getter method for property tableName
     * 
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * setter method for property tableName
     * 
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (obj == null || !(obj instanceof DataDescInfo)) {
            return false;
        }
        DataDescInfo dsObj = (DataDescInfo) obj;
        if (this.productLine != null && dsObj.getProductLine() != null
            && this.productLine.equals(dsObj.getProductLine())) {
            if (this.sourceName != null && dsObj.getSourceName() != null
                && this.sourceName.equals(dsObj.getSourceName())) {
                if (this.tableName != null && this.getTableName() != null
                    && this.tableName.equals(dsObj.getTableName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.productLine);
        sb.append(this.sourceName);
        sb.append(this.tableName);
        
        return sb.toString().hashCode();
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ProductLine:[");
        sb.append(this.productLine + "]");
        sb.append("SourceName:[");
        sb.append(this.sourceName + "]");
        sb.append("TableName:[");
        sb.append(this.tableName + "]");
        return sb.toString();
    }
    
    /**
     * getter method for property isSplitTable
     * 
     * @return the isSplitTable
     */
    public boolean isSplitTable() {
        return isSplitTable;
    }
    
    /**
     * setter method for property isSplitTable
     * 
     * @param isSplitTable
     *            the isSplitTable to set
     */
    public void setSplitTable(boolean isSplitTable) {
        this.isSplitTable = isSplitTable;
    }
    
    /**
     * getter method for property maxDataIdMap
     * 
     * @return the maxDataIdMap
     */
    public Map<String, BigDecimal> getMaxDataIdMap() {
        if ((this.maxDataIdMap == null || this.maxDataIdMap.isEmpty())
                && this.tableNameList != null && this.tableNameList.size() != 0) {
            this.maxDataIdMap = new HashMap<String, BigDecimal>();
            BigDecimal maxId = BigDecimal.ZERO;
            for (String tableName : this.tableNameList) {
                this.maxDataIdMap.put(tableName, maxId);
            }
        }
        return maxDataIdMap;
    }
    
    /**
     * setter method for property maxDataIdMap
     * 
     * @param maxDataIdMap
     *            the maxDataIdMap to set
     */
    public void setMaxDataIdMap(Map<String, BigDecimal> maxDataIdMap) {
        this.maxDataIdMap = maxDataIdMap;
    }
    
    /**
     * getMaxDataId
     * 
     * @param tableName
     *            表名
     * @return BigDecimal
     */
    public BigDecimal getMaxDataId(String tableName) {
        if (this.maxDataIdMap == null || !this.maxDataIdMap.containsKey(tableName)) {
            return null;
        }
        return this.maxDataIdMap.get(tableName);
    }
    
    /**
     * getter method for property idStr
     * 
     * @return the idStr
     */
    public String getIdStr() {
        if (StringUtils.isEmpty(this.idStr)) {
            this.idStr = TesseractConstant.FACTTABLE_ID_DEFAULT;
        }
        return idStr;
    }
    
    /**
     * setter method for property idStr
     * 
     * @param idStr
     *            the idStr to set
     */
    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }
    
}
