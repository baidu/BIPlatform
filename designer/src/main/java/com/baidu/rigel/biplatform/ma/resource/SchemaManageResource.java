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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ma.auth.bo.CalMeasureViewBo;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.SchemaManageService;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.model.utils.HttpUrlUtils;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.ElementUtils;
import com.baidu.rigel.biplatform.ma.resource.view.vo.CubeObject;
import com.baidu.rigel.biplatform.ma.resource.view.vo.DimensionObject;
import com.baidu.rigel.biplatform.ma.resource.view.vo.MeasureObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.internal.StringMap;

/**
 * 
 * SchemaManageResource：<p> {@link Schema} manage resource, 
 * design for provide restful API for remote client manage {@link Schema}, current implements based on Spring boot
 * framework. All the APIs can be invoked through HttpClient or other base on HTTP protocol's client operation
 * </p>
 * @see com.baidu.rigel.biplatform.ma.resource.ResponseResult
 * @since jdk1.8.5 or after Spring 4.0 or after
 * @author david.wang
 * @version Silkroad 1.0.1
 */
@RestController
@RequestMapping("/silkroad/reports")
public class SchemaManageResource {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(SchemaManageResource.class);
    
    /**
     * cache manager
     */
    @Resource(name = "reportModelCacheManager")
    private ReportModelCacheManager reportModelCacheManager;
    
    /**
     * schema manager service
     */
    @Resource(name = "schemaManageService")
    private SchemaManageService service;
    
    /**
     * report design model service
     */
    @Resource(name = "reportDesignModelService")
    private ReportDesignModelService reportDesignModelService;
    
