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
package com.baidu.rigel.biplatform.tesseract.resultset.exception;

/**
 * TODO
 * 
 * @author lijin
 *
 */
public class NoSuchFieldException extends Exception {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1490135782451217666L;
    
    /**
     * MESSAGE_NO_SUCH_FIELD_EXCEPTION
     */
    private static final String MESSAGE_NO_SUCH_FIELD_EXCEPTION = "MESSAGE_NO_SUCH_FIELD_EXCEPTION_";
    
    /**
     * NoSuchFieldException
     */
    public NoSuchFieldException() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * NoSuchFieldException
     * @param message message
     */
    public NoSuchFieldException(String message) {
        super(MESSAGE_NO_SUCH_FIELD_EXCEPTION + message);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * NoSuchFieldException
     * @param cause cause
     */
    public NoSuchFieldException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * NoSuchFieldException
     * @param message message
     * @param cause cause
     */ 
    public NoSuchFieldException(String message, Throwable cause) {
        super(MESSAGE_NO_SUCH_FIELD_EXCEPTION + message, cause);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * NoSuchFieldException
     * @param message message
     * @param cause cause
     * @param enableSuppression enableSuppression
     * @param writableStackTrace writableStackTrace
     */
    public NoSuchFieldException(String message, Throwable cause, 
        boolean enableSuppression, boolean writableStackTrace) {
        super(MESSAGE_NO_SUCH_FIELD_EXCEPTION + message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }
    
}
