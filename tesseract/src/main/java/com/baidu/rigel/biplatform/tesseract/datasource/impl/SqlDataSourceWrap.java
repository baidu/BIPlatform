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
package com.baidu.rigel.biplatform.tesseract.datasource.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourceWrap;

/**
 * SQL数据源的封装
 * 
 * @author xiaoming.chen
 *
 */
public class SqlDataSourceWrap implements DataSourceWrap, DataSource {

    /**
     * dataSource 实际的SQL操作的DataSource
     */
    private DataSource dataSource;
    
    private int failCount;
    
    private long failTime;
    
    /**
     * construct with
     * 
     * @param dataSource
     */
    public SqlDataSourceWrap(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.tesseract.datasource.DataSourceWrape#getDataSourceType()
     */
    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.SQL;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public int getFailCount() {
        return this.failCount;
        
    }

    @Override
    public void increaseFailCount() {
        this.failCount++;
        this.failTime = System.currentTimeMillis();
    }

    @Override
    public void resetFailCount() {
        this.failCount = 0;
    }

    @Override
    public long getFailTime() {
        return this.failTime;
        
    }

}
