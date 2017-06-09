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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户自定义维度表
 * 
 * @author david.wang
 *
 */
public class UserDefineDimTableMetaDefine extends DimTableMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -4029716475434013381L;
    
    /**
     * 维度来源
     */
    private DimSourceType sourceType = DimSourceType.SQL;
    
    /**
     * 来源值，指定sql信息或者文件路径或者其他信息
     */
    private String value;
    
    /**
     * 配置信息
     */
    private Map<String, String> configuration;
    
    /**
     * 参数定义
     */
    private Map<String, String> params;
    
    public UserDefineDimTableMetaDefine() {
        this.configuration = new HashMap<String, String>();
    }
    
    /**
     * 
     * @return params
     */
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }
    
    /**
     * 
     * @param params
     *            params
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    
    /**
     * 
     * @return value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 
     * @param value
     *            value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public StandardDimType getDimType() {
        return StandardDimType.USERDEFINE;
    }
    
    /**
     * 
     * @return 来源值
     */
    public DimSourceType getSourceType() {
        return sourceType;
    }
    
    /**
     * 
     * @param sourceType
     * @see DimSourceType
     */
    public void setSourceType(DimSourceType sourceType) {
        this.sourceType = sourceType;
    }
    
    /**
     * 添加或者更新配置信息
     * 
     * @param key
     * @param value
     */
    public void addConfigItem(String key, String value) {
        this.configuration.put(key, value);
    }
    
    /**
     * 删除配置信息
     * 
     * @param key
     */
    public void removeConfigItem(String key) {
        this.configuration.remove(key);
    }
    
    /**
     * 重置配置信息，清除所有配置薪资
     */
    public void resetConfiguration() {
        this.configuration.clear();
    }
    
    /**
     * 
     * 配置信息默认可选择的key值列表，主要为来自于SQL的维度配置提供支持
     * 
     * @author david.wang
     *
     */
    public static interface DefaultConfigKey {
        
        /**
         * 数据源名称
         */
        String DATASOURCE_NAME = "datasource_name";
        
        /**
         * 数据库连接
         */
        String DB_CONN_URL = "db_conn_url";
        
        /**
         * 数据库用户名
         */
        String DB_USER = "db_user";
        
        /**
         * 数据库密码
         */
        String DB_PWD = "db_pwd";
        
        /**
         * 文件地址
         */
        String FILE_LOCATION = "file_location";
    }
}
