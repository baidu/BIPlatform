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
package com.baidu.rigel.biplatform.ma.report.query;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

/**
 * 请求上下文
 * 
 * @author zhongyi
 *
 *         2014-8-5
 */
public class QueryContext implements Serializable {
    
    /**
     * QueryContext.java -- long
     * description:
     */
    private static final long serialVersionUID = -8916777353652443192L;

    /**
     * params
     */
    private Map<String, Object> params;
    
    /**
     * extendAreaId
     */
    private String extendAreaId;
    
    /**
     * get the params
     * 
     * @return the params
     */
    public Map<String, Object> getParams() {
        if (this.params == null) {
            this.params = Maps.newHashMap();
        }
        return params;
    }
    
    /**
     * set the params
     * 
     * @param params
     *            the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * get the extendAreaId
     * 
     * @return the extendAreaId
     */
    public String getExtendAreaId() {
        return extendAreaId;
    }
    
    /**
     * set the extendAreaId
     * 
     * @param extendAreaId
     *            the extendAreaId to set
     */
    public void setExtendAreaId(String extendAreaId) {
        this.extendAreaId = extendAreaId;
    }
    
    /**
     * 将全局变量放入上下文
     * 
     * @param itemId
     * @param value
     */
    public void put(String id, Object value) {
        
        if (this.params == null) {
            this.params = new ConcurrentHashMap<String, Object>();
        }
        if (StringUtils.isNotEmpty(id) && value != null) {
            this.params.put(id, value);
        }
    }
    
    /**
     * 从上下文获取值
     * 
     * @param id
     * @return 存在返回值，否则返回null
     */
    public Object get(String id) {
        if (this.params == null) {
            return null;
        }
        return this.params.get(id);
    }
    
    /**
     * 
     */
    public void reset() {
        if (this.params != null) {
            this.params.clear();
        }
    }

    public void removeParam(String id) {
        this.getParams().remove(id);
    }
    
}