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
package com.baidu.rigel.biplatform.ma.model.meta.exception;

/**
 * 星型模型操作异常类：对星型模型进行处理过程中会有此异常产生，包括创建、修改、删除模型过程。 实现对底层异常的封装
 * 
 * @author david.wang
 *
 */
public class StarModelOperationException extends Exception {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = -1640983430476790242L;
    
    /**
     * 
     */
    public StarModelOperationException() {
        super();
    }
    
    /**
     * 
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public StarModelOperationException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * 
     * @param message
     * @param cause
     */
    public StarModelOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 
     * @param message
     */
    public StarModelOperationException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param cause
     */
    public StarModelOperationException(Throwable cause) {
        super(cause);
    }
    
}
