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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.ModelConstants;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.PivotTableParseException;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.query.chart.ChartShowType;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.MutilDimTable;
import com.baidu.rigel.biplatform.ma.report.query.newtable.build.MutilDimTableBuilder;
import com.baidu.rigel.biplatform.ma.report.query.newtable.utils.MutilDimTableUtils;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.service.ChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryDataUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportRunTimeModelUtils;
import com.baidu.rigel.biplatform.ma.resource.builder.QueryDataParamBuilder;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.QueryDataResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * CubeTable的页面交互
 * 
 * @author zhongyi
 * 
 *         2014-7-30
 */
@RestController
@RequestMapping("/silkroad/reports")
@Component("queryDataResource")
public class QueryDataResource extends BaseResource {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(QueryDataResource.class);
    /**
     * reportModelCacheManager
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager;

    /**
     * queryBuildService
     */
    @Resource
    private QueryBuildService queryBuildService;

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
     * dsService
     */
    @Resource
    private DataSourceService dsService;

    /**
     * queryDataResourceUtils
     */
    @Resource
    private QueryDataResourceUtils queryDataResourceUtils;

    /**
     * 统一数据查询接口，主要负责处理所有的多维表，平面表，以及图形查询请求
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}", method = { RequestMethod.POST })
    public ResponseResult queryArea(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        ReportDesignModel model = runTimeModel.getModel();
        ReportDesignModel oriDesignModel = DeepcopyUtils.deepCopy(model);
        // 处理真实请求参数
        handleLocalContextParams(request, model, runTimeModel, areaId);
        ExtendArea targetArea = model.getExtendById(areaId);  
        
        /**
         * add by Jin. Dirty solution:
         * 注意：
         *    1、该方案目前只针对iManager的业绩监控报表，不是通用方案
         *    2、参数中closeDownload=true，则识别为iManager的业绩监控报表来源，统一将表格的对齐方向设置为“right”
         * TODO 报表设计器指标对齐方式增加全局设置功能后，如下代码需要被清理。
         * 
         */
        if (runTimeModel.getContext().getParams().containsKey("closeDownload")) {
            String closeDownload = (String) runTimeModel.getContext().getParams()
                .get("closeDownload");
            if (!StringUtils.isEmpty(closeDownload) && closeDownload.equals("true")) {
                String iManagerTextAlign = "right";
                targetArea.getFormatModel().getTextAlignFormat()
                    .put(Constants.DEFAULT_ALIGN_FORMAT_KEY, iManagerTextAlign);
                logger.info("[INFO]set defaultTextAlign=right ,after :", targetArea
                    .getFormatModel().getTextAlignFormat().get(Constants.DEFAULT_ALIGN_FORMAT_KEY));
            }
        }
        
        // 更新区域本地的上下文
        ExtendAreaContext areaContext = QueryDataUtils.getAreaContext(areaId, request, targetArea, runTimeModel);
        logger.info("[INFO] --- --- --- --- --- ---params with context is : " + areaContext.getParams());
        LogicModel logicModel = targetArea.getLogicModel();
        if (targetArea.isLiteOlapType()) {
            
            LiteOlapExtendArea extendArea =
                    (LiteOlapExtendArea) model.getExtendAreas().get(targetArea.getReferenceAreaId());
            // 将lite-olap选择区域的context取出，放入到本次查询的上下文中
            QueryContext queryContext = runTimeModel.getLocalContextByAreaId(extendArea.getSelectionAreaId());
            areaContext.getParams().putAll(queryContext.getParams());
            logicModel = extendArea.getLogicModel();
        }
        // 生成查询动作QueryAction
        QueryAction action = null;
        Cube cube = model.getSchema().getCubes().get(targetArea.getCubeId());
        if (targetArea.isChartType()) {
            action =
                    this.buildChartQueryAction(request, model, runTimeModel, logicModel, areaContext, targetArea,
                            areaId);
        } else {
            action = queryBuildService.generateTableQueryAction(model, areaId, areaContext.getParams());
            action.setChartQuery(false);
            List<Map<String, String>> breadPath = areaContext.getCurBreadCrumPath();
            if (!CollectionUtils.isEmpty(breadPath)) {
                String uniqueName = breadPath.get(breadPath.size() - 1).get("uniqName");
                if (uniqueName.startsWith("@")) {
                    uniqueName = uniqueName.substring(1, uniqueName.length() - 1);
                }
                Cube transformCube = QueryUtils.transformCube(cube);
                Dimension dim = transformCube.getDimensions().get(MetaNameUtil.getDimNameFromUniqueName(uniqueName));
                action.getDrillDimValues().put(logicModel.getItem(dim.getId()), uniqueName);
            }
        }
        if (!targetArea.isPlaneTableType()
                && (action == null || CollectionUtils.isEmpty(action.getRows()) || CollectionUtils.isEmpty(action
                        .getColumns()))) {
            return ResourceUtils.getErrorResult("单次查询至少需要包含一个横轴、一个纵轴元素", 1);
        }

