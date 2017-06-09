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
package com.baidu.rigel.biplatform.tesseract.exception;

/**
 * 查询条件超出指定大小异常
 * 
 * @author xiaoming.chen
 *
 */
public class OverflowQueryConditionException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 76496689426875507L;

    /**
     * constructor
     */
    public OverflowQueryConditionException() {
    }

    /**
     * constructor
     * 
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public OverflowQueryConditionException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * constructor
     * 
     * @param message
     * @param cause
     */
    public OverflowQueryConditionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * constructor
     * 
     * @param message
     */
    public OverflowQueryConditionException(String message) {
        super(message);
    }

    /**
     * constructor
     * 
     * @param cause
     */
    public OverflowQueryConditionException(Throwable cause) {
        super(cause);
    }

}
