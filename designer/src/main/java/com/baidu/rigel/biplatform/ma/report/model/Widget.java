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

/**
 * 
 * 部件定义：描述部件定义
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public class Widget implements Serializable {
    
    /**
     * serialize id
     */
    private static final long serialVersionUID = 8069202837249985161L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 部件类型：按钮、下拉树、图、表等
     */
    private String type;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
}
