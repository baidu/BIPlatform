
/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.regular.report.ExecuteTaskStrategy;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportParam;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportDataFileService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportExecuteTaskService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportNoticeByJmsService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportSettingService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportTaskManageService;
import com.baidu.rigel.biplatform.ma.regular.utils.RegularReportCronExpressionUtils;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.schedule.constant.ScheduleConstant;
import com.baidu.rigel.biplatform.schedule.utils.ScheduleHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

/**
 * 固定报表Resource层接口
 * 
 * @author yichao.jiang
 * @version 2015年7月29日
 * @since jdk 1.8 or after
 */
@RestController
@RequestMapping("/silkroad/reports")
public class RegularReportResource extends BaseResource {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportResource.class);

    /**
     * 报表设计服务
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;

    /**
     * 报表缓存服务
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager;

    /**
     * 固定报表数据文件服务
     */
    @Resource
    private RegularReportDataFileService regularReportDataFileService;

    /**
     * 固定报表队列任务添加
     */
    @Resource
    private RegularReportNoticeByJmsService regularReportNoticeService;

    /**
     * 固定报表设置服务
     */
    @Resource
    private RegularReportSettingService regularReportSettingService;

    /**
     * 固定报表任务管理服务
     */
    @Resource
    private RegularReportTaskManageService regularReportTaskManageService;

    /**
     * 固定报表任务执行服务
     */
    @Resource
    private RegularReportExecuteTaskService regularReportExecuteTaskService;

    /**
     * 固定报表任务失败重试次数
     */
    @Value("${biplatform.ma.regular.report.max.retry.times}")
    private String maxRetryTimesIfTaskFail;

    /**
     * 固定报表任务失败时邮件通知
     */
    @Value("${biplatform.ma.regular.report.mail.receiver}")
    private String mailReceiverIfTaskFail;

    /**
     * 固定报表任务执行
     * 
     * @param reportId 报表id
     * @param taskId 任务id
     * @return
     */
    @RequestMapping(value = "/{reportId}/executeTask", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult executeTask(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String taskId = request.getParameter(ScheduleConstant.TASK_ID);
        if (StringUtils.isEmpty(taskId)) {
            rs.setStatus(1);
            LOG.error("the task id is needed, please check!");
            rs.setStatusInfo("the task id is needed, please check!");
            return rs;
        }
        String productLineName = request.getParameter(ScheduleConstant.PRODUCT_LINE_NAME);
        ContextManager.setProductLine(productLineName);
        if (StringUtils.isEmpty(productLineName)) {
            rs.setStatus(1);
            LOG.error("the productline name is needed, please check!");
            rs.setStatusInfo("the productline name is needed, please check!");
            return rs;
        }
        LOG.info("executeTask in designer, the reportId is { " + reportId + " }, and the taskId is { " + taskId + " }");
        ReportDesignModel model;
        try {
            model = this.getReportDesignModel(reportId);
        } catch (Exception e) {
            rs.setStatus(1);
            rs.setStatusInfo(e.getMessage());
            return rs;
        }
        // 取得对应的任务信息
        RegularReportTaskInfo taskBo = model.getRegularReportTaskInfo(taskId);
        if (taskBo == null) {
            LOG.info("can't get task info with task id { " + taskId + " } in report id { " + reportId + " }");
            rs.setStatus(1);
            rs.setStatusInfo("can't get task info task with id { " + taskId + " } in report with id { " + reportId
                    + " }");
            return rs;
        }
        // 将任务添加到jms任务队列中
        regularReportNoticeService.putTaskIntoJmsQueue(taskBo);
        rs.setStatus(0);
        rs.setStatusInfo("successfully put task { " + taskId + " } into jsm queue!");
        return rs;
    }

    /**
     * 固定报表请求数据文件的接口
     * @param reportId 报表id
     * @param taskId 任务id
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{reportId}/regular/{taskId}", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult getDataJsonFile(@PathVariable("reportId") String reportId, @PathVariable("taskId") String taskId) {
        ResponseResult rs = new ResponseResult();
        ReportDesignModel reportModel;
        try {
            reportModel = this.getReportDesignModel(reportId);
        } catch (Exception e) {
            LOG.error("can get report model with id: " + reportId);
            rs.setStatus(1);
            rs.setStatusInfo("can get report model with id: " + reportId);
            return rs;
        }
        // 取得对应的任务信息
        RegularReportTaskInfo taskInfo = reportModel.getRegularReportTaskInfo(taskId);
        if (taskInfo == null) {
            LOG.info("can't get task info with task id { " + taskId + " } in report id { " + reportId + " }");
            rs.setStatus(1);
            rs.setStatusInfo("can't get task info task with id { " + taskId + " } in report with id { " + reportId
                    + " }");
            return rs;
        }
        MiniCube cube = reportModel.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        Map<String, Dimension> dimensions = cube.getDimensions();
        // 获取固定报表参数信息
        Set<RegularReportParam> regularReportParams = taskInfo.getParams();
        
        // 通过运行态模型取上下文参数
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        if (runtimeModel == null) {
            runtimeModel = new ReportRuntimeModel(reportId);
            runtimeModel.init (reportModel, true);
        }
        Map<String, Object> params = runtimeModel.getContext().getParams();
        
        // 获取权限信息
        String authority = this.genAuthorityInfo(dimensions, regularReportParams, params);
        // 获取时间参数
        String time = this.genTimeInfo(dimensions, params);
        // 读取数据文件
        String resultStr = regularReportDataFileService.readRegularReportDataFile(reportId, taskId, authority, time); 
        try {
            JSONObject obj = new JSONObject(resultStr);
            Map<String, Object> map = new ObjectMapper().readValue(obj.toString(), Map.class);
            rs.setStatus(0);
            rs.setData(map);
            rs.setStatusInfo("read data json file successfully!");
            return rs;
        } catch (Exception e) {
            LOG.error("fail to read data json file", e.fillInStackTrace());
            rs.setStatus(1);
            rs.setStatusInfo("fail to read data json file");
            return rs;
        }
    }

    
    /**
     * 获取某个报表中的所有参数
     * 
     * @param reportId
     * @return
     */
    @RequestMapping(value = "/{reportId}/allparams", method = { RequestMethod.GET })
    public ResponseResult getAllParamsOfReport(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String taskId = request.getParameter("taskId");
        Set<RegularReportParam> params = Sets.newHashSet();
        if (StringUtils.isEmpty(taskId)) {
            params = regularReportSettingService.getAllParamsOfReportModel(reportId);
        } else {
            params = regularReportSettingService.getAllParamsOfReguarTask(reportId, taskId);
        }
        rs.setStatus(0);
        rs.setData(params);
        rs.setStatusInfo("successfully get all params in report model");
        return rs;
    }

    /**
     * 保存或者更新固定报表
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/regular/task", method = { RequestMethod.POST })
    public ResponseResult updateRegularReport(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        try {
            // 获取固定报表设置信息
            // 固定报表任务名称
            String taskName = request.getParameter("taskName");
            if (StringUtils.isEmpty(taskName)) {
                rs.setStatus(1);
                rs.setStatusInfo("the taskName can't be empty, please input!");
                return rs;
            }
            // 判断名称是否存在
            if (regularReportSettingService.isTaskNameExists(reportId, taskName)) {
                rs.setStatus(1);
                rs.setStatusInfo("该名称[ " + taskName + "]已经存在，请检查!");
                return rs;
            }
            String taskId = request.getParameter("taskId");
            boolean isRunNow = Boolean.valueOf(request.getParameter("isRunNow"));
            RegularReportTaskInfo taskBo = this.taskInfoBuilder(request, reportId, taskName);
            // 如果任务id为空，则调用任务提交服务
            if (taskId == null) {
                if (isRunNow) {
                    // 产生任务id
                    taskId = ScheduleHelper.generateTaskId(taskBo);
                    // 设置任务id
                    taskBo.setTaskId(taskId);
                    // 保存报表
                    if (!this.saveRegularReportSetting(reportId, taskBo)) {
                        throw new RuntimeException("fail execute task right now");
                    }
                } else {
                    taskId = regularReportTaskManageService.submitRegularTaskToSchedule(taskBo);
                    if (taskId == null) {
                        throw new RuntimeException("fail submit task to schedule module");
                    }
                }
            } else {
                if (isRunNow) {
                    // 设置任务id
                    taskBo.setTaskId(taskId);
                    // 保存报表
                    if (!this.saveRegularReportSetting(reportId, taskBo)) {
                        throw new RuntimeException("fail execute task right now");
                    }
                } else {
                    // 如果任务id不为空，则调用更新任务服务
                    taskId = regularReportTaskManageService.updateRegularTaskToSchedule(taskBo);
                    if (taskId == null) {
                        throw new RuntimeException("fail update task to schedule module");
                    }
                }
            }
            // 设置任务id
            taskBo.setTaskId(taskId);
            ReportDesignModel model = regularReportSettingService.saveRegularReportSetting(reportId, taskBo);
            if (model == null) {
                throw new RuntimeException("can't get report model with report id: " + reportId);
            }
            // 将报表更新到缓存
            reportModelCacheManager.updateReportModelToCache(reportId, model);
            // 保存报表
            reportDesignModelService.saveOrUpdateModel(model);
        } catch (Exception e) {
            LOG.error("the task failed to submit", e.getMessage());
            rs.setStatus(1);
            rs.setStatusInfo("the task failed to submit");
            return rs;
        }
        rs.setStatus(0);
        rs.setStatusInfo("successfully get all params in report model");
        return rs;
    }

    /**
     * 根据请求，构建任务信息
     * @param reqeust
     * @return
     */
    private RegularReportTaskInfo taskInfoBuilder(HttpServletRequest request, String reportId, String taskName) {
     // 获取固定报表参数，并对其进行处理
        String paramStr = request.getParameter("params");
        Set<RegularReportParam> paramSet = Sets.newHashSet();
        if (!StringUtils.isEmpty(paramStr)) {
            paramSet = GsonUtils.fromJson(paramStr, new TypeToken<Set<RegularReportParam>>() {
            }.getType());
        }
        // 固定报表任务是否立即执行
        boolean isRunNow = Boolean.valueOf(request.getParameter("isRunNow"));
        // 任务执行策略
        String executeStrategyStr = request.getParameter("executeStrategy");
        ExecuteTaskStrategy executeTaskStrategy = GsonUtils.fromJson(executeStrategyStr, ExecuteTaskStrategy.class);
        // 构建任务实体
        RegularReportTaskInfo taskBo = new RegularReportTaskInfo();
        // 设置报表id
        taskBo.setReportId(reportId);
        // 设置参数名称
        taskBo.setTaskName(taskName);
        // 设置是否立即执行
        taskBo.setIsRunNow(isRunNow);
        // 设置参数信息
        taskBo.setParams(paramSet);
        // 设置产品线信息
        taskBo.setProductLineName(ContextManager.getProductLine());
        // 根据执行策略，产生对应的cron表达式
        taskBo.setCronExpression(RegularReportCronExpressionUtils
                .genCronExpression4RegularReport(executeTaskStrategy));
        // 设置执行策略
        taskBo.setExecuteStrategy(executeTaskStrategy);
//        // TODO 为方便调试，临时设置为每一分钟触发一次
//        taskBo.setCronExpression("00 0/1 * * * ? ");
        taskBo.setExcuteAction("silkroad/reports/" + reportId + "/executeTask");
        return taskBo;
    }
    /**
     * 删除固定报表
     * 
     * @param reportId
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/regular/tasks/{taskId}", method = { RequestMethod.DELETE })
    public ResponseResult deleteRegularReport(@PathVariable("reportId") String reportId,
            @PathVariable("taskId") String taskId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        ReportDesignModel model;
        try {
            model = this.getReportDesignModel(reportId);
        } catch (Exception e) {
            rs.setStatus(1);
            rs.setStatusInfo(e.getMessage());
            return rs;
        }
        // 获取任务信息
        Map<String, RegularReportTaskInfo> tasks = model.getRegularTasks();
        if (tasks == null || tasks.size() == 0 || !tasks.containsKey(taskId)) {
            LOG.info("can't get task info with task id { " + taskId + " } in report with id { " + reportId + " }");
            rs.setStatus(1);
            rs.setStatusInfo("can't get task info task with id { " + taskId + " } in report with id { " + reportId
                    + " }");
            return rs;
        }
        // 移除任务id对应的任务
        RegularReportTaskInfo taskBo = tasks.remove(taskId);
        // 设置任务信息
        model.setRegularTasks(tasks);
        try {
            // 重新保存报表
            reportDesignModelService.saveOrUpdateModel(model);
            reportModelCacheManager.updateReportModelToCache(reportId, model);
        } catch (ReportModelOperationException e) {
            // 重新设置任务信息
            tasks.put(taskId, taskBo);
            model.setRegularTasks(tasks);
            LOG.error("fail to delete task with id { " + taskId + " }");
            rs.setStatus(1);
            rs.setStatusInfo("fail to delete task with id { " + taskId + " }");
            return rs;
        }
        rs.setStatus(0);
        rs.setStatusInfo("successfully delete task");
        return rs;
    }

    /**
     * 获取固定报表信息
     * 
     * @param reportId
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/regular/tasks/{taskId}", method = { RequestMethod.GET })
    public ResponseResult getRegularReport(@PathVariable("reportId") String reportId,
            @PathVariable("taskId") String taskId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        RegularReportTaskInfo taskBo = regularReportSettingService.getRegularReportSetting(reportId, taskId);
        if (taskBo == null) {
            rs.setStatus(1);
            rs.setStatusInfo("can't get task model in report[ " + reportId + " ] with task id [ " + taskId + "]");
            return rs;
        }
        // 构建返回数据集
        Map<String, Object> datas = Maps.newHashMap();
        // 名称信息
        datas.put("taskName", taskBo.getTaskName());
        // id信息
        datas.put("taskId", taskBo.getTaskId());
        // 参数信息
        // datas.put("params", taskBo.getParams());
        // 是否立即运行信息
        datas.put("isRunNow", taskBo.getIsRunNow());
        // 任务执行策略信息
        datas.put("executeStrategy", taskBo.getExecuteStrategy());
        rs.setStatus(0);
        rs.setData(datas);
        return rs;
    }

    /**
     * 获取固定报表任务列表
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/regular/tasks")
    public ResponseResult getRegulaReportLists(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String host = request.getServerName();
        String port = String.valueOf(request.getServerPort());
        String hostName = StringUtils.isEmpty(port)? host : host + ":" + port;
        List<RegularReportTaskInfo> tasks = regularReportSettingService.getRegularReportSetting(reportId);
        List<Map<String, String>> tasksInfoReturn = Lists.newArrayList();
        tasks.forEach(k -> {
            tasksInfoReturn.add(this.covRegularTaskBo2Map(k, hostName));
        });
        rs.setStatus(0);
        rs.setData(tasksInfoReturn);
        rs.setStatusInfo("successfully get regular report task lists");
        return rs;
    }

    /**
     * 
     * @param reportId
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/regular/task/{taskId}/operation/{operationId}",
            method = { RequestMethod.POST })
    public ResponseResult operationTask(@PathVariable("reportId") String reportId,
            @PathVariable("taskId") String taskId, @PathVariable("operationId") String operationId,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        switch (operationId) {
            case "0":
                // 任务启动
                break;
            case "1":
                // 任务暂停
                break;
            case "2":
                // 任务终止
                break;
            default:
                throw new UnsupportedOperationException("the current task operation is not supported");
        }
        return rs;
    }

    /**
     * 处理下拉树请求
     * 
     * @param reportId
     * @param paramId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/params/{paramId}/tree")
    public List<Map<String, Object>> handler4ParamTree(@PathVariable("reportId") String reportId,
            @PathVariable("paramId") String paramId, HttpServletRequest request) {
        ReportDesignModel model ;
        try {
            model = this.getReportDesignModel(reportId);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.fillInStackTrace());
            throw new RuntimeException(e.getMessage());
        }
       
        Map<String, String> requestParams = this.getParamsOfReport(model);
        // 添加cookie信息
        requestParams.put(HttpRequest.COOKIE_PARAM_NAME, request.getHeader("Cookie"));
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String taskId = request.getParameter("taskId");
        // 获取树形结构
        List<Map<String, Object>> treeParam =
                regularReportSettingService.getParamTreeOfRegularReport(reportId, taskId, paramId, id, name,
                        requestParams);
        return treeParam;
    }

    /**
     * 获取固定报表的vm文件
     * @param reportId
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping( value = "/{reportId}/regular/{taskId}/report_vm",
            method = {RequestMethod.GET, RequestMethod.POST}, produces = "text/html;charset=utf-8")
    public String getRegularReporVm(@PathVariable("reportId") String reportId, 
            @PathVariable("taskId") String taskId, HttpServletRequest request, HttpServletResponse response) {
        ResponseResult rs = new ResponseResult();
        ReportDesignModel model;
        try {
            model = this.getReportDesignModel(reportId);
            Map<String, RegularReportTaskInfo> tasks = model.getRegularTasks();
            if (tasks == null || tasks.size() == 0 || !tasks.containsKey(taskId)) {
                // TODO 考虑此种情况下，是否需要对任务存在进行验证
                LOG.info("can't get task info with task id { " + taskId + " } in report with id { " 
                    + reportId + " }");
                rs.setStatus(1);
                rs.setStatusInfo("can't get task info task with id { " + taskId + " } in report with id { " + reportId
                    + " }");
            }
            ReportRuntimeModel runtimeModel = null;
            if (model != null) {
                runtimeModel = new ReportRuntimeModel(reportId);
                runtimeModel.init (model, true);
            }
            // 将url参数添加到全局上下文中
            Enumeration<String> params = request.getParameterNames();
            // 请求参数
            Map<String, String> requestParams = Maps.newHashMap();
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                if (request.getParameter (paramName) != null) {
                    runtimeModel.getContext().put(paramName, request.getParameter(paramName));
                    requestParams.put(paramName, request.getParameter(paramName));
                }
            }
            // 添加cookie内容
            runtimeModel.getContext().put(HttpRequest.COOKIE_PARAM_NAME, request.getHeader("Cookie"));
            
            /**
             * 依据查询请求，根据报表参数定义，增量添加报表区域模型参数
             */
            Map<String, Object> tmp = QueryUtils.resetContextParam(request, model);
            runtimeModel.getContext().getParams().putAll(tmp);
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runtimeModel);
            StringBuilder builder = buildVMString(reportId, request, response, model);
            return builder.toString();
        } catch (Exception e) {
            LOG.error("can't get report with id {" + reportId + "}");
            throw new RuntimeException();
        }
    }
    
    /**
     * 获取固定报表的JSON文件
     * @param reportId
     * @param taskId
     * @param request
     * @return
     */
    @RequestMapping( value = "/{reportId}/report_json1", 
            method = {RequestMethod.GET, RequestMethod.POST}, produces = "text/plain;charset=utf-8" )
    public String getRegularReporJson(@PathVariable("reportId") String reportId, 
            HttpServletRequest request, HttpServletResponse response) {
        long begin = System.currentTimeMillis();
        ReportDesignModel model = null;
        try {
            model = this.getReportDesignModel(reportId);
            String json = model.getJsonContent();
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonArrays = jsonObj.getJSONArray("entityDefs");
            for(int i = 0; i < jsonArrays.length(); i++) {
                JSONObject value = jsonArrays.getJSONObject(i);
                if (value.get("clzKey") != null && value.get("clzKey").toString().equals("DI_FORM")) {
                    value.put("reportType", "REGULAR");
                    break;
                }
            }
            LOG.info(json);
            response.setCharacterEncoding("utf-8");
            LOG.info("[INFO] query json operation successfully, cost {} ms", (System.currentTimeMillis() - begin));
            return jsonObj.toString();
        } catch (Exception e) {
            LOG.info("[INFO]--- ---There are no such model in cache. Report Id: " + reportId, e);
            throw new IllegalStateException();
        }
    }
    
    /**
     * 根据报表id获取报表模型
     * @param reportId 报表id
     * @return 报表模型
     * @throws 获取报表过程中的异常信息
     */
    private ReportDesignModel getReportDesignModel(String reportId) throws Exception {
        // 先从缓存中取报表模型
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (Exception e) {
            if (model == null) {
                // 设计端先取本地未发布的
                model = reportDesignModelService.getModelByIdOrName(reportId, false);
                if (model == null) {
                    // 然后取已经发布的
                    model = reportDesignModelService.getModelByIdOrName(reportId, true);
                }
                // 如果找不到报表，则报异常
                if (model == null) {
                    throw new RuntimeException("can't get report model with id { " + reportId + " }");
                }
            }
        }
        return model;
    }

    /**
     * 将固定报表任务转换到前端所需的形式
     * @param taskBo
     * @param hostAddress
     * @return
     */
    private Map<String, String> covRegularTaskBo2Map(RegularReportTaskInfo taskBo, String hostAddress) {
        Map<String, String> taskMap = Maps.newHashMap();
        taskMap.put("taskId", taskBo.getTaskId());
        taskMap.put("taskName", taskBo.getTaskName());
        taskMap.put("statusInfo", taskBo.getExecuteStatus().getStatus());
        String reportId = taskBo.getReportId();
        String taskId = taskBo.getTaskId();
        String productLine = taskBo.getProductLineName();
        String productLineEncrypt = productLine;
        try {
            productLineEncrypt = AesUtil.getInstance().encryptAndUrlEncoding(productLine, securityKey);
        } catch (Exception e) {
            LOG.warn(e.getMessage());
        }
        String regularReportUrl = "http://" + hostAddress + "/silkroad/reports/" + reportId + "/regular/" + taskId + "/report_vm?token=" + productLineEncrypt;
        taskMap.put("taskUrl", regularReportUrl);
        return taskMap;
    }

    /**
     * 对立即执行的任务保存设置信息
     * @param reportId
     * @param taskBo
     * @return
     */
    private boolean saveRegularReportSetting(String reportId, RegularReportTaskInfo taskBo) {
        ReportDesignModel model = regularReportSettingService.saveRegularReportSetting(reportId, taskBo);
        if (model == null) {
            throw new RuntimeException("can't get report model with report id: " + reportId);
        }
        // 将报表更新到缓存
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        // 保存报表
        try {
            reportDesignModelService.saveOrUpdateModel(model);
            boolean result =
                    regularReportExecuteTaskService.executeTask(reportId, taskBo.getTaskId(),
                            ContextManager.getProductLine());
            if (!result) {
                throw new RuntimeException("fail to execute task right now!");
            }
            return true;
        } catch (ReportModelOperationException e) {
            return false;
        }
    }
    
    /**
     * 获取报表模型中的参数
     * @param model
     * @return
     */
    private Map<String, String> getParamsOfReport(ReportDesignModel model) {
        Map<String, ReportParam> params = model.getParams();
        Map<String, String> newParams = Maps.newHashMap();
        params.forEach((k, v) -> {
            newParams.put(v.getName(), v.getDefaultValue());
            newParams.put(v.getElementId(), v.getDefaultValue());
        });
        // TODO 考虑是否放入平面表参数，以及平面表条件
        Map<String, PlaneTableCondition> conditions = model.getPlaneTableConditions();
        conditions.forEach((k, v) -> {
           newParams.put(v.getName(), v.getDefaultValue()); 
           newParams.put(v.getElementId(), v.getDefaultValue());
        });
        return newParams;
    }
    
    /**
     * TODO后续考虑同queryDataResource合并
     * @param reportId
     * @param response
     * @param model
     * @return StringBuilder
     */
    private StringBuilder buildVMString(String reportId, HttpServletRequest request, HttpServletResponse response,
            ReportDesignModel model) {
        // TODO 临时方案，以后前端做
        String vm = model.getVmContent();
        String imageId = request.getParameter ("reportImageId");
        String js =
                "<script type='text/javascript'>" + "\r\n" + "        (function(NS) {" + "\r\n"
                        + "            NS.xui.XView.start(" + "\r\n"
                        + "                'di.product.display.ui.LayoutPage'," + "\r\n" + "                {" + "\r\n"
                        + "                    externalParam: {" + "\r\n" + "                    'reportId':'"
                        + (StringUtils.isEmpty (imageId) ? reportId : imageId)
                        + "','phase':'dev'},"
                        + "\r\n"
                        + "                    globalType: 'PRODUCT',"
                        + "\r\n"
                        + "                    diAgent: '',"
                        + "\r\n"
                        + "                    reportId: '"
                        + (StringUtils.isEmpty (imageId) ? reportId : imageId)
                        + "',"
                        + "\r\n"
                        + "                    webRoot: '/silkroad',"
                        + "\r\n"
                        + "                    phase: 'dev',"
                        + "\r\n"
                        + "                    serverTime: ' "
                        + System.currentTimeMillis ()
                        + "',"
                        + "\r\n"
                        + "                    funcAuth: null,"
                        + "\r\n"
                        + "                    extraOpt: (window.__$DI__NS$__ || {}).OPTIONS"
                        + "\r\n"
                        + "                }"
                        + "\r\n"
                        + "            );"
                        + "\r\n"
                        + "        })(window);"
                        + "\r\n"
                        + "    </script>" + "\r\n";
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>");
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<title>" + model.getName() + "</title>");
        builder.append("<meta content='text/html' 'charset=UTF-8'>");
        final String theme = model.getTheme();
        builder.append("<link rel='stylesheet' href='/silkroad/asset/" + theme + "/css/-di-product-min.css'/>");
        builder.append("<script src='/silkroad/dep/jquery-1.11.1.min.js'/></script>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append(vm);

        builder.append("<script src='/silkroad/asset/" + theme + "/-di-product-debug.js'>");
        builder.append("</script>");
        builder.append(js);
        builder.append("</body>");
        builder.append("</html>");
        response.setCharacterEncoding("utf-8");
        return builder;
    }
    
    /**
     * 获取当前查询中的权限信息
     * @param dimensions
     * @param regularParams
     * @param params
     * @return
     */
    private String genAuthorityInfo(Map<String, Dimension> dimensions, 
            Set<RegularReportParam> regularParams, Map<String, Object> params) {
        if (regularParams == null || regularParams.size() == 0) {
            return null;
        }
        // 对参数进行判断，找到对应的值
        for (RegularReportParam regularParam : regularParams) {
            if (dimensions != null && dimensions.size() != 0 &&
                    dimensions.containsKey(regularParam.getParamId()) &&
                        this.isCallbackDim(dimensions.get(regularParam.getParamId()))) {
                Dimension callbackDim = dimensions.get(regularParam.getParamId());
                if (params != null && params.size() != 0 && params.containsKey(regularParam.getParamId())) {
                    Object valObj = params.get(regularParam.getParamId());
                    if (valObj instanceof String) {
                        String valStr = (String) valObj;
                        if (valStr.contains(",")) {
                            // TODO 仅取第一个，后续考虑
                            return MetaNameUtil.makeUniqueName(callbackDim, valStr.split(",")[0]);
                        } else {
                            return MetaNameUtil.makeUniqueName(callbackDim, valStr);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 获取上下文中的时间参数
     * @param dimensions
     * @param params
     * @return
     */
    private String genTimeInfo(Map<String, Dimension> dimensions, Map<String, Object> params) {
        if (params == null || params.size() == 0 ) {
            return null;
        }
        for (String key : params.keySet()) {
            Object obj = params.get(key);
            if (dimensions != null && dimensions.size() != 0 && dimensions.containsKey(key) 
                    && dimensions.get(key).isTimeDimension()) {
                if (obj instanceof String) {
                    String val = (String) obj;
                    if (val.contains(",")) {
                        return val.split(",")[0];
                    } else {
                        return val;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 判断是否为callback维度
     * @param dim
     * @return
     */
    private boolean isCallbackDim(Dimension dim) {
        if (dim == null) {
            return false;
        }
        Level level = dim.getLevels().values().toArray(new Level[0])[0];
        return level != null && level.getType() == LevelType.CALL_BACK;
    }
}
