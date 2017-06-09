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
package com.baidu.rigel.biplatform.queryrouter.query.service.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.MemberNodeTree;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.queryrouter.query.vo.TesseractResultSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 根据TesseractResultSet和查询上下文构建DataModel
 * 
 * @author xiaoming.chen
 *
 */
public class DataModelBuilder {

    private static final String BLANK_ROW = "blankRow";

    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * HEAD_KEY_SPLIT
     */
    private static final String HEAD_KEY_SPLIT = "_+_";

    /**
     * PROP_KEY_SPLIT
     */
    private static final String PROP_KEY_SPLIT = "^_^";

    /**
     * tesseractResultSet 查询结果
     */
    private TesseractResultSet<SearchIndexResultRecord> tesseractResultSet;

    /**
     * queryContext 查询信息
     */
    private QueryContext queryContext;

    /**
     * columnBaseDatas
     */
    private List<List<BigDecimal>> columnBaseDatas;

    /**
     * constructor
     * 
     * @param tesseractResultSet
     */
    public DataModelBuilder(TesseractResultSet<SearchIndexResultRecord> tesseractResultSet, QueryContext queryContext) {
        if (tesseractResultSet == null) {
            log.warn("queryId:{} ResultSet is null,return table head", QueryRouterContext.getQueryId());
            // throw new IllegalArgumentException("tesseractResultSet is null");
        }
        if (queryContext == null) {
            throw new IllegalArgumentException("queryContext is null");
        }
        this.tesseractResultSet = tesseractResultSet;
        this.queryContext = queryContext;
    }

    public DataModel build(boolean isContainsCallbackMeasure, Cube cube) throws MiniCubeQueryException {
        DataModel dataModel = new DataModel();
        /**
         * get the order of row head
         */
        List<List<String>> rowNodeName = new ArrayList<List<String>>();
        List<MemberNodeTree> rowMemberTrees = queryContext.getRowMemberTrees();
        List<MemberTreePropResult> rowHeadNames =
                getHeadNameByOrder(rowMemberTrees, rowNodeName, isContainsCallbackMeasure);

        /**
         * get the order of col head
         */
        List<List<String>> columnNodeName = new ArrayList<List<String>>();
        List<MemberNodeTree> columnMemberTrees = queryContext.getColumnMemberTrees();
        List<MemberTreePropResult> colHeadNames = getHeadNameByOrder(columnMemberTrees, columnNodeName, false);
        // 构造交叉后的行列取数的KEY
        List<String> rowAxisKeys = generateAxisKeys(rowNodeName, null);
        List<MiniCubeMeasure> queryMeasures = queryContext.getQueryMeasures();
        List<String> columnAxisKeys = generateAxisKeys(columnNodeName, queryMeasures);

        if (this.tesseractResultSet != null) {
            Map<String, Map<String, BigDecimal>> parseData = null;
            try {
                parseData = parseResultSet(rowHeadNames, colHeadNames);
            } catch (Exception e) {
                e.printStackTrace();
                throw new MiniCubeQueryException("get data from resultset error.", e);
            }

            if (CollectionUtils.isNotEmpty(rowAxisKeys) && CollectionUtils.isNotEmpty(columnAxisKeys)) {
                columnBaseDatas = new ArrayList<List<BigDecimal>>(columnAxisKeys.size());
               
                for (int i = 0; i < columnAxisKeys.size(); i++) {
                    String columnName = columnAxisKeys.get(i);
                    if (columnBaseDatas.size() < i + 1) {
                        columnBaseDatas.add(new ArrayList<BigDecimal>(rowAxisKeys.size()));
                    }
                    if (CollectionUtils.isEmpty(rowAxisKeys)) {
                        rowAxisKeys.add(BLANK_ROW);
                    }

                    for (String rowName : rowAxisKeys) {
                        if (parseData.containsKey(rowName) && parseData.get(rowName).containsKey(columnName)) {
                            columnBaseDatas.get(i).add(parseData.get(rowName).get(columnName));
                        }
                        else {
                            // 填充一个null对象
                            columnBaseDatas.get(i).add(null);
                        }
                    }
                }
            }

        }

        dataModel.setColumnBaseData(columnBaseDatas);
        dataModel.setColumnHeadFields(buildAxisHeadFields(columnMemberTrees, queryMeasures));
        // 构建RowHeadField
        dataModel.setRowHeadFields(buildAxisHeadFields(rowMemberTrees, null));

        return dataModel;
    }

