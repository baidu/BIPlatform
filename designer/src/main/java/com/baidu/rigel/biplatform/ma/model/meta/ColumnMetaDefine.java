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

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;

/**
 * 
 * 列元数据定义信息：定义列的属性信息，用于描述逻辑模型(Cube的维度定义)中列的定义描述
 * 
 * @author david.wang
 *
 */
public class ColumnMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -8727178516560114210L;
    
    /**
     * 显示名称
     */
    private String caption;
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 数据库中字段类型
     */
    private String type;
    
    /**
     * 
     * @return 显示名称
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * 
     * @param caption
     *            显示名称
     */
    public void setCaption(String caption) {
        this.caption = caption;
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
     *            列名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return 数据类型
     * @see DataType
     */
    public String getType() {
        return type;
    }
    
    /**
     * 
     * @param type
     *            DataType 数据类型
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ColumnMetaDefine other = (ColumnMetaDefine) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
