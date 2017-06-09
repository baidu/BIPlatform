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

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.exception.NotSupportedDateFormatException;
import com.baidu.rigel.biplatform.tesseract.util.DateFormatType;
import com.baidu.rigel.biplatform.tesseract.util.StringTools;

/**
 * 
 * ISResultSet是TesseractResultSet在索引查询这块的一个实现
 * 
 * @author lijin
 *
 */
public class SearchResultSet implements TesseractResultSet<ResultRecord> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1179095061016573740L;
    /**
     * resultQ
     */
    private Queue<ResultRecord> resultQ;
    /**
     * currRecord
     */
    private ResultRecord currRecord;

    /**
     * 构造函数
     * 
     * @param resultQ 结果集
     * 
     */
    public SearchResultSet(Queue<ResultRecord> resultQ) {
        super();
        this.resultQ = resultQ;
        this.currRecord = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#next()
     */
    @Override
    public boolean next() throws IOException {
        boolean result = false;
        if (this.resultQ != null) {
            this.currRecord = this.resultQ.poll();
            if (this.currRecord == null) {
                result = false;
            } else {
                result = true;
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
        if (this.resultQ != null) {
            this.resultQ.clear();
        }
        this.resultQ = null;
        this.currRecord = null;
    }

    /**
     * 
     * convert2String
     * 
     * @param field 待格式化的字段
     * @return String
     */
    private String convert2String(Object field) {

        return field == null ? null : field.toString();
    }

    /**
     * 
     * convert2Int
     * 
     * @param field 待格式化的字段
     * @return int
     */
    private int convert2Int(Object field) {
        return Integer.valueOf((String) field);
    }

    /**
     * 
     * convert2Long
     * 
     * @param field 待格式化的字段
     * @return long
     */
    private long convert2Long(Object field) {
        return Long.valueOf((String) field);
    }

    /**
     * 
     * convert2Date
     * 
     * @param field 待格式化的字段
     * @return Date
     * @throws NotSupportedDateFormatException 可以抛出异常
     * @throws ParseException 可以抛出异常
     */
    private Date convert2Date(Object field) throws NotSupportedDateFormatException, ParseException {
        String dateStr = (String) field;

        DateFormatType dateType = StringTools.dateFormatType(dateStr);
        if (dateType == null) {
            throw new NotSupportedDateFormatException();
        }
        return dateType.getFormatter().parse(dateStr);
    }

    /**
     * 
     * convert2BigDecimal
     * 
     * @param field 待格式化的字段
     * @return BigDecimal
     */
    private BigDecimal convert2BigDecimal(Object field) {

        return new BigDecimal(field.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getString (int)
     */
    @Override
    public String getString(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2String(field);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getString (java.lang.String)
     */
    @Override
    public String getString(String columnLable) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnLable);
        return convert2String(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getInt (int)
     */
    @Override
    public int getInt(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2Int(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getInt (java.lang.String)
     */
    @Override
    public int getInt(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnLabel);
        return convert2Int(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getLong (int)
     */
    @Override
    public long getLong(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2Long(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getLong (java.lang.String)
     */
    @Override
    public long getLong(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnLabel);
        return convert2Long(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getDate (int)
     */
    @Override
    public Date getDate(int columnIndex) throws NoSuchFieldException, NotSupportedDateFormatException, ParseException {
        Object field = this.currRecord.getField(columnIndex);
        return convert2Date(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getDate (java.lang.String)
     */
    @Override
    public Date getDate(String columnLabel) throws NoSuchFieldException, ParseException,
            NotSupportedDateFormatException {
        Object field = this.currRecord.getField(columnLabel);
        return convert2Date(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return convert2BigDecimal(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getBigDecimal(java.lang.String)
     */
    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnLabel);
        if (field == null) {
            return BigDecimal.valueOf(0L);
        }
        return convert2BigDecimal(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getFieldArray()
     */
    @Override
    public String[] getFieldNameArray() {
        return this.currRecord.getMeta().getFieldNames().keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#size()
     */
    @Override
    public int size() {
        if (this.resultQ != null) {
            return resultQ.size();
        }
        return 0;
    }

    /**
     * getter method for property resultQ
     * 
     * @return the resultQ
     */
    public Queue<ResultRecord> getResultQ() {
        return resultQ;
    }

    /**
     * setter method for property resultQ
     * 
     * @param resultQ the resultQ to set
     */
    public void setResultQ(Queue<ResultRecord> resultQ) {
        this.resultQ = resultQ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SearchResultSet ");
        sb.append("[size=" + this.size() + "]");
        sb.append("[resultQ=[");
        sb.append(" get " + this.resultQ.size() + " result");
        sb.append("]");
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getCurrentRecord()
     */
    @Override
    public ResultRecord getCurrentRecord() {
        return this.currRecord;
    }

    @Override
    public List<ResultRecord> getDataList() {
        return null;
    }
    
    
    
}
