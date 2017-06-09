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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 
 * Tesseract查询结果集
 * 
 * @author lijin
 *
 */
public interface TesseractResultSet<T> extends Serializable {
    /**
     * 
     * get next
     * 
     * @return boolean
     * @throws Exception
     *             , may throw Exception
     */
    boolean next() throws Exception;
    
    /**
     * 
     * close resultset
     * 
     * @throws Exception
     *             , may throw Exception
     */
    void close() throws Exception;
    
    /**
     * 
     * getString by columnIndex
     * 
     * @param columnIndex
     *            column Index
     * @return String
     * @throws Exception
     *             , may throw Exception
     */
    String getString(int columnIndex) throws Exception;
    
    /**
     * 
     * getString by columnLabel
     * 
     * @param columnLabel
     *            columnLabel
     * @return String
     * @throws Exception
     *             , may throw Exception
     */
    String getString(String columnLabel) throws Exception;
    
    /**
     * 
     * getInt by columnIndex
     * 
     * @param columnIndex
     *            columnIndex
     * @return Int
     * @throws Exception
     *             , may throw Exception
     */
    int getInt(int columnIndex) throws Exception;
    
    /**
     * 
     * getInt by columnLabel
     * 
     * @param columnLabel
     *            columnLabel
     * @return int
     * @throws Exception
     *             , may throw Exception
     */
    int getInt(String columnLabel) throws Exception;
    
    /**
     * 
     * getLong by columnIndex
     * 
     * @param columnIndex
     *            columnIndex
     * @return long
     * @throws Exception
     *             , may throw Exception
     */
    long getLong(int columnIndex) throws Exception;
    
    /**
     * 
     * getLong by columnLabel
     * 
     * @param columnLabel
     *            columnLabel
     * @return long
     * @throws Exception
     *             , may throw Exception
     */
    long getLong(String columnLabel) throws Exception;
    
    /**
     * 
     * getDate by columnIndex
     * 
     * @param columnIndex
     *            columnIndex
     * @return Date
     * @throws Exception
     *             , may throw Exception
     */
    Date getDate(int columnIndex) throws Exception;
    
    /**
     * 
     * getDate by columnLabel
     * 
     * @param columnLabel
     *            columnLabel
     * @return Date
     * @throws Exception
     *             , may throw Exception
     */
    Date getDate(String columnLabel) throws Exception;
    
    /**
     * 
     * getBigDecimal by columnIndex
     * 
     * @param columnIndex
     *            columnIndex
     * @return BigDecimal
     * @throws Exception
     *             , may throw Exception
     */
    BigDecimal getBigDecimal(int columnIndex) throws Exception;
    
    /**
     * 
     * getBigDecimal by columnLabel
     * 
     * @param columnLabel
     *            columnLabel
     * @return BigDecimal
     * @throws Exception
     *             , may throw Exception
     */
    BigDecimal getBigDecimal(String columnLabel) throws Exception;
    
    /**
     * 
     * getFieldNameArray
     * @return String[]
     */
    String[] getFieldNameArray();
    
    /**
     * 
     * 结果集大小
     * @return int
     */
    int size();
    
    
    /** 
     * getDataList
     * @return
     */
    List<T> getDataList();
    
    /**
     * 注意，请先调用next()方法，否则直接调用本方法会返回null; 
     * getCurrentRecord
     * 
     * @return TesseractResultRecord;
     */
    TesseractResultRecord getCurrentRecord();
}
