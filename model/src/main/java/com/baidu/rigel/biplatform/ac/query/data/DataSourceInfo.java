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
package com.baidu.rigel.biplatform.ac.query.data;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;

/**
 * query datasource info
 * 
 * @author xiaoming.chen
 *
 */
public interface DataSourceInfo extends Serializable {

    /**
     * JDBC_CHECKTIMEOUT_KEY
     */
    public static final String JDBC_CHECKTIMEOUT_KEY = "jdbc.checkoutTimeout";

    /**
     * JDBC_MAXIDLETIME_KEY
     */
    public static final String JDBC_MAXIDLETIME_KEY = "jdbc.maxIdleTime";

    /**
     * JDBC_IDLECONNECTIONTESTPERIOD_KEY
     */
    public static final String JDBC_IDLECONNECTIONTESTPERIOD_KEY = "jdbc.idleConnectionTestPeriod";

    /**
     * JDBC_INITIALPOOLSIZE_KEY
     */
    public static final String JDBC_INITIALPOOLSIZE_KEY = "jdbc.initialPoolSize";

    /**
     * JDBC_MINPOOLSIZE_KEY
     */
    public static final String JDBC_MINPOOLSIZE_KEY = "jdbc.minPoolSize";

    /**
     * JDBC_MAXPOOLSIZE_KEY
     */
    public static final String JDBC_MAXPOOLSIZE_KEY = "jdbc.maxPoolSize";

    /**
     * JDBC_CONNECTION_ENCODING_KEY
     */
    public static final String JDBC_CHARACTERENCODING_ENCODING_KEY = "characterEncoding";

    /**
     * JDBC_CHARACTERENCODING_ENCODING default encoding
     */
    public static final String JDBC_CHARACTERENCODING_ENCODING = "utf8";

    /**
     * MYSQL_DRIVERMANAGER
     */
    public static final String MYSQL_DRIVERMANAGER = "com.mysql.jdbc.Driver";

    /**
     * H2_DRIVERMANAGER
     */
    public static final String H2_DRIVERMANAGER = "org.h2.Driver";

    /**
     * ORACLE_DRIVERMANAGER
     */
    public static final String ORACLE_DRIVERMANAGER = "Oracle.jdbc.driver.OracleDriver";
    
    /**
     * DRUID_DRIVERMANAGER
     */
    public static final String DRUID_DRIVERMANAGER = "com.baidu.rigel.druid.sql.jdbc.DruidDriver";
    
    /**
     * JDBC_CHECKTIMEOUT default
     */
    public static final String JDBC_CHECKTIMEOUT = "5000";

    /**
     * JDBC_MAXIDLETIME default
     */
    public static final String JDBC_MAXIDLETIME = "3600";

    /**
     * JDBC_IDLECONNECTIONTESTPERIOD default
     */
    public static final String JDBC_IDLECONNECTIONTESTPERIOD = "1800";

    /**
     * JDBC_INITIALPOOLSIZE default
     */
    public static final String JDBC_INITIALPOOLSIZE = "3";

    /**
     * JDBC_MINPOOLSIZE default
     */
    public static final String JDBC_MINPOOLSIZE = "3";

    /**
     * JDBC_MAXPOOLSIZE default
     */
    public static final String JDBC_MAXPOOLSIZE = "30";

    /**
     * get datasource type
     * 
     * @return datasource type
     */
    DataSourceType getDataSourceType();

    /**
     * 返回数据源信息是否可用
     * 
     * @return 可用性
     */
    boolean validate();

    /**
     * 返回数据源的唯一ID
     * 
     * @return DataSource unique key
     */
    String getDataSourceKey();

    /**
     * 取得产品线信息，数据源对应的产品线
     * 
     * @return 数据源对应的产品线
     */
    String getProductLine();

}
