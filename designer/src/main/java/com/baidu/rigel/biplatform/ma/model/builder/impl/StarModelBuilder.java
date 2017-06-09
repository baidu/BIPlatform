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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeLevel;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.DimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ReferenceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * 星型模型构建器，用来构建星型模型
 * 
 * @author david.wang
 *
 */
class StarModelBuilder {
    
    /**
     * 日志记录器
     */
    private Logger logger = LoggerFactory.getLogger(StarModelBuilder.class);
    
    /**
     * 构建星型模型
     * 
     * @param cube
     *            立方体定义
     * @return star mode 定义
     */
    public StarModel buildModel(MiniCube cube) {
        StarModel model = new StarModel();
        model.setCubeId(cube.getId());
        logger.info("current cube is : [{}]", cube);
        logger.info("current cube's schema is : [{}]", cube.getSchema());
        // 确认schema不为空
        if (cube.getSchema() != null) {
            model.setDsId(cube.getSchema().getDatasource());
            model.setSchemaId(cube.getSchema().getId());
        }
        model.setFactTable(buildFactTable(cube));
        model.setDimTables(buildDimTables(cube));
        return model;
    }
    
    /**
     * 构建维度表
     * 
     * @param cube
     *            立方体
     * @return 维度表定义
     */
    private List<DimTableMetaDefine> buildDimTables(MiniCube cube) {
        Collection<Dimension> dimensions = cube.getDimensions().values();
        if (dimensions == null || dimensions.size() == 0) {
            return new ArrayList<DimTableMetaDefine>();
        }
        Map<String, DimTableMetaDefine> dimTableCache = new HashMap<String, DimTableMetaDefine>();
        List<DimTableMetaDefine> rs = new ArrayList<DimTableMetaDefine>();
        String factTableName = cube.getSource();
        for (Dimension dimension : dimensions) {
            if (dimension.getLevels().size() > 1) { // 组合维度 不处理
                continue;
            }
            String dimTableName = dimension.getTableName();
            if (dimTableName == null || dimTableName.equals(factTableName)) {
                continue;
            }
            // 同一个维度表中的列，只需要加入维度表中就可以了
            if (dimTableCache.containsKey(dimension.getTableName())) {
                DimTableMetaDefine dimTable = dimTableCache.get(dimension.getTableName());
                ColumnMetaDefine column = buildColumn(this.getLevel(dimension));
                dimTable.addColumn(column);
            } else { // 构建新的维度表，并加入当前列
                DimTableMetaDefine dimTable = getDimTableDefineWithLevelType(cube.getSource(),
                    dimension);
                ColumnMetaDefine column = buildColumn(this.getLevel(dimension));
                dimTable.addColumn(column);
                dimTableCache.put(dimension.getTableName(), dimTable);
            }
        }
        rs.addAll(dimTableCache.values());
        return rs;
    }
    
    /**
     * 构建维度表定义
     * 
     * @param dimension
     *            维度
     * @return 维度表定义
     */
    private DimTableMetaDefine getDimTableDefineWithLevelType(String factTable, Dimension dimension) {
        Level level = getLevel(dimension);
        if (level == null) {
            throw new RuntimeException("dimension's level is empty : " + dimension);
        }
        switch (level.getType()) {
            case REGULAR: {
                StandardDimTableMetaDefine dimTable = new StandardDimTableMetaDefine();
                dimTable.setName(dimension.getTableName());
                ReferenceDefine reference = buildReference(factTable, dimension, level);
                dimTable.setReference(reference);
                dimTable.addColumn(buildColumn(level));
                return dimTable;
            }
            case CALL_BACK: {
                CallbackDimTableMetaDefine dimTable = buildCallDimTable(factTable, dimension, level);
                return dimTable;
            }
            case PARENT_CHILD:
                // TODO 目前没有处理
                return null;
            case USER_CUSTOM: {
            	// TODO 目前没有处理
//                UserDefineDimTableMetaDefine dimTable = buildUserDefineTable(factTable, dimension,
//                    level);
//                return dimTable;
            	return null;
            }
            default:
                break;
        }
        
        return buildTimeDimTable(factTable, dimension, level);
    }
    
    /**
     * 构建时间维度表
     * 
     * @param factTable
     *            事实表
     * @param dimension
     *            维度定义
     * @param level
     *            级别定义
     * @return 时间维度表定义
     */
    private DimTableMetaDefine buildTimeDimTable(String factTable, Dimension dimension, Level level) {
        TimeDimTableMetaDefine dimTable = new TimeDimTableMetaDefine(TimeDimType.STANDARD_TIME);
        dimTable.setName(dimension.getTableName());
        ReferenceDefine reference = buildReference(factTable, dimension, level);
        dimTable.setReference(reference);
        dimTable.setFormat(((TimeDimension) dimension).getTimeFormat());
        dimTable.addColumn(this.buildColumn(level));
        return dimTable;
    }
    
    /**
     * 构建引用参照
     * 
     * @param factTable
     *            事实表名称
     * @param dimension
     *            维度定义
     * @param level
     *            级别
     * @return
     */
    private ReferenceDefine buildReference(String factTable, Dimension dimension, Level level) {
        ReferenceDefine reference = new ReferenceDefine();
        reference.setMajorColumn(dimension.getFacttableColumn());
        reference.setSalveColumn(level.getPrimaryKey());
        reference.setMajorTable(factTable); // 事实表名称
        return reference;
    }
    
