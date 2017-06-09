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
package com.baidu.rigel.biplatform.ma.resource.view;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;

/**
 * 
 * 立方体映射关系定义
 * @author zhongyi
 *
 *         2014-7-31
 */
public class CubeView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6129194535028630009L;
    
    /**
     * name
     */
    private String name;
    
    /**
     * currDims
     */
    private List<ColumnInfo> currDims;
    
    /**
     * allFields
     */
    private List<ColumnInfo> allFields;
    
    /**
     * get the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * set the name
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * get the currDims
     * 
     * @return the currDims
     */
    public List<ColumnInfo> getCurrDims() {
        return currDims;
    }
    
    /**
     * set the currDims
     * 
     * @param currDims
     *            the currDims to set
     */
    public void setCurrDims(List<ColumnInfo> currDims) {
        this.currDims = currDims;
    }
    
    /**
     * get the allFields
     * 
     * @return the allFields
     */
    public List<ColumnInfo> getAllFields() {
        return allFields;
    }
    
    /**
     * set the allFields
     * 
     * @param allFields
     *            the allFields to set
     */
    public void setAllFields(List<ColumnInfo> allFields) {
        this.allFields = allFields;
    }
    
}