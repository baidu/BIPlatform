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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.utils.OperatorUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.handle.model.QueryHandler;
import com.baidu.rigel.biplatform.queryrouter.query.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.queryrouter.query.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.exception.OverflowQueryConditionException;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextSplitService;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryService;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.AggregateCompute;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.DataModelBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.LogInfoConstants;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.QueryRequestBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.QueryRequestUtil;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.TesseractConstant;
import com.baidu.rigel.biplatform.queryrouter.query.service.utils.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.queryrouter.query.vo.CallbackCondition;
import com.baidu.rigel.biplatform.queryrouter.query.vo.MemberNodeTree;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContextSplitResult;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.queryrouter.query.vo.SearchIndexResultSet;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.DataModelConvertService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.PlaneTableUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.SqlColumnUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.WhereDataUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.connection.DataSourcePoolService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.jdbc.service.impl.JdbcCountNumServiceImpl;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.meta.TableExistCheckService;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.mysql.common.TesseractHttpUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.utils.QueryHandlerBuilder;

/**
 * 查询接口实现
 * 
 * @author luowenlei
 *
 */
@Service("qmQueryService")
@Scope("prototype")
public class ConfigQMQueryServiceImpl implements QueryService {

    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * searchService
     */
    @Resource(name="queryTesseractService")
    private QueryService tesseractQueryService;

    /**
     * dataSourcePoolService
     */
    @Resource
    private DataSourcePoolService dataSourcePoolService;
    /**
     * queryContextSplitService
     */
    @Resource
    private QueryContextSplitService queryContextSplitService;
    
    @Resource
    private QueryContextBuilder queryContextBuilder;
    
    @Autowired
    private CallbackSearchServiceImpl callbackSearchService;

    /**
     * dataModelConvertService
     */
    @Resource(name = "dataModelConvertService")
    private DataModelConvertService dataModelConvertService;
    
    /**
     * jdbcCountNumServiceImpl
     */
    @Resource(name = "jdbcCountNumServiceImpl")
    private JdbcCountNumServiceImpl jdbcCountNumService;
    
    /**
     * TableExistCheck
     */
    @Resource(name = "jdbcTableExistCheckServiceImpl")
    private TableExistCheckService tableExistCheckService;
    
    /**
     * 是否使用Tesseract,默认不使用
     */
    public boolean isQueryTesseract = false;