    /**
     * 构建用户自定义维度表
     * 
     * @param factTable
     *            事实表名称
     * @param dimension
     *            维度定义
     * @param level
     *            级别定义
     * @return 用户自定义表定义
     */
//    private UserDefineDimTableMetaDefine buildUserDefineTable(String factTable,
//        Dimension dimension, Level level) {
//        UserDefineDimTableMetaDefine dimTable = new UserDefineDimTableMetaDefine();
//        dimTable.setName(dimension.getTableName());
//        ReferenceDefine reference = buildReference(factTable, dimension, level);
//        dimTable.setReference(reference);
//        UserCustomLevel tmp = (UserCustomLevel) level;
//        
//        dimTable.setParams(tmp.getCustomParams());
//        dimTable.setSourceType(DimSourceType.SQL);
//        dimTable.setValue(tmp.getValue());
//        
//        dimTable.addColumn(this.buildColumn(level));
//        return dimTable;
//    }
    
    /**
     * 构建回调维度表定义
     * 
     * @param factTable
     *            事实表名称
     * @param dimension
     *            维度定义
     * @param level
     *            级别定义
     * @return
     */
    private CallbackDimTableMetaDefine buildCallDimTable(String factTable, Dimension dimension,
        Level level) {
        CallbackDimTableMetaDefine dimTable = new CallbackDimTableMetaDefine();
        dimTable.setName(dimension.getTableName());
        ReferenceDefine reference = buildReference(factTable, dimension, level);
        dimTable.setReference(reference);
        CallbackLevel tmp = (CallbackLevel) level;
        Map<String, String> params = Maps.newHashMap();
        params.putAll(tmp.getCallbackParams());
        dimTable.setParams(params);
        dimTable.setUrl(tmp.getCallbackUrl());
        int interval = tmp.getRefreshInterval();
        int refreshType = 0;
        if (interval == 0) {
            refreshType = 1;
        } else if (interval == -1) {
            refreshType = 2;
        } else {
            refreshType = 3;
        }
        // 设置刷新时间间隔
        dimTable.addConfigItem(CallbackDimTableMetaDefine.REF_INTERNAL_KEY,
                String.valueOf(interval));
        
        // 设置刷新时间类型
        dimTable.addConfigItem(CallbackDimTableMetaDefine.REFRESH_KEY, 
                String.valueOf(refreshType));
        // TODO 添加刷新类型
        dimTable.addColumn(this.buildColumn(level));
        return dimTable;
    }
    
    /**
     * 
     * 获取维度中的级别定义
     * 
     * @param dimension
     *            维度定义
     * @return 级别定义
     */
    private Level getLevel(Dimension dimension) {
        Level level = null;
//        for (Map.Entry<String, Level> entry : dimension.getLevels().entrySet()) {
//            level = entry.getValue();
//        }
        if (dimension.getLevels() == null || dimension.getLevels().size() == 0) {
            return null;
        }
        level = (Level) dimension.getLevels().values().toArray()[0];
        return level;
    }
    
    /**
     * 构建列的元定义
     * 
     * 
     * @param level
     * @return 列的元定义
     */
    private ColumnMetaDefine buildColumn(Level level) {
        ColumnMetaDefine column = new ColumnMetaDefine();
        if (level != null) {
            column.setCaption(level.getCaption());
            // modify by jiangyichao at 2014-10-15
            String source = null;
            if (level instanceof CallbackLevel) {
                source = ((CallbackLevel)level).getFactTableColumn();
            } else {
                source = ((MiniCubeLevel)level).getSource();
            }
            column.setName(source);
            // TODO 如果是时间维度，需要特殊处理
            // column.setType(level.getType());
        }
        return column;
    }
    
    /**
     * 构建事实表
     * 
     * @param cube
     *            cube定义
     * @return 事实表定义
     */
    private FactTableMetaDefine buildFactTable(MiniCube cube) {
        FactTableMetaDefine factTable = new FactTableMetaDefine();
        factTable.setCubeId(cube.getId());
        factTable.setMutilple(cube.isMutilple());
        if (factTable.isMutilple()) {
            // 分库分表导致多张表组成事实表
            factTable.setRegExp(cube.getSource());
            if (cube.getSource() != null) {
                List<String> regexTables = Lists.newArrayList();
                Collections.addAll(regexTables, cube.getSource().split(","));
                factTable.setRegExpTables(regexTables);
                if (regexTables != null && regexTables.size() > 0 ) {
                    factTable.setName(regexTables.get(0));                                    
                }
            }
            factTable.setDivideTableStrategyVo(cube.getDivideTableStrategyVo());
        } else {
            // 事实表名称
            factTable.setName(cube.getSource());
        }
        if (cube.getMeasures() == null || cube.getMeasures().isEmpty()) {
            logger.debug("measure is empty");
        } else {
            Collection<Measure> measures = cube.getMeasures().values();
            if (measures == null) {
                logger.debug("can't get measuers");
                return factTable;
            }
            measures.forEach(m -> {
                ColumnMetaDefine column = new ColumnMetaDefine();
                column.setCaption(m.getCaption());
                column.setName(m.getDefine());
                factTable.addColumn(column);
            });
            // 添加维度引用的列
            cube.getDimensions().values().forEach(dim -> {
                ColumnMetaDefine column = new ColumnMetaDefine();
                // modify by jiangyichao at 2014-10-09 修改维度caption
                column.setCaption(dim.getFacttableCaption());
                column.setName(dim.getFacttableColumn());
                factTable.addColumn(column);
            });
        }
        return factTable;
    }
}
