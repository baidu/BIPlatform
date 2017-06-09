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
package com.baidu.rigel.biplatform.tesseract.resultset.exception;

/** 
 *  
 * 查询结果异常
 * @author xiaoming.chen 
 * @version  2014年11月20日 
 * @since jdk 1.8 or after
 */
public class QueryResultException extends RuntimeException {

    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -1108927664673555883L;
    
    /** 
     * 构造函数
     */
    public QueryResultException() {
        super();
    }
    
    /** 
     * 构造函数
     */
    public QueryResultException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    /** 
     * 构造函数
     */
    public QueryResultException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 
     * 构造函数
     */
    public QueryResultException(String message) {
        super(message);
    }

    /** 
     * 构造函数
     */
    public QueryResultException(Throwable cause) {
        super(cause);
    }
    

}
