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
package com.baidu.rigel.biplatform.ma.ds.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ma.ds.service.impl.RelationDBInfoReaderServiceImpl;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.google.common.collect.Maps;

/**
 * Description:
 * 
 * @author david.wang
 *
 */
public final class DataSourceMetaServiceHelper {

    /**
     * 元数据服务实例仓库
     */
    private static final Map<String, DataSourceInfoReaderService> SERVICE_REPOSITORY = Maps
            .newHashMap();

    static {
        /**
         * 提供默认数据源类型支持
         */
        try {
            final DataSourceInfoReaderService dsMetaService = new RelationDBInfoReaderServiceImpl();
            SERVICE_REPOSITORY.put(DataSourceType.MYSQL.name(), dsMetaService);
            SERVICE_REPOSITORY.put(DataSourceType.PALO.name(), dsMetaService);
            SERVICE_REPOSITORY.put(DataSourceType.DRUID.name(), dsMetaService);
            SERVICE_REPOSITORY.put(DataSourceType.H2.name(), dsMetaService);
            SERVICE_REPOSITORY.put(DataSourceType.ORACLE.name(), dsMetaService);
            SERVICE_REPOSITORY.put(DataSourceType.MYSQL_DBPROXY.name(),
                    dsMetaService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param dsType
     * @param metaServiceClazz
     */
    public static void registryDsMetaService(String dsType,
            Class<?> metaServiceClazz) {
        try {
            SERVICE_REPOSITORY.put(dsType,
                    (DataSourceInfoReaderService) metaServiceClazz
                            .newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param dsType
     * @return DataSourceInfoReaderService
     */
    public static DataSourceInfoReaderService getDsMetaService(String dsType) {
        return SERVICE_REPOSITORY.get(dsType);
    }

    /**
     * 依据配置注册元数据服务
     */
    public static void registryDsMetaServices(String type, Class<?> clazz) {
        try {
            SERVICE_REPOSITORY.put(type,
                    (DataSourceInfoReaderService) clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
