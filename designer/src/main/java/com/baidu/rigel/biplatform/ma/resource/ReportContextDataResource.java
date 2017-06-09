package com.baidu.rigel.biplatform.ma.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.CallbackMember;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.callback.CallbackConstants;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.ma.comm.util.ParamValidateUtils;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceConnectionException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkParams;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.CellData;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowHeadField;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryDataUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.PlaneTableUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.ResourceUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 此接口提供与报表查询上下文交互相关的数据查询服务
 * 
 * @author majun04
 *
 */
@RestController
@RequestMapping("/silkroad/reports")
public class ReportContextDataResource extends BaseResource {
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(ReportContextDataResource.class);
    /**
     * reportModelCacheManager
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager;

    /**
     * 报表数据查询服务
     */
    @Resource
    private ReportModelQueryService reportModelQueryService;

    /**
     * reportDesignModelService
     */
    @Resource(name = "reportDesignModelService")
    private ReportDesignModelService reportDesignModelService;

    @Resource(name = "fileService")
    private FileService fileService;

    /**
     * 初始化查询参数,初始化查询区域参数
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{reportId}/init_params", method = { RequestMethod.POST })
    public ResponseResult initParams(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO]--- ---begin init params with report id {}", reportId);
        String areaIdList = request.getParameter("paramList");
        String[] areaIds = null;
        final ReportDesignModel model = getDesignModelFromRuntimeModel(reportId);
        if (!StringUtils.isEmpty(areaIdList)) {
            areaIds = areaIdList.split(",");
        }
        if (areaIds == null || areaIds.length == 0) {
            ResponseResult rs = new ResponseResult();
            rs.setStatus(0);
            logger.info("[INFO]--- --- not needed init global params");
            return rs;
        }
        final ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        Map<String, Object> datas = Maps.newConcurrentMap();
        Map<String, String> params = Maps.newHashMap();
        runtimeModel.getContext().getParams().forEach((k, v) -> {
            params.put(k, v == null ? "" : v.toString());
        });
        params.put(HttpRequest.COOKIE_PARAM_NAME, request.getHeader("cookie"));
        for (final String areaId : areaIds) {
            ExtendArea area = model.getExtendById(areaId);
            Cube cube = null;
            if (area != null) {
                // 获取对应的cube
                cube = model.getSchema().getCubes().get(area.getCubeId());
            }
            // TODO 查询条件回填？
            if (area != null && isQueryComp(area.getType()) && !area.listAllItems().isEmpty()) {
                Item item = area.listAllItems().values().toArray(new Item[0])[0];
                Cube tmpCube = QueryUtils.transformCube(cube);
                String dimId = item.getOlapElementId();
                Dimension dim = cube.getDimensions().get(dimId);
                if (dim != null) {
                    List<Map<String, String>> values;
                    final List<String> defaultValues = Lists.newArrayList();
                    try {
                        values = Lists.newArrayList();
                        //Jin. 当外部传参时，控件的默认值按传参设定                        
                        //Jin. 取得http请求传参的值
                        final List<String> defaultValueList=new ArrayList<String>();
                        if(params.containsKey(dim.getId())){
                            defaultValueList.addAll(Arrays.asList(params.get(dim.getId()).split(",")));
                        }
                        
                        params.remove(dim.getId());
                        params.put(Constants.LEVEL_KEY, "1");
                        params.put(CallbackConstants.CB_NEED_SUMMARY, CallbackConstants.CB_NEED_SUMMARY_FALSE);
                        List<Member> members =
                                reportModelQueryService.getMembers(tmpCube, tmpCube.getDimensions().get(dim.getName()),
                                        params, securityKey).get(0);
                        members.forEach(m -> {
                            Map<String, String> tmp = Maps.newHashMap();
                            tmp.put("value", m.getUniqueName());
                            tmp.put("text", m.getCaption());
                            //Jin. 如果当前的member的caption存在于http传参中，取得对应的uniqueName放到defaultValues中
                            if(!CollectionUtils.isEmpty(defaultValueList) && defaultValueList.remove(m.getCaption())){
                                defaultValues.add(m.getUniqueName());
                            }                            
                            
                            if (dim.getLevels().size() <= 1) {
                                tmp.put("isLeaf", "1");
                            }
                            MiniCubeMember realMember = (MiniCubeMember) m;
                            if (realMember.getParent() != null) {
                                tmp.put("parent", realMember.getParent().getUniqueName());
                            } else {
                                tmp.put("parent", "");
                            }
                            values.add(tmp);
                            List<Map<String, String>> children = getChildren(realMember, realMember.getChildren());
                            if (children != null && !children.isEmpty()) {
                                values.addAll(children);
                            }
                        });
                        // List<Map<String, String>> values =
                        // QueryUtils.getMembersWithChildrenValue(members,
                        // tmpCube, dsInfo, Maps.newHashMap());
                        Map<String, Object> datasource = Maps.newHashMap();
                        datasource.put("datasource", values);
                        //Jin. 把默认选中的值填到返回结果里
                        datasource.put("defaultValue", defaultValues);
                        QueryDataUtils.fillBackParamValues(runtimeModel, dim, datasource);
                        datas.put(areaId, datasource);
                    } catch (Exception e) {
                        logger.info(e.getMessage(), e);
                    }
                }
            }
        }
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setData(datas);
        rs.setStatusInfo("OK");
        logger.info("[INFO]--- --- successfully init params, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    /**
     * 查询条件数据获取服务
     */
    @RequestMapping(value = "/{reportId}/members/{areaId}", method = { RequestMethod.POST })
    public ResponseResult getMemberWithParent(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO]--- ---begin init params with report id {}", reportId);
        String currentUniqueName = request.getParameter("uniqueName");
        // int level =
        // MetaNameUtil.parseUnique2NameArray(currentUniqueName).length - 1;
        final ReportDesignModel model = getDesignModelFromRuntimeModel(reportId);
        final ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        Map<String, Object> datas = Maps.newConcurrentMap();
        Map<String, String> params = Maps.newHashMap();
        runtimeModel.getContext().getParams().forEach((k, v) -> {
            params.put(k, v == null ? "" : v.toString());
        });
        ExtendArea area = model.getExtendById(areaId);
        if (area != null && isQueryComp(area.getType()) && !area.listAllItems().isEmpty()) {
            Item item = area.listAllItems().values().toArray(new Item[0])[0];
            Cube cube = model.getSchema().getCubes().get(area.getCubeId());
            Cube tmpCube = QueryUtils.transformCube(cube);
            String dimId = item.getOlapElementId();
            Dimension dim = cube.getDimensions().get(dimId);
            if (QueryDataUtils.isCallbackDim(dim)) {
                String paramsName = QueryDataUtils.getParamName(dim, model);
                String paramsValue = QueryDataUtils.getCallbackParamValue(paramsName, currentUniqueName);
                params.put(paramsName, paramsValue);
            }

            if (dim != null) {
                List<Map<String, String>> values;
                try {
                    values = Lists.newArrayList();
                    params.remove(dim.getId());
                    List<Member> members =
                            reportModelQueryService.getMembers(tmpCube, currentUniqueName, params, securityKey);
                    members.forEach(m -> {
                        Map<String, String> tmp = Maps.newHashMap();
                        int curLevel = MetaNameUtil.parseUnique2NameArray(m.getUniqueName()).length - 1;
                        tmp.put("value", m.getUniqueName());
                        tmp.put("text", m.getCaption());
                        if (QueryDataUtils.isCallbackDim(dim) && m instanceof CallbackMember) {
                            CallbackMember cm = (CallbackMember) m;
                            tmp.put("isLeaf", Boolean.toString(!cm.isHasChildren()));
                        } else {
                            tmp.put("isLeaf", Boolean.toString(curLevel == dim.getLevels().size()));
                        }

                        values.add(tmp);
                    });
                    Map<String, Object> datasource = Maps.newHashMap();
                    datasource.put("datasource", values);
                    if (area.getType() == ExtendAreaType.CASCADE_SELECT) {
                        QueryDataUtils.fillBackParamValues(runtimeModel, dim, datasource);
                    }
                    datas.put(areaId, datasource);
                } catch (Exception e) {
                    logger.info(e.getMessage(), e);
                } // end catch
            } // end if dim != null
        } // end if area != null
        ResponseResult rs = new ResponseResult();
        rs.setStatus(0);
        rs.setData(datas);
        rs.setStatusInfo("OK");
        logger.info("[INFO]--- --- successfully query member, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    private List<Map<String, String>> getChildren(Member parent, List<Member> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        List<Map<String, String>> rs = Lists.newArrayList();
        MiniCubeMember tmp = null;
        for (Member m : children) {
            tmp = (MiniCubeMember) m;
            Map<String, String> map = Maps.newHashMap();
            map.put("value", tmp.getUniqueName());
            map.put("text", tmp.getCaption());
            map.put("parent", parent.getUniqueName());
            rs.add(map);
            if (!CollectionUtils.isEmpty(tmp.getChildren())) {
                rs.addAll(getChildren(tmp, tmp.getChildren()));
            }
        }
        return rs;
    }

