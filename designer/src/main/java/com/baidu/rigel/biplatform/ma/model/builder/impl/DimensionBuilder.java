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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeLevel;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.minicube.UserCustomLevel;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.DimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ReferenceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimType;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimType;
import com.baidu.rigel.biplatform.ma.model.meta.UserDefineDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.utils.TimeTypeAdaptorUtils;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;

/**
 * 
 * 维度构建器，构建维度模型
 * 
 * @author david.wang
 *
 */
class DimensionBuilder {
    
    /**
     * 日志记录器
     */
    private Logger logger = LoggerFactory.getLogger(DimensionBuilder.class);
    
    /**
     * 构建Dimension 不包含多个level级别
     * 
     * @param dimTable
     *            维度表定义
     * @param factTable
     *            事实表
     * @return 如果构建成功，返回Dimension实例，否则返回null
     */
    public Dimension[] buildDimensions(DimTableMetaDefine dimTable, FactTableMetaDefine factTable) {
        if (dimTable == null) {
            logger.warn("dim table is null");
            return new Dimension[0];
        }
        String id = UuidGeneratorUtils.generate();
        logger.info("begin create dimension with id : " + id);
        Dimension[] rs = null;
        // StandardDimType
        if (dimTable.getDimType() instanceof StandardDimType) {
            rs = buildCommDimensions(dimTable, factTable);
        } else if (dimTable.getDimType() instanceof TimeDimType) { // time
            rs = buildTimeDimensions(dimTable);
        }
        return rs;
    }
    
    /**
     * 
     * @param dimTable
     * @return 维度定义
     */
    private Dimension[] buildTimeDimensions(DimTableMetaDefine dimTable) {
        List<TimeDimension> rs = new ArrayList<TimeDimension>();
        ReferenceDefine ref = dimTable.getReference();
        checkReference(ref);
        for (ColumnMetaDefine column : dimTable.getColumns()) {
            if (column == null) {
                continue;
            }
            String caption = StringUtils.isEmpty(column.getCaption()) ? column.getName() : column
                    .getCaption();
            // modify by jiangyichao at 2014-11-06
            String dataTimeType = column.getName();
            String name = generateDimName(dimTable, column);
            TimeDimension dimension = new TimeDimension(name);
            dimension.setCaption(caption);
            dimension.setName(name);
            dimension.setId(UuidGeneratorUtils.generate());
            dimension.setPrimaryKey(dimTable.getReference().getSalveColumn());
            dimension.setFacttableColumn(dimTable.getReference().getMajorColumn());
            dimension.setTableName(dimTable.getName());
            dimension.setVisible(true);
            dimension.setType(DimensionType.TIME_DIMENSION);
            /**
             * TODO 自定义时间维度可能要修改这里
             */
//            dimension.setDataTimeType(TimeTypeAdaptorUtils.parseToTimeType(dimTable.getReference()
//                    .getSalveColumn()));
            dimension.setDataTimeType(TimeTypeAdaptorUtils.parseToTimeType(dataTimeType));
            dimension.setTimeFormat(((TimeDimTableMetaDefine) dimTable).getFormat());
            LevelType type = TimeTypeAdaptorUtils.parseToLevelType(TimeType.valueOf(column.getName()));
            List<Level> levels = buildLevels(dimTable, column, type, dimension);
            if (levels != null) {
                for (Level level : levels) {
                    if (level == null) {
                        continue;
                    }
                    // TODO 需要根据当前维度的粒度，获取level的值
                    
                    ((MiniCubeLevel) level).setType(type);
                    dimension.addLevel(level);
                }
            }
            if (dimension.getDataTimeType() != TimeType.TimeYear) {
                rs.add(dimension);
            }
        }
        if (rs.size() == 0) {
            return new TimeDimension[0];
        }
        return rs.toArray(new TimeDimension[0]);
    }
    
    /**
     * 生成维度名称
     * 
     * @param dimTable
     * @param column
     * @return
     */
    private String generateDimName(DimTableMetaDefine dimTable, ColumnMetaDefine column) {
        return dimTable.getName() + "_" + column.getName();
    }
    
