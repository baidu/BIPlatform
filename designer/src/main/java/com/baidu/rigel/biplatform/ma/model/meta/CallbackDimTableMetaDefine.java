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
package com.baidu.rigel.biplatform.ma.model.meta;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * 回调维度元数据表定义
 * @author david.wang
 * @version 1.0.0.1
 */
public class CallbackDimTableMetaDefine extends DimTableMetaDefine implements Serializable {
    
    /**
     * 刷新时间间隔配置key
     */
    public static final String REF_INTERNAL_KEY = "refresh_internal";
    
    /**
     * 是否启用刷新配置key
     */
    public static final String REFRESH_KEY = "refresh";
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 7946501110609325060L;
    
    /**
     * 回调维度的rest请求URL
     */
    private String url;
    
    /**
     * 回调维度的配置信息，比如是否刷新缓存、缓存刷新时间间隔等 考虑后续扩展，这里采用Map定义
     */
    private Map<String, String> configuration;
    
    /**
     * callback参数
     */
    private Map<String, String> params;
    
    public CallbackDimTableMetaDefine() {
        this.configuration = Maps.newHashMap();
        this.params = Maps.newHashMap();
    }
    
    /**
     * 
     * @return params
     */
    public Map<String, String> getParams() {
        if (this.params == null) {
            this.params = Maps.newHashMap();
        }
        return params;
    }

    /**
     * 
     * @param params
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * 
     * @return 回调维度配置信息
     */
    public Map<String, String> getConfiguration() {
        // return Collections.unmodifiableMap(configuration);
        // 如果为空，则新建
        if (this.configuration == null) {
            this.configuration = Maps.newHashMap();
        }
        return configuration;
    }
    
    /**
     * 
     * @param configuration 回调维度配置信息
     */
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
    
    /**
     * 添加或者更新配置信息
     * 
     * @param key key
     * @param value value
     */
    public void addConfigItem(String key, String value) {
        if (this.configuration == null) {
            this.configuration = Maps.newHashMap();
        }
        this.configuration.put(key, value);
    }
    
    /**
     * 删除配置信息
     * 
     * @param key key
     */
    public void removeConfigItem(String key) {
        if (this.configuration == null) {
            return;
        }
        if (this.configuration.containsKey(key)) {
            this.configuration.remove(key);
        }
    }
    
    /**
     * 重置配置信息，清除所有配置薪资
     */
    public void resetConfiguration() {
        if (this.configuration == null) {
            return;
        }
        this.configuration.clear();
    }
    
    /**
     * 
     * @return url信息
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * 
     * 设置回调请求路径，如果请求路径为null或者空字符串，将抛出IllegalArgumentException异常
     * 
     * @param url
     *            回调URL
     */
    public void setUrl(String url) {
        if (url == null || url.trim().length() == 0) {
            throw new IllegalArgumentException("URL 不能为空 ！");
        }
        this.url = url;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public StandardDimType getDimType() {
        return StandardDimType.CALLBACK;
    }
    
}