    @Override
    public DataModel query(QuestionModel questionModel,
            QueryContext queryContext) throws Exception, RuntimeException {
        
        long current = System.currentTimeMillis();
        // 拆分查询元数据
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        PlaneTableQuestionModel planeTableQuestionModel = PlaneTableUtils
                .convertConfigQuestionModel2PtQuestionModel(configQuestionModel);
        QueryHandler queryHandler = QueryHandlerBuilder.buildQueryHandler(questionModel);
        SqlExpression sqlExpression = queryHandler.getSqlExpression();
        List<SqlColumn> allNeedColumns = SqlColumnUtils.getAllNeedColumns(sqlExpression.getQueryMeta(),
                planeTableQuestionModel.getSelection());
        if (OperatorUtils.isAggQuery(allNeedColumns)
                || OperatorUtils.isMeasureCallBackQuery(allNeedColumns)) {
            // 多维查询
            Cube cube = null;
            DataSourceInfo dataSourceInfo = null;
                configQuestionModel = (ConfigQuestionModel) questionModel;
                dataSourceInfo = configQuestionModel.getDataSourceInfo();
                cube = configQuestionModel.getCube();

            JsonUnSeriallizableUtils.fillCubeInfo(cube);
            configQuestionModel.setCube(cube);
            logger.info("queryId:{} cost :" + (System.currentTimeMillis() - current)
                    + " to get datasource and other data", QueryRouterContext.getQueryId());
            current = System.currentTimeMillis();
            try {
                queryContext = queryContextBuilder.buildQueryContext(questionModel, dataSourceInfo,
                        cube, queryContext);
            } catch (MetaException e1) {
                e1.printStackTrace();
                throw new MiniCubeQueryException(e1);
            }
            logger.info("queryId:{} cost :" + (System.currentTimeMillis() - current)
                    + " to build query context.", QueryRouterContext.getQueryId());
            current = System.currentTimeMillis();
            // 条件笛卡尔积，计算查询中条件数和根据汇总条件填充汇总条件
            int conditionDescartes = stateQueryContextConditionCount(queryContext,
                    questionModel.isNeedSummary());
            if (logger.isDebugEnabled()) {
                logger.debug("query condition descarte:" + conditionDescartes);
                logger.debug("question model:{}", questionModel);
            }
            
            if (questionModel.getQueryConditionLimit().isWarningAtOverFlow()
                    && conditionDescartes > questionModel.getQueryConditionLimit()
                            .getWarnningConditionSize()) {
                StringBuilder sb = new StringBuilder();
                sb.append("condition descartes :").append(conditionDescartes).append(" over :")
                        .append(questionModel.getQueryConditionLimit()).append("");
                logger.error(sb.toString());
                throw new OverflowQueryConditionException(sb.toString());
            }
            logger.info("queryId:{} cost :" + (System.currentTimeMillis() - current)
                    + " to stateQueryContextConditionCount.", QueryRouterContext.getQueryId());
            current = System.currentTimeMillis();
            // 调用拆解自动进行拆解
            QueryContextSplitResult splitResult = queryContextSplitService.split(questionModel,
                    dataSourceInfo, cube, queryContext, null);
            logger.info(
                    "queryId:{} cost :" + (System.currentTimeMillis() - current) + " to split.",
                    QueryRouterContext.getQueryId());
            current = System.currentTimeMillis();
            DataModel result = null;
            // 无法拆分或者 拆分出的结果为空，说明直接处理本地就行
            
            // TODO 怀疑这里有逻辑错误
            if (splitResult != null
                    && (!splitResult.getCompileContexts().isEmpty() || !splitResult
                            .getConditionQueryContext().isEmpty())) {
                DataSourceInfo dsInfo = dataSourceInfo;
                Cube finalCube = cube;
                String queryId = configQuestionModel.getQueryId();
                // TODO 抛出到其它节点去,后续需要修改成调用其它节点的方法
                splitResult
                        .getConditionQueryContext()
                        .forEach(
                                (con, context) -> {
                                    QueryRouterContext.setQueryInfo(queryId);
                                    DataModel dm = null;
                                    if (con instanceof CallbackCondition) {
                                        try {
                                            SearchIndexResultSet resultSet = callbackSearchService
                                                    .query(context, QueryRequestBuilder
                                                            .buildQueryRequest(dsInfo, finalCube,
                                                                    context,
                                                                    questionModel.isUseIndex(),
                                                                    null));
                                            dm = new DataModelBuilder(resultSet, context).build(
                                                    true, finalCube);
                                        } catch (Exception e) {
                                            logger.error(
                                                    "queryId:{} catch error when process callback measure {}",
                                                    QueryRouterContext.getQueryId(), e.getMessage());
                                            QueryRouterContext.removeQueryInfo();
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        try {
                                            dm = executeQuery(questionModel, context);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    splitResult.getDataModels().put(con, dm);
                                    QueryRouterContext.removeQueryInfo();
                                });
                result = queryContextSplitService.mergeDataModel(splitResult);
            } else {
                result = executeQuery(questionModel, queryContext);
            }
            logger.info("queryId:{} cost :" + (System.currentTimeMillis() - current)
                    + " to getdatamodel.", QueryRouterContext.getQueryId());
            
            // sort and filterblank
            if (result != null) {
                long curr = System.currentTimeMillis();
                if (questionModel.isFilterBlank()) {
                    DataModelUtils.filterBlankRow(result);
                    logger.info("queryId:{} query cost:" + (System.currentTimeMillis() - curr)
                            + " filterBlankRow.", QueryRouterContext.getQueryId());
                }
                curr = System.currentTimeMillis();
                result = sortAndTrunc(result, questionModel.getSortRecord(), questionModel
                        .getRequestParams().get(TesseractConstant.NEED_OTHERS));
                logger.info("queryId:{} query cost:" + (System.currentTimeMillis() - curr)
                        + "ms sortAandTrunc.", QueryRouterContext.getQueryId());
            }
            
            return result;
        } else {
            // 平面表
            // 1.检验cube.getSource中的事实表是否在数据库中存在，并过滤不存在的数据表
            List<SqlColumn> needColumns = SqlColumnUtils.getSqlNeedColumns(sqlExpression.getQueryMeta(),
                    planeTableQuestionModel.getSelection());
            String tableNames = tableExistCheckService.getExistTableList(
                    sqlExpression.getTableName(), queryHandler.getJdbcHandler());
            if (StringUtils.isEmpty(tableNames) || CollectionUtils.isEmpty(needColumns)) {
                // 如果获取的cube的数据为空
                logger.info("queryId:{} QuerySqlPlugin find no tables in Db.",
                        questionModel.getQueryId());
                return dataModelConvertService.getEmptyDataModel(needColumns);
            }
            // 2.生成sql
            queryHandler.getSqlExpression().setTableName(tableNames);
            queryHandler.getSqlExpression().generateSql(questionModel);
            
            // 3.execute sql
            String sql = queryHandler.getSqlExpression().getSqlQuery().toSql();
            List<Object> values = queryHandler.getSqlExpression().getSqlQuery().getWhere()
                    .getValues();
            List<Map<String, Object>> rowBasedList = queryHandler.getJdbcHandler().queryForList(
                    sql, values);
            // 4.convert data to datamodel
            DataModel dataModel = dataModelConvertService.convert(needColumns, rowBasedList);
            
            // 5.生成pageSize
            if (planeTableQuestionModel.isGenerateTotalSize()) {
                dataModel.setRecordSize(jdbcCountNumService.getTotalRecordSize(
                        planeTableQuestionModel, queryHandler));
            }
            return dataModel;
        }

    }
    
    /**
     * 根据查询需求判断executeQuery是Qr自己计算还是访问Tessract
     *
     * @param questionModel
     * @return
     * @throws Exception 
     * @throws MiniCubeQueryException
     */
    public DataModel executeQuery(QuestionModel questionModel, QueryContext queryContext
            ) throws RuntimeException, Exception {
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel)questionModel;
        Cube cube = configQuestionModel.getCube();
        DataSourceInfo dataSourceInfo = configQuestionModel.getDataSourceInfo();
        QueryRequest queryRequest =
                QueryRequestBuilder.buildQueryRequest(dataSourceInfo, cube, queryContext,
                        configQuestionModel.isUseIndex(), questionModel.getPageInfo());
        if (statDimensionNode(queryContext.getRowMemberTrees(), false, false) == 0
                || (statDimensionNode(queryContext.getColumnMemberTrees(), false, false) == 0 && CollectionUtils
                        .isEmpty(queryContext.getQueryMeasures()))) {
            return new DataModelBuilder(null, queryContext).build(false, cube);
        }
        if (TesseractHttpUtils.isQueryIndex(questionModel, queryRequest)) {
            return tesseractQueryService.query(questionModel, queryContext);
        } else {
            return this.queryAndAggregate(questionModel, queryRequest, queryContext);
        }
    }
    
    
    /**
     * executeQuery
     *
     * @param questionModel
     * @return
     * @throws MiniCubeQueryException
     */
    public DataModel queryAndAggregate(QuestionModel questionModel,QueryRequest queryRequest,
            QueryContext queryContext) throws MiniCubeQueryException {
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel)questionModel;
        Cube cube = configQuestionModel.getCube();
        long current = System.currentTimeMillis();
        long currentBegin = System.currentTimeMillis();
        if (statDimensionNode(queryContext.getRowMemberTrees(), false, false) == 0
                || (statDimensionNode(queryContext.getColumnMemberTrees(), false, false) == 0 && CollectionUtils
                        .isEmpty(queryContext.getQueryMeasures()))) {
            return new DataModelBuilder(null, queryContext).build(true, cube);
        }
        logger.info("queryId:{} executeQuery cost :" + (System.currentTimeMillis() - current)
                + " to build query request.",
                QueryRouterContext.getQueryId());
        current = System.currentTimeMillis();
        DataModel result = null;
        try {
            QueryHandler queryHandler = QueryHandlerBuilder.buildQueryHandler(questionModel);
            queryHandler.getSqlExpression().getSqlQuery().getWhere().setGeneratePrepareSql(false);
            // 通过QueryReqest中的where转换成 SqlExpression能识别的whereCondition
            Map<String, List<Object>> andCondition = WhereDataUtils.transQueryRequestAndWhereList2Map(
                    queryRequest, ((MiniCube) cube).getSource(),
                    queryHandler.getSqlExpression().getQueryMeta());
            SqlExpression sqlExpression = queryHandler.getSqlExpression();
            List<SqlColumn> needColumns = null;
            if (SqlExpression.class.getSimpleName()
                    .equals(queryHandler.getSqlExpression().getClass().getSimpleName())) {
            // 如果为mysql，不需要agg计算
                needColumns = SqlColumnUtils
                        .getFacttableColumns(sqlExpression.getQueryMeta(), queryRequest.getSelect(),
                                sqlExpression.getTableName(), false, false);
                queryHandler.getSqlExpression().generateNoJoinSql(
                        configQuestionModel, needColumns, andCondition, false);
            } else {
            // 如果不为mysql，需要agg计算
                needColumns = SqlColumnUtils
                        .getFacttableColumns(sqlExpression.getQueryMeta(), queryRequest.getSelect(),
                                sqlExpression.getTableName(), true, false);
                queryHandler.getSqlExpression().generateNoJoinSql(
                        configQuestionModel, needColumns, andCondition, false);
            }
            SqlColumnUtils.setSqlColumnsSqlUniqueName(sqlExpression.getTableName(), needColumns, false, "");
            // 生成事实表gourpby的字段
            List<SqlColumn> noJoinGroupByColumns = sqlExpression.getQueryMeta()
                    .findSqlColumns(sqlExpression.getTableName(), queryRequest.getSelect().getQueryProperties());
            SearchIndexResultSet resultSet = null;
            if (!OperatorUtils.isAggQuery(needColumns)) {
                resultSet = 
                        queryHandler.getJdbcHandler().querySqlListWithAgg(
                                queryHandler.getSqlExpression().getSqlQuery(),
                                noJoinGroupByColumns, queryRequest.getSelect().getQueryMeasures());
            } else {
                resultSet = 
                        queryHandler.getJdbcHandler().querySqlList(
                                queryHandler.getSqlExpression().getSqlQuery(), noJoinGroupByColumns);
            }
            // 生成汇总数据
            logger.info("queryId:{} executeQuery cost :" + (System.currentTimeMillis() - current) + " to get query result.",
                    QueryRouterContext.getQueryId());
            current = System.currentTimeMillis();
            if (queryRequest.getGroupBy() != null && CollectionUtils.isNotEmpty(queryRequest.getGroupBy().getGroups())) {
                try {
                    resultSet = QueryRequestUtil.processGroupBy(resultSet, queryRequest, queryContext);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    logger.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION, "query", "[query:" + queryRequest
                            + "]", e));
                    throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                            IndexAndSearchException.QUERYEXCEPTION_MESSAGE, IndexAndSearchExceptionType.SEARCH_EXCEPTION),
                            e, IndexAndSearchExceptionType.SEARCH_EXCEPTION);
                }

            }
            logger.info("queryId:{} executeQuery cost :" + (System.currentTimeMillis() - current) + " to processGroupBy.",
                    QueryRouterContext.getQueryId());
            
            long beforeBuildCurr=System.currentTimeMillis();
            
            result = new DataModelBuilder(resultSet, queryContext).build(false, cube);
            
            logger.info("queryId:{} executeQuery cost :"
                    + "" + (System.currentTimeMillis() - beforeBuildCurr) + " to build DataModel.",
                    QueryRouterContext.getQueryId());
        } catch (IndexAndSearchException e) {
            logger.error("queryId:{} query occur when search queryRequest:{},e:{}",
                    QueryRouterContext.getQueryId(), queryContext, e);
            throw new MiniCubeQueryException(e);
        }
        logger.info("queryId:{} executeQuery cost :"+ (System.currentTimeMillis() - currentBegin)
                + " to execute query totally.", QueryRouterContext.getQueryId());
        return result;
    }
    
    /**
     * 
     * @param result
     * @param sortRecord
     * @return DataModel
     */
    private DataModel tonNSetting4Chart(DataModel result, SortRecord sortRecord) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal tmp : result.getColumnBaseData().get(0)) {
            //TODO 如果是回调指标，这里需要如何处理？？？？
            if (tmp == null) {
                continue;
            }
            sum = sum.add(tmp);
        }
        // 此处采用默认计算
        result = DataModelUtils.truncModel(result, sortRecord.getRecordSize() - 1); 
        BigDecimal sum1 = BigDecimal.ZERO;
        for (BigDecimal tmp : result.getColumnBaseData().get(0)) {
            //TODO 如果是回调指标，这里需要如何处理？？？？
            if (tmp == null) {
                continue;
            }
            sum1 = sum1.add(tmp);
        }
        BigDecimal other = null;
        if (sum1 != BigDecimal.ZERO) {
            other = sum.subtract(sum1);
        }
        result.getColumnBaseData().get(0).add(other);
        HeadField otherRowField = DeepcopyUtils.deepCopy(result.getRowHeadFields().get(0));
        otherRowField.setSummarizeData(other);
        String caption = "其余";
        otherRowField.setCaption(caption);
        String dimName = MetaNameUtil.getDimNameFromUniqueName(otherRowField.getValue());
        String uniqueName = "[" + dimName + "].[" + caption + "]";
        String nodeUniqueName = "{" + uniqueName + "}";
        otherRowField.setNodeUniqueName(nodeUniqueName);
        otherRowField.setValue(uniqueName);
        result.getRowHeadFields().add(otherRowField);
        return result;
    }
    
    /**
     * 排序并截断结果集，默认显示500条纪录
     * @param result
     * @param sortRecord
     * @param needOthers 
     * @return DataModel
     */
    private DataModel sortAndTrunc(DataModel result, SortRecord sortRecord, String needOthers) {
        if (sortRecord != null) {
            DataModelUtils.sortDataModelBySort(result, sortRecord);
        }
        if (sortRecord == null) {
            return result;
        }
//            int recordSize = sortRecord == null ? 500 : sortRecord.getRecordSize();
        // 二八原则进行统计计算 
        if (TesseractConstant.NEED_OTHERS_VALUE.equals(needOthers)) {
            //TODO 此处先简化计算，由于图形会走此处逻辑，并且图形不包含汇总合集数据 后续考虑处理包含汇总合集的情况
            return tonNSetting4Chart(result, sortRecord);
        }
        return DataModelUtils.truncModel(result, sortRecord.getRecordSize()); 
    }

    /**
     * 
     * TODO  需要后续扩展，目前未实现
     *  根据QueryContext判断当前分组策略是否合理
     *  
     * @param context 当前查询上下文
     * @param needSummary 
     * @return 当前分组之后的QueryContext的结果集笛卡尔积
     */
    private int stateQueryContextConditionCount(QueryContext context, boolean needSummary) {
        if (context == null) {
            throw new IllegalArgumentException("querycontext is null.");
        }
        // 统计行上的总条件数
        int rowConditionCount = statDimensionNode(context.getRowMemberTrees(), needSummary, true);
        // 列上的维度叶子数
        int columnConditionCount = statDimensionNode(context.getColumnMemberTrees(), needSummary, false);

        int filterConditionCount = 1;
        if (MapUtils.isNotEmpty(context.getFilterMemberValues())) {
            for (Set<String> nodeIds : context.getFilterMemberValues().values()) {
                filterConditionCount *= nodeIds.size();
            }
        }
        return rowConditionCount * columnConditionCount * filterConditionCount;
    }

    /**
     * 统计维值信息，根据是否需要查询汇总节点，补全汇总节点查询条件
     * 
     * @param treeNodes
     * @param needSummary
     * @return
     */
    private int statDimensionNode(List<MemberNodeTree> treeNodes, boolean needSummary, boolean isRow) {
        int rowConditionCount = 0;
        if (CollectionUtils.isNotEmpty(treeNodes)) {
            for (MemberNodeTree nodeTree : treeNodes) {
                int dimensionLeafIdCount = 0;
                // 如果根节点的name不为空，那么说明根节点是汇总节点，只需要获取根节点就可以
                if (StringUtils.isBlank(nodeTree.getName()) || MetaNameUtil.isAllMemberName(nodeTree.getName())) {
                    // 统计节点下孩子节点对应的叶子数，如果需要展现汇总节点的话，那么还需要将子节点的叶子节点合并到一起构造汇总节点的查询条件
                    for (MemberNodeTree child : nodeTree.getChildren()) {
                        // 暂时只支持在行上汇总，列上汇总有点怪怪的。。需要再开启
                        if (isRow && needSummary) {
                            nodeTree.setName(MiniCubeMember.SUMMARY_NODE_NAME);
                            nodeTree.setUniqueName(MiniCubeMember.SUMMARY_NODE_NAME);
                            nodeTree.setCaption(MiniCubeMember.SUMMARY_NODE_CAPTION);
                            nodeTree.setSummary(true);
                            nodeTree.setQuerySource(child.getQuerySource());
                            nodeTree.getLeafIds().addAll(child.getLeafIds());
                        }
                        if (nodeTree.getLeafIds().size() == 1
                                && MetaNameUtil.isAllMemberName(nodeTree.getLeafIds().iterator().next())) {
                            continue;
                        } else {
                            dimensionLeafIdCount += child.getLeafIds().size();
                        }
                    }
                } else {
                    dimensionLeafIdCount = nodeTree.getLeafIds().size();
                }
                if (rowConditionCount == 0) {
                    rowConditionCount = dimensionLeafIdCount;
                }else{
                    // 需要保证dimensionLeafIdCount不能为0
                    rowConditionCount *= (dimensionLeafIdCount == 0 ? 1 : dimensionLeafIdCount);
                }
            }
        }
        return rowConditionCount;
    }

    /**
     * default generate get isQueryTesseract
     * @return the isQueryTesseract
     */
    public boolean isQueryTesseract() {
        return isQueryTesseract;
    }

    /**
     * default generate set isQueryTesseract
     * @param isQueryTesseract the isQueryTesseract to set
     */
    public void setQueryTesseract(boolean isQueryTesseract) {
        this.isQueryTesseract = isQueryTesseract;
    }

}
