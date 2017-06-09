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
package com.baidu.rigel.biplatform.ac.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.google.common.collect.Maps;

/**
 * 接入接口
 * 
 * @author xiaoming.chen
 *
 */
public class MiniCubeDriverManager {

    /**
     * TODO 需要调整设计为桥接模式
     * 数据源连接对象管理仓库
     */
    private static final Map<String, Class<? extends MiniCubeConnection>> REPOSITORY = Maps.newConcurrentMap ();
    
    // 默认注入mysql connection
    static {
        REPOSITORY.put (DataSourceType.SQL.toString (), MiniCubeSqlConnection.class);
    }
    
    /**
     * 根据cube对象创建连接
     * 
     * @param cube
     * @param dataSourceInfo
     * @return
     */
    public static MiniCubeConnection getConnection(DataSourceInfo dataSourceInfo) {
        if (dataSourceInfo == null || !dataSourceInfo.validate()) {
            throw new IllegalArgumentException("dataSourceInfo:" + dataSourceInfo);
        }
        if (dataSourceInfo.getDataSourceType().equals(DataSourceType.SQL)) {
            return new MiniCubeSqlConnection((SqlDataSourceInfo) dataSourceInfo);
        }
        Class<? extends MiniCubeConnection> clazz = REPOSITORY.get (dataSourceInfo.getDataSourceType ().toString ());
        if (clazz == null) {
            throw new UnsupportedOperationException("only support SQL type dataSourceinfo.");
        }
        try {
            Constructor<? extends MiniCubeConnection> constructor = clazz.getConstructor (DataSourceInfo.class);
            return constructor.newInstance (dataSourceInfo);
        } catch (NoSuchMethodException 
                | SecurityException 
                | InstantiationException | IllegalAccessException 
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException ("initialized minicubeconnection failed !");
        }
    }

    public static void registryConn (String type, Class<? extends MiniCubeConnection> clazz) {
        REPOSITORY.put (type, clazz);
    }
    // /**
    // * 动态转换Schema文件，为后续预留
    // * @param schemaFile
    // * @param dataSourceInfo
    // * @return
    // */
    // public MiniCubeConnection getConnection(File schemaFile, DataSourceInfo dataSourceInfo){
    // throw new UnsupportedOperationException("not implement.");
    // }

}
