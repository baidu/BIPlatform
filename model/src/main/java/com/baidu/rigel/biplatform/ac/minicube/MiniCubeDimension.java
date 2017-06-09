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
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.LinkedHashMap;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.google.common.collect.Maps;

/**
 * 
 * 唯独定义
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public abstract class MiniCubeDimension extends OlapElementDef implements Dimension {

    /**
     * serialize id
     */
    private static final long serialVersionUID = 941367956116980693L;

    /**
     * 级别定义
     */
    protected LinkedHashMap<String, Level> levels = new LinkedHashMap<String, Level>();

    /**
     * 事实表与维度表关联的外键列
     */
    private String facttableColumn;
    
    /**
     * facttableCaption
     */
    private String facttableCaption;

    /**
     * 构造函数
     * 
     * @param name 维度名称
     */
    public MiniCubeDimension(String name) {
        super(name);
    }

    public LinkedHashMap<String, Level> getLevels() {
        if (this.levels == null) {
            this.levels = Maps.newLinkedHashMap ();
        }
        return levels;
    }

    /**
     * 添加级别定义
     * 
     * @param level 级别定义
     */
    public void addLevel(Level level) {
        this.levels.put(level.getId(), level);
    }

    /**
     * 删除levle定义
     */
    public void clearLevels() {
        this.levels = new LinkedHashMap<String, Level>();
    }

    public String getFacttableColumn() {
        return facttableColumn;
    }

    public void setFacttableColumn(String facttableColumn) {
        this.facttableColumn = facttableColumn;
    }

    /**
     * set levels with levels
     * 
     * @param levels the levels to set
     */
    public void setLevels(LinkedHashMap<String, Level> levels) {
        this.levels = levels;
    }

    /**
     * @return the facttableCaption
     */
    public String getFacttableCaption() {
        return facttableCaption;
    }

    /**
     * @param facttableCaption the facttableCaption to set
     */
    public void setFacttableCaption(String facttableCaption) {
        this.facttableCaption = facttableCaption;
    }

    
}
