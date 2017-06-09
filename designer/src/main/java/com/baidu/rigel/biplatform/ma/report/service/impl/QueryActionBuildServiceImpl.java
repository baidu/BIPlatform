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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.PivotTableParseException;
import com.baidu.rigel.biplatform.ma.report.exception.PlaneTableParseException;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.OrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PlaneTable;
import com.baidu.rigel.biplatform.ma.report.service.AnalysisChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.utils.ItemUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * QueryAction构建服务
 * 
 * @author zhongyi
 *
 *         2014-8-5
 */
@Service("queryBuildService")
public class QueryActionBuildServiceImpl implements QueryBuildService {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(QueryActionBuildServiceImpl.class);
    
    /**
     * analysisChartBuildService
     */
    @Resource
    private AnalysisChartBuildService analysisChartBuildService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.QueryActionBuildService#
     * generateQueryAction()
     */
    @Override
    public QueryAction generateTableQueryAction(ReportDesignModel model, String areaId,
        Map<String, Object> context) {
        
        ExtendArea targetArea = getRealExtendArea(model, areaId, context);
        LogicModel targetLogicModel = targetArea.getLogicModel();
        String cubeId = targetArea.getCubeId();
        return generateQueryAction(model.getSchema(),
            cubeId, targetLogicModel, context, areaId, false, model);
    }

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.ma.report.service.
     * QueryBuildService#generateTableQueryActionForDrill(com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel, 
     * java.lang.String, java.util.Map)
     */
    @Override
    public QueryAction generateTableQueryActionForDrill(ReportDesignModel model, String areaId,
        Map<String, Object> contextParams, int targetIndex) {
        
        ReportDesignModel drillModel = DeepcopyUtils.deepCopy(model);
        ExtendArea targetArea = getRealExtendArea(drillModel, areaId, contextParams);
        LogicModel targetLogicModel = targetArea.getLogicModel();
        String cubeId = targetArea.getCubeId();
        List<String> rowDimsAhead = Lists.newArrayList();
        int target = targetIndex;
        for (int i = 0; i < targetIndex; i++) {
            Item rowDim = targetLogicModel.getRows()[i];
            rowDimsAhead.add(rowDim.getOlapElementId());
        }
        for (String rowAhead : rowDimsAhead) {
            Item item = targetLogicModel.removeRow(rowAhead);
            targetLogicModel.addSlice(item);
            target--;
        }
        QueryAction action = generateQueryAction(drillModel.getSchema(),
            cubeId, targetLogicModel, contextParams, areaId, false, model);
        /**
         * 把下钻的值存下来
         */
        Item item = targetLogicModel.getRows()[target];
        if (item != null && contextParams.containsKey(item.getOlapElementId())) {
            action.getDrillDimValues().put(item, contextParams.get(item.getOlapElementId()));
        }
        return action;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QueryAction generateChartQueryAction(ReportDesignModel model, String areaId,
            Map<String, Object> context, String[] indNames, ReportRuntimeModel runTimeModel)
            throws QueryModelBuildException {

        ExtendArea targetArea = DeepcopyUtils.deepCopy(model).getExtendById(areaId);
        LogicModel targetLogicModel = null;
        String cubeId = targetArea.getCubeId();
        Cube cube = QueryUtils.transformCube(model.getSchema().getCubes().get(cubeId));
        /**
         * generateQueryAction方法中需要指定logicModel所在的区域id，
         * logicModelAreaId即该区域id
         */
        String logicModelAreaId = areaId;
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            /**
             * 针对liteOlap表的查询
             */
            ExtendArea referenceArea = model.getExtendById(targetArea.getReferenceAreaId());
            logicModelAreaId = referenceArea.getId();
            cubeId = referenceArea.getCubeId();
            /**
             * 找到liteOlap的父区域，获取logicModel
             */
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) referenceArea;
            targetLogicModel = referenceArea.getLogicModel();
            /**
             * 针对liteOlap表的图形分析区查询，产生新的logicModel row和col来自用户选中的行和指标
             * row和col的获取步骤： 1. 拿到前端传来的指标id，拿到存储在runtimeModel中的selectedRowIds 2.
             * 根据当前状态，生成查询表所使用的queryAction 3. 根据queryAction得到结果缓存中表的查询结果 4.
             * 按照1中得到的参数，从结果中找到行上的维度和指标 5. 将维度、指标拼成rows和cols
             */
            List<Item> rows = Lists.newArrayList();
            List<Item> cols = Lists.newArrayList();
            /**
             * 1.
             */
            
            String[] selectedRowIds = runTimeModel.getSelectedRowIds().toArray(new String[0]);
            if (selectedRowIds.length > 1) {
                logger.warn("More than one line selected, do not support! Query as One line!");
            }
            /**
             * 2.
             */
            QueryContext queryContextForTable = runTimeModel.getLocalContextByAreaId(
                liteOlapArea.getTableAreaId());
            QueryAction actionForTable = generateTableQueryAction(model, 
                    liteOlapArea.getTableAreaId(),
                    queryContextForTable.getParams());
            /**
             * 3.
             */
            ResultSet resultSet = runTimeModel.getPreviousQueryResult(actionForTable);
            if (resultSet == null) {
                logger.error("There is no result of table for querying liteOlap Chart!!");
                throw new RuntimeException("There is no result of table for querying liteOlap Chart!!");
            }
            /**
             * 按照选中行ID得到行上的维度值
             */
            String[] uniqNames = null;
            // 当过滤空白行生效时，选中行有可能为null，所以这里需要做判断  update by majun 2015-10-20 
            if (!ArrayUtils.isEmpty(selectedRowIds)) {
                uniqNames =
                        com.baidu.rigel.biplatform.ac.util.DataModelUtils
                                .parseNodeUniqueNameToNodeValueArray(selectedRowIds[0]);
            }
            
            /**
             * 从logicmodel里面找到时间维度
             */
            List<String> timeItemIds = runTimeModel.getTimeDimItemIds();
            Item timeDimItem = null;
            for (String timeItemId : timeItemIds) {
                timeDimItem = targetLogicModel.getItemByOlapElementId(timeItemId);
                if (timeDimItem != null) {
                    break;
                }
            }
            /**
             * 从context里面查看，否是有时间维度
             */
            if (timeDimItem == null) {
                for (String key : context.keySet()) {
                    OlapElement element = ReportDesignModelUtils
                        .getDimOrIndDefineWithId(model.getSchema(), cubeId, key);
                    if (element != null && element instanceof TimeDimension) {
                        timeDimItem = new Item();
                        timeDimItem.setAreaId(areaId);
                        timeDimItem.setCubeId(cubeId);
                        timeDimItem.setId(element.getId());
                        timeDimItem.setOlapElementId(element.getId());
                        timeDimItem.setPositionType(PositionType.X);
                        timeDimItem.setSchemaId(model.getSchema().getId());
                    }
                }
            }
            if (!ArrayUtils.isEmpty(uniqNames)) {
                for (String uniqName : uniqNames) {
                    // 当uniqName是无效的，并且当前area不是liteolap的图形或者普通图形是，当前dim才作为条件传入上下文 add by majun 2015-10-20
                    if (!MetaNameUtil.isUniqueName(uniqName)
                            && (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART || targetArea.getType() == ExtendAreaType.CHART)) {
                        continue;
                    }
                    String dimName = MetaNameUtil.getDimNameFromUniqueName(uniqName);
                    Map<String, Item> store = runTimeModel.getUniversalItemStore().get(liteOlapArea.getId());
                    if (CollectionUtils.isEmpty(store)) {
                        String msg = "The item map of area (" + liteOlapArea.getId() + ") is Empty!";
                        logger.error(msg);
                        throw new RuntimeException(msg);
                    }
                    Item row = store.get(dimName);
                    if (row == null) {
                        String msg =
                                String.format("Dimension(%s) Not found in the store of Area(%s)!", dimName,
                                        liteOlapArea.getId());
                        logger.error(msg);
                        throw new RuntimeException(msg);
                    }
                    rows.add(row);
                    
                    Dimension dim = cube.getDimensions().get(dimName);
                    boolean isCallBackDim = dim.getType() == DimensionType.CALLBACK;
                    Object paramObj = context.get(row.getOlapElementId());
                    boolean isParamArray = false;
                    if (paramObj != null && paramObj instanceof String[]) {
                        isParamArray = true;
                    }
                    // 如果传过来的uniqName是个all节点，并且上下文参数里面有条件值，那么在查询的时候，需要把上下文的条件塞进queryaction里去 update by majun 2015-11-26
                    if (!isCallBackDim && isParamArray && !MetaNameUtil.isAllMemberUniqueName(uniqName)) {
                        String[] paramObjArray = (String[]) paramObj;
                        List<String> rsList = new ArrayList<String>();
                        for (String paramStr : paramObjArray) {
                            // 如果发现传入的uniqName要比条件上下文里的paramStr层级多，那么当前条件直接取uniqName即可
                            if (MetaNameUtil.parseUnique2NameArray(uniqName).length >= MetaNameUtil
                                    .parseUnique2NameArray(paramStr).length) {
                                rsList.add(uniqName);
                                break;
                            } else if (paramStr.startsWith(uniqName)) {
                                rsList.add(paramStr);
                            }
                        }
                        context.put(row.getOlapElementId(), rsList.toArray(new String[0]));
                    } else if (!isCallBackDim && MetaNameUtil.isAllMemberUniqueName(uniqName) && isParamArray) {
                        // do nothing
                    } else {
                        context.put(row.getOlapElementId(), uniqName);
                    }

                    Map<String, Object> contextCopy = DeepcopyUtils.deepCopy(context);
                    context.putAll(this.handleUniqueName4Callback(uniqName, row.getOlapElementId(), contextCopy,
                            cubeId, model));
                }
            }
            for (String indName : indNames) {
                cols.add(liteOlapArea.listAllItems().get(indName));
            }
            LogicModel tmp = analysisChartBuildService.generateTrendChartModel(targetLogicModel,
                    model.getSchema(), liteOlapArea.getCubeId(),
                    DeepcopyUtils.deepCopy(rows), DeepcopyUtils.deepCopy(cols), timeDimItem);
            return generateQueryAction(model.getSchema(),
                cubeId, tmp, context, logicModelAreaId, true, model);
        } else {
            targetLogicModel = targetArea.getLogicModel();
            LogicModel cpModel = DeepcopyUtils.deepCopy(targetArea.getLogicModel());
            List<String> timeItemIds = runTimeModel.getTimeDimItemIds();
            Item timeDimItem = null;
            /**
             *  cpModel.getItemByOlapElementId(timeItemId);需要增加一个按所在轴取item的方法
             */
//            for (String timeItemId : timeItemIds) {
//                timeDimItem = cpModel.getItemByOlapElementId(timeItemId);
//                if (timeDimItem != null) {
//                    break;
//                }
//            }
            
//            if (timeDimItem != null && timeDimItem.getPositionType() == PositionType.X) { // 时间序列图
//                Map<String, Object> params = timeDimItem.getParams();
//                params.put("range", true);
//                timeDimItem.setParams(params);
//                context.put("time_line", timeDimItem);
//                isTimeTrend = true;
//            }
            boolean isTimeTrend=false;
            for (String timeItemId : timeItemIds) {
                timeDimItem = cpModel.getItemByOlapElementId(timeItemId , PositionType.X);
                if (timeDimItem != null) {
                    Map<String, Object> params = timeDimItem.getParams();
                    params.put("range", true);
                    timeDimItem.setParams(params);
                    context.put("time_line", timeDimItem);
                    isTimeTrend = true;
                    break;
                }
            }       
            
            
            
            if (cpModel != null && !CollectionUtils.isEmpty(cpModel.getSelectionMeasures())) {
                cpModel.addColumns(cpModel.getSelectionMeasures().values().toArray(new Item[0]));
            }
            // 修正查询条件，重新设置查询指标
            Object index = context.get(Constants.CHART_SELECTED_MEASURE);
            if (index != null) {
                modifyModel(cpModel, Integer.valueOf(index.toString()));
            }
           return generateQueryAction(model.getSchema(),
               cubeId, cpModel, context, logicModelAreaId, isTimeTrend, model);
        }
        
    }
    
    /**
     * 
     * @param uniqueName
     * @param elementId
     * @param cubeId
     * @param model
     * @return
     */
    private Map<String, Object> handleUniqueName4Callback(String uniqueName, String elementId, Map<String, Object> context, 
            String cubeId, ReportDesignModel model) {
        Map<String, Object> params = Maps.newHashMap();
        Map<String, ReportParam> modelParams = model.getParams();
        String paramName = null;
        if (modelParams != null && modelParams.size() != 0) {
            for (ReportParam reportParam : modelParams.values()) {
                if (reportParam.getElementId().equals(elementId)) {
                    paramName = reportParam.getName();
                    break;
                }
            }
        }
        if (MetaNameUtil.isUniqueName(uniqueName)) {
            String[] values = MetaNameUtil.parseUnique2NameArray(uniqueName);
            String paramValue = values[values.length - 1];
            MiniCube cube = (MiniCube) model.getSchema().getCubes().get(cubeId);
            if (cube != null) {
                Map<String, Dimension> dims = cube.getDimensions();
                if (dims != null && dims.size() != 0 && dims.containsKey(elementId) && paramName != null) {
                    Dimension dimension = dims.get(elementId);
                    Level level = dimension.getLevels().values().toArray(new Level[0])[0];
                    if (level.getType() == LevelType.CALL_BACK) {
                        params.put(paramName, paramValue);
                    }
                }
            }
        }
        return params;
    }
    /**
     * 修正查询条件
     * @param model
     * @param index
     */
    private void modifyModel(LogicModel model, Integer index) {
        Item[] items = new Item[1];
        Item[] selMeasures = model.getSelectionMeasures().values().toArray(new Item[0]);
        if (index >= selMeasures.length) {
            throw new IndexOutOfBoundsException("索引越界");
        }
        items = new Item[]{ selMeasures[index] };
        model.resetColumns(items);
    }

    /**
     * 生成QueryAction
     * 
     * @param targetLogicModel
     * @param context
     * @return
     */
    private QueryAction generateQueryAction(Schema schema, String cubeId,
            LogicModel targetLogicModel, Map<String, Object> context,
            String areaId, boolean needTimeRange, ReportDesignModel reportModel) {
        
        final Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            return null;
        }
        if (targetLogicModel == null) {
            return null;
        }
        QueryAction action = new QueryAction();
        action.setExtendAreaId(areaId);
        
        /**
         * TODO 生成一个独立的id
         */
        String id = UuidGeneratorUtils.generate();
        action.setId(id);
        
        /**
         * TODO 生成path
         */
        String queryPath = "";
        action.setQueryPath(queryPath);
        
        Cube oriCube4QuestionModel = genCube4QuestionModel (schema, cubeId,
            targetLogicModel, context, areaId, reportModel);
        
        Map<Item, Object> columns = genereateItemValues(schema,
            cubeId, targetLogicModel.getColumns(), context, needTimeRange, oriCube4QuestionModel);
        action.setColumns(columns);
        
        Map<Item, Object> rows = genereateItemValues(schema,
                cubeId, targetLogicModel.getRows(), context, needTimeRange, oriCube4QuestionModel);
        action.setRows(rows);
        
        Map<Item, Object> slices = genereateItemValues(schema,
            cubeId, targetLogicModel.getSlices(), context, needTimeRange, oriCube4QuestionModel);
        action.setSlices(slices);
        
        
        
        //V2
