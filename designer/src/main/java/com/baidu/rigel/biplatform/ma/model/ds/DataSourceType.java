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

/**
 * 
 * 数据来源类型
 * @author david.wang
 *
 */
public enum DataSourceType {    
    /**
     * excel 文件
     */
    EXCEL("", "", ""), 
    
    /**
     * csv 文件
     */
    CSV("", "", ""),
    
    /**
     * 普通文本文件
     */
    TXT("", "", ""),
    
    /**
     * HDFS文件系统
     */
    HDFS("", "", ""),
    
    /**
     * 列式数据存储介质
     */
    HIVE("", "", ""),
    /**
     * MYSQL
     */
    MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://", "/"),
    
    /**
     * MYSQL-DBPROXY
     */
    MYSQL_DBPROXY("com.mysql.jdbc.Driver", "jdbc:mysql://", "/"),
    
    /**
     * ORACLE
     */
    ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:", ":"),
    
    /**
     * H2
     */
    H2("org.h2.Driver", "jdbc:h2:tcp://", "/"),
    
    /**
     * PALO
     */
    PALO("com.mysql.jdbc.Driver", "jdbc:mysql://", "/"),
    
    /**
     * DRUID
     */
    DRUID("com.baidu.rigel.druid.sql.jdbc.DruidDriver", "jdbc:druid://", "/");
    
    /**
     * driver
     */
    private String driver;
    
    /**
     * 连接地址前缀
     */
    private String prefix;
    
    /**
     * 分隔符
     */
    private String div;
    
    /**
     * DatasourceType
     * 
     * @param id
     *            ID
     * @param name
     *            名称
     * @param driver
     *            驱动
     */
    private DataSourceType(String driver, String prefix, String div) {
        this.setDriver(driver);
        this.setPrefix(prefix);
        this.setDiv(div);
    }
    
    /**
     * get the driver
     * 
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }
    
    /**
     * set the driver
     * 
     * @param driver
     *            the driver to set
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.name();
    }
    
    /**
     * @return the div
     */
    public String getDiv() {
        return div;
    }
    
    /**
     * @param div
     *            the div to set
     */
    public void setDiv(String div) {
        this.div = div;
    }
    
    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * @param prefix
     *            the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
