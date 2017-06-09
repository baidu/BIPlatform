package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LinkParams;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.service.OlapLinkService;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * olapLinkService实现
 * 
 * @author majun04
 *
 */
@Service("olapLinkService")
public class OlapLinkServiceImpl implements OlapLinkService {
    /**
     * singleDImValue
     */
    private static final String SINGLE_DIM_VALUE = "singleDimValue";
    /**
     * uniqueName
     */
    private static final String UNIQUE_NAME = "uniqueName";
    /**
     * reportDesignModelService
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.report.service.OlapLinkService#getDesignModelListContainsPlaneTable()
     */
    public HashMap<ReportDesignModel, ExtendAreaType> getDesignModelListByTypeMap(List<ExtendAreaType> typeList) {
        ReportDesignModel[] allList = reportDesignModelService.queryAllModels(true);
        HashMap<ReportDesignModel, ExtendAreaType> map = Maps.newHashMap();
        if (allList != null && allList.length > 0) {
            for (ReportDesignModel reportDesignModel : allList) {
                ExtendArea[] extendAreaList = reportDesignModel.getExtendAreaList();
                for (ExtendArea extendArea : extendAreaList) {
                    if (typeList.contains(extendArea.getType())) {
                        map.put(reportDesignModel, extendArea.getType());
                        break;
                    }
                }
            }
        }
        return map;
    }


