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
package com.baidu.rigel.biplatform.asyndownload.exception;

/**
 * 异步下载异常定义类
 * @author luowenlei
 *
 */
public class AsynDownloadException extends Exception {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8926622777716054838L;

    /**
     * construct with 
     */
    public AsynDownloadException() {
    }

    /**
     * construct with 
     * @param message error message
     * @param cause error cause
     * @param enableSuppression  enableSuppression
     * @param writableStackTrace writableStackTrace
     */
    public AsynDownloadException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * construct with 
     * @param message error message
     * @param cause error cause
     */
    public AsynDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * construct with 
     * @param message error message
     */
    public AsynDownloadException(String message) {
        super(message);
    }

    /**
     * construct with 
     * @param cause error cause
     */
    public AsynDownloadException(Throwable cause) {
        super(cause);
    }   
}
