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
package com.baidu.rigel.biplatform.ma.model.ds;

import java.io.Serializable;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.google.common.collect.Maps;

/**
 * 
 * 数据源定义
 * 
 * @author david.wang
 *
 */
public class DataSourceDefine implements Serializable {
    
    /**
     * serialize id
     */
    private static final long serialVersionUID = 362406658554196705L;
    
    /**
     * id 无业务含义
     */
    private String id;
    
    /**
     * 数据库名称（唯一）
     */
    private String name;
    
    /**
     * 产品线（必填）
     */
    private String productLine;
    
    /**
     * 数据库用户名称
     */
    private String dbUser;
    
    /**
     * 数据库密码
     */
    private String dbPwd;
    
    /**
     * 数据库名称
     */
    private String dbInstance;
        
    /**
     * 编码格式
     */
    private String encoding;
    
    /**
     * 连接字符串
     */
    private String hostAndPort;
    
    private Map <String, String> properties = null;
    
    /**
     * 数据来源类型
     */
    private DataSourceType dataSourceType = DataSourceType.MYSQL;
        
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getProductLine() {
        return productLine;
    }
    
    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }
        
    public String getDbUser() {
        return dbUser;
    }
    
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }
    
    public String getDbPwd() {
        return this.dbPwd;
    }
    
    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }
    
    public String getDbInstance() {
        return dbInstance;
    }
    
    public void setDbInstance(String dbInstance) {
        this.dbInstance = dbInstance;
    }
    
    public String getHostAndPort() {
        return hostAndPort;
    }
    
    public void setHostAndPort(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }
    
    /**
     * 
     * 将数据源定义转换成json描述
     * 
     * @return json描述的数据源定义信息
     * 
     */
    public String toJson() {
        return GsonUtils.toJson(this);
    }
    
//    /**
//     * 
//     * 将json转换成DataSource定义
//     * 
//     * @return 数据源定义信息
//     * 
//     */
//    public static DataSourceDefine fromJson(String json) {
//        return GsonUtils.fromJson(json, DataSourceDefine.class);
//    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }
    
    /**
     * @param encoding
     *            the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return the sourceType
     */
    public DataSourceType getDataSourceType() {
        if (this.dataSourceType == null) {
            return DataSourceType.MYSQL;
        }
        return this.dataSourceType;
    }

    /**
     * @param sourceType the sourceType to set
     */
    public void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType != null) {
            this.dataSourceType = dataSourceType;
        }
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        if (this.properties == null) {
            this.properties = Maps.newHashMap ();
        }
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }   
    
    
}
