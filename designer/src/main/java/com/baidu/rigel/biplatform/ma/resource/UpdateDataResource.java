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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.MiniCubeDriverManager;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
//import com.baidu.rigel.biplatform.ma.report.service.ReportNoticeByJmsService;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * 同步更新数据rest接口，用于提供同步数据更新支持
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad")
public class UpdateDataResource extends BaseResource {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(UpdateDataResource.class);
    
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /**
     * dsService
     */
    @Resource
    private DataSourceGroupService dsgService;
    
    /**
     * dsgService
     */
    @Resource
    private DataSourceGroupService dataSourceGroupService;
    
    /**
     * reportDesignModelService
     */
    @Resource(name = "reportDesignModelService")
    private ReportDesignModelService reportDesignModelService;

    /**
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return ResponseResult
     */
    @RequestMapping(value = "/reports/dataupdate", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult updateData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("[INFO] --- --- begin update index meta with new request");
        long begin = System.currentTimeMillis();
        String dsName = request.getParameter("dsName");
        String factTables = request.getParameter("factTables");
        if (StringUtils.isEmpty(dsName) || StringUtils.isEmpty(factTables)) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(1);
            rs.setStatusInfo("请求中需要包含dsName, factTables信息。"
                    + "其中dsName为数据源名称，factTables为更新的事实表列表，多张表以’,‘分割");
            return rs;
        }
        String[] factTableArray = factTables.split(",");
        ResponseResult rs = new ResponseResult();
        DataSourceGroupDefine dsgDefine = dsgService.getDataSourceGroupDefine(Integer.toString(dsName.hashCode()));
        DataSourceDefine dsDefault = dsgDefine.getActiveDataSource();
        DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory.
                getDataSourceConnectionServiceInstance(dsDefault.getDataSourceType().toString ());
        List<DataSourceInfo> dsInfoList = dsConnService.getActivedDataSourceInfoList(dsgDefine, securityKey);

