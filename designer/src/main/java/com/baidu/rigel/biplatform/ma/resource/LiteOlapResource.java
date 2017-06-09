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
package com.baidu.rigel.biplatform.ma.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.builder.Director;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.CubeMetaBuildService;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.AnalysisChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.ChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryDataUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.LiteOlapViewUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.IndCandicateForChart;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.MetaData;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.MetaStatusData;
import com.baidu.rigel.biplatform.ma.resource.view.vo.DimensionMemberViewObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 此接口提供liteolap特有的一些数据返回接口和数据查询服务
 * 
 * @author zhongyi
 * 
 *         2014-7-30
 */
@RestController
@RequestMapping("/silkroad/reports")
public class LiteOlapResource extends BaseResource {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(LiteOlapResource.class);

    /**
     * reportModelCacheManager
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager;

    /**
     * cubeMetaBuildService
     */
    @Resource
    private CubeMetaBuildService cubeBuildService;

    /**
     * starModelBuildService
     */
    @Resource
    private StarModelBuildService starModelBuildService;

    /**
     * queryBuildService
     */
    @Resource
    private QueryBuildService queryBuildService;

    /**
     * analysisChartBuildService
     */
    @Resource
    private AnalysisChartBuildService analysisChartBuildService;

    /**
     * 报表数据查询服务
     */
    @Resource
    private ReportModelQueryService reportModelQueryService;

    /**
     * chartBuildService
     */
    @Resource
    private ChartBuildService chartBuildService;

    /**
     * director
     */
    @Resource
    private Director director;

    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;

    /**
     * 维度轴标识
     */
    private static final String ROW = "ROW";

    /**
     * 指标轴标识
     */
    private static final String COLUMN = "COLUMN";

    /**
     * 条件轴标识
     */
    private static final String FILTER = "FILTER";

    /**
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */

