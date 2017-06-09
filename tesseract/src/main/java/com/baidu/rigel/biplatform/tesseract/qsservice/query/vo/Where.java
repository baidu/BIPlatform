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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 过滤条件
 * 
 * @author xiaoming.chen
 *
 */
public class Where implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2245557103626400716L;

    /**
     * andList and 条件列表
     */
    private List<Expression> andList;

    /**
     * orList or 条件列表
     */
    private List<Expression> orList;
    
    private Between between;

    public Between getBetween() {
		return between;
	}

	public void setBetween(Between between) {
		this.between = between;
	}

	/**
     * getter method for property andList
     * 
     * @return the andList
     */
    public List<Expression> getAndList() {
        if (this.andList == null) {
            this.andList = new ArrayList<Expression>();
        }
        return andList;
    }

    /**
     * setter method for property andList
     * 
     * @param andList the andList to set
     */
    public void setAndList(List<Expression> andList) {
        this.andList = andList;
    }

    /**
     * getter method for property orList
     * 
     * @return the orList
     */
    public List<Expression> getOrList() {
        if (this.orList == null) {
            this.orList = new ArrayList<Expression>();
        }
        return orList;
    }

    /**
     * setter method for property orList
     * 
     * @param orList the orList to set
     */
    public void setOrList(List<Expression> orList) {
        this.orList = orList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // 暂时不用orList
        if (CollectionUtils.isNotEmpty(andList)) {
            return StringUtils.join(andList, " and ");
        }
        return "";
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
        result = prime * result + ((andList == null) ? 0 : andList.hashCode());
        result = prime * result + ((orList == null) ? 0 : orList.hashCode());
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
        Where other = (Where) obj;
        if (andList == null) {
            if (other.andList != null) {
                return false;
            }
        } else if (!andList.equals(other.andList)) {
            return false;
        }
        if (orList == null) {
            if (other.orList != null) {
                return false;
            }
        } else if (!orList.equals(other.orList)) {
            return false;
        }
        return true;
    }

}
