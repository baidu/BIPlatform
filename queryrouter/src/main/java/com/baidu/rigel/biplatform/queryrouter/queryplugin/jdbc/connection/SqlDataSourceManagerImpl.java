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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 数据库的数据源管理实现
 * 
 * @author xiaoming.chen
 */
public class SqlDataSourceManagerImpl implements DataSourceManager {
    
    /**
     * DATASOURCEKEY_SEPRATE 数据源KEY的连接字符
     */
    public static final String DATASOURCEKEY_SEPRATE = "|";
    /**
     * log log4j
     */
    private Logger log = Logger.getLogger(this.getClass());
    /**
     * instance 数据库的数据源管理实例
     */
    private static SqlDataSourceManagerImpl instance;
    
    /**
     * dynamicDataSources 一个服务上一般启动的数据源是有限的
     */
    private Map<String, DynamicSqlDataSource> dynamicDataSources = new ConcurrentHashMap<String, DynamicSqlDataSource>(
            10);
    
    /**
     * checkUnusedDataSourceThread 校验数据源可用性的线程池
     */
    private ExecutorService checkUnusedDataSourceThread = null;
    
    /**
     * expireDataSource 默认一天没用就过期，以后需要做成可配置
     */
    private long expireDataSource = 24 * 3600 * 1000;
    
    /**
     * private constructor
     */
    private SqlDataSourceManagerImpl() {
    }
    
