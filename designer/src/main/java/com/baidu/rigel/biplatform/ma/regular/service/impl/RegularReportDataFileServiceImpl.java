

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
import java.util.Map;


import javax.annotation.Resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportDataFileService;
import com.baidu.rigel.biplatform.ma.regular.utils.RegularReportDataFileUtils;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PlaneTable;
import com.baidu.rigel.biplatform.ma.report.service.ChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.QueryDataResourceUtils;
import com.google.common.collect.Maps;

/** 
 * 固定报表数据文件服务实现类 
 * @author yichao.jiang 
 * @version  2015年7月28日 
 * @since jdk 1.8 or after
 */
@Service("regularReportDataFileService")
public class RegularReportDataFileServiceImpl implements RegularReportDataFileService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportDataFileServiceImpl.class);

    /**
     * 数据JSON的文件名
     */
    private static final String DATA_JSON_FILE_NAME = "data_json.js";
        
    /**
     * 报表设计模型服务
     */
    @Resource(name = "reportDesignModelService")
    private ReportDesignModelService reportDesignModelService;
    
    /**
     * 文件服务
     */
    @Resource
    private FileService fileService;
    
    /**
     * Resource层查询工具类
     */
    @Resource
    private QueryDataResourceUtils queryDataResourceUtils;
    
    /**
     * 图形构建服务
     */
    @Resource
    private ChartBuildService chartBuildService;

    /**
     * 构造函数
     */
    public RegularReportDataFileServiceImpl() {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String convertData2Json(ReportDesignModel reportModel, Map<String, DataModel> dataModels) {
        long start = System.currentTimeMillis();
        StringBuilder strBuilder = new StringBuilder();
        if (dataModels == null || dataModels.size() == 0) {
            LOG.warn("the dataModels is empty, please check!");
            return strBuilder.toString();
        }
        if (reportModel.getSchema() == null) {
            LOG.warn("the reportModel { " + reportModel.getId() + "} 's schema is null, please check!" );
            return strBuilder.toString();
        }
        // 结果JSON，key为AreaId, value为数据查询结果
        Map<String, Object> resultJson = Maps.newHashMap();
        Map<String, ? extends Cube> cubes = reportModel.getSchema().getCubes();
        Map<String, ExtendArea> extendAreas = reportModel.getExtendAreas();
        extendAreas.forEach((k, v) -> {
            // 获取某个区域对应的数据模型DataModel
            DataModel dataModel = dataModels.get(k);
            String cubeId = v.getCubeId();
            Cube cube = cubes.get(cubeId);
            FormatModel formatModel = v.getFormatModel();
            LogicModel logicModel = v.getLogicModel();
            Map<String, Object> tmp = Maps.newHashMap();
            switch (v.getType()) {
                // 平面表
                case PLANE_TABLE:
                        // TODO 考虑平面表的queryAction，在转换过程中，会涉及从queryAction中取排序信息
                        QueryAction queryAction = new QueryAction();
                        resultJson.put(k,
                                this.getPlaneTable(cube, dataModel, logicModel, formatModel, queryAction));
                        break;
                case TABLE:
                    PivotTable pivotTable = this.getPivotTable(cube, dataModel, v);
                    tmp.put("pivottable", pivotTable);
                    tmp.put("reportTemplateId", reportModel.getId());
                    tmp.put("totalSize", pivotTable.getActualSize() - 1);
                    tmp.put("currentSize", pivotTable.getActualSize() -1);
                    tmp.put("rowCheckMax", 5);
                    tmp.put("rowCheckMin", 1);
                    resultJson.put(k, tmp);
                    break;
                case CHART:
                    tmp.put("reportChart", this.getReportChart(cube, dataModel, reportModel, v));
                    resultJson.put(k, tmp);
                    break;
                default:
                    throw new UnsupportedOperationException("other extendarea type is not supported { " + v.getType() + " }");
            }
        });
        String result = GsonUtils.toJson(resultJson);
        LOG.info("convert data 2 json cost: " + (System.currentTimeMillis() - start) + "ms" );
        LOG.debug("the result Json is { " + result + " }");
        return result;       
    }
    
    /**
     * 获取平面表信息
     * @param cube
     * @param dataModel
     * @param logicModel
     * @param formatModel
     * @param queryAction
     * @return
     */
    private Map<String, Object> getPlaneTable(Cube cube, DataModel dataModel,
            LogicModel logicModel, FormatModel formatModel, QueryAction queryAction) {
        Map<String, Object> resultMap = Maps.newHashMap();
        // 获取平面表模型
        PlaneTable planeTable = DataModelUtils.transDataModel2PlaneTable(cube,
                dataModel, logicModel, formatModel, queryAction);
        // 按照前端需求，转换平面表数据
        if (planeTable != null) {
            resultMap.put("head", planeTable.getColDefines());
            resultMap.put("data", planeTable.getData());
            resultMap.put("pageInfo", planeTable.getPageInfo());
        }
        return resultMap;
    }
    
    /**
     * 获取多维报表模型
     * @param cube
     * @param dataModel
     * @param area
     * @return
     */
    private PivotTable getPivotTable(Cube cube, DataModel dataModel, ExtendArea area) {
        LogicModel logicModel = area.getLogicModel();
        // FormatModel formatModel = area.getFormatModel();
        // 普通多维表
        PivotTable pivotTable = 
            DataModelUtils.transDataModel2PivotTable(cube, dataModel, false, 0, false, logicModel);
        // Map<String, Object> otherSetting = area.getOtherSetting();
        // boolean isShowZero = DataModelUtils.isShowZero(otherSetting);
        // DataModelUtils.decorateTable(formatModel, pivotTable, isShowZero);
        return pivotTable;
    }
    
    /**
     * 获取图形模型
     * @param cube
     * @param dataModel
     * @param reportModel
     * @param area
     * @return
     */
    private DIReportChart getReportChart(Cube cube, 
            DataModel dataModel, ReportDesignModel reportModel, ExtendArea area) {
        DIReportChart chart = null;
        LogicModel logicModel = area.getLogicModel();
        // 获取图形类型
        Map<String, String> chartType = queryDataResourceUtils.getChartTypeWithExtendArea(reportModel, area);
        // 获取多维表
        PivotTable pivotTable = this.getPivotTable(cube, dataModel, area);
        // 图形处理
        if (logicModel != null && logicModel.getRows().length == 1) {
            Item item = logicModel.getRows()[0];
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(reportModel.getSchema(),
                    area.getCubeId(), item.getOlapElementId());
            if (element instanceof TimeDimension) {
                chart = chartBuildService.parseToChart(pivotTable, chartType, true);
            } else {
                chart = chartBuildService.parseToChart(pivotTable, chartType, false);
            }
        } else {
            chart = chartBuildService.parseToChart(pivotTable, chartType, false);
        }
        // TODO 是否考虑加上 
//        ExtendArea extendArea = DeepcopyUtils.deepCopy (area);
//        extendArea.setFormatModel (area.getFormatModel());
//        QueryUtils.decorateChart(chart, extendArea, reportModel.getSchema(), -1);
        return chart;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveRegularReportDataFile(String reportId, String taskId, String authority, String dataJson) {
        try {
            if (dataJson == null) {
                return false;
            }
            // TODO 临时暂定获取发布的报表
            ReportDesignModel reportModel = reportDesignModelService.getModelByIdOrName(reportId, true);
            if (reportModel == null) {
                reportModel = reportDesignModelService.getModelByIdOrName(reportId, false);
                if (reportModel == null) {
                    LOG.error("can not get report model with id: " + reportId);
                    return false;
                }
            }
            String filePath = this.genFilePath(reportId, taskId, authority, null);
            return fileService.write(filePath, dataJson.getBytes(), true);            
        } catch (FileServiceException e) {
            LOG.error("write file happends exceptions", e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readRegularReportDataFile(String reportId, String taskId, String authority) {
        try {
            String filePath = this.genFilePath(reportId, taskId, authority, null);
            return new String (fileService.read(filePath));
        } catch (FileServiceException e) {
            LOG.error("read file happends exceptions", e.getMessage());
        }
        return null;       
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String readRegularReportDataFile(String reportId, String taskId, String authority, String time) {
        try {
            String filePath = this.genFilePath(reportId, taskId, authority, time);
            return new String (fileService.read(filePath));
        } catch (FileServiceException e) {
            LOG.error("read file happends exceptions", e.getMessage());
        }
        return null; 
        
    }
    
    /**
     * 获取存储文件路径
     * @param reportId 报表id
     * @param taskId 任务id
     * @param authority 权限
     * @param time 时间
     * @return
     */
    private String genFilePath(String reportId, String taskId, String authority, String time) {
        String filePath = RegularReportDataFileUtils.genDataFilePath4RegularReport(reportId, taskId, authority, time);
        filePath = filePath + File.separator + DATA_JSON_FILE_NAME;
        return filePath; 
    }
}

