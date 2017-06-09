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
package com.baidu.rigel.biplatform.tesseract.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.isservice.search.agg.AggregateCompute;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.Expression;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryObject;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.SqlSelectColumnType;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 * QueryRequestUtil
 * 
 * @author lijin
 *
 */
public class QueryRequestUtil {

    /**
     * SQL_STRING_FORMAT
     */
    private static final String SQL_STRING_FORMAT = "\'%s\'";

    private static Logger LOGGER = LoggerFactory
            .getLogger(QueryRequestUtil.class);

    /**
     * 
     * transQueryRequestAndList2Map:analyze andList of queryRequest ,trans
     * andList into Map<String,List<String>>
     * 
     * @param query
     *            queryRequest
     * @return Map<String,List<String>> the result map,whose key is property and
     *         value is leafvalues
     */
    public static Map<String, List<String>> transQueryRequestAndList2Map(
            QueryRequest query) {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        for (Expression expression : query.getWhere().getAndList()) {
            String fieldName = expression.getProperties();
            List<String> valueList = new ArrayList<String>();
            for (QueryObject qo : expression.getQueryValues()) {
                valueList.addAll(qo.getLeafValues());
            }
            resultMap.put(fieldName, valueList);
        }
        return resultMap;
    }

    /**
     * 
     * transQueryRequest2LeafMap: transfer QueryObject into
     * Map<String,Map<String, String>>
     * 
     * @param query
     *            query
     * @return Map<String,Map<String, String>> : key is propertie,and
     *         Map<String,String> key is leafvalue of QueryObject and value is
     *         value of QueryObject
     */
    public static Map<String, Map<String, Set<String>>> transQueryRequest2LeafMap(QueryRequest query) {
        if (query == null || query.getWhere() == null || query.getWhere().getAndList() == null) {
            throw new IllegalArgumentException();
        }

        Map<String, Map<String, Set<String>>> resultMap = new HashMap<String, Map<String, Set<String>>>();
        // process andList
        for (Expression ex : query.getWhere().getAndList()) {
            Map<String, Set<String>> curr = new HashMap<String, Set<String>>();
            if (resultMap.get(ex.getProperties()) != null) {
                curr = resultMap.get(ex.getProperties());
            }
            for (QueryObject qo : ex.getQueryValues()) {
                for (String leaf : qo.getLeafValues()) {
                    Set<String> valueSet = curr.get(leaf);
                    if (valueSet == null) {
                        valueSet = new HashSet<String>();
                    }
                    if (!qo.isSummary()
                            && !StringUtils.equals(leaf, qo.getValue())) {
                        valueSet.add(qo.getValue());
                    }
                    if (CollectionUtils.isNotEmpty(valueSet)) {
                        curr.put(leaf, valueSet);
                    }
                }
            }
            if (query.getSelect().getQueryProperties().contains(ex.getProperties())
                && !curr.isEmpty()) {
                resultMap.put(ex.getProperties(), curr);
            }
        }

        return resultMap;
    }

    /**
     * 
     * transQueryRequest2LuceneQuery queryRequest->query for lucene
     * 
     * @param query
     *            queryRequest
     * @return Query query for lucene
     * @throws ParseException
     *             解析异常
     */
    public static Query transQueryRequest2LuceneQuery(QueryRequest query)
            throws ParseException {
        if (query == null || query.getWhere() == null) {
            throw new IllegalArgumentException();
        }
        BooleanQuery queryAll = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        // process where
        // process and condition
        Map<String, List<String>> andCondition = transQueryRequestAndList2Map(query);
        for (String fieldName : andCondition.keySet()) {
            BooleanQuery subQuery = new BooleanQuery();
            for (String qs : andCondition.get(fieldName)) {
                subQuery.add(new TermQuery(new Term(fieldName, qs)),
                        Occur.SHOULD);
            }
            queryAll.add(subQuery, Occur.MUST);
        }

        return queryAll;
    }

