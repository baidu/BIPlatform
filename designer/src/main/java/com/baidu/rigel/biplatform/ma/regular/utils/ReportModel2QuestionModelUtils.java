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
package com.baidu.rigel.biplatform.ma.regular.utils;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryConditionLimit;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *Description:
 * @author david.wang
 *
 */
public final class ReportModel2QuestionModelUtils {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger (ReportModel2QuestionModelUtils.class);
    
    /**
     * 构造函数
     */
    private ReportModel2QuestionModelUtils() {
    }
    
    /**
     * 针对报表模型以及请求参数获取区域与问题模型对应关系
     * 注意：所有问题模型均不包含数据源信息
     * @param model 报表模型
     * @param params 报表参数映射关系 key为报表参数名称，value为参数值，字符串或数组
     * @return 报表区域id与问题模型的对应关系
     */
    public static Map<String, QuestionModel> genQuestionModelsExcludeDs(final ReportDesignModel model, 
        final Map<String, String> params) {
        long begin = System.currentTimeMillis ();
        LOG.info ("begin gen question model with report model");
        if (model == null) {
            LOG.info ("error : report model is null");
            throw new IllegalArgumentException ("必要参数确实：report mode is null");
        }
        if (ArrayUtils.isEmpty (model.getExtendAreaList ())) {
            LOG.info ("error : report model not contains area model");
            throw new IllegalArgumentException ("模型定义错误：report mode area is null");
        }
        
        final Map<String, QuestionModel> rs = Maps.newHashMap ();
        Stream.of (model.getExtendAreaList ())
            .filter (area -> isDataArea(area.getType ()))
            .forEach (area -> {
                try {
                    rs.put (area.getId (), genAreaQuestionModel(model, area, params));
                } catch (Exception e) {
                    LOG.error (e.getMessage (), e);
                    throw new RuntimeException (e.getMessage ());
                }
            });
        long end = System.currentTimeMillis ();
        LOG.info ("execute successfully : convert question model from report model . cost {} ms", (end - begin));
        return rs;
    }
    
    /**
     * 判断当前区域是否是数据区域
     * @param type 区域类型
     * @return true 是数据区域 false 不是数据区域
     */
    private static boolean isDataArea(ExtendAreaType type) {
        return type == ExtendAreaType.TABLE 
                || type == ExtendAreaType.PLANE_TABLE
                || type == ExtendAreaType.CHART;
    }

    /**
     * 
     * @param model 报表模型
     * @param area 区域
     * @param params 报表参数映射关系 key为报表参数名称，value为参数值，字符串或数组
     * @return 当前区域模型对应的问题模型
     */
    private static QuestionModel genAreaQuestionModel(ReportDesignModel model, 
        ExtendArea area, Map<String, String> params) throws Exception {
        LogicModel logicModel = area.getLogicModel ();
        if (logicModel == null) {
            return null;
        }
        switch (area.getType ()) {
            case TABLE :
                return genPivotTableQueryQuestionModel(model, area, params);
            case CHART :
                return genChartQueryQuestionModel(model, area, params);
            default :
                return genPlantableQueryQuestionModel(model, area, params);
        }
    }

    /**
     * 
     * @param model
     * @param area
     * @param params
     * @return QuestionModel
     */
    private static QuestionModel genPlantableQueryQuestionModel(
        ReportDesignModel model, ExtendArea area, Map<String, String> params) throws Exception {
        QuestionModel rs = genPivotTableQueryQuestionModel(model, area, params);
        rs.setQuerySource ("SQL");
        rs.setNeedSummary (false);
        rs.setFilterBlank (true);
        return rs;
    }

    /**
     * 
     * @param model
     * @param area
     * @param params
     * @return QuestionModel
     */
    private static QuestionModel genChartQueryQuestionModel(
        ReportDesignModel model, ExtendArea area, Map<String, String> params) throws Exception {
        // 目前chart的问题模型与多维报表并无差别，此处复用统一模型，此处需要注意将汇总方式去除
        QuestionModel rs = genPivotTableQueryQuestionModel (model, area, params);
        rs.getQueryConditions ().forEach ((k, v) -> {
            if (v instanceof DimensionCondition) {
                DimensionCondition c = (DimensionCondition) v;
                if (CollectionUtils.isNotEmpty (c.getQueryDataNodes ())) {
                    c.getQueryDataNodes ().forEach (queryData -> {
                        queryData.setExpand (true);
                        queryData.setShow (false);
                    });
                }
            }
        });
        return rs;
    }