    /**
     * @param reportId
     * @return ReportDesignModel
     */
    ReportDesignModel getDesignModelFromRuntimeModel(String reportId) {
        return reportModelCacheManager.getRuntimeModel(reportId).getModel();
    }

    /**
     * 
     * @param type 区域类型
     * @return boolean
     */
    private boolean isQueryComp(ExtendAreaType type) {
        return QueryUtils.isFilterArea(type);
    }

    /**
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{reportId}/report_id", method = { RequestMethod.GET })
    public ResponseResult getReport(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO] --- --- begin query report model");
        ReportDesignModel model = null;
        try {
            model = this.getDesignModelFromRuntimeModel(reportId); // reportModelCacheManager.getReportModel(reportId);
        } catch (CacheOperationException e1) {
            logger.info("[INFO]--- --- can't not get report form cache", e1.getMessage());
            return ResourceUtils.getErrorResult(e1.getMessage(), ResponseResult.FAILED);
        }
        // reportModelCacheManager.loadReportModelToCache(reportId);
        ResponseResult rs = ResourceUtils.getCorrectResult("OK", model);
        logger.info("[INFO] --- --- query report model successuffly, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    /**
     * 
     * @param reportId
     * @param request
     * @param response
     * @return String
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{reportId}/report_vm", method = { RequestMethod.GET, RequestMethod.POST },
            produces = "text/html;charset=utf-8")
    public String queryVM(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) {
        long begin = System.currentTimeMillis();
        ReportDesignModel model = null;
        String reportPreview = request.getParameter("reportPreview");
        String imageId = request.getParameter("reportImageId");
        
        //Jin.reload参数,对于请求中增加reload参数的请求，直接从cache中取前一次的runtimemodel进行查询
        //TODO 后续需要提供reload接口，而不是这种参数形式
        String reload = request.getParameter("reload");
        
        ReportRuntimeModel runtimeModel = null;
        String activedsName = null;
        ReportRuntimeModel planeTableRuntimeModel = null;
        // 先将动态数据源参数保存下来，以在后续初始化完毕之后再将数据源名称设置回context中
        try {
            planeTableRuntimeModel = reportModelCacheManager.getRuntimeModel(reportId);
            if (planeTableRuntimeModel != null) {
                activedsName = (String) planeTableRuntimeModel.getContext().getParams().get("activeds");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (StringUtils.isEmpty(imageId) || reportId.equals(imageId)) {
                if (!StringUtils.isEmpty(reportPreview) && Boolean.valueOf(reportPreview)) {
                    model = reportModelCacheManager.getReportModel(reportId);
                    if (model != null) {
                        model = DeepcopyUtils.deepCopy(model);
                    }
                } else {// if ((runtimeModel =
                        // reportModelCacheManager.getRuntimeModelUnsafety(reportId))
                        // == null) {
                    if(!StringUtils.isEmpty(reload)){
                        runtimeModel = reportModelCacheManager.getRuntimeModelUnsafety(reportId);                        
                    }
                    
                    if(StringUtils.isEmpty(reload) || runtimeModel == null ){
                        model = reportDesignModelService.getModelByIdOrName(reportId, true);
                    }
                
                    
                }
            }
        } catch (CacheOperationException e1) {
            logger.info("[INFO]--- ---Fail in loading release report model into cache. ", e1);
            // throw new IllegalStateException();
        }

        if (model != null) {
            runtimeModel = new ReportRuntimeModel(reportId);
            runtimeModel.init(model, true);
            // 这里需要把动态数据源的参数加入初始化完毕的全局上下文中 以免初始化的时候参数丢失 update by majun
            if (!StringUtils.isEmpty(activedsName)) {
                runtimeModel.getContext().getParams().put("activeds", activedsName);
            }
        } else if (runtimeModel == null) {
            try {
                String path = getSavedReportPath(request);
                String fileName = path + File.separator + reportId + File.separator + imageId;
                runtimeModel = (ReportRuntimeModel) SerializationUtils.deserialize(fileService.read(fileName));
                model = runtimeModel.getModel();
            } catch (FileServiceException e) {
                logger.info("[INFO]--- ---加载保存的报表失败 ", e);
            }
        } else {
            model = runtimeModel.getModel();
        }
        if (runtimeModel == null) {
            logger.info("[INFO]--- ---init runtime model failed ");
            throw new RuntimeException("初始化报表模型失败");
        }
        // modify by jiangyichao at 2014-10-10
        // 将url参数添加到全局上下文中
        Enumeration<String> params = request.getParameterNames();
        // 请求参数
        Map<String, String> requestParams = Maps.newHashMap();
        while (params.hasMoreElements()) {
            String paramName = params.nextElement();
            if (request.getParameter(paramName) != null) {
                runtimeModel.getContext().put(paramName, request.getParameter(paramName));
                requestParams.put(paramName, request.getParameter(paramName));
            }
        }
        // 添加cookie内容
        runtimeModel.getContext().put(HttpRequest.COOKIE_PARAM_NAME, request.getHeader("Cookie"));

        // 获取多维数据表的报表Id
        String fromReportId = request.getParameter("fromReportId");
        // 平面表id
        String toReportId = request.getParameter("toReportId");
        // 如果是由多维跳转到明细
        if (!StringUtils.isEmpty(fromReportId) && !StringUtils.isEmpty(toReportId)) {
            // 从cache中取得多维表的运行态模型
            ReportRuntimeModel fromRuntimeModel = reportModelCacheManager.getRuntimeModel(fromReportId);
            // 如果从cache中取不到多维表的运行态模型，则抛出异常
            if (fromRuntimeModel == null) {
                logger.info("[INFO]--- ---无法获取多维表运行态模型, id :", fromReportId);
                throw new IllegalStateException("[INFO]--- ---无法获取多维表运行态模型, id :" + fromReportId);
            }
            
            // copy from report参数
            if (fromRuntimeModel != null && fromRuntimeModel.getContext() != null
                    && fromRuntimeModel.getContext().getParams() != null) {
                Map<String, Object> map = Maps.newHashMap();
                map.putAll(fromRuntimeModel.getContext().getParams());
                map.putAll(runtimeModel.getContext().getParams());
                runtimeModel.getContext().getParams().putAll(map);
            }
            
            // 处理p参数
            for (Entry<String, ReportParam> entry : model.getParams().entrySet()) {
                if (runtimeModel.getContext().getParams().get(entry.getKey()) != null) {
                    String value = runtimeModel.getContext().getParams().get(entry.getKey()).toString();
                    runtimeModel.getContext().getParams().put(entry.getValue().getElementId(), value);
                }
            }

            // 多维表cube
            Cube multiCube = null;
            // 多维表查询参数
            Map<String, Object> queryParams = Maps.newHashMap();
            ExtendArea[] multiExtendAreas = fromRuntimeModel.getModel().getExtendAreaList();
            // 获取多维表对应的因为此处仅考虑一个cube
            // fix by yichao.jiang
            // 并非每个控件都存在cube，cube可能会取不到，必须保证该extendArea中有cubeId，还不能是查询过滤控件
            // 是否为lite-olap报表
            boolean isLiteOlap = false;
            // lite-olap选择区域的id
            String liteOlapSelectAreaId = "";
            for (ExtendArea extendArea : multiExtendAreas) {
                if (extendArea != null && !StringUtils.isEmpty(extendArea.getCubeId())
                        && !QueryUtils.isFilterArea(extendArea.getType())) {
                    multiCube = fromRuntimeModel.getModel().getSchema().getCubes().get(extendArea.getCubeId());
                }
                // 分别对应普通多维表、lite-olap报表的候选区域以及下拉选择控件
                if (extendArea.getType() == ExtendAreaType.TABLE || extendArea.getType() == ExtendAreaType.SELECTION_AREA
                        || QueryUtils.isFilterArea(extendArea.getType()))
                    // 如果为lite-olap报表
                    if (extendArea.getType() == ExtendAreaType.SELECTION_AREA) {
                        isLiteOlap = true;
                        liteOlapSelectAreaId = extendArea.getId();
                    }
                    queryParams.putAll(fromRuntimeModel.getLocalContextByAreaId(extendArea.getId()).getParams());
            }
            // TODO 此处参数处理上，存在全局和局部的矛盾问题，后续应考虑将全局和局部参数问题处理
            // 问题描述：对于下拉框或者级联下拉框，参数作为全局条件，此时应该以全局条件为主，否则下拉框切换条件无效
            // 对于lite-olap报表，条件存在选择区域select_area上，为局部条件，此时应该以局部条件为主，否则lite-olap报表上的
            // 放大镜功能在切换后不生效
            if (!isLiteOlap) {
                queryParams.putAll(fromRuntimeModel.getContext().getParams());
            } else {
                queryParams.putAll(fromRuntimeModel.getLocalContextByAreaId(liteOlapSelectAreaId).getParams());
            }
            // targetReport Cube
            Cube targetCube = null;
            ExtendArea[] planeExtendAreas = model.getExtendAreaList();
            for (ExtendArea extendArea : planeExtendAreas) {
                targetCube = model.getSchema().getCubes().get(extendArea.getCubeId());
            }

            Map<String, Object> fromParams = fromRuntimeModel.getContext().getParams();
            // 如果包含跳转参数
            if (fromParams != null && fromParams.containsKey("linkBridgeParams")) {
                Map<String, LinkParams> linkParams = (Map<String, LinkParams>) fromParams.get("linkBridgeParams");
                Map<String, String> targetTableCond = Maps.newHashMap();
                
                
                // 添加指标
                for (Entry<String, Measure> entry : targetCube.getMeasures().entrySet()) {
                    // 添加callback p参数对应的情况
                    for (ReportParam reportParam : model.getParams().values()) {
                        Dimension dim = targetCube.getDimensions().get(reportParam.getElementId());
                        if (dim != null && dim.getType() == DimensionType.CALLBACK) {
                            targetTableCond.put(reportParam.getName(), entry.getValue().getName());
                            targetTableCond.put(entry.getValue().getName(), reportParam.getName());
                        }
                    }
                    targetTableCond.put(entry.getKey(), entry.getValue().getName());
                    targetTableCond.put(entry.getValue().getName(), entry.getKey());
                }
                
                // 添加维度
                for (Entry<String, Dimension> entry : targetCube.getDimensions().entrySet()) {
                    // 添加callback p参数对应的情况
                    for (ReportParam reportParam : model.getParams().values()) {
                        Dimension dim = targetCube.getDimensions().get(reportParam.getElementId());
                        if (dim != null && dim.getType() == DimensionType.CALLBACK) {
                            targetTableCond.put(reportParam.getName(), entry.getValue().getName());
                            targetTableCond.put(entry.getValue().getName(), reportParam.getName());
                        }
                    }
                    targetTableCond.put(entry.getKey(), entry.getValue().getName());
                    targetTableCond.put(entry.getValue().getName(), entry.getKey());
                }
                
                // 添加平面表条件
                if (!MapUtils.isEmpty(runtimeModel.getModel().getPlaneTableConditions())) {
                    for (PlaneTableCondition planeTableCondition : runtimeModel.getModel()
                            .getPlaneTableConditions().values()) {
                        if (linkParams.get(planeTableCondition.getName()) != null) {
                            String dimName = linkParams.get(planeTableCondition.getName()).getDimName();
                            targetTableCond.put(dimName, planeTableCondition.getElementId());
                            targetTableCond.put(planeTableCondition.getElementId(), dimName);
                        }
                        
                        targetTableCond.put(planeTableCondition.getName(), planeTableCondition.getElementId());
                        targetTableCond.put(planeTableCondition.getElementId(), planeTableCondition.getName());
                    }
                }
                for (String key : linkParams.keySet()) {
                    LinkParams linkParam = linkParams.get(key);
                    if (StringUtils.isEmpty(linkParam.getOriginalDimValue())
                            || StringUtils.isEmpty(linkParam.getUniqueName())) {
                        continue;
                    }
                    String newValue = null;
                    String planeTableConditionKey = null;
                    try {
                        // TODO 兼容老的跳转配置
                        planeTableConditionKey = targetTableCond.get(linkParam.getParamName());
                        if (planeTableConditionKey == null) {
                            planeTableConditionKey = targetTableCond.get(linkParam.getDimName());
                        }
                        if (linkParam.getOriginalDimValue() != null
                                && PlaneTableUtils.isTimeDim(targetCube, planeTableConditionKey)) {
                            TimeDimension timeDim = (TimeDimension) targetCube.getDimensions().get(
                                    planeTableConditionKey);
                            // 如果是普通时间JSON字符串
                            if (PlaneTableUtils.isTimeJson(linkParam.getOriginalDimValue())) {
                                newValue = linkParam.getOriginalDimValue();
                            } else {
                                // 如果不是规范的时间JSON字符串，则需特殊处理
                                newValue = PlaneTableUtils.convert2TimeJson(
                                        linkParam.getOriginalDimValue(), fromParams, timeDim);
                            }
                        } else {
                            // 从form表cube中获取该维度
                            Dimension[] dim =
                                    multiCube.getDimensions().values().stream()
                                            .filter(v -> v.getName().equals(linkParam.getParamName())).toArray(Dimension[]::new);
                            // TODO 兼容老版本跳转配置
                            if (ArrayUtils.isEmpty(dim)) {
                                dim = multiCube.getDimensions().values().stream()
                                        .filter(v -> v.getName().equals(linkParam.getDimName()))
                                        .toArray(Dimension[]::new);
                            }
                            
                            String name = null;
                            String id = null;
                            DimensionType type = null;
                            boolean isMeasureParam = false;
                            // 添加跳转为指标的情况,平面表跳多维的情况
                            if (ArrayUtils.isEmpty(dim)) {
                                Measure[] measures = multiCube.getMeasures().values().stream()
                                        .filter(v -> v.getName().equals(linkParam.getDimName()))
                                        .toArray(Measure[]::new);
                                if (!ArrayUtils.isEmpty(measures)) {
                                    isMeasureParam = true;
                                    name = linkParam.getDimName();
                                }
                                id =  measures[0].getId();
                                // 处理查询的uniquename
                                String[] uniqueNames = MetaNameUtil.parseUnique2NameArray(linkParam.getUniqueName());
                                uniqueNames[0] = name;
                                linkParam.setUniqueName(MetaNameUtil.makeUniqueNamesArray(uniqueNames));
                            } else {
                                name = dim[0].getName();
                                id = dim[0].getId();
                                type = dim[0].getType();
                                // 判断维度组兼容老配置
                                Dimension[] dimGroup = multiCube.getDimensions().values().stream()
                                        .filter(v -> v.getName().equals(linkParam.getDimName()))
                                        .toArray(Dimension[]::new);
                                if (!ArrayUtils.isEmpty(dimGroup) 
                                        && dimGroup[0].getType() != DimensionType.GROUP_DIMENSION) {
                                    String[] uniqueNames = MetaNameUtil
                                            .parseUnique2NameArray(linkParam.getUniqueName());
                                    uniqueNames[0] = name;
                                    linkParam.setUniqueName(MetaNameUtil.makeUniqueNamesArray(uniqueNames));
                                }
                            }
                            
                            if (name == null) {
                                throw new Exception("获取维度或指标数据出错");
                            }
                            

                            // 利用维度信息从上下文中获取是否有参数
                            Object filterValue = null;
                            Set<String> filterValueSet = Sets.newHashSet();
                            if (queryParams.containsKey(id)) {
                                filterValue = queryParams.get(id);
                                filterValueSet = this.covObject2Set(filterValue);
                            } else if (type == DimensionType.GROUP_DIMENSION) {
                                Set<String> totalFilterValueSet = Sets.newHashSet();
                                dim[0].getLevels().forEach((k, v) -> {
                                    if (queryParams.containsKey(k)) {
                                        totalFilterValueSet.addAll(this.covObject2Set(queryParams.get(k)));
                                    }
                                });
                                filterValueSet = totalFilterValueSet;
                            }
                            if (MetaNameUtil.isUniqueName(linkParam.getUniqueName())) {
                                requestParams.put(HttpRequest.COOKIE_PARAM_NAME, request.getHeader("Cookie"));
                                newValue =
                                        this.handleReqParams4Table(multiCube, targetTableCond,
                                                linkParam.getUniqueName(), requestParams,
                                                filterValueSet, securityKey, isMeasureParam);
                            } else {
                                newValue = linkParam.getOriginalDimValue();
                            }
                        }
                        if (StringUtils.isEmpty(newValue)) {
                            throw new Exception("处理后的参数值为空");
                        }
                        logger.debug("the linkParam {" + linkParam.getParamName() + "}, and it's origin value is ["
                                + linkParam.getOriginalDimValue() + "], and it's new value are [" + newValue + "]");
                    } catch (Exception e) {
                        logger.error("处理平面表参数出错，请检查!", e);
                        throw new RuntimeException("处理平面表参数出错，请检查!");
                    }
                    if (newValue != null) {
                        runtimeModel.getContext().getParams().put(key, newValue);
                    }
                    if (planeTableConditionKey != null && newValue != null) {
                        runtimeModel.getContext().getParams().put(planeTableConditionKey, newValue);
                    }
                }
            }
        } else {
            /**
             * 依据查询请求，根据报表参数定义，增量添加报表区域模型参数
             */
            Map<String, Object> tmp = QueryUtils.resetContextParam(request, model);
            runtimeModel.getContext().getParams().putAll(tmp);
        }

        if (StringUtils.isEmpty(imageId) || reportId.equals(imageId)) {
            reportModelCacheManager.updateRunTimeModelToCache(reportId, runtimeModel);
        } else {
            reportModelCacheManager.updateRunTimeModelToCache(imageId, runtimeModel);
        }
        StringBuilder builder = buildVMString(reportId, request, response, model);
        logger.info("[INFO] query vm operation successfully, cost {} ms", (System.currentTimeMillis() - begin));
        // 如果请求中包含UID 信息，则将uid信息写入cookie中，方便后边查询请求应用
        String uid = request.getParameter(UID_KEY);
        if (uid != null) {
            Cookie cookie = new Cookie(UID_KEY, uid);
            cookie.setPath(Constants.COOKIE_PATH);
            response.addCookie(cookie);
        }
        if (request.getParameter("newPlatform") != null) {
            return "<!DOCTYPE html><html>" + "<head><meta charset=\"utf-8\"><title>报表平台-展示端</title>"
                    + "<meta name=\"description\" content=\"报表平台展示端\">"
                    + "<meta name=\"viewport\" content=\"width=device-width\">" + "</head>" + "<body>"
                    + "<script type=\"text/javascript\">" + "var seed = document.createElement('script');"
                    + "seed.src = '/silkroad/new-biplatform/asset/seed.js?action=display&t=' + (+new Date());"
                    + "document.getElementsByTagName('head')[0].appendChild(seed);" + "</script>" + "</body>"
                    + "</html>";
        }
        return "<!DOCTYPE html><html>" + "<head><meta charset=\"utf-8\"><title>报表平台-展示端</title>"
        + "<meta name=\"description\" content=\"报表平台展示端\">"
        + "<meta name=\"viewport\" content=\"width=device-width\">" + "</head>" + "<body>"
        + "<script type=\"text/javascript\">" + "var seed = document.createElement('script');"
        + "seed.src = '/silkroad/new-biplatform/asset/seed.js?action=display&t=' + (+new Date());"
        + "document.getElementsByTagName('head')[0].appendChild(seed);" + "</script>" + "</body>"
        + "</html>";
    }

    /**
     * 将Object类型的value转为Set类型
     * 
     * @param paramValue
     * @return
     */
    private Set<String> covObject2Set(Object paramValue) {
        Set<String> sets = Sets.newHashSet();
        if (paramValue == null) {
            return sets;
        }
        // 此处处理时，需要对条件做一步处理
        // 如[纬度组].[All_维度组]的情况，此时是没有过滤条件的
        // 而对于[维度组].[交通运输].[All_交通运输]的情况，相当于过滤条件仅在第一级上，需要处理
        if (paramValue instanceof String[]) {
            String[] paramValueArray = (String[]) paramValue;
            Stream.of(paramValueArray).forEach(value -> {
                sets.add(value);
            });
        }
        if (paramValue instanceof String) {
            if (MetaNameUtil.isUniqueName((String) paramValue)) {
                String[] uniqueNameValue = MetaNameUtil.parseUnique2NameArray((String) paramValue);
                List<String> newUniqueNameValueList = Lists.newArrayList();
                for (int i = uniqueNameValue.length - 1; i >= 0; i--) {
                    if (uniqueNameValue[i].contains("All_")) {
                        continue;
                    }
                    newUniqueNameValueList.add(uniqueNameValue[i]);
                }
                Collections.reverse(newUniqueNameValueList);
                String[] newUniqueNameValue = newUniqueNameValueList.toArray(new String[0]);
                if (newUniqueNameValue.length > 1) {
                    String value = Stream.of(newUniqueNameValue).map((val -> {
                        return MetaNameUtil.makeUniqueName(val);
                    })).collect(Collectors.joining("."));
                    sets.add(value);
                }
            }
        }
        return sets;
    }

    /**
     * 处理平面表跳转时的参数问题 handleReqParams4PlaneTable
     * 
     * @param cube
     * @param uniqueName
     * @param params
     * @return
     */
    private String handleReqParams4Table(Cube cube, Map<String, String> planeTableCond, String uniqueName,
            Map<String, String> params, Set<String> filterValues, String securityKey, boolean isMeasureParam)
            throws DataSourceOperationException {

        if (!ParamValidateUtils.check("cube", cube)) {
            return null;
        }
        if (!ParamValidateUtils.check("planeTableCond", planeTableCond)) {
            return null;
        }
        if (!ParamValidateUtils.check("uniqueName", uniqueName)) {
            return null;
        }
        String dimName = MetaNameUtil.getDimNameFromUniqueName(uniqueName);
        Cube oriCube = QueryUtils.transformCube(cube);
        
        DataSourceDefine dsDefine = null;
        DataSourceInfo dsInfo = null;
        DataSourceService dataSourceService =
                (DataSourceService) ApplicationContextHelper.getContext().getBean("dsService");
        try {
            dsDefine = dataSourceService.getDsDefine(cube.getSchema().getDatasource());
            DataSourceConnectionService<?> dsConnService =
                    DataSourceConnectionServiceFactory.getDataSourceConnectionServiceInstance(dsDefine
                            .getDataSourceType().name());
            dsInfo = dsConnService.parseToDataSourceInfo(dsDefine, securityKey);
        } catch (DataSourceOperationException | DataSourceConnectionException e) {
            logger.error("Fail in parse datasource to datasourceInfo.", e);
            throw new DataSourceOperationException(e);
        }
        
        Level tmpLevel = null;
        String[] tmp = MetaNameUtil.parseUnique2NameArray(uniqueName);
        if (isMeasureParam) {
            return tmp[tmp.length - 1];
        } else {
            Dimension dim = oriCube.getDimensions().get(dimName);
            if (dim != null && dim.getLevels() != null) {
                tmpLevel = dim.getLevels().values().toArray(new Level[0])[0];
            }
            
            if (QueryDataUtils.isCallbackLevel(tmpLevel)) {
                // 处理callback
                CallbackLevel callbackLevel = (CallbackLevel) tmpLevel;
                Map<String, String> callbackParams = callbackLevel.getCallbackParams();
                String callbackParam = null;
                // TODO 是否考虑多个参数问题
                for (String key : callbackParams.keySet()) {
                    if (planeTableCond.containsKey(key)) {
                        callbackParam = key;
                        break;
                    }
                }
                callbackLevel.getCallbackParams().put(callbackParam, tmp[tmp.length - 1]);
                List<Member> members = callbackLevel.getMembers(oriCube, dsInfo, params);
                for (Member member : members) {
                    if (member.getUniqueName().equals(uniqueName)) {
                        if (!CollectionUtils.isEmpty(filterValues) && !filterValues.contains(uniqueName)) {
                            continue;
                        }
                        MiniCubeMember miniCubeMember = (MiniCubeMember) member;
                        // 传入的uniqueName下的所有的节点信息
                        Set<String> queryNodes = miniCubeMember.getQueryNodes();
                        // 获取过滤条件中的所有节点信息，因为过滤条件中存储的为[post_id].[34378]，需要将其转为34378
                        Set<String> newFilterValues = filterValues.stream().map(value -> {
                            String[] tmpValue = MetaNameUtil.parseUnique2NameArray(value);
                            return tmpValue[tmpValue.length - 1];
                        }).collect(Collectors.toSet());
                        final String finalCallbackParam = callbackParam;
                        final DataSourceInfo finalDsInfo = dsInfo;
                        Set<String> newQueryNodes = Sets.newHashSet();
                        // 对每一个过滤条件遍历，如果该过滤条件属于该uniqueName下的节点
                        // 则应该以该过滤条件信息为主
                        newFilterValues.forEach(value -> {
                            // 如果该值在queryNodes中出现过
                                if (queryNodes.contains(value)) {
                                    // 更改callbackLevel
                                    callbackLevel.getCallbackParams().put(finalCallbackParam, value);
                                    // 获取新的member信息
                                    List<Member> tmpMembers = callbackLevel.getMembers(oriCube, finalDsInfo, params);
                                    Set<String> tmpQueryNodes = Sets.newHashSet();
                                    // 获取新的member信息下的queryNodes信息
                                    tmpMembers.stream().forEach(tmpVal -> {
                                        MiniCubeMember tmpMiniCubeMember = (MiniCubeMember) tmpVal;
                                        tmpQueryNodes.addAll(tmpMiniCubeMember.getQueryNodes());
                                    });
                                    // 最终新的queryNodes信息
                                    newQueryNodes.addAll(tmpQueryNodes);
                                }
                            });
                        if (!CollectionUtils.isEmpty(newQueryNodes)) {
                            return newQueryNodes.stream().collect(Collectors.joining(","));
                        }
                        return queryNodes.stream().collect(Collectors.joining(","));
                    }
                }
            } else {
                // 如果有孩子结点，则要取到孩子结点数值
                if ((dim.getLevels().size() > tmp.length - 1)) {
                    Level level = dim.getLevels().values().toArray(new Level[0])[tmp.length - 2];
                    if (MetaNameUtil.isAllMemberUniqueName(uniqueName)) {
                        level = dim.getLevels().values().toArray(new Level[0])[dim.getLevels().size() - 1];
                    }
                    // 产生新的过滤条件，过滤条件应该作用在对应的level上
                    filterValues = this.generateNewFilterValues(dim, filterValues, oriCube, dsInfo, params);
                    return getChildMembersStrByParentAndUniqueName(level, oriCube, dsInfo, params, filterValues, uniqueName);
                }
                // 如果当前维度是个维度组，并且传入参数为形如[行业维度].[交通运输].[All_交通运输s]，
                // 那么其实需要取一级行业对应的全部二级行业节点，主要用于级联下拉框控件 update by majun
                else if (dim.getType() == DimensionType.GROUP_DIMENSION && (dim.getLevels().size() == tmp.length - 1)
                        && MetaNameUtil.isAllMemberName(tmp[tmp.length - 1])) {
                    // 这里需要注意，传入的level应该是指定层级的上一级
                    Level level = dim.getLevels().values().toArray(new Level[0])[dim.getLevels().size() - 2];
                    if (MetaNameUtil.isAllMemberName(tmp[tmp.length - 1])) {
                        uniqueName = uniqueName.substring(0, uniqueName.lastIndexOf("."));
                    }
                    return getChildMembersStrByParentAndUniqueName(level, oriCube, dsInfo, params, filterValues, uniqueName);

                } else {
                    // 如果没有孩子，则直接返回
                    return tmp[tmp.length - 1];
                }
            }
        }
        return null;
    }

    /**
     * 根据需要，产生新的过滤条件
     * 
     * @param dim
     * @param filterValues
     * @param oriCube
     * @param dsInfo
     * @param params
     * @return
     */
    private Set<String> generateNewFilterValues(Dimension dim, Set<String> filterValues, Cube oriCube,
            DataSourceInfo dsInfo, Map<String, String> params) {
        // 如果没有条件，则返回空
        if (CollectionUtils.isEmpty(filterValues)) {
            return Sets.newHashSet();
        }
        // 获取过滤条件对应的level
        Level filterLevel = this.getLevelFromFilterValue(filterValues, dim);
        // 总的level层级数目
        int totalLevelIndex = filterLevel.getDimension().getLevels().size();
        int currentLevelIndex = this.getLevelIndex(filterValues);
        // 新的过滤条件
        Set<String> newFilterValues = Sets.newHashSet();
        if (currentLevelIndex < totalLevelIndex) {
            filterValues.forEach(value -> {
                // 获取该uniqueName下的所有孩子节点
                    List<Member> childMembersTmp =
                            reportModelQueryService.getMembers(oriCube, value, params, securityKey);
                    Set<Member> tmpTotalMembers = Sets.newHashSet();
                    childMembersTmp.stream().forEach(member -> {
                        MiniCubeMember realMember = (MiniCubeMember) member;
                        String[] realMemberUniqueName = MetaNameUtil.parseUnique2NameArray(realMember.getUniqueName());
                        // 未到最底层级
                            if (realMemberUniqueName.length - 1 < totalLevelIndex) {
                                List<Member> children =
                                        getChildrenMember(realMember, oriCube, dsInfo, params, totalLevelIndex);
                                if (!CollectionUtils.isEmpty(children)) {
                                    tmpTotalMembers.addAll(children);
                                }
                            } else if (realMemberUniqueName.length - 1 == totalLevelIndex) {
                                // 已经到了最底层级
                                tmpTotalMembers.add(realMember);
                            }
                        });
                    Set<String> tmpFilterValues =
                            tmpTotalMembers.stream().map(tmpFilter -> tmpFilter.getUniqueName())
                                    .collect(Collectors.toSet());
                    newFilterValues.addAll(tmpFilterValues);
                });
            if (!CollectionUtils.isEmpty(newFilterValues)) {
                return newFilterValues;
            }
        }
        // int levelIndex = this.getLevelIndex(filterValues);
        // // 如果条件所在的level已经在最底层(LevelIndex == dim.getLevels().size())，则无需进一步获取
        // // 如果该level不属于最底层级，则需要将过滤条件转为最底层级
        // if (filterLevel != null && levelIndex < dim.getLevels().size()) {
        // // 获取过滤条件下的member
        // List<Member> tmpFilterMembers = filterLevel.getMembers(oriCube, dsInfo, params);
        // List<Member> filterMembers = Lists.newArrayList();
        // final DataSourceInfo finalDsInfo = dsInfo;
        // // 过滤条件，并重新调整
        // tmpFilterMembers
        // .stream()
        // // 仅取在过滤条件中出现的值
        // .filter(val -> this.isContainsInFilterValues(val.getUniqueName(), filterValues))
        // .forEach(
        // val -> {
        // filterMembers.addAll(this.getMembersByLevel(val, this.getLevelIndex(filterValues), dim
        // .getLevels().size(), oriCube, finalDsInfo, params));
        // });
        // // 清除过滤条件
        // filterValues.clear();
        // // 添加新的过滤条件
        // filterMembers.stream().forEach(value -> {
        // filterValues.add(value.getUniqueName());
        // });
        // return filterValues;
        // }
        return Sets.newHashSet(filterValues);
    }

    /**
     * 获取过滤条件对应的level层级位置 [一级行业名称].[交通运输]，则返回level为1 如果没有过滤条件，则返回-1
     * 
     * @param filterValues
     * @return
     */
    private int getLevelIndex(Set<String> filterValues) {
        // 如果已经有需要过滤的条件存在
        if (!CollectionUtils.isEmpty(filterValues)) {
            String tmpValue = filterValues.toArray(new String[0])[0];
            return this.getLevelIndex(tmpValue);
        }
        return -1;
    }

    /**
     * 获取该uniqueName下的层级
     * 
     * @param uniqueName
     * @return
     */
    private int getLevelIndex(String uniqueName) {
        if (MetaNameUtil.isUniqueName(uniqueName)) {
            String[] tmpValueArray = MetaNameUtil.parseUnique2NameArray(uniqueName);
            return tmpValueArray.length - 1;
        }
        return -1;
    }

    /**
     * 根据上下文中的查询条件，获取该查询条件对应的维度层级 如果没有过滤条件，则返回null 应该返回一级行业所在的level
     * 
     * @param filterValues
     * @param dim
     * @return
     */
    private Level getLevelFromFilterValue(Set<String> filterValues, Dimension dim) {
        // 如果已经有需要过滤的条件存在
        if (!CollectionUtils.isEmpty(filterValues)) {
            String tmpValue = filterValues.toArray(new String[0])[0];
            String[] tmpValueArray = MetaNameUtil.parseUnique2NameArray(tmpValue);
            // 取该条件对应的层级
            // 如果过滤条件为[一级行业].[交通运输]，转为数组，长度为2
            // 而维度中有两个level，一级行业和二级行业，转为数组，一级行业下标为0
            // 也就是tmpValueArray.length - 2
            return dim.getLevels().values().toArray(new Level[0])[tmpValueArray.length - 2];
        }
        return null;
    }

    // /**
    // * 获取某个member下的某一层的所有元素
    // *
    // * @param member
    // * @param levelIndex
    // * @param targetLevelIndex
    // * @param cube
    // * @param dataSourceInfo
    // * @param params
    // * @return
    // */
    // private List<Member> getMembersByLevel(Member member, int levelIndex, int targetLevelIndex, Cube cube,
    // DataSourceInfo dataSourceInfo, Map<String, String> params) {
    // if (levelIndex == targetLevelIndex) {
    // return member.getChildMembers(cube, dataSourceInfo, params);
    // }
    // // 获取孩子member
    // List<Member> result = Lists.newArrayList();
    // List<Member> childMember =
    // this.getMembersByLevel(member, levelIndex + 1, targetLevelIndex, cube, dataSourceInfo, params);
    // result.addAll(childMember);
    // return result;
    // }

    /**
     * 根据给定的level和uniqueName，查找指定条件下对应的child成员，并以以“,”连接返回
     * 
     * @param level level
     * @param oriCube oriCube
     * @param dsInfo dsInfo
     * @param params params
     * @param uniqueName uniqueName
     * @return 子member拼成的字符串，以“,”连接
     */
    private String getChildMembersStrByParentAndUniqueName(Level level, Cube oriCube, DataSourceInfo dsInfo,
            Map<String, String> params, Set<String> filterValues, String uniqueName) {
        // 获取当前level下的所有成员
        List<Member> members = level.getMembers(oriCube, dsInfo, params);
        // 如果uniqueName是all节点，直接返回最底层节点的孩子成员即可 update by majun04
        if (!CollectionUtils.isEmpty(members) && MetaNameUtil.isAllMemberUniqueName(uniqueName)) {
            // 此时传过来的过滤值已经是作用到最底层的条件值了
            if (!CollectionUtils.isEmpty(filterValues)) {
                // 拿过滤条件过滤值
                return members.stream()
                        .filter(value -> this.isContainsInFilterValues(value.getUniqueName(), filterValues))
                        .map(child -> child.getName()).collect(Collectors.joining(","));
            } else {
                return members.stream().map(child -> child.getName()).collect(Collectors.joining(","));
            }
        }

        // 获取该uniqueName下的所有孩子节点
        List<Member> childMembersTmp = reportModelQueryService.getMembers(oriCube, uniqueName, params, securityKey);
        List<Member> totalMembers = Lists.newArrayList();
        // 总的level层级数目
        int totalLevelIndex = level.getDimension().getLevels().size();
        childMembersTmp.stream().forEach(member -> {

            MiniCubeMember realMember = (MiniCubeMember) member;
            String[] realMemberUniqueName = MetaNameUtil.parseUnique2NameArray(realMember.getUniqueName());
            // 未到最底层级
                if (realMemberUniqueName.length - 1 < totalLevelIndex) {
                    List<Member> children = getChildrenMember(realMember, oriCube, dsInfo, params, totalLevelIndex);
                    if (!CollectionUtils.isEmpty(children)) {
                        totalMembers.addAll(children);
                    }
                } else if (realMemberUniqueName.length - 1 == totalLevelIndex) {
                    // 已经到了最底层级
                    totalMembers.add(realMember);
                }
            });
        if (!CollectionUtils.isEmpty(totalMembers)) {
            if (CollectionUtils.isEmpty(filterValues)) {
                return totalMembers.stream().map(child -> child.getName()).collect(Collectors.joining(","));
            }
            return totalMembers.stream()
                    .filter(value -> this.isContainsInFilterValues(value.getUniqueName(), filterValues))
                    .map(child -> child.getName()).collect(Collectors.joining(","));

        }
        // // 获取当前的level层级
        // int currentLevelIndex = this.getLevelIndex(uniqueName);
        // for (Member member : members) {
        // if (this.isPartial(member.getUniqueName(), uniqueName)) {
        // List<Member> childMember =
        // this.getMembersByLevel(member, currentLevelIndex, totalLevelIndex, oriCube, dsInfo, params);
        // childMember.stream().forEach(val -> System.out.println(val.getUniqueName()));
        // String[] uniqueNameArray = MetaNameUtil.parseUnique2NameArray(uniqueName);
        // String[] filterUniqueNameArray = null;
        // if (!CollectionUtils.isEmpty(filterValues)) {
        // filterUniqueNameArray = this.getUniqueNameArrayFromFilterSet(filterValues);
        // if (!filterValues.contains(member.getUniqueName()) && filterUniqueNameArray != null
        // && filterUniqueNameArray.length == uniqueNameArray.length)
        // continue;
        // }
        // List<Member> childMembers = member.getChildMembers(oriCube, dsInfo, params);
        // if (filterUniqueNameArray != null && filterUniqueNameArray.length > uniqueNameArray.length) {
        // return childMembers.stream().filter(v -> filterValues.contains(v.getUniqueName()))
        // .map(child -> child.getName()).collect(Collectors.joining(","));
        // } else {
        // return childMembers.stream().map(child -> child.getName()).collect(Collectors.joining(","));
        // }
        // }
        // }
        return null;
    }

    /**
     * 递归获取孩子节点
     * 
     * @param parent
     * @param children
     * @return
     */
    private List<Member> getChildrenMember(MiniCubeMember parent, Cube oriCube, DataSourceInfo dsInfo,
            Map<String, String> params, int totalLevelIndex) {
        List<Member> rs = Lists.newArrayList();
        MiniCubeMember tmp = null;
        List<Member> childMembersTmp =
                reportModelQueryService.getMembers(oriCube, parent.getUniqueName(), params, securityKey);
        if (CollectionUtils.isEmpty(childMembersTmp)) {
            return null;
        }
        Member member = childMembersTmp.get(0);
        String[] uniqueNameArray = MetaNameUtil.parseUnique2NameArray(member.getUniqueName());
        if (uniqueNameArray.length - 1 == totalLevelIndex) {
            rs.addAll(childMembersTmp);
        }
        for (Member m : childMembersTmp) {
            tmp = (MiniCubeMember) m;
            String[] tmpUniqueName = MetaNameUtil.parseUnique2NameArray(tmp.getUniqueName());
            if (tmpUniqueName.length - 1 < totalLevelIndex) {
                rs.addAll(getChildrenMember(tmp, oriCube, dsInfo, params, totalLevelIndex));
            }
        }
        return rs;
    }

    /**
     * 判断某个值是否在过滤条件中出现过
     * 
     * @param uniqueName
     * @param filterValues
     * @return
     */
    private boolean isContainsInFilterValues(String uniqueName, Set<String> filterValues) {
        String[] tmpValueArray = MetaNameUtil.parseUnique2NameArray(uniqueName);
        long count = filterValues.stream().filter(tmpVal -> {
            String[] tmpValArray = MetaNameUtil.parseUnique2NameArray(tmpVal);
            Set<String> tmpValSets = Sets.newHashSet(tmpValArray);
            boolean isContain = true;
            for (String tmpValue : tmpValueArray) {
                isContain = isContain && tmpValSets.contains(tmpValue);
            }
            return isContain;
        }).count();
        if (count >= 1) {
            return true;
        }
        return false;
    }

