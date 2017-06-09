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
package com.baidu.rigel.biplatform.tesseract.isservice.exception;


/**
 * 
 * 异常-索引元数据为空
 * 
 * @author lijin
 *
 */
public class IndexMetaIsNullException extends Exception {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2867445630243248804L;
    
    /**
     * IndexMetaIsNullException
     */
    public IndexMetaIsNullException() {
        super();
    }
    
    /**
     * IndexMetaIsNullException
     * @param message message
     * @param cause cause
     * @param enableSuppression enableSuppression
     * @param writableStackTrace writableStackTrace
     */
    public IndexMetaIsNullException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * IndexMetaIsNullException
     * @param message  message
     * @param cause cause
     */
    public IndexMetaIsNullException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * IndexMetaIsNullException
     * @param message message
     */
    public IndexMetaIsNullException(String message) {
        super(message);
    }
    
    /**
     * IndexMetaIsNullException
     * @param cause cause
     */
    public IndexMetaIsNullException(Throwable cause) {
        super(cause);
    }
    
    
}
