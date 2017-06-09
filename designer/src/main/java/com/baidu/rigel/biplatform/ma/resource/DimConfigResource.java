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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.builder.Director;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.CallbackDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.DimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StandardDimType;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TimeDimType;
import com.baidu.rigel.biplatform.ma.model.meta.UserDefineDimTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.service.CubeMetaBuildService;
import com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.resource.cache.CacheManagerForResource;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.view.CubeView;
import com.baidu.rigel.biplatform.ma.resource.view.DateRelationTableView;
import com.baidu.rigel.biplatform.ma.resource.view.DimBindConfigView;
import com.baidu.rigel.biplatform.ma.resource.view.DimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.RelationTableView;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CallbackDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CustDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.DateDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.NormalDimDetail;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CallbackDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CustDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.DateDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.NormalDimBindView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * CubeTable的页面交互
 * 
 * @author zhongyi
 *
 *         2014-7-30
 */
@RestController
@RequestMapping("/silkroad/reports")
public class DimConfigResource extends BaseResource {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(CacheManagerForResource.class);
    
    /**
     * gson
     */
    private Gson gson = new GsonBuilder().create();
    
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
     * 
     * @param reportId
     * @param tableId
     * @return
     */
    @RequestMapping(value = "/{reportId}/tables/{tableId}", method = { RequestMethod.GET })
    public ResponseResult getTable(@PathVariable("reportId") String reportId,
            @PathVariable("tableId") String tableId) {
        
        // TODO 非内置时间以后实现
        return null;
    }
    
