
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

import java.util.List;

import com.baidu.rigel.biplatform.parser.node.Node;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月23日 
 * @since jdk 1.8 or after
 */
public class IllegalFunctionArgumentException extends RuntimeException {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -4578367889356066906L;
    
    private String functionName;
    
    private List<Node> args;
    
    private int argsLength;
    
    public static final String MESSAGE_PATTERN = "Function name:%s, expect args length is : %d, actually args : %s";

    
    /** 
     * 构造函数
     */
    public IllegalFunctionArgumentException(String functionName, List<Node> args, int argsLength) {
        super(String.format(MESSAGE_PATTERN, functionName, argsLength, args));
        this.functionName = functionName;
        this.args = args;
        this.argsLength = argsLength;
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
     * 获取 args 
     * @return the args 
     */
    public List<Node> getArgs() {
    
        return args;
    }


    /** 
     * 设置 args 
     * @param args the args to set 
     */
    public void setArgs(List<Node> args) {
    
        this.args = args;
    }


    /** 
     * 获取 argsLength 
     * @return the argsLength 
     */
    public int getArgsLength() {
    
        return argsLength;
    }


    /** 
     * 设置 argsLength 
     * @param argsLength the argsLength to set 
     */
    public void setArgsLength(int argsLength) {
    
        this.argsLength = argsLength;
    }

}

