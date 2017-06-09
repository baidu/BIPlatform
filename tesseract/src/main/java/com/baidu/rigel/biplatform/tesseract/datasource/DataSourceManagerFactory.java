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

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.datasource.impl.SqlDataSourceManagerImpl;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;

/**
 * DataSourceManager的实例化工厂
 * 
 * @author xiaoming.chen
 *
 */
public class DataSourceManagerFactory {
    /**
     * 根据DataSourceInfo获取datasourceManager的实例
     * 
     * @param dataSourceInfo 数据源信息
     * @return dataSourceManager实例
     * @throws DataSourceException 无法找到实例
     * @throws IllegalArgumentException 数据源信息为空或者不合法
     */
    public static DataSourceManager getDataSourceManagerInstance(DataSourceInfo dataSourceInfo)
            throws DataSourceException {
        if (dataSourceInfo == null || !dataSourceInfo.validate()) {
            throw new IllegalArgumentException(
                    "can not get DataSourceManager instance by invalidate datasourceInfo:"
                        + dataSourceInfo);
        }
        switch (dataSourceInfo.getDataSourceType().getId()) {
            case MiniCubeConnection.DATASOURCETYPE_SQL:
                return SqlDataSourceManagerImpl.getInstance();
            default:
                throw new DataSourceException("unknow datasource type:"
                        + dataSourceInfo.getDataSourceType());
        }
        
    }
}
