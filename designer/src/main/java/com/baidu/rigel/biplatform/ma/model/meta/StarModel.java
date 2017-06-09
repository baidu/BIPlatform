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

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.google.common.collect.Lists;

/**
 * 
 * 星型模型：构建报表逻辑模型的基础，做为报表逻辑模型(Schema)与数据库元数据定转换的桥梁。
 * 
 * @author david.wang
 *
 */
public class StarModel implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -8323051566750264132L;
    
    /**
     * schema Id
     */
    private String schemaId;
    
    /**
     * cube Id
     */
    private String cubeId;
    
    /**
     * 事实表定义
     */
    private FactTableMetaDefine factTable;
    
    /**
     * 维度表定义
     */
    private List<DimTableMetaDefine> dimTables;
    
    /**
     * 数据源id信息
     */
    private String dsId;
    
    /**
     * 
     * @return schema id
     */
    public String getSchemaId() {
        return schemaId;
    }
    
    /**
     * 
     * @param schemaId
     *            schema id
     */
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
    
    /**
     * 
     * @return 立方体id
     */
    public String getCubeId() {
        return cubeId;
    }
    
    /**
     * 
     * @param cubeId
     *            cube id
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }
    
    /**
     * 
     * @return 事实表
     */
    public FactTableMetaDefine getFactTable() {
        return factTable;
    }
    
    /**
     * 
     * @param factTable
     *            事实表名称
     */
    public void setFactTable(FactTableMetaDefine factTable) {
        this.factTable = factTable;
    }
    
    /**
     * 
     * @return 维度表定义
     */
    public List<DimTableMetaDefine> getDimTables() {
        if (dimTables == null) {
            dimTables = Lists.newArrayList();
        }
        return dimTables;
    }
    
    public void addDimTable(DimTableMetaDefine dimTable) {
        if (dimTables == null) {
            dimTables = Lists.newArrayList();
        }
        dimTables.add(dimTable);
    }
    
    public void addDimTables(List<? extends DimTableMetaDefine> newDimTables) {
        if (dimTables == null) {
            dimTables = Lists.newArrayList();
        }
        dimTables.addAll(newDimTables);
    }
    
    /**
     * 
     * @param dimTables
     *            维度表
     */
    public void setDimTables(List<DimTableMetaDefine> dimTables) {
        this.dimTables = dimTables;
    }
    
    /**
     * 
     * @return 数据源id
     */
    public String getDsId() {
        return dsId;
    }
    
    /**
     * 
     * @param dsId
     *            数据源id
     */
    public void setDsId(String dsId) {
        this.dsId = dsId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cubeId == null) ? 0 : cubeId.hashCode());
        result = prime * result + ((dimTables == null) ? 0 : dimTables.hashCode());
        result = prime * result + ((dsId == null) ? 0 : dsId.hashCode());
        result = prime * result + ((factTable == null) ? 0 : factTable.hashCode());
        result = prime * result + ((schemaId == null) ? 0 : schemaId.hashCode());
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
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
        StarModel other = (StarModel) obj;
        if (cubeId == null) {
            if (other.cubeId != null) {
                return false;
            }
        } else if (!cubeId.equals(other.cubeId)) {
            return false;
        }
        if (dimTables == null) {
            if (other.dimTables != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(dimTables, other.dimTables)) {
            return false;
        }
        if (dsId == null) {
            if (other.dsId != null) {
                return false;
            }
        } else if (!dsId.equals(other.dsId)) {
            return false;
        }
        if (factTable == null) {
            if (other.factTable != null) {
                return false;
            }
        } else if (!factTable.equals(other.factTable)) {
            return false;
        }
        if (schemaId == null) {
            if (other.schemaId != null) {
                return false;
            }
        } else if (!schemaId.equals(other.schemaId)) {
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
    
}
