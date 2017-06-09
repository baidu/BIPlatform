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
package com.baidu.rigel.biplatform.ma.report.query;

import java.io.Serializable;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.report.model.Item;

/**
 * 
 * 查询操作，由查询服务创建action，将action传递给运行时模型 作为操作备忘，运行时模型依据查询action创建问题模型，调用查询接口查询数据
 * 
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class QueryAction implements Serializable {
    
    /**
     * QueryAction.java -- long description:
     */
    private static final long serialVersionUID = 61085589512178310L;
    
    /**
     * id 无业务含义
     */
    private String id;
    
    /**
     * 查询路径
     */
    private String queryPath;
    
    /**
     * 查询动作对应的区域id
     */
    private String extendAreaId;
    
    /**
     * 查询行轴信息 key item's id
     */
    private Map<Item, Object> columns = new LinkedHashMap<Item, Object> ();
    
    /**
     * 下载的维度值
     */
    private Map<Item, Object> drillDimValues = new LinkedHashMap<Item, Object> ();
    
    /**
     * 查询列轴信息 key为具体 item's id
     */
    private Map<Item, Object> rows = new LinkedHashMap<Item, Object> ();
    
    /**
     * 查询过滤描述信息 key为具体维度或者指标id，value为过滤值
     */
    private Map<Item, Object> slices = new LinkedHashMap<Item, Object> ();
    
    /**
     * 是否是图查询
     */
    private boolean chartQuery;
    /**
     * 是否是时间趋势图
     * TODO trendQuery应该与chartQuery合并成枚举类-->queryType
     */
    private boolean trendQuery;
    
    private OrderDesc orderDesc;
    
    /**
     * 
     */
    private boolean filterBlank = false;
    
    /**
     * 是否需要采用二八原则进行统计分析
     */
    private boolean needOthers = false;
    
    /**
     * 构造函数
     * 
     * QueryAction
     */
    public QueryAction() {
        columns = new LinkedHashMap<Item, Object> ();
        rows = new LinkedHashMap<Item, Object> ();
        slices = new LinkedHashMap<Item, Object> ();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getQueryPath() {
        return queryPath;
    }
    
    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }
    
    public String getExtendAreaId() {
        return extendAreaId;
    }
    
    public void setExtendAreaId(String extendAreaId) {
        this.extendAreaId = extendAreaId;
    }
    
    public Map<Item, Object> getSlices() {
        return slices;
    }
    
    public void setSlices(Map<Item, Object> slices) {
        this.slices = slices;
    }
    
    /**
     * get the columns
     * 
     * @return the columns
     */
    public Map<Item, Object> getColumns() {
        return columns;
    }
    
    /**
     * set the columns
     * 
     * @param columns
     *            the columns to set
     */
    public void setColumns(Map<Item, Object> columns) {
        this.columns = columns;
    }
    
    /**
     * get the rows
     * 
     * @return the rows
     */
    public Map<Item, Object> getRows() {
        return rows;
    }
    
    /**
     * set the rows
     * 
     * @param rows
     *            the rows to set
     */
    public void setRows(Map<Item, Object> rows) {
        this.rows = rows;
    }
    
    /**
     * 生成标示当前查询动作的唯一标示
     * 
     * @return 当前查询动作的唯一标示
     */
    public String getDistinctId() {
        StringBuilder key = new StringBuilder ();
        key.append (this.extendAreaId);
        key.append (this.queryPath);
        key.append (parseToKeyStr (rows));
        key.append (parseToKeyStr (columns));
        key.append (parseToKeyStr (slices));
        return Base64.getEncoder ()
                .encodeToString (key.toString ().getBytes ());
    }
    
    private StringBuilder parseToKeyStr(Map<Item, Object> values) {
        StringBuilder key = new StringBuilder ();
        for (Map.Entry<Item, Object> entry : values.entrySet ()) {
            key.append (entry.getKey ().getOlapElementId ());
            Object value = entry.getValue ();
            if (value instanceof Object[]) {
                String[] tmp = (String[]) value;
                for (String str : tmp) {
                    key.append (str);
                }
            } else {
                key.append (value);
            }
        }
        return key;
    }
    
    /**
     * @return the drillDimValues
     */
    public Map<Item, Object> getDrillDimValues() {
        return drillDimValues;
    }
    
    /**
     * @param drillDimValues
     *            the drillDimValues to set
     */
    public void setDrillDimValues(Map<Item, Object> drillDimValues) {
        this.drillDimValues = drillDimValues;
    }
    
    /**
     * @return the chartQuery
     */
    public boolean isChartQuery() {
        return chartQuery;
    }
    
    public void setChartQuery(boolean chartQuery) {
        this.chartQuery = chartQuery;
    }
    
    
    
    

    /**
     * @return the trendQuery
     */
    public boolean isTrendQuery() {
        return trendQuery;
    }

    /**
     * @param trendQuery the trendQuery to set
     */
    public void setTrendQuery(boolean trendQuery) {
        this.trendQuery = trendQuery;
    }

    /**
     * @return the orderDesc
     */
    public OrderDesc getOrderDesc() {
        return orderDesc;
    }
    
    /**
     * @param oderDesc
     *            the orderDesc to set
     */
    public void setOrderDesc(OrderDesc oderDesc) {
        this.orderDesc = oderDesc;
    }
    
    /**
     * 排序定义
     * @author david.wang
     *
     */
    public static class OrderDesc implements Serializable {
        
        /**
         * 序列id
         */
        private static final long serialVersionUID = -1589055019481416885L;
        
        /**
         * name
         */
        private final String name;
        
        /**
         * orderType
         */
        private final String orderType;
        
        /**
         * recordSize
         */
        private final int recordSize;
        
        /**
         * 
         * @param name
         * @param orderType
         * @param recordSize
         */
        public OrderDesc(String name, String orderType, int recordSize) {
            super ();
            this.name = name;
            this.orderType = orderType;
            this.recordSize = recordSize;
        }
        
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * @return the orderType
         */
        public String getOrderType() {
            return orderType;
        }
        
        /**
         * @return the recordSize
         */
        public int getRecordSize() {
            return recordSize;
        }
        
        /**
         * 
         */
        @Override
        public String toString() {
            return "name = " + this.name + " type = " + this.orderType + " recordSize = " + this.recordSize;
        }
    }
    
    /**
     * 
     * @param filterBlank
     */
    public void setFilterBlank(boolean filterBlank) {
        this.filterBlank = filterBlank;
    }
    
    /**
     * @return the filterBlank
     */
    public boolean isFilterBlank() {
        return filterBlank;
    }
    
    public boolean isNeedOthers() {
        return needOthers;
    }
    
    public void setNeedOthers(boolean needOthers) {
        this.needOthers = needOthers;
    }
    
}