    /**
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/dim_config", method = { RequestMethod.GET })
    public ResponseResult getDimConfig(@PathVariable("id") String reportId,
            HttpServletRequest request) {
        
        ReportDesignModel reportModel = getReportModel(reportId);
        if (reportModel == null) {
            String message = "can not get report model with id : " + reportId;
            logger.info(message);
            return ResourceUtils.getErrorResult(message, 1);
        }
        Schema schema = reportModel.getSchema();
        StarModel[] starModels = director.getStarModel(schema);
        
        DimBindConfigView view = new DimBindConfigView();
        List<RelationTableView> relationsTables = null;
        try {
            relationsTables = starModelBuildService.getAllTablesAndCols(reportModel.getDsId(), securityKey);
        } catch (DataSourceOperationException e) {
            logger.error("Fail in get table info from ds. Ds Id: " + reportModel.getDsId(), e);
            return ResourceUtils.getErrorResult("获取数据源中的表格、列信息失败！请检查数据源配置。", 1);
        }
        view.setRelationTables(relationsTables);
        
        DimBindView dimBind = new DimBindView();
        view.setDim(dimBind);
        
        Map<String, CubeView> cubes = Maps.newHashMap();
        
        for (StarModel starModel : starModels) {
            
            NormalDimBindView normal = new NormalDimBindView();
            normal.setCubeId(starModel.getCubeId());
            CallbackDimBindView callback = new CallbackDimBindView();
            callback.setCubeId(starModel.getCubeId());
            CustDimBindView cust = new CustDimBindView();
            cust.setCubeId(starModel.getCubeId());
            DateDimBindView date = new DateDimBindView();
            date.setCubeId(starModel.getCubeId());
            
            dimBind.getCallback().add(callback);
            dimBind.getCustom().add(cust);
            dimBind.getNormal().add(normal);
            dimBind.getDate().add(date);
            
            for (DimTableMetaDefine dimTable : starModel.getDimTables()) {
                
                if (dimTable.getDimType() instanceof TimeDimType) {
                    DateDimDetail dateDim = starModelBuildService.generateDateDimDetail(
                            starModel.getCubeId(), (TimeDimTableMetaDefine) dimTable);
                    date.getChildren().add(dateDim);
                } else if (dimTable.getDimType() == StandardDimType.CALLBACK) {
                    CallbackDimDetail callbackDim = starModelBuildService
                        .generateCallbackDimDetail((CallbackDimTableMetaDefine) dimTable);
                    callback.getChildren().add(callbackDim);
                } else if (dimTable.getDimType() == StandardDimType.USERDEFINE) {
                    CustDimDetail dateDim = starModelBuildService
                        .generateCustDimDetail((UserDefineDimTableMetaDefine) dimTable);
                    cust.getChildren().add(dateDim);
                } else {
                    NormalDimDetail dateDim = starModelBuildService
                        .generateNormalDimBindView((StandardDimTableMetaDefine) dimTable);
                    if (dateDim != null) {
                        normal.getChildren().add(dateDim);
                    }
                }
            }
            
            FactTableMetaDefine cubeTable = starModel.getFactTable();
            CubeView cubeView = new CubeView();
            cubeView.setName(cubeTable.getName());
            /**
             * 获取事实表的所有字段
             */
            DataSourceDefine ds;
            try {
                ds = dsService.getDsDefine(reportModel.getDsId());
            } catch (DataSourceOperationException e) {
                String msg = "找不到数据源" + reportModel.getDsId();
                logger.error(msg, e);
                return ResourceUtils.getErrorResult(msg, 1);
            }
            DataSourceInfoReaderService dsInfoReaderService = null;
            String tableName = cubeTable.getName();
            if (cubeTable.isMutilple() && !CollectionUtils.isEmpty(cubeTable.getRegExpTables())) {
                tableName = cubeTable.getRegExpTables().get(0);
            }
            List<ColumnInfo> cols = null;
            try {
                dsInfoReaderService = DataSourceInfoReaderServiceFactory.
                		getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
                cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, tableName);
            } catch(Exception e) {
            	logger.error("fail to get columninfos from datasource", e);
            }
            if (CollectionUtils.isEmpty(cols)) {
                String msg = String.format("不能从表%s中获取字段！", tableName);
                logger.error(msg);
                return ResourceUtils.getErrorResult(msg, 1);
            }
            cubeView.setAllFields(cols);
            cubeView.setCurrDims(cols);
            cubes.put(cubeTable.getCubeId(), cubeView);
        }
        view.setCubes(cubes);
        
        List<DateRelationTableView> dateRelationTables = Lists.newArrayList();
        // TODO 非内置时间以后实现
        view.setDateRelationTables(dateRelationTables);
        
        ResponseResult rs = ResourceUtils.getCorrectResult("Success Getting Dim Config", view);
        logger.info("put operation rs is : " + rs.toString());
        return rs;
    }
    
    /**
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/dim_config", method = { RequestMethod.POST })
    public ResponseResult saveDimConfig(@PathVariable("id") String reportId,
            HttpServletRequest request) {
        
        String normalStr = request.getParameter("normal");
        String dateStr = request.getParameter("date");
        String callbackStr = request.getParameter("callback");
        String customStr = request.getParameter("custom");
        
        List<NormalDimBindView> normalDims = gson.fromJson(normalStr,
            new TypeToken<List<NormalDimBindView>>() {
            }.getType());
        List<CallbackDimBindView> callbackDims = gson.fromJson(callbackStr,
            new TypeToken<List<CallbackDimBindView>>() {
            }.getType());
        List<CustDimBindView> custDims = gson.fromJson(customStr,
            new TypeToken<List<CustDimBindView>>() {
            }.getType());
        List<DateDimBindView> dateDims = gson.fromJson(dateStr,
            new TypeToken<List<DateDimBindView>>() {
            }.getType());
        
        /**
         * for efficiency
         */
        Map<String, NormalDimBindView> normals = Maps.newHashMap();
        Map<String, DateDimBindView> dates = Maps.newHashMap();
        Map<String, CallbackDimBindView> callbacks = Maps.newHashMap();
        Map<String, CustDimBindView> customs = Maps.newHashMap();
        for (NormalDimBindView normalDim : normalDims) {
            normals.put(normalDim.getCubeId(), normalDim);
        }
        for (DateDimBindView dateDim : dateDims) {
            dates.put(dateDim.getCubeId(), dateDim);
        }
        for (CallbackDimBindView callbackDim : callbackDims) {
            callbacks.put(callbackDim.getCubeId(), callbackDim);
        }
        for (CustDimBindView custDim : custDims) {
            customs.put(custDim.getCubeId(), custDim);
        }
        
        ReportDesignModel reportModel = getReportModel(reportId);
        DataSourceDefine ds = null;
        try {
            ds = dsService.getDsDefine(reportModel.getDsId());
        } catch (DataSourceOperationException e1) {
            logger.error("[ERROR] --- --- 获取数据源信息失败", e1);
        }
        Schema schema = reportModel.getSchema();
        StarModel[] starModels = director.getStarModel(schema);
        for (StarModel starModel : starModels) {
            List<DimTableMetaDefine> newDimTables = Lists.newArrayList();
            Map<String, String> oldName = Maps.newHashMap();
            for (DimTableMetaDefine dimMetaDefine : starModel.getDimTables()) {
                /**
                 * 非用户自定义的维度，要保留原有的名字
                 */
                if (dimMetaDefine.getDimType() != StandardDimType.USERDEFINE) {
                    oldName.put(dimMetaDefine.getReference().getMajorColumn(),
                            dimMetaDefine.getName());
                }
            }
            
            NormalDimBindView normal = normals.get(starModel.getCubeId());
            DateDimBindView date = dates.get(starModel.getCubeId());
            CallbackDimBindView callback = callbacks.get(starModel.getCubeId());
            CustDimBindView custom = customs.get(starModel.getCubeId());
            
            try {
                newDimTables.addAll(starModelBuildService.generateMetaDefine(reportModel.getDsId(),
                    normal, oldName, securityKey));
            } catch (DataSourceOperationException e) {
                logger.error("添加普通维度失败！", e);
                return ResourceUtils.getErrorResult("添加普通维度失败！", 1);
            }
            newDimTables.addAll(starModelBuildService.generateMetaDefine(date, oldName));
            newDimTables.addAll(starModelBuildService.generateMetaDefine(callback, oldName));
            newDimTables.addAll(starModelBuildService.generateMetaDefine(custom, oldName));
            
            // 重置starModel的
            starModel.setDimTables(newDimTables);
            // 修正事实表定义
            modifyFactTable(starModel, ds);
        }
        schema = director.modifySchemaWithNewModel(schema, starModels);
        ResponseResult rs = null;
        if (schema == null) {
            rs = ResourceUtils.getErrorResult("error when modify", 1);
        } else {
            reportModel.setSchema((MiniCubeSchema) schema);
            updateReportModelWithSchema(reportModel, schema);
            try {
                reportModelCacheManager.updateReportModelToCache(reportId, reportModel);
            } catch (CacheOperationException e) {
                logger.error("Fail in updating report model by id: " + reportId);
                return ResourceUtils.getErrorResult("更新报表模型失败！" , 1);
            }
            rs = ResourceUtils.getCorrectResult("Success modifying Starmodel! ", "");
        }
        return rs;
    }
    
    /**
     * 
     * @param reportModel
     * @param schema
     */
    private void updateReportModelWithSchema(ReportDesignModel reportModel, Schema schema) {
        if (reportModel.getExtendAreas () == null || reportModel.getExtendAreas ().isEmpty ()) {
            return;
        }
        reportModel.getExtendAreas ().values ().forEach (area -> {
            Cube cube = schema.getCubes ().get (area.getCubeId ());
            if (cube != null) {
                LogicModel logicModel = area.getLogicModel ();
                if (logicModel != null) {
                    boolean isPlaneTable = area.getType() == ExtendAreaType.PLANE_TABLE;
                    updateLogicModelWithCube(logicModel, cube, isPlaneTable);
                    if (isPlaneTable) {
                        updatePlaneTableCond(reportModel.getPlaneTableConditions(), logicModel.getSlices());
                    }
                }
            }
        });
    }

    private void updateLogicModelWithCube(LogicModel logicModel, Cube cube, boolean isPlaneTable) {
        Item[] cols = logicModel.getColumns ();
        if (cols.length > 0) {
            updateCols (logicModel, cube, cols, isPlaneTable);
        }
        
        if (logicModel.getSelectionMeasures () != null && !logicModel.getSelectionMeasures ().isEmpty ()) {
            for(Item item : logicModel.getSelectionMeasures ().values ().toArray (new Item[0])) {
                if (!cube.getMeasures ().containsKey (item.getOlapElementId ())) {
                    logicModel.getSelectionMeasures ().remove (item.getOlapElementId ());
                }
            }
        }
        
        if (logicModel.getRows ().length > 0) {
            updateDimItem(logicModel, cube, logicModel.getRows (), false);
        }
        
        Item[] slices = logicModel.getSlices ();
        if (slices.length > 0) {
            updateDimItem (logicModel, cube, slices, true);
        }
        
        if (logicModel.getSelectionDims () != null && !logicModel.getSelectionDims ().isEmpty ()) {
            for(Item item : logicModel.getSelectionDims ().values ().toArray (new Item[0])) {
                if (!cube.getDimensions ().containsKey (item.getOlapElementId ())) {
                    logicModel.getSelectionDims ().remove (item.getOlapElementId ());
                }
            }
        }
    }

    private void updateCols(LogicModel logicModel, Cube cube, Item[] cols, boolean isPlaneTable) {
        for (Item item : cols) {
            if (isPlaneTable && cube.getDimensions ().containsKey (item.getOlapElementId ())) {
                continue;
            }
            if (cube.getMeasures ().containsKey (item.getOlapElementId ())) {
                continue;
            }
            logicModel.removeColumn (item.getOlapElementId ());
        }
    }

    private void updateDimItem(LogicModel logicModel, Cube cube, Item[] slices, boolean isSlices) {
        for (Item item : slices) {
            if (cube.getDimensions ().containsKey (item.getOlapElementId ()) 
                    || cube.getMeasures().containsKey(item.getOlapElementId())) {
                continue;
            }
            if (isSlices) {
                logicModel.removeSlice (item.getOlapElementId ());
            } else {
                logicModel.removeRow (item.getOlapElementId ());
            }
        }
    }

    private void updatePlaneTableCond(Map<String, PlaneTableCondition> conditions, Item[] slices) {
        Set<String> elementKey = Sets.newHashSet();
        for (Item item : slices) {
            elementKey.add(item.getOlapElementId());
        }
        
        Map<String, PlaneTableCondition> newConditions = Maps.newHashMap();
        conditions.forEach( (k, v) -> {
            if (elementKey.contains(k)) {
                newConditions.put(k, v);
            }
        });
        conditions.clear();
        conditions.putAll(newConditions);
    }
    /**
     * 
     * @param starModel
     */
    private void modifyFactTable(StarModel starModel, DataSourceDefine ds) {
        if (ds == null) {
            return;
        }
        List<ColumnInfo> cols = null;
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            dsInfoReaderService = DataSourceInfoReaderServiceFactory.
            		getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
            cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, starModel.getFactTable().getName());
        } catch (Exception e) {
            logger.error("[ERROR] --- --- --- --- fail to get columnInfos from datasource : {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
        }
        if (cols == null || cols.isEmpty()) {
            return;
        }
        starModel.getFactTable().clearColumns();
        ColumnMetaDefine column = null;
        for (ColumnInfo col : cols) {
            column = new ColumnMetaDefine();
//            column.setName(StringUtils.hasText(col.getComment())? col.getName() : col.getId());
//            column.setCaption(StringUtils.hasText(col.getComment())? col.getComment() :col.getName());
            column.setName(col.getName());
            column.setCaption(col.getComment());
            starModel.getFactTable().addColumn(column);
        }
    }

    /**
     * 
     * @param reportId
     * @return
     */
    private ReportDesignModel getReportModel(String reportId) {
        ReportDesignModel reportModel = null;
        try {
            reportModel = reportModelCacheManager.getReportModel(reportId);
            return reportModel;
        } catch (CacheOperationException e) {
            logger.debug("There is no such report model in cache. ", e);
            logger.info("Add report model into cache. ");
        }
        return reportModelCacheManager.loadReportModelToCache(reportId);
    }
    
}