    /**
     * getTargetTableDimList
     *
     * @param tableDesignModel
     * @return
     */
    public List<Dimension> getTargetTableDimList(ReportDesignModel tableDesignModel, String tableType) {
        // 获取当前表的ExtendArea的类型，目前不支持多个 table，plane_talbe,olap_table在一个报表上面
        List<Dimension> result = new ArrayList<Dimension>();
        if (tableDesignModel != null) {
            ExtendArea[] extendAreaList = tableDesignModel.getExtendAreaList();
            for (ExtendArea extendArea : extendAreaList) {
                if (tableType.equals(extendArea.getType().toString())) {
                    return this.getOlapDims(tableDesignModel, extendArea);
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.OlapLinkService#getOlapDims(com.baidu.rigel.biplatform.ma.report
     * .model.ReportDesignModel, com.baidu.rigel.biplatform.ma.report.model.ExtendArea)
     */
    public List<Dimension> getOlapDims(ReportDesignModel olapTableDesignModel, ExtendArea olapTableArea) {
        List<Dimension> dimList = new ArrayList<Dimension>();
        Set<String> countedSet = new HashSet<String>();
        if (olapTableArea.getType() == ExtendAreaType.TABLE
                || olapTableArea.getType() == ExtendAreaType.LITEOLAP){
     
            List<Dimension> conditionDims = this.getOlapTableConditionDims(olapTableDesignModel, countedSet);
            List<Dimension> rowDims = this.getOlapTableRowDims(olapTableDesignModel, olapTableArea, countedSet);
            List<Dimension> filterDims = this.getOlapTableFilterDims(olapTableDesignModel, olapTableArea, countedSet);
            dimList.addAll(conditionDims);
            dimList.addAll(filterDims);
            dimList.addAll(rowDims);
        } else if (olapTableArea.getType() == ExtendAreaType.PLANE_TABLE){
            dimList.addAll(this.getTableSelectionDims(olapTableDesignModel, olapTableArea, countedSet));
        }
        return dimList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.OlapLinkService#buildConditionMapFromRequestParams(java.lang.String)
     */
    public Map<String, Map<String, String>> buildConditionMapFromRequestParams(String uniqueName,
            ReportDesignModel olapTableDesignModel, QueryContext queryContext) {
        Map<String, Map<String, String>> conditionMap = new HashMap<String, Map<String, String>>();
        conditionMap = appendBuildCondMap(conditionMap, olapTableDesignModel, queryContext);
        /**
         * 得到的点击行的维值表达式
         */
        String[] uniqNames = DataModelUtils.parseNodeUniqueNameToNodeValueArray(uniqueName);
        for (String metaName : uniqNames) {
            String singleDimName = MetaNameUtil.getDimNameFromUniqueName(metaName);
            String singleDimValue = MetaNameUtil.getNameFromMetaName(metaName);
            Map<String, String> uniqueMap = buildConditionUniqueMap(singleDimValue, metaName);
            conditionMap.put(singleDimName, uniqueMap);
        }
        return conditionMap;
    }

    /**
     * 根据olapTableDesignModel和queryContext，先获取到公共查询区域的维度条件，并拼接完毕放入condMap中
     * 
     * @param conditionMap conditionMap
     * @param olapTableDesignModel olapTableDesignModel
     * @param queryContext queryContext
     * @return 返回拼接完成的公共查询条件condMap
     */
    private Map<String, Map<String, String>> appendBuildCondMap(Map<String, Map<String, String>> conditionMap,
            ReportDesignModel olapTableDesignModel, QueryContext queryContext) {
        ExtendArea[] extendAreaArray = olapTableDesignModel.getExtendAreaList();
        for (ExtendArea extendArea : extendAreaArray) {
            if (QueryUtils.isFilterArea(extendArea.getType())) {
                Item condDim = extendArea.getLogicModel().getRows()[0];
                addDimToMap(condDim, queryContext, olapTableDesignModel, extendArea, conditionMap);
            }
            // 此处需要新增对表格过滤轴的参数处理逻辑
            else if (ExtendAreaType.TABLE.equals(extendArea.getType())) {
                Stream.of(extendArea.getLogicModel().getSlices())
                        .filter(items -> items != null)
                        .forEach(condDim -> { 
                            addDimToMap(condDim, queryContext, olapTableDesignModel, extendArea, conditionMap);
                        });
            } else if (ExtendAreaType.LITEOLAP_TABLE.equals(extendArea.getType())) {
                ExtendArea liteOlapExtendArea = olapTableDesignModel.getExtendById(extendArea.getReferenceAreaId());
                Stream.of(liteOlapExtendArea.getLogicModel().getSlices())
                        .filter(items -> items != null)
                        .forEach(condDim -> { 
                            addDimToMap(condDim, queryContext, olapTableDesignModel, extendArea, conditionMap);
                });
            }
        }
        return conditionMap;
    }

    /**
     * 从queryContext取到需要的参数值，放入要传给平面表条件的conditionMap中
     * 
     * @param condDim condDim
     * @param queryContext queryContext
     * @param olapTableDesignModel olapTableDesignModel
     * @param extendArea extendArea
     * @param conditionMap conditionMap
     * @return conditionMap conditionMap
     */
    private Map<String, Map<String, String>> addDimToMap(Item condDim, 
            QueryContext queryContext, ReportDesignModel olapTableDesignModel,
            ExtendArea extendArea, Map<String, Map<String, String>> conditionMap) {
        // 如果是过滤条件的话，维度必须放在row上，并且维值只能有一个
        String olapElementId = condDim.getOlapElementId();
        String condDimValue = "";
        if (queryContext.get(olapElementId) == null) {
            condDimValue = null;
        } else {
            condDimValue = String.valueOf(queryContext.get(olapElementId));
        }
        Cube cube = olapTableDesignModel.getSchema().getCubes().get(extendArea.getCubeId());
        Dimension dim = cube.getDimensions().get(olapElementId);
        String condDimName = dim.getName();
        String condUniqueName = "";
        // 如果发现是时间条件或者本来传过来的就是uniqueName，则uniqueName直接等于condDimValue
        if (extendArea.getType() == ExtendAreaType.TIME_COMP || MetaNameUtil.isUniqueName(condDimValue)) {
            condUniqueName = condDimValue;
        } else {
            condUniqueName = MetaNameUtil.getNameFromMetaName(condDimValue);
            if (!StringUtils.isEmpty(condUniqueName) && !MetaNameUtil.isUniqueName(condUniqueName)) {
                condUniqueName = MetaNameUtil.makeUniqueName(dim, condDimValue);
            }
        }
        Map<String, String> uniqueMap = buildConditionUniqueMap(condDimValue, condUniqueName);
        conditionMap.put(condDimName, uniqueMap);
        return conditionMap;
    }

    /**
     * 根据由原生维值和不带层级的维度值构成的条件map对象
     * 
     * @param singleDimValue 不带层级的维度值
     * @param metaName 带层级表示的原生维度值
     * @return 返回根据由原生维值和不带层级的维度值构成的条件map对象
     */
    private Map<String, String> buildConditionUniqueMap(String singleDimValue, String metaName) {
        Map<String, String> uniqueMap = new HashMap<String, String>();
        uniqueMap.put(SINGLE_DIM_VALUE, singleDimValue);
        uniqueMap.put(UNIQUE_NAME, metaName);
        return uniqueMap;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.OlapLinkService#buildLinkBridgeParams(com.baidu.rigel.biplatform
     * .ma.report.model.LinkInfo, java.util.Map)
     */
    public Map<String, LinkParams> buildLinkBridgeParams(LinkInfo linkInfo,
            Map<String, Map<String, String>> conditionMap) {
        Map<String, LinkParams> linkBridgeParams = new HashMap<String, LinkParams>();
        for (String paramName : linkInfo.getParamMapping().keySet()) {
            LinkParams linkParams = new LinkParams();
            linkParams.setParamName(paramName);
            String dimName = linkInfo.getParamMapping().get(paramName);
            linkParams.setDimName(dimName);
            Map<String, String> uniqueMap = conditionMap.get(dimName);
            String dimValue = "";
            String metaName = "";
            if (!MapUtils.isEmpty(uniqueMap)) {
                dimValue = uniqueMap.get(SINGLE_DIM_VALUE);
                metaName = uniqueMap.get(UNIQUE_NAME);
            }
            linkParams.setOriginalDimValue(dimValue);
            linkParams.setUniqueName(metaName);
            linkBridgeParams.put(paramName, linkParams);
        }

        return linkBridgeParams;
    }

    /**
     * 根据报表设计模型，得到报表公共查询条件维度集合
     * 
     * @param olapTableDesignModel 多维报表设计模型
     * @return 返回报表公共查询条件维度组成的集合对象
     */
    private List<Dimension> getOlapTableConditionDims(ReportDesignModel olapTableDesignModel, Set<String> countedSet) {
        ExtendArea[] extendAreaArray = olapTableDesignModel.getExtendAreaList();
        List<Dimension> dimList = new ArrayList<Dimension>();
        for (ExtendArea extendArea : extendAreaArray) {
            boolean isFilter = QueryUtils.isFilterArea(extendArea.getType());
            if (isFilter) {
                Item item = extendArea.listAllItems().values().toArray(new Item[0])[0];
                Cube cube = olapTableDesignModel.getSchema().getCubes().get(extendArea.getCubeId());
                String dimId = item.getOlapElementId();
                Dimension dim = cube.getDimensions().get(dimId);
                addDimToList(dimList, dim, countedSet);
            }
        }
        return dimList;
    }

    /**
     * 判断维度是否已经被加入多维跳转维度备选列表中，如果被添加过了，就直接返回维度列表
     * 
     * @param dimList 维度列表
     * @param dim 维度对象
     * @param countedSet 计数用的countedSet
     * @return 返回添加完备选维度的维度列表
     */
    private List<Dimension> addDimToList(List<Dimension> dimList, Dimension dim, Set<String> countedSet) {
        if (!isDimAdded(dim.getId(), countedSet)) {
            dimList.add(dim);
            countedSet.add(dim.getId());
        }
        return dimList;
    }

    /**
     * 判断维度是否已经被加入多维跳转维度备选列表中
     * 
     * @param dimId 维度元素id
     * @param countedSet 计数用的countedSet
     * @return 如果已经被加入维度备选列表了，那么返回true，反之返回false
     */
    private boolean isDimAdded(String dimId, Set<String> countedSet) {
        if (countedSet.contains(dimId)) {
            return true;
        }
        return false;
    }

    /**
     * 根据报表设计模型，得到报表表格filter条件维度集合
     * 
     * @param olapTableDesignModel 多维报表设计模型
     * @return
     */
    private List<Dimension> getOlapTableFilterDims(ReportDesignModel olapTableDesignModel, ExtendArea olapTableArea,
            Set<String> countedSet) {
        LogicModel olapTableLogicModel = olapTableArea.getLogicModel();
        Cube cube = olapTableDesignModel.getSchema().getCubes().get(olapTableArea.getCubeId());
        Item[] items = olapTableLogicModel.getSlices();
        List<Dimension> dimList = new ArrayList<Dimension>();
        if (items != null && items.length > 0) {
            for (Item dimItem : items) {
                String dimId = dimItem.getOlapElementId();
                Dimension dim = cube.getDimensions().get(dimId);
                addDimToList(dimList, dim, countedSet);
            }
        }

        return dimList;
    }

    /**
     * 根据报表设计模型和table所在区域对象，得到放到table列上的维度集合
     * 
     * @param olapTableDesignModel 多维报表设计模型
     * @param olapTableArea 多维报表area区域对象
     * @return 返回放到table列上的维度集合
     */
    private List<Dimension> getOlapTableRowDims(ReportDesignModel olapTableDesignModel, ExtendArea olapTableArea,
            Set<String> countedSet) {
        LogicModel olapTableLogicModel = olapTableArea.getLogicModel();
        Cube cube = olapTableDesignModel.getSchema().getCubes().get(olapTableArea.getCubeId());
        Item[] items = olapTableLogicModel.getRows();
        List<Dimension> dimList = new ArrayList<Dimension>();
        for (Item dimItem : items) {
            String dimId = dimItem.getOlapElementId();
            Dimension dim = cube.getDimensions().get(dimId);
            if (dim == null) {
                continue;
            }
            addDimToList(dimList, dim, countedSet);
        }
        return dimList;
    }
    
    /**
     * 根据报表设计模型和table所在区域对象，得到放到table列上的维度集合
     * 
     * @param olapTableDesignModel 多维报表设计模型
     * @param olapTableArea 多维报表area区域对象
     * @return 返回放到table列上的维度集合
     */
    private List<Dimension> getTableSelectionDims(ReportDesignModel olapTableDesignModel, ExtendArea olapTableArea,
            Set<String> countedSet) {
        LogicModel olapTableLogicModel = olapTableArea.getLogicModel();
        Cube cube = olapTableDesignModel.getSchema().getCubes().get(olapTableArea.getCubeId());
        // add dim
        List<Dimension> dimList = new ArrayList<Dimension>();
        for (Item dimItem : olapTableLogicModel.getColumns()) {
            String id = dimItem.getOlapElementId();
            Dimension dim = cube.getDimensions().get(id);
            Measure measure = cube.getMeasures().get(id);
            if (dim != null) {
                addDimToList(dimList, dim, countedSet);
            } else if (measure != null) {
                StandardDimension standardDimension = new StandardDimension(measure.getName());
                standardDimension.setId(id);
                standardDimension.setCaption(measure.getCaption());
                addDimToList(dimList, standardDimension, countedSet);
            }
        }
        
        for (PlaneTableCondition planeTableCondition : olapTableDesignModel.getPlaneTableConditions().values()) {
            String id = planeTableCondition.getElementId();
            Dimension dim = cube.getDimensions().get(id);
            Measure measure = cube.getMeasures().get(id);
            if (dim != null) {
                addDimToList(dimList, dim, countedSet);
            } else if (measure != null) {
                StandardDimension standardDimension = new StandardDimension(measure.getName());
                standardDimension.setCaption(measure.getCaption());
                standardDimension.setId(id);
                addDimToList(dimList, standardDimension, countedSet);
            }
        }
        return dimList;
    }
}
