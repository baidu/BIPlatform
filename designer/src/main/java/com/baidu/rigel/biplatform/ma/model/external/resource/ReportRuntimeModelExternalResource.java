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
package com.baidu.rigel.biplatform.ma.model.external.resource;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.external.service.MeasureClassfyService;
import com.baidu.rigel.biplatform.ma.model.external.utils.MeasureClassfyMetaUtils;
import com.baidu.rigel.biplatform.ma.model.external.vo.MeasureClassfyObject;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.resource.BaseResource;
import com.baidu.rigel.biplatform.ma.resource.ReportRuntimeModelManageResource;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.QueryDataResourceUtils;
import com.google.common.collect.Lists;

/**
 *Description:
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad/reports/")
public class ReportRuntimeModelExternalResource extends BaseResource {
    
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReportRuntimeModelManageResource.class);

    /**
     * cache manager
     */
    @Resource(name = "reportModelCacheManager")
    private ReportModelCacheManager reportModelCacheManager;

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
    
    @Resource
    private MeasureClassfyService measureClassfyService;
    
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /**
     * 根据用户提供的变换的指标信息，修改逻辑模型
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     * @throws Exception
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/changedMeasures", method = RequestMethod.POST)
    public ResponseResult modifyLogicModel(@PathVariable("reportId") String reportId, 
            @PathVariable("areaId") String areaId, HttpServletRequest request) 
            throws Exception {
        String selectedMeasures = request.getParameter ("selectedMeasures");
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            if (StringUtils.isNotEmpty (request.getParameter ("reportImageId"))) {
                reportId = request.getParameter ("reportImageId");
            }
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            LOG.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        Assert.notNull (runTimeModel);
        ReportDesignModel model = runTimeModel.getModel ();
        ExtendArea extendArea = model.getExtendById (areaId);
        if (extendArea.getType () != ExtendAreaType.TABLE) {
            throw new UnsupportedOperationException ("未支持的区域类型");
        }
        modifyLogicModel (areaId, selectedMeasures, model, extendArea);
        // 已经重置了LogicModel，需要清除操作纪录
        // restOtherStatus (runTimeModel);
        MiniCube cube = (MiniCube) model.getSchema ().getCubes ().get (extendArea.getCubeId ());
        LogicModel logicModel = extendArea.getLogicModel ();
        DataSourceDefine ds = dsService.getDsDefine (model.getDsId ());
        List<MeasureClassfyObject> rs = getMeasureclassfyDefine(cube.getSource (), ds, logicModel, cube, securityKey,areaId);
        changeMeasureDesc (extendArea, rs);
        reportModelCacheManager.updateRunTimeModelToCache (reportId, runTimeModel);
        result.setStatus (0);
        result.setStatusInfo ("successfully");
        return result;
    }


    /**
     * 根据选择的指标修改动态逻辑模型
     * @param areaId 区域id
     * @param selectedMeasures 选中的指标列表
     * @param model ReportDesignModel
     * @param extendArea 扩展区域
     */
    private void modifyLogicModel(String areaId, 
            String selectedMeasures, 
            ReportDesignModel model, ExtendArea extendArea) {
        LogicModel logicModel = extendArea.getLogicModel ();
        Cube cube = model.getSchema ().getCubes ().get (extendArea.getCubeId ());
        Cube newCube = QueryUtils.transformCube (DeepcopyUtils.deepCopy (cube));
        if (!StringUtils.isBlank (selectedMeasures)) {
            List<Item> columns = Lists.newArrayList ();
            for (String indName : selectedMeasures.split (",")) {
                Measure m = newCube.getMeasures ().get (indName);
                Item item = new Item();
                item.setAreaId (areaId);
                item.setCubeId (cube.getId ());
                item.setOlapElementId (m.getId ());
                item.setPositionType (PositionType.Y);
                item.setReportId (model.getId ());
                item.setSchemaId (cube.getSchema ().getId ());
                columns.add (item);
            }
            List<Item> tmp = Lists.newArrayList ();
            // 对于计算列，需要保留配置
            for (Item item : logicModel.getColumns ()) {
                if (columns.contains (item)) {
                    columns.remove (item);
                    tmp.add (item);
//                } else {
//                    Measure m = cube.getMeasures ().get (item.getOlapElementId ());
//                    if (m.getType () == MeasureType.CAL || m.getType () == MeasureType.CALLBACK) {
//                        columns.add (item);
//                    }
                }
            }
            columns.addAll (0, tmp);
            logicModel.resetColumns (columns.toArray (new Item[0]));
        }
    }
    
    /**
     * 获取可选的变换指标列表
     * @param reportId
     * @param areaId
     * @param request
     * @return ResponseResult
     * @throws Exception
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/changablemeasures", method = RequestMethod.POST)
    public ResponseResult queryChangeableMeasures(@PathVariable("reportId") String reportId, 
            @PathVariable("areaId") String areaId, HttpServletRequest request) 
            throws Exception {
        ResponseResult result = new ResponseResult();
        ReportRuntimeModel runTimeModel = null;
        try {
            runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        } catch (CacheOperationException e1) {
            LOG.info("[INFO] There are no such model in cache. Report Id: " + reportId, e1);
            result.setStatus(1);
            result.setStatusInfo("未能获取正确的报表定义");
            return result;
        }

        Assert.notNull (runTimeModel);
        ReportDesignModel model = runTimeModel.getModel ();
        ExtendArea extendArea = model.getExtendById (areaId);
        if (extendArea.getType () != ExtendAreaType.TABLE) {
            throw new UnsupportedOperationException ("未支持的区域类型");
        }
        LogicModel logicModel = extendArea.getLogicModel ();
        MiniCube cube = (MiniCube) model.getSchema ().getCubes ().get (extendArea.getCubeId ());
        DataSourceDefine ds = dsService.getDsDefine (model.getDsId ());
        List<MeasureClassfyObject> rs = getMeasureclassfyDefine(cube.getSource (), ds, logicModel, cube, securityKey,areaId);
        changeMeasureDesc (extendArea, rs);
        result.setStatus (0);
        result.setData (rs);
        result.setStatusInfo ("successfully");
        reportModelCacheManager.updateRunTimeModelToCache (reportId, runTimeModel);
        return result;
    }

    private void changeMeasureDesc(ExtendArea extendArea,
            List<MeasureClassfyObject> rs) {
        List<MeasureClassfyObject> leafObj = getLeafMeasures(rs);
        leafObj.forEach (tmp -> {
            extendArea.getFormatModel ().getToolTips ().put (tmp.getName (), tmp.getDesc());
        });
    }

    private List<MeasureClassfyObject> getLeafMeasures(List<MeasureClassfyObject> rs) {
        List<MeasureClassfyObject> leaf = Lists.newArrayList ();
        if (CollectionUtils.isEmpty (rs)) {
            return leaf;
        }
        for (MeasureClassfyObject obj : rs) {
            if (CollectionUtils.isEmpty (obj.getChildren ())) {
                leaf.add (obj);
            } else {
                leaf.addAll (getLeafMeasures(obj.getChildren ()));
//                for (MeasureClassfyObject child : obj.getChildren ()) {
//                    if (CollectionUtils.isEmpty (child.getChildren ())) {
//                        leaf.add (child);
//                    } else {
//                        child.getChildren ().forEach (c -> leaf.add (c));
//                    }
//                }
            }
        }
        return leaf;
    }

    /**
     * 
     * @param source
     * @param ds
     * @param logicModel
     * @param securityKey
     * @return List<MeasureClassfyObject>
     */
    private List<MeasureClassfyObject> getMeasureclassfyDefine(String source, 
            DataSourceDefine ds, LogicModel logicModel, Cube cube,
            String securityKey, String areaId) throws Exception {
        List<MeasureClassfyObject> measureClassfyMetas = 
            measureClassfyService.getChangableMeasureClassfyMeta (source, ds, securityKey,areaId);
        measureClassfyMetas = 
            MeasureClassfyMetaUtils.changeIndMetaSelectStatus (source, logicModel, cube, measureClassfyMetas);
        return measureClassfyMetas;
    }

    
}
