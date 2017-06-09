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

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;

/**
 * 
 * 引用关系定义：用于定义事实表和维度表之间的关联关系，在星型模型中描述引用参照关系
 * 
 * @author david.wang
 *
 */
public class ReferenceDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 93073076074911766L;
    
    /**
     * 事实表名称，这里做冗余存储，主要方便业务处理
     */
    private String majorTable;
    
    /**
     * 主表参考列
     */
    private String majorColumn;
    
    /**
     * 从表参考列
     */
    private String salveColumn;
    
    /**
     * 
     * @return 主表名称
     */
    public String getMajorTable() {
        return majorTable;
    }
    
    /**
     * 
     * @param majorTable
     *            事实表名称
     */
    public void setMajorTable(String majorTable) {
        this.majorTable = majorTable;
    }
    
    /**
     * 
     * @return 外键列
     */
    public String getMajorColumn() {
        return majorColumn;
    }
    
    /**
     * 
     * @param majorColumn
     *            事实表中事实表与维度表关联的列
     */
    public void setMajorColumn(String majorColumn) {
        this.majorColumn = majorColumn;
    }
    
    /**
     * 
     * @return 引用列
     */
    public String getSalveColumn() {
        return salveColumn;
    }
    
    /**
     * 
     * @param salveColumn
     *            维度表中与事实表关联的列
     */
    public void setSalveColumn(String salveColumn) {
        this.salveColumn = salveColumn;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((majorColumn == null) ? 0 : majorColumn.hashCode());
        result = prime * result + ((majorTable == null) ? 0 : majorTable.hashCode());
        result = prime * result + ((salveColumn == null) ? 0 : salveColumn.hashCode());
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
        ReferenceDefine other = (ReferenceDefine) obj;
        if (majorColumn == null) {
            if (other.majorColumn != null) {
                return false;
            }
        } else if (!majorColumn.equals(other.majorColumn)) {
            return false;
        }
        if (majorTable == null) {
            if (other.majorTable != null) {
                return false;
            }
        } else if (!majorTable.equals(other.majorTable)) {
            return false;
        }
        if (salveColumn == null) {
            if (other.salveColumn != null) {
                return false;
            }
        } else if (!salveColumn.equals(other.salveColumn)) {
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

    public boolean isInvalidate() {
        return StringUtils.isEmpty(this.majorColumn) 
                || StringUtils.isEmpty(this.majorTable) 
                    || StringUtils.isEmpty(this.salveColumn);
    }
}
