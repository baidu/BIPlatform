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
import java.util.Map;

/**
 * 
 * 维度配置绑定关系
 * @author zhongyi
 *
 *         2014-7-31
 */
public class DimBindConfigView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2454251843468166530L;
    
    /**
     * 关联关系表定义
     */
    private List<RelationTableView> relationTables;
    
    /**
     * 日期关联表关系定义
     */
    private List<DateRelationTableView> dateRelationTables;
    
    /**
     * 立方体映射定义
     */
    private Map<String, CubeView> cubes;
    
    /**
     * 维度映射
     */
    private DimBindView dim;
    
    /**
     * get the relationTables
     * 
     * @return the relationTables
     */
    public List<RelationTableView> getRelationTables() {
        return relationTables;
    }
    
    /**
     * set the relationTables
     * 
     * @param relationTables
     *            the relationTables to set
     */
    public void setRelationTables(List<RelationTableView> relationTables) {
        this.relationTables = relationTables;
    }
    
    /**
     * get the dateRelationTables
     * 
     * @return the dateRelationTables
     */
    public List<DateRelationTableView> getDateRelationTables() {
        return dateRelationTables;
    }
    
    /**
     * set the dateRelationTables
     * 
     * @param dateRelationTables
     *            the dateRelationTables to set
     */
    public void setDateRelationTables(List<DateRelationTableView> dateRelationTables) {
        this.dateRelationTables = dateRelationTables;
    }
    
    /**
     * get the cubes
     * 
     * @return the cubes
     */
    public Map<String, CubeView> getCubes() {
        return cubes;
    }
    
    /**
     * set the cubes
     * 
     * @param cubes
     *            the cubes to set
     */
    public void setCubes(Map<String, CubeView> cubes) {
        this.cubes = cubes;
    }
    
    /**
     * get the dim
     * 
     * @return the dim
     */
    public DimBindView getDim() {
        return dim;
    }
    
    /**
     * set the dim
     * 
     * @param dim
     *            the dim to set
     */
    public void setDim(DimBindView dim) {
        this.dim = dim;
    }
    
}