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
 * QueryException
 * 
 * @author lijin
 *
 */
public class IndexAndSearchException extends Exception {
    /**
     * QUERYEXCEPTION_MESSAGE
     */
    public static final String QUERYEXCEPTION_MESSAGE = "QueryException : Type [%s]";
    
    /**
     * INDEXEXCEPTION_MESSAGE
     */
    public static final String INDEXEXCEPTION_MESSAGE = "IndexException occur : Type [%s]";
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7900161513913440368L;
    
    /**
     * 异常类型
     */
    private IndexAndSearchExceptionType qType;
    
    /**
     * QueryException
     */
    public IndexAndSearchException() {
        
    }
    
    /**
     * QueryException
     * 
     * @param message
     *            message
     * @param qType
     *            qType
     */
    public IndexAndSearchException(String message, IndexAndSearchExceptionType qType) {
        super(message);
        this.qType = qType;
        
    }
    
    /**
     * QueryException
     * 
     * @param cause
     *            cause
     * @param qType
     *            qType
     */
    public IndexAndSearchException(Throwable cause, IndexAndSearchExceptionType qType) {
        super(cause);
        this.qType = qType;
    }
    
    /**
     * QueryException
     * 
     * @param message
     *            message
     * @param cause
     *            cause
     * @param qType
     *            qType
     */
    public IndexAndSearchException(String message, Throwable cause,
        IndexAndSearchExceptionType qType) {
        super(message, cause);
        this.qType = qType;
    }
    
    /**
     * QueryException
     * 
     * @param message
     *            message
     * @param cause
     *            cause
     * @param enableSuppression
     *            enableSuppression
     * @param writableStackTrace
     *            writableStackTrace
     * @param qType
     *            qType
     */
    public IndexAndSearchException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace, IndexAndSearchExceptionType qType) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.qType = qType;
    }
    
    /**
     * get qType
     * 
     * @return the qType
     */
    public IndexAndSearchExceptionType getqType() {
        return qType;
    }
    
    /**
     * set qType with qType
     * 
     * @param qType
     *            the qType to set
     */
    public void setqType(IndexAndSearchExceptionType qType) {
        this.qType = qType;
    }
    
}
