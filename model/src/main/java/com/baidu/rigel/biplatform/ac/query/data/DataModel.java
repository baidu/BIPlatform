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
package com.baidu.rigel.biplatform.ac.query.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 结果集
 * 
 */
public class DataModel implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8762333585798457136L;

    /**
     * 数据处理类型
     **/
    public static enum FillDataType {
        ROW, COLUMN
    }

    /**
     * 基于行的表信息
     */
    private List<HeadField> rowHeadFields = new ArrayList<HeadField>();

    /**
     * 基于列的表信息
     */
    private List<HeadField> columnHeadFields = new ArrayList<HeadField>();

    /**
     * 基于列存放的表格数据，外层是 列的数目，内存List是每一列包含的行上的数据
     */
    private List<List<BigDecimal>> columnBaseData = new ArrayList<List<BigDecimal>>();
    
    /**
     * 二维表数据模型，透视表可以不设置
     */
    private TableData tableData;

    /**
     * operateIndex 排序的列
     */
    private int operateIndex;
    
    /**
     * 结果集原始大小
     */
    private int recordSize;
    
    /**
     * 非数据模型内容
     */
    private String others;

    /**
     * default generate get rowHeadFields
     * 
     * @return the rowHeadFields
     */
    public List<HeadField> getRowHeadFields() {
        if (this.rowHeadFields == null) {
            this.rowHeadFields = new ArrayList<HeadField>();
        }
        return rowHeadFields;
    }

    /**
     * default generate set rowHeadFields
     * 
     * @param rowHeadFields the rowHeadFields to set
     */
    public void setRowHeadFields(List<HeadField> rowHeadFields) {
        this.rowHeadFields = rowHeadFields;
    }

    /**
     * default generate get columnHeadFields
     * 
     * @return the columnHeadFields
     */
    public List<HeadField> getColumnHeadFields() {
        if (this.columnHeadFields == null) {
            this.columnHeadFields = new ArrayList<HeadField>();
        }
        return columnHeadFields;
    }

    /**
     * default generate set columnHeadFields
     * 
     * @param columnHeadFields the columnHeadFields to set
     */
    public void setColumnHeadFields(List<HeadField> columnHeadFields) {
        this.columnHeadFields = columnHeadFields;
    }

    /**
     * default generate get columnBaseData
     * 
     * @return the columnBaseData
     */
    public List<List<BigDecimal>> getColumnBaseData() {
        return columnBaseData;
    }

    /**
     * default generate set columnBaseData
     * 
     * @param columnBaseData the columnBaseData to set
     */
    public void setColumnBaseData(List<List<BigDecimal>> columnBaseData) {
        this.columnBaseData = columnBaseData;
    }

    /**
     * default generate get operateIndex
     * 
     * @return the operateIndex
     */
    public int getOperateIndex() {
        return operateIndex;
    }

    /**
     * default generate set operateIndex
     * 
     * @param operateIndex the operateIndex to set
     */
    public void setOperateIndex(int operateIndex) {
        this.operateIndex = operateIndex;
    }

    
    /**
     * @return the recordSize
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * @param recordSize the recordSize to set
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DataModel [rowHeadFields=" + rowHeadFields + ", columnHeadFields=" + columnHeadFields
                + ", columnBaseData=" + columnBaseData + ", operateIndex=" + operateIndex + "]";
    }
    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    /**
     * @return the tableData
     */
    public TableData getTableData() {
        return tableData;
    }

    /**
     * @param tableData the tableData to set
     */
    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    
}
