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

package com.baidu.rigel.biplatform.ma.regular.service.impl;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.regular.report.ExecuteTaskStrategy;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportParam;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportDataFileService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportExecuteTaskService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportQueryService;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportSettingService;
import com.baidu.rigel.biplatform.ma.regular.utils.RegularReportDataFileUtils;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 固定报表任务执行实现类，同时作为任务队列的观察者，当有任务需要执行时运行
 * 
 * @author yichao.jiang
 * @version 2015年7月30日
 * @since jdk 1.8 or after
 */
@Service("regularReportExecuteTaskService")
public class RegularReportExecuteTaskServiceImpl implements RegularReportExecuteTaskService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportExecuteTaskServiceImpl.class);

    /**
     * TODO，统一移动该变量的位置，数据JSON的文件名
     */
    private static final String DATA_JSON_FILE_NAME = "data_json.js";

    /**
     * 单个任务线程池大小，可通过配置文件修改
     */
    private static int TASK_THREAD_POOL_SIZE = 5;

    /**
     * 注入
     * 
     * @param taskThreadPoolSize
     */
    @Value("${biplatform.ma.schedule.task.thread.pool.size}")
    public void setTaskThreadPoolSize(int taskThreadPoolSize) {
        RegularReportExecuteTaskServiceImpl.TASK_THREAD_POOL_SIZE = taskThreadPoolSize;
    }

    /**
     * 当数据文件写入失败时，最多的重试次数
     */
    private static int MAX_TRY_TIMES_WHEN_WRITE_FAILED = 5;

    /**
     * 注入
     * 
     * @param maxTryTimesWhenFailed
     */
    @Value("${biplatform.ma.schedule.task.max.try.times.when.write.failed}")
    public void setMaxTryTimesWhenWriteFailed(int maxTryTimesWhenFailed) {
        RegularReportExecuteTaskServiceImpl.MAX_TRY_TIMES_WHEN_WRITE_FAILED = maxTryTimesWhenFailed;
    }

    /**
     * 获取数据线程池
     */
    private static ExecutorService GEN_DATA_TASK_POOL = Executors.newFixedThreadPool(TASK_THREAD_POOL_SIZE);

    /**
     * 保存数据线程池
     */
    private static ExecutorService SAVE_DATA_TASK_POOL = Executors.newFixedThreadPool(TASK_THREAD_POOL_SIZE);

    /**
     * 密钥信息
     */
    @Value("${biplatform.ma.ser_key}")
    protected String securityKey;

    /**
     * 报表服务
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;

    /**
     * 固定报表文件服务
     */
    @Resource
    private RegularReportDataFileService regularReportDataFileService;

    /**
     * 文件服务
     */
    @Resource
    private FileService fileService;

    /**
     * 数据源服务
     */
    @Resource
    private DataSourceService dsService;

    /**
     * 固定报表查询服务
     */
    @Resource
    private RegularReportQueryService regularReportQueryService;

    /**
     * 固定报表设置服务
     */
    @Resource
    private RegularReportSettingService regularReportSettingService;

    /**
     * 构造函数
     */
    public RegularReportExecuteTaskServiceImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeTask(String reportId, String taskId, String productLine) {
        // 设置产品线信息
        ContextManager.cleanProductLine();
        ContextManager.setProductLine(productLine);
        // 当前固定报表的所有权限
        List<String> authoritys = regularReportSettingService.getAuthoritysOfRegularReport(reportId, taskId);
        // 如果没有权限信息
        if (authoritys == null || authoritys.size() == 0) {
            return this.executeTaskInSingleThread(reportId, taskId, null);
        } else if (authoritys.size() == 1) {
            // 仅包含一个权限信息
            return this.executeTaskInSingleThread(reportId, taskId, authoritys.get(0));
        } else {
            // 此时存在多个权限岗位，需要启动多线程处理
            // 多线程处理数据查询和转换
            Map<String, String> dataJsons =
                    this.multiThread4QueryAndConvertDataJson(reportId, taskId, authoritys, productLine);
            // 如果此线程处理结果为null，则表明获取数据阶段出现错误，直接返回false
            if (dataJsons == null || dataJsons.size() == 0) {
                return false;
            }
            // MAX_TRY_TIMES_WHEN_SAVE_FAIL
            int maxRetryTimes = MAX_TRY_TIMES_WHEN_WRITE_FAILED;
            // 如果前一个线程执行完毕，则需要继续执行文件存储的线程
            boolean saveDataResult =
                    this.multiThread4SaveDataJson(reportId, taskId, authoritys, dataJsons, productLine);
            int tryTimes = 1;
            // 如果任务执行失败，并且任务执行次数小于重试次数最大值
            while (!saveDataResult && tryTimes <= maxRetryTimes) {
                saveDataResult = this.multiThread4SaveDataJson(reportId, taskId, authoritys, dataJsons, productLine);
                tryTimes = tryTimes + 1;
            }
            return saveDataResult;
        }
    }

    /**
     * 多线程处理数据查询和转换
     * 
     * @param reportId
     * @param taskId
     * @param authoritys
     * @return
     */
    private Map<String, String> multiThread4QueryAndConvertDataJson(String reportId, String taskId,
            List<String> authoritys, String productLine) {
        // 查询数据结果
        List<Future<Map<String, String>>> genDataTaskFutures = Lists.newArrayList();
        int authoritySize = authoritys.size();
        for (int i = 0; i < authoritySize; i++) {
            // 初始化查询数据任务
            RegularReportGenDataJSONTask genDataGsonTask =
                    new RegularReportGenDataJSONTask(reportId, taskId, authoritys.get(i), productLine);
            // 添加任务
            Future<Map<String, String>> future = GEN_DATA_TASK_POOL.submit(genDataGsonTask);
            // 添加执行结果
            genDataTaskFutures.add(future);
        }
        // 关闭线程池
        // GEN_DATA_TASK_POOL.shutdown();
        // 查询并转为JSON字符串的线程处理结果
        Map<String, String> genDataResults = Maps.newHashMap();
        // 处理查询数据和数据转换线程的执行结果
        for (Future<Map<String, String>> future : genDataTaskFutures) {
            try {
                Map<String, String> result = future.get();
                genDataResults.putAll(result);
            } catch (Exception e) {
                LOG.error("query data and convert 2 json happend exception : " + e.getMessage());
                return null;
            }
        }
        return genDataResults;
    }

    /**
     * 多线程处理JSON数据文件写入
     * 
     * @param reportId
     * @param taskId
     * @param authoritys
     * @param dataJsons
     * @param productLine
     * @return
     */
    private boolean multiThread4SaveDataJson(String reportId, String taskId, List<String> authoritys,
            Map<String, String> dataJsons, String productLine) {
        // 保存数据文件的执行结果
        List<Future<Map<String, Boolean>>> saveDataTaskFutures = Lists.newArrayList();
        // 权限个数
        int authoritySize = authoritys.size();
        for (int i = 0; i < authoritySize; i++) {
            // 初始化保存JSON数据文件任务
            RegularReportSaveDataJSONTask saveDataGsonTask =
                    new RegularReportSaveDataJSONTask(reportId, taskId, authoritys.get(i), dataJsons.get(authoritys
                            .get(i)), productLine);
            // 添加任务
            Future<Map<String, Boolean>> future = SAVE_DATA_TASK_POOL.submit(saveDataGsonTask);
            // 添加执行结果
            saveDataTaskFutures.add(future);
        }
        // 关闭线程池
        // SAVE_DATA_TASK_POOL.shutdown();
        // 处理结果
        boolean saveDataJsonResult = true;
        // 对结果进行处理
        for (Future<Map<String, Boolean>> future : saveDataTaskFutures) {
            try {
                Boolean tmpResult = future.get().values().toArray(new Boolean[0])[0];
                if (!tmpResult) {
                    saveDataJsonResult = false;
                }
            } catch (Exception e) {
                LOG.error("save data, happend exception : " + e.getMessage());
                saveDataJsonResult = false;
            }
        }
        return saveDataJsonResult;
    }

    /**
     * 执行回滚操作
     * 
     * @param reportId
     * @param taskId
     * @return
     */
    private boolean executeRollBack(String reportId, String taskId) {
        // 获取任务权限
        List<String> authoritys = regularReportSettingService.getAuthoritysOfRegularReport(reportId, taskId);
        if (authoritys == null || authoritys.size() == 0) {
            String filePath = RegularReportDataFileUtils.genDataFilePath4RegularReport(reportId, taskId, null, null);
            filePath = filePath + File.separator + DATA_JSON_FILE_NAME;
            try {
                return fileService.rm(filePath);
            } catch (FileServiceException e) {
                LOG.error("happend exception in rollback stage, ", e.getMessage());
                return false;
            }
        } else {
            // 多个岗位
            for (String authority : authoritys) {
                String filePath =
                        RegularReportDataFileUtils.genDataFilePath4RegularReport(reportId, taskId, authority, null);
                filePath = filePath + File.separator + DATA_JSON_FILE_NAME;
                try {
                    if (!fileService.rm(filePath)) {
                        return false;
                    }
                } catch (FileServiceException e) {
                    LOG.error("happend exception in rollback stage, ", e.getMessage());
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 在单一线程中处理
     * 
     * @param reportId
     * @param taskId
     * @param authority
     * @return
     */
    private boolean executeTaskInSingleThread(String reportId, String taskId, String authority) {
        ReportDesignModel reportModel;
        try {
            reportModel = this.getDesignModelAccordingReportId(reportId);
        } catch (Exception e) {
            return false;
        }
        // 获取数据源信息
        DataSourceInfo dsInfo = this.getDsInfoFromDsId(reportModel.getDsId());
        // 如果数据源信息为空，则返回false
        if (dsInfo == null) {
            return false;
        }
        // STEP 1: 获取数据，调用接口，获取数据信息，key为areaId,value为该区域对应的数据模型DataModel
        // 获取任务信息
        RegularReportTaskInfo taskBo = regularReportSettingService.getRegularReportSetting(reportId, taskId);
        // 参数转换
        Map<String, String> params = this.convertParams2String(taskBo.getParams(), reportModel);
        // TODO 时间参数
        params.putAll(this.getTimeDimParamsOfReport(reportModel, taskBo));
        // 数据查询
        Map<String, DataModel> dataModels = regularReportQueryService.queryReportData(dsInfo, reportModel, params);
        // 转为JSON
        String dataJson = regularReportDataFileService.convertData2Json(reportModel, dataModels);
        // STEP 2: 保存该固定报表
        return regularReportDataFileService.saveRegularReportDataFile(reportId, taskId, authority, dataJson);
    }

    /**
     * 将固定报表的参数信息转为字符串
     * 
     * @param oriParams
     * @return
     */
    private Map<String, String> convertParams2String(Set<RegularReportParam> oriParams, ReportDesignModel model) {
        MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        Map<String, Dimension> dimensions = cube.getDimensions();
        Map<String, String> params = Maps.newHashMap();
        oriParams.forEach(k -> {
            String paramName = k.getParamName();
            Map<String, Object> idAndValue = k.getParamValue();
            if (dimensions != null && dimensions.size() != 0 && dimensions.containsKey(k.getParamId())) {
                Dimension dim = dimensions.get(k.getParamId());
                Object value = "";
                if (this.isCallback(dim)) {
                    value = idAndValue.get("id");
                } else {
                    value = idAndValue.get("name");
                }
                if (value instanceof String) {
                    String valueStr = (String) value;
                    if (MetaNameUtil.isUniqueName(valueStr)) {
                        String[] tmp = MetaNameUtil.parseUnique2NameArray(valueStr);
                        valueStr = tmp[tmp.length - 1];
                    }
                    params.put(paramName, valueStr);
                }
            }
        });
        return params;
    }

    /**
     * 判断某个纬度是否为callback纬度
     * 
     * @param dim
     * @return
     */
    private boolean isCallback(Dimension dim) {
        if (dim == null) {
            return false;
        }
        Level level = dim.getLevels().values().toArray(new Level[0])[0];
        return this.isCallbackLevel(level);
    }

    /**
     * 判断某个level是否为callback isCallbackLevel
     * 
     * @param level
     * @return
     */
    private boolean isCallbackLevel(Level level) {
        return level != null && level.getType() == LevelType.CALL_BACK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rollBack(String reportId, String taskId) {
        return this.executeRollBack(reportId, taskId);
    }

    /**
     * 根据数据源id，获取对应的数据源信息
     * 
     * @param dsId
     * @return
     */
    private DataSourceInfo getDsInfoFromDsId(String dsId) {
        try {
            // 获取报表使用的数据源信息
            DataSourceDefine dsDefine = dsService.getDsDefine(dsId);
            if (dsDefine == null) {
                return null;
            }
            // 将其转为queryrouter所需的datasourceInfo
            DataSourceInfo dsInfo =
                    DataSourceConnectionServiceFactory.getDataSourceConnectionServiceInstance(
                            dsDefine.getDataSourceType().name()).parseToDataSourceInfo(dsDefine, securityKey);
            return dsInfo;
        } catch (DataSourceOperationException | DataSourceConnectionException e) {
            LOG.error("fail to get datasource info with datasource id " + dsId);
        }
        return null;
    }

    /**
     * 根据报表id获取报表模型
     * 
     * @param reportId
     * @return
     */
    private ReportDesignModel getDesignModelAccordingReportId(String reportId) {
        // 如果缓存中取不到则从已发布文件中取
        ReportDesignModel model = reportDesignModelService.getModelByIdOrName(reportId, true);
        if (model == null) {
            // 如果从已发布中找不到，则从未发布中找
            model = reportDesignModelService.getModelByIdOrName(reportId, false);
        }
        // 如果找不到报表，则报异常
        if (model == null) {
            throw new RuntimeException("can't get report model with id { " + reportId + " }");
        }
        return model;
    }

    /**
     * 获取model中的时间参数信息
     * 
     * @param model
     * @return
     */
    private Map<String, String> getTimeDimParamsOfReport(ReportDesignModel model, RegularReportTaskInfo taskInfo) {
        Map<String, String> newParams = Maps.newHashMap();
        if (taskInfo.getExecuteStrategy() == null) {
            return newParams;
        }
        // 获取cube信息
        MiniCube cube = model.getSchema().getCubes().values().toArray(new MiniCube[0])[0];
        // 获取维度
        Map<String, Dimension> dimensions = cube.getDimensions();
        // 获取日期
        String timeJson = this.genDetailTime(taskInfo.getExecuteStrategy());
        // newParams.put(timeDim.getId(), timeJson);
        Map<String, ReportParam> reportParams = model.getParams();
        reportParams.forEach((k, v) -> {
            if (dimensions != null && dimensions.size() != 0 && dimensions.containsKey(v.getElementId())
                    && (dimensions.get(v.getElementId()).isTimeDimension())) {
                // TODO 构建时间参数
                newParams.put(v.getName(), timeJson);
                newParams.put(v.getElementId(), timeJson);
            }
        });
        Map<String, PlaneTableCondition> conditions = model.getPlaneTableConditions();
        conditions.forEach((k, v) -> {
            if (dimensions != null && dimensions.size() != 0 && dimensions.containsKey(v.getElementId())
                    && (dimensions.get(v.getElementId()).isTimeDimension())) {
                // TODO 构建时间参数
                newParams.put(v.getName(), timeJson);
                newParams.put(v.getElementId(), timeJson);
            }
        });
        return newParams;
    }

    /**
     * 获取任务执行策略对应的时间，多个日期以逗号分割
     * 
     * @param strategy
     * @return
     */
    private String genDetailTime(ExecuteTaskStrategy strategy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 粒度信息
        String granularity = strategy.getGranularity();
        // detail
        String detail = strategy.getDetail();
        // 当前日期
        LocalDate now = LocalDate.now();
        // 起始时间
        LocalDate start = now;
        // 终止时间
        LocalDate end = now;
        switch (granularity) {
            case "D":
                // 日粒度默认使用当前时间
                break;
            case "W":
                String weekDetail = detail.substring(1, detail.length());
                // 如果是周一，则取前一周的数据;否则，取当前周数据
                if (weekDetail.equals("1")) {
                    // 一周前的周一
                    start = now.minus(1, ChronoUnit.WEEKS);
                    // 前一天
                    end = now.minus(1, ChronoUnit.DAYS);
                } else {
                    // 获取当前周所在的周一
                    start = now.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                    end = now;
                }
                break;
            case "M":
                String monthDetail = detail.substring(1, detail.length());
                // 如果是1号，则取前一月份的数据;否则，取当前月份数据
                if (monthDetail.equals("1")) {
                    // 一月前的1号
                    start = now.minus(1, ChronoUnit.MONTHS);
                    // 先取前一个月，然后再取最后一天
                    end = now.minus(1, ChronoUnit.MONTHS).with(TemporalAdjusters.lastDayOfMonth());
                } else {
                    // 本月起始日期
                    start = now.with(TemporalAdjusters.firstDayOfMonth());
                    // 当前日期作为截止日期
                    end = now;
                }
                break;
            case "Q":
                String quarterDetail = detail.substring(1, detail.length());
                // 如果是1号，则取前一季度的数据;否则，取当前季度数据
                if (quarterDetail.equals("1")) {
                    // 前一个季度的起始日期
                    start = now.minus(3, ChronoUnit.MONTHS);
                    // 前一个季度的结束日期
                    start = now.minus(1, ChronoUnit.DAYS);
                } else {
                    // 获取当前月份
                    int[] quarterMonth = { 1, 4, 7, 10 };
                    int monthValue = now.getMonth().getValue();
                    int startMonth = quarterMonth[monthValue / 4];
                    // 本季度第一天
                    start = LocalDate.of(now.getYear(), startMonth, 1);
                    // 当前日期
                    end = now;
                }
                break;
            case "Y":
                String yearDetail = detail.substring(1, detail.length());
                // 如果是1号，则取前一年数据;否则，取当前年数据
                if (yearDetail.equals("1")) {
                    // 前一年的起始天
                    start = now.minus(1, ChronoUnit.YEARS);
                    // 截止日期
                    end = now.minus(1, ChronoUnit.DAYS);
                } else {
                    // 起始日期
                    start = now.with(TemporalAdjusters.firstDayOfYear());
                    // 当前日期
                    end = now;
                }
                break;
            default:
                throw new UnsupportedOperationException("the task execute granularity can't supported, granularity = "
                        + granularity);
        }
        return this.getDetailTimeCondition(start.format(formatter), end.format(formatter));
    }

    /**
     * 获取起始日期和截止日期之间的所有天，之间用逗号分隔
     * 
     * @param startTime 起始日期
     * @param endTime 截止日期
     * @return
     */
    private String getDetailTimeCondition(String startTime, String endTime) {
        TimeRangeDetail range = new TimeRangeDetail(startTime, endTime);
        StringBuilder message = new StringBuilder();
        String[] days = range.getDays();
        for (int i = 0; i < days.length - 1; i++) {
            message.append(days[i] + ",");
        }
        message.append(days[days.length - 1]);
        return message.toString();
    }

    /**
     * 固定报表查询产生数据
     * 
     * @author yichao.jiang
     * @version 2015年7月30日
     * @since jdk 1.8 or after
     */
    class RegularReportGenDataJSONTask implements Callable<Map<String, String>> {

        /**
         * 报表id
         */
        private String reportId;

        /**
         * 任务id
         */
        private String taskId;

        /**
         * 权限信息
         */
        private String authority;

        /**
         * 产品线信息
         */
        private String productLine;

        /**
         * 构造函数
         */
        public RegularReportGenDataJSONTask(String reportId, String taskId, String authority, String productLine) {
            super();
            this.reportId = reportId;
            this.taskId = taskId;
            this.authority = authority;
            this.productLine = productLine;
        }

        /**
         * {@inheritDoc} <br>
         * 任务执行方法，返回任务执行结果
         */
        @Override
        public Map<String, String> call() throws Exception {
            try {
                ContextManager.cleanProductLine();
                ContextManager.setProductLine(productLine);
                // 任务执行开始时间
                long start = System.currentTimeMillis();
                long firstStart = start;
                LOG.info("start begin execute query data task with report id : [ " + reportId + "], task id : [ "
                        + taskId + "], authority: " + authority);
                // 存储最终结果，key为authority，value为最终的JSON数据
                Map<String, String> result = Maps.newHashMap();
                // // 从cache中取得reportModel
                // String key = reportId + SEPERATOR + taskId;
                // ReportDesignModel reportModel = (ReportDesignModel) cacheManagerForReource.getFromCache(key);
                ReportDesignModel reportModel;
                try {
                    reportModel = getDesignModelAccordingReportId(reportId);
                } catch (Exception e) {
                    return result;
                }
                // STEP 1: 获取数据，调用接口，获取数据信息，key为areaId,value为该区域对应的数据模型DataModel
                // 获取数据源信息
                DataSourceInfo dsInfo = getDsInfoFromDsId(reportModel.getDsId());
                // 如果数据源信息为空，则返回false
                if (dsInfo == null) {
                    result.put(authority, null);
                    return result;
                }
                // STEP 1: 获取数据，调用接口，获取数据信息，key为areaId,value为该区域对应的数据模型DataModel
                // 获取任务信息
                RegularReportTaskInfo taskBo = regularReportSettingService.getRegularReportSetting(reportId, taskId);
                // 参数转换
                Map<String, String> params = convertParams2String(taskBo.getParams(), reportModel);
                // 获取时间参数
                params.putAll(getTimeDimParamsOfReport(reportModel, taskBo));
                // 数据查询
                Map<String, DataModel> dataModels =
                        regularReportQueryService.queryReportData(dsInfo, reportModel, params);
                // 记录获取数据时间
                start = System.currentTimeMillis();
                LOG.info("get data with report id : [" + reportId + "], task id : [" + taskId + "],  authority: ["
                        + authority + "] cost " + (System.currentTimeMillis() - start) + "ms");
                // STEP2: 转换数据，将dataModel转为前端所需的JSON字符串，用于显示
                String dataJson = regularReportDataFileService.convertData2Json(reportModel, dataModels);
                result.put(authority, dataJson);
                // 记录转换数据时间
                start = System.currentTimeMillis();
                LOG.info("convert data 2 json with report id : [" + reportId + "], task id : [" + taskId
                        + "],  authority: [" + authority + "] cost " + (System.currentTimeMillis() - start) + "ms");
                // 记录整个任务执行时间
                LOG.info("end execute task with report id : [ " + reportId + "], task id : [ " + taskId
                        + "], authority: " + authority + ", the total cost is :"
                        + (System.currentTimeMillis() - firstStart) + "ms");
                return result;
            } finally {
                ContextManager.cleanProductLine();
            }

        }
    }

    /**
     * 固定报表存储数据文件任务
     * 
     * @author yichao.jiang
     * @version 2015年7月31日
     * @since jdk 1.8 or after
     */
    class RegularReportSaveDataJSONTask implements Callable<Map<String, Boolean>> {

        /**
         * 报表id
         */
        private String reportId;

        /**
         * 任务id
         */
        private String taskId;

        /**
         * 权限信息
         */
        private String authority;

        /**
         * 产品线信息
         */
        private String productLine;

        /**
         * 数据JSON
         */
        private String dataJson;

        /**
         * 构造函数
         */
        public RegularReportSaveDataJSONTask(String reportId, String taskId, String authority, String dataJson,
                String productLine) {
            super();
            this.reportId = reportId;
            this.taskId = taskId;
            this.authority = authority;
            this.dataJson = dataJson;
            this.productLine = productLine;
        }

        /**
         * 保存数据文件任务 {@inheritDoc}
         */
        @Override
        public Map<String, Boolean> call() throws Exception {
            try {
                
                ContextManager.cleanProductLine();
                ContextManager.setProductLine(productLine);
                // 任务执行开始时间
                long start = System.currentTimeMillis();
                long firstStart = start;
                LOG.info("start begin execute save data task with report id : [ " + reportId + "], task id : [ " + taskId
                        + "], authority: " + authority);
                // 存储最终结果，key为authority，value为保存文件结果
                Map<String, Boolean> result = Maps.newHashMap();
                boolean saveResult =
                        regularReportDataFileService.saveRegularReportDataFile(reportId, taskId, authority, dataJson);
                result.put(authority, saveResult);
                // 记录存储数据时间
                LOG.info("save data json file with report id : [" + reportId + "], task id : [" + taskId
                        + "],  authority: [" + authority + "] cost " + (System.currentTimeMillis() - start) + "ms");
                // 记录整个任务执行时间
                LOG.info("end execute save data task with report id : [ " + reportId + "], task id : [ " + taskId
                        + "], authority: " + authority + ", the total cost is :"
                        + (System.currentTimeMillis() - firstStart) + "ms");
                return result;
            } finally {
                ContextManager.cleanProductLine();
            }
        }
    }

}
