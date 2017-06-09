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
import java.util.List;

import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;

/**
 * 排序信息
 * 
 * @author xiaoming.chen
 *
 */
public class Order implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -683745389497262377L;

    /**
     * 排序类型
     */
    private SortType sortType = SortType.NONE;

    /**
     * orderData 排序的字段or表达式
     */
    private List<String> orderDatas;

    /**
     * getter method for property sortType
     * 
     * @return the sortType
     */
    public SortType getSortType() {
        return sortType;
    }

    /**
     * setter method for property sortType
     * 
     * @param sortType the sortType to set
     */
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    /**
     * get orderDatas
     * 
     * @return the orderDatas
     */
    public List<String> getOrderDatas() {
        return orderDatas;
    }

    /**
     * set orderDatas with orderDatas
     * 
     * @param orderDatas the orderDatas to set
     */
    public void setOrderDatas(List<String> orderDatas) {
        this.orderDatas = orderDatas;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return orderDatas + ", " + sortType;
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
        result = prime * result + ((orderDatas == null) ? 0 : orderDatas.hashCode());
        result = prime * result + ((sortType == null) ? 0 : sortType.hashCode());
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
        Order other = (Order) obj;
        if (orderDatas == null) {
            if (other.orderDatas != null) {
                return false;
            }
        } else if (!orderDatas.equals(other.orderDatas)) {
            return false;
        }
        if (sortType != other.sortType) {
            return false;
        }
        return true;
    }

}