    /**
     * 构建维度的level信息
     * 
     * @param dimTable
     *            事实表
     * @param column
     *            该层级对应的列信息
     * @param type
     *            层级类别
     * @return 如果构建失败，返回空信息
     */
    private List<Level> buildLevels(DimTableMetaDefine dimTable, ColumnMetaDefine column,
            LevelType type, Dimension dim) {
        List<Level> levels = new ArrayList<Level>();
        if (column == null) {
            return levels;
        }
        String id = UuidGeneratorUtils.generate();
        logger.info("create level with id : " + id);
        MiniCubeLevel level = new MiniCubeLevel(column.getName());
        level.setCaption(column.getCaption() == null ? column.getName() : column.getCaption());
        level.setId(id);
        level.setDimension(dim);
        level.setDimTable(dimTable.getName());
        level.setName(column.getName());
        level.setPrimaryKey(dimTable.getReference().getSalveColumn());
        level.setSource(column.getName());
        level.setVisible(true);
        level.setType(type);
        level.setFactTableColumn(dimTable.getReference().getMajorColumn());
        levels.add(level);
        logger.info("create level successfully");
        return Collections.unmodifiableList(levels);
    }
    
    /**
     * 
     * @param dimTable 维度表
     * @param factTable 事实表
     * @return 维度定义
     * 
     */
    private Dimension[] buildCommDimensions(DimTableMetaDefine dimTable, FactTableMetaDefine factTable) {
        
        StandardDimType dimType = dimTable.getDimType();
        switch (dimType) {
            case STANDARD: // 标准维度
                return buildStandardDims((StandardDimTableMetaDefine) dimTable, factTable);
            case CALLBACK: // 回调维度
            {
                CallbackDimTableMetaDefine table = (CallbackDimTableMetaDefine) dimTable;
                return buildCallbackDims(table, factTable);
            }
            case USERDEFINE: // 用户自定义维度
            {
                UserDefineDimTableMetaDefine table = (UserDefineDimTableMetaDefine) dimTable;
                return buildUserDefineDims(table);
            }
            case REGRESS: // 退化维度
            {
                return buildStandardDims((StandardDimTableMetaDefine) dimTable, factTable);
            }
            default:
                break;
        
        }
        return new Dimension[0];
        
    }
    
    /**
     * 构建用户自定义维度
     * 
     * @param table
     *            维度表
     * @return 维度定义
     */
    private Dimension[] buildUserDefineDims(UserDefineDimTableMetaDefine table) {
        List<Dimension> dimensions = new ArrayList<Dimension>();
        if (table.getColumns() == null || table.getColumns().length == 0) {
            return new Dimension[0];
        }
        ReferenceDefine ref = table.getReference();
        checkReference(ref);
        ColumnMetaDefine column = table.getColumns()[0];
        String id = UuidGeneratorUtils.generate();
        logger.info("create dimension with id : " + id);
        String caption = StringUtils.isEmpty(column.getCaption()) ? column.getName() : column
                .getCaption();
        String name = this.generateDimName(table, column);
        StandardDimension dimension = initDimension(table, id, caption, name);
        dimension.setPrimaryKey(table.getReference().getSalveColumn());
        dimension.setFacttableColumn(table.getReference().getMajorColumn());
        UserCustomLevel level = new UserCustomLevel(column.getName());
        level.setCaption(column.getCaption());
        level.setType(LevelType.USER_CUSTOM);
        /**
         * TODO 需要支持其他类型
         */
        level.setUserCustomType(DataSourceType.SQL);
        level.setVisible(true);
        level.setCustomParams(table.getParams());
        level.setName(column.getName());
        level.setDimension(dimension);
        level.setValue(table.getValue()); // 维度值信息，当前为sql
        
        // Map<String, Level> levels = new HashMap<String, Level>();
        // levels.put(level.getId(), level);
        dimension.addLevel(level);
        dimensions.add(dimension);
        return dimensions.toArray(new Dimension[0]);
    }
    
