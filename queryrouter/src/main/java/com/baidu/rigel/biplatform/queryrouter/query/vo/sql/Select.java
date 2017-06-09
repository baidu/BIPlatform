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
package com.baidu.rigel.biplatform.queryrouter.query.vo.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询的属性
 * 
 * @author xiaoming.chen
 *
 */
public class Select implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3513983220906833848L;

    /**
     * queryProperties 直接查看的属性
     */
    private List<String> queryProperties;

    /**
     * queryMeasures 按照汇总方式查看的属性
     */
    private List<QueryMeasure> queryMeasures;

    /**
     * getter method for property queryProperties
     * 
     * @return the queryProperties
     */
    public List<String> getQueryProperties() {
        if (this.queryProperties == null) {
            this.queryProperties = new ArrayList<String>();
        }
        return queryProperties;
    }

    /**
     * setter method for property queryProperties
     * 
     * @param queryProperties the queryProperties to set
     */
    public void setQueryProperties(List<String> queryProperties) {
        this.queryProperties = queryProperties;
    }

    /**
     * getter method for property queryMeasures
     * 
     * @return the queryMeasures
     */
    public List<QueryMeasure> getQueryMeasures() {
        if (this.queryMeasures == null) {
            return this.queryMeasures = new ArrayList<QueryMeasure>();
        }
        return queryMeasures;
    }

    /**
     * setter method for property queryMeasures
     * 
     * @param queryMeasures the queryMeasures to set
     */
    public void setQueryMeasures(List<QueryMeasure> queryMeasures) {
        this.queryMeasures = queryMeasures;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "queryProperties=" + queryProperties + ", queryMeasures=" + queryMeasures;
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
        result = prime * result + ((queryMeasures == null) ? 0 : queryMeasures.hashCode());
        result = prime * result + ((queryProperties == null) ? 0 : queryProperties.hashCode());
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
        Select other = (Select) obj;
        if (queryMeasures == null) {
            if (other.queryMeasures != null) {
                return false;
            }
        } else if (!queryMeasures.equals(other.queryMeasures)) {
            return false;
        }
        if (queryProperties == null) {
            if (other.queryProperties != null) {
                return false;
            }
        } else if (!queryProperties.equals(other.queryProperties)) {
            return false;
        }
        return true;
    }
}
