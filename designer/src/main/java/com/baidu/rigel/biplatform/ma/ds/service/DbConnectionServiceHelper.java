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

import com.baidu.rigel.biplatform.ma.ds.service.impl.RelationDBConnectionServiceImpl;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.google.common.collect.Maps;

/**
 * Description:
 * 
 * @author david.wang
 *
 */
public final class DbConnectionServiceHelper {

    /**
     * REPOSITORY
     */
    private static final Map<String, DataSourceConnectionService<?>> REPOSITORY = Maps
            .newHashMap();

    static {
        /**
         * 提供默认数据源类型支持
         */
        try {
            final RelationDBConnectionServiceImpl dsMetaService = new RelationDBConnectionServiceImpl();
            REPOSITORY.put(DataSourceType.MYSQL.name(), dsMetaService);
            REPOSITORY.put(DataSourceType.PALO.name(), dsMetaService);
            REPOSITORY.put(DataSourceType.DRUID.name(), dsMetaService);
            REPOSITORY.put(DataSourceType.H2.name(), dsMetaService);
            REPOSITORY.put(DataSourceType.ORACLE.name(), dsMetaService);
            REPOSITORY.put(DataSourceType.MYSQL_DBPROXY.name(), dsMetaService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private DbConnectionServiceHelper() {
    }

    /**
     * 
     * @param dsType
     * @return DataSourceInfoReaderService
     */
    public static DataSourceConnectionService<?> getDsMetaService(String dsType) {
        return REPOSITORY.get(dsType);
    }

    /**
     * 依据配置注册元数据服务
     */
    @SuppressWarnings("rawtypes")
    public static void registryDsMetaServices(String type, Class clazz) {
        try {
            REPOSITORY.put(type,
                    (DataSourceConnectionService<?>) clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
