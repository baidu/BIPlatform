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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeSchema;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.MiniCubeDriverManager;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableFormat;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableFormat.PaginationSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
//import com.baidu.rigel.biplatform.ma.report.service.ReportNoticeByJmsService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * ReportModel服务接口实现
 * 
 * @author david.wang
 *
 */
@Service("reportDesignModelService")
public class ReportDesignModelServiceImpl implements ReportDesignModelService {

    /**
     * 文件描述分割正则表达式
     */
    private static final String FILE_SPLIT_REG = "\\^_\\^";

    /**
     * 文件管理服务
     */ 
    @Resource
    private FileService fileService;

    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /**
     * dsgService
     */
    @Resource
    private DataSourceGroupService dsgService;

    @Value("${biplatform.ma.report.location}")
    private String reportBaseDir;
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 构造函数
     * 
     */
    public ReportDesignModelServiceImpl() {

    }
    
    /**
     * {@inheritDoc}
     * @throws DataSourceOperationException 
     */
    @Override
    public boolean publishReport(ReportDesignModel model, String securityKey)
            throws ReportModelOperationException, DataSourceOperationException {
        
        boolean result = false;
        String devReportLocation = this.generateDevReportLocation(model);
        String realeaseLocation = this.getReleaseReportLocation(model);
        try {
            // 删除原来已经发布的报表，如果不存在，忽略此处异常
            ReportDesignModel tmp = this.getModelByIdOrName (model.getId (), true);
            if (tmp != null) {
                try {
                    fileService.rm(getReleaseReportLocation(tmp));
                } catch (Exception e) {
                    fileService.rm(getOriReleaseReportLocation(tmp));
                }
            }
        } catch (FileServiceException e1) {
            logger.info (e1.getMessage (), e1);
        }
        try {
            result = this.fileService.copy(devReportLocation, realeaseLocation);
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
            throw new ReportModelOperationException("发布报表失败！");
        }
        if (!result) {
            logger.error("拷贝报表失败！");
            throw new ReportModelOperationException("发布报表失败！");
        }
        /**
         * 发布
         */
        List<DataSourceInfo> dsInfoList;
        try {
            
            DataSourceGroupDefine dataSourceGroupDefine = dsgService.getDataSourceGroupDefine(model.getDsId());
            DataSourceDefine dsDefineActived = dsService.getDsDefine(model.getDsId());
            DataSourceConnectionService<?> dsConnService = DataSourceConnectionServiceFactory.
                    getDataSourceConnectionServiceInstance(dsDefineActived.getDataSourceType().toString ());
            dsInfoList = dsConnService.getActivedDataSourceInfoList(dataSourceGroupDefine, securityKey);
        } catch (DataSourceOperationException e) {
            logger.error("Fail in Finding datasource define. ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Fail in parse datasource to datasourceInfo.", e);
            throw new DataSourceOperationException(e);
        }
        List<Cube> cubes = Lists.newArrayList();
        for (ExtendArea area : model.getExtendAreaList()) {
            try {
                if ((area.getType() != ExtendAreaType.TABLE 
                        && area.getType () != ExtendAreaType.LITEOLAP_TABLE
                        && area.getType() != ExtendAreaType.CHART
                        && area.getType () != ExtendAreaType.LITEOLAP_CHART)
                        || area.getType () == ExtendAreaType.PLANE_TABLE
                        || QueryUtils.isFilterArea(area.getType())) {
                    continue;
                }
                Cube cube = QueryUtils.getCubeWithExtendArea(model, area);
                cubes.add(cube);
            } catch (QueryModelBuildException e) {
                logger.warn("It seems that logicmodel of area is null. Ingore this area. ");
                continue;
            }
        }
        return true;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel[] queryAllModels(boolean released) {
        try {
            String[] listFile = null;
            if (released) {
                listFile = fileService.ls(this.getReleaseReportDir ());
            } else {
                listFile = fileService.ls(this.getDevReportDir());
            }
            if (listFile == null || listFile.length == 0) {
                return new ReportDesignModel[0];
            }
            ReportDesignModel[] modelList = buildResult(listFile, released);
            List<ReportDesignModel> reportList = Arrays.asList(modelList);
            Collections.sort(reportList, new Comparator<ReportDesignModel>() {

                @Override
                public int compare(ReportDesignModel o1, ReportDesignModel o2) {
                    if (o1 == null || StringUtils.isEmpty(o1.getName())) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 0;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return reportList.toArray(new ReportDesignModel[0]);

        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
        }
        return new ReportDesignModel[0];
    }

    /**
     * 通过文件内容构建报表模型
     * 
     * @param listFile
     * @param released 
     * @return
     */
    private ReportDesignModel[] buildResult(String[] listFile, boolean released) {
        final List<ReportDesignModel> rs = new ArrayList<ReportDesignModel>();
        String reportDir = null; 
        if (released) {
            reportDir = this.getReleaseReportDir ();
        } else {
            reportDir = getDevReportDir();
        }
        for (final String f : listFile) {
            if (f.contains(".")) {
                continue;
            }
            try {
                byte[] content = fileService.read(reportDir + File.separator
                        + f);
                ReportDesignModel model = (ReportDesignModel) SerializationUtils
                        .deserialize(content);
                rs.add(model);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return rs.toArray(new ReportDesignModel[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel getModelByIdOrName(String idOrName,
            boolean isRelease) {
        String baseDir = null;
        if (!isRelease) {
            baseDir = getDevReportDir();
        } else {
            baseDir = getReleaseReportDir();
        }
        String[] modelFileList = null;
        try {
            modelFileList = fileService.ls(baseDir);
        } catch (FileServiceException e) {
            logger.debug(e.getMessage(), e);
        }
        if (modelFileList == null || modelFileList.length == 0) {
            logger.warn("can not get report model define in directory: "
                    + baseDir);
            return null;
        }

        try {
            for (String modelFile : modelFileList) {
                if (modelFile.contains(".")) {
                    continue;
                }
                String[] tmpArray = modelFile.split(FILE_SPLIT_REG);
                if (idOrName.equals(tmpArray[0])
                        || tmpArray[1].equals(idOrName)
                        || String.valueOf(idOrName.hashCode()).equals(
                                tmpArray[1])) {
                    byte[] content = fileService.read(baseDir + File.separator
                            + modelFile);
                    ReportDesignModel model = (ReportDesignModel) SerializationUtils
                            .deserialize(content);
                    return model;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 升级方法
     * 
     * @param model
     * @return String
     */
    @Deprecated
    private String generateOriDevReportLocation(ReportDesignModel model) {
        if (model == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getDevReportDir());
        builder.append(File.separator);
        String name = null;
        try {
            String[] listFile = fileService.ls(this.getDevReportDir());
            for (String file : listFile) {

                if (file.startsWith(model.getId())) {
                    name = file;
                    builder.append(name);
                    break;
                }
            }
        } catch (FileServiceException e) {
            logger.error(e.getMessage (), e);
        }
        if (name == null) {
            builder.append(model.getId());
            builder.append(Constants.FILE_NAME_SEPERATOR);
            builder.append(name);
            builder.append(Constants.FILE_NAME_SEPERATOR);
            builder.append(model.getDsId());
        }
        return builder.toString();
    }

    public boolean isNameExist(String name, String id) {
        if (name == null) {
            return false;
        }
        String[] listFile = null;
        try {
            listFile = fileService.ls(this.getDevReportDir());
        } catch (FileServiceException e) {
            logger.debug(e.getMessage(), e);
        }
        if (listFile == null || listFile.length == 0) {
            return false;
        }
        String idTarget = id + Constants.FILE_NAME_SEPERATOR;
        String nameTarget = Constants.FILE_NAME_SEPERATOR + name.hashCode()
                + Constants.FILE_NAME_SEPERATOR;
        for (String file : listFile) {
            // 名称相同，id不相同
            if (!file.startsWith(idTarget) && file.contains(nameTarget)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNameExist(String name) {
        if (name == null) {
            return false;
        }
        String[] listFile = null;
        try {
            listFile = fileService.ls(this.getDevReportDir());
        } catch (FileServiceException e) {
            logger.debug(e.getMessage(), e);
        }
        if (listFile == null || listFile.length == 0) {
            return false;
        }
        for (String file : listFile) {
            if (file.contains(".")) {
                continue;
            }
            String[] tmpArray = file.split(FILE_SPLIT_REG);
            if (name.equals(tmpArray[0]) || name.equals(tmpArray[1])
                    || String.valueOf(name.hashCode ()).equals(tmpArray[1])) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel saveOrUpdateModel(ReportDesignModel model)
            throws ReportModelOperationException {
        if (model == null) {
            logger.warn("current model is null");
            throw new ReportModelOperationException("model can not be null");
        }
        if (StringUtils.isEmpty(model.getName())) {
            logger.debug("model's name can not be empty");
            throw new ReportModelOperationException(
                    "model's name can not be empty");
        }
        if (StringUtils.isEmpty(model.getId())) {
            model.setId(UuidGeneratorUtils.generate());
        }
        try {
            ReportDesignModel oldReport = getModelByIdOrName(model.getId(),
                    false);
            if (oldReport != null) {
                try {
                    this.deleteModel(oldReport, true);
                } catch (Exception e) {
                    try {
                        fileService.rm(generateOriDevReportLocation(model));
                    } catch (Exception e1) {
                        logger.error (e.getMessage (), e);
                    }
                }
            }
            boolean rs = fileService.write(generateDevReportLocation(model),
                    SerializationUtils.serialize(model));
            if (rs) {
                return model;
            }
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportDesignModel copyModel(String src, String targetName)
            throws ReportModelOperationException {
        if (StringUtils.isEmpty(src)) {
            logger.warn("source name is empty");
            throw new ReportModelOperationException("source name is empty");
        }
        if (StringUtils.isEmpty(targetName)) {
            logger.warn("target name is empty");
            throw new ReportModelOperationException("target name is empty");
        }
        if (isNameExist(targetName)) {
            throw new ReportModelOperationException(
                    "target name already exists: " + targetName);
        }

        if (isNameExist(src)) {
            ReportDesignModel model = getModelByIdOrName(src, false);
            model.setId(UuidGeneratorUtils.generate());
            model.setName(targetName);
            MiniCubeSchema schema = (MiniCubeSchema) model.getSchema();
            if (schema != null) {
                schema.setId(UuidGeneratorUtils.generate());
            }
            return saveOrUpdateModel(model);
        }
        throw new ReportModelOperationException("source not exists: " + src);
    }

    /**
     * 获取开发状态报表存储路径
     * 
     * @return
     */
    private String getDevReportDir() {
        String productLine = ContextManager.getProductLine();
        return productLine + File.separator + reportBaseDir + File.separator
                + "dev";
    }

    /**
     * 获取开发状态报表存储路径
     * 
     * @return
     */
    private String getReleaseReportDir() {
        String productLine = ContextManager.getProductLine();
        return productLine + File.separator + reportBaseDir + File.separator
                + "release";
    }

    /**
     * 获取发布的报表的存储路径
     * 
     * @return
     */
    private String getReleaseReportLocation(ReportDesignModel model) {
        if (model == null) {
            return null;
        }
        String productLine = ContextManager.getProductLine();
        return productLine + File.separator + reportBaseDir + File.separator
                + "release" + File.separator + model.getId()
                + Constants.FILE_NAME_SEPERATOR + model.getName().hashCode();
    }

    /**
     * 依据model对象生成持久化文件名称
     * 
     * @param model
     * @return
     */
    private String generateDevReportLocation(ReportDesignModel model) {
        if (model == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getDevReportDir());
        builder.append(File.separator);
        builder.append(model.getId());
        builder.append(Constants.FILE_NAME_SEPERATOR);
        builder.append(model.getName().hashCode());
        builder.append(Constants.FILE_NAME_SEPERATOR);
        builder.append(model.getDsId());
        return builder.toString();
    }

    

    // @Resource
    // private ReportNoticeByJmsService reportNoticeByJmsService=null;

    @Deprecated
    private String getOriReleaseReportLocation(ReportDesignModel model) {
        if (model == null) {
            return null;
        }
        String name = model.getName();
        String productLine = ContextManager.getProductLine();
        try {
            String[] listFile = fileService.ls(this.getReleaseReportDir());
            for (String file : listFile) {
                if (file.startsWith(model.getId())) {
                    return productLine + File.separator + reportBaseDir
                            + File.separator + "release" + File.separator
                            + file;
                }
            }
        } catch (FileServiceException e) {
            logger.error (e.getMessage (), e);
        }
        return productLine + File.separator + reportBaseDir + File.separator
                + "release" + File.separator + model.getId()
                + Constants.FILE_NAME_SEPERATOR + name;
    }

    @Override
    public void updateAreaWithDataFormat(ExtendArea area, String dataFormat) {
        FormatModel model = area.getFormatModel();
        model.getDataFormat().putAll(convertStr2Map(dataFormat));
    }

    /**
     * 讲json串转换为map
     * 
     * @param dataFormat
     * @return Map<String, String>
     */
    private Map<String, String> convertStr2Map(String dataFormat) {
        try {
            JSONObject json = new JSONObject(dataFormat);
            Map<String, String> rs = Maps.newHashMap();
            for (String str : JSONObject.getNames(json)) {
                rs.put(str, json.getString(str));
            }
            return rs;
        } catch (JSONException e) {
            throw new IllegalArgumentException("数据格式必须为Json格式， dataFormat = "
                    + dataFormat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAreaWithToolTips(ExtendArea area, String toolTips) {
        logger.info("[INFO] update tooltips define with : " + toolTips);
        FormatModel model = area.getFormatModel();
        model.getToolTips().putAll(convertStr2Map(toolTips));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAreaWithTopSetting(ExtendArea area, String topSetting) {
        logger.info("[INFO] receive user top N setting define : " + topSetting);
        MeasureTopSetting setting = GsonUtils.fromJson(topSetting,
                MeasureTopSetting.class);
        setting.setAreaId(area.getId());
        area.getLogicModel().setTopSetting(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAreaWithOtherSetting(ExtendArea area, String otherSetting) {
        @SuppressWarnings("unchecked")
        Map<String, Object> setting = GsonUtils.fromJson(otherSetting,
                HashMap.class);
        area.setOtherSetting(setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> lsReportWithDsId(String id) {
        String[] modelFileList = null;
        try {
            modelFileList = fileService.ls(getDevReportDir());
        } catch (FileServiceException e) {
            logger.debug(e.getMessage(), e);
            return Lists.newArrayList();
        }
        if (modelFileList == null || modelFileList.length == 0) {
            return Lists.newArrayList();
        }
        List<String> rs = Lists.newArrayList();
        for (String str : modelFileList) {
            if (str.contains(id)) {
                rs.add(str.substring(
                        str.indexOf(Constants.FILE_NAME_SEPERATOR),
                        str.lastIndexOf(Constants.FILE_NAME_SEPERATOR))
                        .replace(Constants.FILE_NAME_SEPERATOR, ""));
            }
        }
        return rs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateReportModel(ReportDesignModel model,
            boolean modelInCache) {
        ReportDesignModel persModel = DeepcopyUtils.deepCopy(model);
        // 如果当前model在编辑状态，需要更新持久化的model的name
        if (isNameExist(model.getName(), model.getId())) {
            return false;
        }
        if (modelInCache) {
            persModel = getModelByIdOrName(model.getId(), false);
        }
        try {
            try {
                this.deleteModel(persModel, true);
            } catch (Exception e) {
                try {
                    fileService.rm(generateOriDevReportLocation(persModel));
                } catch (FileServiceException e1) {
                    throw new ReportModelOperationException(e1);
                }
            }
            persModel.setName(model.getName());
            this.saveOrUpdateModel(persModel);
        } catch (ReportModelOperationException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 
     * @param model
     * @param removeFromDisk
     */
    private void deleteModel(ReportDesignModel model, boolean removeFromDisk)
            throws ReportModelOperationException {
        try {
            // TODO 升级兼容 后续考虑删除
            fileService.rm(generateDevReportLocation(model));
            logger.info("delete report successfully");
        } catch (FileServiceException e) {
            logger.warn(e.getMessage(), e);
            throw new ReportModelOperationException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean deleteModel(String id, boolean removeFromDisk)
            throws ReportModelOperationException {
        try {
            ReportDesignModel model = this.getModelByIdOrName(id, false);
            if (model != null) {
                boolean result = true;
                // 如果存在发布态报表，删除发布态报表
                if (this.getModelByIdOrName(id, true) != null) {
                    /**
                     * 已经发布了，不能删除 TODO 应该有下线操作，下线以后把发布的报表删除，才能够进一步删除正在开发的报表
                     */
                    return false;
                }
                result = fileService.rm(generateDevReportLocation(model));
                // 尝试删除原有命名方式命名的报表
                if (!result) {
                    result = fileService
                            .rm(generateOriDevReportLocation(model));
                }
                logger.info("delete report "
                        + (result ? "successfully" : "failed"));
                return result;
            }
            return false;
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
            throw new ReportModelOperationException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAreaColorFormat(ExtendArea area, String colorFormat) {
        if (StringUtils.isEmpty(colorFormat)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> format = GsonUtils.fromJson(colorFormat,
                HashMap.class);
        area.getFormatModel().setColorFormat(format);
    }

    @Override
    public void updateAreaPositionDef(ExtendArea area, String positions) {
        if (StringUtils.isEmpty(positions)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> positionMap = GsonUtils.fromJson(positions,
                HashMap.class);
        area.getFormatModel().setPositions(positionMap);
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void updateAreaTextAlignFormat(ExtendArea area,
            String textAlignFormat) {
        if (StringUtils.isEmpty(textAlignFormat)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> textAlignFormatMap = GsonUtils.fromJson(
                textAlignFormat, HashMap.class);
        area.getFormatModel().setTextAlignFormat(textAlignFormatMap);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePageSetting4PlaneTable(ExtendArea area, 
            String pageSettingStr) {
        if (StringUtils.isEmpty(pageSettingStr)) {
            return;
        }
        PaginationSetting pageSetting = GsonUtils.fromJson(pageSettingStr, PaginationSetting.class);
        PaginationSetting defaultSetting = new PlaneTableFormat().new PaginationSetting();
        if (StringUtils.isEmpty(pageSetting.getPageSizeOptions())) {
            pageSetting.setPageSizeOptions(defaultSetting.getPageSizeOptions());
        }
        area.getPlaneTableFormat().setPageSetting(pageSetting);
    }
}
