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
package com.baidu.rigel.biplatform.ma.ds.exception;

/**
 * 数据源操作异常
 * 
 * @author david.wang
 *
 */
public class DataSourceOperationException extends Exception {
    
    /**
     * serialize id
     */
    private static final long serialVersionUID = -7778754615080054332L;
    
    /**
     * {@inheritDoc}
     */
    public DataSourceOperationException() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    public DataSourceOperationException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * {@inheritDoc}
     */
    public DataSourceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * {@inheritDoc}
     */
    public DataSourceOperationException(String message) {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public DataSourceOperationException(Throwable cause) {
        super(cause);
    }
    
}
