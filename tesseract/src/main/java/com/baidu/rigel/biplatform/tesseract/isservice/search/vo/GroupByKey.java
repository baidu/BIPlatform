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
package com.baidu.rigel.biplatform.tesseract.isservice.search.vo;

import java.util.Arrays;
import java.util.Map;

/**
 * 
 * GroupByKey
 * @author lijin
 *
 */
public class GroupByKey {
    /**
     * 进行groupBy的字段
     */
    private String[] keys;
    /**
     * values对应的值
     */
    private Map<String, Object> values;
    
    /**
     * GroupByKey 构造函数
     * @param keys keys
     */
    public GroupByKey(String[] keys) {
        this.keys = keys;
    }
    
    

    /**
     * getter method for property keys
     * @return the keys
     */
    public String[] getKeys() {
        return keys;
    }



    /**
     * setter method for property keys
     * @param keys the keys to set
     */
    public void setKeys(String[] keys) {
        this.keys = keys;
    }



    /**
     * getter method for property values
     * @return the values
     */
    public Map<String, Object> getValues() {
        return values;
    }



    /**
     * setter method for property values
     * @param values the values to set
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }



    @Override
    public String toString() {
        return "GroupByKey [keys=" + Arrays.toString(keys) + ", values=" + values + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(keys);
        result = prime * result + ((values == null) ? 0 : values.hashCode());
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
        if (!(obj instanceof GroupByKey)) {
            return false;
        }
        GroupByKey other = (GroupByKey) obj;
        if (!Arrays.equals(keys, other.keys)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    
    
    
    
}
