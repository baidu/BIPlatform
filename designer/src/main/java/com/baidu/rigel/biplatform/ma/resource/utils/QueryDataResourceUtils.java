package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.PivotTableParseException;
import com.baidu.rigel.biplatform.ma.report.exception.PlaneTableParseException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesDataUnit;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesInputInfo.SeriesUnitType;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.BaseTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PlaneTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowHeadField;
import com.baidu.rigel.biplatform.ma.report.service.ChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 对查询结果进行处理，用于前端展现
 * 
 * @author yichao.jiang
 *
 */
@Service("queryDataResourceUtils")
public class QueryDataResourceUtils {

    /**
     * 日志对象f
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(QueryDataResourceUtils.class);    
    /**
     * queryBuildService
     */
    @Resource
    private QueryBuildService queryBuildService;

    /**
     * chartBuildService
     */
    @Resource
    private ChartBuildService chartBuildService;
    
    /**
     * 转换查询结果
     * @param runtimeModel 报表运行态模型
     * @param targetArea 区域
     * @param result 结果
     * @param areaContext 区域上下文
     * @param action QueryAction
     * @return
     */
    public ResponseResult parseQueryResultToResponseResult(ReportRuntimeModel runtimeModel, ExtendArea targetArea,
            ResultSet result, ExtendAreaContext areaContext, QueryAction action) {
        BaseTable baseTable = null;
        ReportDesignModel designModel = runtimeModel.getModel();

        Cube cube = designModel.getSchema().getCubes().get(targetArea.getCubeId());

        if (targetArea.getType() == ExtendAreaType.PLANE_TABLE) {
            // 获取平面表
            try {
                baseTable = queryBuildService.parseToPlaneTable(cube, result.getDataModel(), targetArea.getLogicModel(),
                                targetArea.getFormatModel(), action);
                Map<String, Object> resultMap = Maps.newHashMap();
                PlaneTable planeTable = (PlaneTable) baseTable;
                Map<String, Object> otherSetting = targetArea.getOtherSetting();
                boolean isShowZero = DataModelUtils.isShowZero(otherSetting);
                DataModelUtils.decoratePlaneTable(planeTable, isShowZero);
                if (planeTable != null) {
                    resultMap.put("head", planeTable.getColDefines());
                    resultMap.put("data", planeTable.getData());
                    resultMap.put("pageInfo", planeTable.getPageInfo());
                    resultMap.put("operationColumns", planeTable.getOperationColumnDefine());
                }
                return ResourceUtils.getResult("Success", "Fail", resultMap);
            } catch (PlaneTableParseException e) {
                LOG.info(e.getMessage(), e);
                return ResourceUtils.getErrorResult("Fail in parsing result to planeTable. ",
                        1);
            }
        } else {
            // 获取pivotTable
            try {
                LogicModel logicModel = targetArea.getLogicModel ();
                if (targetArea.getType () == ExtendAreaType.LITEOLAP_TABLE) {
                    logicModel = 
                        runtimeModel.getModel ().getExtendById (targetArea.getReferenceAreaId ()).getLogicModel ();
                }
                baseTable = 
                    queryBuildService.parseToPivotTable(cube, result.getDataModel(), logicModel);
                // 对多维表格进行处理
                return this.handlePivotTable((PivotTable) baseTable, runtimeModel, 
                        targetArea, areaContext, action, cube);
            } catch (PivotTableParseException e) {
                LOG.info(e.getMessage(), e);
                return ResourceUtils.getErrorResult("Fail in parsing result to pivotTable. ",
                        1);
            }
        }
    }

    /**
     * 对表数据进行处理
     * 
     * @param table
     * @param targetArea
     * @param runtimeModel
     */
    private ResponseResult handlePivotTable(PivotTable pivotTable, ReportRuntimeModel runtimeModel, ExtendArea targetArea,
             ExtendAreaContext areaContext, QueryAction action, Cube cube) {
        
        FormatModel formatModel = targetArea.getFormatModel();
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE 
                || targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            ExtendArea refArea = runtimeModel.getModel ().getExtendById (targetArea.getReferenceAreaId ());
            formatModel = refArea.getFormatModel ();
        }
        
