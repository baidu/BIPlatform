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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.DimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;

/**
 * 
 * 
 * @author david.wang
 *
 */
class CubeBuilder {
    
    /**
     * 日志记录器
     */
    private Logger logger = LoggerFactory.getLogger(CubeBuilder.class);
    
    /**
     * 构建立方体模型
     * 
     * @return cube 立方体对象
     */
    public Cube buildCube(StarModel model) {
        String id = UuidGeneratorUtils.generate();
        logger.info("create cube with id : " + id);
        if (model.getFactTable() == null) {
            return null;
        }
        String factTableName = model.getFactTable().getName();
        MiniCube cube = new MiniCube("cube_" + factTableName);
        cube.setCaption(factTableName);
        cube.setEnableCache(true);
        cube.setId(id);
        cube.setVisible(true);
        cube.setMutilple(model.getFactTable().isMutilple());
        cube.setDivideTableStrategyVo(model.getFactTable().getDivideTableStrategyVo());
        cube.setSource(factTableName);
        // cube.setSource(model.getDsId());
        // modify by jiangyichao at 2014-10-09  构建维度时，增加事实表参数
        cube.setDimensions(buildDimensions(cube, model.getDimTables(), model.getFactTable()));
        cube.setMeasures(buildMeasures(cube, model.getDimTables(), model.getFactTable()));
        logger.info("create cube operation successfully : " + cube);
        return cube;
    }
    
    /**
     * 
     * 构建度量集合信息（不包含自定义度量、计算成员）： 从事实表中的列构建默认度量信息
     * 
     * @param cube
     *            立方体
     * @param dimTables 
     * @param factTable
     *            事实表中的列的定义
     * @return 度量的集合信息 如果不存在，返回空集合
     * 
     */
    private Map<String, Measure> buildMeasures(MiniCube cube, List<DimTableMetaDefine> dimTables, 
            FactTableMetaDefine factTable) {
        logger.info("begin create measures");
        ColumnMetaDefine[] columns = factTable.getColumns();
        Map<String, Measure> rs = new HashMap<String, Measure>();
        if (columns == null || columns.length == 0) {
            logger.warn("measure columns is empty ");
            return rs;
        }
        final Set<String> refColumnNames = new HashSet<String>();
        dimTables.stream().filter(dimTable -> {
            return dimTable.getReference() != null;
        }).forEach(dimTable -> {
            String refColName = dimTable.getReference().getMajorColumn();
            refColumnNames.add(refColName);
        });
        MeasureBuilder measureBuilder = new MeasureBuilder();
        for (ColumnMetaDefine column : columns) {
            if (column == null) {
                logger.warn("column is empty");
                continue;
            }
            if (StringUtils.isBlank(column.getName())) {
                throw new IllegalStateException("column's name can not be null");
            }
            if (refColumnNames.contains(column.getName())) {
                continue;
            }
            Measure measure = measureBuilder.buildMeasure(column);
            if (measure != null) {
                ((MiniCubeMeasure) measure).setCube(cube);
                rs.put(measure.getId(), measure);
            }
        }
        logger.info("create measure successfully : " + rs);
        return rs;
    }
    
    /**
     * 构建维度信息 不包括level信息的维度（简单维度）
     * 
     * @param cube
     *            立方体
     * @param dimTables
     *            星系模型
     * @param factTable 
     *            事实表
     * @return 维度信息 如果不存在 返回空集合
     */
    private Map<String, Dimension> buildDimensions(MiniCube cube, List<DimTableMetaDefine> dimTables, 
            FactTableMetaDefine factTable) {
        if (dimTables == null || dimTables.isEmpty()) {
            return new HashMap<String, Dimension>();
        }
        Map<String, Dimension> rs = new HashMap<String, Dimension>();
        DimensionBuilder builder = new DimensionBuilder();
        for (DimTableMetaDefine dimTable : dimTables) {
            Dimension[] dimensions = builder.buildDimensions(dimTable, factTable);
            if (dimensions == null) { // result is null
                continue;
            }
            for (Dimension dimension : dimensions) {
                rs.put(dimension.getId(), dimension);
            }
        }
        return rs;
    }
}