//    /**
//     * 判断某个局部uniqueName是否在全量uniqueName当中 TODO 此问题是由于queryRouter返回的member的uniqueName是[维度组].[中医]类似的
//     * 缺少父级别信息的uniqueName，所以暂时提供该方法
//     * 
//     * @param partialUniqueName
//     * @param fullUniqueName
//     * @return
//     */
//    private boolean isPartial(String partialUniqueName, String fullUniqueName) {
//        String[] fullUnqiueNameArray = MetaNameUtil.parseUnique2NameArray(fullUniqueName);
//        Set<String> set = Sets.newHashSet(fullUnqiueNameArray);
//        String[] partialUniqueNameArray = MetaNameUtil.parseUnique2NameArray(partialUniqueName);
//        boolean result = true;
//        for (String tmp : partialUniqueNameArray) {
//            result = result && set.contains(tmp);
//        }
//        return result;
//    }
//
//    /**
//     * 根据查询条件，获取uniqueName
//     * 
//     * @param filterValues
//     * @return
//     */
//    private String[] getUniqueNameArrayFromFilterSet(Set<String> filterValues) {
//        String value = filterValues.toArray(new String[0])[0];
//        if (MetaNameUtil.isUniqueName(value)) {
//            return MetaNameUtil.parseUnique2NameArray(value);
//        }
//        return null;
//    }

    /**
     * @param reportId
     * @param response
     * @param model
     * @return StringBuilder
     */
    private StringBuilder buildVMString(String reportId, HttpServletRequest request, HttpServletResponse response,
            ReportDesignModel model) {
        // TODO 临时方案，以后前端做
        String vm = model.getVmContent();
        String imageId = request.getParameter("reportImageId");
        String js =
                "<script type='text/javascript'>" + "\r\n" + "        (function(NS) {" + "\r\n"
                        + "            NS.xui.XView.start(" + "\r\n"
                        + "                'di.product.display.ui.LayoutPage'," + "\r\n" + "                {" + "\r\n"
                        + "                    externalParam: {" + "\r\n" + "                    'reportId':'"
                        + (StringUtils.isEmpty(imageId) ? reportId : imageId)
                        + "','phase':'dev'},"
                        + "\r\n"
                        + "                    globalType: 'PRODUCT',"
                        + "\r\n"
                        + "                    diAgent: '',"
                        + "\r\n"
                        + "                    reportId: '"
                        + (StringUtils.isEmpty(imageId) ? reportId : imageId)
                        + "',"
                        + "\r\n"
                        + "                    webRoot: '/silkroad',"
                        + "\r\n"
                        + "                    phase: 'dev',"
                        + "\r\n"
                        + "                    serverTime: ' "
                        + System.currentTimeMillis()
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

        builder.append("<script src='/silkroad/asset/" + theme + "/-di-product-min.js'>");
        builder.append("</script>");
        builder.append(js);
        builder.append("</body>");
        builder.append("</html>");
        response.setCharacterEncoding("utf-8");
        return builder;
    }

    @RequestMapping(value = "/{reportId}/report_json", method = { RequestMethod.GET, RequestMethod.POST },
            produces = "text/plain;charset=utf-8")
    public String queryJson(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) {
        long begin = System.currentTimeMillis();
        ReportDesignModel model = null;
        String json = null;
        try {
            model = this.getDesignModelFromRuntimeModel(reportId);
            if (!CollectionUtils.isEmpty(model.getRegularTasks())) {
                json = this.setReportJson(model.getJsonContent(), "REGULAR");
            } else {
                json = this.setReportJson(model.getJsonContent(), "RTPL_VIRTUAL");
            }
        } catch (Exception e) {
            logger.info("[INFO]--- ---There are no such model in cache. Report Id: " + reportId, e);
            throw new IllegalStateException();
        }
        logger.info(json);
        response.setCharacterEncoding("utf-8");
        logger.info("[INFO] query json operation successfully, cost {} ms", (System.currentTimeMillis() - begin));
        return json;
    }

    /**
     * 设置报表的JSON
     * 
     * @param json
     * @param reportType
     * @return
     */
    private String setReportJson(String json, String reportType) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (jsonObj.has("entityDefs")) {
                JSONArray jsonArrays = jsonObj.getJSONArray("entityDefs");
                for (int i = 0; i < jsonArrays.length(); i++) {
                    JSONObject value = jsonArrays.getJSONObject(i);
                    if (value.has("clzKey") && value.get("clzKey") != null
                            && value.get("clzKey").toString().equals("DI_FORM")) {
                        value.put("reportType", reportType);
                        break;
                    }
                }
            }
            return jsonObj.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return json;
    }

    /**
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{reportId}/runtime_model", method = { RequestMethod.POST })
    public ResponseResult initRunTimeModel(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO]--- ---begin init runtime env");
        boolean edit = Boolean.valueOf(request.getParameter(Constants.IN_EDITOR));
        ReportDesignModel model = null;
        if (edit) {
            /**
             * 编辑报表
             */
            model = reportModelCacheManager.loadReportModelToCache(reportId);
            model.setPersStatus(false);
        } else {
            /**
             * 如果是新建的报表，从缓存中找
             */
            try {
                model = reportModelCacheManager.getReportModel(reportId);
                model.setPersStatus(false);
            } catch (CacheOperationException e) {
                logger.info("[INFO]There are no such model in cache. Report Id: " + reportId, e);
                return ResourceUtils.getErrorResult("缓存中不存在的报表！id: " + reportId, 1);
            }
        }
        ReportRuntimeModel runtimeModel = new ReportRuntimeModel(reportId);
        runtimeModel.init(model, true);
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (value != null) {
                /**
                 * value 不能是null，但可以为空字符串，空字符串可能有含义
                 */
                runtimeModel.getContext().put(key, value);
            }

        }
        reportModelCacheManager.updateRunTimeModelToCache(reportId, runtimeModel);
        // reportModelCacheManager.updateReportModelToCache(reportId, model);
        ResponseResult rs = ResourceUtils.getCorrectResult("OK", "");
        logger.info("[INFO] successfully init runtime evn, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }

    /**
     * 
     * @param reportId
     * @param runTimeModel
     * @return ReportDesignModel
     */
    private ReportDesignModel getRealModel(String reportId, ReportRuntimeModel runTimeModel) {
        ReportDesignModel model = runTimeModel.getModel();
        return model;
    }

    /**
     * 更新上下文 将格式态的报表模型转化成运形态的报表模型存入缓存 或者依据用户查询逻辑更新运形态报表模型
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{reportId}/runtime/context", method = { RequestMethod.POST })
    public ResponseResult updateContext(@PathVariable("reportId") String reportId, HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        logger.info("[INFO]------begin update global runtime context");
        Map<String, String[]> contextParams = request.getParameterMap();
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        ReportDesignModel model = runTimeModel.getModel();
        model.getExtendAreas().forEach((k, v) -> {
            reportModelCacheManager.updateAreaContext(reportId, k, new ExtendAreaContext());
        });
        Map<String, String> params = Maps.newHashMap();
        if (model.getParams() != null) {
            model.getParams().forEach((k, v) -> {
                params.put(v.getElementId(), v.getName());
            });
        }
        // add by jiangyichao， 取出DesignModel中的平面表条件
        Map<String, String> condition = Maps.newHashMap();
        if (model.getPlaneTableConditions() != null) {
            model.getPlaneTableConditions().forEach((k, v) -> {
                condition.put(v.getElementId(), v.getName());
            });
        }
        // prepare4Test(model, runTimeModel, contextParams, params);
        runTimeModel =
                QueryDataUtils.modifyRuntimeModel4RuntimeContext(model, runTimeModel, contextParams, params, condition);

        reportModelCacheManager.updateRunTimeModelToCache(reportId, runTimeModel);
        logger.info("[INFO]current context params status {}", runTimeModel.getContext().getParams());
        logger.info("[INFO]successfully update global context, cost {} ms", (System.currentTimeMillis() - begin));
        // return initParams (reportId, request);
        ResponseResult rs = ResourceUtils.getResult("Success Getting VM of Report", "Fail Getting VM of Report", "");
        return rs;
    }

    /**
     * 当在设计端编辑报表的时候，每次设置操作完毕，需要通知后台刷新model里面的无用条件
     * 
     * @param reportId reportId
     * @param areaId areaId
     * @param request request
     * @return responseResult
     */
    @RequestMapping(value = "/{reportId}/runtime/extend_area/{areaId}/refresh4design", method = { RequestMethod.POST })
    public ResponseResult refresh4design(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request) {
        ReportRuntimeModel runTimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        ReportDesignModel designModel = getRealModel(reportId, runTimeModel);
        ExtendArea targetArea = designModel.getExtendById(areaId);
        // 如果是liteolap报表，需要取其中的table区域对象
        if (targetArea.getType() == ExtendAreaType.LITEOLAP) {
            LiteOlapExtendArea liteOlapExtendArea = (LiteOlapExtendArea) targetArea;
            targetArea = designModel.getExtendById(liteOlapExtendArea.getTableAreaId());
        }
        ExtendAreaContext areaContext = reportModelCacheManager.getAreaContext(reportId, targetArea.getId());
        // 处理上一次查询的遗留脏数据，后续可能还有更多清理动作
        if (areaContext.getCurBreadCrumPath() != null) {
            areaContext.getCurBreadCrumPath().clear();
        }
        reportModelCacheManager.updateAreaContext(reportId, targetArea.getId(), areaContext);
        ResponseResult result = new ResponseResult();
        result.setStatus(0);
        return result;
    }

    /**
     * 数据查询API，获取基于报表模型的数据
     * 
     * @param reportId
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/{reportId}/data", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseResult queryData(@PathVariable("reportId") String reportId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        long begin = System.currentTimeMillis();
        queryVM(reportId, request, response);
        ResponseResult rs = updateContext(reportId, request);
        if (rs.getStatus() != 0) {
            return rs;
        }
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);
        ReportDesignModel model = this.getRealModel(reportId, runtimeModel);
        rs = new ResponseResult();
        if (model == null) {
            rs.setStatus(1);
            rs.setStatusInfo("未找到相应数据模型");
            logger.info("cannot get report define in queryData");
        }
        if (model.getExtendAreas().size() != 1) {
            rs.setStatus(1);
            rs.setStatusInfo("数据区域个数大于2, 不能确定数据区域");
            logger.info("more than one data areas, return");
        } else {
            ExtendArea area = model.getExtendAreaList()[0];
            QueryDataResource queryDataResource =
                    (QueryDataResource) ApplicationContextHelper.getContext().getBean("queryDataResource");
            // 通过action调用数据查询action的入口方法
            rs = queryDataResource.queryArea(reportId, area.getId(), request);
            if (rs.getStatus() != 0) {
                logger.info("unknown error!");
                return rs;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) rs.getData();
            Map<String, List<String>> datas = Maps.newHashMap();
            if (data.containsKey("pivottable")) {
                PivotTable pivotTable = (PivotTable) data.get("pivottable");
                pivotTable.getDataSourceColumnBased();
                List<List<RowHeadField>> rowHeadFields = pivotTable.getRowHeadFields();
                List<List<RowHeadField>> colFieldBaseRow = Lists.newArrayList();
                // 行列互转
                for (int i = 0; i < rowHeadFields.size(); ++i) {
                    for (int j = 0; j < rowHeadFields.get(i).size(); ++j) {
                        if (colFieldBaseRow.size() <= j) {
                            List<RowHeadField> tmp = Lists.newArrayList();
                            tmp.add(rowHeadFields.get(i).get(j));
                            colFieldBaseRow.add(tmp);
                        } else {
                            colFieldBaseRow.get(j).add(rowHeadFields.get(i).get(j));
                        }
                    }
                }
                colFieldBaseRow.forEach(list -> {
                    String key = list.get(0).getUniqueName();
                    key = MetaNameUtil.getDimNameFromUniqueName(key);
                    List<String> value = list.stream().map(f -> f.getV()).collect(Collectors.toList());
                    datas.put(key, value);
                });
                for (int i = 0; i < pivotTable.getColDefine().size(); ++i) {
                    String key = pivotTable.getColDefine().get(i).getUniqueName();
                    if (MetaNameUtil.isUniqueName(key)) {
                        key = MetaNameUtil.getNameFromMetaName(key);
                    }
                    List<CellData> v = pivotTable.getDataSourceColumnBased().get(i);
                    List<String> tmpV =
                            v.stream().map(cellData -> (cellData.getV() == null ? "-" : cellData.getV().toString()))
                                .collect(Collectors.toList());
                    datas.put(key, tmpV);
                }
            } else {
                // DoNothing 暂时不支持平面表
            }
            rs.setData(datas);
        }
        logger.info("successfully get data from report model, cost {} ms", (System.currentTimeMillis() - begin));
        return rs;
    }
}