        Map<String, Map<String, String>> conds = Maps.newHashMap();
        for (String factTable : factTableArray) {
            String str = request.getParameter(factTable);
            LOG.info("[INFO] --- --- conditions for {} is : {}", factTable, str);
            if (isValidate(str)) {
                conds.put(factTable, GsonUtils.fromJson(str, new TypeToken<Map<String, String>>() {}.getType()));
            }
        }
        String condsStr = null;
        if (conds.size() > 0) {
            condsStr = GsonUtils.toJson(conds);
        }
        LOG.info("[INFO] --- --- conds : {}", conds);
        LOG.info("[INFO] --- --- request params list ----------------------------------");
        LOG.info("[INFO] --- --- dsName = {}", dsName);
        LOG.info("[INFO] --- --- factTables = {}", factTables);
        LOG.info("[INFO] --- --- conds = {}", condsStr);
        LOG.info("[INFO] --- --- --- ---- ---- end pring param list --- --- --- --- ---- ");
        boolean result = MiniCubeConnection.ConnectionUtil.refresh(dsInfoList, factTableArray, condsStr);
//        reportNoticeByJmsService.refreshIndex(dsInfo, factTableArray, condsStr);
        if (result) {
            rs.setStatus(0);
            rs.setStatusInfo("successfully");
        } else {
            rs.setStatus(1);
            rs.setStatusInfo("failed");
        }
        LOG.info("[INFO] -- --- update index meta result : {}", result);
        LOG.info("[INFO] --- --- end update index meta, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }
    
    /**
     * 
     * @param request
     *            HttpServletRequest
     * @param response
     *            HttpServletResponse
     * @return ResponseResult
     */
    @RequestMapping(value = "/index/publish", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult publish(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        LOG.info("[INFO] --- --- begin create index meta with new request");
        String reportId = request.getParameter("reportId");
        ResponseResult rs = new ResponseResult();
        if (StringUtils.isEmpty(reportId)) {
            rs.setStatus(1);
            rs.setStatusInfo("请求中需要包含reportId信息。其中reportId为报表ID或报表名称");
            return rs;
        }
        ReportDesignModel model = this.reportDesignModelService.getModelByIdOrName(reportId, false);
        /**
         * 发布
         */
        List<DataSourceInfo> dsInfoList;
        try {
            
            DataSourceGroupDefine dataSourceGroupDefine = dsgService.getDataSourceGroupDefine(model
                    .getDsId());
            DataSourceDefine dsDefineActived = dsService.getDsDefine(model.getDsId());
            DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory
                    .getDataSourceConnectionServiceInstance(dsDefineActived.getDataSourceType()
                            .toString());
            dsInfoList = dsConnService.getActivedDataSourceInfoList(dataSourceGroupDefine,
                    securityKey);
        } catch (DataSourceOperationException e) {
            LOG.error("Fail in Finding datasource define. ", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Fail in parse datasource to datasourceInfo.", e);
            throw new DataSourceOperationException(e);
        }
        List<Cube> cubes = Lists.newArrayList();
        for (ExtendArea area : model.getExtendAreaList()) {
            try {
                if ((area.getType() != ExtendAreaType.TABLE
                        && area.getType() != ExtendAreaType.LITEOLAP_TABLE
                        && area.getType() != ExtendAreaType.CHART && area.getType() != ExtendAreaType.LITEOLAP_CHART)
                        || area.getType() == ExtendAreaType.PLANE_TABLE
                        || QueryUtils.isFilterArea(area.getType())) {
                    continue;
                }
                Cube cube = QueryUtils.getCubeWithExtendArea(model, area);
                cubes.add(cube);
            } catch (QueryModelBuildException e) {
                LOG.warn("It seems that logicmodel of area is null. Ingore this area. ");
                continue;
            }
        }
        /** palo不需要通知tesseract建立索引，与tesseract直接建立接口 **/
        if (CollectionUtils.isNotEmpty(dsInfoList) && dsInfoList.get(0) != null
                && dsInfoList.get(0) instanceof SqlDataSourceInfo) {
            SqlDataSourceInfo sqlDataSourceInfo = (SqlDataSourceInfo) dsInfoList.get(0);
            if (sqlDataSourceInfo.getDataBase() != DataBase.PALO
                    && sqlDataSourceInfo.getDataBase() != DataBase.DRUID) {
                if (cubes.size() == 0) {
                    LOG.info("cube is empty, don't need to create index!");
                    rs.setStatus(1);
                    rs.setStatusInfo("cube is empty, don't need to create index!");
                    return rs;
                }
                LOG.info("report published successfully, begin to request createIndex.. ,"
                        + "databasetype:{}, productline:{}, reportName:{}.", sqlDataSourceInfo
                        .getDataBase().name(), ContextManager.getProductLine(), model.getName());
                new Thread() {
                    public void run() {
                        MiniCubeConnection connection = MiniCubeDriverManager
                                .getConnection(dsInfoList.get(0));
                        if (connection.publishCubes(cubes, dsInfoList)) {
                            LOG.info("request of createIndex successfully, reportName:{}.",
                                    model.getName());
                        } else {
                            LOG.warn("request of createIndex failed!! reportName:{}.",
                                    model.getName());
                        }
                    }
                }.start();
            } else {
                LOG.info("report published successfully, databasetype:{},"
                        + "productline:{}, reportName:{}.", sqlDataSourceInfo.getDataBase().name(),
                        ContextManager.getProductLine(), model.getName());
                rs.setStatus(1);
                rs.setStatusInfo("only Mysql of the data source to create an index.");
                return rs;
            }
        } else {
            LOG.warn("report published successfully, can not found SqlDataSourceInfo,"
                    + "productline:{}, reportName:{}.", ContextManager.getProductLine(),
                    model.getName());
            rs.setStatus(1);
            rs.setStatusInfo("can not found SqlDataSourceInfo from this report.");
            return rs;
        }
        rs.setStatus(0);
        rs.setStatusInfo("create index successfull.");
        return rs;
    }

    private boolean isValidate(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(str);
            if (StringUtils.isEmpty(json.getString("begin")) || StringUtils.isEmpty(json.getString("end"))) {
                throw new IllegalStateException("request param status incorrected");
            }
            Long begin = Long.valueOf(json.getString("begin"));
            if (begin <= 0) {
                throw new IllegalStateException("begin value need bigger than zero");
            }
            Long end = Long.valueOf(json.getString("end"));
            if (end < begin) {
                throw new IllegalStateException("end value must larger than begin");
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw new IllegalStateException("request param must be json style");
        }
        return true;
    }
}
