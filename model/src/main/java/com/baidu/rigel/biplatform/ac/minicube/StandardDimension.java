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

import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;

/**
 * 
 * 标准维度定义
 * 
 * @author david.wang
 *
 */
public class StandardDimension extends MiniCubeDimension {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6761451919163111341L;

    /**
     * 维度表 如果是退化为或者用户自定义维度 该属性值为空
     */
    private String tableName;

    /**
     * 维度类型
     */
    private DimensionType type = DimensionType.STANDARD_DIMENSION;

    // /**
    // * 级别信息
    // */
    // private Map<String, Level> levels = new LinkedHashMap<String, Level>();

    public StandardDimension(String name) {
        super(name);
    }

    public StandardDimension() {
        super(null);
    }

    // @Override
    // public DimensionType getType() {
    // return DimensionType.STANDARD_DIMENSION;
    // }

    // @Override
    // public Map<String, Level> getLevels() {
    // return this.levels;
    // }

    @Override
    public boolean isTimeDimension() {
        return false;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 
     * @param groupDimension
     */
    public void setType(DimensionType type) {
        this.type = type;
    }

    // public void setLevels(Map<String, Level> levels) {
    // this.levels = levels;
    // }

    public DimensionType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String split = ",";
        StringBuilder rs = new StringBuilder();
        rs.append("{ id : " + this.getId() + split + " name : " + this.getName());
        rs.append(split + "type : " + this.getType() + "," + " levels :[");
        if (this.levels == null) {
            rs.append("{null}");
        }
        if (this.levels != null) {
                for (Level level : this.levels.values()) {
                    rs.append(level.toString());
                }
        }
        rs.append("]}");
        return rs.toString();
    }
}
