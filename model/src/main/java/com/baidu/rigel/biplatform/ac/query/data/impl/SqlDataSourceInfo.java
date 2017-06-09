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
/**
 * 
 */
package com.baidu.rigel.biplatform.ac.query.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.AesUtil;

/**
 * sql data source info ,include connection infos
 * 
 * @author xiaoming.chen
 *
 */
public class SqlDataSourceInfo implements DataSourceInfo {

    /**
     * DATABASE_MYSQL_ID
     */
    public static final int DATABASE_MYSQL_ID = 1;

    /**
     * DATABASE_H2_ID
     */
    public static final int DATABASE_H2_ID = 2;

    /**
     * DATABASE_OTHER_ID
     */
    public static final int DATABASE_OTHER_ID = 3;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2854325607026426967L;

    /**
     * dataSourceKey 数据源的KEY
     */
    private String dataSourceKey;

    /**
     * hosts support multi host for example：127.0.0.1:8080
     */
    /**
     * hosts 数据库的地址和端口列表
     */
    private List<String> hosts;

    /**
     * jdbcUrls 根据数据库地址列表拼接而成的jdbc连接字符串
     */
    private List<String> jdbcUrls;

    /**
     * username db connect username
     */
    private String username;

    /**
     * password db connect password
     */
    private String password;

    /**
     * databaseName connect db name
     */
    private String instanceName;

    /**
     * isDBProxy 是否dbproxy，如果是的话，用DriverManagerDataSource连接池
     */
    private boolean isDBProxy;

    /**
     * dbPoolInfo 数据连接池信息
     */
    private Map<String, String> dbPoolInfo;

    /**
     * description 描述
     */
    private String description;

    /**
     * dataBase 数据库类型
     */
    private DataBase dataBase = DataBase.MYSQL;

    /**
     * productLine 产品线信息
     */
    private String productLine;

    /**
     * dataSourceType 类型
     */
    private DataSourceType dataSourceType = DataSourceType.SQL;

    /**
     * construct with
     * 
     * @param dataSourceKey 数据源的唯一标识
     */
    public SqlDataSourceInfo(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    /**
     * 数据库类型
     * 
     * @author xiaoming.chen
     *
     */
    public enum DataBase {
        /**
         * MYSQL
         */
        MYSQL(1, DataSourceInfo.MYSQL_DRIVERMANAGER),
        /**
         * PALO
         */
        PALO(6, DataSourceInfo.MYSQL_DRIVERMANAGER),
        
        /**
         * DRUID
         */
        DRUID(7, DataSourceInfo.DRUID_DRIVERMANAGER),
        
        /**
         * H2
         */
        H2(2, DataSourceInfo.H2_DRIVERMANAGER),
        /**
         * OTHER
         */
        OTHER(3, null),
        /**
         * ORACLE
         */
        ORACLE(4, DataSourceInfo.ORACLE_DRIVERMANAGER),
        
        /**
         * HIVE
         */
        HIVE(5, null),
        
        /**
         * SPARK
         */
        SPARK(8, null);

        /**
         * id 数据库ID
         */
        private int id;

        /**
         * driver 数据库连接driver
         */
        private String driver;

        /**
         * 构造函数
         * 
         * @param id 数据库ID
         * @param driverClass 数据库连接字符串
         * @return 数据库定义
         */
        private DataBase(int id, String driverClass) {
            this.id = id;
            this.driver = driverClass;
        }

        /**
         * getter method for property id
         * 
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * getter method for property driver
         * 
         * @return the driver
         */
        public String getDriver() {
            return driver;
        }

        /**
         * setter method for property driver
         * 
         * @param driver the driver to set
         */
        public void setDriver(String driver) {
            this.driver = driver;
        }

    }

    /**
     * getter method for property hosts
     * 
     * @return the hosts
     */
    public List<String> getHosts() {
        return hosts;
    }

    /**
     * setter method for property hosts
     * 
     * @param hosts the hosts to set
     */
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    /**
     * getter method for property username
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * setter method for property username
     * 
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * getter method for property password
     * 
     * @return the password
     * @throws Exception exception when decrypt password error
     */
    public String getPassword() throws Exception {
        return AesUtil.getInstance().decodeAnddecrypt(this.password);
    }

    /**
     * setter method for property password
     * 
     * @param password the password to set
     * @throws Exception throw exception when encrypt password error
     */
    public void setPassword(String password) throws Exception {
        this.password = AesUtil.getInstance().encryptAndUrlEncoding(password);
    }

    /**
     * getter method for property isDBProxy
     * 
     * @return the isDBProxy
     */
    public boolean isDBProxy() {
        return isDBProxy;
    }

    /**
     * setter method for property isDBProxy
     * 
     * @param isDBProxy the isDBProxy to set
     */
    public void setDBProxy(boolean isDBProxy) {
        this.isDBProxy = isDBProxy;
    }

    @Override
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "SqlDataSourceInfo [dataSourceKey=" + dataSourceKey + ", hosts=" + hosts + ", jdbcUrls=" + jdbcUrls
                + ", username=" + username + ", instanceName=" + instanceName + ", isDBProxy=" + isDBProxy
                + ", dbPoolInfo=" + dbPoolInfo + ", dataBase=" + dataBase + "]";
    }

