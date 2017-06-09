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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * 查询表达式，查询限定都是一个个维值 比如一个查询一级行业A 那么queryValues里面的对象就是value为A，leafValues为A对应的2级行业节点
 * 
 * @author xiaoming.chen
 *
 */
public class Expression implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4873611763687676417L;

    /**
     * properties 查询属性
     */
    private final String properties;

    /**
     * queryValues 属性的范围
     */
    private Set<QueryObject> queryValues;

    /**
     * construct with
     * 
     * @param properties 查询属性
     * @param queryValues 属性的范围
     */
    public Expression(String properties, Set<QueryObject> queryValues) {
        this.properties = properties;
        this.queryValues = queryValues;
    }

    /**
     * constructor
     * 
     * @param properties
     */
    public Expression(String properties) {
        this.properties = properties;
    }

    /**
     * getter method for property properties
     * 
     * @return the properties
     */
    public String getProperties() {
        return properties;
    }

    /**
     * getter method for property queryValues
     * 
     * @return the queryValues
     */
    public Set<QueryObject> getQueryValues() {
        if (this.queryValues == null) {
            this.queryValues = new HashSet<QueryObject>();
        }
        return queryValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(queryValues)) {
            sb.append(properties).append(" in ").append(queryValues);
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((queryValues == null) ? 0 : queryValues.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
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
        Expression other = (Expression) obj;
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        if (queryValues == null) {
            if (other.queryValues != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection (queryValues, other.queryValues)) {
            return false;
        }
        return true;
    }
}
