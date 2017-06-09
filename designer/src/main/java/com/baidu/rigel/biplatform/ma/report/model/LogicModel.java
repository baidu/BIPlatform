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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.utils.LinkedHashMapUtils;
import com.google.common.collect.Maps;

/**
 * 
 * 报表逻辑模型定义：逻辑模型用来描述一张数据表或者一种统计图或者数据部件的基本组成。 逻辑模型包含行轴、列轴、过滤轴(切片轴)但不需要完全包含所有定义信息。
 * 如：对于提供过滤条件的部件，值需要提供行轴信息即可，也可以提供所有信息
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class LogicModel implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 2692928208926779245L;
    
    /**
     * id 定义
     */
    private String id;
    
    /**
     * 列轴
     */
    private Map<String, Item> columns = new LinkedHashMap<String, Item>();
    
    /**
     * 行轴
     */
    private Map<String, Item> rows = new LinkedHashMap<String, Item>();
    
    /**
     * 切片轴
     */
    private Map<String, Item> slices = new LinkedHashMap<String, Item>();
    
    /**
     * 备选维度
     */
    private Map<String, Item> selectionDims = Maps.newLinkedHashMap();
    
    /**
     * 备选指标
     */
    private Map<String, Item> selectionMeasures = Maps.newLinkedHashMap();
    
    /**
     * topN设置
     */
    private MeasureTopSetting topSetting;
    
    public Item[] getColumns() {
        return this.columns.values().toArray(new Item[0]);
    }
    
    public Item[] getRows() {
        return this.rows.values().toArray(new Item[0]);
    }
    
    public Item[] getSlices() {
        return this.slices.values().toArray(new Item[0]);
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 添加列定义
     * 
     * @param column
     */
    public void addColumn(Item column) {
        this.columns.put(column.getOlapElementId(), column);
    }
    
    /**
     * 添加列定义
     * 
     * @param column
     */
    public void addColumn(Item columnItem, int index) {
        this.columns = LinkedHashMapUtils.buildLinkedMapWithNewEntry((LinkedHashMap<String, Item>) this.columns,
                columnItem, columnItem.getOlapElementId(), index);
    }
    
    /**
     * 添加列定义
     * 
     * @param column
     */
    public void addColumns(Item[] columns) {
        for (Item col : columns) {
            addColumn(col);
        }
    }
    
    /**
     * 添加行定义
     * 
     * @param column
     */
    public void addRows(Item[] rows) {
        for (Item row : rows) {
            if (row != null) {
                addRow(row);
            }
        }
    }
    
    /**
     * 添加过滤定义
     * 
     * @param column
     */
    public void addSlice(Item sliceItem, int index) {
        this.slices = LinkedHashMapUtils.buildLinkedMapWithNewEntry((LinkedHashMap<String, Item>) this.slices,
                sliceItem, sliceItem.getOlapElementId(), index);
    }
    
    /**
     * 添加过滤定义
     * 
     * @param column
     */
    public void addSlices(Item[] slices) {
        for (Item slice : slices) {
            addSlice(slice);
        }
    }
        
    /**
     * 添加行数据定义
     * 
     * @param row
     */
    public void addRow(Item row) {
        this.rows.put(row.getOlapElementId(), row);
    }
    
    /**
     * 添加行定义
     * 
     * @param column
     */
    public void addRow(Item rowItem, int index) {
        this.rows = LinkedHashMapUtils.buildLinkedMapWithNewEntry((LinkedHashMap<String, Item>) this.rows,
                rowItem, rowItem.getOlapElementId(), index);
    }
    
    /**
     * 添加切片定义
     * 
     * @param slice
     */
    public void addSlice(Item slice) {
        this.slices.put(slice.getOlapElementId(), slice);
    }
    
    /**
     * 删除列
     * 
     * @param column
     */
    public Item removeColumn(String column) {
        Iterator<String> keyIt = this.columns.keySet().iterator();
        String key = null;
        while (keyIt.hasNext()) {
            key = keyIt.next();
            Item item = this.columns.get(key);
            if (key.equals(column) || item.getId().equals(column)) {
                break;
            }
        }
        return this.columns.remove(key);
    }
    
    /**
     * 删除行
     * 
     * @param row
     */
    public Item removeRow(String row) {
        Iterator<String> keyIt = this.rows.keySet().iterator();
        String key = null;
        while (keyIt.hasNext()) {
            key = keyIt.next();
            Item item = this.rows.get(key);
            if (key.equals(row) || item.getId().equals(row)) {
                key = item.getOlapElementId();
                break;
            }
        }
        return this.rows.remove(key);
    }
    
    /**
     * 删除切片轴
     * 
     * @param slice
     */
    public Item removeSlice(String slice) {
        Iterator<String> keyIt = this.slices.keySet().iterator();
        String key = null;
        while (keyIt.hasNext()) {
            key = keyIt.next();
            Item item = this.slices.get(key);
            if (key.equals(slice) || item.getId().equals(slice)) {
                key = item.getOlapElementId();
                break;
            }
        }
        return this.slices.remove(key);
    }
    
    /**
     * 获取当前模型引用的所有维度、指标信息
     * 
     * @return
     */
    public Item[] getItems() {
        /**
         * 这里一定要使用linkedHashMap，保留顺序
         */
        Map<String, Item> allItems = collectItems(true);
        return allItems.values().toArray(new Item[0]);
    }
    
    /**
     * 获取当前模型引用的所有维度、指标信息
     * 
     * @return
     */
    public Item[] getItems(boolean needSelDimMeasures) {
        /**
         * 这里一定要使用linkedHashMap，保留顺序
         */
        Map<String, Item> allItems = collectItems(needSelDimMeasures);
        return allItems.values().toArray(new Item[0]);
    }

    /**
     * @return
     */
    private Map<String, Item> collectItems(boolean needSelDimMeasures) {
        Map<String, Item> allItems = new LinkedHashMap<String, Item>();
        if (needSelDimMeasures) {
            allItems.putAll(this.getSelectionDims());
            allItems.putAll(this.getSelectionMeasures());
        }
        allItems.putAll(this.columns);
        allItems.putAll(this.rows);
        allItems.putAll(this.slices);
        return allItems;
    }
    
    /**
     * 
     * @param itemId
     * @return Item
     */
    public Item getItem(String itemId) {
            // TODO 优先查找非选择轴上的，此处需要优化
        Map<String, Item> allItems = Maps.newHashMap();//collectItems(true);
        allItems.putAll(this.columns);
        allItems.putAll(this.rows);
        allItems.putAll(this.slices);
        for (Item item : allItems.values()) {
            if (item.getOlapElementId().equals(itemId)) {
                return item;
            }
        }
        allItems.clear();
        allItems.putAll(this.getSelectionDims());
        allItems.putAll(this.getSelectionMeasures());
        for (Item item : allItems.values()) {
            if (item.getOlapElementId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * getItem
     * @param itemId
     * @param reload
     * @return
     */
    public Item getItem(String itemId, boolean reload) {
        Map<String, Item> allItems = Maps.newHashMap();
        if (!reload) {
            allItems.putAll(this.columns);
            allItems.putAll(this.rows);
            allItems.putAll(this.slices);
        } else {
            allItems.putAll(this.getSelectionDims());
            allItems.putAll(this.getSelectionMeasures());
        }
        for (Item item : allItems.values()) {
            if (item.getOlapElementId().equals(itemId)) {
                return item;
            }
        }
        if (!reload) {
            reload = true;
            return getItem(itemId, reload);
        }
        return null;
    }
    
    public Item getItemByOlapElementId(String olapElementId) {
//        Map<String, Item> allItems = collectItems();
//        return allItems.get(olapElementId);
            return this.getItem(olapElementId);
    }
    
    /**
     * getItemByOlapElementId
     * @param olapElementId
     * @param posType
     * @return
     */
    public Item getItemByOlapElementId(String olapElementId,
                    PositionType posType) {
        Item result = this.getItem(olapElementId, false);
        if (result.getPositionType().equals(posType)) {
            return result;
        }
        return null;
    }
    
    public boolean containsOlapElement(String olapElementId) {
        return collectItems(true).containsKey(olapElementId);
    }

    /**
     * 
     * @param rows
     */
    public void resetRows(Item[] rows) {
        this.rows.clear();
        for (Item item : rows) {
            this.rows.put(item.getOlapElementId(), item);
        }
    }

    /**
     * 
     * @param columns
     */
    public void resetColumns(Item[] columns) {
        this.columns.clear();
        for (Item item : columns) {
            this.columns.put(item.getOlapElementId(), item);
        }
    }

    /**
     * @return the selectionDims
     */
    public Map<String, Item> getSelectionDims() {
        if (this.selectionDims == null) {
            this.selectionDims = Maps.newLinkedHashMap();
        }
        return selectionDims;
    }

    /**
     * @param selectionDims the selectionDims to set
     */
    public void setSelectionDims(Map<String, Item> selectionDims) {
        this.selectionDims = selectionDims;
    }

    /**
     * @return the selectionMeasures
     */
    public Map<String, Item> getSelectionMeasures() {
        if (this.selectionMeasures == null) {
            this.selectionMeasures = Maps.newLinkedHashMap();
        }
        return selectionMeasures;
    }

    /**
     * @param selectionMeasures the selectionMeasures to set
     */
    public void setSelectionMeasures(Map<String, Item> selectionMeasures) {
        this.selectionMeasures = selectionMeasures;
    }

    /**
     * 
     * @param oLapElementId
     * @param axisType
     * @return boolean
     */
    public boolean containsOlapElement(String olapElementId, String axisType) {
        final Item item = this.getItemByOlapElementId(olapElementId);
        if (item != null && item.getPositionType() == PositionType.valueOf(axisType)) {
            return true;
        }
        return false;
    }

    /**
     * @return the topSetting
     */
    public MeasureTopSetting getTopSetting() {
        return topSetting;
    }

    /**
     * @param topSetting the topSetting to set
     */
    public void setTopSetting(MeasureTopSetting topSetting) {
        this.topSetting = topSetting;
    }

    public void resetSlices(Item[] items) {
        this.slices.clear ();
        for (Item item : items) {
            this.slices.put (item.getOlapElementId (), item);
        }
    }
    
}