        Map<String, Object> resultMap = Maps.newHashMap();
        ReportDesignModel reportDesignModel = runtimeModel.getModel();
        if (targetArea.getType() == ExtendAreaType.TABLE || targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {

            DataModelUtils.decorateTable(formatModel, pivotTable, targetArea, reportDesignModel);
            /**
             * 每次查询以后，清除选中行，设置新的
             */
            runtimeModel.getSelectedRowIds().clear();
            for (RowDefine rowDefine : pivotTable.getRowDefine()) {
                if (rowDefine.isSelected()) {
                    runtimeModel.getSelectedRowIds().add(rowDefine.getUniqueName());
                }
            }
            if (pivotTable.getDataSourceColumnBased().size() == 0) {
                ResponseResult rs = new ResponseResult();
                rs.setStatus(0);
                pivotTable.setRowDefine(Lists.newArrayList());
                pivotTable.setRowHeadFields(Lists.newArrayList());
                resultMap.put("pivottable", pivotTable);
                rs.setData(resultMap);
                rs.setStatusInfo("未查到任何数据");
                return rs;
            } else {
                resultMap.put("pivottable", pivotTable);
            }
            setTableResultProperty (reportDesignModel.getId(), pivotTable, resultMap);
            List<Map<String, String>> mainDims = Lists.newArrayList();
            
            LogicModel logicModel = targetArea.getLogicModel ();
            if (targetArea.getType () == ExtendAreaType.LITEOLAP_TABLE) {
                logicModel = reportDesignModel.getExtendAreas ().get (targetArea.getReferenceAreaId ()).getLogicModel ();
            }
            if (logicModel.getRows().length >= 2) {
                if (CollectionUtils.isEmpty(areaContext.getCurBreadCrumPath())) {
                    Map<String, String> root = genRootDimCaption(pivotTable, logicModel, areaContext.getParams(), cube);
                    List<Map<String, String>> tmp = Lists.newArrayList();
                    tmp.add(root);
                    areaContext.setCurBreadCrumPath(tmp);
                    // resultMap.put("mainDimNodes", dims);
                    // 在运行时上下文保存当前区域的根节点名称 方便面包屑展示路径love
                    if (!root.get("uniqName").toLowerCase().contains("all")) {
                        root.put("uniqName", this.genRootUniqueName(root.get("uniqName")));
                        root.put("showName", "全部");
                        // runTimeModel.getContext().put(vertualDimKey, action);
                    }
                    mainDims.add(root);
                    Collections.reverse(mainDims);
                    areaContext.setCurBreadCrumPath(mainDims);
                    resultMap.put("mainDimNodes", mainDims);
                } else {
                    areaContext.setCurBreadCrumPath(areaContext.getCurBreadCrumPath());
                    resultMap.put("mainDimNodes", areaContext.getCurBreadCrumPath());
//                    resultMap.remove("mainDimNodes");
                    // resultMap.put("mainDimNodes", areaContext.getCurBreadCrumPath ());
                }
                // runTimeModel.getContext().put(areaId, root);
            } 
        } else if (targetArea.getType() == ExtendAreaType.CHART 
                || targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            DIReportChart chart = null;
            Map<String, String> chartType = getChartTypeWithExtendArea(reportDesignModel, targetArea);
            if (action.getRows().size() == 1) {
                Item item = action.getRows().keySet().toArray(new Item[0])[0];
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(reportDesignModel.getSchema(),
                        targetArea.getCubeId(), item.getOlapElementId());
                if (element instanceof TimeDimension) {
                    if (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
                        chart = chartBuildService.parseToLiteOlapChart(pivotTable, chartType, true, areaContext.getParams());
                    } else {
                        chart = chartBuildService.parseToChart(pivotTable, chartType, true);
                    }
                } else {
                    chart = chartBuildService.parseToChart(pivotTable, chartType, false);
                }
                
                if (CollectionUtils.isNotEmpty(chart.getSeriesData())
                        && MapUtils.isNotEmpty(areaContext.getParams())
                        && areaContext.getParams().get("displayName") != null) {
                    // 如果前端有设置，传入displayName，那么显示前端传入的图例信息
                    SeriesDataUnit seriesDataUnit = chart.getSeriesData().get(0);
                    seriesDataUnit.setName(areaContext.getParams().get("displayName").toString()
                            + "-" + seriesDataUnit.getName());
                }
            } else {
                chart = chartBuildService.parseToChart(pivotTable, chartType, false);
            }
            ExtendArea area = DeepcopyUtils.deepCopy (targetArea);
            area.setFormatModel (formatModel);
            QueryUtils.decorateChart(chart, area, reportDesignModel.getSchema(), -1);
            resultMap.put("reportChart", chart);
        }
        ResponseResult rs = ResourceUtils.getResult("Success", "Fail", resultMap);
        return rs;
    }
    
