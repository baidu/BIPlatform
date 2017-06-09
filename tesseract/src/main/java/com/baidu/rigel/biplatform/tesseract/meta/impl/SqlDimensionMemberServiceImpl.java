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
package com.baidu.rigel.biplatform.tesseract.meta.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.SearchService;
import com.baidu.rigel.biplatform.tesseract.meta.DimensionMemberService;
import com.baidu.rigel.biplatform.tesseract.meta.MetaDataService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.Expression;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.From;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryObject;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.Where;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

/**
 * sql类型维度维值获取实现
 * 
 * @author xiaoming.chen
 *
 */
@Service(DimensionMemberService.SQL_MEMBER_SERVICE)
public class SqlDimensionMemberServiceImpl implements DimensionMemberService {

    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * searchService
     */
    @Resource
    private SearchService searchService;
    
    @Override
    public List<MiniCubeMember> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo, Member parentMember,
            Map<String, String> params) throws MiniCubeQueryException {
        long current = System.currentTimeMillis();
        if (cube == null || level == null || dataSourceInfo == null || !dataSourceInfo.validate()) {
            StringBuilder sb = new StringBuilder();
            sb.append("param illegal,cube:").append(cube).append(" level:").append(level).append(" datasourceInfo:")
                    .append(dataSourceInfo);
            log.error(sb.toString());
            throw new IllegalArgumentException(sb.toString());
        }
        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        // 发出的查询SQL类似 select name,caption from dim_table where pid = 1 and parentValue=0 group by name,caption
        QueryRequest nameQuery = buildQueryRequest(cube, queryLevel, parentMember, dataSourceInfo);
        if (isFromFactTable (cube, queryLevel)) {
            nameQuery.setDistinct (true);
        }
        String filterDimKey = params.get(QueryContextBuilder.FILTER_DIM_KEY);
        if (StringUtils.isNotEmpty(filterDimKey)) {
            nameQuery.setWhere(genWhere(nameQuery.getWhere(), filterDimKey, params));
        }
        List<Expression> whereCondition = genWhereCondition(cube, level, params, dataSourceInfo);
        if (!whereCondition.isEmpty ()) {
            for (Expression exp : whereCondition) {
                nameQuery.getWhere ().getAndList ().add (exp);
            }
        }
        nameQuery.setDataSourceInfo(dataSourceInfo);
        // 调用查询接口开始查询，查询返回一个resultSet
        List<MiniCubeMember> members = null;
        try {
            current = System.currentTimeMillis();
            SearchIndexResultSet resultSet = searchService.query(nameQuery);
            log.info("cost:{}ms in query request.level:{}",System.currentTimeMillis() - current,nameQuery);
            members = buildMembersFromCellSet(resultSet, queryLevel, parentMember, dataSourceInfo, cube);
        } catch (MiniCubeQueryException e) {
            log.error("get members error,queryLevel:" + queryLevel + " parentMember:" + parentMember, e);
            throw e;
        } catch (IndexAndSearchException e) {
            log.error("get members error,queryLevel:" + queryLevel + " parentMember:" + parentMember, e);
            throw new MiniCubeQueryException("get members error,queryLevel:" + queryLevel + " parentMember:"
                    + parentMember, e);
        }
        log.info("cost:{}ms in get members,size:{}",System.currentTimeMillis() - current, members.size());
        return members;

    }

    private boolean isFromFactTable(Cube cube, MiniCubeLevel queryLevel) {
        MiniCube miniCube = (MiniCube) cube;
        boolean dimTableNotBlank = StringUtils.isNotBlank (queryLevel.getDimTable ());
        // 退化维 这里需要增加distinct 设置
        final boolean isFromFactTable = dimTableNotBlank && queryLevel.getDimTable ().equals (miniCube.getSource ());
        return isFromFactTable;
    }

    private Where genWhere(Where where, String filterDimKey, Map<String, String> params) {
            List<Expression> andList = Lists.newArrayList();
            for (String key : filterDimKey.split(",")) {
                if (StringUtils.isEmpty(key)) {
                    continue;
                }
                if (StringUtils.isEmpty(params.get(key))) {
                    continue;
                }
                Set<QueryObject> value = Sets.newHashSet();
                for (String v : params.get(key).split(",")) {
                    QueryObject queryObj = new QueryObject(v, Sets.newHashSet());
                    value.add(queryObj);
                }
                Expression exp = new Expression(key, value);
                andList.add(exp);
            }
            if (!andList.isEmpty()) {
                if (where == null) {
                    where = new Where();
                }
                where.setAndList(andList);
            }
        return where;
    }

    /**
     * 将查询的结果集封装成member
     * 
     * @param resultSet
     * @param queryLevel
     * @param parentMember
     * @param dataSourceInfo
     * @param cube
     * @return List<MiniCubeMember>
     * @throws MiniCubeQueryException
     */
    private List<MiniCubeMember> buildMembersFromCellSet(SearchIndexResultSet resultSet, MiniCubeLevel queryLevel,
            Member parentMember, DataSourceInfo dataSourceInfo, Cube cube) throws MiniCubeQueryException {
        try {
            long current = System.currentTimeMillis();
            Map<String, MiniCubeMember> members = new TreeMap<String, MiniCubeMember>();
            while (resultSet.next()) {
            
                SearchIndexResultRecord record = resultSet.getCurrentRecord();
                String value = record.getField(resultSet.getMeta().getFieldIndex(queryLevel.getSource())).toString();
                if (StringUtils.isBlank(value)) {
                   // log.warn("can not get:" + queryLevel.getSource() + " from record:" + record);
                    continue;
                    // return;
                }
                MiniCubeMember member = members.get(value);
                if (member == null) {
                    member = new MiniCubeMember(value);
                    members.put(member.getName(), member);
                }
                member.setLevel(queryLevel);
                if (StringUtils.isNotBlank(queryLevel.getCaptionColumn())) {
                    member.setCaption(record.getField(resultSet.getMeta().getFieldIndex(queryLevel.getCaptionColumn())).toString());
                }
                member.setParent(parentMember);
                // 手动调用生成一下UniqueName，这时候生成代价最小
                member.generateUniqueName(null);
                // 需要查询Member对应的最细粒度节点，即与事实表关联的字段的外键
                if (StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                        && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
                    member.getQueryNodes().add(record.getField(resultSet.getMeta().getFieldIndex(queryLevel.getPrimaryKey())).toString());
                } else {
                    member.getQueryNodes().add(value);
                }
            }
            log.info("cost:{} in build dimension:{} member,size:{}",System.currentTimeMillis() - current , queryLevel.getDimension().getName(), members.size());
            return Lists.newArrayList(members.values());
        } catch (Exception e) {
            log.error("build members error:" + e.getMessage(), e);
            throw new MiniCubeQueryException(e.getMessage(), e);
        }
    }

    /**
     * @param cube
     * @param queryLevel
     * @param parentMember
     * @param dataSourceInfo
     * @return QueryRequest
     */
    protected QueryRequest buildQueryRequest(Cube cube, MiniCubeLevel queryLevel, Member parentMember,
            DataSourceInfo dataSourceInfo) {
        // 查询节点信息需要分2次查询，
        // 1.查询节点的ID和对应的显示名称（必须）
        // 2.查询节点对应事实表的字段查询的ID，如果该字段和本身的ID字段一致，可忽略
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setDataSourceInfo(dataSourceInfo);
        queryRequest.setCubeName(cube.getName());
        queryRequest.setCubeId(cube.getId());
        From from = new From(queryLevel.getDimTable());
        queryRequest.setFrom(from);
        if (StringUtils.isBlank(queryLevel.getDimTable())) {
            from.setFrom(((MiniCube) cube).getSource());
        }
        // 查询的ID字段，需要groupBy
        queryRequest.selectAndGroupBy(queryLevel.getSource());
        if(StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
            queryRequest.selectAndGroupBy(queryLevel.getPrimaryKey());
        }
        // 先把caption也进行groupby吧，要不一个ID对应多个名称不知道怎么取
        if (StringUtils.isNotBlank(queryLevel.getCaptionColumn())) {
            queryRequest.selectAndGroupBy(queryLevel.getCaptionColumn());
        }
        Where where = new Where();
        // 父节点不为空，且和当前查询level查的是同一个表，那么需要添加父节点的限制
        // 父子维度支持，暂时注释掉
//        if (queryLevel.isParentChildLevel()) {
//            Expression expression = null;
//            if (parentMember == null || parentMember.isAll()) {
//                expression = new Expression(queryLevel.getParent());
//                expression.getQueryValues().add(new QueryObject(queryLevel.getNullParentValue()));
//                // 判断2个level从同一个维度表获取
//            } else if (parentMember != null && parentMember.getLevel() != null
//                    && StringUtils.equals(parentMember.getLevel().getDimTable(), queryLevel.getDimTable())) {
//                if (parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM)) {
//                    throw new UnsupportedOperationException("no supported user custom group level");
//                    // 父节点的level和当前查询的level为同一个level，说明直接用parent=父节点的名称
//                } else if (StringUtils.equals(parentMember.getLevel().getName(), queryLevel.getName())) {
//                    expression = new Expression(queryLevel.getParent());
//                    expression.getQueryValues().add(new QueryObject(parentMember.getName()));
//                } else {
//                    // 先获取parentMember所属的Level
//                    if (parentMember.getLevel().getType().equals(LevelType.CALL_BACK)) {
//                        expression = new Expression(parentMember.getLevel().getPrimaryKey());
//                        // TODO 这里得修改下，如果是Callback的话，查询的应该是Member对应的leafID，后续sh
//                        QueryObject qo = new QueryObject(parentMember.getName(), ((MiniCubeMember)parentMember).getQueryNodes()); 
//                        expression.getQueryValues().add(qo);
//
//                    } else if (!parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM)) {
//                        MiniCubeLevel parentLevel = (MiniCubeLevel) parentMember.getLevel();
//                        expression = new Expression(parentLevel.getSource());
//                        expression.getQueryValues().add(new QueryObject(parentMember.getName()));
//                    }
//                    Expression parentExpression = new Expression(queryLevel.getParent());
//                    parentExpression.getQueryValues().add(new QueryObject(queryLevel.getNullParentValue()));
//                    where.getAndList().add(parentExpression);
//                }
//            }
//            if (expression != null) {
//                where.getAndList().add(expression);
//            }
//        } else {
        Expression expression = null;
        if (parentMember != null
                && !parentMember.isAll ()
                && parentMember.getLevel () != null
                && StringUtils.equals (parentMember.getLevel ().getDimTable (), queryLevel.getDimTable ())) {
            if (parentMember.getLevel ().getType ().equals (LevelType.CALL_BACK)) {
                expression = new Expression (parentMember.getLevel ().getPrimaryKey ());
                QueryObject qo = 
                    new QueryObject (parentMember.getName (), ((MiniCubeMember) parentMember).getQueryNodes ());
                expression.getQueryValues ().add (qo);
            } else if (!parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM))
             {
             MiniCubeLevel parentLevel = (MiniCubeLevel)
             parentMember.getLevel();
             expression = new Expression(parentLevel.getSource());
             expression.getQueryValues().add(new
             QueryObject(parentMember.getName()));
             }
        }
        
        if (expression != null) {
            where.getAndList ().add (expression);
        }
