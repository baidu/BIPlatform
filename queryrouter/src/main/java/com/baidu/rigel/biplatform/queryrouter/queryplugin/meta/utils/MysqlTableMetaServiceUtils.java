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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.meta.utils;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.meta.impl.TableMetaServiceImpl;


/**
 * 类DruidTableMetaUtils.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年11月16日 下午8:10:58
 */
public class MysqlTableMetaServiceUtils {
    
    public static TableMetaServiceImpl setTableMetaServiceInfo(TableMetaServiceImpl tableMetaService) {
        tableMetaService.setSql("select COLUMN_NAME,TABLE_NAME,DATA_TYPE from information_schema.`columns` "
                + " where TABLE_NAME in ([TABLENAME])"
                + " and COLUMN_NAME in ([COLUMNNAME])");
        tableMetaService.setTableName("TABLE_NAME");
        tableMetaService.setColumnName("COLUMN_NAME");
        tableMetaService.setDataType("DATA_TYPE");
        return tableMetaService;
    }
}