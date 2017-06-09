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
package com.baidu.rigel.biplatform.ma.model.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.baidu.rigel.biplatform.ma.model.exception.DBInfoReadException;

/**
 * DB的url工具类
 * 
 * @author zhongyi
 *
 */
public final class DBUrlGeneratorUtils {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(DBUrlGeneratorUtils.class);
    
    /**
     * DBUrlGeneratorUtils
     */
    private DBUrlGeneratorUtils() {
        
    }
    
    /**
     * 获取某个ds的url
     * 
     * @param ds
     *            ds对象
     * @return url
     * @throws Exception 
     */
    public static String getConnUrl(DataSourceDefine ds) {
        if (ds == null) {
            throw new DBInfoReadException("Datasource can not be null! ");
        }
        DataSourceType type = ds.getDataSourceType();
        String connUrl = type.getPrefix() + ds.getHostAndPort() + type.getDiv() + ds.getDbInstance();
        if (StringUtils.hasText(ds.getEncoding())) {
            if (type == DataSourceType.MYSQL || type == DataSourceType.MYSQL_DBPROXY) {
                connUrl = connUrl + "?useUniCode=true&characterEncoding=" + ds.getEncoding();
            }
        }
        LOG.debug("Conn URL: " + connUrl);
        return connUrl;
    }
}