    /**
     * 
     * 解析ResultSet转换成按照单元格数据,此方法生成的结果集为宽表模型
     * 
     * @return 将ResultSet转换成的单元格数据
     * @throws Exception 取数的异常
     * 
     */
    private Map<String, Map<String, BigDecimal>> parseResultSet(List<MemberTreePropResult> rowHeadNames,
            List<MemberTreePropResult> colHeadNames) throws Exception {

        // 结构是 列 行，指标 值
        /**
         * data.key - 行的元数据的交叉值：比如dist与product交叉，dist维度值为:[1,2], product值为[3,4],data.key = [dist^_^1_+_product^_^3, ...
         * ...] data.value的map的key -- 列的元数据的交叉值 比如date与m1(指标字段名称)，date值为[2011,2012],交叉结果[date^_^2011_+_m1,......]
         * dava.value的map的value -- 单元格的具体数值
         */
        Map<String, Map<String, BigDecimal>> data = Maps.newHashMap();
        SearchIndexResultRecord record = null;
        // 按行遍历结果集，构建基于单元格的数据模型
        while (this.tesseractResultSet.next()) {

            record = (SearchIndexResultRecord) this.tesseractResultSet.getCurrentRecord();
            StringBuilder oneLine = new StringBuilder();

            // 构建行轴元数据key的交叉值
            for (MemberTreePropResult rowHeadName : rowHeadNames) {
                for (String prop : rowHeadName.queryPropers.keySet()) {
                    String value = tesseractResultSet.getString(prop);
                    if (rowHeadName.queryPropers.get(prop).isEmpty()
                            || (rowHeadName.queryPropers.get(prop).contains(value) && record.getGroupBy().contains(
                                    value)) || value.equals(record.getGroupBy())) {
                        oneLine.append(prop);
                        oneLine.append(PROP_KEY_SPLIT);
                        oneLine.append(value);
                        oneLine.append(HEAD_KEY_SPLIT);
                        break;
                    }
                }
            }
            if (oneLine.length() > 1) {
                oneLine.delete(oneLine.length() - HEAD_KEY_SPLIT.length(), oneLine.length());
            } else {
                oneLine.append(BLANK_ROW);
            }

            // 构建列轴元数据的交叉key
            StringBuilder oneColumn = new StringBuilder();
            for (MemberTreePropResult colHeadName : colHeadNames) {
                for (String prop : colHeadName.queryPropers.keySet()) {
                    String value = tesseractResultSet.getString(prop);
                    if (colHeadName.queryPropers.get(prop).isEmpty()
                            || colHeadName.queryPropers.get(prop).contains(value)) {
                        oneColumn.append(prop);
                        oneColumn.append(PROP_KEY_SPLIT);
                        oneColumn.append(value);
                        oneColumn.append(HEAD_KEY_SPLIT);
                        break;
                    }
                }

            }

            // 获取单元格值
            Map<String, BigDecimal> colValues = Maps.newHashMap();
            final String oneLineKey = oneLine.toString();
            for (MiniCubeMeasure measure : queryContext.getQueryMeasures()) {
                // if (measure.getAggregator () == Aggregator.CALCULATED) {
                // continue;
                // }
                final BigDecimal currentVal = tesseractResultSet.getBigDecimal(measure.getDefine());
                StringBuilder columnKey = new StringBuilder();
                columnKey.append(oneColumn);
                columnKey.append(measure.getName());
                if (data.containsKey(oneLineKey)) {
                    final BigDecimal oldVal = data.get(oneLineKey).get(columnKey.toString());
                    if (oldVal != null && currentVal != null && supportedCalculated(measure)) {
                        final Serializable newTmp = measure.getAggregator().aggregate(oldVal, currentVal);
                        BigDecimal newVal =
                                newTmp instanceof BigDecimal ? (BigDecimal) newTmp : new BigDecimal(newTmp.toString());
                        // data.put(columnKey.toString(), newVal);
                        data.get(oneLineKey).put(columnKey.toString(), newVal);
                    } else if (data.containsKey(oneLineKey)) {
                        data.get(oneLineKey).put(columnKey.toString(), currentVal);
                    }
                } else {
                    colValues.put(columnKey.toString(), currentVal);
                    data.put(oneLineKey, colValues);
                }
            }
            // 单行数据构建完毕
            // data.put(oneLineKey, colValues);
        }

        return data;
    }

    /**
     * 
     * @param measure
     * @return boolean
     */
    private boolean supportedCalculated(MiniCubeMeasure measure) {
        return measure.getAggregator() == Aggregator.COUNT || measure.getAggregator() == Aggregator.DISTINCT_COUNT
                || measure.getAggregator() == Aggregator.SUM;
    }