    /**
     * query all cubes which the report reference
     * 
     * @param reportId -- report id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- the operation result
     * @throws Exception 
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes", method = { RequestMethod.GET })
    public ResponseResult getCubes(@PathVariable("reportId") String reportId,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        List<CubeObject> cubes = Lists.newArrayList();
        for (Cube cube : schema.getCubes().values()) {
            CubeObject cubeObj = new CubeObject();
            cubeObj.setId(cube.getId());
            cubeObj.setName(cube.getName());
            cubes.add(cubeObj);
        }
        rs.setData(cubes);
        rs.setStatus(0);
        rs.setStatusInfo("successfully");

        return rs;
    }
    
    /**
     * 
     * query {@link Measures}'s list through cube id. 
     * All the {@link Measure}s belong to the cube which id equals cube id.
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/inds", method = { RequestMethod.GET })
    public ResponseResult getMeasures(@PathVariable("reportId") String reportId, 
            @PathVariable("cubeId") String cubeId,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取立方体定义 : " + cubeId);
            logger.debug("can't find cube with id : " + cubeId);
            return rs;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        List<MeasureObject> measures = Lists.newArrayList();
        Set<String> changableInds = ElementUtils.getChangableIndNames(model, cube);
        for (Measure measure : cube.getMeasures().values()) {
            MeasureObject measureObj = new MeasureObject();
            measureObj.setAggregator(measure.getAggregator());
            measureObj.setCaption(measure.getCaption());
            measureObj.setId(measure.getId());
            measureObj.setName(measure.getName());
            measureObj.setVisible(measure.isVisible());
            measureObj.setExpr(measure.getDefine());
            measureObj.setType(measure.getType());
            if (measure.getType() == MeasureType.CAL) {
                measureObj.setFormula(((ExtendMinicubeMeasure) measure).getFormula()); 
            }
            if (measure instanceof CallbackMeasure) {
                CallbackMeasure m = (CallbackMeasure) measure;
                Map<String, String> params = m.getCallbackParams() == null ? 
                		HttpUrlUtils.getParams(m.getCallbackUrl()) : m.getCallbackParams();
                measureObj.setUrl(HttpUrlUtils.generateTotalUrl(m.getCallbackUrl(), params));
                Map<String, String> prop = Maps.newHashMap();
                prop.put(Constants.SOCKET_TIME_OUT_KEY, String.valueOf(m.getSocketTimeOut()));
                measureObj.setProperties(prop);
            }
            measureObj.setCanToDim(changableInds.contains(measure.getName()));
            measures.add(measureObj);
        }
        rs.setData(measures);
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        return rs;
    }
    
    /**
     * 
     * query {@link Dimension}'s list through cube id. 
     * All the {@link Dimension}s belong to the cube which id equals cube id.
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dims", method = { RequestMethod.GET })
    public ResponseResult getDims(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取立方体定义 : " + cubeId);
            logger.debug("can't find cube with id : " + cubeId);
            return rs;
        }
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        List<DimensionObject> dimensionObjs = Lists.newArrayList();
        Set<String> changableDims = ElementUtils.getChangableDimNames(model, cube);
        Map<String, DimensionObject> dimensionObjMapper = Maps.newHashMap();
        List<Dimension> dimGroups = Lists.newArrayList();
        for (Dimension dimension : cube.getDimensions().values()) {
            DimensionObject dimensionObj = new DimensionObject();
            dimensionObj.setId(dimension.getId());
            dimensionObj.setName(dimension.getName());
            dimensionObj.setCaption(dimension.getCaption());
            dimensionObj.setVisible(dimension.isVisible());
            dimensionObj.setType(dimension.getType());
            dimensionObj.setCanToInd(changableDims.contains(dimension.getName()));
            dimensionObjs.add(dimensionObj);
            dimensionObjMapper.put(dimension.getName(), dimensionObj);
            if (dimension.getType() == DimensionType.GROUP_DIMENSION) {
                dimGroups.add(dimension);
            }
        }
        for (Dimension dimGroup : dimGroups) {
            for (Level level : dimGroup.getLevels().values()) {
                DimensionObject source = dimensionObjMapper.get(level.getDimension().getName());
                DimensionObject child = new DimensionObject();
                child.setId(level.getId());
                child.setCanToInd(source.isCanToInd());
                child.setCaption(source.getCaption());
                child.setLevels(Lists.newArrayList());
                child.setName(source.getName());
                child.setType(source.getType());
                child.setVisible(source.isVisible());
                dimensionObjMapper.get(dimGroup.getName()).getLevels().add(child);
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("dimList", dimensionObjs);
        rs.setData(data);
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        return rs;
    }
    
    /**
     * 
     * query {@link Cube} define with cube id
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}", method = RequestMethod.GET)
    public ResponseResult getCube(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        // 从cache中获取缓存的report对象
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(0);
            rs.setStatusInfo("不能获取立方体定义 id : " + cubeId);
            logger.info("can't get cube with id : " + cubeId);
        } else {
            rs.setStatus(0);
            rs.setStatusInfo("successfully");
            rs.setData(cube);
        }
        return rs;
    }
    
    /**
     * 
     * convert {@link Measure} to {@link Dimension}
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param measureId -- measure id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/ind-to-dim/{measureId}", method = RequestMethod.POST)
    public ResponseResult moveInd2Dim(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("measureId") String measureId,
            HttpServletRequest request) {
        
        ResponseResult rs = new ResponseResult();
        Schema schema = this.getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取cube定义 : " + reportId);
            logger.debug("can't find cube with id : " + reportId);
            return rs;
        }
        Measure measure = cube.getMeasures().get(measureId);
        schema = service.converMeasure2Dim(schema, cubeId, measure);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("指标拖动失败");
            logger.debug("can't move the current measure : measureId " + measureId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * convert {@link Dimension} to {@link Measure}
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimId -- dimension id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim-to-ind/{dimId}", method = RequestMethod.POST)
    public ResponseResult moveDim2Ind(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("dimId") String dimId,
            HttpServletRequest request) {
        
        ResponseResult rs = new ResponseResult();
        Schema schema = this.getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义 : " + reportId);
            logger.debug("can't find model with id : " + reportId);
            return rs;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能获取cube定义 : " + reportId);
            logger.debug("can't find cube with id : " + reportId);
            return rs;
        }
        Dimension dimension = cube.getDimensions().get(dimId);
        schema = service.convertDim2Measure(schema, cubeId, dimension);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("维度拖动失败");
            logger.debug("can't move the current measure : measureId " + dimId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * update or modify {@link Dimension}, {@link Measure}'s visible property value.
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}", method = RequestMethod.POST)
    public ResponseResult modifyDimAndDimVisible(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, HttpServletRequest request) {
        
        ResponseResult rs = new ResponseResult();
        String oriInd = request.getParameter("oriInd");
        List<Measure> measures = this.buildMeasureFromJson(oriInd);
        String oriDim = request.getParameter("oriInd");
        List<Dimension> dims = this.buidDimFromJson(oriDim);
        Schema schema = this.getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        schema = service.modifyIndAndDimVisibility(schema, cubeId, dims, measures);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("操作失败");
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * modify {@link Measure}'s name and aggregation type
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param measuerId -- measure id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/inds/{measureId}", method = RequestMethod.POST)
    public ResponseResult modifyMeasure(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("measureId") String measureId,
            HttpServletRequest request) {
        
        String measureCaption = request.getParameter("caption");
        String aggType = request.getParameter("aggregator");
        
        Schema schema = getSchema(reportId);
        ResponseResult rs = checkSchema(cubeId, reportId, measureCaption, schema);
        if (rs == null) {
            rs = new ResponseResult();
        } else {
            return rs;
        }
        
        MiniCubeMeasure measure = new MiniCubeMeasure(null);
        measure.setId(measureId);
        if (!StringUtils.isEmpty(measureCaption)) {
            measure.setCaption(measureCaption);
        }
        if (!StringUtils.isEmpty(aggType)) {
            measure.setAggregator(Aggregator.valueOf(aggType));
        }
        schema = service.modifyMeasure(schema, cubeId, measure);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("操作失败，请检查输入");
            logger.debug("can't modify name : measureId " + measureId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * change {@link Dimension}'s name
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimId -- dimension id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dims/{dimId}", method = RequestMethod.POST)
    public ResponseResult modifyDimension(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("dimId") String dimId,
            HttpServletRequest request) {
        String dimCaption = request.getParameter("caption");
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        rs = checkDimName(cubeId, dimCaption, schema);
        if (rs == null) {
            rs = new ResponseResult();
        } else {
            return rs;
        }
        StandardDimension dim = new StandardDimension("");
        dim.setId(dimId);
        dim.setCaption(dimCaption);
        schema = service.modifyDimension(schema, cubeId, dim);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("修改维度操作失败");
            logger.info("modify dimension operatio failed");
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * create dimension group
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim_groups", method = RequestMethod.POST)
    public ResponseResult createDimGroup(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, HttpServletRequest request) {
        String dimGroupName = request.getParameter("groupName");
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        rs = checkDimName(cubeId, dimGroupName, schema);
        if (rs == null) {
            rs = new ResponseResult();
        } else {
            return rs;
        }
        schema = service.createDimGroup(schema, cubeId, dimGroupName);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("创建维度组失败");
        } else {
            buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * delete dimension group from cube
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimId -- dimension group id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim_groups/{dimId}", method = RequestMethod.DELETE)
    public ResponseResult removeDim(@PathVariable("reportId")String reportId, @PathVariable("cubeId")String cubeId,
            @PathVariable("dimId")String dimId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        schema = service.removeDimention(schema, cubeId, dimId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("删除失败");
            logger.debug("不能成功删除维度定义");
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * add dimension reference into dimension group
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimGroupId -- dimension group id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim_groups/{dimGroupId}/dims", method = RequestMethod.POST)
    public ResponseResult addDimIntoGroup(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("dimGroupId") String dimGroupId,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String dimId = request.getParameter("dimId");
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        schema = service.addDimIntoDimGroup(schema, cubeId, dimGroupId, dimId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能将当前维度添加到维度组");
            logger.debug("can't add current dim into dim group dimId : " + dimId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * remove dimension reference from dimension group
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimGroupId -- dimension group id
     * @param levleId -- level id
     * @param request -- HttpSerletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim_groups/{dimGroupId}/level/{levelId}",
            method = RequestMethod.DELETE)
    public ResponseResult removeDimFromGroup(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId, @PathVariable("dimGroupId") String dimGroupId,
            @PathVariable("levelId") String levelId, HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        schema = service.removeDimFromGroup(schema, cubeId, dimGroupId, levelId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能将当前维度添加到维度组");
            logger.debug("can't add current dim into dim group dimId : " + levelId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * change level's index 
     * 
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param dimGroupId -- dimension group id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/dim_groups/{dimGroupId}/dim_sorting",
            method = RequestMethod.POST)
    public ResponseResult changeDimOrder(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId,
            @PathVariable("dimGroupId") String dimGroupId,
            HttpServletRequest request) {
        
        ResponseResult rs = new ResponseResult();
        String beforeId = request.getParameter("beforeDimId");
        String targetId = request.getParameter("dimId");
        Schema schema = getSchema(reportId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("没有找到对应报表定义");
            logger.debug("can not find report define");
            return rs;
        }
        schema = service.modifyDimOrder(schema, cubeId, dimGroupId, beforeId, targetId);
        if (schema == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能将当前维度添加到维度组");
            logger.debug("can't add current dim into dim group dimId : " + dimGroupId);
        } else {
            this.buildSuccessResponseResult(reportId, rs, schema);
        }
        return rs;
    }
    
    /**
     * 
     * add or modify extend measure
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param request -- HttpServletRequest
     * @return ResponseResult
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/extend_measures", 
        method = RequestMethod.POST)
    public ResponseResult addOrModifyExtendMeasure(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId,
            HttpServletRequest request) {
        String json = request.getParameter("deriveInds");
        CalMeasureViewBo extendMeasure = CalMeasureViewBo.fromJson(json);
        ResponseResult rs = new ResponseResult();
        String msg = null;
        try {
            ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
            if (model == null) {
                msg = "can not get report define with id " + reportId;
            } else {
                Schema schema = service
                        .addOrModifyExtendMeasure(model.getSchema(), cubeId, extendMeasure);
                model.setSchema(schema);
                // if exception happened can not get here
                reportModelCacheManager.updateReportModelToCache(reportId, model);
                rs.setStatus(0);
                rs.setStatusInfo("successfully");
                return rs;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            msg = e.getCause().getMessage();
        }
        rs.setStatus(1);
        rs.setStatusInfo(msg);
        return rs;
    }
    
    /**
     * 
     * delete extend measure from cube
     * @param reportId -- report id
     * @param cubeId -- cube id
     * @param measuerId -- measure id
     * @param request -- HttpServletRequest
     * @return ResponseResult -- operation result
     * 
     */
    @RequestMapping(value = "/{reportId}/cubes/{cubeId}/extend_measures/{measureId}", 
            method = RequestMethod.DELETE)
    public ResponseResult delExtendMeasure(@PathVariable("reportId") String reportId,
            @PathVariable("cubeId") String cubeId,
            @PathVariable("measureId") String measureId,
            HttpServletRequest request) {
        ResponseResult rs = new ResponseResult();
        String msg = null;
        try {
            ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
            if (model == null) {
                msg = "can not get report define with id " + reportId;
            } else {
                Measure m = model.getSchema().getCubes().get(cubeId).getMeasures().get(measureId);
                model.getSchema().getCubes().get(cubeId)
                    .getMeasures().values().forEach(measure -> {
                        if (measure instanceof ExtendMinicubeMeasure && !measure.getId().equals(measureId) 
                                &&((ExtendMinicubeMeasure) measure).getRefIndNames().contains(m.getName())) {
                            throw new IllegalStateException(
                                    "current measure reference by measure " + measure );
                        }
                    });
                // remove extend area reference
                Schema schema = model.getSchema();
                if (model.getExtendAreas() != null && !model.getExtendAreas().isEmpty()) {
                    Map<String, ExtendArea> areas = DeepcopyUtils.deepCopy(model.getExtendAreas());
                    areas.values().forEach(area -> {
                        area.listAllItems().values().stream().filter(item -> {
                            return item != null && item.getOlapElementId().equals(measureId);
                        }).forEach(item -> {
                            ExtendArea tmp = model.getExtendAreas().get(area.getId());
                            tmp.removeItem(item.getId());
                        });
                    });
                }
                schema = service.delExtendMeasure(model.getSchema(), cubeId, measureId);
                model.setSchema(schema);
                // if exception happened can not get here
                reportModelCacheManager.updateReportModelToCache(reportId, model);
                rs.setStatus(0);
                rs.setStatusInfo("successfully");
                return rs;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            msg = e.getCause().getMessage();
        }
        rs.setStatus(1);
        rs.setStatusInfo(msg);
        return rs;
    }
    
    
    /**
     * query {@link Schema} with report's id
     * 
     * @param reportId -- report id
     * @return schema -- Schema
     */
    private Schema getSchema(String reportId) {
        ReportDesignModel model = null;
        try {
            model = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e1) {
            logger.debug("There are no such model in cache: " + reportId, e1);
        }
        logger.info("Try to get Schema from file. ");
        if (model == null) {
            model = reportDesignModelService.getModelByIdOrName(reportId, false);
        }
        Schema schema = model.getSchema();
        return schema;
    }
    
    /**
     * 
     * build successful operation result
     * 
     */
    private void buildSuccessResponseResult(String reportId, ResponseResult rs, Schema schema) {
        ReportDesignModel model = reportModelCacheManager.getReportModel(reportId);
        model.setSchema((MiniCubeSchema) schema);
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel (reportId);
        runtimeModel.setModel (model);
        reportModelCacheManager.updateRunTimeModelToCache (reportId, runtimeModel);
        reportModelCacheManager.updateReportModelToCache(reportId, model);
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
    }
    
    /**
     * 
     * check dimension's name
     * 
     * @param cubeId -- cube id
     * @param dimCaption -- dimension caption
     * @param schema -- schema
     * @return ResponseResult -- if invalidate return result else null
     */
    private ResponseResult checkDimName(String cubeId, String dimCaption, Schema schema) {
        ResponseResult rs = new ResponseResult();
        StandardDimension dim = new StandardDimension("");
        dim.setCaption(dimCaption);
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            rs.setStatus(1);
            rs.setStatusInfo("不能通过cubeid获取cube，请检查输入 id ： " + cubeId);
            return rs;
        }
        for (Map.Entry<String, Dimension> entry : cube.getDimensions().entrySet()) {
            if (dim.getCaption().equals(entry.getValue().getCaption())) {
                logger.error("name already exist");
                rs.setStatus(1);
                rs.setStatusInfo("名称已经存在");
                return rs;
            }
        }
        return null;
    }
    