    /**
     * 
     * transQueryRequest2SqlQuery queryRequest->sqlQuery
     * 
     * @param query
     *            query
     * @return SqlQuery SqlQuery
     */
    public static SqlQuery transQueryRequest2SqlQuery(QueryRequest query) {
        if (query == null || query.getWhere() == null) {
            throw new IllegalArgumentException();
        }

        SqlQuery result = new SqlQuery();
        SqlDataSourceInfo sqlDataSourceInfo = (SqlDataSourceInfo)query.getDataSourceInfo();
        if (sqlDataSourceInfo.getDataBase() == DataBase.PALO) {
            result.setAggSql(true);
        }
        result.setDatabase(((SqlDataSourceInfo) query.getDataSourceInfo()).getDataBase());
        // 处理from
        if (query.getGroupBy() != null) {
            result.setGroupBy(query.getGroupBy().getGroups());
        }

        LinkedList<String> fromList = new LinkedList<String>();
        fromList.add(query.getFrom().getFrom());
        result.setFromList(fromList);
        // 处理limit
        if (query.getLimit() != null) {
            result.setLimitMap(query.getLimit().getStart(), query.getLimit().getSize());
        }

        /**
         * 添加distinct 约束
         */
        result.setDistinct(query.isDistinct());
        // 处理select
        // getQueryProperties
        Set<String> selectList = Sets.newLinkedHashSet();
        if (query.getSelect() != null) {
            selectList.addAll(query.getSelect().getQueryProperties());
            for (String s : query.getSelect().getQueryProperties()) {
                result.getSqlSelectColumn(s).setSelect(s);
            }
            if (CollectionUtils.isNotEmpty(query.getSelect().getQueryMeasures())) {
                for (QueryMeasure qm : query.getSelect().getQueryMeasures()) {
                    result.getSqlSelectColumn(qm.getProperties()).setSelect(qm.getProperties());
                    result.getSqlSelectColumn(qm.getProperties()).setOperator(qm.getAggregator().name());
                    
                    if (SqlQuery.getAggcommonoperator().contains(qm.getAggregator().name())) {
                        result.getSqlSelectColumn(qm.getProperties())
                            .setSqlSelectColumnType(SqlSelectColumnType.OPERATOR_COMMON);
                    } else {
                        result.getSqlSelectColumn(qm.getProperties())
                            .setSqlSelectColumnType(SqlSelectColumnType.OPERATOR_DISTINCT_COUNT);
                    }
                    selectList.add(qm.getProperties());
                }
            }
        }
        // 处理where
        Map<String, List<String>> andCondition = transQueryRequestAndList2Map(query);
        List<String> whereList = new ArrayList<String>();
        String betweenStr = null;
//        if (query.getWhere().getBetween() != null) {
//            betweenStr = query.getWhere().getBetween().getProperties();
//            selectList.add(betweenStr);
//            result.getSqlSelectColumn(betweenStr).setSelect(betweenStr);
//            result.getSqlFunction().put(
//                    betweenStr,
//                    "DATE_FORMAT(" + betweenStr + ", \"%Y%m%d\") as "
//                            + betweenStr + ", ");
//            whereList.add(query.getWhere().getBetween().toString());
//        }

        for (String key : andCondition.keySet()) {
            if (key.equals(betweenStr)) {
                continue;
            }
            if (andCondition.get(key) == null || andCondition.get(key).size() == 0) {
                continue;
            }
            if (!result.isAggSql()) {
                result.getSqlSelectColumn(key).setSelect(key);
                selectList.add(key);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append(" in (");
            sb.append(StringUtils.join(transValue2SqlString(andCondition.get(key)), ","));
            sb.append(")");
            whereList.add(sb.toString());
        }
        result.setWhereList(whereList);
        result.getSelectList().addAll(selectList);

        return result;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 
     * transValue2SqlString
     * 
     * @param valueList
     *            valueList
     * @return List<String>
     */
    private static List<String> transValue2SqlString(List<String> valueList) {
        List<String> result = new ArrayList<String>();
        if (valueList == null || valueList.size() == 0) {
            return result;
        }
        for (String key : valueList) {
            String sqlKey = org.apache.commons.lang.StringEscapeUtils.escapeSql(key);
            if (!isNumeric(sqlKey)) {
                sqlKey = String.format(SQL_STRING_FORMAT, sqlKey);
            }
            if (sqlKey.contains("å") || sqlKey.contains("�")) {
                continue;
            }
            result.add(sqlKey.replace("\\", "\\\\"));
        }
        return result;
    }

    static class PullUpProperties {
        String childField;

        String pullupField;

        String pullupValue;

        /**
         * 构造函数
         */
        public PullUpProperties(String pullupField, String pullupValue) {
            this.pullupField = pullupField;
            this.pullupValue = pullupValue;
        }

    }

    /**
     * collectAllMem
     * 
     * @param queryContext
     * @return
     */
    private static List<PullUpProperties> collectAllMem(
            QueryContext queryContext) {
        List<PullUpProperties> results = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryContext.getColumnMemberTrees())) {
            queryContext.getColumnMemberTrees().forEach(tree -> {
                PullUpProperties properties = coolectAllMem(tree);
                if (properties != null) {
                    results.add(properties);
                }
            });
        }

        if (CollectionUtils.isNotEmpty(queryContext.getRowMemberTrees())) {
            queryContext.getRowMemberTrees().forEach(tree -> {
                PullUpProperties properties = coolectAllMem(tree);
                if (properties != null) {
                    results.add(properties);
                }
            });
        }

        return results;
    }

