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
package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.PlaceHolderUtils;
import com.baidu.rigel.biplatform.ma.divide.table.service.DivideTableContext;
import com.baidu.rigel.biplatform.ma.divide.table.service.DivideTableService;
import com.baidu.rigel.biplatform.ma.divide.table.service.impl.DayDivideTableStrategyServiceImpl;
import com.baidu.rigel.biplatform.ma.divide.table.service.impl.MonthDivideTableStrategyServiceImpl;
import com.baidu.rigel.biplatform.ma.divide.table.service.impl.YearDivideTableStrategyServiceImpl;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.utils.HttpUrlUtils;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.OrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesDataUnit;
import com.baidu.rigel.biplatform.ma.report.query.newtable.utils.MutilDimTableUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * 
 * 查询工具类，负责将QueryAction转化城QuestionModel
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class QueryUtils {
  
    private static final Logger LOG = LoggerFactory.getLogger (QueryUtils.class);
    
    private static final String NEED_SUMMARY = "needSummary";
    /**
     * 构造函数
     */
    private QueryUtils() {
        
    }
    
    /**
     * 
     * 将查询动作转化成问题模型
     * @param dsDefine 
     * 
     * @param queryAction
     *            查询动作
     * @return 问题模型
     * @throws QueryModelBuildException
     *             构建失败异常
     */
    public static QuestionModel convert2QuestionModel(DataSourceDefine dsDefine,
            ReportDesignModel reportModel,
            QueryAction queryAction, Map<String, Object> requestParams, 
            PageInfo pageInfo, String securityKey) throws QueryModelBuildException {
        if (queryAction == null) {
            throw new QueryModelBuildException("query action is null");
        }
        ConfigQuestionModel questionModel = new ConfigQuestionModel();
        String areaId = queryAction.getExtendAreaId();
        if (StringUtils.isEmpty(areaId)) {
            throw new QueryModelBuildException("area id is empty");
        }
        ExtendArea area = reportModel.getExtendById(areaId);
        if (area == null) {
            throw new QueryModelBuildException("can not get area with id : " + areaId);
        }
        Cube cube = getCubeFromReportModel(reportModel, area);
        if (cube == null) {
            throw new QueryModelBuildException("can not get cube define in area : " + areaId);
        }
        if (area.getType() == ExtendAreaType.PLANE_TABLE) {
            cube = transformCube(cube);          
            MiniCube miniCube = (MiniCube) cube;
            for (Entry<String, Measure> entry : miniCube.getMeasures().entrySet()) {
                MiniCubeMeasure m = (MiniCubeMeasure) entry.getValue();
                m.setAggregator(Aggregator.NONE);
            }
            DivideTableStrategyVo divideVo = miniCube.getDivideTableStrategyVo();
            DivideTableContext divideContext = new DivideTableContext();
            DivideTableService divideTableService = null;
            if (divideVo != null) {
                switch (divideVo.getCondition()) {
                    case "yyyyMMdd":
                        divideTableService = new DayDivideTableStrategyServiceImpl(); 
                        break;
                    case "yyyyMM":
                        divideTableService = new MonthDivideTableStrategyServiceImpl(); 
                        break;
                    case "yyyy":
                        divideTableService = new YearDivideTableStrategyServiceImpl(); 
                        break;
                    default:
                        throw new UnsupportedOperationException("暂时不支持该分表策略");
                }
                divideContext.setDivideTableService(divideTableService);
                if (divideContext.getAllFactTableName(divideVo, requestParams) != null) {
                    miniCube.setSource(divideContext.getAllFactTableName(divideVo, requestParams));                    
                }
                MiniCubeSchema schema = (MiniCubeSchema) reportModel.getSchema();
                schema.getCubes().put(miniCube.getId(), miniCube);
                reportModel.setSchema(schema);
            }
            miniCube.setProductLine(dsDefine.getProductLine());
            questionModel.setCube(miniCube);
        } else {
            cube = getCubeWithExtendArea(reportModel, area);
            ((MiniCube) cube).setProductLine(dsDefine.getProductLine());
            questionModel.setCube(cube);
        }
        // 设置轴信息
        questionModel.setAxisMetas(buildAxisMeta(reportModel.getSchema(), area, queryAction));
       
        questionModel.setCubeId(area.getCubeId());
        // TODO 动态更新cube 针对查询过程中动态添加的属性 需要仔细考虑此处逻辑
        Set<Item> tmp = Sets.newHashSet();
        tmp.addAll(queryAction.getSlices().keySet());
        tmp.addAll(queryAction.getRows().keySet());
        try {
            DataSourceInfo dataSource = DataSourceConnectionServiceFactory
                    .getDataSourceConnectionServiceInstance(dsDefine.getDataSourceType().name ())
                    .parseToDataSourceInfo (dsDefine, securityKey);
            questionModel.setDataSourceInfo (dataSource);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        OrderDesc orderDesc = queryAction.getOrderDesc();
        if (orderDesc != null) {
            SortType sortType = SortType.valueOf(orderDesc.getOrderType());
            String uniqueName = "";
            if (DataModelUtils.isMeasure(orderDesc.getName(), cube)) {
                uniqueName = uniqueName + "[Measure].";
            } else {
                uniqueName = uniqueName + "[Dimension].";
            }
            uniqueName = uniqueName + "[" +orderDesc.getName()+ "]";
            SortRecord sortRecord = new SortRecord(sortType, uniqueName , orderDesc.getRecordSize());
            questionModel.setSortRecord(sortRecord);
        }
        // TODO 此处没有考虑指标、维度交叉情况，如后续有指标维度交叉情况，此处需要调整
        questionModel.getQueryConditionLimit().setWarningAtOverFlow(false);
        if (queryAction.isNeedOthers()) {
            // TODO 需要开发通用工具包 将常量定义到通用工具包中
            questionModel.getRequestParams().put("NEED_OTHERS", "1");
        }
        if(queryAction.isChartQuery()){
            questionModel.getRequestParams().put("isChartQuery", "true");
        }
     // 设置请求参数信息
        if (requestParams != null) {
            for (String key : requestParams.keySet()) {
                Object value = requestParams.get(key);
                if (value != null && value instanceof String) {
                    questionModel.getRequestParams().put(key, (String) value);
                }
            } 
            // 暂时将设计器状态下的limit 100的条件限制去掉，这样保证在编辑器状态下也能看到全量数据，并且预览和发布不会受limit条件的影响 update by majun
            // 设计器中, 设置分页信息
            // if (requestParams.get(Constants.IN_EDITOR) != null
            // && Boolean.valueOf(requestParams.get(Constants.IN_EDITOR).toString())) {
            // questionModel.setPageInfo(pageInfo);
            // }
        }
        if (area.getType() == ExtendAreaType.PLANE_TABLE) {
            questionModel.setQuerySource("SQL");
            // 对于平面表不使用汇总方式
            questionModel.setNeedSummary(false);
            // 设置分页信息
            questionModel.setPageInfo(pageInfo);
            // 针对平面表构建查询条件
            Map<String, MetaCondition> conditionsForPlaneTable = 
                QueryConditionUtils.buildQueryConditionsForPlaneTable(reportModel, area, queryAction);
            questionModel.setQueryConditions(conditionsForPlaneTable);            
        } else {
            // 针对其他情况构建查询条件
            Map<String, MetaCondition> conditionsForPivotTable = 
                QueryConditionUtils.buildQueryConditionsForPivotTable(reportModel, area, queryAction);
            questionModel.setQueryConditions(conditionsForPivotTable);        
            if (queryAction.getDrillDimValues() == null || !queryAction.getDrillDimValues().isEmpty()
                    || queryAction.isChartQuery()) {
                questionModel.setNeedSummary(false);
            } else {
                ExtendAreaType areaType = reportModel.getExtendById(queryAction.getExtendAreaId()).getType();
                if (areaType != ExtendAreaType.TABLE && areaType != ExtendAreaType.LITEOLAP_TABLE) {
                    questionModel.setNeedSummary(false);
                } else {
//                    Object newNeedSummaryConfigValue = "";
//                    Object oldneedSummaryConfigValue = "";
                    // 如果是liteolap表格，则需要取到其引用的父model的area区域对象才能取到相关other配置
//                    if (areaType == ExtendAreaType.LITEOLAP_TABLE) {
//                        newNeedSummaryConfigValue = reportModel.getExtendById(area.getReferenceAreaId()).getOtherSetting()
//                                        .get(MutilDimTableUtils.PERSONALITY_SUMMARY_CAPTION);
//                        oldneedSummaryConfigValue = reportModel.getExtendById(area.getReferenceAreaId()).getOtherSetting()
//                                .get(NEED_SUMMARY);
//                    } else {
//                        newNeedSummaryConfigValue = area.getOtherSetting().get(MutilDimTableUtils.PERSONALITY_SUMMARY_CAPTION);
//                        oldneedSummaryConfigValue = area.getOtherSetting().get(NEED_SUMMARY);
//                    }
                    questionModel.setNeedSummary(false);
                }
            }
            
        }
        questionModel.setUseIndex(true);
        putSliceConditionIntoParams (queryAction, questionModel);
        questionModel.setFilterBlank(queryAction.isFilterBlank());
        return questionModel;
    }

    /**
     * 如果之前存了了有配置信息，则直接取配置的值即可，若无相关配置，则走原来的判断逻辑
     * 
     * @param questionModel questionModel
     * @param oldNeedSummaryConfigValue 有关配置的值,旧配置，向下兼容
     * @param newNeedSummaryConfigValue 有关配置的值
     * @return 返回是否需要合计行标识
     */
    private static boolean needSummaryWhitConfigValue(QuestionModel questionModel,
            Object oldNeedSummaryConfigValue, Object newNeedSummaryConfigValue) {
        if (!StringUtils.isEmpty(newNeedSummaryConfigValue)
                || (oldNeedSummaryConfigValue != null 
                && "true".equals(oldNeedSummaryConfigValue.toString()))) {
            return true;
        } else if (oldNeedSummaryConfigValue != null && "false".equals(oldNeedSummaryConfigValue.toString())) {
            return false;
        } else {
            return needSummary(questionModel);
        }
    }
    /**
     * 判断是否需要汇总
     * @param questionModel
     * @return
     */
    private static boolean needSummary(QuestionModel questionModel) {
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        for (AxisMeta meta : questionModel.getAxisMetas().values()) {
            if (meta.getAxisType() == AxisType.ROW) {
                for (String str : meta.getCrossjoinDims()) {
                    DimensionCondition condition = (DimensionCondition) questionModel.getQueryConditions().get(str);
                    Dimension dim = configQuestionModel.getCube ().getDimensions ().get (condition.getMetaName ());
                    
                    if (dim != null && dim.getType () == DimensionType.CALLBACK ) {
                        if (condition.getQueryDataNodes () != null && condition.getQueryDataNodes ().size () == 1) {
                            List<Member> members = dim.getLevels ().values ().toArray (new Level[0])[0]
                                    .getMembers (
                                            configQuestionModel.getCube (), 
                                            configQuestionModel.getDataSourceInfo (), 
                                            configQuestionModel.getRequestParams ());
                            String uniqueName = condition.getQueryDataNodes ().get (0).getUniqueName ();
                            if (MetaNameUtil.isAllMemberUniqueName (uniqueName)) {
                                return false;
                            } else if (meta.getCrossjoinDims ().size () > 1) {
                                return false;
                            } else if (members.size () == 1) {
                                MiniCubeMember m = (MiniCubeMember) members.get (0);
                                if (m.getChildren () != null && m.getChildren ().size () > 1) {
                                    return false;
                                } else if (m.getChildren() == null ) {
                                    return false;
                                }
                            }
                        } else if (CollectionUtils.isEmpty (condition.getQueryDataNodes ())) {
                            return false;
                        }
                        return true;
                    }
                    if (condition.getQueryDataNodes() == null || condition.getQueryDataNodes().isEmpty()) {
                        return false;
                    } else {
                        List<QueryData> queryDatas = condition.getQueryDataNodes();
                        for (QueryData queryData : queryDatas) {
                            if (MetaNameUtil.isAllMemberName(queryData.getUniqueName())) {
                                return false;
                            } else {
                                // TODO 这里需要修改 需要修改为可配置方式
                                String[] tmp = MetaNameUtil.parseUnique2NameArray(queryData.getUniqueName());
                                if (tmp[tmp.length - 1].contains(":")) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
        return true;
    }
    
    /**
     * 
     * @param queryAction
     * @param questionModel
     */
    private static void putSliceConditionIntoParams(QueryAction queryAction, QuestionModel questionModel) {
        if (queryAction.getSlices () != null && !queryAction.getSlices ().isEmpty ()) {
            for (Map.Entry<Item, Object> entry : queryAction.getSlices ().entrySet ()) {
                String olapElementId = entry.getKey ().getOlapElementId ();
                Object value = entry.getValue ();
                if (value instanceof String[]) {
                    StringBuilder rs = new StringBuilder();
                    for (String str : (String[]) value) {
                        rs.append (str + ",");
                    }
                    questionModel.getRequestParams().put(olapElementId, rs.toString ());
                } else if (value != null){
                    questionModel.getRequestParams().put(olapElementId, value.toString ());
                }
            }
        }
    }
    
    /**
     * 通过查询
     * 
     * @param reportModel
     * @param area
     * @param queryAction
     * @return Map<AxisType, AxisMeta>
     */
    private static Map<AxisType, AxisMeta> buildAxisMeta(Schema schema,
        ExtendArea area, QueryAction queryAction) throws QueryModelBuildException {
        Map<Item, Object> columns = queryAction.getColumns();
        Map<AxisType, AxisMeta> rs = new HashMap<AxisType, AxisMeta>();
        AxisMeta columnMeta = buildAxisMeta(schema, area, columns, AxisType.COLUMN);
        rs.put(columnMeta.getAxisType(), columnMeta);
        
        Map<Item, Object> rows = queryAction.getRows();
        AxisMeta rowMeta = buildAxisMeta(schema, area, rows, AxisType.ROW);
        rs.put(rowMeta.getAxisType(), rowMeta);
        
        AxisMeta filterMeta = buildAxisMeta(schema, area, queryAction.getSlices(), AxisType.FILTER);
        rs.put(filterMeta.getAxisType(), filterMeta);
        return rs;
    }
    
    /**
     * 
     * @param schema
     * @param area
     * @param items
     * @param axisType
     * @return
     * @throws QueryModelBuildException
     */
    private static AxisMeta buildAxisMeta(Schema schema, ExtendArea area,
        Map<Item, Object> items, AxisType axisType) throws QueryModelBuildException {
        AxisMeta meta = new AxisMeta(axisType);
        for (Map.Entry<Item, Object> entry : items.entrySet()) {
            Item item = entry.getKey();
            OlapElement olapElement = 
                ReportDesignModelUtils.getDimOrIndDefineWithId(schema, area.getCubeId(), item.getOlapElementId());
            if (olapElement == null) {
                continue;
            }
            if (olapElement instanceof Dimension) {
                meta.getCrossjoinDims().add(olapElement.getName());
                // 针对列上的各个查询字段，保证其查询顺序，该字段仅供平面表查询使用
                if (AxisType.COLUMN.equals(axisType)) {
                    meta.getQueryItemsOrder().add("[Dimension].[" + olapElement.getName() + "]");
                }
            } else {
                meta.getQueryMeasures().add(olapElement.getName());
                // 针对列上的各个查询字段，保证其查询顺序，该字段仅供平面表查询使用
                if (AxisType.COLUMN.equals(axisType)) {
                    meta.getQueryItemsOrder().add("[Measure].[" + olapElement.getName() + "]");
                }
            }
        }
        return meta;
    }
    
    /**
     * 
     * @param reportModel
     * @param area
     * @return
     * @throws QueryModelBuildException
     */
    private static Cube getCubeFromReportModel(ReportDesignModel reportModel, ExtendArea area)
            throws QueryModelBuildException {
        String cubeId = area.getCubeId();
        if (StringUtils.isEmpty(cubeId)) {
            throw new QueryModelBuildException("cube id is empty");
        }
        Schema schema = reportModel.getSchema();
        if (schema == null) {
            throw new QueryModelBuildException("schema is not define");
        }
        Map<String, ? extends Cube> cubes = schema.getCubes();
        if (cubes == null) {
            throw new QueryModelBuildException("can not get cube define from schema : " + schema.getId());
        }
        Cube oriCube = cubes.get(area.getCubeId());
        if (oriCube == null) {
            throw new QueryModelBuildException("can not get cube define from schema : " + area.getCubeId());
        }
        return oriCube;
    }
    
    /**
     * 获取扩展区域包含的立方体定义
     * 
     * @param reportModel
     *            报表模型
     * @param area
     *            扩展区域
     * @return 立方体定义
     * @throws QueryModelBuildException
     */
    public static Cube getCubeWithExtendArea(ReportDesignModel reportModel, ExtendArea area)
        throws QueryModelBuildException {
        Cube oriCube = getCubeFromReportModel(reportModel, area);
        // 对于可选指标，默认将事实表包含的所有列作为查询条件
        if ("true".equals(area.getOtherSetting ().get (Constants.CAN_CHANGED_MEASURE))) {
            Cube rs = transformCube (oriCube);
            modifyMeasures(rs.getMeasures (), rs);
            return rs;
        }
        Map<String, List<Dimension>> filterDims = collectFilterDim(reportModel);
        MiniCube cube = new MiniCube(area.getCubeId());
        String areaId = area.getId();
        LogicModel logicModel = area.getLogicModel();
        if (area.getType() == ExtendAreaType.SELECTION_AREA
                || area.getType() == ExtendAreaType.LITEOLAP_CHART 
                || area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = 
                (LiteOlapExtendArea) reportModel.getExtendById(area.getReferenceAreaId());
            logicModel = liteOlapArea.getLogicModel();
            areaId = area.getReferenceAreaId();
        }
        if (logicModel == null) {
            throw new QueryModelBuildException("logic model is empty");
        }
        Item[] items = logicModel.getItems(area.getType() != ExtendAreaType.TABLE);
        Map<String, Dimension> dimensions = new HashMap<String, Dimension>();
        Map<String, Measure> measures = new HashMap<String, Measure>();
        
        for (Item item : items) {
            OlapElement olapElement = oriCube.getDimensions().get(item.getOlapElementId());
            if (olapElement == null) { // 维度不存在或者可能是指标信息
                olapElement = oriCube.getMeasures().get(item.getOlapElementId());
                if (olapElement != null) {
                    Measure measure = (Measure) olapElement;
                    measures.put(measure.getName(), measure);
                } 
            } else {
                MiniCubeDimension dim = (MiniCubeDimension) DeepcopyUtils.deepCopy(olapElement);
                dim.setLevels(Maps.newLinkedHashMap());;
                ((Dimension) olapElement).getLevels().values().forEach(level -> {
                    level.setDimension(dim);
                    dim.getLevels().put(level.getName(), level);
                });
                dimensions.put(dim.getName(), dim);
            }
        }
        if (area.getType() == ExtendAreaType.LITEOLAP || area.getType() == ExtendAreaType.LITEOLAP_CHART
                || area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            /**
             * TODO 把liteOlap中候选的维度和指标加入到items里面
             */
            Map<String, Item> candDims = Maps.newHashMap();
            if (area.getType() == ExtendAreaType.LITEOLAP) {
                candDims = ((LiteOlapExtendArea) area).getCandDims();
            } else {
                LiteOlapExtendArea liteOlapArea = 
                        (LiteOlapExtendArea) reportModel.getExtendById(area.getReferenceAreaId());
                candDims = liteOlapArea.getCandDims();
            }
            
            Schema schema = reportModel.getSchema();
            String cubeId = area.getCubeId();
            for (String elementId : candDims.keySet()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, elementId);
                if (element != null && !dimensions.containsKey(element.getName())) {
                    MiniCubeDimension dim = (MiniCubeDimension) DeepcopyUtils.deepCopy(element);
                    dim.setLevels(Maps.newLinkedHashMap());
                    ((Dimension) element).getLevels().values().forEach(level -> {
                        level.setDimension(dim);
                        dim.getLevels().put(level.getName(), level);
                    });
                    dimensions.put(element.getName(), (Dimension) element);
                }
            }
            Map<String, Item> candInds = Maps.newHashMap();
            if (area.getType() == ExtendAreaType.LITEOLAP) {
                candInds = ((LiteOlapExtendArea) area).getCandInds();
            } else {
                LiteOlapExtendArea liteOlapArea = 
                        (LiteOlapExtendArea) reportModel.getExtendById(area.getReferenceAreaId());
                candInds = liteOlapArea.getCandInds();
            }
            for (String elementId : candInds.keySet()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, elementId);
                if (element != null && !measures.containsKey(element.getName())) {
                    if (element instanceof CallbackMeasure) {
                        CallbackMeasure m = DeepcopyUtils.deepCopy((CallbackMeasure) element);
                        String url = ((CallbackMeasure) element).getCallbackUrl();
                        m.setCallbackUrl(HttpUrlUtils.getBaseUrl(url));
                        m.setCallbackParams(HttpUrlUtils.getParams(url));
                        measures.put(m.getName(), m);
                    } else {
                        measures.put(element.getName(), (Measure) element);
                    }
                }
            }
        }
        if (filterDims != null ) {
            List<Dimension> dims = filterDims.get(area.getCubeId());
            if (dims != null) {
                for(Dimension dim : dims) {
                    if (dim != null) {
                        dimensions.put(dim.getName(), dim);
                    }
                }
            }
            
            // TODO 处理不同cube共用同一查询条件情况
            filterDims.forEach((key, dimArray) -> {
                if (key != null && !key.equals(area.getCubeId())) {
                    dimArray.stream().filter(dim -> {
                        return dim instanceof TimeDimension;
                    }).forEach(dim -> {
                        for (Dimension tmp : oriCube.getDimensions().values()) {
                            if (dim.getName().equals(tmp.getName())) {
                                MiniCubeDimension tmpDim = (MiniCubeDimension) DeepcopyUtils.deepCopy(dim);
                                tmpDim.setLevels((LinkedHashMap<String, Level>) tmp.getLevels());
                                tmpDim.setFacttableColumn(tmp.getFacttableColumn());
                                tmpDim.setFacttableCaption(tmp.getFacttableCaption());
                                dimensions.put(tmpDim.getName(), tmpDim);
                            }
                        }
                    });
                }
            });
        }
        cube.setDimensions(dimensions);
        modifyMeasures(measures, oriCube);
        cube.setMeasures(measures);
        cube.setSource(((MiniCube) oriCube).getSource());
        cube.setPrimaryKey(((MiniCube) oriCube).getPrimaryKey());
        cube.setId(oriCube.getId() + "_" + areaId);
        return cube;
    }

    /**
     * 修正measure，将measure引用的measure放到cube中
     * @param measures
     * @param oriCube
     */
    private static void modifyMeasures(Map<String, Measure> measures, Cube oriCube) {
        Set<String> refMeasuers = Sets.newHashSet();
        measures.values().stream().filter(m -> {
            return m.getType() == MeasureType.CAL || m.getType() == MeasureType.RR || m.getType() == MeasureType.SR;
        }).forEach(m -> {
            ExtendMinicubeMeasure tmp = (ExtendMinicubeMeasure) m;
            if (m.getType() == MeasureType.CAL) {
                refMeasuers.addAll(PlaceHolderUtils.getPlaceHolderKeys(tmp.getFormula()));
            } else {
                final String refName = m.getName().substring(0, m.getName().length() - 3);
                refMeasuers.add(refName);
                if (m.getType() == MeasureType.RR) {
                    tmp.setFormula("rRate(${" + refName + "})");
                } else if (m.getType() == MeasureType.SR) {
                    tmp.setFormula("sRate(${" + refName + "})");
                }
            }
            tmp.setAggregator(Aggregator.CALCULATED);
        });
        refMeasuers.stream().filter(str -> {
            return !measures.containsKey(str);
        }).map(str -> {
            Set<Map.Entry<String, Measure>> entry = oriCube.getMeasures().entrySet();
            for (Map.Entry<String, Measure> tmp : entry) {
                if (str.equals(tmp.getValue().getName())) {
                    return tmp.getValue();
                }
            }
            return null;
        }).forEach(m -> {
            if (m != null) {
                measures.put(m.getName(), m);
            }
        });
    }

    /**
     * 
     * @param dim -- Dimension
     * @return Dimension
     */
    public static Dimension convertDim2Dim(Dimension dim) {
        StandardDimension rs = new StandardDimension(dim.getName());
        rs.setCaption(dim.getCaption());
        rs.setDescription(dim.getDescription());
        rs.setTableName(dim.getTableName());
        rs.setFacttableCaption(dim.getFacttableCaption());
        rs.setFacttableColumn(dim.getFacttableColumn());
        rs.setPrimaryKey(dim.getPrimaryKey());
        rs.setType(dim.getType());
        rs.setVisible(true);
        rs.setId(dim.getId());
        rs.setName(dim.getName());
        LinkedHashMap<String, Level> levels = Maps.newLinkedHashMap();
        dim.getLevels().values().forEach(level -> {
            level.setDimension(dim);
            levels.put(level.getName(), level);
        });
        rs.setLevels(levels);
        return rs;
    }

    /**
     * 
     * @param model
     * @return Map<String, List<Dimension>>
     */
    private static Map<String, List<Dimension>> collectFilterDim(ReportDesignModel model) {
        Map<String, List<Dimension>> rs = Maps.newHashMap();
        for (ExtendArea area : model.getExtendAreaList()) {
            if (isFilterArea(area.getType())) {
                Cube cube = model.getSchema().getCubes().get(area.getCubeId());
                if (rs.get(area.getCubeId()) == null) {
                    List<Dimension> dims = Lists.newArrayList();
                    area.listAllItems().values().forEach(key -> {
                        MiniCubeDimension dim = 
                            (MiniCubeDimension) DeepcopyUtils.deepCopy(cube.getDimensions().get(key.getId()));
                        dim.setLevels(Maps.newLinkedHashMap());
                        cube.getDimensions().get(key.getId()).getLevels().values().forEach(level ->{
                            dim.getLevels().put(level.getName(), level);
                        });
                        dims.add(dim);
                    });
                    rs.put(area.getCubeId(), dims);
                } else {
                    area.listAllItems().values().forEach(key -> {
                        MiniCubeDimension dim = 
                            (MiniCubeDimension) DeepcopyUtils.deepCopy(cube.getDimensions().get(key.getId()));
                        dim.setLevels(Maps.newLinkedHashMap());;
                        cube.getDimensions().get(key.getId()).getLevels().values().forEach(level ->{
                            dim.getLevels().put(level.getName(), level);
                        });
                        rs.get(area.getCubeId()).add(dim);
                    });
                }
            } 
        }
        return rs;
    }

    /**
     * 
     * @param type
     * @return boolean
     * 
     */
    public static boolean isFilterArea(ExtendAreaType type) {
        return type == ExtendAreaType.TIME_COMP 
                || type == ExtendAreaType.SELECT 
                || type == ExtendAreaType.MULTISELECT
                || type == ExtendAreaType.CASCADE_SELECT
                || type == ExtendAreaType.SINGLE_DROP_DOWN_TREE;
    }

    /**
     * trans cube
     * @param cube
     * @return new Cube
     */
    public static Cube transformCube(Cube cube) {
        MiniCube newCube = (MiniCube) DeepcopyUtils.deepCopy(cube);
        final Map<String, Measure> measures = Maps.newConcurrentMap();
        cube.getMeasures().values().forEach(m -> {
            measures.put(m.getName(), m);
        });
        newCube.setMeasures(measures);
        final Map<String, Dimension> dimensions = Maps.newLinkedHashMap();
        cube.getDimensions().values().forEach(dim -> {
            MiniCubeDimension tmp = (MiniCubeDimension) DeepcopyUtils.deepCopy(dim);
            LinkedHashMap<String, Level> tmpLevel = Maps.newLinkedHashMap();
            dim.getLevels().values().forEach(level -> {
                level.setDimension (dim);
                tmpLevel.put(level.getName(), level);
            });
            tmp.setLevels(tmpLevel);
            dimensions.put(tmp.getName(), tmp);
        });
        newCube.setDimensions(dimensions);
        return newCube;
    }

    /**
     * decorate chart with extend area
     * @param chart
     * @param area
     * @param index 
     */
    public static void decorateChart(DIReportChart chart, ExtendArea area, Schema schema, int index) {
        if (area.getType() == ExtendAreaType.CHART) {
            assert area.getLogicModel () != null : "当前区域未设置逻辑模型";
            // 设置topN默认设置
            if (area.getLogicModel().getTopSetting() != null) {
                MeasureTopSetting topSetting = area.getLogicModel().getTopSetting();
                chart.setRecordSize(topSetting.getRecordSize());
                chart.setTopedMeasureId(topSetting.getMeasureId());
                chart.setTopType(topSetting.getTopType().name());
                chart.setAreaId(area.getId());
            }
            FormatModel formatModel = area.getFormatModel ();
            if (formatModel != null && formatModel.getDataFormat () != null) {
                addDataFormatInfo(chart, formatModel.getDataFormat ());
                Map<String, String> colorFormat = formatModel.getColorFormat ();
                if (colorFormat != null && !colorFormat.isEmpty () && chart.getSeriesData () != null) {
                    for (SeriesDataUnit data : chart.getSeriesData ()) {
                        if (data == null) {
                            continue;
                        }
                        data.setColorDefine (colorFormat.get (data.getyAxisName ()));
                    }
                }
                Map<String, String> positions = formatModel.getPositions ();
                if (colorFormat != null && !positions.isEmpty () && chart.getSeriesData () != null) {
                    for (SeriesDataUnit data : chart.getSeriesData ()) {
                        if (data == null) {
                            continue;
                        }
                        data.setPosition (positions.get (data.getyAxisName ()));
                    }
                }
            }
            chart.getRender ().setAxisCaption (area.getChartFormatModel ().getSetting ().getAxisCaption ());
            chart.setAppearance (area.getChartFormatModel ().getAppearance ().getLegend ());
            final Map<String, String> dimMap = Maps.newConcurrentMap();
            String[] allDims = area.getLogicModel().getSelectionDims().values().stream().map(item -> {
                OlapElement tmp = getOlapElement(area, schema, item);
                if (tmp != null) {
                    dimMap.put(tmp.getId(), tmp.getName());
                    return tmp.getCaption();
                } else {
                    return null;
                }
            }).filter(x -> x != null).toArray(String[] :: new);
            chart.setDimMap(dimMap);
            chart.setAllDims(allDims);
            String[] allMeasures = area.getLogicModel().getSelectionMeasures().values().stream().map(item -> {
                OlapElement tmp = getOlapElement(area, schema, item);
                if (tmp != null) {
                    chart.getMeasureMap().put(tmp.getId(), tmp.getCaption());
                    return tmp.getCaption();
                } else {
                    return null;
                }
            }).filter(x -> x != null).toArray(String[] :: new);
            chart.setAllMeasures(allMeasures);
            
            final Item[] columns = area.getLogicModel().getColumns();
            List<String> tmp = getOlapElementNames(columns, area.getCubeId(), schema);
            if (tmp.size() > 0) {
                chart.setDefaultMeasures(tmp.toArray(new String[0]));
            }
            for (int i = 0; i < columns.length; ++i) {
                chart.getMeasureMap().put(columns[i].getOlapElementId(), tmp.get(i));
            }
            if (index >= 0 && index < chart.getAllMeasures().length) {
                chart.setDefaultMeasures(new String[]{ chart.getAllMeasures()[index] });
            } 
        } else if (area.getType() == ExtendAreaType.LITEOLAP_CHART) {
            addDataFormatInfo (chart, area.getFormatModel ().getDataFormat ());
        }
    }

    private static void addDataFormatInfo(DIReportChart chart, Map<String, String> dataFormat) {
        if (chart.getSeriesData () == null || chart.getSeriesData ().isEmpty ()) {
            return;
        }
        for (SeriesDataUnit seriesData : chart.getSeriesData ()) {
            if (seriesData == null) {
                continue;
            }
            String format = dataFormat.get (seriesData.getyAxisName ());
            if (StringUtils.isEmpty (format)) {
                format = dataFormat.get ("defaultFormat");
            }
            seriesData.setFormat (format);
        }
    }

    /**
     * @param area
     * @param schema
     * @return
     */
    private static List<String> getOlapElementNames(Item[] items, String cubeId, Schema schema) {
        List<String> tmp = Lists.newArrayList();
        if (items == null || items.length == 0) {
            return tmp;
        }
        for (Item item : items) {
            OlapElement olapElement = 
                    ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, item.getOlapElementId());
            tmp.add(olapElement.getCaption());
        }
        return tmp;
    }

    /**
     * 
     * @param area
     * @param schema
     * @param item
     * @return String
     * 
     */
    private static OlapElement getOlapElement(ExtendArea area, Schema schema, Item item) {
        OlapElement olapElement = 
                ReportDesignModelUtils.getDimOrIndDefineWithId(schema, area.getCubeId(), item.getOlapElementId());
        if (olapElement != null) {
            return olapElement;
        }
        return null;
    }

    /**
     * 修正报表区域模型参数
     * @param request
     * @param model
     */
    public static Map<String, Object> resetContextParam(final HttpServletRequest request, ReportDesignModel model) {
        Map<String, Object> rs = Maps.newHashMap();
        
        LOG.info ("context params ============== " + ContextManager.getParams ());
        // 当前请求参数
        Map<String, String> requestParams = collectRequestParams(request);
        rs.putAll(requestParams);
        LOG.info ("current request params ============== " + requestParams);
        //处理报表参数
        Collection<ReportParam> params = DeepcopyUtils.deepCopy(model.getParams()).values();
        // modify by jiangyichao at 2015-05-19
        Collection<PlaneTableCondition> planeTableConditions = 
            DeepcopyUtils.deepCopy(model.getPlaneTableConditions()).values();
        if (params.size() == 0 && planeTableConditions.size() == 0) {
            return rs;
        }        
        
        // TODO 先处理P功能对应的参数
        if (params.size() != 0) {
            params.forEach(param -> {
                LOG.info ("current param define ============== " + param.toString());
                if (param.isNeeded() && StringUtils.isEmpty(requestParams.get(param.getName()))) {
                    if (StringUtils.isEmpty(param.getDefaultValue())) {
                        throw new RuntimeException("必要参数未赋值");
                    }
                    rs.put(param.getElementId(), param.getDefaultValue());
                    rs.put(param.getName(), param.getDefaultValue());
                } else if (!StringUtils.isEmpty(requestParams.get(param.getName()))) {
                    rs.put(param.getElementId(), requestParams.get(param.getName()));
                } else if (!StringUtils.isEmpty(param.getDefaultValue())) {
                    rs.put(param.getElementId(), param.getDefaultValue());
                    rs.put(param.getName(), param.getDefaultValue());
                }
            });            
        }
        
        // 处理平面表对应的条件
        if (planeTableConditions.size() != 0) {
            planeTableConditions.forEach(condition -> {
                LOG.info ("current planeTable condition define ============== " + condition.toString());
                // 如果请求参数中没有条件对应的参数，则使用默认值对应的参数值
                if (StringUtils.isEmpty(requestParams.get(condition.getName()))) {                   
                    rs.put(condition.getElementId(), condition.getDefaultValue());
                    rs.put(condition.getName(), condition.getDefaultValue());
                } else if (!StringUtils.isEmpty(requestParams.get(condition.getName()))) {
                    // 如果请求参数中有条件对应的参数，则取请求参数中的参数值
                    // TODO 对时间条件和层级条件做特殊处理
                    String requestParam = requestParams.get(condition.getName());
                    // 处理时间条件
//                    requestParam = PlaneTableUtils.handleTimeCondition(requestParam);
                    // 处理层级条件
//                    requestParam = PlaneTableUtils.handleLayerCondition(requestParam);
                    rs.put(condition.getElementId(), requestParam);
                } 
            }); 
        }
        
        LOG.info ("after reset params is : " + rs);
        return rs;
    }

    /**
     * 
     * @param params
     * @param request
     * @return Map<String, String>
     */
    private static Map<String, String> collectRequestParams(HttpServletRequest request) {
        Map<String, String> rs = Maps.newHashMap();
        request.getParameterMap().forEach((k, v) -> {
            rs.put(k, v[0]);
        }); 
        // cookie中如果包含参数值，覆盖url中参数
        if (request.getCookies() != null)  {
            for (Cookie cookie : request.getCookies()) {
                rs.put(cookie.getName(), cookie.getValue());
            }
        }
        
        // 如果当前线程中包含参数值，则覆盖cookie中参数值
        rs.putAll(ContextManager.getParams());
        // 容错，处理其他可能的参数
        rs.remove(Constants.RANDOMCODEKEY);
        rs.remove(Constants.TOKEN);
        rs.remove(Constants.BIPLATFORM_PRODUCTLINE);

        return rs;
    }

    /**
     * TODO:
     * @param members
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> getMembersWithChildrenValue(List<Member> members,
        Cube cube, DataSourceInfo dataSource, Map<String, String> params) {
        List<Map<String, String>> rs = Lists.newArrayList();
        if (members == null || members.isEmpty()) {
            return rs;
        }
        members.forEach(m -> {
            Map<String, String> tmp = Maps.newHashMap();
            tmp.put("value", m.getUniqueName());
            tmp.put("text", m.getCaption());
            Member parent = m.getParentMember(cube, dataSource, params);
            if (parent != null) {
                tmp.put("parent", parent.getUniqueName());
            }
            rs.add(tmp);
            List<Member> children = m.getChildMembers(cube, dataSource, params);
            if (children != null) {
                rs.addAll(getMembersWithChildrenValue(children, cube, dataSource, params));
            }
        });
        
        return rs;
    }
    

}