    /**
     * 
     * check input
     * 
     * @param cubeId -- cube id
     * @param reportId -- report id
     * @param measureCaption -- measure's caption
     * @param schema -- schema
     * @return ResponseResult -- if invalidate return result else null
     */
    private ResponseResult checkSchema(String cubeId, String reportId, 
            String measureCaption, Schema schema) {
        ResponseResult rs = new ResponseResult();
        if (schema == null) {
            logger.debug("can't get report define with id : " + reportId);
            rs.setStatus(1);
            rs.setStatusInfo("不能获取报表定义");
            return rs;
        }
        
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can't get cube define with id : " + cubeId);
            rs.setStatus(1);
            rs.setStatusInfo("不能获取cube ：" + cubeId);
            return rs;
        }
        
        if (!StringUtils.isEmpty(measureCaption)) {
            for (Map.Entry<String, Measure> m : cube.getMeasures().entrySet()) {
                if (measureCaption.equals(m.getValue().getCaption())) {
                    rs.setStatus(1);
                    rs.setStatusInfo("名称已经存在");
                    return rs;
                }
            }
        }
        return null;
    }
    
    /**
     * convert JSON string to {@link Dimension}
     * @param json -- JSON style string
     * @return List<Dimension> -- dimension list
     */
    private List<Dimension> buidDimFromJson(String json) {
        List<StringMap<String>> tmp = convertJsonToObj(json);
        List<Dimension> rs = new ArrayList<Dimension>();
        for (StringMap<String> map : tmp) {
            StandardDimension dim = new StandardDimension("");
            dim.setId(map.get("id"));
            dim.setVisible(Boolean.valueOf(map.get("visible")));
            rs.add(dim);
        }
        return rs;
    }
    
    /**
     * 
     * convert JSON string to {@link Measure} list
     * 
     * @param json -- JSON style string
     * @return List<Measure> -- result
     * 
     */
    private List<Measure> buildMeasureFromJson(String json) {
        List<StringMap<String>> tmp = convertJsonToObj(json);
        List<Measure> rs = new ArrayList<Measure>();
        for (StringMap<String> map : tmp) {
            MiniCubeMeasure measure = new MiniCubeMeasure("");
            measure.setId(map.get("id"));
            measure.setVisible(Boolean.valueOf(map.get("visible")));
            rs.add(measure);
        }
        return rs;
    }
    
    /**
     * convert JSON string to 
     * 
     * @param json －－ JSON style string
     * @return List<StringMap<String>> -- result
     */
    @SuppressWarnings("unchecked")
    private List<StringMap<String>> convertJsonToObj(String json) {
        if (StringUtils.isEmpty(json)) {
            return new ArrayList<StringMap<String>>();
        }
        return GsonUtils.fromJson(json, List.class);
    }
    
}
