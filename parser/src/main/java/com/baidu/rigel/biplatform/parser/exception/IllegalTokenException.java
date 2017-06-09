
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
 * 非法的关键词异常
 * @author xiaoming.chen
 * @version  2014年12月10日 
 * @since jdk 1.8 or after
 */
public class IllegalTokenException extends RuntimeException {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 7409746069697696035L;
    
    /** 
     * token 关键词
     */
    private String token;
    
    
    /** 
     * msg 错误标识
     */
    private String msg;

    
    /** 
     * 构造函数
     */
    public IllegalTokenException(String token, String msg) {
        super("illegal token:" + token + ",error code:" + msg);
        this.token = token;
        this.msg = msg;
    }


    /** 
     * 获取 token 
     * @return the token 
     */
    public String getToken() {
    
        return token;
    }


    /** 
     * 设置 token 
     * @param token the token to set 
     */
    public void setToken(String token) {
    
        this.token = token;
    }


    /** 
     * 获取 msg 
     * @return the msg 
     */
    public String getMsg() {
    
        return msg;
    }


    /** 
     * 设置 msg 
     * @param msg the msg to set 
     */
    public void setMsg(String msg) {
    
        this.msg = msg;
    }

    
    
}