    /**
     * 构建callback维度
     * 
     * @param table
     *            维度表
     * @return 维度定义
     */
    private Dimension[] buildCallbackDims(CallbackDimTableMetaDefine table, 
            FactTableMetaDefine factTable) {
        List<Dimension> dimensions = new ArrayList<Dimension>();
        if (table.getColumns() == null || table.getColumns().length == 0) {
            return new Dimension[0];
        }
        ReferenceDefine ref = table.getReference();
        checkReference(ref);
        
        ColumnMetaDefine column = table.getColumns()[0];
        String id = UuidGeneratorUtils.generate();
        logger.info("create dimension with id : " + id);
        String caption = StringUtils.isEmpty(column.getCaption()) ? column.getName() : column
                .getCaption();
        String name = column.getName();//this.generateDimName(table, column);
        StandardDimension dimension = initDimension(table, id, caption, name);
        dimension.setFacttableColumn(table.getReference().getMajorColumn());
        if (StringUtils.isEmpty(table.getName())) {
            dimension.setTableName(name);
        } else {
            dimension.setTableName(table.getName());
        }
        dimension.setType(DimensionType.CALLBACK);
        ColumnMetaDefine columnMetaDefine = factTable.getColumnMetaDefineByColumnName(
                table.getReference().getMajorColumn());
        dimension.setFacttableCaption(columnMetaDefine.getCaption());
        CallbackLevel level = new CallbackLevel(column.getName(), table.getUrl());
        id = UuidGeneratorUtils.generate();
        level.setId(id);
        level.setCaption(column.getCaption());
        level.setName(column.getName());
        level.setDimension(dimension);
        level.setType(LevelType.CALL_BACK);
        level.setFactTableColumn(table.getReference().getMajorColumn());
        String refreshInterval = table.getConfiguration().get(
                CallbackDimTableMetaDefine.REF_INTERNAL_KEY);
        if (StringUtils.isEmpty(refreshInterval)) {
            level.setRefreshInterval(-1);
        } else {
            level.setRefreshInterval(Integer.valueOf(refreshInterval));
        }
        Map<String, String> params = new HashMap<String, String>();
        for (String key : table.getParams().keySet()) {
            params.put(key, table.getParams().get(key));
        }
        level.setCallbackParams(params);
        // Map<String, Level> levels = new HashMap<String, Level>();
        // levels.put(level.getId(), level);
        dimension.addLevel(level);
        dimensions.add(dimension);
        return dimensions.toArray(new Dimension[0]);
    }
    
    /**
     * 初始化dimension
     * 
     * @param table
     * @param id
     * @param caption
     * @param name
     * @return
     */
    private StandardDimension initDimension(DimTableMetaDefine table, String id, String caption,
            String name) {
        StandardDimension dimension = new StandardDimension(name);
        dimension.setId(id);
        dimension.setCaption(caption);
        dimension.setName(name);
        dimension.setPrimaryKey(table.getReference().getSalveColumn());
        dimension.setVisible(true);
        return dimension;
    }
    
    /**
     * 构建普通维度
     * 
     * @param dimTable
     *            唯独表
     * @param factTable 
     *            事实表
     * @return 普通维度定义
     */
    private Dimension[] buildStandardDims(StandardDimTableMetaDefine dimTable, 
            FactTableMetaDefine factTable) {
        List<Dimension> dimensions = new ArrayList<Dimension>();
        ReferenceDefine ref = dimTable.getReference();
        checkReference(ref);
        
        String refName = dimTable.getReference().getSalveColumn();
        for (ColumnMetaDefine column : dimTable.getColumns()) {
            if (column == null) {
                continue;
            }
            
            if (refName.equals(column.getName())) {
                continue;
            }
            String id = UuidGeneratorUtils.generate();
            logger.info("create dimension with id : " + id);
            String caption = StringUtils.isEmpty(column.getCaption()) ? column.getName() : column
                    .getCaption();
            String name = this.generateDimName(dimTable, column);
            StandardDimension dimension = initDimension(dimTable, id, caption, name);
            dimension.setTableName(dimTable.getName());
            dimension.setVisible(true);
            dimension.setFacttableColumn(dimTable.getReference().getMajorColumn());
            ColumnMetaDefine columnMetaDefine = factTable.getColumnMetaDefineByColumnName(
                    dimTable.getReference().getMajorColumn());
            dimension.setFacttableCaption(columnMetaDefine.getCaption());
            List<Level> levels = buildLevels(dimTable, column, LevelType.REGULAR, dimension);
            if (levels != null) {
                for (Level level : levels) {
                    if (level == null) {
                        continue;
                    }
                    dimension.addLevel(level);
                }
            }
            dimensions.add(dimension);
        }
        if (dimensions.size() == 0) {
            return new Dimension[0];
        }
        return dimensions.toArray(new Dimension[0]);
    }
    
    /**
     * 
     * @param ref
     * 
     */
    private void checkReference(ReferenceDefine ref) {
        if (ref == null) {
            String msg = "Dimension table reference can not be null";
            logger.debug(msg);
            throw new IllegalStateException(msg);
        }
        
        // if (ref.isInvalidate()) {
        // String msg = "Dimension table reference is invalidate : {0}";
        // msg = String.format(msg, ref.toString());
        // logger.debug(msg);
        // throw new IllegalStateException(msg);
        // }
    }
    
}