    /**
     * getter method for property instanceName
     * 
     * @return the instanceName
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * setter method for property instanceName
     * 
     * @param instanceName the instanceName to set
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * getter method for property dbPoolInfo
     * 
     * @return the dbPoolInfo
     */
    public Map<String, String> getDbPoolInfo() {
        if (this.dbPoolInfo == null) {
            dbPoolInfo = new HashMap<String, String>();
        }
        return dbPoolInfo;
    }

    /**
     * setter method for property dbPoolInfo
     * 
     * @param dbPoolInfo the dbPoolInfo to set
     */
    public void setDbPoolInfo(Map<String, String> dbPoolInfo) {
        this.dbPoolInfo = dbPoolInfo;
    }

    @Override
    public boolean validate() {
        boolean validate =
                CollectionUtils.isEmpty(hosts) || StringUtils.isBlank(username) || StringUtils.isBlank(password)
                        || StringUtils.isBlank(instanceName) || StringUtils.isBlank(productLine)
                        || CollectionUtils.isEmpty(jdbcUrls) || jdbcUrls.size() != hosts.size();
        return !validate;
    }

    /**
     * getter method for property description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * setter method for property description
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 从DataSourceInfo中获取属性信息
     * 
     * @param key 获取属性的KEY
     * @param defaultValue 默认值，如果根据KEY获取不到，返回默认值
     * @return 属性
     */
    public String getConnectionProperties(String key, String defaultValue) {
        if (this.getDbPoolInfo().containsKey(key)) {
            return this.getDbPoolInfo().get(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * getter method for property dataBase
     * 
     * @return the dataBase
     */
    public DataBase getDataBase() {
        return dataBase;
    }

    /**
     * setter method for property dataBase
     * 
     * @param dataBase the dataBase to set
     */
    public void setDataBase(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    /**
     * setter method for property dataSourceKey
     * 
     * @param dataSourceKey the dataSourceKey to set
     */
    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    @Override
    public String getProductLine() {
        return this.productLine;
    }

    /**
     * setter method for property productLine
     * 
     * @param productLine the productLine to set
     */
    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    /**
     * getter method for property jdbcUrls
     * 
     * @return the jdbcUrls
     */
    public List<String> getJdbcUrls() {
        return jdbcUrls;
    }

    /**
     * setter method for property jdbcUrls
     * 
     * @param jdbcUrls the jdbcUrls to set
     */
    public void setJdbcUrls(List<String> jdbcUrls) {
        this.jdbcUrls = jdbcUrls;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataBase == null) ? 0 : dataBase.hashCode());
        result = prime * result + ((dataSourceKey == null) ? 0 : dataSourceKey.hashCode());
        result = prime * result + ((dataSourceType == null) ? 0 : dataSourceType.hashCode());
        result = prime * result + ((dbPoolInfo == null) ? 0 : dbPoolInfo.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((hosts == null) ? 0 : hosts.hashCode());
        result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
        result = prime * result + (isDBProxy ? 1231 : 1237);
        result = prime * result + ((jdbcUrls == null) ? 0 : jdbcUrls.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((productLine == null) ? 0 : productLine.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SqlDataSourceInfo other = (SqlDataSourceInfo) obj;
        if (dataBase != other.dataBase) {
            return false;
        }
        if (dataSourceKey == null) {
            if (other.dataSourceKey != null) {
                return false;
            }
        } else if (!dataSourceKey.equals(other.dataSourceKey)) {
            return false;
        }
        if (dataSourceType != other.dataSourceType) {
            return false;
        }
        if (dbPoolInfo == null) {
            if (other.dbPoolInfo != null) {
                return false;
            }
        } else if (!dbPoolInfo.equals(other.dbPoolInfo)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (hosts == null) {
            if (other.hosts != null) {
                return false;
            }
        } else if (!hosts.equals(other.hosts)) {
            return false;
        }
        if (instanceName == null) {
            if (other.instanceName != null) {
                return false;
            }
        } else if (!instanceName.equals(other.instanceName)) {
            return false;
        }
        if (isDBProxy != other.isDBProxy) {
            return false;
        }
        if (jdbcUrls == null) {
            if (other.jdbcUrls != null) {
                return false;
            }
        } else if (!jdbcUrls.equals(other.jdbcUrls)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (productLine == null) {
            if (other.productLine != null) {
                return false;
            }
        } else if (!productLine.equals(other.productLine)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDataSourceKey() {
        return this.dataSourceKey;
        
    }

}
