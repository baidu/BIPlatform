
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
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.MeasureType;

/** 
 *  
 * @author xiaoming.chen
 * @version  2015年1月14日 
 * @since jdk 1.8 or after
 */
public class CallbackMeasure extends MiniCubeMeasure {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -2968393330082175364L;
    
    /**
     * callbackUrl Callback的URL,URL支持多个host，多个host以逗号分隔后放到中括号中 如 http://[127.0.0.1:8080,123.1.1.1:8080]/abc/abc.action
     * url中支持占位符，占位符以$开头，$结尾，如： http://${url}/abc/${action}.action 请求的时候，需要把占位符放入callbackParams中，以上url的Callbackparams必须
     * 有 url:127.0.0.1,action:callback，系统在请求Callback url的时候会自动替换url中占位符
     */
    private String callbackUrl;

    /**
     * callbackParams Callback的参数
     */
    private Map<String, String> callbackParams;
    
    
    /** 
     * socketTimeOut Callback请求的超时时间
     */
    private long socketTimeOut = 30000;
    
    /** 
     * 构造函数
     */
    public CallbackMeasure(String name) {
        super(name);
    }

    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.ac.model.Measure#getAggregator() 
     */
    @Override
    public Aggregator getAggregator() {
        // Callback指标的聚集类型也设置成计算类型，统一进行合并处理
        return Aggregator.CALCULATED;

    }

    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.ac.model.Measure#getType() 
     */
    @Override
    public MeasureType getType() {
        return MeasureType.CALLBACK;

    }


    /** 
     * 获取 callbackUrl 
     * @return the callbackUrl 
     */
    public String getCallbackUrl() {
    
        return callbackUrl;
    }


    /** 
     * 设置 callbackUrl 
     * @param callbackUrl the callbackUrl to set 
     */
    public void setCallbackUrl(String callbackUrl) {
    
        this.callbackUrl = callbackUrl;
    }


    /** 
     * 获取 callbackParams 
     * @return the callbackParams 
     */
    public Map<String, String> getCallbackParams() {
    
        return callbackParams;
    }


    /** 
     * 设置 callbackParams 
     * @param callbackParams the callbackParams to set 
     */
    public void setCallbackParams(Map<String, String> callbackParams) {
    
        this.callbackParams = callbackParams;
    }


    /** 
     * 获取 socketTimeOut 
     * @return the socketTimeOut 
     */
    public long getSocketTimeOut() {
    
        return socketTimeOut;
    }


    /** 
     * 设置 socketTimeOut 
     * @param socketTimeOut the socketTimeOut to set 
     */
    public void setSocketTimeOut(long socketTimeOut) {
    
        this.socketTimeOut = socketTimeOut;
    }

}

