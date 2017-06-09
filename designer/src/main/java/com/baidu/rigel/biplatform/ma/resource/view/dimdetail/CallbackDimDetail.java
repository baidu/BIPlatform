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
package com.baidu.rigel.biplatform.ma.resource.view.dimdetail;

import java.io.Serializable;

/**
 * 
 * 回调维度定义
 * @author zhongyi
 *
 *         2014-7-31
 */
public class CallbackDimDetail implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3065957845448569193L;
    
    /**
     * 回调地址
     */
    private String address;
    
    /**
     * 刷新类型
     */
    private int refreshType;
    
    /**
     * 时间间隔
     */
    private int interval;
    
    /**
     * 维度id
     */
    private String currDim;
    
    /**
     * get the address
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * set the address
     * 
     * @param address
     *            the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * get the refreshType
     * 
     * @return the refreshType
     */
    public int getRefreshType() {
        return refreshType;
    }
    
    /**
     * set the refreshType
     * 
     * @param refreshType
     *            the refreshType to set
     */
    public void setRefreshType(int refreshType) {
        this.refreshType = refreshType;
    }
    
    /**
     * get the interval
     * 
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }
    
    /**
     * set the interval
     * 
     * @param interval
     *            the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }
    
    /**
     * get the currDim
     * 
     * @return the currDim
     */
    public String getCurrDim() {
        return currDim;
    }
    
    /**
     * set the currDim
     * 
     * @param currDim
     *            the currDim to set
     */
    public void setCurrDim(String currDim) {
        this.currDim = currDim;
    }
}