    /**
     * 
     * @param model
     * @param area
     * @param params
     * @return QuestionModel
     */
    private static QuestionModel genPivotTableQueryQuestionModel(
            ReportDesignModel model, ExtendArea area, Map<String, String> params) 
            throws Exception {
        ConfigQuestionModel rs = new ConfigQuestionModel ();
        try {
            rs.setCube (QueryUtils.getCubeWithExtendArea (model, area));
        } catch (Exception e) {
            // For Test
            LOG.error (e.getMessage (), e);
        }
        QueryConditionLimit queryConditionLimit = new QueryConditionLimit ();
        queryConditionLimit.setWarnningConditionSize (Integer.MAX_VALUE);
        rs.setQueryConditionLimit (queryConditionLimit);
        rs.setCubeId (area.getCubeId ());
        LogicModel logicModel = area.getLogicModel ();
        if (!ArrayUtils.isEmpty (logicModel.getColumns ())) {
            AxisMeta column = genAxisMeta (model, area, logicModel.getColumns (), AxisType.COLUMN);
            rs.getAxisMetas ().put (AxisType.COLUMN, column);
        }
        if (!ArrayUtils.isEmpty (logicModel.getRows ())) {
            AxisMeta row = genAxisMeta (model, area, logicModel.getRows (), AxisType.ROW);
            rs.getAxisMetas ().put (AxisType.ROW, row);
        }
        if (!ArrayUtils.isEmpty (logicModel.getSlices ())) {
            AxisMeta filter = genAxisMeta (model, area, logicModel.getSlices (), AxisType.FILTER);
            rs.getAxisMetas ().put (AxisType.FILTER, filter);
        }
        
        Cube cube = model.getSchema ().getCubes ().get (area.getCubeId ());
        if (params != null && params.size () > 0 && model.getParams ().size () > 0) {
            // 设置查询条件
            Map<String, MetaCondition> queryConditions = genQueryConditions (model, area, params, cube);
            rs.setQueryConditions (queryConditions);
            
            // 设置callback请求参数
            resetCallbackParams (params, cube);
            // 设置参数
            rs.setRequestParams (params);
        }
        // 个性化设置
        Map<String, Object> otherSettings = area.getOtherSetting ();
        if (otherSettings.containsKey (Constants.FILTER_BLANK)) {
            if (Boolean.valueOf (otherSettings.get (Constants.FILTER_BLANK).toString ())) {
                rs.setFilterBlank (true);
            }
        }
        // 设置cube
        rs.setCube(QueryUtils.transformCube(cube));
        rs.setQuerySource("TESSERACT");
        return rs;
    }

    /**
     * 
     * @param params
     * @param cube
     */
    private static void resetCallbackParams(Map<String, String> params, Cube cube) {
        cube.getDimensions ().values ().stream ()
            .filter (dim -> isCallbackDim (dim))
            .forEach (dim -> {
                CallbackLevel level = (CallbackLevel) dim.getLevels ().values ().toArray (new Level[0])[0];
                params.forEach ((k, v) -> {
                    if (level.getCallbackParams ().containsKey (k)) {
                        level.getCallbackParams ().put (k, v);
                    }
                });
            });
    }

    /**
     * 
     * @param model
     * @param area
     * @param params
     * @param cube
     * @return Map<String, MetaCondition>
     */
    private static Map<String, MetaCondition> genQueryConditions(ReportDesignModel model, 
        ExtendArea area, Map<String, String> params, Cube cube) {
        Map<String, MetaCondition> queryConditions = Maps.newHashMap ();
        BiConsumer<? super String, ? super ReportParam> innerAction = (k, p) -> {
            if (p.isNeeded () && !params.containsKey (p.getName()) && StringUtils.isEmpty (p.getDefaultValue ())) {
                LOG.info ("param not has value [{}]", p.getName ());
                throw new RuntimeException ("必选参数未赋值: " + p.getName ());
            } 
            String value = params.get (p.getName ());
            if (StringUtils.isEmpty (value)) {
                value = p.getDefaultValue ();
            }
            
            if (!StringUtils.isEmpty (value)) {
                Dimension dim = cube.getDimensions ().get (p.getElementId ());
                if (dim == null) {
                    LOG.info ("can't get correct dim with [{}] which param [{}] mapped", 
                        p.getElementId (), p.getName ());
                    throw new RuntimeException("参数未找到对应维度信息");
                }
                // if (!isCallbackDim(dim)) {
                DimensionCondition condition = genComDimQueryCondition(value, dim);
                queryConditions.put(dim.getName(), condition);
                // }
            }
        };
        Map<String, ReportParam> reportParams = model.getParams ();
        reportParams.forEach (innerAction);
        return queryConditions;
    }

    private static DimensionCondition genComDimQueryCondition(String value, Dimension dim) {
        DimensionCondition condition = new DimensionCondition (dim.getName ());
        List<QueryData> queryDataNodes = Lists.newArrayList ();
        for (String v : value.split (",")) {
            String uniqueName = "[" + dim.getName () + "].[" + v + "]";
            QueryData data = new QueryData (uniqueName);
            data.setShow (true);
            data.setExpand (true);
            queryDataNodes.add (data);
        }
        condition.setQueryDataNodes (queryDataNodes);
        return condition;
    }

    /**
     * 
     * @param dim
     * @return true is current dimension is callback dim other false
     */
    private static boolean isCallbackDim(Dimension dim) {
        if (dim.getLevels () == null || dim.getLevels ().size () <= 0) {
            return false;
        }
        Level level = dim.getLevels ().values ().toArray (new Level[0])[0];
        return level instanceof CallbackLevel;
    }

    /**
     * 
     * @param model
     * @param area
     * @param items
     * @param type
     * @return AxisMeta
     */
    private static AxisMeta genAxisMeta(ReportDesignModel model, ExtendArea area,
        Item[] items, AxisType  type) {
        AxisMeta meta = new AxisMeta (type);
        BiConsumer<? super Boolean, ? super List<OlapElement>> action = (k, l) -> {
           if (k) {
               l.forEach (ele -> meta.getQueryMeasures ().add (ele.getName ()));
           } else {
               l.forEach (ele -> meta.getCrossjoinDims ().add (ele.getName ()));
           }
        };
        Stream.of (items)
               .map (item -> {
                   Cube cube = model.getSchema ().getCubes ().get (area.getCubeId ());
                   if (cube.getDimensions ().get (item.getOlapElementId ()) != null) {
                       return cube.getDimensions ().get (item.getOlapElementId ());
                   } else {
                       return cube.getMeasures ().get (item.getOlapElementId ());
                   }
               })
               .filter (ele -> ele != null)
               .collect (Collectors.groupingBy (ele -> ele instanceof MiniCubeMeasure))
               .forEach (action);
        return meta;
    }
}
