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
package com.baidu.rigel.biplatform.ac.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 维度接口：描述同一业务主题的不同的角度（该主题的属性）。 如：在某一时间范围内某一线上产品的访问量。该业务主题拥有三个属性： 时间、产品、以及访问量。访问量可以量化的评价产品的好坏，因此，在多维分析中
 * 将访问量定义为特殊的维度Measures（指标或者度量），而时间、产品则是确定 衡量标准的前置条件，也即问题模型中的维度定义。
 * 
 * @author xiaoming.chen
 * @see java.lang.Cloneable
 */
@JsonIgnoreProperties
public interface Dimension extends OlapElement, Cloneable {

    /**
     * ALL_DIMENSION_NAME_PATTERN 默认的all节点名称
     */
    String ALL_DIMENSION_NAME_PATTERN = "All_%ss";

    /**
     * 返回维度类型
     * 
     * @return 维度类型
     */
    DimensionType getType();

    /**
     * 
     * 返回维度的层级（如果是Callback和custom类型维度，每个获取到的level是不一样的）
     * 
     * @return 维度层级
     */
    Map<String, Level> getLevels();

    /**
     * 是否是时间维度
     * 
     * @return 时间维度
     */
    boolean isTimeDimension();

    // /**
    // * 是否是Callback维度
    // *
    // * @return Callback维度
    // */
    // boolean isCallbackDimension();

    /**
     * 获取维度表名称
     * 
     * @return 维度表名称
     */
    String getTableName();

    /**
     * 
     * @return 事实表与维度表关联的外键列
     */
    String getFacttableColumn();
    
    /**
     * 
     * @return 事实表外键列的caption定义
     */
    String getFacttableCaption();

    /**
     * 维度表与事实表关联的列列名，默认为维度表主键
     * 
     * @return 维度表与事实表关联的key
     */
    String getPrimaryKey();

    @Override
    @JsonIgnore
    default public String getUniqueName() {
        return MetaNameUtil.makeUniqueName(getName());
    }

    /**
     * 获取维度的all节点
     * 
     * @return 返回维度的all节点
     */
    @JsonIgnore
    default public Member getAllMember() {
        MiniCubeMember member = new MiniCubeMember(String.format(ALL_DIMENSION_NAME_PATTERN, this.getName()));
        List<Level> levels = new ArrayList<Level>(getLevels().values());
        member.setLevel(levels.get(0));
        return member;
    }
}
