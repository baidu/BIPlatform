
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

import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContextSplitResult;


/**
 * 类IllegalSplitResultException.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年12月9日 下午8:18:01
 */
public class IllegalSplitResultException extends RuntimeException {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -7818981920156121112L;
    
    private QueryContextSplitResult splitResult;
    
    private String message;

    private String action;
    
    /** 
     * 构造函数
     */
    public IllegalSplitResultException(QueryContextSplitResult splitResult, String message, String action) {
        super("catch error in:" + action + " error message:" + message + " splitResult:" + splitResult);
        this.splitResult = splitResult;
        this.message = message;
        this.action = action;
    }

    /** 
     * 获取 splitResult 
     * @return the splitResult 
     */
    public QueryContextSplitResult getSplitResult() {
    
        return splitResult;
    }

    /** 
     * 设置 splitResult 
     * @param splitResult the splitResult to set 
     */
    public void setSplitResult(QueryContextSplitResult splitResult) {
    
        this.splitResult = splitResult;
    }

    /** 
     * 获取 message 
     * @return the message 
     */
    public String getMessage() {
    
        return message;
    }

    /** 
     * 设置 message 
     * @param message the message to set 
     */
    public void setMessage(String message) {
    
        this.message = message;
    }

    /** 
     * 获取 action 
     * @return the action 
     */
    public String getAction() {
    
        return action;
    }

    /** 
     * 设置 action 
     * @param action the action to set 
     */
    public void setAction(String action) {
    
        this.action = action;
    }
    
    

}

