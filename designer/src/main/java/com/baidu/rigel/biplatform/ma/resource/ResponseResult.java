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
package com.baidu.rigel.biplatform.ma.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Rest Service结果集对象，封装rest返回值
 * 
 * @author david.wang
 *
 */
public class ResponseResult implements Serializable {
    
    /**
     * 操作成功
     */
    public static final int SUCCESS = 0;
    
    /**
     * 操作失败
     */
    public static final int FAILED = 1;

    /**
     * serialize id
     */
    private static final long serialVersionUID = -4826258907545698916L;
    
    /**
     * 返回状态信息，如提示信息、错误描述信息等
     */
    private String statusInfo;
    
    /**
     * 返回状态，0代表成功处理业务，1代表处理业务出现异常
     */
    private int status;
    
    /**
     * 数据信息，如果需要返回数据，将数据封装到集合对象中返回
     */
    private Object data;
    
    /**
     * 其他未定属性信息
     */
    private Map<String, Serializable> properties = new HashMap<String, Serializable>();
    
    public String getStatusInfo() {
        return statusInfo;
    }
    
    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Map<String, ? extends Serializable> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
    }
    
    /**
     * 添加属性信息
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, Serializable value) {
        this.properties.put(key, value);
    }
    
    /**
     * 
     * {@inheritDoc}
     * 
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
