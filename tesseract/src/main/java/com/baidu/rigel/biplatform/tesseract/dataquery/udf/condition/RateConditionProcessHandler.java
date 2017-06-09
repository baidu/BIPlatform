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
package com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.google.common.collect.Lists;

/**
 * 
 * Description: AbsRateConditionProcessHandler
 * @author david.wang
 *
 */
abstract class RateConditionProcessHandler {
    /**
     * log
     */
    private static final Logger LOG = Logger.getLogger(RateConditionProcessHandler.class);
    
    /**
     * 
     * @param context 原始查询请求
     * @return QueryContext 经过处理之后的查询请求
     */
    public QueryContext processCondition(QueryContext context) {
        // 同比分子计算条件，此处默认不需要做任何处理
        // 环比分子查询条件处理，此处不需要做任何处理
        if (!(context instanceof QueryContextAdapter)) {
            throw new IllegalArgumentException("参数必须为QueryContextAdapter类型");
        }
        QueryContextAdapter adapter = (QueryContextAdapter) context;
        return adapter.getQueryContext();
    }
    
    /**
     * 
     * @param adapter
     * @return TimeDimension
     */
    TimeDimension getTimeDimension(QueryContextAdapter adapter) {
        Map<String, MetaCondition> queryConditions = adapter.getQuestionModel().getQueryConditions();
        final Map<String, Dimension> dims = adapter.getCube().getDimensions();
        Iterator<String> it = queryConditions.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (dims.containsKey(key) && dims.get(key) instanceof TimeDimension) {
                return (TimeDimension) dims.get(key);
            }
        }
        return null;
    }
    
    /**
     * 
     * @param dimension
     * @param adapter
     * @return Date
     * @throws MiniCubeQueryException 
     * @throws ParseException 
     */
    Date getFirstDayOfTimeDim(TimeDimension dimension, QueryContextAdapter adapter) 
            throws MiniCubeQueryException, ParseException {
        SimpleDateFormat format = new SimpleDateFormat(TimeRangeDetail.FORMAT_STRING);
        List<Member> members = getMembers(dimension, adapter);
        MetaCondition condition = adapter.getQuestionModel().getQueryConditions().get(dimension.getName());
        String value = null;
        if (condition != null) {
            DimensionCondition dimCondition = (DimensionCondition) condition;
            String uniqueName = dimCondition.getQueryDataNodes().get(0).getUniqueName();
            String[] tmp = MetaNameUtil.parseUnique2NameArray(uniqueName);
            value = tmp[tmp.length - 1];
        } else {
            // TODO 这里需要考虑一下，先排序，后处理
            value = members.get(0).getName();
        }
        return format.parse(value);
    }

    /**
     * @param dimension
     * @param adapter
     * @return List<Member>
     * @throws MiniCubeQueryException
     */
    private List<Member> getMembers(TimeDimension dimension,
            QueryContextAdapter adapter) throws MiniCubeQueryException {
        if (dimension.getLevels() == null || dimension.getLevels().size() == 0) {
            throw new IllegalArgumentException("非法参数，时间维度至少包含一个Level");
        }
        Level[] levels = dimension.getLevels().values().toArray(new Level[0]);
        List<Member> members = levels[0].getMembers(adapter.getCube(), 
                adapter.getDataSoruceInfo(), adapter.getQuestionModel().getRequestParams());
        return members;
    }
    
    /**
     * 
     * @param days
     * @param dimension
     * @param adapter
     * @return QueryContext
     */
    QueryContext createOrModifyNewContext(String[] days,
            TimeDimension dimension, QueryContextAdapter adapter) {
        QueryContext context = DeepcopyUtils.deepCopy(adapter.getQueryContext());
        QuestionModel questionModel = DeepcopyUtils.deepCopy(adapter.getQuestionModel());
        Cube cube = adapter.getCube();
//        Map<String, String> params = DeepcopyUtils.deepCopy(questionModel.getRequestParams());
        MetaCondition condition = questionModel.getQueryConditions().get(dimension.getName());
        if (condition == null || !(condition instanceof DimensionCondition)) {
            throw new IllegalStateException("未查到与时间维度相关的条件信息");
        }
        DimensionCondition tmp = (DimensionCondition) condition;
        List<QueryData> queryDatas = Lists.newArrayList();
//        int memberSize = -1;
//        try {
//            memberSize = getMembers(dimension, adapter).size();
//        } catch (MiniCubeQueryException e1) {
//            LOG.error(e1.getMessage(), e1);
//            throw new IllegalStateException("未能正确的获取当前时间维度的成员信息");
//        }
        final boolean hasQueryNodeDatas = CollectionUtils.isEmpty(tmp.getQueryDataNodes());
        boolean isExpand = hasQueryNodeDatas? false : tmp.getQueryDataNodes().get(0).isExpand();
        boolean isShow = hasQueryNodeDatas? false : tmp.getQueryDataNodes().get(0).isShow();
        for (int i = 0; i < days.length; ++i) {
            QueryData data = new QueryData("[" + dimension.getName() + "].[" + days[i] + "]");
            data.setExpand(isExpand);
            data.setShow(isShow);
            queryDatas.add(data);
        }
        tmp.setQueryDataNodes(queryDatas);
        questionModel.getQueryConditions().put(dimension.getName(), tmp);
        QueryContextBuilder builder = adapter.getBuilder();
        try {
            if (questionModel.getAxisMetas().get(AxisType.ROW).getCrossjoinDims().contains(dimension.getName())) {
                int index = questionModel.getAxisMetas()
                        .get(AxisType.ROW).getCrossjoinDims().indexOf(dimension.getName());
                MemberNodeTree tree = builder.buildQueryMemberTree(
                        adapter.getDataSoruceInfo(), cube, tmp, false, questionModel.getRequestParams());
                if (context.getRowMemberTrees () instanceof LinkedList) {
                    context.getRowMemberTrees().add (tree);
                } else {
                    context.getRowMemberTrees().set(index, tree);
                }
            } else {
                Map<String, Set<String>> fiters = builder.buildFilterCondition(
                        adapter.getDataSoruceInfo(), cube, tmp, questionModel.getRequestParams());
                context.getFilterMemberValues().putAll(fiters);
            }
            return context;
        } catch (MiniCubeQueryException | MetaException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