    /**
     * 依据维度成员以及指标定义构建行轴或者列轴的key字段 比如：地区产品交叉作为数据集的行，输入参数格式为： [[北京， 上海]，[食品，家电，玩具]] 返回结果[北京_+_食品，北京_+_家电，......,
     * 上海_+_食品，......]
     * 
     * @param nodeNames 交叉维度名称
     * @param measures 如果取行轴标示，值为空(后续行列转置可能会有不同)
     * @return List<String> 按照顺序生成的轴的key
     */
    private List<String> generateAxisKeys(List<List<String>> nodeNames, List<MiniCubeMeasure> measures) {
        List<String> axisKeys = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            for (int i = 0; i < nodeNames.size(); i++) {
                axisKeys = getCrossjoinNode(axisKeys, nodeNames.get(i));
            }
        }
        if (CollectionUtils.isNotEmpty(measures)) {
            List<String> measureName = new ArrayList<String>();
            measures.forEach((measure) -> {
                measureName.add(measure.getName());
            });
            axisKeys = getCrossjoinNode(axisKeys, measureName);
        }
        return axisKeys;
    }

    private List<String> getCrossjoinNode(List<String> parentNode, List<String> nextNode) {
        if (CollectionUtils.isEmpty(parentNode)) {
            return nextNode;
        } else if (CollectionUtils.isEmpty(nextNode)) {
            return parentNode;
        }
        int size = parentNode.size() * nextNode.size();
        List<String> result = new ArrayList<String>(size);
        for (int i = 0; i < parentNode.size(); i++) {
            for (String name : nextNode) {
                result.add(parentNode.get(i) + HEAD_KEY_SPLIT + name);
            }
        }
        return result;
    }

    /**
     * 获取交叉维度查询的key，按照一定顺序
     * 
     * @param memberNodes 交叉维度节点
     * @param rowNodeName
     * @return 维度查询KEY列表
     */
    private List<MemberTreePropResult> getHeadNameByOrder(List<MemberNodeTree> memberNodes,
            List<List<String>> rowNodeName, boolean isContainsCallbackMeasure) {
        List<MemberTreePropResult> headNames = Lists.newArrayList();
        for (int i = 0; i < memberNodes.size(); i++) {
            MemberNodeTree nodeTree = memberNodes.get(i);
            if (MetaNameUtil.isAllMemberName(nodeTree.getName()) && nodeTree.getChildren().isEmpty()) {
                return headNames;
            }
            MemberTreePropResult treeProp = new MemberTreePropResult();
            rowNodeName.add(getNodeName(nodeTree, null, treeProp, isContainsCallbackMeasure));
            if (MapUtils.isEmpty(treeProp.getQueryPropers())) {
                log.warn("queryId:{} query proper:{} is null,skip", QueryRouterContext.getQueryId(), nodeTree);
                continue;
            }
            headNames.add(treeProp);

        }
        return headNames;
    }

    /**
     * 解析节点树的查询属性对应的值，中间结果，用完就扔,构造层内部类，方便后续添加属性
     * 
     * @author xiaoming.chen
     * @version 2015年1月16日
     * @since jdk 1.8 or after
     */
    class MemberTreePropResult {

        /**
         * queryPropers 非叶子节点对应的查询属性，VALUE为查询的值
         */
        private Map<String, Set<String>> queryPropers = new LinkedHashMap<String, Set<String>>(3);

        /**
         * 获取 queryPropers
         * 
         * @return the queryPropers
         */
        public Map<String, Set<String>> getQueryPropers() {
            return queryPropers;
        }

    }

    /**
     * 获取每个层级查询的节点列表
     * 
     * @param nodeTree 维度维值树
     * @param nodeNames 节点列表
     * @return 查询的节点的名称列表
     */
    private List<String> getNodeName(MemberNodeTree nodeTree, List<String> nodeNames,
            MemberTreePropResult treePropResult, boolean isContainsCallbackMeasure) {
        if (nodeNames == null) {
            nodeNames = new ArrayList<String>();
        }
        String prop = nodeTree.getQuerySource();
        if (StringUtils.isNotBlank(nodeTree.getName())) {
            final boolean allMemberName = MetaNameUtil.isAllMemberName(nodeTree.getName());
            if (allMemberName && nodeTree.getChildren().isEmpty()) {
                return nodeNames;
            }
            if (StringUtils.isNotBlank(prop)) {
                if (allMemberName && isContainsCallbackMeasure) {
                    nodeNames.add(prop + PROP_KEY_SPLIT + TesseractConstant.SUMMARY_KEY);
                } else {
                    nodeNames.add(prop + PROP_KEY_SPLIT + nodeTree.getName());
                }
                treePropResult.getQueryPropers().put(prop, new HashSet<String>());
            }
        }
        if (CollectionUtils.isNotEmpty(nodeTree.getChildren())) {
            String childProp = null;
            for (MemberNodeTree child : nodeTree.getChildren()) {
                getNodeName(child, nodeNames, treePropResult, isContainsCallbackMeasure);
                if (childProp == null) {
                    childProp = child.getQuerySource();
                }
            }
            if (StringUtils.isNotBlank(prop) && !prop.equals(childProp)) {
                treePropResult.getQueryPropers().get(prop).add(nodeTree.getName());
            }

        }
        return nodeNames;
    }

    /**
     * 根据维度交叉信息构造头节点信息
     * 
     * @param treeNodes 维度交叉信息
     * @return
     */
    public static List<HeadField> buildAxisHeadFields(List<MemberNodeTree> treeNodes, List<MiniCubeMeasure> measures) {
        List<HeadField> result = Lists.newArrayList();
        for (MemberNodeTree nodeTree : treeNodes) {
            List<HeadField> curHeadFields = buildFieldsByMemberNodeTree(nodeTree, result.isEmpty(), null);
            if (result.isEmpty()) {
                result.addAll(curHeadFields);
            } else {
                addTailFields(result, curHeadFields);
            }
        }
        // 如果有指标的话，需要在每个叶子里面
        if (CollectionUtils.isNotEmpty(measures)) {
            List<HeadField> measureFields = new ArrayList<HeadField>(measures.size());
            measures.forEach((measure) -> {
                HeadField measureField = new HeadField(null);
                measureField.setCaption(measure.getCaption());
                measureField.setValue(measure.getUniqueName());
                measureFields.add(measureField);
            });
            // 将指标添加到末尾
            addTailFields(result, measureFields);
        }

        return result;
    }

    /**
     * 将tailHeadFields插入的headFields的后面，和每个headField的叶子交叉
     * 
     * @param headFields 原先的头节点信息
     * @param tailFields 插入的新的叶子
     */
    private static void addTailFields(List<HeadField> headFields, List<HeadField> tailFields) {
        if (CollectionUtils.isNotEmpty(headFields)) {
            if (CollectionUtils.isNotEmpty(tailFields)) {
                headFields.forEach((node) -> {
                    List<HeadField> leafNodes = node.getLeafFileds(true);
                    for (HeadField leaf : leafNodes) {
                        // 在每个叶子节点后面添加下层节点
                        leaf.getNodeList().addAll(packageParentLevelField(DeepcopyUtils.deepCopy(tailFields), leaf));
                    }
                });
            }
        } else {
            headFields.addAll(tailFields);
        }
    }

    /**
     * 将指定节点和该节点的上层节点改成parentLevelField
     * 
     * @param nodes 需要修改上层节点的节点列表
     * @param parentLevelField 上层节点
     * @return
     */
    private static List<HeadField> packageParentLevelField(List<HeadField> nodes, HeadField parentLevelField) {
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (HeadField field : nodes) {
                field.setParentLevelField(parentLevelField);
                // 生成一下节点的UniqueName
                parentLevelField.getNodeUniqueName();
                field.getNodeUniqueName();
                packageParentLevelField(field.getChildren(), parentLevelField);
            }
        }
        return nodes;
    }

    public static List<HeadField> buildFieldsByMemberNodeTree(MemberNodeTree nodeTree, boolean isFirstNode,
            HeadField parent) {
        List<HeadField> result = Lists.newArrayList();
        HeadField node = null;
        if (StringUtils.isNotBlank(nodeTree.getName())) {
            node = buildField(nodeTree, isFirstNode, parent);
        }
        List<HeadField> children = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(nodeTree.getChildren())) {
            final HeadField parentField = node;
            nodeTree.getChildren().forEach((field) -> {
                children.addAll(buildFieldsByMemberNodeTree(field, isFirstNode, parentField));
            });
        }

        if (node != null) {
            node.setChildren(children);
            result.add(node);
            return result;
        } else {
            return children;
        }
    }

    /**
     * 根据节点创建HeadField
     * 
     * @param node 节点
     * @return DataModel的头节点
     */
    private static HeadField buildField(MemberNodeTree node, boolean isParent, HeadField parent) {
        HeadField headField = new HeadField(null);
        headField.setCaption(node.getCaption());
        headField.setValue(node.getUniqueName());
        if (isParent) {
            headField.getNodeUniqueName();
        }
        headField.setHasChildren(node.isHasChildren());
        headField.setParent(parent);
        return headField;
    }

}
