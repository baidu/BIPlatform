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
 * 关联表定义
 * @author zhongyi
 *
 *         2014-7-31
 */
public class RelationTableView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3694223714963995063L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * 关联表名称
     */
    private String name;
    
    /**
     * 关联列定义
     */
    private List<ColumnInfo> fields;
    
    /**
     * get the id
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * set the id
     * 
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
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
     * get the fields
     * 
     * @return the fields
     */
    public List<ColumnInfo> getFields() {
        return fields;
    }
    
    /**
     * set the fields
     * 
     * @param fields
     *            the fields to set
     */
    public void setFields(List<ColumnInfo> fields) {
        this.fields = fields;
    }
}