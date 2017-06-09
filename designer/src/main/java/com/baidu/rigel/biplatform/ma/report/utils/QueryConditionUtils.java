package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MeasureCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableCondition;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.google.common.collect.Lists;

/**
 * 查询条件构建工具类
 * 
 * @author yichao.jiang
 *
 */
public class QueryConditionUtils {

    /**
     * 为平面表查询构造条件
     * 
     * @param reportModel
     * @param area
     * @param queryAction
     * @return
     * @throws QueryModelBuildException
     */
    public static Map<String, MetaCondition> buildQueryConditionsForPlaneTable(ReportDesignModel reportModel,
        ExtendArea area, QueryAction queryAction) throws QueryModelBuildException {
        // 查询条件的map，key为对应的item的elementId，value值为DimCondition或者是MeasureCondition
        Map<String, MetaCondition> rs = new HashMap<String, MetaCondition>();
        // 将QueryAction中的横轴、纵轴、过滤轴的信息先进行添加
        Map<Item, Object> items = new HashMap<Item, Object>();
        items.putAll(queryAction.getColumns());
        items.putAll(queryAction.getRows());
        items.putAll(queryAction.getSlices());
        // 获取平面表查询条件
        Map<String, PlaneTableCondition> planeTableConditions = reportModel.getPlaneTableConditions();
        // 遍历所有的item对象
        for (Map.Entry<Item, Object> entry : items.entrySet()) {
            Item item = entry.getKey();
            // 获取OlapElement定义
            OlapElement olapElement = getOlapElement(reportModel, area, item, true);
            // 如果在维度和指标中都不存在，则跳出
            if (olapElement == null
            // 如果entry.getValue为all membername则不组织questionmodel的querycondition条件
                    || (entry != null && entry.getValue() != null
                            && entry.getValue() instanceof String[] && MetaNameUtil
                                .isAllMemberName(((String[]) entry.getValue())[0]))) {
                continue;
            }
            // 如果为维度，则构建维度条件
            if (olapElement instanceof Dimension) {
                DimensionCondition condition = buildDimensionConditionForPlaneTable(olapElement, entry.getValue());
                rs.put(condition.getMetaName(), condition);
            } else if (olapElement instanceof Measure) {
                // 如果该指标被选为条件
                if (planeTableConditions.containsKey(olapElement.getId())) {
                    PlaneTableCondition planeTableCondition = planeTableConditions.get(olapElement.getId());
                    MeasureCondition measureCondition =
                            buildMeasureConditionForPlaneTable(olapElement, entry.getValue(), planeTableCondition);
                    rs.put(olapElement.getName(), measureCondition);
                }
            }
        }
        return rs;
    }

    /**
     * 构建平面表的维度条件
     * 
     * @param olapElement
     * @param valueObj 对应的维度值
     * @return
     */
    private static DimensionCondition buildDimensionConditionForPlaneTable(OlapElement olapElement, Object valueObj) {
        // 维度条件
        DimensionCondition condition = new DimensionCondition(olapElement.getName());
        // // 对应的条件值
        // Object valueObj = entry.getValue();
        if (valueObj != null) {
            List<String> values = Lists.newArrayList();
            if (valueObj instanceof String[]) {
                values = Lists.newArrayList();
                String[] tmp = resetValues(olapElement.getName(), (String[]) valueObj);
                CollectionUtils.addAll(values, (String[]) tmp);
            } else {
                String tmp = resetValues(olapElement.getName(), valueObj.toString())[0];
                values.add(tmp);
            }
            // 所有条件
            List<QueryData> datas = Lists.newArrayList();
            // 对所有的条件值进行遍历，构建QueryData
            for (String value : values) {
                QueryData data = new QueryData(value);
                data.setExpand(true);
                data.setShow(false);
                datas.add(data);
            }
            condition.setQueryDataNodes(datas);
        } else {
            // 如果没有条件值，则置为空
            Dimension dim = (Dimension) olapElement;
            List<QueryData> datas = new ArrayList<QueryData>();
            QueryData data = new QueryData(dim.getAllMember().getUniqueName());
            data.setExpand(true);
            data.setShow(false);
            datas.add(data);
            condition.setQueryDataNodes(datas);
        }
        return condition;
    }

