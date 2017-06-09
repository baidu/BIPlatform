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
package com.baidu.rigel.biplatform.queryrouter.query.udf.condition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.service.DimensionMemberService;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.vo.MemberNodeTree;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContextAdapter;
import com.google.common.collect.Lists;

/**
 * 
 *Description: 日期偏移条件
 * @author david.wang
 *
 */
public class DateDataCondition implements Condition {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5833323348538008330L;

    /**
     * log
     */
    private static final Logger LOG = Logger.getLogger(DateDataCondition.class);
    
    /**
     *时间偏移步长 
     */
    private final int offset;
    
    /**
     * 指标名称
     */
    private final String variable;
    
    public DateDataCondition(String variable, int offset) {
        this.offset = offset;
        this.variable = variable;
    }
    
    @Override
    public ConditionType getConditionType() {
        return ConditionType.Other;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T processCondition(T context) {
        if (!(context instanceof QueryContextAdapter)) {
            throw new IllegalArgumentException("参数必须为QueryContextAdapter类型");
        }
        QueryContextAdapter adapter = (QueryContextAdapter) context;
        TimeDimension dimension = getTimeDimension(adapter);
        if (dimension == null) {
            throw new IllegalStateException("计算同环比必须包含时间维度，请确认查询结果");
        }
        TimeType timeType = dimension.getDataTimeType();
        Date firstDayOfTimeRange = null;
        try {
            firstDayOfTimeRange = getFirstDayOfTimeDim(dimension, adapter);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDayOfTimeRange);
//        cal.add(Calendar.YEAR, -1);
        TimeRangeDetail timeRange = null;
        switch (timeType) {
            case TimeDay:
                MetaCondition condition = adapter.getQuestionModel().getQueryConditions().get(dimension.getName());
                int size = 0;
                cal.add(Calendar.DAY_OF_YEAR, offset);
                if (condition instanceof DimensionCondition) {
                    DimensionCondition dimCondition = (DimensionCondition) condition;
                    size = dimCondition.getQueryDataNodes().size();
                }
                // 取昨天
                timeRange = TimeUtils.getDays(cal.getTime(), 0, size - 1);
                break;
            case TimeWeekly:
                // 星期需要特殊处理
                cal.add (Calendar.DAY_OF_YEAR, offset * 7);
                timeRange = TimeUtils.getWeekDays(cal.getTime());
                break;
            case TimeMonth:
                cal.add (Calendar.MONTH, offset);
                timeRange = TimeUtils.getMonthDays(cal.getTime());
                break;
            case TimeQuarter:
                cal.add (Calendar.MONTH, offset * 3);
                timeRange = TimeUtils.getQuarterDays(cal.getTime());
                break;
            case TimeYear:
                cal.add (Calendar.DAY_OF_YEAR, offset * 365);
                timeRange = TimeUtils.getYearDays(cal.getTime());
                break;
            default:
                throw new RuntimeException("未知的时间维度类型：" + timeType.name());
        }
       
        String[] days = timeRange.getDays();
        QueryContext rs = createOrModifyNewContext(days, dimension, adapter);
        return (T) rs;
    }
    
    /**
     * 
     * @param dimension
     * @param adapter
     * @return Date
     * @throws MiniCubeQueryException 
     * @throws ParseException 
     * @throws MetaException 
     */
    Date getFirstDayOfTimeDim(TimeDimension dimension, QueryContextAdapter adapter) 
            throws MiniCubeQueryException, ParseException, MetaException {
        SimpleDateFormat format = new SimpleDateFormat(TimeRangeDetail.FORMAT_STRING);
        List<MiniCubeMember> members = getMembers(dimension, adapter);
        MetaCondition condition = adapter.getQuestionModel().getQueryConditions().get(dimension.getName());
        String value = null;
        if (condition != null) {
            DimensionCondition dimCondition = (DimensionCondition) condition;
            String uniqueName = dimCondition.getQueryDataNodes().get(0).getUniqueName();
            String[] tmp = MetaNameUtil.parseUnique2NameArray(uniqueName);
            value = tmp[tmp.length - 1];
        } else {
            value = members.get(0).getName();
        }
        return format.parse(value);
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
        MetaCondition condition = questionModel.getQueryConditions().get(dimension.getName());
        if (condition == null || !(condition instanceof DimensionCondition)) {
            throw new IllegalStateException("未查到与时间维度相关的条件信息");
        }
        DimensionCondition tmp = (DimensionCondition) condition;
        List<QueryData> queryDatas = Lists.newArrayList();
        final boolean hasQueryNodeDatas = CollectionUtils.isEmpty(tmp.getQueryDataNodes());
        boolean isExpand = hasQueryNodeDatas ? false : tmp.getQueryDataNodes().get(0).isExpand();
        boolean isShow = hasQueryNodeDatas ? false : tmp.getQueryDataNodes().get(0).isShow();
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
     * @param dimension
     * @param adapter
     * @return List<Member>
     * @throws MiniCubeQueryException
     * @throws MetaException 
     */
    private List<MiniCubeMember> getMembers(TimeDimension dimension,
            QueryContextAdapter adapter) throws MiniCubeQueryException, MetaException {
        if (dimension.getLevels() == null || dimension.getLevels().size() == 0) {
            throw new IllegalArgumentException("非法参数，时间维度至少包含一个Level");
        }
        Level[] levels = dimension.getLevels().values().toArray(new Level[0]);
        
        return DimensionMemberService.getDimensionMemberServiceByLevelType(levels[0].getType()).getMembers(adapter.getCube(), levels[0],
        		adapter.getDataSoruceInfo(), null, adapter.getQuestionModel().getRequestParams());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
        result = prime * result + ((variable == null) ? 0 : variable.hashCode ());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass () != obj.getClass ()) {
            return false;
        }
        DateDataCondition other = (DateDataCondition) obj;
        if (offset != other.offset) {
            return false;
        }
        if (variable == null) {
            if (other.variable != null) {
                return false;
            }
        } else if (!variable.equals (other.variable)) {
            return false;
        }
        return true;
    }
    
    
}
