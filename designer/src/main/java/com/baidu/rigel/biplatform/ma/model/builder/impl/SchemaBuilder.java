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
package com.baidu.rigel.biplatform.ma.model.builder.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;

/**
 * schema构建器
 * 
 * @author david.wang
 *
 */
class SchemaBuilder {
    
    /**
     * 日志记录管理工具
     */
    private Logger logger = LoggerFactory.getLogger(SchemaBuilder.class);
    
    /**
     * 构建schema对象
     * 
     * @param dsId
     *            数据源id
     * @return 转换成功的schema对象
     */
    public Schema buildSchema(String dsId) {
        logger.info("begin create schema process ");
        String id = UuidGeneratorUtils.generate();
        logger.info("create schema with id " + id);
        MiniCubeSchema schema = new MiniCubeSchema("schema_" + id);
        schema.setId(id);
        schema.setVisible(true);
        schema.setCaption("schema_" + id);
        schema.setDatasource(dsId);
        // schema.setUniqueName(id);
        logger.info("create schema successfully : " + schema);
        return schema;
    }
    
}
