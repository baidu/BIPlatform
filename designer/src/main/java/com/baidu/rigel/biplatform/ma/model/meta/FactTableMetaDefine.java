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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 * 事实表定义：用来描述事实表基本信息以及指标列信息以及不能确定的维度列信息 不能确定的列多用来作为自定义维度、退化维度的参考列，默认当作指标列处理
 * 
 * @author david.wang
 *
 */
public class FactTableMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -1519046657383458922L;
    
    /**
     * schema id
     */
    private String schemaId;
    
    /**
     * cube id
     */
    private String cubeId;
    
    /**
     * 事实表名称
     */
    private String name;
    
    /**
     * 指标列定义
     */
    private Set<ColumnMetaDefine> columns = Sets.newHashSet();
    
    /**
     * regExpTables
     */
    private List<String> regExpTables;
    
    /**
     * 描述事实表名称的正则，用于多表做事实表
     */
    private String regExp;
    
    /**
     * 分表策略
     */
    private DivideTableStrategyVo divideTableStrategyVo;
    
    /**
     * 是否是多表联合作为事实表
     */
    private boolean mutilple;
    
    /**
     * 构造函数
     */
    public FactTableMetaDefine() {
        columns = new HashSet<ColumnMetaDefine>();
    }
    
    /**
     * 
     * @return 名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @param name
     *            name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return 正则
     */
    public String getRegExp() {
        return regExp;
    }
    
    /**
     * 
     * @param regExp
     *            正则表达式
     */
    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }
    
    
    /** 
     * 获取 divideTableStrategyVo 
     * @return the divideTableStrategyVo 
     */
    public DivideTableStrategyVo getDivideTableStrategyVo() {
    
        return divideTableStrategyVo;
    }

    /** 
     * 设置 divideTableStrategyVo 
     * @param divideTableStrategyVo the divideTableStrategyVo to set 
     */
    public void setDivideTableStrategyVo(DivideTableStrategyVo divideTableStrategyVo) {
    
        this.divideTableStrategyVo = divideTableStrategyVo;
    }

    /** 
     * 获取 serialversionuid 
     * @return the serialversionuid 
     */
    public static long getSerialversionuid() {
    
        return serialVersionUID;
    }

    /** 
     * 设置 columns 
     * @param columns the columns to set 
     */
    public void setColumns(Set<ColumnMetaDefine> columns) {
    
        this.columns = columns;
    }

    /**
     * 
     * @return 是否是正则匹配
     */
    public boolean isMutilple() {
        return mutilple;
    }
    
    /**
     * 
     * @param mutilple
     *            是否是正则匹配
     */
    public void setMutilple(boolean mutilple) {
        this.mutilple = mutilple;
    }
    
    /**
     * 添加指标列描述信息
     * 
     * @param column
     *            列定义
     */
    public void addColumn(ColumnMetaDefine column) {
        this.columns.add(column);
    }
    
    /**
     * 
     * @return 列定义
     */
    public ColumnMetaDefine[] getColumns() {
        return columns.toArray(new ColumnMetaDefine[0]);
    }
    
    /**
     * 新增方法，方便stream api调用
     * @return
     */
    public Set<ColumnMetaDefine> getColumnList() {
        return Collections.unmodifiableSet(columns);
    }
    
    /**
     * 通过列名获取列定义
     * @param columnName
     * @return
     */
    public ColumnMetaDefine getColumnMetaDefineByColumnName(String columnName) {
        for (ColumnMetaDefine columnMetaDefine : this.columns) {
            if (columnMetaDefine.getName().equals(columnName)) {
                return columnMetaDefine;
            }
        }
        return null;
    }
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
     */
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
    
    /**
     * 
     * @return cube id
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((cubeId == null) ? 0 : cubeId.hashCode());
        result = prime * result + (mutilple ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((regExp == null) ? 0 : regExp.hashCode());
        result = prime * result + ((regExpTables == null) ? 0 : regExpTables.hashCode());
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
        FactTableMetaDefine other = (FactTableMetaDefine) obj;
        if (columns == null) {
            if (other.columns != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(columns, other.columns)) {
            return false;
        }
        if (cubeId == null) {
            if (other.cubeId != null) {
                return false;
            }
        } else if (!cubeId.equals(other.cubeId)) {
            return false;
        }
        if (mutilple != other.mutilple) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (regExp == null) {
            if (other.regExp != null) {
                return false;
            }
        } else if (!regExp.equals(other.regExp)) {
            return false;
        }
        if (regExpTables == null) {
            if (other.regExpTables != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(regExpTables, other.regExpTables)) {
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
     * get the regExpTables
     * @return the regExpTables
     */
    public List<String> getRegExpTables() {
        if (regExpTables == null) {
            regExpTables = Lists.newArrayList();
        }
        return regExpTables;
    }

    /**
     * set the regExpTables
     * @param regExpTables the regExpTables to set
     */
    public void setRegExpTables(List<String> regExpTables) {
        this.regExpTables = regExpTables;
    }
    
    public void clearColumns() {
        columns.clear();
    }
}
