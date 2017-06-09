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

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;

/**
 * 
 * 度量模型构造器
 * 
 * @author david.wang
 *
 */
class MeasureBuilder {
    
    /**
     * 日志记录工具
     */
    private Logger logger = LoggerFactory.getLogger(MeasureBuilder.class);
    
    /**
     * 构建度量模型
     * 
     * @param column
     *            事实表指标列定义
     * @return 指标定义描述信息
     */
    public Measure buildMeasure(ColumnMetaDefine column) {
        String id = UuidGeneratorUtils.generate();
        logger.info("create measure with id : " + id);
        MiniCubeMeasure measure = new MiniCubeMeasure(column.getName());
        measure.setAggregator(Aggregator.SUM);
        measure.setId(id);
        measure.setCaption(column.getCaption());
        measure.setType(MeasureType.COMMON);
        measure.setVisible(true);
        // 事实表所在列列名
        measure.setDefine(column.getName());
        // measure.setUniqueName("Measures." + column.getName());
        logger.info("create measure successfully : id : {}", measure.getId());
        return measure;
    }
    
}