    /**
     * coolectAllMem
     * 
     * @param memberNodeTree
     * @return
     */
    private static PullUpProperties coolectAllMem(MemberNodeTree memberNodeTree) {
        PullUpProperties result = null;
        if (StringUtils.isNotBlank(memberNodeTree.getName())
                && memberNodeTree.getChildren().size() >= 1) {
            // Fixed by Me David.wang 解决callback维度下钻返回多层，汇总数据计算错误问题
            // update by majun  当维度为非callback维度时，获取all成员时，只需当children个数大于0即可
            if ((memberNodeTree.isCallback() && memberNodeTree.getChildren().size() == 1 && memberNodeTree
                    .getChildren().get(0).getChildren().size() > 1)
                    || memberNodeTree.getChildren().size() == 1
                    && memberNodeTree.getChildren().get(0).getChildren().size() > 0) {
                return coolectAllMem(memberNodeTree.getChildren().get(0));
            }
            result = new PullUpProperties(memberNodeTree.getQuerySource(),
                    memberNodeTree.getName());
            result.childField = memberNodeTree.getChildren().get(0)
                    .getQuerySource();
            if (result.pullupField.equals(result.childField)) {
                result.childField = null;
            }
            return result;
        } else {
            if (memberNodeTree.getChildren().size() == 1) {
                return coolectAllMem(memberNodeTree.getChildren().get(0));
            }
        }
        return result;
    }

    public static SearchIndexResultSet processGroupBy(SearchIndexResultSet dataSet, QueryRequest query,
        QueryContext queryContext) throws NoSuchFieldException {
        List<SearchIndexResultRecord> transList = null;
        long current = System.currentTimeMillis();
        Map<String, Map<String, Set<String>>> leafValueMap = QueryRequestUtil.transQueryRequest2LeafMap(query);
        List<PullUpProperties> allDimVal = collectAllMem(queryContext);
        
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " to collect leaf map.");
        current = System.currentTimeMillis();
        List<String> groupList = Lists.newArrayList(query.getGroupBy().getGroups());
        List<QueryMeasure> queryMeasures = query.getSelect().getQueryMeasures();
        // 这里开始算值都得将count改成sum了
        queryMeasures.forEach(measure -> {
            if (measure.getAggregator().equals(Aggregator.COUNT)) {
                measure.setAggregator(Aggregator.SUM);
            }
            if (query.isSqlAgg() && measure.getAggregator().equals(Aggregator.DISTINCT_COUNT)) {
                measure.setAggregator(Aggregator.SUM);
            }
        });
        Meta meta = dataSet.getMeta();
        List<MemberNodeTree> rowMemberTrees = queryContext.getRowMemberTrees ();
        boolean hasSameNode = false;
        for (MemberNodeTree tmp : getLeafNodes (rowMemberTrees)) {
            if (tmp != null && tmp.isCallback () && hasSameNode(tmp, rowMemberTrees)) {
                    hasSameNode = true;
                    break;
                }
        }
        final boolean hasSameNodeCopy = hasSameNode;
        MemberNodeTree root = getRootMember (rowMemberTrees);
        if (hasSameNode) {
            for (SearchIndexResultRecord d : dataSet.getDataList ()) {
                Serializable field = d.getField (meta.getFieldIndex (root.getQuerySource ()));
                if (CollectionUtils.isNotEmpty (leafValueMap.get (root.getQuerySource ()).get (field + ""))) {
                    leafValueMap.get (root.getQuerySource ()).get (field + "").add (root.getName ());
                }
            }
        }
        
