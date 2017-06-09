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
/**
 * 
 */
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.exception.MiniCubeException;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 自定义的层级取数 自定义的支持3种，支持以下类型：
 * 
 * @see com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType
 * 
 * 
 *      SQL：在select中必须有name,caption 如：select trade_id name,trade_name caption from table where xxxx
 *      FILE:文件按列获取，第一列表示维值的唯一标识，第二列为显示名称，如果只有一列显示名称和唯一名称一致， 输入文件地址，如：http://abc.dd.com/aa.txt、/home/work/local/dim.txt
 *      CUSTOM:待实现，用户自定义取数逻辑
 * @author xiaoming.chen
 *
 */
@JsonIgnoreProperties
public class UserCustomLevel extends OlapElementDef implements Level {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5532174807311453650L;

    /**
     * type 层级类型
     */
    private LevelType type = LevelType.USER_CUSTOM;

    /**
     * dimension 层级所属维度
     */
    @JsonIgnore
    @GsonIgnore
    private Dimension dimension;

    /**
     * userCustomType 该级别的自定义取数方式
     */
    private DataSourceType userCustomType;

    /**
     * customParams 取数的参数
     */
    private Map<String, String> customParams;

    /**
     * 自定义级别的定义 比如：sql 文件路径等
     */
    private String value;

    /**
     * factTableColumn 事实表关联字段
     */
    private String factTableColumn;

    /**
     * 需要指定维度在事实表字段的构造方法
     * 
     * @param name 维度在事实表对应字段
     */
    public UserCustomLevel(String name) {
        super(name);
    }

    public UserCustomLevel() {
        super(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Level#getType()
     */
    @Override
    public LevelType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Level#getDimension()
     */
    @Override
    @JsonIgnore
    public Dimension getDimension() {
        return this.dimension;
    }

    /**
     * getter method for property userCustomType
     * 
     * @return the userCustomType
     */
    public DataSourceType getUserCustomType() {
        return userCustomType;
    }

    /**
     * setter method for property userCustomType
     * 
     * @param userCustomType the userCustomType to set
     */
    public void setUserCustomType(DataSourceType userCustomType) {
        this.userCustomType = userCustomType;
    }

    /**
     * getter method for property customParams
     * 
     * @return the customParams
     */
    public Map<String, String> getCustomParams() {
        return customParams;
    }

    /**
     * setter method for property customParams
     * 
     * @param customParams the customParams to set
     */
    public void setCustomParams(Map<String, String> customParams) {
        this.customParams = customParams;
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
     * setter method for property dimension
     * 
     * @param dimension the dimension to set
     */
    @JsonIgnore
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public String getDimTable() {
        throw new MiniCubeException();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomLevel clone() throws CloneNotSupportedException {
        return DeepcopyUtils.deepCopy(this);
    }

    @Override
    public String getFactTableColumn() {
        return this.factTableColumn;
    }

    /**
     * 设置事实表关联字段
     * 
     * @param factTableColumn
     */
    public void setFactTableColumn(String factTableColumn) {
        this.factTableColumn = factTableColumn;
    }
}
