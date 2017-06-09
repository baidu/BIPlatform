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
package com.baidu.rigel.biplatform.ma.report.query.pivottable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 数据母体。
 * 
 * @author mengran
 * 
 */
public class PivotTable extends BaseTable implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * colFields
     */
    private List<List<ColField>> colFields = new ArrayList<List<ColField>>();
    
    /**
     * colDefine
     */
    private List<ColDefine> colDefine = new ArrayList<ColDefine>();
    
    /**
     * rowHeadFields
     */
    private List<List<RowHeadField>> rowHeadFields = new ArrayList<List<RowHeadField>>();
    
    /**
     * rowDefine
     */
    private List<RowDefine> rowDefine = new ArrayList<RowDefine>();
    
    /**
     * dataSourceRowBased
     */
    private List<List<CellData>> dataSourceRowBased = new ArrayList<List<CellData>>();
    
    /**dataSourceColumnBased
     * 
     */
    private List<List<CellData>> dataSourceColumnBased = new ArrayList<List<CellData>>();
    
    /**
     * dataRows
     */
    private int dataRows;
    
    /**
     * dataColumns
     */
    private int dataColumns;
    
    /**
     * colHeadHeight
     */
    private int colHeadHeight;
    
    /**
     * rowHeadWidth
     */
    private int rowHeadWidth;
    
    /**
     * actualSize
     */
    private int actualSize;
    
    /**
     * filterBlankRowCount 被过滤的空白行
     */
    private int filterBlankRowCount;

    /**
     * 此处内容非报表模型定义内容
     */
    private String others;
    
    /**
     * @return the colFields
     */
    public List<List<ColField>> getColFields() {
        return colFields;
    }
    
    /**
     * @param colFields
     *            the colFields to set
     */
    public void setColFields(List<List<ColField>> colFields) {
        this.colFields = colFields;
    }
    
    /**
     * @return the colDefine
     */
    public List<ColDefine> getColDefine() {
        return colDefine;
    }
    
    /**
     * @param colDefine
     *            the colDefine to set
     */
    public void setColDefine(List<ColDefine> colDefine) {
        this.colDefine = colDefine;
    }
    
    /**
     * @return the rowHeadFields
     */
    public List<List<RowHeadField>> getRowHeadFields() {
        return rowHeadFields;
    }
    
    /**
     * @param rowHeadFields
     *            the rowHeadFields to set
     */
    public void setRowHeadFields(List<List<RowHeadField>> rowHeadFields) {
        this.rowHeadFields = rowHeadFields;
    }
    
    public List<List<CellData>> getDataSourceRowBased() {
        return dataSourceRowBased;
    }
    
    public void setDataSourceRowBased(List<List<CellData>> dataSourceRowBased) {
        this.dataSourceRowBased = dataSourceRowBased;
    }
    
    public List<List<CellData>> getDataSourceColumnBased() {
        return dataSourceColumnBased;
    }
    
    public void setDataSourceColumnBased(List<List<CellData>> dataSourceColumnBased) {
        this.dataSourceColumnBased = dataSourceColumnBased;
    }
    
    public int getDataRows() {
        return dataRows;
    }
    
    public void setDataRows(int dataRows) {
        this.dataRows = dataRows;
    }
    
    public int getDataColumns() {
        return dataColumns;
    }
    
    public void setDataColumns(int dataColumns) {
        this.dataColumns = dataColumns;
    }
    
    public int getColHeadHeight() {
        return colHeadHeight;
    }
    
    public void setColHeadHeight(int colHeadHeight) {
        this.colHeadHeight = colHeadHeight;
    }
    
    public int getRowHeadWidth() {
        return rowHeadWidth;
    }
    
    public void setRowHeadWidth(int rowHeadWidth) {
        this.rowHeadWidth = rowHeadWidth;
    }
    
    public List<RowDefine> getRowDefine() {
        return rowDefine;
    }
    
    public void setRowDefine(List<RowDefine> rowDefine) {
        this.rowDefine = rowDefine;
    }
    
    public int getActualSize() {
        return actualSize;
    }
    
    public void setActualSize(int actualSize) {
        this.actualSize = actualSize;
    }
    
    /**
     * 
     * @param uniqRowName
     * @return
     */
    public Map<String, String> rowDimValues(String uniqRowName) {
        Map<String, String> dimValues = Maps.newHashMap();
        return dimValues;
    }
    
    /**
     * getter method for property filterBlankRowCount
     * 
     * @return the filterBlankRowCount
     */
    public int getFilterBlankRowCount() {
        return filterBlankRowCount;
    }
    
    /**
     * setter method for property filterBlankRowCount
     * 
     * @param filterBlankRowCount
     *            the filterBlankRowCount to set
     */
    public void setFilterBlankRowCount(int filterBlankRowCount) {
        this.filterBlankRowCount = filterBlankRowCount;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }
    
    
}