        ResultSet result = null;
        PageInfo pageInfo = null;
        try {
            if (targetArea.isPlaneTableType()) {
                // 获取上一次查询的QueryAction
                QueryAction queryActionPrevious = runTimeModel.getPreviousQueryAction(areaId);
                // 携带之前的排序信息
                if (queryActionPrevious != null && action.getOrderDesc() != queryActionPrevious.getOrderDesc()) {
                    action.setOrderDesc(queryActionPrevious.getOrderDesc());
                }
                pageInfo =
                        QueryDataUtils.constructPageInfo4Query(runTimeModel, targetArea, queryActionPrevious, request);
                result =
                        reportModelQueryService.queryDatas(model, action, true, areaContext.getParams(), pageInfo,
                                securityKey);
            } else {
                result =
                        reportModelQueryService.queryDatas(model, action, true, true, areaContext.getParams(),
                                securityKey);
            }
        } catch (DataSourceOperationException | QueryModelBuildException e1) {
            logger.info("获取数据源失败！", e1);
            return ResourceUtils.getErrorResult("获取数据源失败！", 1);
        }
        runTimeModel.setModel(model);
        ResponseResult rs =
                queryDataResourceUtils.parseQueryResultToResponseResult(runTimeModel, targetArea, result, areaContext,
                        action);
        rs = generatePageInfo(pageInfo, targetArea, rs);
        this.transNewTable(rs, model, targetArea, result.getDataModel(), cube, logicModel, null, true);
        // 完成查询，再将之前深拷贝的designModel对象设置回去保存一次，update by majun
        runTimeModel.setModel(oriDesignModel);
        // 清除当前request中的请求参数，保证areaContext的参数正确
        resetAreaContext(areaContext, request);
        resetContext(runTimeModel.getLocalContextByAreaId(areaId), request);
        reportModelCacheManager.updateAreaContext(reportId, targetArea.getId(), areaContext);
        runTimeModel.updateDatas(action, result);
        // 因为本方法是每次新查询的入口，所以需要将之前设置的一些下钻展开收起历史都一并清除掉，
        // 但是有两种情况需要排除:1.图表联动点击表格某一行时也会执行该查询，update by majun
        if (StringUtils.isEmpty(request.getParameter("displayName"))) {
            runTimeModel.setLinkedQueryAction(null);
        }
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        return rs;
    }

    /**
     * 根据生成的ResponseResult，继续拼装PageInfo信息
     * 
     * @param result result
     * @param pageInfo pageInfo
     * @param targetArea targetArea
     * @param rs rs
     */
    @SuppressWarnings("unchecked")
    private ResponseResult generatePageInfo(PageInfo pageInfo, ExtendArea targetArea, ResponseResult rs) {
        if (targetArea.isPlaneTableType()) {
            if (rs.getStatus() == 0) {
                Map<String, Object> data = (Map<String, Object>) rs.getData();
                if (data.containsKey("head") && data.containsKey("pageInfo") && data.containsKey("data")) {
                    PageInfo page = (PageInfo) data.get("pageInfo");
                    page.setCurrentPage(pageInfo.getCurrentPage() + 1);
                    page.setPageSize(pageInfo.getPageSize());
                    if (pageInfo.getTotalRecordCount() != -1) {
                        page.setTotalRecordCount(pageInfo.getTotalRecordCount());
                    }
                    data.put("pageInfo", page);
                    rs.setData(data);
                }
            }
        }
        return rs;
    }

    /**
     * 构造图形的queryAction
     * 
     * @param request request
     * @param model model
     * @param runTimeModel runTimeModel
     * @param logicModel logicModel
     * @param areaContext areaContext
     * @param targetArea targetArea
     * @param areaId areaId
     * @return 返回图形的queryAction
     */
    private QueryAction buildChartQueryAction(HttpServletRequest request, ReportDesignModel model,
            ReportRuntimeModel runTimeModel, LogicModel logicModel, ExtendAreaContext areaContext,
            ExtendArea targetArea, String areaId) {
        QueryAction action = null;
        String[] indNames = new String[0];
        if (StringUtils.hasText(request.getParameter("indNames"))) {
            indNames = request.getParameter("indNames").split(",");
        }
        try {
            String topSetting = request.getParameter(Constants.TOP);
            if (!StringUtils.isEmpty(topSetting)) {
                logicModel.setTopSetting(GsonUtils.fromJson(topSetting, MeasureTopSetting.class));
            }
            action =
                    queryBuildService.generateChartQueryAction(model, areaId, areaContext.getParams(), indNames,
                            runTimeModel);
            if (action == null) {
                throw new RuntimeException("该区域未包含任何维度信息");
            }
            action.setChartQuery(true);
            boolean timeLine = isTimeDimOnFirstCol(model, targetArea, action);
            // TODO to be delete
            boolean isPieChart = isPieChart(QueryDataUtils.getChartTypeWithExtendArea(model, targetArea));
            if (!timeLine && isPieChart) {
                action.setNeedOthers(true);
            }
        } catch (QueryModelBuildException e) {
            String msg = "没有配置时间维度，不能使用liteOlap趋势分析图！";
            logger.warn(msg);
        }
        if (action.isTrendQuery()) {
            areaContext.getParams().put(ModelConstants.NEED_SPECIAL_TRADE_TIME, "true");
        }
        return action;
    }

    /**
     * 将request请求参数集合local参数处理，再放入context上下文参数中去
     *
     * @param request request
     * @param model model
     * @param runTimeModel runTimeModel
     * @param areaId areaId
     */
    private void handleLocalContextParams(HttpServletRequest request, ReportDesignModel model,
            ReportRuntimeModel runTimeModel, String areaId) {

        Map<String, Object> tmp = QueryUtils.resetContextParam(request, model);
        tmp.forEach((k, v) -> {
            if (runTimeModel.getContext().getParams().containsKey("fromReportId")
                    && runTimeModel.getContext().getParams().containsKey("toReportId")) {
                if (!runTimeModel.getLocalContextByAreaId(areaId).getParams().containsKey(k)) {
                    runTimeModel.getLocalContextByAreaId(areaId).put(k, v);
                }
            } else {
                runTimeModel.getLocalContextByAreaId(areaId).put(k, v);
            }
        });
    }

    /**
     * 根据dataModel生成新表格所需的数据格式
     * 
     * @param rs ResponseResult
     * @param model ReportDesignModel
     * @param targetArea targetArea
     * @param dataModel dataModel
     * @param cube cube
     * @param logicModel logicModel
     * @param lineUniqueNamePrefix lineUniqueNamePrefix
     * @return 返回多维表格数据
     */
    @SuppressWarnings("unchecked")
    private MutilDimTable transNewTable(ResponseResult rs, ReportDesignModel model, ExtendArea targetArea,
            DataModel dataModel, Cube cube, LogicModel logicModel,
            String lineUniqueNamePrefix, boolean isDrillOption) {
        MutilDimTable mutilDimTable = null;
        // 只有当该查询区域是多维表格的情况下，才进行mutilDimTable的结构封装，后续对图和平面表会有自己对应的格式
        if (rs.getData() instanceof Map && targetArea.isMutiDimTableType()) {
            String[] dimCaptionArray = DataModelUtils.getDimCaptions(cube, logicModel);
            MutilDimTableBuilder mutilDimTableBuilder =
                    MutilDimTableBuilder.getInstance(dataModel, cube, Arrays.asList(dimCaptionArray),
                            lineUniqueNamePrefix);
            mutilDimTable =
                    mutilDimTableBuilder.buildIndsDefine()
                    .buildDimsDefine(isDrillOption).buildTableData().buildMutilDimTable();
            FormatModel formatModel = getFormatModel(model, targetArea);
            Map<String, Object> otherSetting = targetArea.getOtherSetting();
            if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
                otherSetting = model.getExtendById(targetArea.getReferenceAreaId()).getOtherSetting();
            }
            MutilDimTableUtils.decorateTable(formatModel, mutilDimTable, otherSetting);
            Map<String, Object> resultMap = (Map<String, Object>) rs.getData();
            resultMap.put("mutilDimTable", mutilDimTable);
        }
        return mutilDimTable;
    }

    /**
     * @param reportId
     * @return ReportDesignModel
     */
    private ReportDesignModel getDesignModelFromRuntimeModel(String reportId) {
        return reportModelCacheManager.getRuntimeModel(reportId).getModel();
    }

    private FormatModel getFormatModel(ReportDesignModel model, ExtendArea targetArea) {
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            return model.getExtendById(targetArea.getReferenceAreaId()).getFormatModel();
        }
        return targetArea.getFormatModel();
    }

    private boolean isPieChart(Map<String, String> chartType) {
        for (String chart : chartType.values()) {
            if (ChartShowType.PIE.name().toLowerCase().equals(chart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断时间是不是在维度列的第一列上
     * 
     * @param model model
     * @param targetArea targetArea
     * @param action action
     * @return 如果在第一列上，返回true，反之返回false
     */
    private boolean isTimeDimOnFirstCol(ReportDesignModel model, ExtendArea targetArea, QueryAction action) {
        if (action.getRows().isEmpty()) {
            return false;
        }
        Item item = action.getRows().keySet().toArray(new Item[0])[0];
        OlapElement element =
                ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), targetArea.getCubeId(),
                        item.getOlapElementId());
        boolean timeLine = element instanceof TimeDimension;
        return timeLine;
    }

    /**
     * 
     * @param queryContext
     * @param request
     */
    private void resetContext(QueryContext queryContext, HttpServletRequest request) {
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            queryContext.getParams().remove(params.nextElement());
        }
    }

    /**
     * 
     * @param areaContext
     * @param request
     */
    private void resetAreaContext(ExtendAreaContext areaContext, HttpServletRequest request) {
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            areaContext.getParams().remove(params.nextElement());
        }
    }

    /**
     * 选中行
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param rowId rowId
     * @param request 请求对象
     * @return 操作结果
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/selected_row/{rowId}",
            method = { RequestMethod.POST })
    public ResponseResult selectRowWhitRowId(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("rowId") String rowId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO] begin select row operation");
        if (!StringUtils.hasText(rowId)) {
            logger.info("[INFO]no rowid for input! ");
            return ResourceUtils.getErrorResult("no rowid for input! ", 1);
        }
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.info("no such runtime model found for id: " + reportId);
            return ResourceUtils.getErrorResult("no such runtime model found for id: " + reportId, 1);
        }
        runTimeModel.getSelectedRowIds().add(rowId);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("[INFO]------successfully execute select row, cost {} ms", (System.currentTimeMillis() - begin));
        return ResourceUtils.getCorrectResult("Success adding selectedRow. ", "");
    }

    /**
     * 选中表上的行
     * 
     * @param reportId reportId
     * @param areaId areaId
     * @param request request
     * @return 操作结果
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/selected_row", method = { RequestMethod.POST })
    public ResponseResult selectRow(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        String uniqueName = request.getParameter("uniqueName");
        if (!StringUtils.hasText(uniqueName)) {
            logger.error("Empty Row Id when Select! ");
            return ResourceUtils.getErrorResult("Empty Row Id when Select! ", 1);
        }
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("没有运行时的报表实例！报表ID：" + reportId, 1);
        }
        /**
         * 清楚当前选中行，增加一行
         */
        runTimeModel.getSelectedRowIds().clear();
        runTimeModel.getSelectedRowIds().add(uniqueName);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("selected", true);
        return ResourceUtils.getCorrectResult("OK", resultMap);
    }

    /**
     * 反选行
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request 请求对象
     * @return 操作结果
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/selected_row/{rowId}",
            method = { RequestMethod.DELETE })
    public ResponseResult deselectRow(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            @PathVariable("rowId") String rowId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        if (!StringUtils.hasText(rowId)) {
            logger.info("[INFO] --- ---no rowid for input! ");
            return ResourceUtils.getErrorResult("no rowid for input! ", 1);
        }
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.info("[INFO]--- ---no such runtime model found for id: " + reportId);
            return ResourceUtils.getErrorResult("no such runtime model found for id: " + reportId, 1);
        }
        runTimeModel.getSelectedRowIds().remove(rowId);
        logger.info("[INFO]successfully deslect row operation, cost {} ms", (System.currentTimeMillis() - begin));
        return ResourceUtils.getCorrectResult("Success removing selectedRow. ", "");
    }

    /**
     * 展开操作
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @param request 请求对象
     * @return 返回展开操作的操作结果
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/drill", method = { RequestMethod.POST })
    public ResponseResult drillDown(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO]------ begin drill down operation");
        String uniqueName = request.getParameter("uniqueName");
        boolean isRoot = uniqueName.startsWith("@") && uniqueName.endsWith("@");
        uniqueName = uniqueName.replace("@", "");
        ReportDesignModel model;
        try {
            model = this.getDesignModelFromRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.info("[INFO]------Can not find such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
        }
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO]------There are no such model in cache. Report Id: " + reportId, e1);
        }
        ExtendArea targetArea = model.getExtendById(areaId);
        LogicModel targetLogicModel = null;
        String logicModelAreaId = areaId;
        if (targetArea.getType() == ExtendAreaType.CHART || targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            return ResourceUtils.getErrorResult("can not drill down a chart type ", 1);
        } else if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) model.getExtendById(targetArea.getReferenceAreaId());
            targetLogicModel = liteOlapArea.getLogicModel();
            logicModelAreaId = liteOlapArea.getId();
        } else {
            targetLogicModel = targetArea.getLogicModel();
        }

        QueryAction action = null; // (QueryAction) runTimeModel.getContext().get(uniqueName);
        String drillTargetUniqueName = null;
        Map<String, Object> queryParams =
                QueryDataUtils.updateLocalContextAndReturn(runTimeModel, areaId, Maps.newHashMap());
        Item row = null;
        Map<String, Item> store = runTimeModel.getUniversalItemStore().get(logicModelAreaId);
        if (CollectionUtils.isEmpty(store)) {
            String msg = "The item map of area (" + logicModelAreaId + ") is Empty!";
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        if (uniqueName.contains(",")) {
            String[] uniqueNameArray = uniqueName.split(",");
            String dimName = MetaNameUtil.getDimNameFromUniqueName(uniqueNameArray[0]);
            // 获取下钻的值
            row = store.get(dimName);
            List<String> paramValues = Lists.newArrayList();
            for (int i = 0; i < uniqueNameArray.length; i++) {
                String[] tmp = MetaNameUtil.parseUnique2NameArray(uniqueNameArray[i]);
                paramValues.add(tmp[tmp.length - 1]);
            }
            // 先放置全局的参数，此参数会替换参数维度p里面指定的参数
            for (ReportParam p : model.getParams().values()) {
                if (p.getElementId().equals(row.getOlapElementId())) {
                    // queryParams.put(row.getOlapElementId(), String.join(",", paramValues));
                    queryParams.put(p.getName(), String.join(",", paramValues));
                    break;
                }
            }
            // 仅放置本地参数
            queryParams.put(row.getOlapElementId(), String.join(",", paramValues));
        } else {
            /**
             * 找到展开的维度节点
             */
            String[] uniqNames =
                    com.baidu.rigel.biplatform.ac.util.DataModelUtils.parseNodeUniqueNameToNodeValueArray(uniqueName);
            if (uniqNames == null || uniqNames.length == 0) {
                String msg = String.format("Fail in drill down. UniqueName param is empty.");
                logger.error(msg);
                return ResourceUtils.getErrorResult(msg, 1);
            }
            drillTargetUniqueName = uniqNames[uniqNames.length - 1];
            logger.info("[INFO] drillTargetUniqueName : {}", drillTargetUniqueName);
            // isRoot = drillTargetUniqueName.toLowerCase().contains("all");
            Map<String, String[]> oriQueryParams = Maps.newHashMap();
            String dimName = MetaNameUtil.getDimNameFromUniqueName(drillTargetUniqueName);
            row = store.get(dimName);
            if (row == null) {
                throw new IllegalStateException("未找到下钻节点 -" + dimName);
            }
            String[] drillName = new String[] { drillTargetUniqueName };
            oriQueryParams.putAll(request.getParameterMap());
            /**
             * update context
             */
            queryParams = QueryDataUtils.updateLocalContextAndReturn(runTimeModel, areaId, oriQueryParams);
            queryParams.put(row.getOlapElementId(), drillName);
        }
        /**
         * 如果是lite-olap表格，还需要将selection-area中的参数取出，放入到表或者图的area中，modify by yichao.jiang
         */
        this.getLiteOlapParams(targetArea, runTimeModel, queryParams, row.getOlapElementId());
        action = queryBuildService.generateTableQueryAction(model, areaId, queryParams);
        /**
         * 把下钻的值存下来 TODO 临时放在这里，需要重新考虑
         */
        if (!isRoot) {
            action.getDrillDimValues().put(row, drillTargetUniqueName);
        } else {
            action.getDrillDimValues().remove(row);
        }
        runTimeModel.setLinkedQueryAction(action);
        Cube cube = model.getSchema().getCubes().get(targetArea.getCubeId());
        Map<String, Object> params = QueryDataParamBuilder.modifyReportParams(model.getParams(), queryParams, cube);
        reportModelCacheManager.getAreaContext(reportId, areaId).getParams().putAll(params);
        runTimeModel.getLocalContextByAreaId(areaId).setParams(params);
        /**
         * TODO 针对参数映射修改，将当前下钻条件设置到对应参数上
         */
        String[] tmp = new String[0];
        String elementId = row.getOlapElementId();
        if (!StringUtils.isEmpty(drillTargetUniqueName)) {
            tmp = MetaNameUtil.parseUnique2NameArray(drillTargetUniqueName);
            if (!MetaNameUtil.isAllMemberUniqueName(drillTargetUniqueName)) {
                for (ReportParam p : model.getParams().values()) {
                    if (p.getElementId().equals(elementId)) {
                        queryParams.put(p.getName(), tmp[tmp.length - 1]);
                    }
                }
            }
        }
        ResultSet result;
        try {
            result = reportModelQueryService.queryDatas(model, action, true, true, queryParams, securityKey);
        } catch (DataSourceOperationException e1) {
            logger.info("[INFO]--- ---can't get datasource！", e1);
            return ResourceUtils.getErrorResult("获取数据源失败！", 1);
        } catch (QueryModelBuildException e1) {
            logger.info("[INFO]--- ----can't not build question model！", e1);
            return ResourceUtils.getErrorResult("构建问题模型失败！", 1);
        } catch (MiniCubeQueryException e1) {
            logger.info("[INFO] --- --- can't query data ", e1);
            return ResourceUtils.getErrorResult("查询数据失败！", 1);
        }
        runTimeModel.drillDown(action, result);
        Map<String, Object> resultMap = Maps.newHashMap();
        Dimension drillDim = null;
        try {
            drillDim = cube.getDimensions().get(elementId);
        } catch (PivotTableParseException e) {
            logger.info(e.getMessage(), e);
            return ResourceUtils.getErrorResult("Fail in parsing result. ", 1);
        }
        ExtendAreaContext areaContext = reportModelCacheManager.getAreaContext(reportId, targetArea.getId());
        List<Map<String, String>> mainDims =
                generateMainDims(targetArea, areaContext, queryParams, drillTargetUniqueName, isRoot, drillDim, model,
                        cube);
        resultMap.put("mainDimNodes", mainDims);
        reportModelCacheManager.updateAreaContext(reportId, targetArea.getId(), areaContext);
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            for (ExtendArea area : model.getExtendAreaList()) {
                if (ExtendAreaType.LITEOLAP_CHART == area.getType()) {
                    runTimeModel.getLocalContext().put(area.getId(),
                            runTimeModel.getLocalContext().get(targetArea.getId()));
                    break;
                }
            }
        }
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        ResponseResult rs =
                ResourceUtils.getResult("Success Getting VM of Report", "Fail Getting VM of Report", resultMap);
        // for test
        transNewTable(rs, model, targetArea, result.getDataModel(), cube, targetLogicModel, null, true);
        logger.info("[INFO]Successfully execute drill operation. cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }
    
    
    /**
     * getLiteOlapParams
     *
     * @param targetArea
     * @param runTimeModel
     * @param queryParams
     */
    private void getLiteOlapParams(ExtendArea targetArea,
            ReportRuntimeModel runTimeModel, Map<String, Object> queryParams,
            String optionOlapElementId) {

        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) runTimeModel.getModel()
                    .getExtendById(targetArea.getReferenceAreaId());
            // 取得lite-olap select id
            String liteOlapSelectAreaId = liteOlapArea.getSelectionAreaId();
            for (String key : runTimeModel.getLocalContextByAreaId(liteOlapSelectAreaId).getParams().keySet()) {
                // 将不属于下钻或展开维度的参数替换
                if (optionOlapElementId == null || !key.equals(optionOlapElementId)) {
                    // TODO 考虑参数维度特殊处理
                    queryParams.put(key, runTimeModel.getLocalContextByAreaId(liteOlapSelectAreaId).getParams()
                            .get(key));
                }
            }
        }
    }

    /**
     * 生成面包屑信息
     * 
     * @param targetArea targetArea
     * @param areaContext areaContext
     * @param queryParams queryParams
     * @param drillTargetUniqueName drillTargetUniqueName
     * @param isRoot isRoot
     * @param drillDim drillDim
     * @param model model
     * @param cube cube
     * @return 返回生成的面包屑
     */
    private List<Map<String, String>> generateMainDims(ExtendArea targetArea, ExtendAreaContext areaContext,
            Map<String, Object> queryParams, String drillTargetUniqueName, boolean isRoot, Dimension drillDim,
            ReportDesignModel model, Cube cube) {
        if (targetArea.getType() == ExtendAreaType.TABLE || targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            // modify by yichao.jiang将此处新增的参数放置到Context中
            areaContext.getParams().putAll(queryParams);
            /**
             * TODO 考虑一下这样的逻辑是否应该放到resource中
             */
            List<Map<String, String>> mainDims = areaContext.getCurBreadCrumPath();
            boolean remove = false;
            if (mainDims.size() > 0 && !isRoot
                    && !mainDims.get(mainDims.size() - 1).values().toArray()[0].equals(drillTargetUniqueName)) {
                Iterator<Map<String, String>> it = mainDims.iterator();
                while (it.hasNext()) {
                    if (remove) {
                        it.next();
                        it.remove();
                        continue;
                    }
                    Map<String, String> tmpMap = it.next();
                    if (tmpMap.values().toArray()[1].equals(drillTargetUniqueName)) {
                        remove = true;
                    }
                }
            }
            if (!remove && drillTargetUniqueName != null && !drillTargetUniqueName.toLowerCase().contains("all")) {
                Map<String, String> dims3 = Maps.newHashMap();
                dims3.put("uniqName", drillTargetUniqueName);
                DataSourceDefine define = null;
                DataSourceInfo dsInfo = null;
                try {
                    define = dsService.getDsDefine(model.getDsId());
                    dsInfo =
                            DataSourceConnectionServiceFactory.getDataSourceConnectionServiceInstance(
                                    define.getDataSourceType().name()).parseToDataSourceInfo(define, securityKey);
                } catch (DataSourceOperationException | DataSourceConnectionException e) {
                    logger.error(e.getMessage(), e);
                }
                String showName = genShowName(drillTargetUniqueName, drillDim, cube, dsInfo, queryParams);
                if (isRoot) {
                    showName = areaContext.getCurBreadCrumPath().get(0).get("showName");
                }
                dims3.put("showName", showName);
                mainDims.add(dims3);
                // drillTargetUniqueName = MetaNameUtil.getParentUniqueName(drillTargetUniqueName);
            }
            if (isRoot) {
                Iterator<Map<String, String>> it = mainDims.iterator();
                it.next();
                while (it.hasNext()) {
                    it.next();
                    it.remove();
                }
            }

            areaContext.setCurBreadCrumPath(mainDims);
            areaContext.getParams().put("bread_key", mainDims);
            return mainDims;
        }
        return null;
    }

    /**
     * 
     * @param drillTargetUniqueName
     * @param drillDim
     * @param queryParams
     * @param dsInfo
     * @param cube
     * @return
     * 
     */
    private String genShowName(String drillTargetUniqueName, Dimension drillDim, Cube cube, DataSourceInfo dsInfo,
            Map<String, Object> params) {
        String showName =
                drillTargetUniqueName.substring(drillTargetUniqueName.lastIndexOf("[") + 1,
                        drillTargetUniqueName.length() - 1);
        if (showName.contains("All_")) {
            showName = showName.replace("All_", "全部");
            showName = showName.substring(0, showName.length() - 1);
        } else if (drillDim.getType() == DimensionType.CALLBACK) {
            String[] nameArray = MetaNameUtil.parseUnique2NameArray(drillTargetUniqueName);
            Level l = drillDim.getLevels().values().toArray(new Level[0])[nameArray.length - 2];
            Map<String, String> tmp = Maps.newHashMap();
            params.forEach((k, v) -> {
                tmp.put(k, v.toString());
            });
            logger.info("in callback dim show name generate");
            return l.getMembers(QueryUtils.transformCube(cube), dsInfo, tmp).get(0).getCaption();
        }
        return showName;
    }

    /**
     * 处理下钻操作请求
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @param type 下钻类型
     * @param request http请求对象
     * @return 下钻操作 操作结果
     * @throws Exception
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/drill/{type}", method = { RequestMethod.POST })
    public ResponseResult drillDown(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            @PathVariable("type") String type, HttpServletRequest request) throws Exception {
        // // 解析查询条件条件 来自于rowDefine
        String lineUniqueName = request.getParameter("lineUniqueName");
        String prefix = request.getParameter("prefix");
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        ReportDesignModel model;
        try {
            model = DeepcopyUtils.deepCopy(runTimeModel.getModel());
        } catch (CacheOperationException e) {
            logger.info("[INFO] Can not find such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
        }
        ExtendArea targetArea = model.getExtendById(areaId);
        if (targetArea.isChartType()) {
            return ResourceUtils.getErrorResult("can not drill down a chart. ", 1);
        }
        ExtendAreaContext areaContext = reportModelCacheManager.getAreaContext(reportId, targetArea.getId());
        LogicModel targetLogicModel = ReportRunTimeModelUtils.getLogicModel4AnyType(targetArea, model);
        String logicModelAreaId = ReportRunTimeModelUtils.getAreaId4AnyType(targetArea, model, areaId);
        Map<String, Object> queryParams =
                QueryDataUtils.updateLocalContextAndReturn(runTimeModel, areaId, request.getParameterMap());
        // 添加liteolap放大镜的情况
        this.getLiteOlapParams(targetArea, runTimeModel, queryParams, null);
        // 为维度组下钻和展示处理参数中的uniqueName和lineUniqueName，具体原理为：收起和展开时，需要考虑到查询条件当前所选维度值，否则收起再展开后，子层级有可能会多出 updata by majun
        if (targetLogicModel != null && targetLogicModel.getRows() != null) {
            Item[] itemsOnRow = targetLogicModel.getRows();
            for (int i = 0; i < itemsOnRow.length; i++) {
                Object dimParamObj = areaContext.getParams().get(itemsOnRow[i].getId());
                queryParams =
                        QueryDataParamBuilder.bulidDillDownParams4PrepareExpand(dimParamObj, type, lineUniqueName,
                                queryParams);
            }
        }
        /**
         * 找到下钻的维度节点
         */
        String[] uniqNames =
                com.baidu.rigel.biplatform.ac.util.DataModelUtils.parseNodeUniqueNameToNodeValueArray(lineUniqueName);
        if (uniqNames == null || uniqNames.length == 0) {
            String msg = String.format("Fail in drill down. UniqueName param is empty.");
            logger.error(msg);
            return ResourceUtils.getErrorResult(msg, 1);
        }
        Map<String, Item> store = runTimeModel.getUniversalItemStore().get(logicModelAreaId);
        if (CollectionUtils.isEmpty(store)) {
            String msg = "The item map of area (" + logicModelAreaId + ") is Empty!";
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        int targetIndex = uniqNames.length - 1;
        queryParams = QueryDataParamBuilder.buildDillDownParams(queryParams, uniqNames, targetIndex, store, model);
        QueryAction action =
                queryBuildService.generateTableQueryActionForDrill(model, areaId, queryParams, targetIndex);
        
        
        
        
        ResultSet result;
        try {
            result = reportModelQueryService.queryDatas(model, action, true, false, queryParams, securityKey);
        } catch (DataSourceOperationException | QueryModelBuildException | MiniCubeQueryException e1) {
            logger.error(e1.getMessage(), e1);
            return ResourceUtils.getErrorResult("查询出错", 1);
        }
        Map<String, Object> resultMap = Maps.newHashMap();
        Cube cube = model.getSchema().getCubes().get(targetArea.getCubeId());

        String uniqueNameFromReq = request.getParameter("uniqueName");
        QueryContext context = runTimeModel.getLocalContextByAreaId(areaId);
        if (!StringUtils.isEmpty(uniqueNameFromReq)) {
            context.getParams().put("uniqueName", uniqueNameFromReq);
        }
        reportModelCacheManager.updateAreaContext(reportId, targetArea.getId(), areaContext);
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            for (ExtendArea area : model.getExtendAreaList()) {
                if (ExtendAreaType.LITEOLAP_CHART == area.getType()) {
                    QueryContext liteTableContext = runTimeModel.getLocalContextByAreaId(targetArea.getId());
                    runTimeModel.getLocalContext().put(area.getId(), liteTableContext);
                    break;
                }
            }
        }
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        if (targetArea.getType() == ExtendAreaType.TABLE || targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            logger.info("[INFO] row length = " + targetLogicModel.getRows().length);
            if (targetLogicModel.getRows().length >= 2) {
                Object breadCrum = areaContext.getParams().get("bread_key");
                if (breadCrum == null) {
                    List<Map<String, String>> tmp = Lists.newArrayList();
                    if (areaContext.getCurBreadCrumPath() != null && !areaContext.getCurBreadCrumPath().isEmpty()) {
                        tmp.addAll(areaContext.getCurBreadCrumPath());
                        breadCrum = tmp;
                    }
                }
                if (breadCrum != null) {
                    resultMap.put("mainDimNodes", breadCrum);
                }
            } else {
                resultMap.remove("mainDimNodes");
            }
        }
        ResponseResult rs =
                ResourceUtils.getResult("Success Getting VM of Report", "Fail Getting VM of Report", resultMap);
        String uniqueName = request.getParameter("uniqueName");
        DataModel dm4Merage = sectionDataModel4Expand(result, uniqueName, lineUniqueName);
        // 判断操作是否为展开操作
        boolean isDrill = true;
        if ("expand".equals(type)) {
            isDrill = false;
        }
        this.transNewTable(rs, model, targetArea, dm4Merage, cube, targetLogicModel, prefix, isDrill);
        return rs;
    }

    /**
     * 为展开操作将DataModel数据切片
     * 
     * @param result 查询数据集
     * @param uniqueName 下钻的uniqueName
     * @param lineUniqueName 节点所在的行lineUniqueName
     * @return 返回经过切片DataModel数据片
     */
    private DataModel sectionDataModel4Expand(ResultSet result, String uniqueName, String lineUniqueName) {
        DataModel dm4Merage = result.getDataModel();
        // 值针对单维度组下转时候需要做truncation截取操作
        dm4Merage = DataModelUtils.truncationDataModel(dm4Merage, uniqueName, lineUniqueName);
        if (CollectionUtils.isEmpty(dm4Merage.getRowHeadFields())) {
            return dm4Merage;
        }
        HeadField summayHeadField = dm4Merage.getRowHeadFields().get(0);
        if (uniqueName.equals(summayHeadField.getValue())) {
            List<HeadField> subHeads = summayHeadField.getChildren();
            dm4Merage.setRowHeadFields(subHeads);
            int removeTimes = summayHeadField.getNodeList().size() == 0 ? 1 : summayHeadField.getNodeList().size();
            dm4Merage.setRecordSize(dm4Merage.getRecordSize() - removeTimes);
            // 去掉一层父级，列数据也需要对应去掉removeTimes级
            for (List<BigDecimal> baseDataList : dm4Merage.getColumnBaseData()) {
                for (int i = 0 ; i < removeTimes; i ++) {
                    baseDataList.remove(0);
                }
            }
        }
        return dm4Merage;
        
    }

    /**
     * 图形指标切换操作api TODO 目前只支持图形，后续考虑支持表格
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/index/{index}", method = { RequestMethod.POST })
    public ResponseResult changeChartMeasure(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("index") int index, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO] begin query data with new measure");
        ReportDesignModel model;
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        try {
            model = runTimeModel.getModel();
        } catch (CacheOperationException e) {
            logger.info("[INFO]Report model is not in cache! ", e);
            ResponseResult rs = ResourceUtils.getErrorResult("缓存中不存在的报表，ID " + reportId, 1);
            return rs;
        }
        ExtendArea targetArea = model.getExtendById(areaId);
        ExtendAreaContext areaContext = QueryDataUtils.getAreaContext(areaId, request, targetArea, runTimeModel);
        QueryAction action = null;
        if (targetArea.getType() == ExtendAreaType.CHART || targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            String[] indNames = new String[0];
            if (StringUtils.hasText(request.getParameter("indNames"))) {
                indNames = request.getParameter("indNames").split(",");
            }
            try {
                areaContext.getParams().put(Constants.CHART_SELECTED_MEASURE, index);
                action =
                        queryBuildService.generateChartQueryAction(model, areaId, areaContext.getParams(), indNames,
                                runTimeModel);
                if (action != null) {
                    action.setChartQuery(true);
                }
                // TODO to be delete
                boolean timeLine = isTimeDimOnFirstCol(model, targetArea, action);
                boolean isPieChart = isPieChart(QueryDataUtils.getChartTypeWithExtendArea(model, targetArea));
                if (!timeLine && isPieChart) {
                    action.setNeedOthers(true);
                }
            } catch (QueryModelBuildException e) {
                String msg = "没有配置时间维度，不能使用liteOlap趋势分析图！";
                logger.warn(msg);
                DIReportChart chart = new DIReportChart();
                return ResourceUtils.getCorrectResult(msg, chart);
            }
        } else {
            throw new UnsupportedOperationException("未支持的操作");
        }

        ResultSet result;
        try {
            if (action == null || CollectionUtils.isEmpty(action.getRows())
                    || CollectionUtils.isEmpty(action.getColumns())) {
                return ResourceUtils.getErrorResult("单次查询至少需要包含一个横轴、一个纵轴元素", 1);
            }
            areaContext.getParams().remove(Constants.CHART_SELECTED_MEASURE);
            result =
                    reportModelQueryService.queryDatas(model, action, true, true, areaContext.getParams(), securityKey);

        } catch (DataSourceOperationException e1) {
            logger.info("获取数据源失败！", e1);
            return ResourceUtils.getErrorResult("获取数据源失败！", 1);
        } catch (QueryModelBuildException e1) {
            logger.info("构建问题模型失败！", e1);
            return ResourceUtils.getErrorResult("构建问题模型失败！", 1);
        } catch (MiniCubeQueryException e1) {
            logger.info("查询数据失败！", e1);
            return ResourceUtils.getErrorResult("没有查询到相关数据", 1);
        }
        PivotTable table = null;
        Map<String, Object> resultMap = Maps.newHashMap();
        try {
            Cube cube = model.getSchema().getCubes().get(targetArea.getCubeId());
            table = queryBuildService.parseToPivotTable(cube, result.getDataModel(), targetArea.getLogicModel());
        } catch (PivotTableParseException e) {
            logger.info(e.getMessage(), e);
            return ResourceUtils.getErrorResult("Fail in parsing result. ", 1);
        }
        if (targetArea.isMutiDimTableType()) {
            throw new UnsupportedOperationException("未支持的操作");
        } else if (targetArea.isChartType()) {
            DIReportChart chart = null;
            Map<String, String> chartType = QueryDataUtils.getChartTypeWithExtendArea(model, targetArea);
            if (action.getRows().size() == 1) {
                Item item = action.getRows().keySet().toArray(new Item[0])[0];
                OlapElement element =
                        ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(), targetArea.getCubeId(),
                                item.getOlapElementId());
                if (element instanceof TimeDimension) {
                    chart = chartBuildService.parseToChart(table, chartType, true);
                } else {
                    chart = chartBuildService.parseToChart(table, chartType, false);
                }
            } else {
                chart = chartBuildService.parseToChart(table, chartType, false);
            }
            QueryUtils.decorateChart(chart, targetArea, model.getSchema(), index);
            resultMap.put("reportChart", chart);
        }
        // 清除当前request中的请求参数，保证areaContext的参数正确
        resetAreaContext(areaContext, request);
        resetContext(runTimeModel.getLocalContextByAreaId(areaId), request);
        reportModelCacheManager.updateAreaContext(reportId, targetArea.getId(), areaContext);
        runTimeModel.updateDatas(action, result);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("[INFO] successfully query data operation. cost {} ms", (System.currentTimeMillis() - begin));
        ResponseResult rs = ResourceUtils.getResult("Success", "Fail", resultMap);
        return rs;
    }

}
