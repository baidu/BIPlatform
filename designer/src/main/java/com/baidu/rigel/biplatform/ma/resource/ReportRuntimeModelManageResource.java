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

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LinkParams;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.OrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.service.OlapLinkService;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.PlaneTableUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.QueryDataResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * 报表运行时模型管理服务接口： 此接口针提供对运行时报表模型的修改、动态查询条件修改、当前运行模型对应的报表逻辑模型定义检索服务等
 * 
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad/reports/")
public class ReportRuntimeModelManageResource extends BaseResource {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(ReportRuntimeModelManageResource.class);

    /**
     * cache manager
     */
    @Resource(name = "reportModelCacheManager")
    private ReportModelCacheManager reportModelCacheManager;

    @Resource(name = "fileService")
    private FileService fileService;

    /**
     * 报表数据查询服务
     */
    @Resource
    private ReportModelQueryService reportModelQueryService;

    /**
     * queryDataResourceUtils
     */
    @Resource
    private QueryDataResourceUtils queryDataResourceUtils;
    /**
     * olapLinkResource
     */
    @Resource
    private OlapLinkService olapLinkService;
    /**
     * reportDesignModelService
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;

    private static final String REPORT_IMAGE_NAME = "reportImageName";

    private static final String REPORT_IMAGE_ID = "reportImageId";

    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/dimAndInds", method = RequestMethod.POST)
    public ResponseResult getAllDimAndMeasuers(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        // 获取扩展区域
        ExtendArea extendArea = runTimeModel.getModel().getExtendById(areaId);
        // 逻辑模型
        LogicModel logicModel = extendArea.getLogicModel();
        Item[] items = logicModel.getColumns();
        String cubeId = runTimeModel.getModel().getExtendById(areaId).getCubeId();
        Cube cube = runTimeModel.getModel().getSchema().getCubes().get(cubeId);
        List<Map<String, Object>> cols = Lists.newArrayList();

        // 设置维度，如果在LogicModel中则设置为选中，否则不选中；指标处理类似
        cube.getDimensions().forEach((k, dim) -> {
            if (dim.getType() != DimensionType.TIME_DIMENSION) {
                Map<String, Object> map = Maps.newHashMap();
                boolean isInLogicModel = false;
                map.put("id", dim.getId());
                map.put("name", dim.getCaption());
                for (Item item : items) {
                    if (item.getOlapElementId().equals(dim.getId())) {
                        map.put("selected", true);
                        isInLogicModel = true;
                        break;
                    }
                }
                if (!isInLogicModel) {
                    map.put("selected", false);
                }
                cols.add(map);
            }
        });
        cube.getMeasures().forEach((k, m) -> {
            Map<String, Object> map = Maps.newHashMap();
            boolean isInLogicModel = false;
            map.put("id", m.getId());
            map.put("name", m.getCaption());
            for (Item item : items) {
                if (item.getOlapElementId().equals(m.getId())) {
                    map.put("selected", true);
                    isInLogicModel = true;
                    break;
                }
            }
            if (!isInLogicModel) {
                map.put("selected", false);
            }
            cols.add(map);
        });
        result.setStatus(0);
        result.setData(cols);
        return result;
    }

    /**
     * 重置区域逻辑模型
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/reset", method = RequestMethod.POST)
    public ResponseResult resetArea(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }
        ReportDesignModel reportModel = runTimeModel.getModel();
        LogicModel model = reportModel.getExtendById(areaId).getLogicModel();
        // 时间维度
        List<Item> timeItem = Lists.newArrayList();
        // 获取旧的item
        Item[] oldItems = model.getItems();
        ExtendArea area = reportModel.getExtendById(areaId);
        String cubeId = area.getCubeId();
        Schema schema = reportModel.getSchema();
        Cube cube = schema.getCubes().get(cubeId);

        // 寻找原有logicModel时间维度
        for (Item oldItem : oldItems) {
            if (cube != null && cube.getDimensions() != null) {
                for (Dimension dimension : cube.getDimensions().values()) {
                    if ((dimension.getId().equals(oldItem.getId()) && dimension.getType() == DimensionType.TIME_DIMENSION)) {
                        timeItem.add(oldItem);
                    }
                }
            }
        }
        String[] ids = request.getParameter("selectedFields").split(",");
        Item[] items = new Item[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            items[i] = new Item();
            items[i].setAreaId(areaId);
            items[i].setId(ids[i]);
            items[i].setOlapElementId(ids[i]);
            items[i].setReportId(reportId);
            items[i].setPositionType(PositionType.Y);
        }
        model.resetColumns(new Item[0]);
        // 默认将时间维度添加到前面
        timeItem.forEach(item -> {
            model.addColumn(item);
        });
        for (Item item : items) {
            model.addColumn(item);
        }
        model.resetSlices(new Item[0]);
        result.setStatus(0);
        result.setStatusInfo("success");

        Map<String, Object> contextParams = Maps.newHashMap();
        contextParams.putAll(runTimeModel.getContext().getParams());

        Map<String, Object> localContextParams = Maps.newHashMap();
        localContextParams.putAll(runTimeModel.getLocalContextByAreaId(areaId).getParams());

        runTimeModel.getContext().getParams().clear();
        runTimeModel.getLocalContextByAreaId(areaId).reset();
        runTimeModel.getQueryActions().clear();

        // TODO 考虑修改参数信息
        Map<String, PlaneTableCondition> conditions = reportModel.getPlaneTableConditions();
        for (Entry<String, PlaneTableCondition> condition : conditions.entrySet()) {
            String id = condition.getKey();
            PlaneTableCondition planeTableCondition = condition.getValue();
            String paramName = planeTableCondition.getName();
            if (contextParams.containsKey(id)) {
                runTimeModel.getContext().getParams().put(id, contextParams.get(id));
            }
            if (contextParams.containsKey(paramName)) {
                runTimeModel.getContext().getParams().put(paramName, contextParams.get(paramName));
            }

            if (localContextParams.containsKey(id)) {
                runTimeModel.getLocalContextByAreaId(areaId).put(id, localContextParams.get(id));
            }
            if (localContextParams.containsKey(paramName)) {
                runTimeModel.getLocalContextByAreaId(areaId).put(paramName, localContextParams.get(paramName));
            }
        }
        runTimeModel.setModel(reportModel);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        return result;
    }

    /**
     * 增加或修改运行时平面表条件 add by jiangyichao at 2015-05-25, 平面表条件设置或修改
     * 
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/submitSetInfo", method = { RequestMethod.POST })
    public ResponseResult addOrModifyRuntimePlaneTableCondition(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("[INFO] begin query data with new measure");
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model;
        // 获取运行时报表模型
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        try {
            // 根据运行态取得设计模型
            model = getDesignModelFromRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.info("[INFO]Report model is not in cache! ", e);
            result = ResourceUtils.getErrorResult("缓存中不存在的报表，ID " + reportId, 1);
            return result;
        }

        // 获取平面表条件
        String conditions = request.getParameter("conditions");
        // TODO 是否修改
        if (!StringUtils.isEmpty(conditions)) {
            Map<String, String> conditionMap = GsonUtils.fromJson(conditions, new TypeToken<Map<String, String>>() {
            }.getType());
            // 获取条件
            String id = conditionMap.get("id");
            String name = conditionMap.get("field");
            String defaultValue = conditionMap.get("defaultValue");
            String condition = conditionMap.get("condition");
            // 对LIKE条件进行特殊处理
            if ("LIKE".equals(condition)) {
                if (defaultValue != null) {
                    // 如果不是以%开头，则需要在defaultValue的开头添加%
                    if (!defaultValue.startsWith("%")) {
                        defaultValue = "%" + defaultValue;
                    }
                    // 如果不是以%结尾，则需要在defaultValue的结尾添加%
                    if (!defaultValue.endsWith("%")) {
                        defaultValue = defaultValue + "%";
                    }
                }
            }
            if (PlaneTableUtils.checkSQLCondition(condition, defaultValue)) {
                PlaneTableCondition planeTableCondition = new PlaneTableCondition();
                planeTableCondition.setElementId(id);
                planeTableCondition.setName(name);
                planeTableCondition.setSQLCondition(SQLConditionType.valueOf(condition));
                planeTableCondition.setDefaultValue(defaultValue);
                // 获取原有报表的平面表条件信息
                Map<String, PlaneTableCondition> oldConditions = model.getPlaneTableConditions();
                PlaneTableCondition oldCondition = oldConditions.get(id);
                // TODO 仔细考虑
                if (oldCondition != null) {
                    runTimeModel.getContext().getParams().remove(id);
                    runTimeModel.getLocalContextByAreaId(areaId).getParams().remove(id);
                    runTimeModel.getLocalContextByAreaId(areaId).getParams().remove(oldCondition.getName());
                }
                oldConditions.put(id, planeTableCondition);
                model.setPlaneTableConditions(oldConditions);
                runTimeModel.setModel(model);
                // 更新报表模型
                reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
                // reportModelCacheManager.updateReportModelToCache(reportId, model);
                logger.info("successfully add planeTable condition in runtime phase");
                result.setStatus(0);
                result.setData(model);
                result.setStatusInfo("successfully add planeTable condition in runtime phase ");
                return result;
            } else {
                result.setStatus(1);
                result.setStatusInfo("参数设置不正确，请注意检查");
                return result;
            }
        } else {
            result.setStatus(1);
            result.setStatusInfo("没有传入参数条件，请检查");
            return result;
        }

    }

    /**
     * 删除平面表条件信息 add by jiangyichao at 2015-05-25，删除平面表条件信息
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/item/{elementId}/removeSetInfo",
            method = { RequestMethod.POST })
    public ResponseResult removeRuntimePlaneTableConditions(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("elementId") String elementId,
            HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model;
        // 获取运行时报表模型
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        try {
            // 根据运行态取得设计模型
            model = getDesignModelFromRuntimeModel(reportId);
        } catch (CacheOperationException e) {
            logger.info("[INFO]Report model is not in cache! ", e);
            result = ResourceUtils.getErrorResult("缓存中不存在的报表，ID " + reportId, 1);
            return result;
        }

        // 获取该element对应的平面表条件信息
        Map<String, PlaneTableCondition> oldConditionsMap = model.getPlaneTableConditions();
        // 参数名称
        String conditionName = null;
        // 删除对应条件
        if (oldConditionsMap.containsKey(elementId)) {
            conditionName = oldConditionsMap.get(elementId).getName();
            oldConditionsMap.remove(elementId);
        }
        model.setPlaneTableConditions(oldConditionsMap);
        runTimeModel.setModel(model);
        // TODO 仔细考虑
        runTimeModel.getContext().getParams().remove(elementId);
        runTimeModel.getLocalContextByAreaId(areaId).getParams().remove(elementId);
        runTimeModel.getLocalContextByAreaId(areaId).getParams().remove(conditionName);
        // runTimeModel.getQueryActions().clear();
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        // reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("successfully remove planeTable condition in runtime phase");
        result.setStatus(0);
        // result.setData(model);
        result.setStatusInfo("successfully remove planeTable condition in runtime phase ");
        return result;
    }

    /**
     * 平面表排序
     * 
     * @param reportId
     * @param areaId
     * @param elementId
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/item/{elementId}/sort",
            method = { RequestMethod.POST })
    public ResponseResult sortPlaneTableColumns(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("elementId") String elementId,
            HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("begin execuet sort for planeTable");
        // String orderBy = request.getParameter("orderbyParamKey");
        String sort = request.getParameter("sortType");
        // 获取排序方式
        ResponseResult result = new ResponseResult();

        // 获取运行态模型
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        ReportDesignModel model;
        ReportDesignModel oriModel;
        try {
            // 根据运行态取得设计模型
            model = getDesignModelFromRuntimeModel(reportId);
            oriModel = DeepcopyUtils.deepCopy(model);
        } catch (CacheOperationException e) {
            logger.info("[INFO]Report model is not in cache! ", e);
            result = ResourceUtils.getErrorResult("缓存中不存在的报表，ID " + reportId, 1);
            return result;
        }

        // 区域上下文
        ExtendAreaContext areaContext = reportModelCacheManager.getAreaContext(reportId, areaId);
        areaContext.getParams().clear();
        areaContext.getParams().putAll(runTimeModel.getContext().getParams());

        // 扩展区域
        ExtendArea area = model.getExtendById(areaId);
        Schema schema = model.getSchema();
        Map<String, ? extends Cube> cubes = schema.getCubes();
        Cube cube = cubes.get(area.getCubeId());

        // 获取上一次查询的QueryAction
        QueryAction queryAction = runTimeModel.getPreviousQueryAction(areaId);
        // 获取排序条件
        OrderDesc orderDesc = this.getNewOrderDesc(cube, elementId, sort);
        if (orderDesc != null) {
            // 重新设置QueryAction的排序方式
            queryAction.setOrderDesc(orderDesc);
        }
        // 构建分页信息
        PageInfo pageInfo = this.getPageInfo(request);
        // 结果集
        ResultSet resultSet = null;
        // 重新查询数据
        try {
            resultSet =
                    reportModelQueryService.queryDatas(model, queryAction, true, areaContext.getParams(), pageInfo,
                            securityKey);
        } catch (DataSourceOperationException | QueryModelBuildException e1) {
            logger.info("获取数据源失败！", e1);
            return ResourceUtils.getErrorResult("获取数据源失败！", 1);
        }

        runTimeModel.setModel(model);
        // 对返回结果进行处理，用于表、图显示
        result =
                queryDataResourceUtils.parseQueryResultToResponseResult(runTimeModel, area, resultSet, areaContext,
                        queryAction);
        runTimeModel.setModel(oriModel);
        // 维护平面表分页信息
        if (result.getStatus() == 0) {
            Map<String, Object> data = (Map<String, Object>) result.getData();
            if (data.containsKey("head") && data.containsKey("pageInfo") && data.containsKey("data")) {
                PageInfo page = (PageInfo) data.get("pageInfo");
                page.setCurrentPage(pageInfo.getCurrentPage() + 1);
                page.setPageSize(pageInfo.getPageSize());
                data.put("pageInfo", page);
                result.setData(data);
            }
        }
        // 更新本次操作结果
        runTimeModel.updateDatas(queryAction, resultSet);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        // reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("[INFO]successfully sort by " + orderDesc.getName() + " as " + orderDesc.getOrderType()
                + " for planeTable ");
        logger.info("[INFO]sort planeTable cost : " + (System.currentTimeMillis() - begin) + " ms");
        result.setStatus(0);
        // result.setData(model);
        result.setStatusInfo("successfully sort plane table in runtime phase ");
        return result;
    }

    /**
     * 当点击指标跳转之后，要执行的action
     * 
     * @param reportId 报表id
     * @param areaId 表格所在区域id
     * @param request request请求对象
     * @param attr redirect之后要带的跳转参数对象
     * @return 返回redirect的ModelAndView对象
     */
    @RequestMapping(value = "/{reportId}/linkBridge/extend_area/{areaId}", method = { RequestMethod.POST })
    public ModelAndView linkBridge(@PathVariable("reportId") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request, RedirectAttributes attr) {
        String uniqueName = request.getParameter("uniqueName");
        if (StringUtils.isEmpty(uniqueName)) {
            throw new RuntimeException("selected table's uniqueName must not be null!");
        }
        String measureId = request.getParameter("measureId");
        ReportRuntimeModel reportRuntimeModel = reportModelCacheManager.getRuntimeModel(reportId);

        ReportDesignModel olapTableDesignModel = reportRuntimeModel.getModel();
        ExtendArea tableArea = olapTableDesignModel.getExtendAreas().get(areaId);
        Map<String, LinkInfo> linkInfoMap = null;
        if (tableArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            linkInfoMap =
                    olapTableDesignModel.getExtendAreas().get(tableArea.getReferenceAreaId()).getFormatModel()
                            .getLinkInfo();
        } else {
            linkInfoMap = tableArea.getFormatModel().getLinkInfo();
        }
        LinkInfo linkInfo = linkInfoMap.get(measureId);
        // 跳转到的reportId
        String toTableId = linkInfo.getTargetTableId();

        Map<String, Object> params = reportRuntimeModel.getContext().getParams();
        reportRuntimeModel.getLocalContext().forEach((k, v) -> {
            params.putAll(v.getParams());
        });
        reportRuntimeModel.getContext().setParams(params);
        Map<String, Map<String, String>> conditionMap =
                this.olapLinkService.buildConditionMapFromRequestParams(uniqueName, olapTableDesignModel,
                        reportRuntimeModel.getContext());
        Map<String, LinkParams> linkBridgeParams = this.olapLinkService.buildLinkBridgeParams(linkInfo, conditionMap);
        reportRuntimeModel.getContext().getParams().put("linkBridgeParams", linkBridgeParams);

        attr.addAttribute("fromReportId", reportId);
        attr.addAttribute("toReportId", toTableId);

        /**
         * 因为在redirect的时候，浏览器会将请求地址重置为designer服务器自身的host+port，使得bfe设置的反向代理失效， 故这里需要将redirect的地址拼接为全路径，以避免换域问题
         */
        String referer = request.getHeader("referer");
        String realmName = "";
        if (!StringUtils.isEmpty(referer)) {
            int index = referer.indexOf("/silkroad");
            realmName = referer.substring(0, index);
        }
        String redirectUrl = "redirect:" + realmName + "/silkroad/reports/" + toTableId + "/report_vm";
        ModelAndView mav = new ModelAndView(redirectUrl);
        ReportDesignModel planeTableModel = null;
        try {
            attr.addAttribute("token",
                    AesUtil.getInstance().encryptAndUrlEncoding(ContextManager.getProductLine(), securityKey));
            attr.addAttribute("_rbk", ContextManager.getProductLine());
            // 先从已发布的报表中寻找
            planeTableModel = reportDesignModelService.getModelByIdOrName(toTableId, true);
            // 如果从已发布当中找不到，则直接报错
            if (planeTableModel == null) {
                throw new RuntimeException("no planetable exist the report id is : " + toTableId);
            }
            // 将跳转到的表的设计态模型放入cache中
            reportModelCacheManager.updateReportModelToCache(toTableId, planeTableModel);
            // 将跳转表的运行态模型放入cache中
            reportModelCacheManager.updateRunTimeModelToCache(reportId, reportRuntimeModel);

            // 添加对动态数据源的处理逻辑
//            Object activedsObj = reportRuntimeModel.getContext().getParams().get("activeds");
//            if (activedsObj != null && StringUtils.hasLength(activedsObj.toString())) {
//                String activedsName = activedsObj.toString();
//                ReportRuntimeModel planeTableRuntimeModel = null;
//                try {
//                    planeTableRuntimeModel = reportModelCacheManager.getRuntimeModel(toTableId);
//                } catch (Exception e) {
//                    logger.error(e.getMessage(), e);
//                }
//                if (planeTableRuntimeModel == null) {
//                    planeTableRuntimeModel = new ReportRuntimeModel(toTableId);
//                    planeTableRuntimeModel.init(planeTableModel, true);
//                }
//                planeTableRuntimeModel.getContext().getParams().put("activeds", activedsName);
            try {
                ReportRuntimeModel planeTableRuntimeModel = reportModelCacheManager
                        .getRuntimeModel(toTableId);
                
                if (planeTableRuntimeModel == null) {
                    planeTableRuntimeModel = new ReportRuntimeModel(toTableId);
                    planeTableRuntimeModel.init(planeTableModel, true);
                }
                if (reportRuntimeModel != null && reportRuntimeModel.getContext() != null
                        && reportRuntimeModel.getContext().getParams() != null) {
                    Map<String, Object> map = Maps.newHashMap();
                    map.putAll(reportRuntimeModel.getContext().getParams());
                    map.putAll(planeTableRuntimeModel.getContext().getParams());
                    planeTableRuntimeModel.getContext().getParams().putAll(map);
                }
                reportModelCacheManager
                        .updateRunTimeModelToCache(toTableId, planeTableRuntimeModel);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mav = new ModelAndView("redirect:" + realmName + "/silkroad/error");
        }

        return mav;
    }

    /**
     * 产生新的排序信息
     * 
     * @param cube
     * @param elementId
     * @param sort
     * @return
     */
    private OrderDesc getNewOrderDesc(Cube cube, String elementId, String sort) {
        // 获取指标
        Map<String, Measure> measures = cube.getMeasures();
        // 获取维度
        Map<String, Dimension> dimensions = cube.getDimensions();
        // 如果待排序列为指标
        if (measures.containsKey(elementId)) {
            Measure measure = measures.get(elementId);
            return new OrderDesc(measure.getName(), sort, 500);
        } else if (dimensions.containsKey(elementId)) {
            // 如果待排序列为维度
            Dimension dimension = dimensions.get(elementId);
            if (dimension.getType() == DimensionType.TIME_DIMENSION) {
                Level l = dimension.getLevels().values().toArray(new Level[0])[0];
                return new OrderDesc(l.getDimTable() + "_" + l.getName(), sort, 500);
            } else {
                Level l = dimension.getLevels().values().toArray(new Level[0])[0];
                if (l.getDimension() != null) {
                    return new OrderDesc(l.getDimension().getName(), sort, 500);
                }
                return new OrderDesc(l.getDimTable() + "_" + l.getName(), sort, 500);
            }
        }
        return null;
    }

    /**
     * 获取平面表分页信息
     * 
     * @param request
     * @return
     */
    private PageInfo getPageInfo(HttpServletRequest request) {
        PageInfo pageInfo = new PageInfo();
        // 设置分页大小
        if (StringUtils.hasLength(request.getParameter("pageSize"))) {
            pageInfo.setPageSize(Integer.valueOf(request.getParameter("pageSize")));
        }
        // 设置当前页
        if (StringUtils.hasLength(request.getParameter("currentPage"))) {
            pageInfo.setCurrentPage(Integer.valueOf(request.getParameter("currentPage")) - 1);
        }
        // 设置总的记录数
        if (StringUtils.hasLength(request.getParameter("totalRecordCount"))) {
            pageInfo.setTotalRecordCount(Integer.valueOf(request.getParameter("totalRecordCount")));
        } else {
            pageInfo.setTotalRecordCount(-1);
        }
        return pageInfo;
    }

    /**
     * @param reportId
     * @return ReportDesignModel
     */
    ReportDesignModel getDesignModelFromRuntimeModel(String reportId) {
        return reportModelCacheManager.getRuntimeModel(reportId).getModel();
    }

    /**
     * 报表保存功能实现
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/new_status", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseResult saveRuntimeModel(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }
        String name = request.getParameter(REPORT_IMAGE_NAME);
        ReportRuntimeModel copy = copyRuntimeModel(runTimeModel);
        modifyCopyWithParams(copy, request);
        copy.getModel().setName(name);
        String savedReportPath = getRealStorePath(request, copy);
        try {
            copy.setCreateTime(System.nanoTime());
            fileService.write(savedReportPath, SerializationUtils.serialize(copy), true);
            reportModelCacheManager.updateRunTimeModelToCache(copy.getReportModelId(), copy);
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
        }
        result.setStatus(0);
        result.setStatusInfo("successfully");
        Map<String, String> datas = Maps.newHashMap();

        datas.put(REPORT_IMAGE_ID, copy.getReportModelId());
        result.setData(datas);
        logger.info("save report succcessfully with id : {} on path {}", copy.getReportModelId(), savedReportPath);
        return result;
    }

    /**
     * 已经保存报表更新功能实现
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/status", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseResult update(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }
        runTimeModel.getModel().setName(request.getParameter(REPORT_IMAGE_NAME));
        String savedReportPath = getRealStorePath(request, runTimeModel);
        try {
            fileService.write(savedReportPath, SerializationUtils.serialize(runTimeModel), true);
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
        }
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        result.setStatus(0);
        result.setStatusInfo("successfully");
        logger.info("save report succcessfully with id : {} on path {}", reportId, savedReportPath);
        return result;
    }

    /**
     * 已经保存报表更新功能实现
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/del_status", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseResult delete(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        if (runTimeModel == null) {
            logger.info("[INFO] There are no such model in cache. Report Id: " + reportId);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }
        String removeFile = getRealStorePath(request, runTimeModel);
        try {
            fileService.rm(removeFile);
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
            result.setStatus(1);
            result.setStatusInfo("error");
            return result;
        }
        result.setStatus(0);
        result.setStatusInfo("successfully");
        logger.info("save report succcessfully with id : {} on path {}", reportId, removeFile);
        return result;
    }

    @RequestMapping(value = "/{reportId}/status/list", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseResult listAllSavedReport(@PathVariable("reportId") String reportId, HttpServletRequest request)
            throws Exception {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        String fileList = this.getSavedReportPath(request) + File.separator + runtimeModel.getOriReportId();
        String[] files = null;
        try {
            files = fileService.ls(fileList);
        } catch (Exception e) {
            // 如果发生异常，则将文件列表设置为null
            files = null;
        }
        LinkedHashMap<String, String> rep = Maps.newLinkedHashMap();
        if (files == null) {
            result.setStatus(0);
            result.setStatusInfo("successfully");
            result.setData(Maps.newHashMap());
            return result;
        }
        Map<String, Object> datas = Maps.newHashMap();
        List<ReportRuntimeModel> tmp = Lists.newArrayList();
        for (String file : files) {
            String filePath = fileList + File.separator + file;
            ReportRuntimeModel model = (ReportRuntimeModel) SerializationUtils.deserialize(fileService.read(filePath));
            tmp.add(model);
        }
        Collections.sort(tmp, new Comparator<ReportRuntimeModel>() {

            @Override
            public int compare(ReportRuntimeModel o1, ReportRuntimeModel o2) {
                return Long.valueOf(o1.getCreateTime()).compareTo(o2.getCreateTime());
            }
        });
        for (ReportRuntimeModel m : tmp) {
            rep.put(m.getReportModelId(), m.getModel().getName());
        }

        datas.put("reportId", runtimeModel.getOriReportId());
        datas.put("reportImage", rep);
        result.setData(datas);
        result.setStatus(0);
        result.setStatusInfo("successfully");
        logger.info("save report succcessfully with id : {} on path {}", reportId, fileList);
        return result;
    }

    private String getRealStorePath(HttpServletRequest request, ReportRuntimeModel runTimeModel) {
        return getSavedReportPath(request) + File.separator + runTimeModel.getOriReportId() + File.separator
                + runTimeModel.getReportModelId();
    }

    /**
     * 依据用户请求参数信息，在上下文中保存用户当前请求参数信息
     * 
     * @param copy
     * @param request
     */
    private void modifyCopyWithParams(final ReportRuntimeModel copy, final HttpServletRequest request) {
        Map<String, ReportParam> reportParams = copy.getModel().getParams();
        if (reportParams == null || reportParams.isEmpty()) {
            return;
        }
        reportParams.forEach((k, v) -> {
            String parameter = request.getParameter(v.getName());
            if (parameter != null) {
                copy.getContext().put(v.getName(), parameter);
            }
        });
    }

    /**
     * 构建当前runtimeModel的副本
     * 
     * @param runTimeModel
     * @return ReportRuntimeModel
     */
    private ReportRuntimeModel copyRuntimeModel(ReportRuntimeModel runTimeModel) {
        String uuid = UuidGeneratorUtils.generate();
        ReportRuntimeModel copy = new ReportRuntimeModel(uuid);
        ReportDesignModel model = DeepcopyUtils.deepCopy(runTimeModel.getModel());
        copy.setOriReportId(runTimeModel.getOriReportId());
        model.setId(uuid);
        copy.setModel(model);
        copy.setContext(runTimeModel.getContext());
        copy.setId(UuidGeneratorUtils.generate().toString());
        copy.setLocalContext(runTimeModel.getLocalContext());
        copy.setTimeDimItemIds(runTimeModel.getTimeDimItemIds());
        copy.setUniversalItemStore(runTimeModel.getUniversalItemStore());

        return copy;
    }
}
