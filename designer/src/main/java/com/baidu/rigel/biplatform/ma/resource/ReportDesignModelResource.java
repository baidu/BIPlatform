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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.ma.auth.bo.ReportDesignModelBo;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.model.TimerAreaLogicModel;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelManageService;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.report.utils.ExtendAreaUtils;
import com.baidu.rigel.biplatform.ma.report.utils.NameCheckUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.NameCheckCacheManager;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.DragRuleCheckUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.PlaneTableUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.view.vo.ExtendAreaViewObject;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * 报表模型管理rest接口
 * 
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad/reports")
public class ReportDesignModelResource extends BaseResource {

    /**
     * 日志记录器
     */
    private static Logger logger = LoggerFactory.getLogger(ReportDesignModelService.class);

    /**
     * success message
     */
    private static final String SUCCESS = "successfully";

    /**
     * cache manager
     */
    @Resource(name = "reportModelCacheManager")
    private ReportModelCacheManager reportModelCacheManager;

    /**
     * manageService
     */
    @Resource(name = "manageService")
    private ReportDesignModelManageService manageService;

    /**
     * reportDesignModelService
     */
    @Resource(name = "reportDesignModelService")
    private ReportDesignModelService reportDesignModelService;

    /**
     * nameCheckCacheManager
     */
    @Resource(name = "nameCheckCacheManager")
    private NameCheckCacheManager nameCheckCacheManager;
    
    @Resource(name = "fileService")
    private FileService fileService;

    /**
     * 
     * 查询报表模型状态
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(method = { RequestMethod.GET })
    public ResponseResult listAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // modify by jiangyichao at 2014-10-13
        // 当status为0，data为null时，表示cookie中没有产品线信息，需要跳转到登录页面
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setData(null);
        rs.setStatusInfo("can not get productline message, please login first!");
        String productLine = ContextManager.getProductLine();
        if (!StringUtils.isEmpty(productLine)) {
            ReportDesignModel[] modelList = reportDesignModelService.queryAllModels(false);
            ReportDesignModelBo[] reportList = genReportModelList(modelList);
            rs = getResult(SUCCESS, "can not get model list", reportList);
        }
        return rs;
    }

    /**
     * 
     * 查询报表模型状态
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/online", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult listAllReleaseReport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setData(null);
        rs.setStatusInfo("can not get productline message, please login first!");
        String productLine = ContextManager.getProductLine();
        if (!StringUtils.isEmpty(productLine)) {
            ReportDesignModel[] modelList = reportDesignModelService.queryAllModels(true);
            ReportDesignModelBo[] reportList = genReportModelList(modelList);
            rs = getResult(SUCCESS, "can not get model list", reportList);
        }
        return rs;
    }

    /**
     * 
     * @param modelList
     * @return ReportDesignModelBo[]
     */
    private ReportDesignModelBo[] genReportModelList(ReportDesignModel[] modelList) {
        if (modelList == null || modelList.length == 0) {
            return new ReportDesignModelBo[0];
        }
        ReportDesignModelBo[] rs = new ReportDesignModelBo[modelList.length];
        String token = null;
        try {
            token = AesUtil.getInstance().encryptAndUrlEncoding(ContextManager.getProductLine(), securityKey);
        } catch (Exception e) {
        }
        int i = 0;
        for (ReportDesignModel model : modelList) {
            rs[i] = new ReportDesignModelBo();
            rs[i].setId(model.getId());
            rs[i].setName(model.getName());
            rs[i].setDsId(model.getDsId());
            rs[i].setTheme(model.getTheme());
            rs[i].setToken(token);
            rs[i++].setRunTimeId(model.getRunTimeId());
        }
        return rs;
    }

    /**
     * 查询报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.GET })
    public ResponseResult queryById(@PathVariable("id") String id, HttpServletRequest request) {
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (model != null) {
            logger.info("get model from cache");
        } else {
            model = reportDesignModelService.getModelByIdOrName(id, false);
        }
        ResponseResult rs = getResult(SUCCESS, "can not get mode define info", model);
        logger.info("query operation rs is : " + rs.toString());
        return rs;
    }

    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param errorMessage
     * @param data
     * @return
     */
    private ResponseResult getResult(String successMessage, String errorMessage, Object data) {
        ResponseResult rs = new ResponseResult();
        if (data == null) {
            rs = ResourceUtils.getErrorResult(errorMessage, ResponseResult.FAILED);
        } else {
            rs = ResourceUtils.getCorrectResult(successMessage, data);
        }
        return rs;
    }

