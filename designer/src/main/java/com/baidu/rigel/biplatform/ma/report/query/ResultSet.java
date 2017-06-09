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
import java.util.List;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;

/**
 * 
 * 报表查询结果集
 * @author david.wang
 * @version 1.0.0.1
 */
public class ResultSet implements Serializable {
    
    /**
     * ResultSet.java -- long
     * description:
     */
    private static final long serialVersionUID = 3256474758462396877L;

    /**
     * 行
     */
    private int rowCount;
    
    /**
     * 列
     */
    private int columnCount;
    
    /**
     * 数据模型，兼容老系统
     */
    private DataModel dataModel;
    
    /**
     * 数据信息
     */
    private List<List<Cell>> datas;
    
    /**
     * 行头
     */
    private List<Head> columnHeaders;
    
    /**
     * 列头
     */
    private List<Head> rowHeaders;
    
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
    
    /**
     * 获取结果集的行数信息
     * 
     * @return 结果行数
     */
    public int getRowCount() {
        return this.rowCount;
    }
    
    /**
     * 
     * @return 结果列数
     */
    public int getColumnCount() {
        return this.columnCount;
    }
    
    /**
     * 
     * @return 具体数据内容 第一维为行 第二维为列
     * 
     */
    public List<List<Cell>> getDatas() {
        return this.datas;
    }
    
    /**
     * 
     * 设置数据信息
     * 
     * @param datas
     * 
     */
    public void setDatas(List<List<Cell>> datas) {
        this.datas = datas;
    }
    
    /**
     * 
     * @return 列头 包括列头定义以及列头内容
     */
    public List<Head> getColumnHeaders() {
        return this.columnHeaders;
    }
    
    /**
     * 设置列头信息
     * 
     * @param columnHeaders
     */
    public void setColumnHeaders(List<Head> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }
    
    /**
     * 设置行头信息
     * 
     * @param rowHeaders
     */
    public void setRowHaders(List<Head> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }
    
    /**
     * 
     * @return 行头 包括行头定义以及行头数据
     */
    public List<Head> getRowHeaders() {
        return this.rowHeaders;
    }
    
    /**
     * 
     * @return
     */
    public DataModel getDataModel() {
        return this.dataModel;
    }
    
    /**
     * 
     * @param dataModel
     */
    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }
    
}
