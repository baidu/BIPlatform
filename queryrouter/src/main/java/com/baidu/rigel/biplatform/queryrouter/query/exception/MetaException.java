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
package com.baidu.rigel.biplatform.queryrouter.query.exception;

/**
 * 元数据相关的异常信息
 * @author xiaoming.chen
 *
 */
public class MetaException extends Exception {

    /**
     * default generate uuid
     */
    private static final long serialVersionUID = 4228951958535098619L;

    /**
     * constructor
     */
    public MetaException() {
    }

    /**
     * constructor
     * @param message
     * @param cause
     */
    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * constructor
     * @param message
     */
    public MetaException(String message) {
        super(message);
    }

    /**
     * constructor
     * @param cause
     */
    public MetaException(Throwable cause) {
        super(cause);
    }

}
