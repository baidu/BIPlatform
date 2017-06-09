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
package com.baidu.rigel.biplatform.queryrouter.query.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.handle.model.QueryHandler;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.service.DimensionMemberService;
import com.baidu.rigel.biplatform.queryrouter.query.service.MetaDataService;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryObject;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultSet;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Expression;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.From;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Where;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.SqlColumnUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.WhereDataUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.utils.QueryHandlerBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

/**
 * sql类型维度维值获取实现
 * 
 * @author luowenlei
 *
 */
@Service(DimensionMemberService.SQL_MEMBER_SERVICE)
public class SqlDimensionMemberServiceImpl implements DimensionMemberService {
    
    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public List<MiniCubeMember> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo,
            Member parentMember, Map<String, String> params) throws MiniCubeQueryException {
        if (cube == null || level == null || dataSourceInfo == null || !dataSourceInfo.validate()) {
            StringBuilder sb = new StringBuilder();
            sb.append("param illegal,cube:").append(cube).append(" level:").append(level)
                    .append(" datasourceInfo:").append(dataSourceInfo);
            log.error(sb.toString());
            throw new IllegalArgumentException(sb.toString());
        }
        QueryHandler queryHandler = QueryHandlerBuilder.buildQueryHandler(cube, dataSourceInfo);
        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        String groupDimkey = this.getGroupDimKey(queryLevel);
        if (!StringUtils.isEmpty(groupDimkey)
                && LevelTypeContants.PARAM_LEVEL_ALL.equals(params
                        .get(LevelTypeContants.PARAM_LEVEL))) {
            // 如果是维度组的查询
            return this.getGroupAllLvlMembers(cube, queryLevel, dataSourceInfo, parentMember,
                    params, queryHandler);
        } else {
            // 如果单独维度查询
            List<MiniCubeLevel> levels = Lists.newArrayList();
            levels.add((MiniCubeLevel) level);
            List<Map<String, Object>> result = this.getOneLvlMembers(cube, levels, dataSourceInfo,
                    parentMember, params, queryHandler);
            return buildMembersFromCellSet(result, queryLevel, parentMember, dataSourceInfo,
                    queryHandler.getSqlExpression().getQueryMeta(), false);
        }
    }
    
    /**
     * getGroupAllLvlMembers
     *
     * @param cube
     * @param queryLevel
     * @param dataSourceInfo
     * @param parentMember
     * @param params
     * @param queryHandler
     * @return
     */
    private List<MiniCubeMember> getGroupAllLvlMembers(Cube cube, MiniCubeLevel queryLevel,
            DataSourceInfo dataSourceInfo, Member parentMember, Map<String, String> params,
            QueryHandler queryHandler) {
        Map<String, List<Map<String, Object>>> groupMembersResult = this.buildGroupResult(cube,
                queryLevel, dataSourceInfo, parentMember, params, queryHandler);
        // 存放没有层级关系的members,外面的list的一个节点为一个level的层级member，里面的list节点为一个member的节点
        List<List<MiniCubeMember>> allMemberList = Lists.newArrayList();
        boolean isQueryLevel = false;
        for (Level level : queryLevel.getDimension().getLevels().values()) {
            MiniCubeLevel currentLevel = (MiniCubeLevel) level;
            if ((queryLevel.getDimTable().equals(currentLevel.getDimTable()) && queryLevel
                    .getSource().equals(currentLevel.getSource())) || isQueryLevel) {
                // 如何level相等，那么存放下面的level信息
                isQueryLevel = true;
                // 这里存放所有level每级的Member信息
                allMemberList.add(buildMembersFromCellSet(
                        groupMembersResult.get(currentLevel.getDimTable()), currentLevel, null,
                        dataSourceInfo, queryHandler.getSqlExpression().getQueryMeta(), true));
            }
        }
        try {
            return buildLevelRelation(allMemberList, parentMember);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * addRelation,建立关系
     *
     */
    private void addRelation(MiniCubeMember parent, MiniCubeMember children) {
        children.setParent(parent);
        children.generateUniqueName(null);
        if (parent.getChildren() == null) {
            List<MiniCubeMember> c = Lists.newArrayList();
            c.add(children);
            parent.setChildren(c);
        } else if (!parent.getChildren().contains(children)) {
            parent.getChildren().add(children);
        }
    }
    
    /**
     * buildLevelRelation,将allMemberList建立父级存级关系，设置parent层级
     *
     * @param allMemberList
     * @return
     * @throws Exception
     */
    private List<MiniCubeMember> buildLevelRelation(List<List<MiniCubeMember>> allMemberList,
            Member parentMember) throws Exception {
        if (CollectionUtils.isEmpty(allMemberList)) {
            return Lists.newArrayList();
        }
        MiniCubeMember parentMiniCubeMember = (MiniCubeMember) parentMember;
        List<MiniCubeMember> previousLevelMembersReal = null;
        for (int levelIndex = 0; levelIndex < allMemberList.size(); levelIndex++) {
            List<MiniCubeMember> currentLvlMembersReal = allMemberList.get(levelIndex);
            if (levelIndex == 0) {
                if (parentMember != null) {
                    // 设置parent
                    for (MiniCubeMember currentLvlMember : currentLvlMembersReal) {
                        this.addRelation(parentMiniCubeMember, currentLvlMember);
                        currentLvlMember.generateUniqueName(null);
                    }
                }
            } else if (previousLevelMembersReal != null
                    && StringUtils.isEmpty(currentLvlMembersReal.iterator().next()
                            .getParentMemberName())) {
                currentLvlMembersReal = Lists.newArrayList();
                // 如果ParentMemberName为all的情况
                for (MiniCubeMember previousLevelMember : previousLevelMembersReal) {
                    List<MiniCubeMember> cpCurrentLvlMembers = DeepcopyUtils.deepCopy(allMemberList
                            .get(levelIndex));
                    currentLvlMembersReal.addAll(cpCurrentLvlMembers);
                    for (MiniCubeMember cpCurrentLvlMember : cpCurrentLvlMembers) {
                        this.addRelation(previousLevelMember, cpCurrentLvlMember);
                    }
                }
            } else {
                // 处理能查到父节点的情况,此时没有all的情况，所以currentLvlMembersReal节点的个数为Template的个数
                this.buildOneLevelRelation(currentLvlMembersReal, previousLevelMembersReal);
            }
            previousLevelMembersReal = currentLvlMembersReal;
        }
        return allMemberList.get(0);
    }
    
    /**
     * buildOneLevelParentRelation,此方法不考录第一级的处理问题
     *
     * @param oneLevelMembers
     * @param previousLevelMembers
     */
    private void buildOneLevelRelation(List<MiniCubeMember> oneLevelMembers,
            List<MiniCubeMember> previousLevelMembers) {
        for (MiniCubeMember member : oneLevelMembers) {
            // 从第二级开始，需要找到父的Member
            for (MiniCubeMember previousLevelMember : previousLevelMembers) {
                if (MetaNameUtil.getNameFromMetaName(previousLevelMember.getUniqueName()).equals(
                        member.getParentMemberName())) {
                    member.setParent(previousLevelMember);
                    this.addRelation(previousLevelMember, member);
                    break;
                }
            }
        }
    }
    
    /**
     * getOneLvlMembers
     *
     * @param cube
     * @param level
     * @param dataSourceInfo
     * @param parentMember
     * @param params
     * @return
     */
    private List<Map<String, Object>> getOneLvlMembers(Cube cube, List<MiniCubeLevel> levels,
            DataSourceInfo dataSourceInfo, Member parentMember, Map<String, String> params,
            QueryHandler queryHandler) {
        long current = System.currentTimeMillis();
        MiniCubeLevel queryLevel = (MiniCubeLevel) levels.iterator().next();
        // 发出的查询SQL类似 select name,caption from dim_table where pid = 1 and
        // parentValue=0 group by name,caption
        QueryRequest queryRequest = buildQueryRequest(cube, levels, parentMember, dataSourceInfo);
        if (isFromFactTable(cube, queryLevel)) {
            queryRequest.setDistinct(true);
        } else {
            // 如果不是退化维，则设置id信息
            queryRequest.selectAndGroupBy(queryLevel.getPrimaryKey());
        }
        String filterDimKey = params.get(QueryContextBuilder.FILTER_DIM_KEY);
        if (StringUtils.isNotEmpty(filterDimKey)) {
            queryRequest.setWhere(genWhere(queryRequest.getWhere(), filterDimKey, params));
        }
        List<Expression> whereCondition = genWhereCondition(cube, queryLevel, params,
                dataSourceInfo);
        if (!whereCondition.isEmpty()) {
            for (Expression exp : whereCondition) {
                queryRequest.getWhere().getAndList().add(exp);
            }
        }
        queryRequest.setDataSourceInfo(dataSourceInfo);
        List<Map<String, Object>> result = Lists.newArrayList();
        // 调用查询接口开始查询，查询返回一个resultSet
        try {
            current = System.currentTimeMillis();
            SqlExpression sqlExpression = queryHandler.getSqlExpression();
            
            sqlExpression.setNeedColums(SqlColumnUtils.getFacttableColumns(sqlExpression
                    .getQueryMeta(), queryRequest.getSelect(), queryRequest.getFrom().getFrom(),
                    false, false));
            String tableName = sqlExpression.getNeedColums().iterator().next().getTableName();
            Map<String, List<Object>> andCondition = WhereDataUtils
                    .transQueryRequestAndWhereList2Map(queryRequest, tableName, queryHandler
                            .getSqlExpression().getQueryMeta());
            sqlExpression.setTableName(queryRequest.getFrom().getFrom());
            sqlExpression.getSqlQuery().getSelect().setDistinct(queryRequest.isDistinct());
            sqlExpression.generateNoJoinSql(sqlExpression.getNeedColums(), null, null,
                    andCondition, false);
            result = queryHandler.getJdbcHandler().queryForList(
                    queryHandler.getSqlExpression().getSqlQuery().toPrintSql(),
                    Lists.newArrayList());
            log.info("queryId:{} cost:{} ms in query request.level:{}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - current,
                    queryRequest);
            
        } catch (MiniCubeQueryException e) {
            log.error("queryId:{} get members error,queryLevel:{} parentMember:{}",
                    QueryRouterContext.getQueryId(), queryLevel, parentMember);
            throw e;
        } catch (Exception e) {
            log.error("queryId:{} get members error,queryLevel:" + queryLevel + " parentMember:"
                    + parentMember, QueryRouterContext.getQueryId());
            throw new MiniCubeQueryException("get members error,queryLevel:" + queryLevel
                    + " parentMember:" + parentMember, e);
        }
        log.info("queryId:{} cost:{}ms in get members,size:{}", QueryRouterContext.getQueryId(),
                System.currentTimeMillis() - current, result.size());
        return result;
    }
    
    /**
     * buildGroupResult
     *
     * @param cube
     * @param queryLevel
     * @param dataSourceInfo
     * @param parentMember
     * @param params
     * @param queryHandler
     * @return
     */
    private Map<String, List<Map<String, Object>>> buildGroupResult(Cube cube,
            MiniCubeLevel queryLevel, DataSourceInfo dataSourceInfo, Member parentMember,
            Map<String, String> params, QueryHandler queryHandler) {
        Map<String, List<Map<String, Object>>> result = Maps.newConcurrentMap();
        String groupDimkey = this.getGroupDimKey(queryLevel);
        if (!StringUtils.isEmpty(groupDimkey)) {
            // 查询为需要级联结果的查询,即维度组
            Collection<Level> set = queryLevel.getDimension().getLevels().values();
            Map<String, List<MiniCubeLevel>> levels = Maps.newConcurrentMap();
            // 组织level组，可以为tablename，一样的tablename为一个组
            for (Level level : set) {
                if (levels.get(level.getDimTable()) == null) {
                    levels.put(level.getDimTable(), Lists.newArrayList());
                }
                levels.get(level.getDimTable()).add((MiniCubeLevel) level);
            }
            for (Entry<String, List<MiniCubeLevel>> oneTableLevels : levels.entrySet()) {
                result.put(oneTableLevels.getKey(), this.getOneLvlMembers(cube,
                        oneTableLevels.getValue(), dataSourceInfo, parentMember, params,
                        queryHandler));
            }
        }
        return result;
    }
    
    private boolean isFromFactTable(Cube cube, MiniCubeLevel queryLevel) {
        MiniCube miniCube = (MiniCube) cube;
        boolean dimTableNotBlank = StringUtils.isNotBlank(queryLevel.getDimTable());
        // 退化维 这里需要增加distinct 设置
        final boolean isFromFactTable = dimTableNotBlank
                && queryLevel.getDimTable().equals(miniCube.getSource());
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
     * getGroupDimKey
     *
     * @param queryLevel
     * @return
     */
    private String getGroupDimKey(MiniCubeLevel queryLevel) {
        if (queryLevel.getDimension().getType() != DimensionType.GROUP_DIMENSION) {
            return "";
        }
        return "[Dimension].[" + queryLevel.getDimension().getName() + "]";
    }
    
    /**
     * getPreviousLevel,获取上一级的level,如果没有上一级返回null
     *
     * @param level
     * @return
     */
    private MiniCubeLevel getPreviousLevel(MiniCubeLevel level) {
        MiniCubeLevel previousLevel = null;
        for (Level currentLevel : level.getDimension().getLevels().values()) {
            MiniCubeLevel currentMiniCubeLevel = (MiniCubeLevel) currentLevel;
            if (level.getDimTable().equals(currentLevel.getDimTable())
                    && level.getSource().equals(currentMiniCubeLevel.getSource())) {
                return previousLevel;
            }
            previousLevel = currentMiniCubeLevel;
        }
        return null;
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
    private List<MiniCubeMember> buildMembersFromCellSet(List<Map<String, Object>> result,
            MiniCubeLevel queryLevel, Member parentMember, DataSourceInfo dataSourceInfo,
            QueryMeta queryMeta, boolean isBuildParentValue) throws MiniCubeQueryException {
        try {
            // 判断如果是获取当前level，并且parentMember为当前level的某值，并不是allmembers则返回自己
            if (parentMember != null
                    && parentMember.getLevel().equals(queryLevel)
                    && !MetaNameUtil.isLastAllMemberUniqueName(parentMember.getUniqueName())) {
                List<MiniCubeMember> list = Lists.newArrayList();
                list.add((MiniCubeMember) parentMember);
                return list;
            }
            long current = System.currentTimeMillis();
            Map<String, MiniCubeMember> members = new TreeMap<String, MiniCubeMember>();
            SqlColumn queryLevelSqlColumn = queryMeta.getSqlColumn(queryLevel.getDimTable(),
                    queryLevel.getSource());
            // 获取上一级的level
            MiniCubeLevel previousLevel = getPreviousLevel(queryLevel);
            SqlColumn previousLevelSqlColumn = null;
            if (previousLevel != null) {
                // 此时为单个维度或维度组为第一个节点的情况
                previousLevelSqlColumn = queryMeta.getSqlColumn(previousLevel.getDimTable(),
                        previousLevel.getSource());
            }
            for (Map<String, Object> row : result) {
                String value = row.get(queryLevelSqlColumn.getName()).toString();
                String previousValue = "";
                if (previousLevelSqlColumn != null
                        && previousLevel.getDimTable().equals(queryLevel.getDimTable())
                        && isBuildParentValue) {
                    previousValue = row.get(previousLevelSqlColumn.getName()).toString();
                }
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                MiniCubeMember member = members.get(value);
                if (member == null) {
                    member = new MiniCubeMember(value);
                    members.put(member.getName(), member);
                }
                // 设置ID信息
                if (queryLevelSqlColumn.getJoinTable() != null
                        && !CollectionUtils.isEmpty(queryLevelSqlColumn.getJoinTable().getJoinOnList())
                        && !StringUtils.isEmpty(queryLevelSqlColumn.getJoinTable().getJoinOnList().get(0)
                                .getJoinTableFieldName())) {
                    String dimPk = queryLevelSqlColumn.getJoinTable().getJoinOnList().get(0)
                            .getJoinTableFieldName();
                    if (row.get(dimPk) != null && !StringUtils.isEmpty(row.get(dimPk).toString())) {
                        member.setId(row.get(dimPk).toString());
                    }
                }
                
                member.setParentMemberName(previousValue);
                member.setLevel(queryLevel);
                if (StringUtils.isNotBlank(queryLevel.getCaptionColumn())) {
                    member.setCaption(queryLevelSqlColumn.getCaption());
                }
                member.setParent(parentMember);
                // 手动调用生成一下UniqueName，这时候生成代价最小
                member.generateUniqueName(null);
                // 需要查询Member对应的最细粒度节点，即与事实表关联的字段的外键
                if (StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                        && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
                    SqlColumn sqlColumnPk = queryMeta.getSqlColumn(queryLevel.getDimTable(),
                            queryLevel.getPrimaryKey());
                    member.getQueryNodes().add(row.get(sqlColumnPk.getName()).toString());
                } else {
                    member.getQueryNodes().add(value);
                }
            }
            log.info("queryId:{} cost:{} in build dimension:{} member,size:{}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - current,
                    queryLevel.getDimension().getName(), members.size());
            return Lists.newArrayList(members.values());
        } catch (Exception e) {
            log.error("queryId{} build members error:" + e.getMessage(),
                    QueryRouterContext.getQueryId());
            throw new MiniCubeQueryException(e.getMessage(), e);
        }
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
    private List<MiniCubeMember> buildMembersFromCellSet(SearchIndexResultSet resultSet,
            MiniCubeLevel queryLevel, Member parentMember, DataSourceInfo dataSourceInfo, Cube cube)
            throws MiniCubeQueryException {
        try {
            long current = System.currentTimeMillis();
            Map<String, MiniCubeMember> members = new TreeMap<String, MiniCubeMember>();
            
            while (resultSet.next()) {
                
                SearchIndexResultRecord record = resultSet.getCurrentRecord();
                String value = record.getField(
                        resultSet.getMeta().getFieldIndex(queryLevel.getSource())).toString();
                if (StringUtils.isBlank(value)) {
                    // log.warn("can not get:" + queryLevel.getSource() +
                    // " from record:" + record);
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
                    member.setCaption(record.getField(
                            resultSet.getMeta().getFieldIndex(queryLevel.getCaptionColumn()))
                            .toString());
                }
                member.setParent(parentMember);
                // 手动调用生成一下UniqueName，这时候生成代价最小
                member.generateUniqueName(null);
                // 需要查询Member对应的最细粒度节点，即与事实表关联的字段的外键
                if (StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                        && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
                    member.getQueryNodes().add(
                            record.getField(
                                    resultSet.getMeta().getFieldIndex(queryLevel.getPrimaryKey()))
                                    .toString());
                } else {
                    member.getQueryNodes().add(value);
                }
            }
            log.info("queryId:{} cost:{} in build dimension:{} member,size:{}",
                    QueryRouterContext.getQueryId(), System.currentTimeMillis() - current,
                    queryLevel.getDimension().getName(), members.size());
            return Lists.newArrayList(members.values());
        } catch (Exception e) {
            log.error("queryId{} build members error:" + e.getMessage(),
                    QueryRouterContext.getQueryId());
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
    protected QueryRequest buildQueryRequest(Cube cube, List<MiniCubeLevel> queryLevels,
            Member parentMember, DataSourceInfo dataSourceInfo) {
        // 查询节点信息需要分2次查询，
        // 1.查询节点的ID和对应的显示名称（必须）
        // 2.查询节点对应事实表的字段查询的ID，如果该字段和本身的ID字段一致，可忽略
        MiniCubeLevel queryLevel = queryLevels.iterator().next();
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
        for (MiniCubeLevel level : queryLevels) {
            queryRequest.selectAndGroupBy(level.getSource());
        }
        if (StringUtils.isNotBlank(queryLevel.getPrimaryKey())
                && !StringUtils.equals(queryLevel.getSource(), queryLevel.getPrimaryKey())) {
            queryRequest.selectAndGroupBy(queryLevel.getPrimaryKey());
        }
        // 先把caption也进行groupby吧，要不一个ID对应多个名称不知道怎么取
        if (StringUtils.isNotBlank(queryLevel.getCaptionColumn())) {
            queryRequest.selectAndGroupBy(queryLevel.getCaptionColumn());
        }
        Where where = new Where();
        Expression expression = null;
        if (parentMember != null
                && !parentMember.isAll()
                && parentMember.getLevel() != null
                && StringUtils.equals(parentMember.getLevel().getDimTable(),
                        queryLevel.getDimTable())) {
            if (parentMember.getLevel().getType().equals(LevelType.CALL_BACK)) {
                expression = new Expression(parentMember.getLevel().getPrimaryKey());
                QueryObject qo = new QueryObject(parentMember.getName(),
                        ((MiniCubeMember) parentMember).getQueryNodes());
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
        // }
        queryRequest.setWhere(where);
        
        // 如果当前level 不是最后一层level，则在查询中增加下一层levle所在列
        Dimension dim = queryLevel.getDimension();
        List<Level> levels = Arrays.asList(dim.getLevels().values().toArray(new Level[0]));
        int index = levels.indexOf(queryLevel);
        if (index < levels.size() - 1) {
            Level nextLevel = levels.get(index + 1);
            if (queryLevel.getDimTable().equals(nextLevel.getDimTable())) {
                queryRequest.getSelect().getQueryProperties().add(0, nextLevel.getName());
            }
        }
        return queryRequest;
    }

    @Override
    public MiniCubeMember getMemberFromLevelByName(DataSourceInfo dataSourceInfo, Cube cube,
            Level level, String name, MiniCubeMember parent, Map<String, String> params)
            throws MiniCubeQueryException, MetaException {
        if (level == null || StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("level is null or name is blank");
        }
        MetaDataService.checkCube(cube);
        MetaDataService.checkDataSourceInfo(dataSourceInfo);
        
        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        List<MiniCubeLevel> list = Lists.newArrayList();
        list.add(queryLevel);
        QueryRequest queryRequest = buildQueryRequest(cube, list, parent, dataSourceInfo);
        Expression expression = new Expression(queryLevel.getSource());
        expression.getQueryValues().add(new QueryObject(name));
        queryRequest.getWhere().getAndList().add(expression);
        log.info("queryId:{} query members,queryRequest:" + queryRequest,
                QueryRouterContext.getQueryId());
        MiniCubeMember result = new MiniCubeMember(name);
        result.setLevel(queryLevel);
        // 根据请求参数生产查询维度的约束条件
        List<Expression> whereCondition = genWhereCondition(cube, level, params, dataSourceInfo);
        if (!whereCondition.isEmpty()) {
            for (Expression exp : whereCondition) {
                queryRequest.getWhere().getAndList().add(exp);
            }
        }
        
        try {
            // 这里的查询主要为了校验数据库是否存在，如果不存在抛异常，后续需要对这个加上配置处理。如果不存在可以不抛异常，直接跳过。。
            // 维度查询，直接采用distinct
            if (isFromFactTable(cube, queryLevel)) {
                queryRequest.setDistinct(true);
            }
            
            QueryHandler queryHandler = QueryHandlerBuilder.buildQueryHandler(cube, dataSourceInfo);
            SqlExpression sqlExpression = queryHandler.getSqlExpression();
            String tableName = queryRequest.getFrom().getFrom();
            sqlExpression
                    .setNeedColums(SqlColumnUtils.getFacttableColumns(sqlExpression.getQueryMeta(),
                            queryRequest.getSelect(), tableName, false, false));
            
            Map<String, List<Object>> andCondition = WhereDataUtils
                    .transQueryRequestAndWhereList2Map(queryRequest, tableName, queryHandler
                            .getSqlExpression().getQueryMeta());
            sqlExpression.setTableName(queryRequest.getFrom().getFrom());
            sqlExpression.getSqlQuery().getSelect().setDistinct(queryRequest.isDistinct());
            sqlExpression.generateNoJoinSql(sqlExpression.getNeedColums(), null, null,
                    andCondition, false);
            PageInfo pageInfo = new PageInfo();
            pageInfo.setCurrentPage(0);
            pageInfo.setPageSize(1000);
            queryHandler.getSqlExpression().getSqlQuery().setPageInfo(pageInfo);
            SearchIndexResultSet resultSet = queryHandler.getJdbcHandler().querySqlList(
                    queryHandler.getSqlExpression().getSqlQuery(), sqlExpression.getNeedColums());
            
            List<MiniCubeMember> memberResultList = this.buildMembersFromCellSet(resultSet,
                    queryLevel, parent, dataSourceInfo, cube);
            result = CollectionUtils.isEmpty(memberResultList) ? result : memberResultList.get(0);
        } catch (Exception e) {
            log.error("queryId:{} error occur when get name:" + name + " from level:" + level,
                    QueryRouterContext.getQueryId());
            throw new MiniCubeQueryException(e);
        }
        return result;
    }
    
    private List<Expression> genWhereCondition(Cube cube, Level level, Map<String, String> params,
            DataSourceInfo ds) {
        List<Expression> expressionList = Lists.newArrayList();
        String dimTable = level.getDimTable();
        Collection<Dimension> dims = cube.getDimensions().values();
        for (Dimension dim : dims) {
            // 过滤条件中包含当前维度表其他列的过滤条件，因此将过滤条件应用到当前维度成员查询上 此处暂时不考虑维度组
            String filterValue = params.get(dim.getId());
            if (StringUtils.isBlank(filterValue)) {
                continue;
            }
            if (MetaNameUtil.isAllMemberUniqueName(filterValue.split(",")[0])) {
                continue;
            }
            int currentSearchLevelIndex = MetaNameUtil
                    .getSearchLevelIndexByUniqueName(StringUtils.split(filterValue, ",")[0]);
            Level dimLevel = dim.getLevels().values().toArray(new Level[0])[currentSearchLevelIndex];
            boolean fromFactable = dimTable.equals(((MiniCube) cube).getSource());
            if ((fromFactable && !(dim instanceof TimeDimension) && !dim.getId().equals(
                    level.getDimension().getId()))
                    && (dimLevel.getType() != LevelType.CALL_BACK)) {
                Expression expression = new Expression(dimLevel.getFactTableColumn());
                // filterValue 格式为{uniqueNameList }
                // 此处需要解析filterValue生成QueryObject
                try {
                    // 此时的 currentSearchLevelIndex + 1 是取filterValue 的值，因为filterValue的index与levels的index多一个维度层
                    String[] filterValueArray = genFilterValue(filterValue, currentSearchLevelIndex + 1);
                    
                    if (dimLevel.getDimTable().equals(dimTable)) {
                     // 退化维的情况
                        Set<String> leafNodes = Sets.newHashSet();
                        for (String tmp : filterValueArray) {
                            leafNodes.add(tmp);
                        }
                        QueryObject queryObject = new QueryObject(null, leafNodes);
                        expression.getQueryValues().add(queryObject);
                    } else {
                     // 正常维度
                        for (String tmp : filterValueArray) {
                            MiniCubeMember member = getMemberFromLevelByName(ds, cube, dimLevel,
                                    tmp, null, params);
                            Set<String> leafNodes = member.getQueryNodes();
                            QueryObject queryObject = new QueryObject(null, leafNodes);
                            expression.getQueryValues().add(queryObject);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    String[] filterValueArray = genFilterValue(filterValue, 0);
                    for (String tmp : filterValueArray) {
                        Set<String> leafNodes = Sets.newHashSet();
                        leafNodes.add(tmp);
                        QueryObject queryObject = new QueryObject(tmp, leafNodes);
                        expression.getQueryValues().add(queryObject);
                    }
                }
                expressionList.add(expression);
            } else if (fromFactable && dimLevel.getType() == LevelType.CALL_BACK) {
                try {
                    List<MiniCubeMember> members = DimensionMemberService
                            .getDimensionMemberServiceByLevelType(LevelType.CALL_BACK).getMembers(
                                    cube, dimLevel, ds, null, params);
                    if (CollectionUtils.isNotEmpty(members)) {
                        Set<String> leafNodes = (members.get(0)).getQueryNodes();
                        Expression expression = new Expression(
                                ((CallbackLevel) dimLevel).getFactTableColumn());
                        if (CollectionUtils.isEmpty(leafNodes)) {
                            leafNodes = Sets.newHashSet();
                            leafNodes.add(members.get(0).getName());
                        }
                        QueryObject queryObject = new QueryObject(members.get(0).getName(),
                                leafNodes);
                        expression.getQueryValues().add(queryObject);
                        expressionList.add(expression);
                    }
                } catch (MiniCubeQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MetaException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (dim instanceof TimeDimension
                    && dimTable.equals(((MiniCube) cube).getSource())) {
                // 此处只考虑了时间维度表和事实表同一张表情况，其他情况暂时不考虑
                Expression expression = new Expression(dimLevel.getFactTableColumn());
                if (!filterValue.contains("start") && !filterValue.contains("end")) {
                    String[] tmp = filterValue.split(",");
                    for (String data : tmp) {
                        if (StringUtils.isNotEmpty(data) && MetaNameUtil.isUniqueName(data)) {
                            String[] valueArray = MetaNameUtil.parseUnique2NameArray(data);
                            Set<String> leafNodes = Sets.newHashSet();
                            leafNodes.add(valueArray[valueArray.length - 1]);
                            QueryObject queryObject = new QueryObject(
                                    valueArray[valueArray.length - 1], leafNodes);
                            expression.getQueryValues().add(queryObject);
                        }
                    }
                } else {
                    Map<String, String> filterMap = AnswerCoreConstant.GSON.fromJson(filterValue,
                            new TypeToken<Map<String, String>>() {
                            }.getType());
                    TimeRangeDetail detail = new TimeRangeDetail(filterMap.get("start"),
                            filterMap.get("end"));
                    String[] days = detail.getDays();
                    
                    for (String day : days) {
                        Set<String> leafNodes = Sets.newHashSet();
                        leafNodes.add(day);
                        QueryObject queryObject = new QueryObject(day, leafNodes);
                        expression.getQueryValues().add(queryObject);
                    }
                }
                
                if (expression.getQueryValues().size() > 0) {
                    expressionList.add(expression);
                }
            } else if ((dimTable.equals(dim.getTableName()) && !dim.getId().equals(
                    level.getDimension().getId()))) {
                String[] filterValueArray = genFilterValue(filterValue, 0);
                Expression expression = new Expression(((MiniCubeLevel) dimLevel).getSource());
                // filterValue 格式为{uniqueNameList }
                // 此处需要解析filterValue生成QueryObject
                
                for (String tmp : filterValueArray) {
                    Set<String> leafNodes = Sets.newHashSet();
                    leafNodes.add(tmp);
                    QueryObject queryObject = new QueryObject(tmp, leafNodes);
                    expression.getQueryValues().add(queryObject);
                }
                expressionList.add(expression);
            }
        }
        return expressionList;
    }
    
    /**
     * 根据
     *
     * @param filterValue
     * @param currentSearchLevelIndex
     * @return
     */
    private String[] genFilterValue(String filterValue, int currentSearchLevelIndex) {
        if (filterValue.contains("{")) {
            filterValue = filterValue.substring(1, filterValue.length() - 1);
        }
        String[] uniqueNameList = filterValue.split(",");
        String[] rs = new String[uniqueNameList.length];
        String[] tmp = null;
        for (int index = 0; index < rs.length; ++index) {
            if (MetaNameUtil.isUniqueName(uniqueNameList[index])) {
                tmp = MetaNameUtil.parseUnique2NameArray(uniqueNameList[index]);
                if (currentSearchLevelIndex <= 0) {
                    currentSearchLevelIndex = tmp.length - 1;
                }
                rs[index] = tmp[currentSearchLevelIndex];
            } else if (!StringUtils.isBlank(uniqueNameList[index])) {
                rs[index] = uniqueNameList[index];
            }
        }
        return rs;
    }

    @Override
    public List<MiniCubeMember> getMemberFromLevelByNames(DataSourceInfo dataSourceInfo, Cube cube,
            Level level, Map<String, String> params, List<String> uniqueNameList) {
        if (level == null || CollectionUtils.isEmpty(uniqueNameList)) {
            throw new IllegalArgumentException("level is null or name is blank");
        }
        try {
            MetaDataService.checkCube(cube);
        } catch (MetaException e1) {
            throw new RuntimeException(e1);
        }
        MetaDataService.checkDataSourceInfo(dataSourceInfo);
        
        MiniCubeLevel queryLevel = (MiniCubeLevel) level;
        List<MiniCubeLevel> list = Lists.newArrayList();
        list.add(queryLevel);
        QueryRequest queryRequest = buildQueryRequest(cube, list, null, dataSourceInfo);
        Expression expression = new Expression(queryLevel.getSource());
        for (String uniqueName : uniqueNameList) {
            String[] tmp = MetaNameUtil.parseUnique2NameArray(uniqueName);
            // 如果tmp的节点大于等于3个是需要建立查询条件的
            if (tmp.length < 3 && MetaNameUtil.isAllMemberUniqueName(uniqueName, 0)) {
                continue;
            }
            int index = MetaNameUtil.getSearchLevelIndexByUniqueName(uniqueName);
            expression.getQueryValues().add(new QueryObject(tmp[index + 1]));
        }
        queryRequest.getWhere().getAndList().add(expression);
        List<Expression> whereCondition = genWhereCondition(cube, level, params, dataSourceInfo);
        if (!whereCondition.isEmpty()) {
            for (Expression exp : whereCondition) {
                
                if (exp.getQueryValues().size() == 1 
                        && CollectionUtils.isNotEmpty(exp.getQueryValues().iterator().next().getLeafValues())) {
                    queryRequest.getWhere().getAndList().add(exp);
                }
            }
        }
        
        List<MiniCubeMember> members = Lists.newArrayList();
        try {
            // 这里的查询主要为了校验数据库是否存在，如果不存在抛异常，后续需要对这个加上配置处理。如果不存在可以不抛异常，直接跳过。。
            // 维度查询，直接采用distinct count
            if (isFromFactTable(cube, queryLevel)) {
                queryRequest.setDistinct(true);
            }
            
            QueryHandler queryHandler = QueryHandlerBuilder.buildQueryHandler(cube, dataSourceInfo);
            SqlExpression sqlExpression = queryHandler.getSqlExpression();
            String tableName = queryRequest.getFrom().getFrom();
            sqlExpression
                    .setNeedColums(SqlColumnUtils.getFacttableColumns(sqlExpression.getQueryMeta(),
                            queryRequest.getSelect(), tableName, false, false));
            Map<String, List<Object>> andCondition = WhereDataUtils
                    .transQueryRequestAndWhereList2Map(queryRequest, tableName, queryHandler
                            .getSqlExpression().getQueryMeta());
            sqlExpression.setTableName(queryRequest.getFrom().getFrom());
            sqlExpression.getSqlQuery().getSelect().setDistinct(queryRequest.isDistinct());
            sqlExpression.generateNoJoinSql(sqlExpression.getNeedColums(), null, null,
                    andCondition, false);
            SearchIndexResultSet resultSet = queryHandler.getJdbcHandler().querySqlList(
                    queryHandler.getSqlExpression().getSqlQuery(), sqlExpression.getNeedColums());
            PageInfo pageInfo = new PageInfo();
            pageInfo.setCurrentPage(0);
            pageInfo.setPageSize(1000);
            queryHandler.getSqlExpression().getSqlQuery().setPageInfo(pageInfo);
            members = buildMembersFromCellSet(resultSet, queryLevel, null, dataSourceInfo, cube);
        } catch (Exception e) {
            log.error("queryId:{} error occur when get name:" + uniqueNameList + " from level:"
                    + level, QueryRouterContext.getQueryId());
            throw new MiniCubeQueryException(e);
        }
        return members;
    }
}