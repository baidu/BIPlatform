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
package com.baidu.rigel.biplatform.ma.model.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.baidu.rigel.biplatform.ma.model.exception.DBInfoReadException;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;
import com.google.common.collect.Lists;

/**
 * 数据库信息获取工具类
 * 
 * @author zhongyi
 * 
 *         2014-7-29
 */
public class DBInfoReader {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DBInfoReader.class);
    
    /**
     * 元数据对象
     */
    private DatabaseMetaData dbMetaData;
    
    /**
     * 链接对象
     */
    private Connection con;
    
    /**
     * 获得数据库元数据对象
     * 
     * @param type
     *            数据源类型
     * @param user
     *            用户名
     * @param password
     *            密码
     * @param url
     *            数据库地址
     * @param securityKey 
     * @return reader对象
     */
    public static DBInfoReader build(DataSourceType type, String user, String password,
        String url, String securityKey) {
        DBInfoReader reader = new DBInfoReader();
        DatabaseMetaData dbMetaData = null;
        Connection con = null;
        try {
            Class.forName(type.getDriver());
            String pwd = AesUtil.getInstance().decodeAnddecrypt(password, securityKey);
            logger.info("[INFO]--- --- --- --- connect to database with user : {}", user);
            StringBuilder pwdStr = new StringBuilder();
            for (char c : pwd.toCharArray()) {
                pwdStr.append(c >> 1);
            }
            logger.info("[INFO]--- --- --- --- connect to database with pwd : {}", pwdStr.toString());
            con = DriverManager.getConnection(url, user, pwd);
            dbMetaData = con.getMetaData();
        } catch (ClassNotFoundException e) {
            logger.error("[ERROR] --- --- --- --- connection to db error : {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DBInfoReadException("ClassNotFoundException when build DBInfoReader! ", e);
        } catch (Exception e) {
            logger.error("[ERROR] --- --- --- --- connection to db error : {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DBInfoReadException("SQLException when build DBInfoReader! ", e);
        }
        reader.setCon(con);
        reader.setDbMetaData(dbMetaData);
        return reader;
    }
    
    /**
     * 关闭链接
     */
    public void closeConn() {
        
        if (con != null) {
            try {
                this.con.close();
            } catch (SQLException e) {
                logger.debug(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获得数据库的一些相关信息
     */
    public void getDataBaseInformations() {
        try {
            logger.info("URL:" + dbMetaData.getURL() + ";");
            logger.info("UserName:" + dbMetaData.getUserName() + ";");
            logger.info("isReadOnly:" + dbMetaData.isReadOnly() + ";");
            logger.info("DatabaseProductName:" + dbMetaData.getDatabaseProductName() + ";");
            logger.info("DatabaseProductVersion:" + dbMetaData.getDatabaseProductVersion()
                + ";");
            logger.info("DriverName:" + dbMetaData.getDriverName() + ";");
            logger.info("DriverVersion:" + dbMetaData.getDriverVersion());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    /**
     * 获得该用户下面的所有表
     * 
     * @return 所有表名称
     */
    public List<TableInfo> getAllTableInfos() {
        List<TableInfo> tableInfos = Lists.newArrayList();
        try {
            // table type. Typical types are "TABLE", "VIEW", "SYSTEM
            // TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
            // "SYNONYM".
            String[] types = {"TABLE"};
            ResultSet rs = dbMetaData.getTables(null, null, "%", types);
            while (rs.next()) {
                TableInfo info = new TableInfo();
                String tableName = rs.getString("TABLE_NAME");
                String comment = tableName; 
                info.setId(tableName);
                info.setName(StringUtils.hasText(comment) ? comment : tableName);
                tableInfos.add(info);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return tableInfos;
    }
    
    /**
     * 获得该用户下面的所有表
     * 
     * @return 所有表名称
     */
    public List<ColumnInfo> getColumnInfos(String tableId) {
        List<ColumnInfo> colInfos = Lists.newArrayList();
        try {
            ResultSet rs = dbMetaData.getColumns(null, null, tableId, "%");
            while (rs.next()) {
                String colId = rs.getString("COLUMN_NAME");
                String colName = rs.getString("REMARKS");
                ColumnInfo info = new ColumnInfo();
                info.setId(colId);
                info.setName(StringUtils.hasText(colName) ? colName : colId);
                colInfos.add(info);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return colInfos;
    }
    
    /**
     * get the dbMetaData
     * 
     * @return the dbMetaData
     */
    public DatabaseMetaData getDbMetaData() {
        return dbMetaData;
    }
    
    /**
     * set the dbMetaData
     * 
     * @param dbMetaData
     *            the dbMetaData to set
     */
    public void setDbMetaData(DatabaseMetaData dbMetaData) {
        this.dbMetaData = dbMetaData;
    }
    
    /**
     * get the con
     * 
     * @return the con
     */
    public Connection getCon() {
        return con;
    }
    
    /**
     * set the con
     * 
     * @param con
     *            the con to set
     */
    public void setCon(Connection con) {
        this.con = con;
    }
}