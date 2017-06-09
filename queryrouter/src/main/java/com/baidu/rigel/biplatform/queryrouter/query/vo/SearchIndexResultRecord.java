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
package com.baidu.rigel.biplatform.queryrouter.query.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类SearchIndexResultRecord.java的实现描述：TODO 类实现描述
 * 
 * @author luowenlei 2015年12月9日 下午8:30:27
 */
public class SearchIndexResultRecord implements Serializable, TesseractResultRecord {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -966624945713278263L;
    
    /**
     * 一行记录，结果数据
     */
    private Serializable[] fieldArray;
    
    /**
     * groupBy 将groupBy的字段的值用,分隔
     */
    private String groupBy;
    
    /**
     * distinctMeasures
     */
    private ConcurrentHashMap<Integer, LinkedHashSet<Serializable>> distinctMeasures = new ConcurrentHashMap<>();
    
    /**
     * 构造方法
     * 
     * @param feildArray
     *            fieldArray
     * @param fieldName
     *            fieldName
     */
    public SearchIndexResultRecord(Serializable[] feildArray, String groupBy) {
        super();
        this.fieldArray = feildArray;
        this.groupBy = groupBy;
        
    }
    
    /**
     * @param fieldLength
     */
    public SearchIndexResultRecord(int fieldLength) {
        this.fieldArray = new Serializable[fieldLength];
    }
    
    /**
     * of
     *
     * @param fieldLength
     * @return
     */
    public static SearchIndexResultRecord of(int fieldLength) {
        return new SearchIndexResultRecord(fieldLength);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.isservice.
     * TesseractResultRecord#getField(int)
     */
    public Serializable getField(int columnIndex) {
        if (this.fieldArray != null && columnIndex < this.fieldArray.length) {
            return fieldArray[columnIndex];
        }
        // System.out.println(this.toString());
        throw new IllegalArgumentException("FieldIndex:" + columnIndex);
        // throw new NoSuchFieldException("FieldIndex:" + columnIndex);
    }
    
    /**
     * setField
     *
     * @param columnIndex
     * @param newField
     */
    public void setField(int columnIndex, Serializable newField) {
        if (columnIndex < 0 || columnIndex > fieldArray.length) {
            throw new IndexOutOfBoundsException("index:" + columnIndex + " fieldArray length:"
                    + fieldArray.length);
        }
        this.fieldArray[columnIndex] = newField;
    }
    
    /**
     * 
     * getFieldList
     * 
     * @return List<Object>
     */
    public List<Serializable> getFieldList() {
        List<Serializable> fieldList = new ArrayList<Serializable>();
        for (Serializable field : this.fieldArray) {
            fieldList.add(field);
        }
        return fieldList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResultRecord [fieldArray=" + Arrays.toString(fieldArray) + ", groupBy=" + groupBy
                + "]";
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
        result = prime * result + ((distinctMeasures == null) ? 0 : distinctMeasures.hashCode());
        result = prime * result + Arrays.hashCode(fieldArray);
        result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
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
        SearchIndexResultRecord other = (SearchIndexResultRecord) obj;
        if (distinctMeasures == null) {
            if (other.distinctMeasures != null) {
                return false;
            }
        } else if (!distinctMeasures.equals(other.distinctMeasures)) {
            return false;
        }
        if (!Arrays.equals(fieldArray, other.fieldArray)) {
            return false;
        }
        if (groupBy == null) {
            if (other.groupBy != null) {
                return false;
            }
        } else if (!groupBy.equals(other.groupBy)) {
            return false;
        }
        return true;
    }
    
    /**
     * get groupBy
     * 
     * @return the groupBy
     */
    public String getGroupBy() {
        return groupBy;
    }
    
    /**
     * set groupBy with groupBy
     * 
     * @param groupBy
     *            the groupBy to set
     */
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
    
    /**
     * getFieldArraySize
     *
     * @return
     */
    public int getFieldArraySize() {
        if (this.fieldArray != null) {
            return this.fieldArray.length;
        }
        return 0;
    }
    
    /**
     * @return the fieldArray
     */
    public Serializable[] getFieldArray() {
        return fieldArray;
    }
    
    /**
     * 获取 distinctMeasures
     * 
     * @return the distinctMeasures
     */
    public ConcurrentHashMap<Integer, LinkedHashSet<Serializable>> getDistinctMeasures() {
        
        return distinctMeasures;
    }
    
    /**
     * 设置 distinctMeasures
     * 
     * @param distinctMeasures
     *            the distinctMeasures to set
     */
    public void setDistinctMeasures(ConcurrentHashMap<Integer, LinkedHashSet<Serializable>> distinctMeasures) {
        
        this.distinctMeasures = distinctMeasures;
    }
}