    /**
     * 
     * @param table
     * @param logicModel 
     * @param params 
     * @return Map<String, String>
     * 
     */
    protected Map<String, String> genRootDimCaption(PivotTable table, LogicModel logicModel, Map<String, Object> params, Cube cube) {
        Item item = logicModel.getRows ()[0];
        Map<String, String> root = Maps.newHashMap();
        if (params.containsKey (item.getOlapElementId ())) {
            Dimension dim = cube.getDimensions ().get (item.getOlapElementId ());
            Object obj = params.get(item.getOlapElementId());
            String uniqueName = "";
            if (obj instanceof String[]) {
                // 如果是多选
                String[] uniqueNames = (String[]) obj;
                for (int i = 0; i < uniqueNames.length; i++) {
                    // for callback
                    if (!MetaNameUtil.isUniqueName(uniqueNames[i])) {
                        uniqueNames[i] = "[" + dim.getName() + "].[" + uniqueNames[i] + "]";
                    }
                }
                uniqueName = StringUtils.join(uniqueNames, ",");
            } else {
                // 如果是单选
                uniqueName = (String) obj;
                // for callback
                if (!MetaNameUtil.isUniqueName(uniqueName)) {
                    uniqueName = "[" + dim.getName() + "].[" + uniqueName + "]";
                }
            }
            root.put("uniqName", genRootUniqueName (uniqueName));
        } else {
            Dimension dimension = cube.getDimensions ().get (item.getOlapElementId ());
            // TODO 修正错误的维度关联关系
            if (dimension.getType () == DimensionType.GROUP_DIMENSION) {
                dimension.getLevels ().forEach ((k, v) -> {
                    v.setDimension (dimension);
                });
            }
            String uniqueName = dimension.getAllMember ().getUniqueName ();
            root.put ("uniqName", genRootUniqueName (uniqueName));
        }
        RowHeadField rowHeadField = table.getRowHeadFields().get(0).get(0);
//        String uniqueName = rowHeadField.getUniqueName();
//        String realUniqueName = uniqueName.replace("}", "").replace("{", "");
        root.put("showName", rowHeadField.getV());
        return root;
    }
    
    protected void setTableResultProperty(String reportId, PivotTable table, Map<String, Object> resultMap) {
            resultMap.put("rowCheckMin", 1);
            resultMap.put("rowCheckMax", 5);
            resultMap.put("reportTemplateId", reportId);
            if (table.getActualSize () <= 1) {
                resultMap.put("totalSize", table.getActualSize());
            } else {
                resultMap.put("totalSize", table.getActualSize() - 1);
            }
            if (table.getDataSourceRowBased().size() <= 1) {
                resultMap.put("currentSize", table.getDataSourceRowBased().size());
            } else {
                resultMap.put("currentSize", table.getDataSourceRowBased().size() - 1);
            }
        }
     
    protected String genRootUniqueName(final String uniqueName) {
        if (uniqueName.endsWith ("@") && uniqueName.startsWith ("@")) {
            return uniqueName;
        }
        return "@" + uniqueName + "@";
    }
    
    
    /**
     * 获取扩展区域中定义的chartType
     * @param targetArea ExtendArea
     * @return SeriesUnitType
     */
    public Map<String, String> getChartTypeWithExtendArea(ReportDesignModel model, ExtendArea targetArea) {
        Map<String, String> chartTypes = Maps.newHashMap();
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            chartTypes.put("null", SeriesUnitType.LINE.name());
            return chartTypes;
//                return new String[]{SeriesUnitType.LINE.name()};
        }
//            List<String> types = Lists.newArrayList();
        targetArea.listAllItems().values().stream().filter(item -> {
            return item.getPositionType() == PositionType.Y 
                    || item.getPositionType() == PositionType.CAND_IND;
        }).forEach(item -> {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                    targetArea.getCubeId(), item.getOlapElementId());
            Object chartType = item.getParams().get("chartType");
            if (chartType == null) {
                chartTypes.put(element.getUniqueName(), SeriesUnitType.COLUMN.name());
            } else {
                chartTypes.put(element.getUniqueName(), chartType.toString());
            }
        });
//            .forEach(str -> {
//                if (StringUtils.isEmpty(str)) {
//                    types.add(SeriesUnitType.COLUMN.name());
//                } else {
//                    types.add(str.toString().toUpperCase());
//                }
//            });
        return chartTypes;
    }
}
