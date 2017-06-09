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
package com.baidu.rigel.biplatform.tesseract.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.datasource.impl.SqlDataSourceWrap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 动态数据源
 * 
 * @author xiaoming.chen
 *
 */
public class DynamicSqlDataSource {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSqlDataSource.class);

    /**
     * TEST_SQL 测试数据库连接的SQL
     */
    private static final String TEST_SQL = "select 1";

    /**
     * validInterval 检测频率
     */
    private long validInterval = 10000L;

    /**
     * dataSources 数据源
     */
    private Map<String, SqlDataSourceWrap> dataSources;

    /**
     * executor 检测数据源的线程池
     */
    private ExecutorService executor;

    /**
     * validSql 校验SQL
     */
    private String validSql = TEST_SQL;

    /**
     * failedMap 失败的DataSource结果
     */
    private final Map<String, SqlDataSourceWrap> failedMap = new ConcurrentHashMap<String, SqlDataSourceWrap>();

    /**
     * recoverHeartBeat 检测失败数据源的心跳线程
     */
    private DataSourceRecoverHeartBeat recoverHeartBeat;

    /**
     * accessValidDataSourceKeys 返回过的可用数据源的KEY
     */
    private Set<String> accessValidDataSourceKeys = Collections.synchronizedSet(new LinkedHashSet<String>());

    /**
     * lastSuccessGetDataSourcTime 上次成功返回数据源的时间戳
     */
    private long lastSuccessGetDataSourcTime;

    /**
     * construct with
     * 
     * @param dataSources 所有数据源
     */
    public DynamicSqlDataSource(Map<String, SqlDataSourceWrap> dataSources) {
        this.dataSources = Maps.newHashMap(dataSources);
        this.lastSuccessGetDataSourcTime = System.currentTimeMillis();
    }

    /**
     * construct with
     * 
     * @param validInterval 失败数据源校验间隔，默认1S
     * @param dataSources 所有数据源
     * @param validSql 校验SQL，除非又特殊指定，默认 select 1
     */
    public DynamicSqlDataSource(long validInterval, Map<String, SqlDataSourceWrap> dataSources, String validSql) {
        this(dataSources);
        this.validInterval = validInterval;
        this.validSql = validSql;
    }

    /**
     * 销毁数据源
     * 
     * @throws Exception 销毁过程出现的异常
     */
    public void destroy() throws Exception {
        shutdownHeartBean();
        if (MapUtils.isNotEmpty(dataSources)) {
            dataSources.forEach((key, ds) -> {
                if (ds.getDataSource() instanceof ComboPooledDataSource) {
                    ((ComboPooledDataSource) ds.getDataSource()).close();
                }
            });
        }
    }

    /**
     * 从已有数据源中根据数据源的KEY移除数据源
     * 
     * @param dataSourceKey 数据源的KEY
     * @return 被移出的数据源
     * @throws IllegalArgumentException 参数异常
     */
    public synchronized SqlDataSourceWrap removeDataSourceByKey(String dataSourceKey) {
        if (StringUtils.isBlank(dataSourceKey)) {
            throw new IllegalArgumentException("can not remove datasource by null key");
        }
        accessValidDataSourceKeys.remove(dataSourceKey);
        failedMap.remove(dataSourceKey);
        return dataSources.remove(dataSourceKey);
    }
    
    
    /** 
     * clearDataSourceFailCount
     */
    public void clearDataSourceFailCount() {
        if(MapUtils.isNotEmpty(failedMap)) {
            final long current = System.currentTimeMillis();
            failedMap.forEach((key, ds) -> {
                if(ds.getFailCount() >= 5 && (ds.getFailTime() - current) / (36 * 2 * 1e5) > 1){
                    ds.resetFailCount();
                }
            });
        }
    }

    /**
     * 新增数据源
     * 
     * @param key 数据源的KEY
     * @param dataSource 新增的数据源
     * @throws IllegalArgumentException 参数异常
     */
    public synchronized void addDataSource(String key, SqlDataSourceWrap dataSource) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("can not add datasource by null key");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("can not add datasource by null datasource");
        }
        dataSources.put(key, dataSource);
    }

    /**
     * 取得可用的DataSource
     * 
     * @return DataSource
     * @throw IllegalArgumentException 无法找到数据源
     */
    public synchronized SqlDataSourceWrap getDataSource() {
        long curr = System.currentTimeMillis();
        String key = getDataSourceKey();
        LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to getDataSourceKey");
        curr = System.currentTimeMillis();
        SqlDataSourceWrap result = dataSources.get(key);
        LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to dataSources.get(key)");
        curr = System.currentTimeMillis();
        Connection connection = null;
        try {
            connection = result.getConnection();
            LOGGER.info("validate datasource by key:" + key);
            LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to result.getConnection()");
            curr = System.currentTimeMillis();
            validateConnection(connection);
            LOGGER.info("return datasource by key:" + key);
            LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to validateConnection(connection)");
            curr = System.currentTimeMillis();
            // 先移除，在放进去才会在第一个
            accessValidDataSourceKeys.remove(key);
            LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to accessValidDataSourceKeys.remove(key);");
            curr = System.currentTimeMillis();
            accessValidDataSourceKeys.add(key);
            LOGGER.info("dynamicDataSource.getDataSource cost: " + (System.currentTimeMillis() - curr) + " ms to accessValidDataSourceKeys.add(key);");
            curr = System.currentTimeMillis();
            return result;
        } catch (Exception e) {
            LOGGER.warn("datasource key:" + key + " is invalide,try another one!", e);
            failedMap.put(key, result);
            accessValidDataSourceKeys.remove(key);
            checkFailedDataSourceHeatBeat();
            return getDataSource();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                    LOGGER.info("dynamicDataSource.getDataSource cost: "
                          + (System.currentTimeMillis() - curr)
                          + " ms to connection.close();");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

    /**
     * 获取DataSource的key，获取所有数据源的KEY 然后去除失败的和已使用过的，如果没有未使用的，则返回最早的已使用过的KEY
     * 
     * @return 可用的DataSourcekey
     * @throw IllegalArgumentException 数据源为空
     */
    private String getDataSourceKey() {
        if (MapUtils.isEmpty(dataSources)) {
            throw new IllegalArgumentException("can not get datasource by empty datasources!");
        }
        List<String> allKeys = Lists.newArrayList(dataSources.keySet());
        LOGGER.debug("get " + allKeys.size() + " size avaliable datasources");
        if (!failedMap.isEmpty()) {
            Set<String> failedKeys = failedMap.keySet();
            LOGGER.warn("found failed datasource keys :" + failedKeys);
            allKeys.removeAll(failedKeys);
            accessValidDataSourceKeys.removeAll(failedKeys);
        }
        if (allKeys.size() > 0) {
            allKeys.removeAll(accessValidDataSourceKeys);
            if (allKeys.size() > 0) {
                LOGGER.debug("after filter access valid datasource," + allKeys);
                return allKeys.get(0);
            } else {
                LOGGER.debug("return first time access datasource key:" + accessValidDataSourceKeys);
                return (String) accessValidDataSourceKeys.toArray()[0];
            }
        }
        throw new IllegalArgumentException("can not get datasource key because all datasource is failed!");
    }

    /**
     * 开始心跳检测连接失败的DataSource
     */
    private synchronized void checkFailedDataSourceHeatBeat() {
        if (recoverHeartBeat == null) {
            recoverHeartBeat = new DataSourceRecoverHeartBeat(this);
            if (executor == null) {
                executor = Executors.newFixedThreadPool(1);
            }
            executor.execute(recoverHeartBeat);
        } else {
            if (!recoverHeartBeat.isRuning()) {
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(1);
                }
                executor.execute(recoverHeartBeat);
            }
        }

    }

    /**
     * 校验connection
     * 
     * @param con 待校验的connection
     * @throws SQLException connection查询异常
     */
    private void validateConnection(Connection con) throws SQLException {
        long current = System.currentTimeMillis();
        PreparedStatement stmt = con.prepareStatement(validSql); // test
                                                                 // connection
                                                                 // is ok
        stmt.executeQuery();
        stmt.close();
        LOGGER.info("validate connection cost:" + (System.currentTimeMillis() - current));
    }

    /**
     * 关闭心跳
     */
    private synchronized void shutdownHeartBean() {
        if (recoverHeartBeat != null) {
            recoverHeartBeat.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Connection status recover heart beat thread.
     * 
     * @author xiemalin
     *
     */
    private static class DataSourceRecoverHeartBeat implements Runnable {
        /**
         * Logger for this class
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRecoverHeartBeat.class);

        /**
         * dynamicDataSource
         */
        private DynamicSqlDataSource dynamicDataSource;
        /**
         * runing 正在执行
         */
        private boolean runing;

        /**
         * close 已经关闭心跳
         */
        private boolean close = false;

        /**
         * 判断是否正在执行
         * 
         * @return 线程池执行状态
         */
        public boolean isRuning() {
            return runing;
        }

        /**
         * 关闭心跳线程池
         */
        public void close() {
            close = true;
        }

        /**
         * construct with
         * 
         * @param dynamicDataSource 动态数据源
         */
        public DataSourceRecoverHeartBeat(DynamicSqlDataSource dynamicDataSource) {
            this.dynamicDataSource = dynamicDataSource;
        }

        public void run() {
            runing = true;
            while (!dynamicDataSource.failedMap.isEmpty() && !close) {

                // copy data
                Map<String, SqlDataSourceWrap> dataSourceMapCopy;
                dataSourceMapCopy = new HashMap<String, SqlDataSourceWrap>(dynamicDataSource.failedMap);

                Iterator<Entry<String, SqlDataSourceWrap>> iter;
                iter = dataSourceMapCopy.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, SqlDataSourceWrap> next = iter.next();
                    SqlDataSourceWrap ds = next.getValue();
                    String key = next.getKey();
                    if(ds.getFailCount() >= 10) {
                        dynamicDataSource.failedMap.remove(key);
                        continue;
                    }
                    Connection con = null;
                    try {
                        con = ds.getConnection();
                        dynamicDataSource.validateConnection(con);
                        LOGGER.debug("Datasource key='" + key + "' valid ok.");
                        dynamicDataSource.failedMap.remove(key);
                        ds.resetFailCount();
                    } catch (SQLException e) {
                        LOGGER.warn("Datasource key='" + key + "' valid failed.");
                        ds.increaseFailCount();
                    } finally {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (SQLException e) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug(e.getMessage(), e);
                                }
                            }
                        }
                    }

                }

                try {
                    Thread.sleep(dynamicDataSource.validInterval);
                } catch (Exception e) {
                    // here we needn't care exception
                }
            }
            runing = false;
        }
    }

    /**
     * getter method for property validInterval
     * 
     * @return the validInterval
     */
    public long getValidInterval() {
        return validInterval;
    }

    /**
     * setter method for property validInterval
     * 
     * @param validInterval the validInterval to set
     */
    public void setValidInterval(long validInterval) {
        this.validInterval = validInterval;
    }

    /**
     * getter method for property validSql
     * 
     * @return the validSql
     */
    public String getValidSql() {
        return validSql;
    }

    /**
     * setter method for property validSql
     * 
     * @param validSql the validSql to set
     */
    public void setValidSql(String validSql) {
        this.validSql = validSql;
    }

    /**
     * get lastSuccessGetDataSourcTime
     * 
     * @return the lastSuccessGetDataSourcTime
     */
    public long getLastSuccessGetDataSourcTime() {
        return lastSuccessGetDataSourcTime;
    }

    /**
     * set lastSuccessGetDataSourcTime with lastSuccessGetDataSourcTime
     * 
     * @param lastSuccessGetDataSourcTime the lastSuccessGetDataSourcTime to set
     */
    public void setLastSuccessGetDataSourcTime(long lastSuccessGetDataSourcTime) {
        this.lastSuccessGetDataSourcTime = lastSuccessGetDataSourcTime;
    }

    /** 
     * 获取 dataSources 
     * @return the dataSources 
     */
    public Map<String, SqlDataSourceWrap> getDataSources() {
    
        return dataSources;
    }
}
