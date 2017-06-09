
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
 *  
 * @author xiaoming.chen
 * @version  2014年12月11日 
 * @since jdk 1.8 or after
 */
public class InvokeFunctionException extends RuntimeException {

    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 3883253126951896129L;
    
    
    /** 
     * functionName
     */
    private String functionName;
    
    /** 
     * errorMsg
     */
    private String errorMsg;
    
    private final static String messagePattern = "invoke function:%s catch error,error message:%s";

    
    /** 
     * 构造函数
     */
    public InvokeFunctionException(String functionName, String errorMsg) {
        super(String.format(messagePattern, functionName, errorMsg));
        this.functionName = functionName;
        this.errorMsg = errorMsg;
    }

    /** 
     * 获取 functionName 
     * @return the functionName 
     */
    public String getFunctionName() {
    
        return functionName;
    }


    /** 
     * 设置 functionName 
     * @param functionName the functionName to set 
     */
    public void setFunctionName(String functionName) {
    
        this.functionName = functionName;
    }


    /** 
     * 获取 errorMsg 
     * @return the errorMsg 
     */
    public String getErrorMsg() {
    
        return errorMsg;
    }


    /** 
     * 设置 errorMsg 
     * @param errorMsg the errorMsg to set 
     */
    public void setErrorMsg(String errorMsg) {
    
        this.errorMsg = errorMsg;
    }
    
    

}