    /**
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/item", method = { RequestMethod.POST })
    public ResponseResult dragAndDrop(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {

        String from = request.getParameter("from");
        String to = request.getParameter("to");
        int toPosition = Integer.valueOf(request.getParameter("toPosition"));
        String targetName = request.getParameter("uniqNameList");

        if (StringUtils.isEmpty(from) && StringUtils.isEmpty(to)) {
            return ResourceUtils.getCorrectResult("OK", "");
        }
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getRuntimeModel(reportId).getModel();
            // reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("没有运行时的报表实例！报表ID：" + reportId, 1);
        }
        if (model == null) {
            return ResourceUtils.getErrorResult("没有运行时的报表实例！报表ID：" + reportId, 1);
        }
        ExtendArea sourceArea = model.getExtendById(areaId);
        if (sourceArea.getType() != ExtendAreaType.SELECTION_AREA) {
            logger.error("Drag Operation is Not supported for type of non-SELECTION_AREA !");
            return ResourceUtils.getErrorResult("Drag Operation is Not supported for type of non-SELECTION_AREA !", 1);
        }
        /**
         * 
         */
        ExtendArea parent = model.getExtendById(sourceArea.getReferenceAreaId());
        if (parent == null || parent.getType() != ExtendAreaType.LITEOLAP) {
            logger.error("Drag Operation is Not supported for type of non-LITEOLAP !");
            return ResourceUtils.getErrorResult("Drag Operation is Not supported for type of non-LITEOLAP !", 1);
        }
        LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) parent;
        LogicModel logicModel = liteOlapArea.getLogicModel();
        Item targetItem = null;
        // // TODO yichao.jiang DirtySolution，后续必须进行修改
        // // 首先校验是否为callback维度或者时间维度，这两种维度不允许进行拖拽
        // boolean isTimeOrCalback = this.isTimeOrCallbackDim(targetName, model);
        // if (isTimeOrCalback) {
        // ResponseResult rs = ResourceUtils.getCorrectResult("OK", "");
        // return rs;
        // }
        // 校验是否允许拖拽，此处是对维度只能拖动到维度和条件轴上
        // 指标只能拖动到指标轴上
        boolean allowDrag = this.preCheck4DragValidate(from, to, targetName, model);
        if (!allowDrag) {
            ResponseResult rs = ResourceUtils.getCorrectResult("OK", "");
            return rs;
        }

        if (StringUtils.isEmpty(from)) {
            if (liteOlapArea.getCandDims().containsKey(targetName)) {
                /**
                 * 移动候选维度
                 */
                targetItem = liteOlapArea.getCandDims().get(targetName);
            } else if (liteOlapArea.getCandInds().containsKey(targetName)) {
                /**
                 * 移动候选指标
                 */
                targetItem = liteOlapArea.getCandInds().get(targetName);
            }
        } else {
            switch (from) {
                case ROW:
                    targetItem = logicModel.removeRow(targetName);
                    break;
                case COLUMN:
                    targetItem = logicModel.removeColumn(targetName);
                    break;
                case FILTER:
                    targetItem = logicModel.removeSlice(targetName);
                    break;
                default:
                    return ResourceUtils.getErrorResult("不认识的位置！From: " + from, 1);
            }
        }
        /**
         * 
         */
        if (model == null || liteOlapArea == null || targetItem == null) {
            throw new RuntimeException("未找到指定的维度或指标信息 : model - [" + model + "] area - [" + liteOlapArea + "] item - ["
                    + targetItem + "]");
        }

        OlapElement element =
                ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), liteOlapArea.getCubeId(),
                        targetItem.getOlapElementId());
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        if (!StringUtils.hasText(to)) {
            // // 如果from不为空，to为空，则表示要将条件拖走，则需要校验该条件是否为必须
            // boolean isNeed = this.checkIsNeed(targetName, model);
            // if (isNeed) {
            // // 如果必须，则将其放置到条件轴上
            // targetItem.setPositionType(PositionType.S);
            // logicModel.addSlice(targetItem, 0);
            // } else {
            /**
             * 根据item的类型加入到候选维度或者候选指标中
             */
            if (element instanceof Dimension) {
                liteOlapArea.getCandDims().put(element.getId(), targetItem);
            } else if (element instanceof Measure) {
                liteOlapArea.getCandInds().put(element.getId(), targetItem);
            }

            // }
        } else {
            // TODO 后续考虑优化
            if (element instanceof Dimension) {
                MiniCubeDimension dimension = (MiniCubeDimension) element;
                Level level = dimension.getLevels().values().toArray(new Level[0])[0];
                // 如果拖动的是岗位条件，则将其放置到第一个位置
                if (level.getType() == LevelType.CALL_BACK) {
                    toPosition = 0;
                } else {
                    // TODO 如果拖动的是其他维度，则需要保证岗位在第一个位置
                    // 如果目标轴是横轴，需要保证岗位在第一个位置
                    if (ROW.equals(to)) {
                        // 横轴上至少有一个维度，所以无需判定非空
                        Item[] items = logicModel.getRows();
                        // 如果维度轴上的第一个位置是岗位
                        if (this.isTimeOrCallbackDim(items[0].getId(), model)) {
                            toPosition = toPosition + 1;
                        }
                    }
                }
            }
            switch (to) {
                case ROW:
                    targetItem.setPositionType(PositionType.X);
                    logicModel.addRow(targetItem, toPosition);
                    break;
                case COLUMN:
                    if (element instanceof Measure) {
                        targetItem.setPositionType(PositionType.Y);
                        logicModel.addColumn(targetItem, toPosition);
                    }
                    break;
                case FILTER:
                    targetItem.setPositionType(PositionType.S);
                    logicModel.addSlice(targetItem, toPosition);
                    break;
                default:
                    return ResourceUtils.getErrorResult("不认识的位置！To: " + to, 1);
            }
        }
        // reportModelCacheManager.updateReportModelToCache(reportId, model);
        ExtendAreaContext extendContext =
                reportModelCacheManager.getAreaContext(reportId, liteOlapArea.getTableAreaId());
        // 获取对应项的elementId
        String olapElementId = targetItem.getOlapElementId();
        // 仅在维度轴上拖拽，此时需要清理图形上的面包屑和uniqueName信息，无需清理候选区域上的参数
        if ((!StringUtils.isEmpty(to) && !StringUtils.isEmpty(from))
                && ((ROW.equals(to) && to.equals(from)) || (ROW.equals(from) && FILTER.equals(to)))) {
            String liteOlapTableId = liteOlapArea.getTableAreaId();
            // 如果面包屑上对应的维度和本次移动的维度不相等，则需要从面包屑中找到对应的维度，将其条件清除
            String uniqueName = (String) extendContext.getParams().get("uniqueName");
            String dimElementId = "";
            if (!StringUtils.isEmpty(uniqueName)) {
                // 如果有@符号，先替换掉
                if (uniqueName.startsWith("@")) {
                    uniqueName = uniqueName.substring(1, uniqueName.length() - 1);
                }
                String dimName = "";
                // 先处理uniqueName为数组的情况
                if (uniqueName.contains(",")) {
                    String[] uniqueNameArray = uniqueName.split(",");
                    dimName = MetaNameUtil.getDimNameFromUniqueName(uniqueNameArray[0]);
                } else {
                    // 根据uniqueName获取维度名称
                    dimName = MetaNameUtil.getDimNameFromUniqueName(uniqueName);
                }
                // 定义一个变量，方便在java 8中使用
                final String finalDimName = dimName;
                // 从cube中读取对应的维度信息，进而获取dimId
                Cube cube = model.getSchema().getCubes().get(sourceArea.getCubeId());
                if (cube.getDimensions() != null && cube.getDimensions().size() != 0) {
                    Object[] tmp = cube.getDimensions().values().stream().filter(dim -> dim != null).filter(dim -> {
                        return finalDimName.equals(dim.getName());
                    }).toArray();
                    if (tmp != null && tmp.length == 1) {
                        dimElementId = ((OlapElement) tmp[0]).getId();
                    }
                }
            }
            // 移除对应的参数
            if (dimElementId != null) {
                runTimeModel.getLocalContextByAreaId(liteOlapTableId).getParams().remove(dimElementId);
                extendContext.getParams().remove(dimElementId);
            }
            // 清除表上同面包屑、参数有关的信息
            runTimeModel.getLocalContextByAreaId(liteOlapTableId).getParams().remove(olapElementId);
            runTimeModel.getLocalContextByAreaId(liteOlapTableId).getParams().remove("uniqueName");
            runTimeModel.getLocalContextByAreaId(liteOlapTableId).getParams().remove("bread_key");

            // 清除图形上同面包屑、参数有关的信息
            String liteOlapChartId = liteOlapArea.getChartAreaId();
            runTimeModel.getLocalContextByAreaId(liteOlapChartId).getParams().remove(olapElementId);
            runTimeModel.getLocalContextByAreaId(liteOlapChartId).getParams().remove("uniqueName");
            runTimeModel.getLocalContextByAreaId(liteOlapChartId).getParams().remove("bread_key");
            // 清除参数信息
            extendContext.getParams().remove(olapElementId);
            extendContext.getParams().remove("uniqueName");
            extendContext.getParams().remove("bread_key");
        }
        // 如果from不为空，但是to为空，则需要根据情况将该项对应的条件从lite-olap选择区域清除，同时需要清除面包屑
        if (StringUtils.isEmpty(to) && !StringUtils.isEmpty(from)) {
            // step1. 清除局部extendAreaContext
            extendContext.getParams().remove(olapElementId);
            extendContext.getParams().remove("uniqueName");
            extendContext.getParams().remove("bread_key");
            // step2. 清除selection-area中的参数
            runTimeModel.getLocalContextByAreaId(liteOlapArea.getSelectionAreaId()).getParams().remove(olapElementId);
            runTimeModel.getLocalContextByAreaId(liteOlapArea.getSelectionAreaId()).getParams().remove("uniqueName");
            runTimeModel.getLocalContextByAreaId(liteOlapArea.getSelectionAreaId()).getParams().remove("bread_key");

            // step3. 从表区域中删除
            runTimeModel.getLocalContextByAreaId(liteOlapArea.getTableAreaId()).getParams().remove(element.getId());
            // step4. 从图区域中删除
            runTimeModel.getLocalContextByAreaId(liteOlapArea.getChartAreaId()).getParams().remove(element.getId());
            // step5. 获取该id对应的参数维度p中的参数名称
            String paramName = this.getParamDimension(element.getId(), model);
            // 如果参数名称不为空，则同样将其移除
            if (StringUtils.hasText(paramName)) {
                extendContext.getParams().remove(paramName);
                runTimeModel.getLocalContextByAreaId(areaId).getParams().remove(paramName);
                runTimeModel.getLocalContextByAreaId(liteOlapArea.getTableAreaId()).getParams().remove(paramName);
                runTimeModel.getLocalContextByAreaId(liteOlapArea.getChartAreaId()).getParams().remove(paramName);
            }
        }
        // 统一清理面包屑问题
        extendContext.setCurBreadCrumPath(null);
        // 更新context
        reportModelCacheManager.updateAreaContext(reportId, liteOlapArea.getTableAreaId(), extendContext);
        // if (extendContext.getCurBreadCrumPath() != null) {
        // List<Map<String, String>> breadPath = extendContext.getCurBreadCrumPath();
        // String uniqueName = breadPath.get(breadPath.size() - 1).get("uniqName");
        // if (uniqueName.startsWith("@")) {
        // uniqueName = uniqueName.substring(1, uniqueName.length() - 1);
        // }
        // Cube cube = model.getSchema().getCubes().get(sourceArea.getCubeId());
        // Dimension dim = cube.getDimensions().get(logicModel.getRows()[0].getId());
        // if (dim != null && !dim.getName().equals(MetaNameUtil.getDimNameFromUniqueName(uniqueName))) {
        // runTimeModel.getLocalContext().clear();
        // reportModelCacheManager.updateAreaContext(reportId,
        // liteOlapArea.getTableAreaId(), new ExtendAreaContext());
        //
        // }
        // }
        // // 移除或移入维度轴清理面包屑
        // if ((StringUtils.isEmpty(to) && ROW.equals(from))
        // || (StringUtils.isEmpty(from) && ROW.equals(to))) {
        // // runTimeModel.getLocalContext().clear();
        // runTimeModel.getLocalContext().remove("bread_key");
        // reportModelCacheManager.updateAreaContext(reportId,
        // liteOlapArea.getTableAreaId(), new ExtendAreaContext());
        // }
        runTimeModel.setLinkedQueryAction(null);
        runTimeModel.getDatas().clear();
        // runTimeModel.setModel (model);
        runTimeModel.updateDimStores(model);
        // 拖拽完之后，没必要再保留下钻以及展开的相关参数：lineUniqueName和uniqueName了，应该从全局参数中清除  update by majun
        runTimeModel.getContext().removeParam("lineUniqueName");
        runTimeModel.getContext().removeParam("uniqueName");
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        ResponseResult rs = ResourceUtils.getCorrectResult("OK", "");
        return rs;
    }

    /**
     * 判断某个维度是否设置了参数维度
     * 
     * @param elementId 维度或者指标id
     * @param model 报表模型
     * @return 返回该维度对应的参数名称
     */
    private String getParamDimension(String elementId, ReportDesignModel model) {
        Map<String, ReportParam> reportParams = model.getParams();
        // 对于必须的参数
        if (reportParams != null && reportParams.size() != 0) {
            for (String key : reportParams.keySet()) {
                ReportParam param = reportParams.get(key);
                if (param.getElementId().equals(elementId)) {
                    return param.getName();
                }
            }
        }
        return null;
    }

    /**
     * 校验是否为时间维度或者callback维度
     * 
     * @param elemengId
     * @param model
     * @return
     */
    private boolean isTimeOrCallbackDim(String elementId, ReportDesignModel model) {
        OlapElement element = this.getOlapElementAccordingName(model, elementId);
        if (element != null && element instanceof Dimension) {
            Dimension dim = (Dimension) element;
            if (dim.isTimeDimension()) {
                return true;
            } else {
                Level level = dim.getLevels().values().toArray(new Level[0])[0];
                return level.getType() == LevelType.CALL_BACK;
            }
        }
        return false;
    }

    /**
     * 在拖拽前校验是否可以拖拽 校验规则：1.维度仅能拖动到维度轴或者条件轴上；2.指标仅能拖动到指标轴
     * 
     * @param to
     * @param elementId
     * @param model
     * @return true表示可以拖拽；false表示不可以
     */
    private boolean preCheck4DragValidate(String from, String to, String elementId, ReportDesignModel model) {
        // 1.检查是否存在非法拖拽情况
        OlapElement element = this.getOlapElementAccordingName(model, elementId);
        // 对于指标，不允许移动到维度轴或者条件轴
        if (element instanceof Measure && (ROW.equals(to) || FILTER.equals(to))) {
            return false;
        }
        // 对于维度，不允许移动到指标轴
        if (element instanceof Dimension && COLUMN.equals(to)) {
            return false;
        }
        return true;
    }

    /**
     * 根据targetName获取维度或者指标，在这里targetName实际为elementId
     * 
     * @param model
     * @param targetName
     * @return
     */
    private OlapElement getOlapElementAccordingName(ReportDesignModel model, String targetName) {
        if (!StringUtils.isEmpty(targetName) && model.getSchema() != null && model.getSchema().getCubes().size() != 0
                && !CollectionUtils.isEmpty(model.getSchema().getCubes().values())) {
            MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
            OlapElement element =
                    ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), cube.getId(), targetName);
            return element;
        }
        return null;
    }

    /**
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/config", method = { RequestMethod.POST })
    public ResponseResult getConfigOfArea(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {

        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getRuntimeModel(reportId).getModel();
        } catch (CacheOperationException e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("没有运行时的报表实例！报表ID：" + reportId, 1);
        }
        ExtendArea target = model.getExtendById(areaId);
        if (target.getType() != ExtendAreaType.SELECTION_AREA) {
            logger.debug("not support for getting config of non-SELECTION area! ");
            return ResourceUtils.getCorrectResult("OK", "");
        }
        ExtendArea parent = model.getExtendById(target.getReferenceAreaId());
        if (parent.getType() != ExtendAreaType.LITEOLAP) {
            logger.error("Get Config Operation is Not supported for type of non-LITEOLAP !");
            return ResourceUtils.getErrorResult("Drag Operation is Not supported for type of non-LITEOLAP !", 1);
        }
        LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) parent;
        MetaData metaData = LiteOlapViewUtils.parseMetaData(liteOlapArea, model.getSchema());
        MetaStatusData metaStatusData = LiteOlapViewUtils.parseMetaStatusData(liteOlapArea, model.getSchema());
        Map<String, Object> selected = LiteOlapViewUtils.parseSelectedItemMap(liteOlapArea, model.getSchema());
        /*
         * wrap the result
         */
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("index4Selected", new String[] { "COLUMN", "ROW", "FILTER" });
        resultMap.put("metaData", metaData);
        resultMap.put("metaStatusData", metaStatusData);
        resultMap.put("selected", selected);
        return ResourceUtils.getCorrectResult("OK", resultMap);
    }

    /**
     * 获取当前图上可选的指标
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/ind_for_chart", method = { RequestMethod.POST })
    public ResponseResult getIndsForChart(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {

        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getRuntimeModel(reportId).getModel();
            // reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("没有运行时的报表实例！报表ID：" + reportId, 1);
        }
        ExtendArea target = model.getExtendById(areaId);
        if (target.getType() != ExtendAreaType.LITEOLAP_CHART) {
            logger.debug("not support for getting config of non-SELECTION area! ");
            return ResourceUtils.getCorrectResult("OK", "");
        }
        ExtendArea parent = model.getExtendById(target.getReferenceAreaId());
        if (parent.getType() != ExtendAreaType.LITEOLAP) {
            logger.error("Get Config Operation is Not supported for type of non-LITEOLAP !");
            return ResourceUtils.getErrorResult("Drag Operation is Not supported for type of non-LITEOLAP !", 1);
        }
        LogicModel logicModel = parent.getLogicModel();
        Map<String, Object> resultMap = Maps.newHashMap();
        List<IndCandicateForChart> inds = Lists.newArrayList();
        for (Item item : logicModel.getColumns()) {
            OlapElement element =
                    ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), item.getCubeId(),
                            item.getOlapElementId());
            IndCandicateForChart indForChart = LiteOlapViewUtils.parseIndForChart(element);
            inds.add(indForChart);
        }
        resultMap.put("currentInds", new String[0]);
        resultMap.put("inds", inds.toArray(new IndCandicateForChart[0]));
        return ResourceUtils.getCorrectResult("OK", resultMap);
    }

    /**
     * 获取维度成员
     * 
     * @return
     */
    @RequestMapping(value = "/runtime/extend_area/{areaId}/dims/{dimId}/members", method = { RequestMethod.POST })
    public ResponseResult queryMembers(@PathVariable("areaId") String areaId, @PathVariable("dimId") String dimId,
            HttpServletRequest request) throws Exception {
        long begin = System.currentTimeMillis();
        logger.info("[INFO] begin query member operation");
        String reportId = request.getParameter("reportId");
        if (StringUtils.isEmpty(reportId)) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(1);
            rs.setStatusInfo("reportId为空，请检查输入");
            return rs;
        }
        ReportDesignModel model = null;
        try {
            model = this.getDesignModelFromRuntimeModel(reportId);
            // reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error(e.getMessage(), e);
            ResponseResult rs = ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
            return rs;
        }
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.error(e.getMessage(), e);
            ResponseResult rs = ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
            return rs;
        }
        if (model == null) {
            ResponseResult rs = ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
            return rs;
        }

        ExtendArea area = model.getExtendById(areaId);
        String cubeId = area.getCubeId();
        Cube cube = model.getSchema().getCubes().get(cubeId);
        Dimension dim = cube.getDimensions().get(dimId);
        /**
         * 通过全局的上下文作为members的参数
         */
        QueryContext queryContext = runTimeModel.getContext();
        Map<String, String> params = Maps.newHashMap();
        for (String key : queryContext.getParams().keySet()) {
            Object value = queryContext.get(key);
            if (value != null) {
                params.put(key, value.toString());
            }
        }
        cube = QueryUtils.getCubeWithExtendArea(model, area);
        ((MiniCube) cube).setSchema(model.getSchema());
        final Dimension newDim = QueryUtils.convertDim2Dim(dim);
        if (params.containsKey(Constants.ORG_NAME) || params.containsKey(Constants.APP_NAME)) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(0);
            rs.setStatusInfo("OK");
            return rs;
        }

        List<List<Member>> members = Lists.newArrayList();
        // TODO 临时展现3级，后续修改此接口 yichao.jiang
        if (QueryDataUtils.isCallbackDim(newDim)) {
            // members = reportModelQueryService.getMembers(cube, newDim, params, securityKey);
            // List<List<Member>> tmpMembers = Lists.newArrayList();
            long callbackBegin = System.currentTimeMillis();
            members = this.handleCallbackLevel4LiteOlapShow(cube, newDim, params, 3);
            // this.handleCallbackLevel4LiteOlapShow(tmpMembers, model, cube, newDim, params, members, 1);
            logger.info("[INFO]query members for lite-olap magnifier cost :"
                    + (System.currentTimeMillis() - callbackBegin) + "ms");
            // if (!CollectionUtils.isEmpty(tmpMembers)) {
            // members.addAll(tmpMembers);
            // }
        } else {
            members = reportModelQueryService.getMembers(cube, newDim, params, securityKey);
        }
        QueryContext context = runTimeModel.getLocalContextByAreaId(area.getId());
        List<DimensionMemberViewObject> datas = Lists.newArrayList();
        final AtomicInteger i = new AtomicInteger(1);
        members.forEach(tmpMembers -> {
            DimensionMemberViewObject viewObject = new DimensionMemberViewObject();
            String caption = "第" + i.getAndAdd(1) + "级";
            viewObject.setCaption(caption);
            String name = "[" + newDim.getName() + "]";
            name += ".[All_" + newDim.getName() + "s]";
            viewObject.setName(name);
            viewObject.setNeedLimit(false);
            viewObject.setSelected(i.get() == 2);
            viewObject.setChildren(genChildren(newDim, tmpMembers, context));
            datas.add(viewObject);
        });
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        Map<String, List<DimensionMemberViewObject>> dimValue = Maps.newHashMap();
        dimValue.put("dimValue", datas);
        rs.setData(dimValue);
        logger.info("[INFO] query member operation successfull, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    /**
     * 更新维度成员
     * 
     * @return ResponseResult
     */
    @RequestMapping(value = "/runtime/extend_area/{areaId}/dims/{dimId}/members/1", method = { RequestMethod.POST })
    public ResponseResult updateMembers(@PathVariable("areaId") String areaId, @PathVariable("dimId") String dimId,
            HttpServletRequest request) throws Exception {
        String reportId = request.getParameter("reportId");
        if (StringUtils.isEmpty(reportId)) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(1);
            rs.setStatusInfo("reportId为空，请检查输入");
            return rs;
        }
        ReportRuntimeModel model = null;
        try {
            model = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ResponseResult rs = ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
            return rs;
        }
        ReportDesignModel designModel = this.getDesignModelFromRuntimeModel(reportId);
        // reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = designModel.getExtendById(areaId);
        Cube cube = designModel.getSchema().getCubes().get(area.getCubeId());

        String[] selectedDims = request.getParameterValues("selectedNodes");
        updateLocalContext(dimId, cube, model, selectedDims, areaId);
        if (area.getType() == ExtendAreaType.SELECTION_AREA) {
            // 针对lite-olap，无需将参数放入到图、表对应的参数中，因为在queryArea中会自动将
            // selection_area中的参数塞入到图、表区域中
            areaId = area.getReferenceAreaId();
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) designModel.getExtendById(areaId);
            // String chartAreaId = liteOlapArea.getChartAreaId();
            String tableAreaId = liteOlapArea.getTableAreaId();
            // 清除表格上的lite-olap面包屑
            ExtendAreaContext context = this.reportModelCacheManager.getAreaContext(reportId, tableAreaId);
            context.setCurBreadCrumPath(null);
            reportModelCacheManager.updateAreaContext(reportId, tableAreaId, context);
            // updateLocalContext(dimId, cube, model, selectedDims, chartAreaId);
            // updateLocalContext(dimId, cube, model, selectedDims, tableAreaId);
        }

        this.reportModelCacheManager.updateRunTimeModelToCache(reportId, model);
        ResponseResult rs = ResourceUtils.getCorrectResult("successfully", null);
        return rs;
    }

    /**
     * updateLocalContext
     * 
     * @param dimId
     * @param model
     * @param selectedDims
     * @param chartAreaId
     * 
     */
    private void updateLocalContext(String dimId, Cube cube, ReportRuntimeModel model, String[] selectedDims,
            String areaId) {
        QueryContext localContext = model.getLocalContextByAreaId(areaId);
        localContext.getParams().put(dimId, selectedDims);
        String reportModelId = model.getReportModelId();
        ExtendAreaContext context = this.reportModelCacheManager.getAreaContext(reportModelId, areaId);
        // 添加局部参数key:elementId
        context.getParams().put(dimId, selectedDims);
        // 添加callback请求参数
        if (cube.getDimensions() != null && cube.getDimensions().size() != 0) {
            Dimension dim = cube.getDimensions().get(dimId);
            if (QueryDataUtils.isCallbackDim(dim)) {
                String callbackParamName = QueryDataUtils.getParamName(dim, model.getModel());
                if (selectedDims != null && selectedDims.length != 0) {
                    List<String> tmpList = Lists.newArrayList();
                    for (int i = 0; i < selectedDims.length; i++) {
                        String tmp = QueryDataUtils.getCallbackParamValue(callbackParamName, selectedDims[i]);
                        if (tmp.contains("All_")) {
                            continue;
                        }
                        tmpList.add(tmp);
                    }
                    String values = String.join(",", tmpList);
                    // 添加局部参数
                    model.getLocalContextByAreaId(areaId).getParams().put(callbackParamName, values);
                }
            }
            // 清除面包屑，因为后续判断为!=null，此处设置为null即可，modify by yichao.jiang
            context.setCurBreadCrumPath(null);
        }
        reportModelCacheManager.updateAreaContext(reportModelId, areaId, context);
    }

    /**
     * @param reportId
     * @return ReportDesignModel
     */
    ReportDesignModel getDesignModelFromRuntimeModel(String reportId) {
        return reportModelCacheManager.getRuntimeModel(reportId).getModel();
    }

    /**
     * 
     * @param tmpMembers
     * @param context
     * @return List<DimensionMemberViewObject>
     */
    private List<DimensionMemberViewObject> genChildren(Dimension dim, List<Member> tmpMembers, QueryContext context) {
        final List<DimensionMemberViewObject> rs = Lists.newArrayList();
        DimensionMemberViewObject all = new DimensionMemberViewObject();
        Map<String, Object> params = context.getParams();
        Set<String> tmpKey = Sets.newHashSet();
        params.values().forEach(strArray -> {
            if (strArray instanceof String[]) {
                String[] tmpArray = (String[]) strArray;
                for (String tmpStr : tmpArray) {
                    tmpKey.add(tmpStr);
                }
            }
        });
        all.setCaption("全部");
        all.setNeedLimit(false);
        String name = "[" + dim.getName() + "]";
        name += ".[All_" + dim.getName() + "s]";
        all.setName(name);
        all.setSelected(tmpKey.contains(name));
        rs.add(all);
        tmpMembers.forEach(m -> {
            DimensionMemberViewObject child = new DimensionMemberViewObject();
            child.setCaption(m.getCaption());
            child.setSelected(tmpKey.contains(m.getUniqueName()));
            child.setNeedLimit(false);
            child.setName(m.getUniqueName());
            rs.add(child);
        });
        return rs;
    }

    /**
     * 根据root的member求出callback维度的层级关系
     * 
     * @param allMembers
     * @param cube
     * @param dim
     * @param params
     * @param levelToRoot
     */
    private List<List<Member>> handleCallbackLevel4LiteOlapShow(Cube cube, Dimension dim, Map<String, String> params,
            int levelToRoot) throws Exception {
        Map<String, String> newParams = Maps.newHashMap(params);
        // 添加levelToRoot参数，请求多个层级的岗位
        newParams.put("levelToRoot", String.valueOf(levelToRoot));
        List<List<Member>> members = reportModelQueryService.getMembers(cube, dim, newParams, securityKey);
        List<Member> secondCallbackLevelMembers = Lists.newArrayList();
        List<Member> thirdCallbackLevelMembers = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(members)) {
            for (int i = 0; i < members.size(); i++) {
                List<Member> firstLevelMembers = members.get(i);
                if (!CollectionUtils.isEmpty(firstLevelMembers)) {
                    for (int j = 0; j < firstLevelMembers.size(); j++) {
                        MiniCubeMember member = (MiniCubeMember) firstLevelMembers.get(j);
                        List<Member> secondChildMembers = member.getChildren();
                        if (!CollectionUtils.isEmpty(secondChildMembers)) {
                            secondCallbackLevelMembers.addAll(secondChildMembers);
                            for (int k = 0; k < secondChildMembers.size(); k++) {
                                member = (MiniCubeMember) secondChildMembers.get(k);
                                List<Member> thirdChildMembers = member.getChildren();
                                if (!CollectionUtils.isEmpty(thirdChildMembers)) {
                                    thirdCallbackLevelMembers.addAll(thirdChildMembers);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(secondCallbackLevelMembers)) {
            members.add(secondCallbackLevelMembers);
        }
        if (!CollectionUtils.isEmpty(thirdCallbackLevelMembers)) {
            members.add(thirdCallbackLevelMembers);
        }
        return members;
    }
}