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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 一行记录，元数据定义
 * 
 * @author lijin
 *
 */
public class Meta implements Serializable {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = -8715266701684746824L;
    /**
     * 字符名与下标关系
     */
    private Map<String, Integer> fieldNames;
    
    private String[] fieldNameArray;
    
    /**
     * 元数据定义
     * 
     * @param fieldNames
     *            字段名
     */
    public Meta(String[] fieldNames) {
        super();
        this.fieldNameArray = fieldNames;
        this.fieldNames = new HashMap<String, Integer>();
        for (int i = 0; i < fieldNames.length; i++) {
            this.fieldNames.put(fieldNames[i], i);
        }
    }
    
    @Override
    public String toString() {
        return "Meta [fieldNames=" + fieldNames + "]";
    }
    
    /**
     * getter method for property fieldNames
     * 
     * @return the fieldNames
     */
    public Map<String, Integer> getFieldNames() {
        return fieldNames;
    }
    
    /**
     * getFieldIndex
     * 
     * @param field
     * @return
     */
    public int getFieldIndex(String field) {
        return fieldNames.get(field);
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
        result = prime * result + ((fieldNames == null) ? 0 : fieldNames.hashCode());
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
        Meta other = (Meta) obj;
        if (fieldNames == null) {
            if (other.fieldNames != null) {
                return false;
            }
        } else if (!fieldNames.equals(other.fieldNames)) {
            return false;
        }
        return true;
    }
    
    /**
     * get fieldNameArray
     * 
     * @return the fieldNameArray
     */
    public String[] getFieldNameArray() {
        return fieldNameArray;
    }
    
}