        int dimSize = query.getSelect().getQueryProperties().size();
        if (dataSet != null && dataSet.size() != 0) {
            transList = dataSet.getDataList();
            transList = handleGroupBy(leafValueMap, transList, meta, hasSameNodeCopy, groupList);
            transList = AggregateCompute.aggregate(transList, dimSize, queryMeasures);
        } else {
            return dataSet;
        }
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " to map leaf.");
        current = System.currentTimeMillis();

        if (CollectionUtils.isEmpty(queryMeasures)) {
            dataSet.setDataList(AggregateCompute.distinct(transList));
            return dataSet;
        }

        transList =
                aggNodeOfAllLevel(transList, allDimVal, root, meta, hasSameNodeCopy, groupList, dimSize, queryMeasures);
        transList = aggSubLevelNode(queryContext, transList, meta, groupList, dimSize, queryMeasures);
        dataSet.setDataList(transList);
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " aggregator leaf.");
        return dataSet;
    }

    /**
     * 为二级父节点做聚集
     * 
     * @param queryContext queryContext
     * @param transList transList
     * @param meta meta
     * @param groupList groupList
     * @param dimSize dimSize
     * @param queryMeasures queryMeasures
     * @return 返回添加了二级父节点的结果集列表
     * @throws NoSuchFieldException NoSuchFieldException
     */
    private static List<SearchIndexResultRecord> aggSubLevelNode(QueryContext queryContext,
            List<SearchIndexResultRecord> transList, Meta meta, List<String> groupList, int dimSize,
            List<QueryMeasure> queryMeasures) throws NoSuchFieldException {
        MemberNodeTree memberNodeTree = queryContext.getRowMemberTrees().get(0);
        List<MemberNodeTree> rs = memberNodeTree.transMemberNodeTreeToList(null, memberNodeTree);

        Set<String> groupByNames = new HashSet<String>();
        // 先找到汇总结果里面已有的维度节点名称列表
        for (SearchIndexResultRecord resultRecord : transList) {
            String groupBy = resultRecord.getGroupBy();
            groupByNames.add(groupBy);
        }
        // 过滤出子节点有，但是汇总结果中没涉及到的节点名称集合
        Set<String> dimNames4Return = new HashSet<String>();
        for (MemberNodeTree nt : rs) {
            String dimName = nt.getName();
            if (!groupByNames.contains(dimName)) {
                dimNames4Return.add(dimName);
            }
        }
        // 为过滤出的未涉及节点，按照已有最细node数据挨个做分组聚集
        if (!CollectionUtils.isEmpty(dimNames4Return)) {
            for (String needAggDimName : dimNames4Return) {
                MemberNodeTree needAggMemberTree = memberNodeTree.getMemberNodeTreeByName(needAggDimName);
                List<MemberNodeTree> lastLevelNodeList = needAggMemberTree.getLastChildNodes(null, needAggMemberTree);
                List<SearchIndexResultRecord> aggLevelRecordList = new ArrayList<SearchIndexResultRecord>();
                for (MemberNodeTree lastNode : lastLevelNodeList) {
                    for (SearchIndexResultRecord rc : transList) {
                        if (rc.getGroupBy().equals(lastNode.getName())) {
                            SearchIndexResultRecord vRecord = DeepcopyUtils.deepCopy(rc);
                            vRecord.setField(meta.getFieldIndex(lastNode.getQuerySource()), needAggMemberTree.getName());
                            generateGroupBy(vRecord, groupList, meta);
                            aggLevelRecordList.add(vRecord);
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(aggLevelRecordList)) {
                    List<SearchIndexResultRecord> result =
                            AggregateCompute.aggregate(aggLevelRecordList, dimSize, queryMeasures);
                    transList.addAll(result);
                }
            }
        }
        return transList;
    }
    
    /**
     * 
     * @param dataSet dataSet
     * @param leafValueMap leafValueMap
     * @param transList transList
     * @param meta meta
     * @param hasSameNodeCopy hasSameNodeCopy
     * @param groupList groupList
     * @return transList
     */
    private static List<SearchIndexResultRecord> handleGroupBy(
            Map<String, Map<String, Set<String>>> leafValueMap, List<SearchIndexResultRecord> transList, Meta meta,
            boolean hasSameNodeCopy, List<String> groupList) {
        if (MapUtils.isNotEmpty(leafValueMap)) {
            List<SearchIndexResultRecord> copyLeafRecords = new ArrayList<SearchIndexResultRecord>();
            Iterator<SearchIndexResultRecord> it = transList.iterator();
            while (it.hasNext()) {
                SearchIndexResultRecord record = it.next();
                it.remove();
                for (Map.Entry<String, Map<String, Set<String>>> entry : leafValueMap.entrySet()) {
                    String prop = entry.getKey();
                    Map<String, Set<String>> valueMap = entry.getValue();
                    try {
                        String currValue =
                                record.getField(meta.getFieldIndex(prop)) != null ? record.getField(
                                        meta.getFieldIndex(prop)).toString() : null;
                        Set<String> valueSet = valueMap.get(currValue);
                        if (valueSet != null && currValue != null) {
                            int i = 0;
                            for (String value : valueSet) {
                                if (i > 0) {
                                    // 如果一个节点有多个父亲，那么在算总的汇总值得时候，会有数据问题。
                                    if (StringUtils.isNotEmpty(value)) {
                                        SearchIndexResultRecord newRec = record;
                                        if (hasSameNodeCopy) {
                                            newRec = DeepcopyUtils.deepCopy(record);
                                        }
                                        newRec.setField(meta.getFieldIndex(prop), value);
                                        generateGroupBy(newRec, groupList, meta);
                                        if (hasSameNodeCopy) {
                                            copyLeafRecords.add(newRec);
                                        }
                                    }
                                } else {
                                    if (StringUtils.isNotEmpty(value)) {
                                        record.setField(meta.getFieldIndex(prop), value);
                                        generateGroupBy(record, groupList, meta);
                                        if (hasSameNodeCopy) {
                                            copyLeafRecords.add(record);
                                        }
                                    }
                                }
                                i++;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                // );
                if (!hasSameNodeCopy) {
                    copyLeafRecords.add(record);
                }
            }
            if (CollectionUtils.isNotEmpty(copyLeafRecords)) {
                // 处理汇总节点的时候，得进行下处理和过滤
                transList.addAll(copyLeafRecords);
            }
        }
        return transList;
    }

    /**
     * aggNodeOfAllLevel
     * 
     * @param transList transList
     * @param allDimVal allDimVal
     * @param root root
     * @param meta meta
     * @param hasSameNode hasSameNode
     * @param groupList groupList
     * @param dimSize dimSize
     * @param queryMeasures queryMeasures
     * @return transList
     * @throws NoSuchFieldException NoSuchFieldException
     */
    private static List<SearchIndexResultRecord> aggNodeOfAllLevel(List<SearchIndexResultRecord> transList,
            List<PullUpProperties> allDimVal, MemberNodeTree root, Meta meta, boolean hasSameNode,
            List<String> groupList, int dimSize, List<QueryMeasure> queryMeasures) throws NoSuchFieldException {
        if (CollectionUtils.isNotEmpty(allDimVal)) {
            for (PullUpProperties properties : allDimVal) {
                List<String> groupList0 = new ArrayList<>(groupList);
                if (StringUtils.isNotBlank(properties.childField)) {
                    groupList0.remove(properties.childField);
                }
                LinkedList<SearchIndexResultRecord> summaryCalcList = new LinkedList<SearchIndexResultRecord>();
                for (SearchIndexResultRecord record : transList) {
                    int index = meta.getFieldIndex(properties.pullupField);
                    String name = String.valueOf(record.getField(index));
                    if (hasSameNode) {
                        if (name.equals(root.getName())) {
                            summaryCalcList.add(record);
                            break;
                        }
                        continue;
                    }
                    SearchIndexResultRecord vRecord = DeepcopyUtils.deepCopy(record);
                    vRecord.setField(meta.getFieldIndex(properties.pullupField), properties.pullupValue);
                    generateGroupBy(vRecord, groupList0, meta);
                    summaryCalcList.add(vRecord);
                }
                if (!hasSameNode) {
                    transList.addAll(AggregateCompute.aggregate(summaryCalcList, dimSize, queryMeasures));
                }
            }
        }
        return transList;
    }

    private static boolean hasSameNode(MemberNodeTree member, List<MemberNodeTree> rowMemberTrees) {
        if (member != null && !member.isCallback ()) {
            return false;
        }
        List<MemberNodeTree> tmp = getLeafNodes(rowMemberTrees);
        if (CollectionUtils.isEmpty (tmp)) {
            return false;
        }
        String uinqueName = getRootUniqueName(rowMemberTrees);
        if (StringUtils.isNotEmpty (uinqueName) && uinqueName.equals (member.getUniqueName ())) {
            return false;
        }
        for (MemberNodeTree node : tmp) {
            if (!member.equals (node) 
                && CollectionUtils.isEqualCollection (node.getLeafIds (), member.getLeafIds ())) {
                return true;
            }
        }
        return false;
    }

    private static MemberNodeTree getRootMember(List<MemberNodeTree> rowMemberTrees) {
        MemberNodeTree tmp = rowMemberTrees.get (0);
        if (StringUtils.isNotEmpty (tmp.getUniqueName ())) {
            return tmp;
        }
        if (CollectionUtils.isNotEmpty (tmp.getChildren ())) {
            return getRootMember(tmp.getChildren ());
        }
        return null;
    }
    
    private static String getRootUniqueName(List<MemberNodeTree> rowMemberTrees) {
        MemberNodeTree tmp = rowMemberTrees.get (0);
        if (StringUtils.isNotEmpty (tmp.getUniqueName ())) {
            return tmp.getUniqueName ();
        }
        if (CollectionUtils.isNotEmpty (tmp.getChildren ())) {
            return getRootUniqueName(tmp.getChildren ());
        }
        return null;
    }

    private static List<MemberNodeTree> getLeafNodes(List<MemberNodeTree> rowMemberTrees) {
        List<MemberNodeTree> rs = Lists.newArrayList ();
        if (CollectionUtils.isEmpty (rowMemberTrees)) {
            return rs;
        }
        for (MemberNodeTree tree : rowMemberTrees) {
            rs.add(tree);
            if (CollectionUtils.isNotEmpty (tree.getChildren ())) {
                rs.addAll (getLeafNodes(tree.getChildren ()));
            }
        }
        return rs;
    }

    protected static MemberNodeTree getCurrentNode(String name, List<MemberNodeTree> nodeTrees) {
        MemberNodeTree rs = null;
        if (CollectionUtils.isEmpty (nodeTrees)) {
            return rs;
        }
        for (MemberNodeTree m : nodeTrees) {
            if (name.equals(m.getName ())) {
                return m;
            } else {
                if (CollectionUtils.isNotEmpty (m.getChildren ())) {
                    return getCurrentNode(name, m.getChildren ());
                }
            }
        }
        return rs;
    }
    
    protected static MemberNodeTree getSameNode(MemberNodeTree node, List<MemberNodeTree> nodeTrees) {
        if (CollectionUtils.isEmpty (nodeTrees)) {
            return node;
        }
        for (MemberNodeTree m : nodeTrees) {
            if (CollectionUtils.isEqualCollection (node.getLeafIds (), m.getLeafIds ())) {
                return m;
            }
        }
        return node;
    }
    
    protected static boolean isNeedSummary(PullUpProperties properties, List<MemberNodeTree> rowMemberTrees) {
        for (MemberNodeTree node : rowMemberTrees) {
            if (StringUtils.isEmpty(node.getName()) && CollectionUtils.isNotEmpty (node.getChildren())) {
                for (MemberNodeTree child : node.getChildren()) {
                    if (child.isCallback() && CollectionUtils.isNotEmpty (child.getSummaryIds())) {
                        for (MemberNodeTree tmp : child.getChildren()) {
                            if (CollectionUtils.isEqualCollection (child.getSummaryIds(), tmp.getLeafIds())) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void generateGroupBy(SearchIndexResultRecord record, 
        List<String> groups, Meta meta) throws NoSuchFieldException {
        if (CollectionUtils.isNotEmpty(groups)) {
            Serializable field = null;
            List<String> fields = new ArrayList<>();
            for (String name : meta.getFieldNameArray()) {
                if (groups.contains(name)) {
                    field = record.getField(meta.getFieldIndex(name));
                    if (field != null) {
                        fields.add(field.toString());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(fields)) {
                record.setGroupBy(StringUtils.join(fields, ","));
            }
        }
    }

}
