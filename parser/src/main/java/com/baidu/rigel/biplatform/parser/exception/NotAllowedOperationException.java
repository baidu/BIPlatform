
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
package com.baidu.rigel.biplatform.parser.exception;

/** 
 * 不允许的操作
 * @author xiaoming.chen
 * @version  2014年12月19日 
 * @since jdk 1.8 or after
 */
public class NotAllowedOperationException extends RuntimeException {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -100011724276193837L;

    
    /** 
     * 构造函数
     */
    public NotAllowedOperationException() {
        super();
        
    }

    
    /** 
     * 构造函数
     */
    public NotAllowedOperationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        
    }

    
    /** 
     * 构造函数
     */
    public NotAllowedOperationException(String message, Throwable cause) {
        super(message, cause);
        
    }

    
    /** 
     * 构造函数
     */
    public NotAllowedOperationException(String message) {
        super(message);
        
    }

    
    /** 
     * 构造函数
     */
    public NotAllowedOperationException(Throwable cause) {
        super(cause);
        
    }

}

