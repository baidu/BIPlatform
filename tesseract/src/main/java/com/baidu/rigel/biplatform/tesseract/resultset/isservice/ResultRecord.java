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
package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * 
 * 结果记录
 * 
 * @author lijin
 *
 */
public class ResultRecord implements Serializable,TesseractResultRecord {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5016094979618797977L;
    /**
     * 一行记录，结果数据
     */
    private Serializable[] fieldArray;
    /**
     * 一行记录，元数据
     */
    private Meta meta;
    
    /**
     * groupBy 将groupBy的字段的值用,分隔
     */
    private String groupBy;
    
    /**
     * 构造方法
     * 
     * @param feildArray
     *            fieldArray
     * @param fieldName
     *            fieldName
     */
    public ResultRecord(Serializable[] feildArray, String[] fieldName) {
        super();
        this.fieldArray = feildArray;
        this.meta = new Meta(fieldName);
    }
    
    /**
     * 构造方法
     * 
     * Constructor by 
     * @param feildArray feildArray
     * @param meta meta
     */
    public ResultRecord(Serializable[] feildArray, Meta meta) {
        super();
        this.fieldArray = feildArray;
        this.meta = meta;
    }
    
    /**
     * ResultRecord
     * 
     * @param doc
     *            doc
     */
    public ResultRecord(Document doc) {
        super();
        List<IndexableField> idxFields = doc.getFields();
        List<String> fieldNameList = new ArrayList<String>();
        List<String> fieldList = new ArrayList<String>();
        for (IndexableField field : idxFields) {
            fieldNameList.add(field.name());
            fieldList.add(field.stringValue());
        }
        
        this.fieldArray = fieldList.toArray(new String[0]);
        this.meta = new Meta(fieldNameList.toArray(new String[0]));
    }
    
    /*
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.resultset.isservice.TesseractResultRecord#getField(int)
     */
    public Serializable getField(int columnIndex) throws NoSuchFieldException {
        if (this.fieldArray != null && columnIndex < this.fieldArray.length) {
            return fieldArray[columnIndex];
        }
        System.out.println(this.toString());
        throw new NoSuchFieldException("FieldIndex:" + columnIndex);
    }
    
    /**
     * 
     * 根据字段名取值
     * 
     * @param columnLable
     *            字段名
     * @return Object
     * @throws NoSuchFieldException
     *             ,可能抛出异常
     */
    public Serializable getField(String columnLable) throws NoSuchFieldException {
        Integer columnIndex = this.meta.getFieldNames().get(columnLable);
        if (columnIndex == null) {
            throw new NoSuchFieldException("FieldName:" + columnLable);
            
        }
        return getField(columnIndex);
    }
    
    /**
     * 返回查询的元数据的数组长度
     * @return 元数据的数据长度
     */
    public int getFieldArrayLen() {
        return this.fieldArray == null ? 0 : fieldArray.length;
    }
    
    /**
     * 
     * setField 覆盖已经存在的feild
     * 
     * @param columnLable
     *            字段名
     * @param newField
     *            值
     * @throws NoSuchFieldException
     *             可能抛出异常
     */
    public void setField(String columnLable, Serializable newField) throws NoSuchFieldException {
        Integer columnIndex = this.meta.getFieldNames().get(columnLable);
        if (columnIndex == null) {
            throw new NoSuchFieldException("FieldName:" + columnLable);
            
        }
        this.fieldArray[columnIndex] = newField;
        return;
        
    }
    
    
    
    public void setField(int columnIndex, Serializable newField) {
        if (columnIndex < 0 || columnIndex > fieldArray.length) {
            throw new IndexOutOfBoundsException("index:" + columnIndex + " fieldArray length:" + fieldArray.length);
        }
        this.fieldArray[columnIndex] = newField;
    }
    
    /**
     * getter method for property meta
     * 
     * @return the meta
     */
    public Meta getMeta() {
        return meta;
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
    
    /**
     * 
     * getFieldNameList
     * @return Set<String>
     */
    public Set<String> getFieldNameList() {
        if (this.meta != null && this.meta.getFieldNames() != null) {
            return this.meta.getFieldNames().keySet();
        }
        return null;
    }
    
    /**
     * 
     * addField
     * 
     * @param field
     *            field
     * @param fieldName
     *            fieldName
     */
    public void addField(Serializable field, String fieldName) {
        List<Serializable> fieldList = getFieldList();
        fieldList.add(field);
        this.meta.getFieldNames().put(fieldName, fieldList.size() - 1);
        this.fieldArray = fieldList.toArray(new Serializable[0]);
    }
    
    @Override
    public String toString() {
        return "ResultRecord [fieldArray=" + Arrays.toString(fieldArray) + ", meta=" + meta + ", groupBy=" + groupBy
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
        result = prime * result + Arrays.hashCode(fieldArray);
        result = prime * result + ((meta == null) ? 0 : meta.hashCode());
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
        ResultRecord other = (ResultRecord) obj;
        if (!Arrays.equals(fieldArray, other.fieldArray)) {
            return false;
        }
        if (meta == null) {
            if (other.meta != null) {
                return false;
            }
        } else if (!meta.equals(other.meta)) {
            return false;
        }
        return true;
    }

    /**
     * get groupBy
     * @return the groupBy
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * set groupBy with groupBy
     * @param groupBy the groupBy to set
     */
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
    
}
