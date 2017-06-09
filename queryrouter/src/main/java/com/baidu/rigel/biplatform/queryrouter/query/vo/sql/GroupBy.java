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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * query model group by condition
 * 
 * @author xiaoming.chen
 *
 */
public class GroupBy implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7084305768299700037L;

    /**
     * groupList 分组的列表，字段名或者属性名
     */
    private Set<String> groups;

    /**
     * construct with
     * 
     * @param groupList groupby list
     */
    public GroupBy(Set<String> groups) {
        this.groups = groups;
    }

    /**
     * 默认构造方法
     */
    public GroupBy() {
        this.groups = new HashSet<String>();
    }

    /**
     * getter method for property groupList
     * 
     * @return the groups
     */
    public Set<String> getGroups() {
        if (this.groups == null) {
            this.groups = new HashSet<String>();
        }
        return groups;
    }

    /**
     * setter method for property groupList
     * 
     * @param groups the groupList to set
     */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringUtils.join(groups, ",");
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
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
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
        GroupBy other = (GroupBy) obj;
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        return true;
    }

}