    /**
     * 获取数据库数据源管理的实例
     * 
     * @return 数据源管理实例
     */
    public static synchronized SqlDataSourceManagerImpl getInstance() {
        if (instance == null) {
            instance = new SqlDataSourceManagerImpl();
            if (instance.checkUnusedDataSourceThread == null) {
                instance.checkUnusedDataSourceThread = Executors.newFixedThreadPool(1);
                
                // instance.checkUnusedDataSourceThread.submit(() -> {
                // while (true) {
                // instance.log.info("check datasource unused begin..");
                // instance.expireDataSource();
                // instance.log.info("check datasource unused end..");
                // try {
                // Thread.sleep(60000);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // }
                // });
                
                instance.checkUnusedDataSourceThread.submit(new Runnable() {
                    public void run() {
                        while (true) {
                            instance.log.debug("check datasource unused begin..");
                            instance.expireDataSource();
                            instance.log.debug("check datasource unused end..");
                            try {
                                Thread.sleep(60000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                
            }
        }
        
        return instance;
    }
    
    private void expireDataSource() {
        if (MapUtils.isNotEmpty(dynamicDataSources)) {
            for (Iterator<Map.Entry<String, DynamicSqlDataSource>> it = dynamicDataSources
                    .entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, DynamicSqlDataSource> entry = it.next();
                if ((System.currentTimeMillis() - entry.getValue()
                        .getLastSuccessGetDataSourcTime()) > expireDataSource) {
                    log.info("datasource:" + entry.getKey()
                            + " is no used for 1 day, remove datasource");
                    try {
                        entry.getValue().destroy();
                        log.info("destroy datasource:" + entry.getKey());
                        it.remove();
                        log.info("remove datasource:" + entry.getKey());
                    } catch (Exception e) {
                        log.warn("destroy datasource error:" + entry.getKey());
                        e.printStackTrace();
                    }
                } else {
                    entry.getValue().clearDataSourceFailCount();
                    
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.tesseract.datasource.DataSourceManager#getDataSourceByKey
     * (java.lang.String)
     */
    @Override
    public DataSourceWrap getDataSourceByKey(String key) throws DataSourceException {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("can not get datasource by empty key!");
        }
        DynamicSqlDataSource dynamicDataSource = dynamicDataSources.get(key);
        if (dynamicDataSource == null) {
            throw new DataSourceException("can not found datasource by key:" + key);
        }
        SqlDataSourceWrap dataSource = null;
        try {
            dataSource = dynamicDataSource.getDataSource();
            // 设置最新成功获取时间戳
            dynamicDataSource.setLastSuccessGetDataSourcTime(System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("get datasource error", e);
            throw new DataSourceException("get datasource error,msg:" + e.getMessage(), e);
        }
        // 返回DataSource的代理类
        return dataSource;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.tesseract.datasource.DataSourceManager#initDataSource(
     * com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public synchronized void initDataSource(DataSourceInfo dataSourceInfo)
            throws DataSourceException {
        if (dataSourceInfo == null) {
            throw new IllegalArgumentException(
                    "can not init datasource,because dataSourceInfo is null or has init yet..:"
                            + dataSourceInfo);
        }
        // sqldatasource 类型转换
        SqlDataSourceInfo sqlDataSourceInfo = (SqlDataSourceInfo) dataSourceInfo;
        
        // 创建动态数据源添加到管理列表
        if (!dynamicDataSources.containsKey(dataSourceInfo.getDataSourceKey())) {
            dynamicDataSources.put(dataSourceInfo.getDataSourceKey(),
                    createDynamicDataSource(sqlDataSourceInfo));
        }
    }
    
    /**
     * 根据datasourceinfo 创建动态数据源
     * 
     * @param dataSourceInfo
     *            数据源信息
     * @return 动态数据源
     */
    private DynamicSqlDataSource createDynamicDataSource(SqlDataSourceInfo dataSourceInfo)
            throws DataSourceException {
        Map<String, SqlDataSourceWrap> dataSources = new HashMap<String, SqlDataSourceWrap>();
        
        for (int i = 0; i < dataSourceInfo.getHosts().size(); i++) {
            
            String dataSourceKey = generateDataSourceKey(dataSourceInfo.getProductLine(),
                    dataSourceInfo.getHosts().get(i), dataSourceInfo.getInstanceName(),
                    dataSourceInfo.getUsername(),
                    Md5Util.encode(dataSourceInfo.toString()))
                    .replace(":", DATASOURCEKEY_SEPRATE);
            if (dataSourceInfo.isDBProxy() || dataSourceInfo.getDataBase() == DataBase.DRUID) {
                // dbproxy 或 druid 需要直连
                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName(dataSourceInfo.getDataBase().getDriver());
                dataSource.setUrl(dataSourceInfo.getJdbcUrls().get(i));
                dataSource.setUsername(dataSourceInfo.getUsername());
                try {
                    dataSource.setPassword(dataSourceInfo.getPassword());
                } catch (Exception e) {
                    log.error("set dataSource password error," + dataSourceInfo, e);
                    throw new DataSourceException("set dataSource password error," + dataSourceInfo, e);
                }
                dataSources.put(dataSourceKey, new SqlDataSourceWrap(dataSource));
            } else {
                ComboPooledDataSource dataSource = new ComboPooledDataSource();
                dataSource.setDataSourceName(dataSourceKey);
                try {
                    dataSource.setDriverClass(dataSourceInfo.getDataBase().getDriver());
                } catch (PropertyVetoException e) {
                    log.error("set dataSource driverclass error," + dataSourceInfo, e);
                    // 不知道为啥会抛异常，包装一下，扔出去
                    throw new DataSourceException("set c3p0 driverclass error when create datasource with:"
                            + dataSourceInfo, e);
                }
                dataSource.setJdbcUrl(dataSourceInfo.getJdbcUrls().get(i));
                dataSource.setUser(dataSourceInfo.getUsername());
                try {
                    dataSource.setPassword(dataSourceInfo.getPassword());
                } catch (Exception e) {
                    log.error("set dataSource password error," + dataSourceInfo, e);
                    throw new DataSourceException("set dataSource password error," + dataSourceInfo, e);
                }
                
                dataSource.setInitialPoolSize(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                        DataSourceInfo.JDBC_INITIALPOOLSIZE_KEY, DataSourceInfo.JDBC_INITIALPOOLSIZE)));
                dataSource.setMaxPoolSize(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                        DataSourceInfo.JDBC_MAXPOOLSIZE_KEY, DataSourceInfo.JDBC_MAXPOOLSIZE)));
                dataSource.setMinPoolSize(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                        DataSourceInfo.JDBC_MINPOOLSIZE_KEY, DataSourceInfo.JDBC_MINPOOLSIZE)));
             
                dataSource
                        .setIdleConnectionTestPeriod(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                                DataSourceInfo.JDBC_IDLECONNECTIONTESTPERIOD_KEY,
                                DataSourceInfo.JDBC_IDLECONNECTIONTESTPERIOD)));
                dataSource.setMaxIdleTime(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                        DataSourceInfo.JDBC_MAXIDLETIME_KEY, DataSourceInfo.JDBC_MAXIDLETIME)));
                dataSource.setCheckoutTimeout(Integer.valueOf(dataSourceInfo.getConnectionProperties(
                        DataSourceInfo.JDBC_CHECKTIMEOUT_KEY, DataSourceInfo.JDBC_CHECKTIMEOUT)));
                log.info("add datasource info into c3p0 pool success:" + dataSourceInfo);

                dataSources.put(dataSourceKey, new SqlDataSourceWrap(dataSource));
            }
        }
        return new DynamicSqlDataSource(dataSources);
    }
    
    /**
     * 生成动态数据源内部管理数据源的KEY generateDataSourceKey
     * 
     * @param productLine
     * @param address
     *            数据库地址
     * @param instance
     *            数据库名
     * @param user
     *            用户名
     * @param md5
     * @return 返回数据源的KEY
     */
    private String generateDataSourceKey(String productLine, String address, String instance,
            String user, String md5) {
        List<String> params = new ArrayList<>();
        params.add(productLine);
        params.add(address);
        params.add(instance);
        params.add(user);
        params.add(md5);
        
        return StringUtils.join(params, DATASOURCEKEY_SEPRATE);
    }
    
    @Override
    public void removeDataSource(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("can not remove datasoucr by empty key.");
        }
        if (dynamicDataSources.containsKey(key)) {
            DynamicSqlDataSource datasource = dynamicDataSources.remove(key);
            log.info("found dynamic datasource by key:" + key);
            try {
                datasource.destroy();
            } catch (Exception e) {
                log.warn("destroy dynamic datasource error.", e);
            }
            
        } else {
            log.warn("no datasource found by key:" + key);
        }
    }
    
    /**
     * get expireDataSource
     * 
     * @return the expireDataSource
     */
    public long getExpireDataSource() {
        return expireDataSource;
    }
    
    /**
     * set expireDataSource with expireDataSource
     * 
     * @param expireDataSource
     *            the expireDataSource to set
     */
    public void setExpireDataSource(long expireDataSource) {
        this.expireDataSource = expireDataSource;
    }
    
    @Override
    public void updateDataSource(DataSourceInfo dataSourceInfo) throws DataSourceException {
        // TODO Auto-generated method stub
        
    }
    
}