//        Map<Item, Object> columns2 = genereateItemValuesV2(schema, cubeId,
//                targetLogicModel.getColumns(), context, needTimeRange,
//                oriCube4QuestionModel);
//        action.setColumns(columns);
//
//        Map<Item, Object> rows2 = genereateItemValuesV2(schema, cubeId,
//                targetLogicModel.getRows(), context, needTimeRange,
//                oriCube4QuestionModel);
//        action.setRows(rows);
//
//        Map<Item, Object> slices2 = genereateItemValuesV2(schema, cubeId,
//                targetLogicModel.getSlices(), context, needTimeRange,
//                oriCube4QuestionModel);
//        action.setSlices(slices);
        
        if (needTimeRange) {
        // 这里需要将lite-olap中的图的维度修改,将column的item放入到过滤轴上
            Map<Item, Object> columnsTmp = Maps.newLinkedHashMap();
            for (Item item : columns.keySet()) {
                OlapElement element = ItemUtils.getOlapElementByItem(item, schema, cubeId);
                if (element instanceof Dimension) {
                    slices.put(item, columns.get(item));
                } else {
                    columnsTmp.put(item, columns.get(item));
                }
            }
            action.setColumns(columnsTmp);
            action.setSlices(slices);
        }
        fillFilterBlankDesc (areaId, reportModel, action);
        ExtendArea area = reportModel.getExtendById(areaId);
        QueryAction.OrderDesc orderDesc = null;
        if (area.getType() != ExtendAreaType.PLANE_TABLE) {
            if (area.getType() != ExtendAreaType.CHART
                    || (targetLogicModel.getTopSetting() != null 
                    && !StringUtils.isEmpty(targetLogicModel.getTopSetting().getMeasureId()))) {
                orderDesc = genMeasureOrderDesc (targetLogicModel, context, action, cube);
            }
            if (orderDesc == null) {
                orderDesc = genDimensionOrderDesc(targetLogicModel, action, cube);
            }
        }
        logger.info ("[INFO] -------- order desc = " + orderDesc);
        action.setOrderDesc(orderDesc);            
        // remove dumplated conditions
        Iterator<Item> it = action.getSlices ().keySet ().iterator ();
        while (it.hasNext ()) {
            Item item = it.next ();
            if (action.getColumns ().containsKey (item)) {
                it.remove ();
            }
        }
        
        action.setTrendQuery(needTimeRange);

        return action;
    }

    private Cube genCube4QuestionModel(Schema schema, String cubeId,
            LogicModel targetLogicModel, Map<String, Object> context,
            String areaId, ReportDesignModel reportModel) {
        Cube oriCube4QuestionModel = null;
        try {
            oriCube4QuestionModel = QueryUtils.getCubeWithExtendArea(reportModel, reportModel.getExtendById(areaId));
        } catch (QueryModelBuildException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        for (String key : context.keySet()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, key);
//            Object level = context.get(key + "_level");
            if (element != null && element instanceof TimeDimension
                    && !targetLogicModel.containsOlapElement(element.getId())) {
                Item item = new Item();
                item.setAreaId(areaId);
                item.setCubeId(cubeId);
                item.setId(element.getId());
                item.setOlapElementId(element.getId());
                item.setPositionType(PositionType.S);
                item.setSchemaId(schema.getId());
                targetLogicModel.addSlice(item);
            }
            
            // TODO 修正过滤条件
            if (oriCube4QuestionModel != null) {
                oriCube4QuestionModel.getDimensions().values().forEach(dim -> {
                    if (dim.getId().equals(key)) {
                        Item item = new Item();
                        if (targetLogicModel.getItemByOlapElementId(key) != null) {
                            item = targetLogicModel.getItemByOlapElementId(key) ;
                        }
                        item.setAreaId(areaId);
                        item.setCubeId(cubeId);
                        item.setId(dim.getId());
                        item.setOlapElementId(dim.getId());
                        if (item.getPositionType() == null) {
                            item.setPositionType(PositionType.S);
                        }
                        item.setSchemaId(schema.getId());
                        targetLogicModel.addSlice(item);
                    }
                });
            }
        }
        return oriCube4QuestionModel;
    }

    /**
     * 获取平面表排序的第一列
     * @param targetLogicModel
     * @param action
     * @param cube
     * @return
     */
    public OrderDesc genFirstOrderDescForPlaneTable(LogicModel targetLogicModel, 
            QueryAction action, final Cube cube) {
        if (targetLogicModel == null) {
            return null;
        }
        if (cube == null) {
            return null;
        }
        Item[] items = targetLogicModel.getColumns();
        if (items != null && items.length != 0) {
            Item item = items[0];
            if (cube.getMeasures() != null) {
                for (Measure measure : cube.getMeasures().values()) {
                    if (measure.getId().equals(item.getOlapElementId())) {
                        return new OrderDesc(measure.getName(), "DESC", 500);
                    }
                }                
            }
            
            if (cube.getDimensions() != null) {
                for (Dimension dimension : cube.getDimensions().values()) {
                    if (dimension.getId().equals(item.getOlapElementId())) {
                        return new OrderDesc(dimension.getName(), "DESC", 500);
                    }
                }
            }
        }
        return null;
    }
    /**
     * 产生维度排序，对于平面表排序，默认使用第一个指标，如果指标为空，则使用第一个维度
     * 注:此处维度和指标使用同一个指标排序信息，后续考虑修改
     * @param targetLogicModel
     * @param cube
     * @return
     */
    private OrderDesc genDimensionOrderDesc(LogicModel targetLogicModel, QueryAction action ,
        final Cube cube) {
        if(!action.getColumns().isEmpty()) {
            Dimension[] tmp = action.getColumns().keySet().stream().filter(item -> {
                return cube.getDimensions().get(item.getOlapElementId()) != null;
            }).map(item -> {
                return cube.getDimensions().get(item.getOlapElementId());
            }).toArray(Dimension[] :: new);
            if (tmp != null && tmp.length >0) {
                if (tmp[0] instanceof TimeDimension) {
                    Level l = tmp[0].getLevels().values().toArray(new Level[0])[0];
                    return new OrderDesc(l.getFactTableColumn(), "DESC", 500);
                }
                return new OrderDesc(tmp[0].getName(), "DESC", 500);
            }
        }
        return null;
    }
    /**
     * 产生指标排序
     * @param targetLogicModel
     * @param context
     * @param action
     * @param cube
     * @return
     */
    private OrderDesc genMeasureOrderDesc(LogicModel targetLogicModel,
        Map<String, Object> context, QueryAction action, final Cube cube) {
        Map<String, Measure> measures = cube.getMeasures();
        MeasureTopSetting topSet = targetLogicModel.getTopSetting();
        if (!action.getColumns().isEmpty()) {
            if (topSet == null) {
                Measure[] tmp = action.getColumns().keySet().stream().filter(item -> {
                    return cube.getMeasures().get(item.getOlapElementId()) != null;
                }).map(item -> {
                    return cube.getMeasures().get(item.getOlapElementId());
                }).toArray(Measure[] :: new);
                if (tmp != null && tmp.length > 0 && context.get(Constants.NEED_LIMITED) == null) {
                    if ( isTimeDimOnFirstCol(action.getRows (), cube)) {
                        return new OrderDesc(tmp[0].getName(), "NONE", 500);
                    }
                    return new OrderDesc(tmp[0].getName(), "DESC", 500);
                } else  if (context.get ("time_line") != null) { // 时间序列图
                    return new OrderDesc (tmp[0].getName (), "NONE", Integer.MAX_VALUE);
                } else {
                    context.remove(Constants.NEED_LIMITED);
                    boolean isTimeDimOnFirstCol = isTimeDimOnFirstCol(action.getRows (), cube);
                    if (isTimeDimOnFirstCol) {
                        return new OrderDesc(tmp[0].getName(), "NONE", Integer.MAX_VALUE);
                    }
                    if (tmp != null && tmp.length != 0) {
                        return new OrderDesc(tmp[0].getName(), "DESC", Integer.MAX_VALUE);                        
                    }
                }
            } else {
                    if (context.get("time_line") != null) { //时间序列图
                    return  new OrderDesc(
                        measures.get(topSet.getMeasureId()).getName(), "NONE", Integer.MAX_VALUE);
                    }
                String olapElementId = action.getColumns().keySet().toArray(new Item[0])[0].getOlapElementId();
                return  new OrderDesc(measures.get(olapElementId).getName(),
                    topSet.getTopType().name(), topSet.getRecordSize());
            }
        }
        return null;
    }

    private boolean isTimeDimOnFirstCol(Map<Item, Object> rows, Cube cube) {
        if (rows  == null || rows.size () == 0) {
            return false;
        }
        Item[] items = rows.keySet ().toArray (new Item[0]);
        Dimension dim = cube.getDimensions ().get (items[0].getOlapElementId ());
        return dim instanceof TimeDimension;
    }

    /**
     * 
     * @param areaId
     * @param reportModel
     * @param action
     */
    private void fillFilterBlankDesc(String areaId, ReportDesignModel reportModel, QueryAction action) {
        ExtendArea area = reportModel.getExtendById(areaId);
        if (area.getType() == ExtendAreaType.TABLE || area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            Object filterBlank = area.getOtherSetting().get(Constants.FILTER_BLANK);
            if (area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
                filterBlank =
                        reportModel.getExtendById(area.getReferenceAreaId()).getOtherSetting()
                                .get(Constants.FILTER_BLANK);
            }
            if (filterBlank == null) {
                action.setFilterBlank(false);
            } else {
                action.setFilterBlank(Boolean.valueOf(filterBlank.toString()));
            }
        }
    }
    
    /**
     * 
     * @param items
     * @param values
     * @param oriCube 修正后的cube
     * @return
     */
    private Map<Item, Object> genereateItemValues(Schema schema,
            String cubeId, Item[] items, Map<String, Object> values,
            boolean timeRange, Cube oriCube) {
        /**
         * item必须保证顺序
         */
        Map<Item, Object> itemValues = Maps.newLinkedHashMap();
        boolean update = true;
        if (CollectionUtils.isEmpty(values)) {
            update = false;
        }
        for (Item item : items) {
            String elementId = item.getOlapElementId();
            if (StringUtils.isEmpty(elementId)) {
                continue;
            }
            if ("519caaf2452c579c6f4e72cfdb65d247".equals(elementId)) {
                System.out.println("");
            }
            // 是否会影响其他值
            Object showLevel = values.get(item.getOlapElementId() + "_level");
            if (showLevel != null) {
                item.getParams().put(Constants.LEVEL, Integer.valueOf(showLevel.toString()));
            }
            OlapElement element = ItemUtils.getOlapElementByItem(item, schema, cubeId);
            if (element == null) {
                for (Dimension dim : oriCube.getDimensions().values()) {
                    if (dim.getId().equals(item.getOlapElementId())) {
                        element = dim;
                        break;
                    }
                }
            }
            Object value = null;
            // TODO 支持url传参数，需后续修改,dirty solution
            // 第一个条件判断是否包含url规定的参数
            // 第二个条件判断是否为下钻,下钻不走此流程
            if ((values.containsKey(Constants.ORG_NAME) || values.containsKey(Constants.APP_NAME)) 
                    && ! (values.containsKey("action") && values.get("action").equals("expand")) 
                    && element instanceof StandardDimension
                    && (item.getPositionType() == PositionType.X || item.getPositionType() == PositionType.S)) {
                StandardDimension standardDim = (StandardDimension) element;
                Map<String, Level> levels = standardDim.getLevels();
                List<String> list = new ArrayList<String>();
                if (levels != null) {
                    for (String key : levels.keySet()) {
                        Level level = levels.get(key);
                        // 获取level所属的维度
                        Dimension dim = level.getDimension();
                        String tableName = dim.getTableName();
                        String name = dim.getName();
                        // 维度的列名
                        String columnName = name.replace(tableName + "_", "");
                        if (columnName.equals(Constants.ORG_NAME) || values.containsKey(Constants.APP_NAME)) {
                            Object val = values.get(columnName);
                            if (val instanceof String) {
                                String [] vals = ((String) val).split(",");
                                for (int i = 0; i < vals.length; i++) {
                                    String temp = "[" + standardDim.getName() + "]";
                                    temp += ".[" + vals[i] + "]";
                                    list.add(temp);
                                }
                            }
                        }
                    }
                }
                value = list.toArray(new String[0]);
            } else {
                value = update 
                    && values.containsKey(elementId) ? values.get(elementId) : item.getParams().get(elementId);
            }
                
            // 时间维度特殊处理
            if (element instanceof TimeDimension) {
                List<String> timeDimItemValue = generateTimeDimensionItemValues(
                        value != null ? value.toString() : null, element, timeRange);
                itemValues.put(item,
                        !CollectionUtils.isEmpty(timeDimItemValue) ? timeDimItemValue.toArray(new String[0]) : null);
            } else if (value instanceof String && !StringUtils.isEmpty(value)) {
                itemValues.put(item, value.toString().split(","));
            } else {
                itemValues.put(item, value);
            }
        }
        return itemValues;
    }
    
    /**
     * 获取时间维度参数解析结果
     * 
     * @param value
     *            element(时间维度)的参数
     * @param element
     *            （时间维度）
     * @param timeRange
     *            是否是时间趋势图
     * @return List<String>
     */
    private List<String> generateTimeDimensionItemValues(String value, OlapElement element,
        boolean timeRange) {
        List<String> result = null;

        if (value != null && !value.toString().toLowerCase().contains("all")) {
            List<String> tmpDays = Lists.newArrayList();
            if (value.contains("_")) {
                String dimName = MetaNameUtil.getDimNameFromUniqueName(value);
                for (String valueTmp : MetaNameUtil.getNameFromMetaName(value).split("_")) {
                    tmpDays.add("[" + dimName + "].[" + valueTmp + "]");
                }
                result = tmpDays;
            } else {
                String[] dataRange = getDateRangeCondition(element, value, timeRange);
                TimeRangeDetail range = new TimeRangeDetail(dataRange[0], dataRange[1]);
                String[] detailDays = range.getDays();
                TimeDimension tmp = (TimeDimension) element;
                for (int i = 0; i < detailDays.length; i++) {
                    if (timeRange && tmp.getDataTimeType() == TimeType.TimeWeekly && i % 7 == 0) {
                        tmpDays.add("[" + element.getName() + "].[" + detailDays[i] + "]");
                    } else if (timeRange && tmp.getDataTimeType() == TimeType.TimeMonth
                            && detailDays[i].endsWith("01")) {
                        tmpDays.add("[" + element.getName() + "].[" + detailDays[i] + "]");
                    } else if (timeRange && tmp.getDataTimeType() == TimeType.TimeQuarter) {
                        Set<String> quarterStart = Sets.newHashSet("0101", "0401", "0701", "1001");
                        String endStr = detailDays[i].substring(4);
                        if (quarterStart.contains(endStr)) {
                            tmpDays.add("[" + element.getName() + "].[" + detailDays[i] + "]");
                        }
                    } else if (timeRange && tmp.getDataTimeType() == TimeType.TimeDay) {
                        tmpDays.add("[" + element.getName() + "].[" + detailDays[i] + "]");
                    } else if (!timeRange) {
                        tmpDays.add("[" + element.getName() + "].[" + detailDays[i] + "]");
                    }
                }
                result = tmpDays;
            }
            
            logger.debug("[DEBUG] --- ---" + tmpDays);
        } else {
            result = Lists.newArrayList();
            if (value != null) {
                result.add(value);
            }
            
        }
        return result;
    }
    
    /**
     * 获取时间范围条件
     * @param element （时间维度）
     * @param value element(时间维度)的参数
     * @param timeRange 是否是时间趋势图
     * @return String[]
     */
    private String[] getDateRangeCondition(OlapElement element, Object value, boolean timeRange) {
        String[] dataRange = new String[2];
        
        try {
            // 解析传入参数,拿到 start & end
            if (MetaNameUtil.isUniqueName(value.toString())) {
                // uniqueName格式
                String[] tmp = MetaNameUtil.parseUnique2NameArray(value.toString());
                dataRange = getDataRangeWithValue((TimeDimension) element, tmp[tmp.length - 1]);
            } else {
                // json串格式
                JSONObject json = new JSONObject(String.valueOf(value));
                dataRange[0] = json.getString("start");
                dataRange[1] = json.getString("end");
                TimeType timeType = TimeUtils.getTimeTypeWithGranularitySymbol(json
                    .getString("granularity"));
                
                if (dataRange[0].contains("-") && dataRange[1].contains("-")) {
                    dataRange[0] = dataRange[0].replace("-", "");
                    dataRange[1] = dataRange[1].replace("-", "");
                    Map<String, String> time = TimeUtils.getTimeCondition(dataRange[0],
                            dataRange[1], timeType);
                    dataRange[0] = time.get("start");
                    dataRange[1] = time.get("end");
                }
                
            }
            
            // 处理时间参数
            /**
             * 如果是时间趋势图（ range=true, timeRange =true） 理想情况下遵守以下规则： 1）日粒度:
             * 当start=end时，取最近30天；否则取指定start-end范围
             * 2）周粒度：当start=end时，取最近4周；否则取指定start-end范围
             * 3）月粒度：当start=end时，取最近12个月；否则取指定start-end范围
             * 4）季粒度：当start=end时，取最近4个季 实际情况下，如果是时间趋势图（ range=true, timeRange
             * =true） 1）日粒度: 取最近30天； 2）周粒度：取最近4周； 3）月粒度：取最近12个月； 4）季粒度：取最近4个季
             * 
             */
            if (timeRange) {
                TimeDimension timeDim = (TimeDimension) element;
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                Date start = format.parse(dataRange[0]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(start);
                switch (timeDim.getDataTimeType()) {
                    case TimeDay:
                        cal.add(Calendar.DAY_OF_YEAR, -30);
                        dataRange[0] = format.format(cal.getTime());
                        
                        break;
                    case TimeWeekly:
                        cal.add(Calendar.DAY_OF_YEAR, -21);
                        dataRange[0] = format.format(cal.getTime());
                        break;
                    case TimeMonth:
                        cal.add(Calendar.MONTH, -11);
                        dataRange[0] = format.format(cal.getTime());
                        break;
                    case TimeQuarter:
                        cal.add(Calendar.MONTH, -9);
                        dataRange[0] = format.format(cal.getTime());
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            logger.warn("Time Condition not Correct. Maybe from row."
                + " Try to use it as UniqueName. Time: " + value, e);
            if (value instanceof String[]) {
                String[] dates = (String[]) value;
                dataRange[0] = parseToDate(dates[0]);
                dataRange[1] = parseToDate(dates[dates.length - 1]);
            } else {
                dataRange[0] = parseToDate(String.valueOf(value));
                dataRange[1] = parseToDate(String.valueOf(value));
            }
            
        }
        
        return dataRange;
    }


    /**
     * 
     * @param timDim
     * @param value
     * @return String[]
     */
    private String[] getDataRangeWithValue(TimeDimension timDim, String value) {
        Calendar cal = Calendar.getInstance ();
        try {
            cal.setTime (new SimpleDateFormat ("yyyyMMdd").parse (value));
            cal.setFirstDayOfWeek (Calendar.MONDAY);
        } catch (ParseException e) {
            throw new RuntimeException (e.getMessage (), e);
        }
        TimeRangeDetail days = null;       
        switch (timDim.getLevels ().values ().toArray (new Level[0])[0].getType ()) {
            case TIME_YEARS:
                days = TimeUtils.getYearDays (cal.getTime ());
                return new String[] {days.getStart (), days.getEnd ()};
            case TIME_QUARTERS:
                days = TimeUtils.getQuarterDays (cal.getTime ());
                return new String[] {days.getStart (), days.getEnd ()};
            case TIME_MONTHS:
                days = TimeUtils.getMonthDays (cal.getTime ());
                return new String[] {days.getStart (), days.getEnd ()};
            case TIME_WEEKS:
                days = TimeUtils.getWeekDays (cal.getTime ());
                return new String[] {days.getStart (), days.getEnd ()};
            case TIME_DAYS:
                return new String[] {value, value};
            default:
        }
        throw new RuntimeException ("Unsupported time dim type");
    }
    
    private String parseToDate(String uniqueName) {
        String[] valueParts = StringUtils.split(uniqueName, "].[");
        if (valueParts.length > 1) {
            String part = valueParts[valueParts.length - 1];
            return part.replace("]", "");
        }
        return uniqueName;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.QueryActionBuildService#
     * generateQueryContext(java.util.Map)
     */
    @Override
    public QueryContext generateQueryContext(String areaId, Map<String, String[]> contextParams) {
        
        QueryContext context = new QueryContext();
        context.setExtendAreaId(areaId);
        Map<String, Object> params = Maps.newHashMap();
        context.setParams(params);
        return context;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.report.service.QueryBuildService#
     * parseToPivotTable(com.baidu.rigel.biplatform.ma.report.query.DataModel)
     */
    @Override
    public PivotTable parseToPivotTable(Cube cube, DataModel dataModel, LogicModel logicModel) throws PivotTableParseException {
        PivotTable table = DataModelUtils.transDataModel2PivotTable(cube, dataModel, false, 0, false, logicModel);
        return table;
    }

    private ExtendArea getRealExtendArea(ReportDesignModel model, String areaId,
            Map<String, Object> contextParams) {
        
        ExtendArea targetArea = model.getExtendById(areaId);
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            /**
             * 针对liteOlap表的查询
             */
            targetArea = model.getExtendById(targetArea.getReferenceAreaId());
        } 
        return targetArea;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public PlaneTable parseToPlaneTable(Cube cube, DataModel dataModel,
            LogicModel logicModel, FormatModel formatModel, QueryAction queryAction) 
            throws PlaneTableParseException {
        PlaneTable planeTable = DataModelUtils
            .transDataModel2PlaneTable(cube, dataModel,logicModel, formatModel, queryAction);
        return planeTable;
    }
    
}