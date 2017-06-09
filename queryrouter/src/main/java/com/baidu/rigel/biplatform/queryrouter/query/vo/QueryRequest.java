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
package com.baidu.rigel.biplatform.queryrouter.query.vo;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.From;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.GroupBy;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Limit;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Order;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Select;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Where;

/**
 * 问题查询模型
 * 
 * @author xiaoming.chen
 *
 */
public class QueryRequest implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1024151542526414L;

    /**
     * dataSourceInfo 数据源信息
     */
    private DataSourceInfo dataSourceInfo;

    /**
     * cubeName 查询的cubeName
     */
    private String cubeName;

    /**
     * cubeId 查询的cube对应的唯一ID
     */
    private String cubeId;

    /**
     * select 查询的select信息
     */
    private Select select;

    /**
     * where 过滤条件
     */
    private Where where;

    /**
     * groupBy 分组信息
     */
    private GroupBy groupBy;

    /**
     * order 排序信息
     */
    private Order order;

    /**
     * limit 分页信息
     */
    private Limit limit;

    /**
     * from 数据来源
     */
    private From from;

    /**
     * useIndex 是否使用索引
     */
    private boolean useIndex = true;

    /**
     * 实否使用distinct 只有在退化维查询成员时为true，其他情况为false
     */
    private boolean distinct = false;

    /**
     * getter method for property dataSourceInfo
     * 
     * @return the dataSourceInfo
     */
    public DataSourceInfo getDataSourceInfo() {
        return dataSourceInfo;
    }

    /**
     * setter method for property dataSourceInfo
     * 
     * @param dataSourceInfo
     *            the dataSourceInfo to set
     */
    public void setDataSourceInfo(DataSourceInfo dataSourceInfo) {
        this.dataSourceInfo = dataSourceInfo;
    }

    /**
     * getter method for property cube
     * 
     * @return the cube
     */
    public String getCubeName() {
        return cubeName;
    }

    /**
     * setter method for property cube
     * 
     * @param cube
     *            the cube to set
     */
    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    /**
     * getter method for property select
     * 
     * @return the select
     */
    public Select getSelect() {
        return select;
    }

    /**
     * setter method for property select
     * 
     * @param select
     *            the select to set
     */
    public void setSelect(Select select) {
        this.select = select;
    }

    /**
     * getter method for property where
     * 
     * @return the where
     */
    public Where getWhere() {
        if (this.where == null) {
            this.where = new Where();
        }
        return where;
    }

    /**
     * setter method for property where
     * 
     * @param where
     *            the where to set
     */
    public void setWhere(Where where) {
        this.where = where;
    }

    /**
     * getter method for property groupBy
     * 
     * @return the groupBy
     */
    public GroupBy getGroupBy() {
        return groupBy;
    }

    /**
     * setter method for property groupBy
     * 
     * @param groupBy
     *            the groupBy to set
     */
    public void setGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * getter method for property order
     * 
     * @return the order
     */
    public Order getOrder() {
        return order;
    }

    /**
     * setter method for property order
     * 
     * @param order
     *            the order to set
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    /**
     * getter method for property limit
     * 
     * @return the limit
     */
    public Limit getLimit() {
        if (limit == null) {
            this.limit = new Limit();
        }
        return limit;
    }

    /**
     * setter method for property limit
     * 
     * @param limit
     *            the limit to set
     */
    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    /**
     * getter method for property from
     * 
     * @return the from
     */
    public From getFrom() {
        return from;
    }

    /**
     * setter method for property from
     * 
     * @param from
     *            the from to set
     */
    public void setFrom(From from) {
        this.from = from;
    }

    /**
     * 将指定属性添加到select和group条件中
     * 
     * @param column
     *            指定的字段信息
     * @throws IllegalArgumentException
     *             column为空抛出异常
     */
    public void selectAndGroupBy(String column) {
        if (StringUtils.isBlank(column)) {
            throw new IllegalArgumentException(
                    "can not select and group by empty column");
        }
        if (this.select == null) {
            select = new Select();
        }
        if (this.groupBy == null) {
            this.groupBy = new GroupBy();
        }
        if (!this.getSelect().getQueryProperties().contains(column)) {
            this.getSelect().getQueryProperties().add(column);
        }
        this.getGroupBy().getGroups().add(column);
    }

    /**
     * get cubeId
     * 
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }

    /**
     * set cubeId with cubeId
     * 
     * @param cubeId
     *            the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }

    /**
     * get useIndex
     * 
     * @return the useIndex
     */
    public boolean isUseIndex() {
        return useIndex;
    }

    /**
     * set useIndex with useIndex
     * 
     * @param useIndex
     *            the useIndex to set
     */
    public void setUseIndex(boolean useIndex) {
        this.useIndex = useIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "";
        // return "QueryRequest [dataSourceInfo=" + dataSourceInfo +
        // ", cubeName=" + cubeName + ", cubeId=" + cubeId
        // + ", select=" + select + ", from=" + from + ", where=" + where +
        // ", groupBy=" + groupBy + ", order="
        // + order + ", limit=" + limit + ", useIndex=" + useIndex + "]";
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
        result = prime * result + ((cubeId == null) ? 0 : cubeId.hashCode());
        result = prime * result
                + ((cubeName == null) ? 0 : cubeName.hashCode());
        result = prime * result
                + ((dataSourceInfo == null) ? 0 : dataSourceInfo.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        result = prime * result + ((select == null) ? 0 : select.hashCode());
        result = prime * result + (useIndex ? 1231 : 1237);
        result = prime * result + ((where == null) ? 0 : where.hashCode());
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
        QueryRequest other = (QueryRequest) obj;
        if (cubeId == null) {
            if (other.cubeId != null) {
                return false;
            }
        } else if (!cubeId.equals(other.cubeId)) {
            return false;
        }
        if (cubeName == null) {
            if (other.cubeName != null) {
                return false;
            }
        } else if (!cubeName.equals(other.cubeName)) {
            return false;
        }
        if (dataSourceInfo == null) {
            if (other.dataSourceInfo != null) {
                return false;
            }
        } else if (!dataSourceInfo.equals(other.dataSourceInfo)) {
            return false;
        }
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (groupBy == null) {
            if (other.groupBy != null) {
                return false;
            }
        } else if (!groupBy.equals(other.groupBy)) {
            return false;
        }
        if (limit == null) {
            if (other.limit != null) {
                return false;
            }
        } else if (!limit.equals(other.limit)) {
            return false;
        }
        if (order == null) {
            if (other.order != null) {
                return false;
            }
        } else if (!order.equals(other.order)) {
            return false;
        }
        if (select == null) {
            if (other.select != null) {
                return false;
            }
        } else if (!select.equals(other.select)) {
            return false;
        }
        if (useIndex != other.useIndex) {
            return false;
        }
        if (where == null) {
            if (other.where != null) {
                return false;
            }
        } else if (!where.equals(other.where)) {
            return false;
        }
        return true;
    }

    /**
     * @return the distinct
     */
    public boolean isDistinct() {
        
        return distinct;
    }

    /**
     * @param distinct
     *            the distinct to set
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * 判断是否是sql做agg计算
     * 
     * @return
     */
    public boolean isSqlAgg() {
        if (((SqlDataSourceInfo) this.getDataSourceInfo()).getDataBase() == DataBase.PALO) {
            return true;
        }
        return false;
    }

}
