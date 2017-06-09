


package com.baidu.rigel.biplatform.ma.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LinkParamMappingVo;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.OlapLinkService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.OlapLinkUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.baidu.rigel.biplatform.ma.resource.view.vo.OlapLinkViewObject;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * olap多维报表设置跳转Controller
 * 
 * @author majun04
 *
 */
@RestController
@RequestMapping("/silkroad/reports")
public class OlapLinkResource {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OlapLinkResource.class);
    /**
     * jackson json ObjectMapper
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * reportModelCacheManager
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager = null;
    /**
     * olapLinkService
     */
    @Resource
    private OlapLinkService olapLinkService = null;

    /**
     * 列出可添加跳转链接的指标信息以及可被跳转的平面报表列表,目前前端只支持此接口，待前端支持通用查询接口后，此接口废弃。
     * 
     * @param reportId 报表id
     * @param areaId 区域id
     * @return 返回指标信息列表以及平面报表列表组成的json格式结果
     */
    @RequestMapping(value = "/{reportId}/extend_area/{areaId}/olaplink", method = { RequestMethod.GET })
    public ResponseResult getLinkInfo(@PathVariable("reportId") String reportId, 
            @PathVariable("areaId") String areaId) {
        List<ExtendAreaType> list = Lists.newArrayList();
        list.add(ExtendAreaType.TABLE);
        list.add(ExtendAreaType.PLANE_TABLE);
        list.add(ExtendAreaType.LITEOLAP_TABLE);
        return this.getLinkInfoByType(reportId, areaId, list);
    }

    /**
     * getLinkInfoByType
     *
     * @param reportId
     * @param areaId
     * @param type
     * @return
     */
    private ResponseResult getLinkInfoByType(String reportId, String areaId, List<ExtendAreaType> extendAreaTypeList) {
        ReportDesignModel reportDesignModel = getReportModel(reportId);
        ExtendArea tableArea = reportDesignModel.getExtendAreas().get(areaId);
        Item[] columns = tableArea.getLogicModel().getColumns();
        Map<String, LinkInfo> linkInfoMap = tableArea.getFormatModel().getLinkInfo();
        OlapLinkViewObject olapLinkViewObj = new OlapLinkViewObject();
        if (linkInfoMap == null) {
            linkInfoMap = Maps.newLinkedHashMap();
        }
        // 列出所有可跳转的表
        HashMap<ReportDesignModel, ExtendAreaType> tableMap = olapLinkService
                .getDesignModelListByTypeMap(extendAreaTypeList);
        if (!MapUtils.isEmpty(tableMap)) {
            for (Entry<ReportDesignModel, ExtendAreaType> entry : tableMap.entrySet()) {
                olapLinkViewObj.addTable(entry.getValue(), entry.getKey().getName(), entry.getKey()
                        .getId());
            }
        }
        // 列出能添加跳转参数的指标列
        if (columns != null && columns.length > 0) {
            for (Item column : columns) {
                OlapElement olapElement =
                        ReportDesignModelUtils.getDimOrIndDefineWithId(reportDesignModel.getSchema(),
                                tableArea.getCubeId(), column.getOlapElementId());
                LinkInfo linkInfo = linkInfoMap.get(olapElement.getId());
                String selectedTableId = "";
                ExtendAreaType extendAreaType = null;
                if (linkInfo != null) {
                    selectedTableId = linkInfo.getTargetTableId();
                    if (!StringUtils.isEmpty(selectedTableId)) {
                     // 获取当前表的ExtendArea的类型，目前不支持多个 table，plane_talbe,olap_table在一个报表上面
                        ReportDesignModel targetDesignModel = getReportModel(selectedTableId);
                        if (targetDesignModel != null) {
                            ExtendArea[] extendAreaList = targetDesignModel.getExtendAreaList();
                            for (ExtendArea extendArea : extendAreaList) {
                                if (extendAreaTypeList.contains(extendArea.getType())) {
                                    extendAreaType = extendArea.getType();
                                    break;
                                }
                            }
                        }
                    }
                }
                olapLinkViewObj.addColunmDefine(extendAreaType, 
                        olapElement.getCaption(), olapElement.getId(), selectedTableId);
            }
        }
        // 列出操作列配置
        List<LinkInfo> savedOperationLinkInfoList = OlapLinkUtils.getOperationColumKeys(linkInfoMap);
        if (!CollectionUtils.isEmpty(savedOperationLinkInfoList)) {
            for (LinkInfo linkInfo : savedOperationLinkInfoList) {
                // 设置回显的操作列vo对象
                if (linkInfo.getTableType() == null) {
                    linkInfo.setTableType("PLANE_TABLE");
                }
                olapLinkViewObj.addOperationColumn(ExtendAreaType.valueOf(linkInfo.getTableType()), 
                        linkInfo.getColunmSourceCaption(), linkInfo.getColunmSourceId(),
                        linkInfo.getTargetTableId());
            }
        }
        // 如果发现是第一次添加操作列，直接进行初始化
        else {
            olapLinkViewObj.addOperationColumn(null, "操作列", "operationColumn_1", "");
        }
        ResponseResult rs = ResourceUtils.getResult("success", null, olapLinkViewObj);
        return rs;
    }

    /**
     * 保存多维报表跳转明细报表的配置信息
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @return 返回操作结束后的成功与否标识
     * @throws ReportModelOperationException reportModelOperationException
     */
    @RequestMapping(value = "/{reportId}/extend_area/{areaId}/olaplink", method = { RequestMethod.POST })
    public ResponseResult saveLinkInfo(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) throws ReportModelOperationException {
        String linkInfoStr = request.getParameter("linkInfo");
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(ArrayList.class, LinkVo4Json.class);
        List<LinkVo4Json> linkInfoList = null;
        try {
            linkInfoList = OBJECT_MAPPER.readValue(linkInfoStr, javaType);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        ResponseResult rs = ResourceUtils.getErrorResult("add link failed!", 400);
        if (!CollectionUtils.isEmpty(linkInfoList)) {
            ReportDesignModel reportDesignModel = getReportModel(reportId);
            ExtendArea tableArea = reportDesignModel.getExtendAreas().get(areaId);
            Map<String, LinkInfo> linkInfoMap = tableArea.getFormatModel().getLinkInfo();
            // 如果发现是第一次新设置参数映射关系，则需要new出一个linkInfoMap
            if (linkInfoMap == null) {
                linkInfoMap = Maps.newLinkedHashMap();
            }
            Map<String, LinkInfo> finalResultMap = Maps.newLinkedHashMap();
            for (LinkVo4Json linkInfoVo : linkInfoList) {
                String colunmId = linkInfoVo.getId();
                LinkInfo linkInfo = linkInfoMap.get(colunmId);
                if (linkInfo == null) {
                    linkInfo = new LinkInfo();
                }
                linkInfo.setTargetTableId(linkInfoVo.getSelectedTable());
                linkInfo.setTableType(linkInfoVo.getSelectedType());
                linkInfo.setColunmSourceId(colunmId);
                linkInfo.setColunmSourceCaption(linkInfoVo.getText());
                finalResultMap.put(colunmId, linkInfo);
            }
            tableArea.getFormatModel().setLinkInfo(finalResultMap);
            reportModelCacheManager.updateReportModelToCache(reportId, reportDesignModel);
            // 做完新增后，需要同步通知RuntimeModel的修改，以便在编辑端看到的model状态是同步的
            ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
            runTimeModel.init(reportDesignModel, false);
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
            rs = ResourceUtils.getResult("success", null, "add link success!");
        }
        return rs;
    }

    /**
     * 删除制定列（包括操作列）的跳转配置
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @return 返回操作结束后的成功与否标识
     * @throws ReportModelOperationException reportModelOperationException
     */
    @RequestMapping(value = "/{reportId}/extend_area/{areaId}/olaplink/{linkId}", method = { RequestMethod.DELETE })
    public ResponseResult deleteLinkInfo(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, @PathVariable("linkId") String linkId, HttpServletRequest request)
            throws ReportModelOperationException {
        // String linkId = request.getParameter("linkId");
        ResponseResult rs = ResourceUtils.getErrorResult("delete linkInfo failed!", 400);
        if (!StringUtils.isEmpty(linkId)) {
            ReportDesignModel reportDesignModel = getReportModel(reportId);
            ExtendArea tableArea = reportDesignModel.getExtendAreas().get(areaId);
            Map<String, LinkInfo> linkInfoMap = tableArea.getFormatModel().getLinkInfo();
            linkInfoMap.remove(linkId);
            tableArea.getFormatModel().setLinkInfo(linkInfoMap);
            reportModelCacheManager.updateReportModelToCache(reportId, reportDesignModel);
            // 做完删除后，需要同步通知RuntimeModel的修改，以便在编辑端看到的model状态是同步的
            ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
            runTimeModel.init(reportDesignModel, false);
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
            rs = ResourceUtils.getResult("success", null, "add link success!");
        }
        return rs;
    }

    /**
     * 得到多维报表跳转明细报表的维度与参数映射关系
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @return 返回操作结束后的成功与否标识
     */
    @RequestMapping(value = "/{reportId}/extend_area/{areaId}/olaplink/paramMapping", method = { RequestMethod.GET })
    public ResponseResult getLinkInfoParamMapping(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) throws ReportModelOperationException {
        ReportDesignModel fromTableDesignModel = getReportModel(reportId);
        ExtendArea fromTableArea = fromTableDesignModel.getExtendAreas().get(areaId);
        String targetTableId = request.getParameter("tableId");

        // 这里的olapElementId其实就是要添加链接的指标id
        String olapElementId = request.getParameter("olapElementId");
        ReportDesignModel targetTableDesignModel = getReportModel(targetTableId);
        Map<String, LinkInfo> linkInfoMap = fromTableArea.getFormatModel().getLinkInfo();
        LinkInfo linkInfo = linkInfoMap.get(olapElementId);

        // 每次保存都是新new一个LinkParamMappingVo，全量保存以替换掉之前存储的旧数据
        LinkParamMappingVo paramMappingVo = new LinkParamMappingVo();
        List<Dimension> targetTableParamList = olapLinkService
                .getTargetTableDimList(targetTableDesignModel, linkInfo.getTableType());
        for (Dimension targetDim : targetTableParamList) {
            String targetParamName = targetDim.getName();
            // 如果有设置值，则设置selectedDim值。
            String savedDimName = linkInfo.getParamMapping().get(targetParamName);
            String selectedDim = "";
            if (!StringUtils.isEmpty(savedDimName)) {
                selectedDim = savedDimName;
            }
            paramMappingVo.addTargetTableParam(targetDim.getName(), targetDim.getCaption(), selectedDim);
        }
        List<Dimension> dimList = olapLinkService.getOlapDims(fromTableDesignModel, fromTableArea);
        for (Dimension dim : dimList) {
            paramMappingVo.addOlapTableDim(dim.getCaption(), dim.getName());
        }
        ResponseResult rs = ResourceUtils.getResult("success", null, paramMappingVo);
        return rs;
    }

    /**
     * 保存多维报表跳转明细报表的维度与参数映射关系
     * 
     * @param reportId 报表id
     * @param areaId 报表区域id
     * @return 返回操作结束后的成功与否标识
     */
    @RequestMapping(value = "/{reportId}/extend_area/{areaId}/olaplink/paramMapping", method = { RequestMethod.POST })
    public ResponseResult saveLinkInfoParamMapping(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) throws ReportModelOperationException {
        String mappingInfo = request.getParameter("mappingInfo");
        JavaType javaType =
                OBJECT_MAPPER.getTypeFactory().constructParametricType(ArrayList.class, SavedMappingVo4Json.class);
        List<SavedMappingVo4Json> mappingVoList = null;
        try {
            mappingVoList = OBJECT_MAPPER.readValue(mappingInfo, javaType);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        ResponseResult rs = ResourceUtils.getErrorResult("save link's param mapping failed!", 400);

        if (!CollectionUtils.isEmpty(mappingVoList)) {
            ReportDesignModel olapTableDesignModel = getReportModel(reportId);
            ExtendArea tableArea = olapTableDesignModel.getExtendAreas().get(areaId);
            String olapElementId = request.getParameter("olapElementId");
            Map<String, LinkInfo> linkInfoMap = tableArea.getFormatModel().getLinkInfo();
            LinkInfo linkInfo = linkInfoMap.get(olapElementId);
            Map<String, String> paramMapping = new HashMap<String, String>();
            for (SavedMappingVo4Json vo : mappingVoList) {
                String dimName = vo.getSelectedDim();
                if (StringUtils.isEmpty(dimName)) {
                // 过滤没有设置的dim
                    continue;    
                }
                Cube cube = olapTableDesignModel.getSchema().getCubes().get(tableArea.getCubeId());
                cube = QueryUtils.transformCube(cube);
                Dimension dim = cube.getDimensions().get(dimName);
                if (tableArea.getType() == ExtendAreaType.PLANE_TABLE
                        && dim == null
                        && cube.getMeasures().get(dimName) != null) {
                    StandardDimension dimTmp = new StandardDimension();
                    dimTmp.setName(cube.getMeasures().get(dimName).getName());
                    dim = dimTmp;
                }
                if (dim != null) {
                    paramMapping.put(vo.getParamName(), dim.getName());
                }
                
            }
            linkInfo.setParamMapping(paramMapping);
            tableArea.getFormatModel().setLinkInfo(linkInfoMap);
            reportModelCacheManager.updateReportModelToCache(reportId, olapTableDesignModel);
            ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
            runtimeModel.init(olapTableDesignModel, false, true);
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runtimeModel);
            rs = ResourceUtils.getResult("success", null, "save mapping success!");
        }
        return rs;
    }

    /**
     * 根据reportId得到ReportModel
     * 
     * @param reportId 报表id
     * @return 返回reportId对应的ReportDesignModel对象实例
     */
    private ReportDesignModel getReportModel(String reportId) {
        ReportDesignModel reportModel = null;
        try {
            reportModel = reportModelCacheManager.getReportModel(reportId);
            return reportModel;
        } catch (CacheOperationException e) {
            LOGGER.warn("There is no such report model in cache. ", e);
            LOGGER.info("Add report model into cache. ");
        }
        return reportModelCacheManager.loadReportModelToCache(reportId);
    }

    /**
     * 多维报表跳转信息vo类
     * 
     * @author majun04
     *
     */
    private static final class LinkVo4Json {
        /**
         * 指标id
         */
        private String id;
        /**
         * 指标名称
         */
        private String text;
        /**
         * 选中的明细报表id
         */
        private String selectedTable;
        
        /**
         * 选中的明细报表类型
         */
        private String selectedType;

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * @param text the text to set
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * @return the selectedTable
         */
        public String getSelectedTable() {
            return selectedTable;
        }

        /**
         * @param selectedTable the selectedTable to set
         */
        public void setSelectedTable(String selectedTable) {
            this.selectedTable = selectedTable;
        }

        /**
         * default generate get selectedType
         * @return the selectedType
         */
        public String getSelectedType() {
            return selectedType;
        }

        /**
         * default generate set selectedType
         * @param selectedType the selectedType to set
         */
        public void setSelectedType(String selectedType) {
            this.selectedType = selectedType;
        }
    }

    /**
     * 前端传入的由json转换而来的多维跳转明细报表参数映射vo对象
     * 
     * @author majun04
     *
     */
    private static final class SavedMappingVo4Json {
        /**
         * 平面报表参数名称
         */
        private String paramName = null;
        
        /**
         * 平面报表参数显示名称
         */
        private String paramCaption = null;
        
        /**
         * 选中的多维报表维度名称
         */
        private String selectedDim = null;

        /**
         * @return the paramName
         */
        public String getParamName() {
            return paramName;
        }

        /**
         * @param paramName the paramName to set
         */
        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        /**
         * @return the selectedDim
         */
        public String getSelectedDim() {
            return selectedDim;
        }

        /**
         * @param selectedDim the selectedDim to set
         */
        public void setSelectedDim(String selectedDim) {
            this.selectedDim = selectedDim;
        }

        /**
         * default generate get paramCaption
         * @return the paramCaption
         */
        public String getParamCaption() {
            return paramCaption;
        }

        /**
         * default generate set paramCaption
         * @param paramCaption the paramCaption to set
         */
        public void setParamCaption(String paramCaption) {
            this.paramCaption = paramCaption;
        }

    }

}
