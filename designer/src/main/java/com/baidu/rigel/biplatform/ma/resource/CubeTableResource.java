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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.builder.Director;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.service.CubeMetaBuildService;
import com.baidu.rigel.biplatform.ma.model.service.StarModelBuildService;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.resource.cache.CacheManagerForResource;
import com.baidu.rigel.biplatform.ma.resource.cache.NameCheckCacheManager;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
public class CubeTableResource extends BaseResource {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(CacheManagerForResource.class);

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
     * dsGService
     */
    @Resource
    private DataSourceGroupService dsGService;

    /**
     * nameCheckCacheManager
     */
    @Resource
    private NameCheckCacheManager nameCheckCacheManager;

    /**
     * 
     * @param reportId
     * @param request
     * @return ResonseResult
     */
    @RequestMapping(value = "/{id}/star_models", method = { RequestMethod.POST })
    public ResponseResult saveCubeTables(@PathVariable("id") String reportId, HttpServletRequest request) {
        String dsId = request.getParameter("dataSourceId");
        // String dsGroupId = request.getParameter("dataSourceGroupId");
        /**
         * check ds
         */
        nameCheckCacheManager.existsDSName(dsId);

        String[] selectedTables = StringUtils.split(request.getParameter("selectedTables"), ",");
        String regexpStr = request.getParameter("regexps");

        // 获取正则表达式
        Map<String, DivideTableStrategyVo> regexps = Maps.newHashMap();
        if (!StringUtils.isEmpty(regexpStr)) {
            regexps =
                    GsonUtils.fromJson(request.getParameter("regexps"),
                            new TypeToken<Map<String, DivideTableStrategyVo>>() {
                            }.getType());
        }

        // 选中的表
        List<String> selectedList = Lists.newArrayList();
        if (selectedTables != null && selectedTables.length > 0) {
            selectedList.addAll(Lists.newArrayList(selectedTables));
        }

        List<FactTableMetaDefine> cubeTables = null;
        try {
            cubeTables = cubeBuildService.initCubeTables(dsId, selectedList, regexps, securityKey);
        } catch (DataSourceOperationException e1) {
            logger.error("Fail in getting table info from datasource. ", e1);
            return ResourceUtils.getErrorResult("未能从数据库中查到相关表定义信息，原因 " + e1.getLocalizedMessage(), 1);
        }
        List<StarModel> models = starModelBuildService.buildStarModel(dsId, cubeTables);
        MiniCubeSchema schema = (MiniCubeSchema) director.getSchema(models.toArray(new StarModel[0]));
        ReportDesignModel report;
        try {
            report = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("Can not get Report from cache. ", e);
            return ResourceUtils.getErrorResult("未能查到报表定义信息， 由于：" + e.getLocalizedMessage(), 1);
        }
        report.setSchema(schema);
        report.setDsId(dsId);
        // report.setDsGroupId(dsGroupId);

        reportModelCacheManager.updateReportModelToCache(reportId, report);
        report = reportModelCacheManager.getReportModel(reportId);
        logger.info("put Schema model to cache");
        ResponseResult rs = ResourceUtils.getResult("successfully", "can not save schema", "");
        logger.info("put operation rs is : " + rs.toString());
        return rs;
    }

    /**
     * 
     * @param reportId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/ds_id", method = { RequestMethod.GET })
    public ResponseResult getSelectedDS(@PathVariable("id") String reportId, HttpServletRequest request) {
        ReportDesignModel reportModel = null;
        try {
            reportModel = reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e) {
            logger.error("Can not get Report from cache. ", e);
            return ResourceUtils.getErrorResult("未能查到报表定义信息， 由于：" + e.getLocalizedMessage(), 1);
        }
        ResponseResult rs = null;
        Map<String, Object> data = Maps.newHashMap();
        if (reportModel != null) {
            try {
                // 兼容前端重构逻辑
                if (StringUtils.isEmpty(reportModel.getDsId())) {
                    rs = ResourceUtils.getCorrectResult("Success", null);
                    return rs;
                }
                DataSourceGroupDefine dsG = dsGService.getDataSourceGroupDefine(reportModel.getDsId());
                if (dsG == null) {
                    logger.error("can't get ds group with id " + reportModel.getDsId());
                    rs = ResourceUtils.getErrorResult("failed", 1);
                } else {
                    DataSourceDefine ds = dsG.getActiveDataSource();
                    if (ds == null) {
                        logger.error("can't get report id " + reportModel.getId());
                        rs = ResourceUtils.getCorrectResult("failed", 1);
                    } else {
                        // 组的id
                        data.put("selectedGroupId", reportModel.getDsId());
                        // 数据源id
                        data.put("selectedDatasourceId", ds.getId());
                        rs = ResourceUtils.getCorrectResult("Success", data);
                    }
                }
            } catch (DataSourceOperationException e) {
                logger.error("can't get ds group with id " + reportModel.getDsId());
                rs = ResourceUtils.getErrorResult("failed", 1);
            }
        } else {
            rs = ResourceUtils.getErrorResult("failed", 1);
        }
        logger.info("put operation rs is : " + rs.toString());
        return rs;
    }

    @RequestMapping(value = "/{id}/cube_tables", method = { RequestMethod.GET })
    public ResponseResult getCubeTables(@PathVariable("id") String id) {

        ReportDesignModel reportModel = null;
        try {
            reportModel = reportModelCacheManager.getReportModel(id);
        } catch (CacheOperationException e) {
            logger.error("Can not get Report from cache. ", e);
            return ResourceUtils.getErrorResult("未能查到报表定义信息， 由于：" + e.getLocalizedMessage(), 1);
        }

        if (reportModel == null) {
            return ResourceUtils.getErrorResult("failed", 1);
        }
        StarModel[] stars = director.getStarModel(reportModel.getSchema());
        List<String> tables = Lists.newArrayList();
        Map<String, DivideTableStrategyVo> regExps = Maps.newHashMap();
        for (StarModel star : stars) {
            DivideTableStrategyVo strategy = star.getFactTable().getDivideTableStrategyVo();
            if (strategy != null) {
                tables.addAll(star.getFactTable().getRegExpTables());
                regExps.put(star.getFactTable().getName(), strategy);
            } else {
                tables.add(star.getFactTable().getName());
            }
        }
        Map<String, Object> data = Maps.newHashMap();
        data.put("selected", tables.toArray(new String[0]));
        data.put("regexps", regExps);
        ResponseResult rs = ResourceUtils.getCorrectResult("success", data);
        return rs;
    }

}