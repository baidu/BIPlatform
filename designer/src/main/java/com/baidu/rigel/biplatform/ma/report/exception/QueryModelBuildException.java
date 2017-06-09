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
package com.baidu.rigel.biplatform.ma.report.exception;

/**
 * 
 * QueryModelBuildException
 * @author david.wang
 * @version 1.0.0.1
 */
public class QueryModelBuildException extends Exception {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 4892541514702825744L;
    
    /**
     * {@inheritDoc}
     */
    public QueryModelBuildException() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryModelBuildException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryModelBuildException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryModelBuildException(String message) {
        super(message);
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryModelBuildException(Throwable cause) {
        super(cause);
    }
    
}
