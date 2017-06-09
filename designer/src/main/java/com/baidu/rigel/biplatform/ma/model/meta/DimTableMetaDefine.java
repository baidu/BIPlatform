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
package com.baidu.rigel.biplatform.ma.model.meta;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.google.common.collect.Sets;

/**
 * 
 * 维度表元数据定义：用于描述星型模型中维度表的定义信息，包含：维度表的列信息， 维度表的属性信息，维度表的扩展信息等
 * 
 * 
 * @author david.wang
 *
 */
public abstract class DimTableMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -5151316866400344913L;
    
    /**
     * 维度表中列的定义，如果主键列为与事实表关联列或者无实际业务含义，不需要包含主键列 如果是退化维度，则列定义为退化维度列
     * 如果是用户自定义维度，则为与事实表关联列
     */
    private Set<ColumnMetaDefine> columns;
    
    /**
     * 维度表名称，如果是退化维度、自定义维度，名称为事实表名称
     */
    private String name;
    
    /**
     * 引用关系定义
     */
    private ReferenceDefine reference;
    
    /**
     * 构造函数
     */
    public DimTableMetaDefine() {
    }
    
    /**
     * 获取维度中定义的列
     * 
     * @return 维度中定义的列
     */
    public ColumnMetaDefine[] getColumns() {
        if (columns == null) {
            columns = Sets.newLinkedHashSet();
        }
        return columns.toArray(new ColumnMetaDefine[0]);
    }
    
    /**
     * 添加列定义
     * 
     * @param column
     *            列定义信息
     */
    public void addColumn(ColumnMetaDefine column) {
        if (columns == null) {
            columns = Sets.newLinkedHashSet();
        }
        columns.add(column);
    }
    
    /**
     * 批量添加列定义
     * 
     * @param columns
     *            列定义信息
     */
    public void addColumns(List<ColumnMetaDefine> columns) {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        if (this.columns == null) {
            this.columns = Sets.newLinkedHashSet();
        }
        this.columns.addAll(columns);
    }
    
    /**
     * 
     * @return 维度名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置维度表名称，如果是退化维度、自定义维度，名称为事实表名称
     * 
     * @param name
     *            维度名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return 参照信息
     */
    public ReferenceDefine getReference() {
        return reference;
    }
    
    /**
     * 设置维度表和事实表之间的关联关系
     * 
     * @param reference
     *            参照信息
     */
    public void setReference(ReferenceDefine reference) {
        this.reference = reference;
    }
    
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DimTableMetaDefine other = (DimTableMetaDefine) obj;
        if (columns == null) {
            if (other.columns != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(columns, other.columns)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
    
    
    /**
     * 
     * 获取维度表中维度类型，同一维度表维度类型一致
     * 
     * @return ? extends DimType
     * @see DimType 维度类型
     */
    public abstract <T extends DimType> T getDimType();
    
}
