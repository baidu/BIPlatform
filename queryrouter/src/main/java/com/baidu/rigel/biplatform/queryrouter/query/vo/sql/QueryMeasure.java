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
package com.baidu.rigel.biplatform.queryrouter.query.vo.sql;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.model.Aggregator;

/**
 * 查询的指标信息
 * 
 * @author xiaoming.chen
 *
 */
public class QueryMeasure implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2144797205821064903L;

    /**
     * aggregator 聚集方式
     */
    private Aggregator aggregator = Aggregator.SUM;

    /**
     * properties 查询的属性，可以为字段，也可以为表达式
     */
    private String properties;

    /**
     * construct with
     * 
     * @param properties 聚集的字段
     */
    public QueryMeasure(String properties) {
        this.properties = properties;
    }

    /**
     * construct with
     * 
     * @param aggregator 聚集方式
     * @param properties 聚集的字段
     */
    public QueryMeasure(Aggregator aggregator, String properties) {
        this.aggregator = aggregator;
        this.properties = properties;
    }

    /**
     * getter method for property aggregator
     * 
     * @return the aggregator
     */
    public Aggregator getAggregator() {
        return aggregator;
    }

    /**
     * setter method for property aggregator
     * 
     * @param aggregator the aggregator to set
     */
    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
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
     * setter method for property properties
     * 
     * @param properties the properties to set
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QueryMeasure [aggregator=" + aggregator + ", properties=" + properties + "]";
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
        result = prime * result + ((aggregator == null) ? 0 : aggregator.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        QueryMeasure other = (QueryMeasure) obj;
        if (aggregator != other.aggregator) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

}