    /**
     * 构建平面表所需的指标条件
     * 
     * @param olapElment
     * @param valueObj
     * @param planeTableCondition
     * @return
     */
    private static MeasureCondition buildMeasureConditionForPlaneTable(OlapElement olapElement, Object valueObj,
        PlaneTableCondition planeTableCondition) {
        MeasureCondition measureCondition = new MeasureCondition(olapElement.getName());
        // 获取SQL查询条件
        SQLConditionType sqlType = planeTableCondition.getSQLCondition();
        // 获取查询条件默认值
        String defaultValue = planeTableCondition.getDefaultValue();
        if (valueObj != null) {
            // 设置指标条件值
            measureCondition.setMeasureConditions(buildSqlCondition((Measure) olapElement, sqlType, valueObj));
        } else {
            if (StringUtils.hasText(defaultValue)) {
                measureCondition.setMeasureConditions(buildSqlCondition((Measure) olapElement, sqlType, defaultValue));
            } else {
                measureCondition.setMeasureConditions(null);
            }
        }
        return measureCondition;
    }

    /**
     * 构建SQL条件 buildSqlCondition
     * 
     * @param measure
     * @param value
     * @return
     */
    private static SQLCondition buildSqlCondition(Measure measure, SQLConditionType sqlType, Object value) {
        SQLCondition sqlCondition = new SQLCondition();
        List<String> conditionValues = Lists.newArrayList();
        if (value instanceof String[]) {
            Collections.addAll(conditionValues, (String[]) value);
        } else {
            String[] tmpValues = ((String) value).split(",");
            Collections.addAll(conditionValues, tmpValues);
        }
        sqlCondition.setConditionValues(conditionValues);
        sqlCondition.setCondition(sqlType);
        sqlCondition.setMetaName(measure.getUniqueName());
        return sqlCondition;
    }

    /**
     * 构建查询条件信息
     * 
     * @param reportModel
     * @param area
     * @param queryAction
     * @return
     * @throws QueryModelBuildException
     */
    public static Map<String, MetaCondition> buildQueryConditionsForPivotTable(ReportDesignModel reportModel,
        ExtendArea area, QueryAction queryAction) throws QueryModelBuildException {
        Map<String, MetaCondition> rs = new HashMap<String, MetaCondition>();
        Map<Item, Object> items = new HashMap<Item, Object>();
        items.putAll(queryAction.getColumns());
        items.putAll(queryAction.getRows());
        items.putAll(queryAction.getSlices());
        int firstIndex = 0;
        for (Map.Entry<Item, Object> entry : items.entrySet()) {
            Item item = entry.getKey();
            // 获取OlapElement定义
            OlapElement olapElement = getOlapElement(reportModel, area, item, false);
            if (olapElement == null) {
                continue;
            }
            if (olapElement instanceof Dimension) {
                DimensionCondition condition = new DimensionCondition(olapElement.getName());
                Object valueObj = entry.getValue();
                if (valueObj != null) {
                    List<String> values = Lists.newArrayList();
                    if (valueObj instanceof String[]) {
                        values = Lists.newArrayList();
                        String[] tmp = resetValues(olapElement.getName(), (String[]) valueObj);
                        CollectionUtils.addAll(values, (String[]) tmp);
                    } else {
                        String tmp = resetValues(olapElement.getName(), valueObj.toString())[0];
                        values.add(tmp);
                    }

                    List<QueryData> datas = Lists.newArrayList();
                    // TODO 需要排查为何多处根节点UniqueName不一致
                    String rootUniqueName = "[" + olapElement.getName() + "].[All_" + olapElement.getName();
                    // TODO QeuryData value如何处理
                    for (String value : values) {
                        if (!queryAction.isChartQuery() 
                                && value.indexOf(rootUniqueName) != -1 
                                && !(olapElement instanceof TimeDimension)) {
                            datas.clear();
                            break;
                        }
                        QueryData data = new QueryData(value);
                        Object drillValue = queryAction.getDrillDimValues().get(item);
                        String tmpValue = null;
                        if (valueObj instanceof String[]) {
                            tmpValue = ((String[]) valueObj)[0];
                        } else {
                            tmpValue = valueObj.toString();
                        }
                        if (drillValue != null && tmpValue.equals(drillValue)) {
                            data.setExpand(true);
                        } else if ((item.getPositionType() == PositionType.X
                                || item.getPositionType() == PositionType.S)
                                && queryAction.isChartQuery()) {
                            // 修正图形查询方式
                            if (MetaNameUtil.isAllMemberUniqueName(data.getUniqueName())) {
                                data.setExpand(true);
                                data.setShow(false);
                            } else {
                                data.setExpand(false);
                                data.setShow(true);
                            }
                        }
                        // 修正展开方式
                        if (item.getParams().get(Constants.LEVEL) != null) {
                            if (item.getParams().get(Constants.LEVEL).equals(1)) {
                                data.setExpand(!queryAction.isChartQuery());
                                data.setShow(true);
                            } else if (item.getParams().get(Constants.LEVEL).equals(2)) {
                                data.setExpand(true);
                                data.setShow(false);
                            }
                            boolean allMemberUniqueName = MetaNameUtil.isAllMemberUniqueName(data.getUniqueName());
                            if (allMemberUniqueName && queryAction.isChartQuery()) {
                                data.setExpand(true);
                                data.setShow(false);
                            }
                        }
                        
//                        if (queryAction.getRows().size() == 1) {
//                            data.setExpand(true);
//                        }
                        datas.add(data);
                    }
                    if (values.isEmpty() && queryAction.isChartQuery()) {
                        QueryData data = new QueryData(rootUniqueName + "s]");
                        data.setExpand(true);
                        data.setShow(false);
                        datas.add(data);
                    }
                    condition.setQueryDataNodes(datas);
                } else {
                    List<QueryData> datas = new ArrayList<QueryData>();
                    Dimension dim = (Dimension) olapElement;
                    if ((item.getPositionType() == PositionType.X || item.getPositionType() == PositionType.S)
                            && queryAction.isChartQuery()) {
                        QueryData data = new QueryData(dim.getAllMember().getUniqueName());
                        data.setExpand(true);
                        data.setShow(false);
                        datas.add(data);
                    } else if (dim.getType() == DimensionType.CALLBACK) {
                        QueryData data = new QueryData(dim.getAllMember().getUniqueName());
                        data.setExpand(firstIndex == 0);
                        data.setShow(firstIndex != 0);
                        datas.add(data);
                    }
                    condition.setQueryDataNodes(datas);
                }
                // 时间维度，并且在第一列位置，后续改成可配置方式
                if (item.getPositionType() == PositionType.X 
                        && olapElement instanceof TimeDimension 
                        && firstIndex == 0
                        && !queryAction.isChartQuery()) {
                    condition.setMemberSortType(SortType.DESC);
                    ++firstIndex;
                }
                rs.put(condition.getMetaName(), condition);
            }
        }
        return rs;
    }

