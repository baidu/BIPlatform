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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;

/**
 * 平面表问题模型
 * 
 * @author 罗文磊
 *
 */
public class PlaneTableQuestionModel extends QuestionModel implements Serializable {

    /**
     * default generate id
     */
    private static final long serialVersionUID = 1576666141762101245L;
    
    /**
     * dataSourceInfo 问题模型对应的数据源信息（有数据源的key优先从缓存取）
     */
    private DataSourceInfo dataSourceInfo;
    
    /**
     * source,需要查找的表，多事实表已逗号分隔
     */
    private String source;
    
    /**
     * 元数据信息
     */
    private Map<String, Column> metaMap;
    
    /**
     * axisMetas 轴上的元数据信息
     */
    private List<String> selection;
    
    /**
     * 是否生成总页数
     */
    private boolean generateTotalSize = false;
    
    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }
    
    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    /**
     * @return the dataSourceInfo
     */
    public DataSourceInfo getDataSourceInfo() {
        return dataSourceInfo;
    }
    
    /**
     * @param dataSourceInfo
     *            the dataSourceInfo to set
     */
    public void setDataSourceInfo(DataSourceInfo dataSourceInfo) {
        this.dataSourceInfo = dataSourceInfo;
    }
    
    /**
     * @return the metaMap
     */
    public Map<String, Column> getMetaMap() {
        return metaMap;
    }
    
    /**
     * @param metaMap
     *            the metaMap to set
     */
    public void setMetaMap(Map<String, Column> metaMap) {
        this.metaMap = metaMap;
    }
    
    /**
     * @return the selection
     */
    public List<String> getSelection() {
        return selection;
    }
    
    /**
     * @param selection
     *            the selection to set
     */
    public void setSelection(List<String> selection) {
        this.selection = selection;
    }
    
    /**
     * default generate get generateTotalSize
     * 
     * @return the generateTotalSize
     */
    public boolean isGenerateTotalSize() {
        return generateTotalSize;
    }
    
    /**
     * default generate set generateTotalSize
     * 
     * @param generateTotalSize
     *            the generateTotalSize to set
     */
    public void setGenerateTotalSize(boolean generateTotalSize) {
        this.generateTotalSize = generateTotalSize;
    }
    
}