    /**
     * 删除报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseResult deleteReport(@PathVariable("id") String id, HttpServletRequest request) {

        ResponseResult result = new ResponseResult();
        try {
            boolean rs = reportDesignModelService.deleteModel(id, true);
            if (rs) {
                result.setStatus(0);
                result.setStatusInfo(SUCCESS);
                // Important: Remove model from cache if exist
                reportModelCacheManager.deleteReportModel(id);
                logger.info("delete operation successfully");
            } else {
                result.setStatus(1);
                result.setStatusInfo("删除报表失败，请确认这张报表没有发布。如果仍然需要删除，请联系维护人员。");
                logger.info("delete operation failed");
            }
        } catch (ReportModelOperationException e) {
            logger.error(e.getMessage(), e);
            result.setStatusInfo(e.getMessage());
        }
        logger.info("delete report rs is :" + result.toString());
        return result;
    }

    /**
     * 拷贝报表模型
     * 
     * @return
     */
    @RequestMapping(value = "/{id}/duplicate", method = { RequestMethod.POST })
    public ResponseResult copyReport(@PathVariable("id") String id, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String targetName = request.getParameter("name");
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(targetName)) {
            rs.setStatus(1);
            rs.setStatusInfo("id or target name is empty : id = " + id + ", target = " + targetName);
        } else {
            String productLine = ContextManager.getProductLine();
            String tmpKey = productLine + "_" + targetName;
            try {
                if (reportDesignModelService.isNameExist(targetName)) {
                    return ResourceUtils.getErrorResult("名称已经存在,请更换名称 ! ", 1);
                }
                ReportDesignModel tmp = reportDesignModelService.copyModel(id, targetName);
                if (tmp != null) {
                    reportModelCacheManager.updateReportModelToCache(tmp.getId(), tmp);
                    reportModelCacheManager.updateReportModelToCache(tmpKey, tmp);
                    logger.info("cached model object : " + tmpKey);
                }
                rs = this.getResult(SUCCESS, "error", tmp);
                logger.info(tmp == null ? "error happened" : "copy report successfully");
            } catch (ReportModelOperationException e) {
                logger.error(e.getMessage(), e);
                rs.setStatus(1);
                rs.setStatusInfo(e.getMessage());
            }
        }
        return rs;
    }

    /**
     * 创建报表模型
     * 
     * @return
     */
    @RequestMapping(method = { RequestMethod.POST })
    public ResponseResult createReport(HttpServletRequest request) {
        String name = request.getParameter("name");
        ResponseResult rs = new ResponseResult();
        if (StringUtils.isEmpty(name)) {
            rs.setStatus(1);
            rs.setStatusInfo("name can not be null");
            logger.debug("name is empty");
            return rs;
        }

        if (NameCheckUtils.isInvalidName(name)) {
            rs.setStatus(1);
            rs.setStatusInfo("名称格式非法");
            logger.debug("name too length ：" + name);
            return rs;
        }

        if (reportDesignModelService.isNameExist(name)) {
            logger.info("name already exist: " + name);
            rs.setStatus(1);
            rs.setStatusInfo("名称已经存在");
            return rs;
        }
        ReportDesignModel model = new ReportDesignModel();
        String id = UuidGeneratorUtils.generate();
        model.setId(id);
        model.setName(name);

        logger.info("create report : " + rs.toString());
        reportModelCacheManager.updateReportModelToCache(id, model);
        logger.info("create report successuflly");

        rs.setStatus(0);
        rs.setStatusInfo(SUCCESS);
        rs.setData(model);
        return rs;
    }

    /**
     * 删除区域
     * 
     * @return 返回操作结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}", method = { RequestMethod.DELETE })
    public ResponseResult removeArea(@PathVariable("id") String reportId, @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        ReportDesignModel copy = DeepcopyUtils.deepCopy (model);
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        try {
            ExtendArea area = DeepcopyUtils.deepCopy (model.getExtendById (areaId));
            model = manageService.removeExtendArea(model, areaId);
            ExtendAreaType type = area.getType ();
            if (runTimeModel != null && QueryUtils.isFilterArea (type)) {
                /**
                 * 删除区域时，清除运行时的上下文中的维度条件 TODO 这里的逻辑要移除到别处
                 */
                for (final Item item : area.listAllItems().values()) {
                    String dimId = item.getOlapElementId();
                    runTimeModel.getContext().getParams().remove(dimId);
                    runTimeModel.getLocalContext().values().forEach(ctx -> {
                        ctx.getParams().remove(dimId);
                    });
                    // TODO 需要优化测出执行逻辑
                    copy.getExtendAreas ().forEach ((k, v) -> {
                        ExtendAreaContext ctx = reportModelCacheManager.getAreaContext (reportId, k);
                        if (ctx != null) {
                            ctx.getParams ().remove (dimId);
                            reportModelCacheManager.updateAreaContext (reportId, k, ctx);
                        }
                    });
                }
            }
            if (type == ExtendAreaType.REPORT_SAVE_COMP) {
                String path = this.getSavedReportPath (request) + File.separator + reportId;
                String[] files = fileService.ls (path);
                if (files != null) {
                    for (String f : files) {
                        fileService.rm (path + File.separator + f);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage (), e);
            result.setStatus(1);
            result.setStatusInfo("fail remove area from model! ");
            return result;
        }
        
       
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("successfully create area for current report");
        result.setStatus(0);
        result.setData("");
        result.setStatusInfo("successfully");
        return result;
    }

    /**
     * 保存报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = { RequestMethod.POST })
    public ResponseResult saveReport(@PathVariable("id") String id, HttpServletRequest request) {
        ReportDesignModel model = reportModelCacheManager.getReportModel(id);
        ResponseResult rs = new ResponseResult();
        if (model == null) {
            logger.info("can not get model from cache with current id : " + id);
            rs.setStatus(1);
            rs.setStatusInfo("save operation failed");
            return rs;
        }

        try {
            model.setPersStatus(true);
            if (reportDesignModelService.isNameExist(model.getName(), model.getId())) {
                rs.setStatus(1);
                rs.setStatusInfo("名称已经存在");
                return rs;
            }
            model = reportDesignModelService.saveOrUpdateModel(model);
            if (model != null) {
                reportModelCacheManager.updateReportModelToCache(id, model);
            }
            rs.setData(model);
            rs.setStatus(0);
            rs.setStatusInfo(SUCCESS);
            updateRuntimeModel(model);
            logger.info("save operation successfully");
        } catch (ReportModelOperationException e) {
            logger.error(e.getMessage(), e);
            rs.setStatus(1);
            rs.setStatusInfo(e.getMessage());
        }
        logger.info("operation result is :" + rs.toString());
        return rs;
    }

    /**
     * 保存报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/json_vm", method = { RequestMethod.POST })
    public ResponseResult updateJsonVM(@PathVariable("id") String id, HttpServletRequest request) {
        ReportDesignModel model = reportModelCacheManager.getReportModel(id);
        String json = request.getParameter("json");
        String vm = request.getParameter("vm");
        ResponseResult rs = new ResponseResult();
        if (model == null) {
            logger.info("can not get model from cache with current id : " + id);
            rs.setStatus(1);
            rs.setStatusInfo("save operation failed");
            return rs;
        }
        model.setPersStatus(false);
        model.setJsonContent(json);
        model.setVmContent(vm);
        reportModelCacheManager.updateReportModelToCache(id, model);
        rs.setStatus(0);
        rs.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return rs;
    }

    /**
     * 获取报表模型使用的schema中定义的星型模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/start_models", method = { RequestMethod.GET })
    public ResponseResult getStarModels(@PathVariable("id") String id) {
        ReportDesignModel model = reportDesignModelService.getModelByIdOrName(id, false);
        ResponseResult rs = new ResponseResult();
        if (model == null) {
            logger.debug("can not get report model");
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义");
            return rs;
        }
        StarModel[] models = model.toStarModel();
        rs = this.getResult(SUCCESS, "error happend", models);
        logger.info(rs.toString());
        return rs;
    }

    /**
     * 创建区域
     * 
     * @return 返回操作结果
     */
    @RequestMapping(value = "/{id}/extend_area", method = { RequestMethod.POST })
    public ResponseResult createArea(@PathVariable("id") String reportId, HttpServletRequest request) {
        String type = request.getParameter("type");
        String referenceId = request.getParameter("referenceId");
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        List<ExtendArea> areas = ExtendAreaUtils.genereateExtendAreas(type, referenceId);
        for (ExtendArea area : areas) {
            try {
                manageService.addExtendArea(model, area);
            } catch (ReportModelOperationException e) {
                logger.error("fail add area into model! ");
                result.setStatus(1);
                result.setStatusInfo("fail add area into model! ");
                return result;
            }
        }
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("successfully create area for current report");
        result.setStatus(0);
        result.setData(areas.get(0));
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 创建区域
     * 
     * @return 返回操作结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}", method = { RequestMethod.GET })
    public ResponseResult getArea(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }

        logger.info("successfully create area for current report");
        result.setStatus(0);
        ExtendArea area = model.getExtendById(areaId);
        ExtendAreaViewObject areaVo = ResourceUtils.buildValueObject(model, area);
        result.setData(areaVo);
        result.setStatusInfo(SUCCESS);
        return result;
    }

    @RequestMapping(value = "/{reportId}/json", method = { RequestMethod.GET })
    public ResponseResult queryJson(@PathVariable("reportId") String reportId,
            HttpServletRequest request) {
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
        }
        String json = model.getJsonContent();
        return ResourceUtils.getCorrectResult("Success Getting Json of Report", json);
    }

    @RequestMapping(value = "/{reportId}/vm", method = { RequestMethod.GET })
    public ResponseResult queryVM(@PathVariable("reportId") String reportId,
            HttpServletRequest request) {
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.debug("Report model is not in cache! ", e);
        }
        if (model == null) {
            model = reportModelCacheManager.loadReportModelToCache(reportId);
        }
        ResponseResult rs = null;
        if (model == null) {
            rs = ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
            return rs;
        }
        String vm = model.getVmContent();
        rs = ResourceUtils.getCorrectResult("Success Getting VM of Report", vm);
        return rs;
    }

    /**
     * 添加条目
     * 
     * @return 操作结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item", method = { RequestMethod.POST })
    public ResponseResult addItem(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        String cubeId = request.getParameter("cubeId");
        String oLapElementId = request.getParameter("oLapElementId");
        String axisType = request.getParameter("axisType");
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        if (StringUtils.isEmpty(areaId)) {
            result.setStatus(1);
            result.setStatusInfo("area id is empty");
            return result;
        }
        ReportDesignModel model;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("no such report model in cache for report id: " + reportId, e);
            return ResourceUtils.getErrorResult("no such report model in cache for report id: " + reportId, 1);
        }
        /**
         * check whether the element exist
         */
        ExtendArea targetArea = model.getExtendById(areaId);
        /**
         * check whether new item is from the same cube
         */
        String oriCubeId = targetArea.getCubeId();
        if (StringUtils.isEmpty(oriCubeId)) {
            targetArea.setCubeId(cubeId);
            if (targetArea instanceof LiteOlapExtendArea) {
                LiteOlapExtendArea liteOlapArea = ((LiteOlapExtendArea) targetArea);
                model.getExtendById(liteOlapArea.getTableAreaId()).setCubeId(cubeId);
                model.getExtendById(liteOlapArea.getChartAreaId()).setCubeId(cubeId);
                model.getExtendById(liteOlapArea.getSelectionAreaId()).setCubeId(cubeId);
            }
        } else if (!oriCubeId.equals(cubeId)) {
            logger.error("Item from different cubes can not be added into one ExtendArea ! ");
            return ResourceUtils.getErrorResult(
                    "Item from different cubes can not be added into one ExtendArea ! ", 1);
        }
        LogicModel logicModel = targetArea.getLogicModel();
        if (logicModel == null) {
            logicModel = new LogicModel();
            targetArea.setLogicModel(logicModel);
            if (targetArea.getType() == ExtendAreaType.TIME_COMP) {
                targetArea.setLogicModel(new TimerAreaLogicModel());
            }
        }
        if (logicModel.containsOlapElement(oLapElementId, axisType.toUpperCase())) {
            return ResourceUtils.getErrorResult("该维度或者指标已经存在于区域中！", 1);
        }
        Item item = new Item();
        item.setAreaId(areaId);
        item.setCubeId(cubeId);
        item.setOlapElementId(oLapElementId);
        PositionType position = PositionType.valueOf(axisType.toUpperCase());
        item.setPositionType(position);
        ExtendArea area = model.getExtendById(areaId);
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                item.getCubeId(), item.getOlapElementId());
        if (element instanceof Dimension) {
            Dimension dim = (Dimension) element;
            if (dim.getLevels() == null || dim.getLevels().isEmpty()) {
                return ResourceUtils.getErrorResult("纬度组为空，不能作为分析维度！", 1);
            }
        }
        if (!DragRuleCheckUtils.checkIllegal(element, position, area)) {
            logger.error("Can not drag item " + oLapElementId + " to " + position);
            return ResourceUtils.getErrorResult("非法的拖拽！", 1);
        }
        try {
            model = manageService.addOrUpdateItemIntoArea(model, areaId, item, item.getPositionType());
            // 需要移到业务方法中处理，此处为临时方案 TODO 需要确认
            if (element instanceof Measure) {
                area.getFormatModel().init(element.getName());
            }
        } catch (ReportModelOperationException e) {
            logger.error("Exception when add or update item in area: " + areaId, e);
        }

        if (item.getPositionType() != PositionType.CAND_DIM && item.getPositionType() != PositionType.CAND_IND) {
            try {
                if (element instanceof Dimension) {
                    model =
                            manageService
                                    .addOrUpdateItemIntoArea(model, areaId, DeepcopyUtils.deepCopy(item),
                                            PositionType.CAND_DIM);
                } else {
                    model =
                            manageService
                                    .addOrUpdateItemIntoArea(model, areaId, DeepcopyUtils.deepCopy(item),
                                            PositionType.CAND_IND);
                }
            } catch (ReportModelOperationException e) {
                logger.error("Fail in adding or updating item into area for id: " + areaId, e);
                return ResourceUtils.getErrorResult(
                        "Fail in adding or updating item into area for id: " + areaId, 1);
            }

        }
        if (model == null) {
            result.setStatus(1);
            result.setStatusInfo("不能将该列加入到报表");
            return result;
        }
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, false, true);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("successfully add item into current area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 改变报表中指标或者维度的顺序
     * 
     * @param reportId
     * @param areaId
     * @param from 要改变的指标或维度
     * @param type 目标地址的前一个地址
     * @param to
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item_sorting",
            method = { RequestMethod.POST })
    public ResponseResult changeItemOrder(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        String source = request.getParameter("source");
        String target = request.getParameter("target");
        String type = request.getParameter("type");
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        if (StringUtils.isEmpty(areaId)) {
            result.setStatus(1);
            result.setStatusInfo("area id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        try {
            model = manageService.changeItemOrder(model, areaId, source,
                    target, PositionType.valueOf(type.toUpperCase()));
        } catch (ReportModelOperationException e) {
            logger.error("不能移动指定元素(" + source + ") from area(" + areaId + ")", e);
            return ResourceUtils.getErrorResult(e.getMessage(), 1);
        }
        if (model == null) {
            result.setStatus(1);
            result.setStatusInfo("不能移动指定列");
            return result;
        }
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, false, true);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("successfully remode item from area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 删除条目
     * 
     * @return 操作结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item/{itemId}/type/{type}",
            method = { RequestMethod.DELETE })
    public ResponseResult removeItem(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("itemId") String olapElementId,
            @PathVariable("type") String type,
            HttpServletRequest request) {

        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        if (StringUtils.isEmpty(areaId)) {
            result.setStatus(1);
            result.setStatusInfo("area id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        ExtendArea oriArea = model.getExtendById(areaId);
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                oriArea.getCubeId(), olapElementId);
        try {
            model = manageService.removeItem(model, areaId, olapElementId, PositionType.valueOf(type.toUpperCase()));
        } catch (ReportModelOperationException e) {
            logger.error("Fail in remove item(" + olapElementId + ") from area(" + areaId + ")", e);
            return ResourceUtils.getErrorResult(e.getMessage(), 1);
        }
        if (model == null) {
            result.setStatus(1);
            result.setStatusInfo("不能将该列删除");
            return result;
        }
        
        // 对于平面表，需同时删除条件轴上的信息
        if (model.getExtendById(areaId).getType() 
                == ExtendAreaType.PLANE_TABLE) {
            try {
                if (model.getPlaneTableConditions() != null &&
                        model.getPlaneTableConditions().containsKey(olapElementId)) {
                    model = manageService.removeItem(model, areaId, olapElementId, PositionType.S);                    
                    if (model == null) {
                        result.setStatus(1);
                        result.setStatusInfo("不能将该列删除");
                        return result;
                    }
                    // 删除平面表条件中的对应的item信息
                    model.getPlaneTableConditions().remove(olapElementId);
                }
            } catch (ReportModelOperationException e) {
                logger.error("Fail in remove item(" + olapElementId + ") from area(" + areaId + ")", e);
                return ResourceUtils.getErrorResult(e.getMessage(), 1);
            }
        }
        // remove condition in context

        // remove unused format define
        model.getExtendById(areaId).getFormatModel().removeItem(element.getName());
        if (model.getExtendById(areaId).getFormatModel().getDataFormat().size() == 0) {
            model.getExtendById(areaId).getFormatModel().reset();
        }
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, false, true);
        if (model.getExtendById(areaId) instanceof LiteOlapExtendArea) {
            LiteOlapExtendArea area = (LiteOlapExtendArea) model.getExtendById(areaId);
            runTimeModel.getLocalContextByAreaId(area.getChartAreaId()).reset();
            runTimeModel.getLocalContextByAreaId(area.getTableAreaId()).reset();
        }
        runTimeModel.getContext().removeParam(element.getId());
        runTimeModel.getLocalContext().values().forEach(ctx -> ctx.removeParam(element.getId()));
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("successfully remode item from area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 发布报表
     * 
     * @return 将设计态的报表发布到预览态
     */
    @RequestMapping(value = "/{id}/publish", method = { RequestMethod.POST })
    public ResponseResult publishReport(@PathVariable("id") String reportId,
            HttpServletRequest request) {
        ReportDesignModel model = this.reportDesignModelService.getModelByIdOrName(reportId, false);
        if (model == null) {
            return ResourceUtils.getErrorResult("不存在的报表，ID " + reportId, 1);
        }
        try {
            this.reportDesignModelService.publishReport(model, securityKey);
        } catch (ReportModelOperationException | DataSourceOperationException e) {
            logger.error("报表发布失败。 Report Id: " + reportId, e);
            return ResourceUtils.getErrorResult("报表发布失败。ID " + reportId, 1);
        }
        String requestUri = request.getRequestURL().toString();
        requestUri = requestUri.replace("/publish", "");
        String token = ContextManager.getProductLine();
        String publishInfo = this.getPublishInfo(requestUri, token);
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        rs.setData(publishInfo);
        return rs;
    }

    /**
     * 获取报表预览信息
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/preview_info", method = { RequestMethod.GET })
    public ResponseResult getPreviewInfo(@PathVariable("id") String reportId,
            HttpServletRequest request) {
        if (reportDesignModelService.getModelByIdOrName(reportId, true) == null) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(1);
            rs.setStatusInfo("报表没有发布，暂时不能预览");
            return rs;
        }
        String requestUri = request.getRequestURL().toString();
        requestUri = requestUri.replace("/preview_info", "");
        String token = ContextManager.getProductLine();
        String previewUrl = this.getPublishInfo(requestUri, token);
        logger.info("preview report uri is : " + previewUrl);
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        rs.setData(previewUrl);
        return rs;
    }

    /**
     * 
     * 查看发布信息
     * 
     * @param reportId reportId
     * @param request HttpServletRequest
     * @return ResponseResult
     * 
     */
    @RequestMapping(value = "/{id}/publish", method = { RequestMethod.GET })
    public ResponseResult viewPublishInfo(@PathVariable("id") String reportId,
            HttpServletRequest request) {
        if (reportDesignModelService.getModelByIdOrName(reportId, true) == null) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(0);
            rs.setStatusInfo("successfully");
            rs.setData("Not published!");
            return rs;
        }
        String requestUri = request.getRequestURL().toString();
        requestUri = requestUri.replace("/publish", "");
        String token = ContextManager.getProductLine();
        String publishInfo = this.getPublishInfo(requestUri, token);
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        rs.setData(publishInfo);
        return rs;
    }

    /**
     * 添加条目
     * 
     * @return 操作结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item/{itemId}/chart/{type}", method = { RequestMethod.POST })
    public ResponseResult updateItem(@PathVariable("id") String reportId, @PathVariable("itemId") String itemId,
            @PathVariable("areaId") String areaId, HttpServletRequest request,
            @PathVariable("type") String type) {

        ReportDesignModel model;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("no such report model in cache for report id: " + reportId);
            return ResourceUtils.getErrorResult("no such report model in cache for report id: " + reportId, 1);
        }
        /**
         * check whether the element exist
         */
        ExtendArea targetArea = model.getExtendById(areaId);
        ResponseResult result = new ResponseResult();
        if (!"COLUMN".equals(type) && !"LINE".equals(type)) {
            for (Item item : targetArea.getLogicModel().getColumns()) {
                item.getParams().put("chartType", type);
            }
            for (Item item : targetArea.getLogicModel().getSelectionMeasures().values()) {
                item.getParams().put("chartType", type);
            }
        } else {
            Item item = targetArea.getItem(itemId);
            if (item == null || item.getPositionType() == PositionType.X) {
                logger.error("can't set chart type on dimension");
                result.setStatus(1);
                result.setStatusInfo("纬度不能设置图形格式");
                return result;
            }
            item.getParams().put("chartType", type);
            try {
                model = manageService.addOrUpdateItemIntoArea(model, areaId, item, item.getPositionType());
            } catch (ReportModelOperationException e) {
                logger.error("Exception when add or update item in area: " + areaId, e);
            }
        }

        if (model == null) {
            result.setStatus(1);
            result.setStatusInfo("图形类型属性设置失败");
            return result;
        }
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, false, true);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("successfully add item into current area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 修改报表模型数据格式配置
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http servlet request
     * @return 处理结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/tooltips",
            method = { RequestMethod.POST })
    public ResponseResult updateToolTips(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        String toolTips = request.getParameter("toolTips");
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaWithToolTips(area, toolTips);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getFormatModel().getToolTips());
        result.setStatusInfo(SUCCESS);
        result.setStatus(0);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 修改报表模型数据格式配置
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http servlet request
     * @return 处理结果
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/dataformat",
            method = { RequestMethod.POST })
    public ResponseResult updateFormatDef(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId,
            HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String dataFormat = request.getParameter("dataFormat");
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaWithDataFormat(area, dataFormat);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getFormatModel().getDataFormat());
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 查看报表模型数据格式配置
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http servlet request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/tooltips",
            method = { RequestMethod.GET })
    public ResponseResult queryMeasureToolTips(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        ExtendArea area = model.getExtendById(areaId);
        result.setData(area.getFormatModel().getToolTips());
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 查看报表模型数据格式配置
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http servlet request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/dataformat",
            method = { RequestMethod.GET })
    public ResponseResult queryDataFormat(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        ExtendArea area = model.getExtendById(areaId);
        LinkedHashMap<String, String> tmp = Maps.newLinkedHashMap ();
        Item[] items = area.getLogicModel ().getColumns ();
        Map<String, String> dataFormat = area.getFormatModel ().getDataFormat ();
        String defaultFormatKey = "defaultFormat";
        tmp.put (defaultFormatKey, dataFormat.get(defaultFormatKey));
        if (items != null && items.length > 0) {
            Cube cube = model.getSchema ().getCubes ().get (area.getCubeId ());
            for (Item item : items) {
                String olapElementId = item.getOlapElementId ();
                if (cube.getMeasures() != null && cube.getMeasures().containsKey(olapElementId)) {
                    String indName = cube.getMeasures ().get (olapElementId).getName ();
                    tmp.put (indName, dataFormat.get (indName));                    
                }
            }
        }
        result.setData(tmp);
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/topn",
            method = { RequestMethod.POST })
    public ResponseResult updateMesaureTopSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String topSetting = request.getParameter(Constants.TOP);
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaWithTopSetting(area, topSetting);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getLogicModel().getTopSetting());
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/topn",
            method = { RequestMethod.GET })
    public ResponseResult getMesaureTopSettiong(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer top setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getLogicModel() == null) {
            result.setData(Maps.newHashMap());
        } else {
            result.setData(area.getLogicModel().getTopSetting());
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/othersetting",
            method = { RequestMethod.POST })
    public ResponseResult updateAreaOtherSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer top setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        String otherSetting = request.getParameter("others");
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaWithOtherSetting(area, otherSetting);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getOtherSetting());
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/othersetting",
            method = { RequestMethod.GET })
    public ResponseResult getAreaOtherSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer other setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getOtherSetting());
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }

    /**
     * 返回发布信息
     * 
     * @param requestUri 请求url
     * @param token 产品线
     * @return
     */
    private String getPublishInfo(String requestUri, String token) {
        StringBuilder publishInfoBuilder = new StringBuilder();
        publishInfoBuilder.append(requestUri);
        publishInfoBuilder.append("/report_vm");
        // modify by jiangyichao at 2014-09-28 token 加密
        String tokenEncrypt = token;
        try {
            tokenEncrypt = AesUtil.getInstance().encryptAndUrlEncoding(token, securityKey);
        } catch (Exception e) {
            throw new RuntimeException("token encrpt happen exception, please check");
        }
        // 报表访问链接接口增加_rbk属性，对于原有逻辑流程无影响
        publishInfoBuilder.append("?token=" + tokenEncrypt + "&_rbk=" + token);
        return publishInfoBuilder.toString();
    }

    /**
     * 预览前的处理
     * 
     * @param reportId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/{reportId}/prePreview", method = { RequestMethod.GET })
    public ResponseResult prePreview(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }

        ExtendArea[] extendAreas = model.getExtendAreaList();
        if (extendAreas != null) {
            for (ExtendArea extendArea : extendAreas) {
                if (extendArea.getType() == ExtendAreaType.PLANE_TABLE) {
                    result.setStatus(0);
                    Map<String, Object> data = Maps.newHashMap();
                    data.put("conditions", model.getPlaneTableConditions());
                    result.setData(data);
                    break;
                }
            }
        }
        return result;
    }

    @RequestMapping(value = "/{reportId}/preview", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult preview(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) {
        long begin = System.currentTimeMillis();
        String requestUri = request.getRequestURL().toString();
        requestUri = requestUri.replace("/preview", "");
        String token = ContextManager.getProductLine();
        String uri = this.getPublishInfo(requestUri, token);
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        rs.setData(uri + "&reportPreview=true");
        logger.info("[INFO] query vm operation successfully, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param olapElementId
     * @param type
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/params", method = { RequestMethod.POST })
    public ResponseResult addOrModifyParams(@PathVariable("id") String reportId, HttpServletRequest request) {

        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        String paramsStr = request.getParameter("params");
        if (!StringUtils.isEmpty(paramsStr)) {
            Map<String, ReportParam> params = GsonUtils.fromJson(request.getParameter("params"),
                    new TypeToken<Map<String, ReportParam>>() {
                    }.getType());
            model.setParams(params);
        }
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, false, true);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("successfully remode item from area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
//        updateRuntimeModel(model);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param olapElementId
     * @param type
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/params", method = { RequestMethod.GET })
    public ResponseResult getParams(@PathVariable("id") String reportId, HttpServletRequest request) {

        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        result.setData(model.getParams());
        result.setStatusInfo(SUCCESS);
        return result;
    }

    /**
     * 增加或修改平面表条件 add by jiangyichao at 2015-05-18, 平面表条件设置或修改
     * 
     * @return
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item/{elementId}/condition", method = { RequestMethod.POST })
    public ResponseResult addOrModifyPlaneTableCondition(@PathVariable("id") String reportId,
            @PathVariable("elementId") String elementId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        // 获取平面表条件
        String name = request.getParameter("name");
        String sqlCondition = request.getParameter("sqlCondition");
        String defaultValue = request.getParameter("defaultValue");
        // 对LIKE条件进行特殊处理
        if ("LIKE".equals(sqlCondition)) {
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
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(sqlCondition)) {
            if (StringUtils.hasText(defaultValue)) {
                // 检查输入值是否合理
                if (!PlaneTableUtils.checkSQLCondition(sqlCondition, defaultValue)) {
                    result.setStatus(1);
                    result.setStatusInfo("条件参数设置不合理，请检查！");
                    return result;
                }
            }
            PlaneTableCondition condition = new PlaneTableCondition();
            condition.setElementId(elementId);
            condition.setName(name);
            condition.setSQLCondition(SQLConditionType.valueOf(sqlCondition));
            condition.setDefaultValue(defaultValue);
            // 获取原有报表的平面表条件信息
            model.getPlaneTableConditions().put(elementId, condition);
//            Map<String, PlaneTableCondition> conditions = model.getPlaneTableConditions();
//            // 替换原有条件
//            conditions.put(elementId, condition);
//            model.setPlaneTableConditions(conditions);
        } else {
            result.setStatus(1);
            result.setStatusInfo("部分参数为空，请补充完整");
            return result;
        }
        /**
         * 配置端，在修改Item以后，需要重新初始化上下文
         */
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        runTimeModel.init(model, true, true);
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("successfully remode item from area");
        result.setStatus(0);
        result.setData(model);
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 获取平面表条件信息 add by jiangyichao at 2015-05-18，获取平面表条件信息
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item/{elementId}/condition", method = { RequestMethod.GET })
    public ResponseResult getPlaneTableConditions(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId,
            @PathVariable("elementId") String elementId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }

        ExtendArea extendArea = model.getExtendById(areaId);
        MiniCubeSchema schema = (MiniCubeSchema) model.getSchema();
        Cube cube = schema.getCubes().get(extendArea.getCubeId());

        if (model.getPlaneTableConditions() != null) {
            // 返回对应条目(elementId)的条件信息
            Map<String, Object> data = Maps.newHashMap();
            PlaneTableCondition condition = model.getPlaneTableConditions().get(elementId);
            data.put("id", elementId);
            if (condition != null) {
                data.put("name", condition.getName());
                data.put("sqlCondition", condition.getSQLCondition());
                String defaultValue = condition.getDefaultValue();
                if (defaultValue != null) {
                    data.put("defaultValue", defaultValue.replace("%", ""));
                } else {
                    data.put("defaultValue", defaultValue);
                }
                // TODO 判断是维度还是指标
                data.put("isMeasure", cube.getMeasures().containsKey(elementId));
            } else {
                data.put("isMeasure", cube.getMeasures().containsKey(elementId));
            }
            result.setStatus(0);
            result.setData(data);
            result.setStatusInfo(SUCCESS);
        } else {
            result.setStatus(1);
            result.setStatusInfo("no planeTable condition in this report design model");
        }
        return result;
    }

    /**
     * 删除平面表条件信息 add by jiangyichao at 2015-05-18，获取平面表条件信息
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/item/{elementId}/type/s", method = { RequestMethod.DELETE })
    public ResponseResult deletePlaneTableConditions(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId,
            @PathVariable("elementId") String elementId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }

        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }

        if (model.getPlaneTableConditions() != null) {
            // 返回对应条目(elementId)的条件信息
            Map<String, PlaneTableCondition> conditions = model.getPlaneTableConditions();
            if (conditions.containsKey(elementId)) {
                conditions.remove(elementId);
            }

            ExtendArea oriArea = model.getExtendById(areaId);
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                    oriArea.getCubeId(), elementId);
            try {
                model = manageService.removeItem(model, areaId, elementId, PositionType.S);
            } catch (ReportModelOperationException e) {
                logger.error("Fail in remove item(" + elementId + ") from area(" + areaId + ")", e);
                return ResourceUtils.getErrorResult(e.getMessage(), 1);
            }
            if (model == null) {
                result.setStatus(1);
                result.setStatusInfo("不能将该列删除");
                return result;
            }
            // remove condition in context

            // remove unused format define
            model.getExtendById(areaId).getFormatModel().removeItem(element.getName());
            if (model.getExtendById(areaId).getFormatModel().getDataFormat().size() == 0) {
                model.getExtendById(areaId).getFormatModel().reset();
            }
            /**
             * 配置端，在修改Item以后，需要重新初始化上下文
             */
            ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
            // 这一步没必要去清除全局上下文的条件  update by majun
            runTimeModel.init(model, false, true);
            if (model.getExtendById(areaId) instanceof LiteOlapExtendArea) {
                LiteOlapExtendArea area = (LiteOlapExtendArea) model.getExtendById(areaId);
                runTimeModel.getLocalContextByAreaId(area.getChartAreaId()).reset();
                runTimeModel.getLocalContextByAreaId(area.getTableAreaId()).reset();
            }
            runTimeModel.getContext().removeParam(element.getId());
            runTimeModel.getLocalContext().values().forEach(ctx -> ctx.removeParam(element.getId()));
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
            reportModelCacheManager.updateReportModelToCache(reportId, model);

            result.setStatus(0);
            result.setStatusInfo(SUCCESS);
        } else {
            result.setStatus(1);
            result.setStatusInfo("no planeTable condition in this report design model");
        }
        return result;
    }

    /**
     * 设置报表主题
     * 
     * @param id
     * @param type
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/theme/{type}", method = { RequestMethod.POST })
    public ResponseResult modifyTheme(@PathVariable("id") String id, @PathVariable("type") String type,
            HttpServletRequest request) {
        ReportDesignModel model = reportModelCacheManager.getReportModel(id);
        if (model == null) {
            model = reportDesignModelService.getModelByIdOrName(id, false);
        }
        if (model == null) {
            throw new RuntimeException("未查到报表定义信息");
        }
        model.setTheme(type);
        reportModelCacheManager.updateReportModelToCache(id, model);
        updateRuntimeModel(model);
        ResponseResult rs = getResult(SUCCESS, "can not get mode define info", model);
        logger.info("query operation rs is : " + rs.toString());
        return rs;
    }

    /**
     * 不保证正确性，请勿随意使用
     * 
     * @param request
     * @return String
     */
    @RequestMapping(value = "/jsonVm", method = { RequestMethod.POST })
    public String modifyJsonVm(HttpServletRequest request) {
        ReportDesignModel model = null;
        String reportId = request.getParameter("reportId");
        try {
            model = this.reportDesignModelService.getModelByIdOrName(reportId, false);
            String json = request.getParameter("jsonTxt");
            String vm = request.getParameter("vmTxt");
            if (model == null) {
                return "not get report";
            }
            if (StringUtils.isEmpty(json)) {
                return "json is null";
            } else {
                model.setJsonContent(json);
            }
            if (StringUtils.isEmpty(vm)) {
                return "vm is null";
            } else {
                model.setVmContent(vm);
            }
            reportDesignModelService.saveOrUpdateModel(model);
            updateRuntimeModel(model);
        } catch (Exception e) {
            logger.error("There are no such model in cache. Report Id: " + reportId, e);
            return "error";
        }
        return "ok";
    }

    /**
     * 查询报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/name", method = { RequestMethod.GET })
    public ResponseResult queryReportNameById(@PathVariable("id") String id, HttpServletRequest request) {
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(id);
        } catch (Exception e) {
        }
        if (model != null) {
            logger.info("get model from cache");
        } else {
            model = reportDesignModelService.getModelByIdOrName(id, false);
        }
        Map<String, String> datas = Maps.newHashMap();
        datas.put("name", model.getName());
        ResponseResult rs = getResult(SUCCESS, "can not get report name", datas);
        logger.info("query operation rs is : " + rs.toString());
        return rs;
    }

    /**
     * 查询报表模型
     * 
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/name/{name}", method = { RequestMethod.POST })
    public ResponseResult updateReportName(@PathVariable("id") String id, HttpServletRequest request,
            @PathVariable("name") String name) {
        // check name
        if (NameCheckUtils.isInvalidName(name)) {
            return ResourceUtils.getErrorResult("名称非法", ResponseResult.FAILED);
        }
        if (reportDesignModelService.isNameExist(name, id)) {
            return ResourceUtils.getErrorResult("报表名称已经存在", ResponseResult.FAILED);
        }
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(id);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        boolean modelInCache = false;
        if (model != null) {
            logger.info("get model from cache");
            modelInCache = true;
        } else {
            model = reportDesignModelService.getModelByIdOrName(id, false);
        }
        model.setName(name);
        boolean rs = reportDesignModelService.updateReportModel(model, modelInCache);
        if (rs) {
            if (modelInCache) {
                reportModelCacheManager.updateReportModelToCache(id, model);
            }
            updateRuntimeModel(model);
            return ResourceUtils.getCorrectResult("修改成功,需重新发布才能影响生产环境", null);
        }
        return ResourceUtils.getErrorResult("修改失败", 1);
    }

    @RequestMapping(value = "/{id}/extend_area/{areaId}/colorformat",
            method = { RequestMethod.POST })
    public ResponseResult updateAreaColorformat(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String colorFormat = request.getParameter(Constants.COLOR_FORMAT);
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaColorFormat(area, colorFormat);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getLogicModel().getTopSetting());
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/colorformat",
            method = { RequestMethod.GET })
    public ResponseResult getAreaColorFormat(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer top setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getFormatModel() == null) {
            result.setData(Maps.newHashMap());
        } else {
            result.setData(area.getFormatModel().getColorFormat());
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }

    @RequestMapping(value = "/{id}/extend_area/{areaId}/textAlign",
            method = { RequestMethod.POST })
    public ResponseResult updateAreaTextAlignformat(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String textAlignFormat = request.getParameter(Constants.ALIGN_FORMAT);
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaTextAlignFormat(area, textAlignFormat);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        // result.setData(area.getLogicModel().getTopSetting());
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 获取平面表分页信息
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http请求
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/pagination",
            method = { RequestMethod.GET })
    public ResponseResult getPlaneTablePaginationSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin get text align format");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        Map<String, Object> data = Maps.newHashMap();
        if (area.getPlaneTableFormat() == null) {
            data.put(Constants.PAGINATION_SETTING, Maps.newHashMap());
        } else {
            data.put(Constants.PAGINATION_SETTING, area.getPlaneTableFormat().getPageSetting());
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        result.setData(data);
        logger.info("get plane table page setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }

    @RequestMapping(value = "/{id}/extend_area/{areaId}/position",
            method = { RequestMethod.POST })
    public ResponseResult updateAreaMeasurePosition(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String positions = request.getParameter(Constants.POSITION);
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updateAreaPositionDef(area, positions);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setData(area.getLogicModel().getTopSetting());
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/position",
            method = { RequestMethod.GET })
    public ResponseResult getAreaPosition(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer top setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getFormatModel() == null) {
            result.setData(Maps.newHashMap());
        } else {
            result.setData(area.getFormatModel().getPositions());
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }

    /**
     * 修改平面表的分页信息
     * updatePlaneTablePageSetting
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/pagination",
            method = { RequestMethod.POST })
    public ResponseResult updatePlaneTablePaginationSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ResponseResult result = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            logger.debug("report id is empty");
            result.setStatus(1);
            result.setStatusInfo("report id is empty");
            return result;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        if (model == null) {
            logger.debug("can not get model with id : " + reportId);
            result.setStatus(1);
            result.setStatusInfo("不能获取报表定义 报表ID：" + reportId);
            return result;
        }
        result.setStatus(0);
        String pageSettingStr = request.getParameter(Constants.PAGINATION_SETTING);
        ExtendArea area = model.getExtendById(areaId);
        reportDesignModelService.updatePageSetting4PlaneTable(area, pageSettingStr);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        result.setStatusInfo(SUCCESS);
        updateRuntimeModel(model);
        return result;
    }

    /**
     * 获取表格文本对齐样式
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @param request http请求
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/textAlign",
            method = { RequestMethod.GET })
    public ResponseResult getAreaTextAlignFormat(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin get text align format");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        Map<String, Object> data = Maps.newHashMap();
        if (area.getFormatModel() == null) {
            data.put("indList", Maps.newHashMap());
        } else {
            data.put("indList", area.getFormatModel().getTextAlignFormat());
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        result.setData(data);
        logger.info("[INFO]query measure setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }
    
    
    protected void updateRuntimeModel(ReportDesignModel model) {
        if (model != null) {
            ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(model.getId());
            if (runtimeModel == null) {
                runtimeModel = new ReportRuntimeModel(model.getId());
            }
            runtimeModel.init(model, false);
            reportModelCacheManager.updateRunTimeModelToCache(model.getId(), runtimeModel);
        }
    }
    
    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/caption",
            method = { RequestMethod.POST })
    public ResponseResult updateChartCaptionSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer axis's caption setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getType () != ExtendAreaType.CHART) {
            throw new UnsupportedOperationException ("非图形区域，不支持操作");
        }
        String axisCaption = request.getParameter ("axisCaption");
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        if (StringUtils.isEmpty (axisCaption)) {
            return result;
        }
        Map<String, String> fromJson = AnswerCoreConstant.GSON.fromJson (
                axisCaption, new TypeToken<Map<String, String>> () {
                }.getType ());
        area.getChartFormatModel ().getSetting ().setAxisCaption (fromJson);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel (reportId);
        runtimeModel.setModel (model);
        reportModelCacheManager.updateRunTimeModelToCache (reportId, runtimeModel);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }
    
    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/caption", method = { RequestMethod.GET })
    public ResponseResult getChartCaptionSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer axis's caption setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getType () != ExtendAreaType.CHART) {
            throw new UnsupportedOperationException ("非图形区域，不支持操作");
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        result.setData (area.getChartFormatModel ().getSetting ().getAxisCaption ());
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }
    
    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/individuation",
            method = { RequestMethod.POST })
    public ResponseResult updateChartAppearanceSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info ("begin query measuer appearance setting");
        long begin = System.currentTimeMillis ();
        ResponseResult result = new ResponseResult ();
        ReportDesignModel model = reportModelCacheManager.getReportModel (reportId);
        ExtendArea area = model.getExtendById (areaId);
        if (area.getType () != ExtendAreaType.CHART) {
            throw new UnsupportedOperationException ("非图形区域，不支持操作");
        }
        String appearance = request.getParameter ("appearance");
        result.setStatus (0);
        result.setStatusInfo (SUCCESS);
        if (StringUtils.isEmpty (appearance)) {
            return result;
        }
        Map<String, Object> fromJson = AnswerCoreConstant.GSON.fromJson (
                appearance, new TypeToken<Map<String, Object>> () {
                }.getType ());
        area.getChartFormatModel ().getAppearance ().setLegend (fromJson);
        this.reportModelCacheManager.updateReportModelToCache (reportId, model);
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel (reportId);
        runtimeModel.setModel (model);
        reportModelCacheManager.updateRunTimeModelToCache (reportId, runtimeModel);
        logger.info ("[INFO]query measuer setting result {}, cose {} ms", 
                GsonUtils.toJson (result.getData ()),
                (System.currentTimeMillis () - begin));
        return result;
    }
    
    /**
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{id}/extend_area/{areaId}/individuation", method = { RequestMethod.GET })
    public ResponseResult getChartAppearanceSetting(@PathVariable("id") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        logger.info("begin query measuer appearance setting");
        long begin = System.currentTimeMillis();
        ResponseResult result = new ResponseResult();
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        ExtendArea area = model.getExtendById(areaId);
        if (area.getType () != ExtendAreaType.CHART) {
            throw new UnsupportedOperationException ("非图形区域，不支持操作");
        }
        result.setStatus(0);
        result.setStatusInfo(SUCCESS);
        Map<String, Map<String, Object>> data = Maps.newHashMap ();
        data.put ("appearance", area.getChartFormatModel ().getAppearance ().getLegend ());
        result.setData (data);
        this.reportModelCacheManager.updateReportModelToCache(reportId, model);
        logger.info("[INFO]query measuer setting result {}, cose {} ms",
                GsonUtils.toJson(result.getData()), (System.currentTimeMillis() - begin));
        return result;
    }
}
