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
package com.baidu.rigel.biplatform.tesseract.isservice.exception;

/**
 * QueryExceptionType 查询异常类型
 * 
 * @author lijin
 *
 */
public enum IndexAndSearchExceptionType {
    /**
     * 网络异常
     */
    NETWORK_EXCEPTION("NETWORK_EXCEPTION"),
    /**
     * INDEX_EXCEPTION
     */
    INDEX_EXCEPTION("INDEX_EXCEPTION"),
    /**
     * INDEX_UPDATE_EXCEPTION
     */
    INDEX_UPDATE_EXCEPTION("INDEX_UPDATE_EXCEPTION"),
    /**
     * SEARCH_EXCEPTION
     */
    SEARCH_EXCEPTION("SEARCH_EXCEPTION"),
    /**
     * 参数错误
     */
    ILLEGALARGUMENT_EXCEPTION("ILLEGALARGUMENT_EXCEPTION"),
    /**
     * SQL异常
     */
    SQL_EXCEPTION("SQLQUERY_EXCEPTION");
    
    /**
     * 异常类型名称
     */
    private String typeName;
    
    /**
     * QueryExceptionType
     * 
     * @param typeName
     *            typeName
     */
    private IndexAndSearchExceptionType(String typeName) {
        this.typeName = typeName;
    }
    
    /**
     * getter method for property typeName
     * 
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }
    
}
