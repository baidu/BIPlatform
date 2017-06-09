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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * 查询的节点属性
 * @author xiaoming.chen
 *
 */
public class QueryObject implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5166897630296574821L;
    
    /**
     * value 查询对应的值
     */
    private String value;
    
    /**
     * leafValues 每个值对应的叶子节点
     */
    private Set<String> leafValues;
    
    /**
     * isSummary 是否是汇总节点
     */
    private boolean isSummary;

    /**
     * constructor
     * @param value
     * @param leafValues
     */
    public QueryObject(String value, Set<String> leafValues) {
        this.value = value;
        this.leafValues = leafValues;
    }
    
    
    public QueryObject(String value, Set<String> leafValues, boolean isSummary) {
        this(value, leafValues);
        this.isSummary = isSummary;
    }

    /**
     * constructor
     * @param value
     */
    public QueryObject(String value) {
        this.value = value;
        // 默认将叶子节点设置为当前节点的值
        this.leafValues = new HashSet<String>();
        this.leafValues.add(value);
    }

    /**
     * get value
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * set value with value
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leafValues == null) ? 0 : leafValues.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
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
        QueryObject other = (QueryObject) obj;
        if (leafValues == null) {
            if (other.leafValues != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection (leafValues, other.leafValues)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * get leafValues
     * @return the leafValues
     */
    public Set<String> getLeafValues() {
        return leafValues;
    }

    /**
     * set leafValues with leafValues
     * @param leafValues the leafValues to set
     */
    public void setLeafValues(Set<String> leafValues) {
        this.leafValues = leafValues;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QueryObject [value=" + value + ", leafValues=" + leafValues + "]";
    }

    /**
     * @return the isSummary
     */
    public boolean isSummary() {
        return isSummary;
    }

    /**
     * @param isSummary the isSummary to set
     */
    public void setSummary(boolean isSummary) {
        this.isSummary = isSummary;
    }

}
