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

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * general cube level
 * 
 * @author xiaoming.chen
 * 
 */
@JsonIgnoreProperties
public class MiniCubeLevel extends OlapElementDef implements Level {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 606138737233837292L;

    /**
     * dimension level 所属维度
     */
    @JsonIgnore
    @GsonIgnore
    private Dimension dimension;

    /**
     * source 获取level下member的id
     */
    private String source;

    /**
     * 维值的名称对应字段
     */
    private String captionColumn;

    /**
     * parent 父子层级的维度需要
     */
    private String parent;

    /**
     * type 层级类型
     */
    private LevelType type = LevelType.REGULAR;

    /**
     * nullParentValue 根节点的值，默认为null，只有父子节点有效
     */
    private String nullParentValue;

    /**
     * 维度表名称
     */
    private String dimTable;

    /**
     * 关联的事实表中的列
     */
    private String factTableColumn;
    
    /**
     * construct with level name
     * 
     * @param name 获取层级的字段或者名称
     */
    public MiniCubeLevel(String name) {
        super(name);
    }

    public MiniCubeLevel() {
        super(null);
    }

    @Override
    public LevelType getType() {
        return this.type;
    }

    /**
     * 返回是否是父子结构层级
     * 
     * @return 是否是父子结构层级
     */
    public boolean isParentChildLevel() {
        if (StringUtils.isNotBlank(parent)) {
            return true;
        }
        return false;
    }

    @Override
    @JsonIgnore
    public Dimension getDimension() {
        return this.dimension;
    }

    /**
     * getter method for property source
     * 
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * setter method for property source
     * 
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * getter method for property parent
     * 
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * setter method for property parent
     * 
     * @param parent the parent to set
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * setter method for property dimension
     * 
     * @param dimension the dimension to set
     */
    @JsonIgnore
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     * setter method for property type
     * 
     * @param type the type to set
     */
    public void setType(LevelType type) {
        this.type = type;
    }

    /**
     * getter method for property nullParentValue
     * 
     * @return the nullParentValue
     */
    public String getNullParentValue() {
        return nullParentValue;
    }

    /**
     * setter method for property nullParentValue
     * 
     * @param nullParentValue the nullParentValue to set
     */
    public void setNullParentValue(String nullParentValue) {
        this.nullParentValue = nullParentValue;
    }

    @Override
    public String getDimTable() {
        return this.dimTable;
    }

    public void setDimTable(String dimTable) {
        this.dimTable = dimTable;
    }

    /**
     * @return the captionColumn
     */
    public String getCaptionColumn() {
        return captionColumn;
    }

    /**
     * @param captionColumn the captionColumn to set
     */
    public void setCaptionColumn(String captionColumn) {
        this.captionColumn = captionColumn;
    }

    /**
     * @return the factTableColumn
     */
    public String getFactTableColumn() {
        return factTableColumn;
    }

    /**
     * @param factTableColumn the factTableColumn to set
     */
    public void setFactTableColumn(String factTableColumn) {
        this.factTableColumn = factTableColumn;
    }

}