    /**
     * 
     * @param reportModel
     * @param area
     * @param item
     * @param includeMeasure
     * @return
     * @throws QueryModelBuildException
     */
    private static OlapElement getOlapElement(ReportDesignModel reportModel, ExtendArea area, Item item,
        boolean includeMeasure) throws QueryModelBuildException {
        // 获取该OlapElement
        OlapElement olapElement = ReportDesignModelUtils
                .getDimOrIndDefineWithId(reportModel.getSchema(), area.getCubeId(),
                item.getOlapElementId());
        if (olapElement == null) {
            // 判断其是否为维度
            Cube cube = com.baidu.rigel.biplatform.ma.report.utils.QueryUtils.getCubeWithExtendArea(reportModel, area);
            // 在Cube的维度中寻找是否有此olapElement
            for (Dimension dim : cube.getDimensions().values()) {
                if (dim.getId().equals(item.getOlapElementId())) {
                    olapElement = dim;
                    break;
                }
            }

            // 如果需要在指标当中进行查找
            if (includeMeasure) {
                // 在Cube的指标中，寻找是否有此olapElement
                for (Measure measure : cube.getMeasures().values()) {
                    if (item.getOlapElementId().equals(measure.getId())) {
                        olapElement = measure;
                        break;
                    }
                }
            }
        }
        return olapElement;
    }

    /**
     * 
     * @param dimName
     * @param valueObj
     * @return
     */
    private static String[] resetValues(String dimName, String...valueObj) {
        if (valueObj == null) {
            return null;
        }
        String[] rs = new String[valueObj.length];
        int i = 0;
        for (String str : valueObj) {
            if (!MetaNameUtil.isUniqueName(str)) {
//                // TODO，后续对LIKE维度条件进行处理
//                String tmpStr = str.replace("%", "");
                if (!StringUtils.hasText(str)) {
                    // 该设置，默认会将该维度条件过滤掉
                    rs[i] = "[" + dimName + "].[All_]";
                } else {
                    rs[i] = "[" + dimName + "].[" + str + "]";
                }
            } else {
                rs[i] = str;
            }
            ++i;
        }
        return rs;
    }

}
