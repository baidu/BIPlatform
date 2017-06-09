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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;


/**
 * 
 * Description: QuestionModelTransformationException
 * 
 * @author 罗文磊
 *
 */
public class QuestionModelTransformationException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8774159232736125882L;
    
    /**
     * default constructor
     */
    public QuestionModelTransformationException() {
        super();
    }

    /**
     * construct with exception message and cause
     * @param message exception message
     * @param cause  exception cause
     */
    public QuestionModelTransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * construct with exception message
     * @param message exception message
     */
    public QuestionModelTransformationException(String message) {
        super(message);
    }

    /**
     * construct with exception cause
     * @param cause exception cause
     */
    public QuestionModelTransformationException(Throwable cause) {
        super(cause);
    }
}