//        }
        queryRequest.setWhere(where);
        
        // 如果当前level 不是最后一层level，则在查询中增加下一层levle所在列 
        Dimension dim = queryLevel.getDimension ();
        List<Level> levels = Arrays.asList (dim.getLevels ().values ().toArray (new Level[0]));
        int index = levels.indexOf (queryLevel);
        if (index < levels.size () - 1) {
            Level nextLevel = levels.get (index + 1);
            if (queryLevel.getDimTable ().equals (nextLevel.getDimTable ())) {
                queryRequest.getSelect ().getQueryProperties ().add(0, nextLevel.getName ());
            }
        }
        return queryRequest;
    }

    @Override
    public MiniCubeMember getMemberFromLevelByName(DataSourceInfo dataSourceInfo, 
            Cube cube, Level level, String name,
            MiniCubeMember parent, Map<String, String> params) throws MiniCubeQueryException, MetaException {
        if (level == null || StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("level is null or name is blank");
        }
        MetaDataService.checkCube(cube);
        MetaDataService.checkDataSourceInfo(dataSourceInfo);

        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        QueryRequest queryRequest = buildQueryRequest(cube, queryLevel, parent, dataSourceInfo, params);
        Expression expression = new Expression(queryLevel.getSource());
        expression.getQueryValues().add(new QueryObject(name));
        queryRequest.getWhere().getAndList().add(expression);
        log.info("query members,queryRequest:" + queryRequest);
        MiniCubeMember result = new MiniCubeMember(name);
        result.setLevel(queryLevel);
        // 根据请求参数生产查询维度的约束条件
        List<Expression> whereCondition = genWhereCondition(cube, level, params, dataSourceInfo);
        if (!whereCondition.isEmpty ()) {
            for (Expression exp : whereCondition) {
                queryRequest.getWhere ().getAndList ().add (exp);
            }
        }

        try {
            // 这里的查询主要为了校验数据库是否存在，如果不存在抛异常，后续需要对这个加上配置处理。如果不存在可以不抛异常，直接跳过。。
            // 维度查询，直接采用distinct
            if (isFromFactTable (cube, queryLevel)) {
                queryRequest.setDistinct (true);
            }
            SearchIndexResultSet resultSet = searchService.query(queryRequest);
            List<MiniCubeMember> memberResultList = this.buildMembersFromCellSet (resultSet, queryLevel, parent, dataSourceInfo, cube);
            result = CollectionUtils.isEmpty (memberResultList) ? result : memberResultList.get(0);
        } catch (Exception e) {
            log.error("error occur when get name:" + name + " from level:" + level, e);
            throw new MiniCubeQueryException(e);
        }
        return result;
    }

    private List<Expression> genWhereCondition(Cube cube, Level level, Map<String, String> params, DataSourceInfo ds) {
        List<Expression> expressionList = Lists.newArrayList ();
        String dimTable = level.getDimTable ();
        Collection<Dimension> dims = cube.getDimensions ().values ();
        for (Dimension dim : dims) {
            // 过滤条件中包含当前维度表其他列的过滤条件，因此将过滤条件应用到当前维度成员查询上 此处暂时不考虑维度组
            String filterValue = params.get (dim.getId ());
            if (StringUtils.isBlank (filterValue)) {
                continue;
            }
            Level dimLevel = dim.getLevels ().values ().toArray (new Level[0])[0];
            boolean fromFactable = dimTable.equals (((MiniCube) cube).getSource());
            if ((fromFactable && !(dim instanceof TimeDimension)
                    && !dim.getId ().equals (level.getDimension ().getId ())) 
                    && (dimLevel.getType () != LevelType.CALL_BACK)) {
                Expression expression = new Expression(dimLevel.getFactTableColumn ());
                // filterValue 格式为{uniqueNameList } 此处需要解析filterValue生成QueryObject
                try {
                    String[] filterValueArray = genFilterValue(filterValue);
                    if (dimLevel.getDimTable ().equals (dimTable)) {
                        Set<String> leafNodes = Sets.newHashSet ();
                        for (String tmp : filterValueArray) {
                            leafNodes.add (tmp);
                        }
                        QueryObject queryObject = new QueryObject(null, leafNodes);
                        expression.getQueryValues ().add (queryObject);
                    } else {
                        for (String tmp :filterValueArray) {
                            MiniCubeMember member = 
                                    getMemberFromLevelByName (ds, cube, dimLevel, tmp, null, params);
                            Set<String> leafNodes = member.getQueryNodes ();
                            QueryObject queryObject = new QueryObject(null, leafNodes);
                            expression.getQueryValues ().add (queryObject);
                        }
                    }
                } catch (Exception e) {
                    log.error (e.getMessage (), e);
                    String[] filterValueArray = genFilterValue(filterValue);
                    for (String tmp :filterValueArray) {
                        Set<String> leafNodes = Sets.newHashSet ();
                        leafNodes.add (tmp);
                        QueryObject queryObject = new QueryObject(tmp, leafNodes);
                        expression.getQueryValues ().add (queryObject);
                    }
                }
                expressionList.add (expression);
            } else if (fromFactable && dimLevel.getType () == LevelType.CALL_BACK ) {
                List<Member> members = dimLevel.getMembers (cube, ds, params);
                if (CollectionUtils.isNotEmpty (members)) {
                    Set<String> leafNodes = ((MiniCubeMember) members.get (0)).getQueryNodes ();
                    Expression expression = new Expression(((CallbackLevel) dimLevel).getFactTableColumn ());
                    if (CollectionUtils.isEmpty (leafNodes)) {
                        leafNodes = Sets.newHashSet ();
                        leafNodes.add (members.get (0).getName ());
                    }
                    QueryObject queryObject = new QueryObject(members.get (0).getName (), leafNodes);
                    expression.getQueryValues ().add (queryObject);
                    expressionList.add (expression);
                }
            } else if (dim instanceof TimeDimension && dimTable.equals (((MiniCube) cube).getSource())) {
                // 此处只考虑了时间维度表和事实表同一张表情况，其他情况暂时不考虑
//                MiniCubeLevel dimLevel = (MiniCubeLevel) dim.getLevels ().values ().toArray (new Level[0])[0];
                Expression expression = new Expression(dimLevel.getFactTableColumn ());
                if (!filterValue.contains ("start") && !filterValue.contains ("end")) {
                    String[] tmp = filterValue.split (",");
                    for (String data : tmp) {
                        if (StringUtils.isNotEmpty (data) && MetaNameUtil.isUniqueName (data)) {
                            String[] valueArray = MetaNameUtil.parseUnique2NameArray (data);
                            Set<String> leafNodes = Sets.newHashSet ();
                            leafNodes.add (valueArray[valueArray.length - 1]);
                            QueryObject queryObject = new QueryObject(valueArray[valueArray.length - 1], leafNodes);
                            expression.getQueryValues ().add (queryObject);
                        }
                    }
                } else {
                    Map<String, String> filterMap = AnswerCoreConstant.GSON.fromJson (filterValue, 
                            new TypeToken<Map<String, String>>(){
                            }.getType());
                    TimeRangeDetail detail = new TimeRangeDetail(filterMap.get ("start"), filterMap.get ("end"));
                    String[] days = detail.getDays ();
                    
                    for (String day :days) {
                        Set<String> leafNodes = Sets.newHashSet ();
                        leafNodes.add (day);
                        QueryObject queryObject = new QueryObject(day, leafNodes);
                        expression.getQueryValues ().add (queryObject);
                    }
                }
                
                if (expression.getQueryValues ().size () > 0) {
                    expressionList.add (expression);
                }
            } else if ((dimTable.equals (dim.getTableName ()) 
                && !dim.getId ().equals (level.getDimension ().getId ()))) {
                Expression expression = new Expression(((MiniCubeLevel) dimLevel).getSource ());
                // filterValue 格式为{uniqueNameList } 此处需要解析filterValue生成QueryObject
                String[] filterValueArray = genFilterValue(filterValue);
                for (String tmp :filterValueArray) {
                    Set<String> leafNodes = Sets.newHashSet ();
                    leafNodes.add (tmp);
                    QueryObject queryObject = new QueryObject(tmp, leafNodes);
                    expression.getQueryValues ().add (queryObject);
                }
                expressionList.add (expression);
            }
        }
        return expressionList;
    }

    private String[] genFilterValue(String filterValue) {
        if (filterValue.contains ("{")) {
            filterValue = filterValue.substring (1, filterValue.length () - 1);
        }
        String[] uniqueNameList = filterValue.split (",");
        String[] rs = new String[uniqueNameList.length];
        String[] tmp = null;
        for (int index = 0; index < rs.length; ++index) {
            if (MetaNameUtil.isUniqueName (uniqueNameList[index])) {
                tmp = MetaNameUtil.parseUnique2NameArray (uniqueNameList[index]);
                rs[index] = tmp[tmp.length - 1];
            } else if(!StringUtils.isBlank(uniqueNameList[index]))  {
                rs[index] = uniqueNameList[index];
            }
        }
        return rs;
    }

    /**
     * @param cube
     * @param queryLevel
     * @param parentMember
     * @param dataSourceInfo
     * @return
     */
    private QueryRequest buildQueryRequest(Cube cube, MiniCubeLevel queryLevel, Member parentMember,
            DataSourceInfo dataSourceInfo, Map<String, String> params) {
        // 查询节点信息需要分2次查询，
        // 1.查询节点的ID和对应的显示名称（必须）
        // 2.查询节点对应事实表的字段查询的ID，如果该字段和本身的ID字段一致，可忽略
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setDataSourceInfo(dataSourceInfo);
        queryRequest.setCubeName(cube.getName());
        queryRequest.setCubeId(cube.getId());
        From from = new From(queryLevel.getDimTable());
        queryRequest.setFrom(from);
        if (StringUtils.isBlank(queryLevel.getDimTable())) {
            from.setFrom(((MiniCube) cube).getSource());
        }
        // 查询的ID字段，需要groupBy
        queryRequest.selectAndGroupBy(queryLevel.getSource());
        if(StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
            queryRequest.selectAndGroupBy(queryLevel.getPrimaryKey());
        }
        // 先把caption也进行groupby吧，要不一个ID对应多个名称不知道怎么取
        if (StringUtils.isNotBlank(queryLevel.getCaptionColumn())) {
            queryRequest.selectAndGroupBy(queryLevel.getCaptionColumn());
        }
        // 父节点不为空，且和当前查询level查的是同一个表，那么需要添加父节点的限制
        Where where = new Where();
//        if (queryLevel.isParentChildLevel()) {
//            Expression expression = null;
//            if (parentMember == null || parentMember.isAll()) {
//                expression = new Expression(queryLevel.getParent());
//                expression.getQueryValues().add(new QueryObject(queryLevel.getNullParentValue()));
//                // 判断2个level从同一个维度表获取
//            } else if (parentMember != null && parentMember.getLevel() != null
//                    && StringUtils.equals(parentMember.getLevel().getDimTable(), queryLevel.getDimTable())) {
//                if (parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM)) {
//                    throw new UnsupportedOperationException("no supported user custom group level");
//                    // 父节点的level和当前查询的level为同一个level，说明直接用parent=父节点的名称
//                } else if (StringUtils.equals(parentMember.getLevel().getName(), queryLevel.getName())) {
//                    expression = new Expression(queryLevel.getParent());
//                    expression.getQueryValues().add(new QueryObject(parentMember.getName()));
//                } else {
//                    // 先获取parentMember所属的Level
//                    if (parentMember.getLevel().getType().equals(LevelType.CALL_BACK)) {
//                        expression = new Expression(parentMember.getLevel().getPrimaryKey());
//                        QueryObject qo = new QueryObject(parentMember.getName(), ((MiniCubeMember)parentMember).getQueryNodes()); 
//                        expression.getQueryValues().add(qo);
//
//                    } else if (!parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM)) {
//                        MiniCubeLevel parentLevel = (MiniCubeLevel) parentMember.getLevel();
//                        expression = new Expression(parentLevel.getSource());
//                        expression.getQueryValues().add(new QueryObject(parentMember.getName()));
//                    }
//                    Expression parentExpression = new Expression(queryLevel.getParent());
//                    parentExpression.getQueryValues().add(new QueryObject(queryLevel.getNullParentValue()));
//                    where.getAndList().add(parentExpression);
//                }
//            }
//            if (expression != null) {
//                where.getAndList().add(expression);
//            }
//        } else {
        Expression expression = null;
        if (parentMember != null && !parentMember.isAll() && parentMember.getLevel() != null
                && StringUtils.equals(parentMember.getLevel().getDimTable(), queryLevel.getDimTable())) {
            if (parentMember.getLevel().getType().equals(LevelType.CALL_BACK)) {
                expression = new Expression(parentMember.getLevel().getPrimaryKey());
                QueryObject qo = new QueryObject(parentMember.getName(), ((MiniCubeMember)parentMember).getQueryNodes()); 
                expression.getQueryValues().add(qo);
            } else if (!parentMember.getLevel().getType().equals(LevelType.USER_CUSTOM)) {
                MiniCubeLevel parentLevel = (MiniCubeLevel) parentMember.getLevel();
                expression = new Expression(parentLevel.getSource());
                expression.getQueryValues().add(new QueryObject(parentMember.getName()));
            }
        }

        if (expression != null) {
            where.getAndList().add(expression);
        }
            
//        }
        queryRequest.setWhere(where);

        return queryRequest;
    }

    @Override
    public List<MiniCubeMember> getMemberFromLevelByNames(DataSourceInfo dataSourceInfo,
        Cube cube, Level level, Map<String, String> params, List<String> uniqueNameList) {
        if (level == null || CollectionUtils.isEmpty (uniqueNameList)) {
            throw new IllegalArgumentException("level is null or name is blank");
        }
        try {
            MetaDataService.checkCube(cube);
        } catch (MetaException e1) {
            throw new RuntimeException (e1);
        }
        MetaDataService.checkDataSourceInfo(dataSourceInfo);

        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        QueryRequest queryRequest = buildQueryRequest(cube, queryLevel, null, dataSourceInfo, params);
        Expression expression = new Expression(queryLevel.getSource());
        for (String uniqueName : uniqueNameList) {
            String[] tmp = MetaNameUtil.parseUnique2NameArray (uniqueName);
            expression.getQueryValues().add(new QueryObject(tmp[tmp.length - 1]));
        }
        queryRequest.getWhere().getAndList().add(expression);
        log.info("query members,queryRequest:" + queryRequest);
        List<Expression> whereCondition = genWhereCondition(cube, level, params, dataSourceInfo);
        if (!whereCondition.isEmpty ()) {
            for (Expression exp : whereCondition) {
                queryRequest.getWhere ().getAndList ().add (exp);
            }
        }

        List<MiniCubeMember> members = Lists.newArrayList ();
        try {
            // 这里的查询主要为了校验数据库是否存在，如果不存在抛异常，后续需要对这个加上配置处理。如果不存在可以不抛异常，直接跳过。。
            // 维度查询，直接采用distinct count
            if (isFromFactTable (cube, queryLevel)) {
                queryRequest.setDistinct (true);
            }
            SearchIndexResultSet resultSet = searchService.query(queryRequest);
            members = 
                buildMembersFromCellSet (resultSet, queryLevel, null, dataSourceInfo, cube);
        } catch (Exception e) {
            log.error("error occur when get name:" + uniqueNameList + " from level:" + level, e);
            throw new MiniCubeQueryException(e);
        }
        return members;
    }
    
    protected void setSearchService(SearchService service) {
        this.searchService = service;
    }
    
}
