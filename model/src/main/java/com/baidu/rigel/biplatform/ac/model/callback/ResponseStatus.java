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
package com.baidu.rigel.biplatform.ac.model.callback;

/**
 * Description:
 *     Callback请求状态定义：用于描述callback服务的请求状态
 * @author david.wang
 *
 */
public enum ResponseStatus {
    
    /**
     * SUCCESS
     */
    SUCCESS(2000),
    
    /**
     * 未知服务
     */
    UN_KNOW_SERVICE(404),
    
    /**
     * 未受支持的方法、请求
     */
    UN_SUPPORTED_METHOD(405),
    
    /**
     * 未提供用户身份信息
     */
    NOT_PROVIDE_USER_ID(5001),
    
    /**
     * 用户身份信息非法或不正确
     */
    INVALIDATE_USER_ID(5002),
    
    /**
     * 用户未授权
     */
    UN_AUTH(5003),
    
    /**
     * 参数个数少于必须值
     */
    INVALIDATE_PARAM_NUM(6001),
    
    /**
     * 参数缺失
     */
    MIS_PARAM(6002),
    
    /**
     * 参数未赋值
     */
    PARAM_NOT_ASSIGN_VALUE(6003),
    
    /**
     * 参数类型不匹配
     */
    INVALID_PARAM_TYPE(6004),
    
    /**
     * 未知参数错误
     */
    UNKNOW_PARAMS(6005),
    
    /**
     * 未提供cookie信息
     */
    NOT_CONTENT_COOKIE(7001),
    
    /**
     * cookie值未null
     */
    COOKIE_VALUE_IS_NULL (7002),
    
    /**
     * 服务提供方服务异常
     */
    INTERNAL_SERVER_ERROR(5000);
    
    /**
     * value
     */
    private int value;
    
    /**
     * 构造函数
     * @param value
     */
    private ResponseStatus (int value) {
        this.value = value;
    }
    
    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
    
    
}

