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
package com.baidu.rigel.biplatform.tesseract.node.exception;

/**
 * IsNodeException
 * @author lijin
 *
 */
public class IsNodeException extends Exception {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = -8440844895932167357L;

    /**
     * Constructor by no param
     */
    public IsNodeException() {
        super();
    }
    
    /**
     * Constructor by 
     * @param message
     */
    public IsNodeException(String message) {
        super(message);
        
    }
    
    /**
     * Constructor by 
     * @param cause
     */
    public IsNodeException(Throwable cause) {
        super(cause);
        
    }
    
    /**
     * Constructor by 
     * @param message
     * @param cause
     */
    public IsNodeException(String message, Throwable cause) {
        super(message, cause);
        
    }
    
    /**
     * Constructor by 
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public IsNodeException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        
    }
    
}
