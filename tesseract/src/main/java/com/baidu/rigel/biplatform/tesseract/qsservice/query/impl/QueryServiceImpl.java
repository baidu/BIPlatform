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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.CallbackCondition;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.exception.OverflowQueryConditionException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.SearchService;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.impl.CallbackSearchServiceImpl;
import com.baidu.rigel.biplatform.tesseract.meta.MetaDataService;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService.QueryContextSplitStrategy;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryRequestBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContextSplitResult;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.baidu.rigel.biplatform.tesseract.util.DataModelBuilder;
import com.baidu.rigel.biplatform.tesseract.util.QueryRequestUtil;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * 查询接口实现
 * 
 * @author xiaoming.chen
 *
 */
@Service
public class QueryServiceImpl implements QueryService {

    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * searchService
     */
    @Resource
    private SearchService searchService;

    /**
     * metaDataService
     */
    @Resource
    private MetaDataService metaDataService;

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

    @Override
    public DataModel query(QuestionModel questionModel, QueryContext queryContext,
            QueryContextSplitStrategy preSplitStrategy) throws MiniCubeQueryException {
        long current = System.currentTimeMillis();
        if (questionModel == null) {
            throw new IllegalArgumentException("questionModel is null");
        }
        DataSourceInfo dataSourceInfo = null;
        Cube cube = null;
        // 如果是
        if (questionModel instanceof ConfigQuestionModel) {
            ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
            dataSourceInfo = configQuestionModel.getDataSourceInfo();
            cube = configQuestionModel.getCube();
            // 如果是配置端查询的话，默认不使用cache
//            questionModel.setUseIndex(false);
        }
        if (cube == null) {
            cube = metaDataService.getCube(questionModel.getCubeId());
        }
        if (dataSourceInfo == null) {
            dataSourceInfo = dataSourcePoolService.getDataSourceInfo(questionModel.getDataSourceInfoKey());
        }
        logger.info("cost :" + (System.currentTimeMillis() - current) + " to get datasource and other data");
        current = System.currentTimeMillis();
        try {
            queryContext =
                    queryContextBuilder.buildQueryContext(questionModel, dataSourceInfo, cube, queryContext);
        } catch (MetaException e1) {
            e1.printStackTrace();
            throw new MiniCubeQueryException(e1);
        }
        logger.info("cost :" + (System.currentTimeMillis() - current) + " to build query context.");
        current = System.currentTimeMillis();
        // 条件笛卡尔积，计算查询中条件数和根据汇总条件填充汇总条件
        int conditionDescartes = stateQueryContextConditionCount(queryContext, questionModel.isNeedSummary());
        if (logger.isDebugEnabled ()) {
            logger.debug("query condition descarte:" + conditionDescartes);
            logger.debug("question model:{}", questionModel);
        }
        
        if (questionModel.getQueryConditionLimit().isWarningAtOverFlow()
                && conditionDescartes > questionModel.getQueryConditionLimit().getWarnningConditionSize()) {
            StringBuilder sb = new StringBuilder();
            sb.append("condition descartes :").append(conditionDescartes).append(" over :")
                    .append(questionModel.getQueryConditionLimit()).append("");
            logger.error(sb.toString());
            throw new OverflowQueryConditionException(sb.toString());
        }
        logger.info("cost :" + (System.currentTimeMillis() - current) + " to stateQueryContextConditionCount.");
        current = System.currentTimeMillis();
        // 调用拆解自动进行拆解
        QueryContextSplitResult splitResult = queryContextSplitService.split(questionModel, dataSourceInfo, cube, queryContext, preSplitStrategy);
        logger.info("cost :" + (System.currentTimeMillis() - current) + " to split.");
        current = System.currentTimeMillis();
        DataModel result = null;
        // 无法拆分或者 拆分出的结果为空，说明直接处理本地就行
        // TODO 怀疑这里有逻辑错误
        if (splitResult != null 
            && (!splitResult.getCompileContexts().isEmpty() || !splitResult.getConditionQueryContext().isEmpty())) {
            DataSourceInfo dsInfo = dataSourceInfo;
            Cube finalCube = cube;
            // TODO 抛出到其它节点去,后续需要修改成调用其它节点的方法
            splitResult.getConditionQueryContext().forEach((con, context) -> {
                        DataModel dm = null;
                        if (con instanceof CallbackCondition) {
                            try {
                                SearchIndexResultSet resultSet = callbackSearchService
                                        .query(context, QueryRequestBuilder.buildQueryRequest(dsInfo, finalCube, 
                                        context, questionModel.isUseIndex(), null));
                                dm = new DataModelBuilder(resultSet, context).build(true, finalCube);
                            } catch (Exception e) {
                                logger.error("catch error when process callback measure {}",e.getMessage());
                                throw new RuntimeException(e);
                            }
                        } else {
                            dm = executeQuery(dsInfo, finalCube, context, questionModel.isUseIndex(),
                                    questionModel.getPageInfo());
                        }
                        splitResult.getDataModels().put(con, dm);
            });
            
            result = queryContextSplitService.mergeDataModel(splitResult);
        } else {
            result = executeQuery(dataSourceInfo, cube, queryContext,questionModel.isUseIndex(), questionModel.getPageInfo());
        }
        logger.info("cost :" + (System.currentTimeMillis() - current) + " to getdatamodel.");
        return result;

    }

    private DataModel executeQuery(DataSourceInfo dataSourceInfo, Cube cube,
            QueryContext queryContext,boolean useIndex, PageInfo pageInfo) throws MiniCubeQueryException {
        long current = System.currentTimeMillis();
        long currentBegin = System.currentTimeMillis();
        QueryRequest queryRequest =
                QueryRequestBuilder.buildQueryRequest(dataSourceInfo, cube, queryContext, useIndex,pageInfo);
//        logger.info("transfer queryContext:{} to queryRequest:{} cost:{} ", queryContext, queryRequest, System.currentTimeMillis() - current);
        if (statDimensionNode(queryContext.getRowMemberTrees(), false, false) == 0
                || (statDimensionNode(queryContext.getColumnMemberTrees(), false, false) == 0 && CollectionUtils
                        .isEmpty(queryContext.getQueryMeasures()))) {
            return new DataModelBuilder(null, queryContext).build(false, cube);
        }
        logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to build query request.");
        current = System.currentTimeMillis();
        DataModel result = null;
        try {
            SearchIndexResultSet resultSet = searchService.query(queryRequest);
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to get query result.");
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
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to processGroupBy.");
            
            long beforeBuildCurr=System.currentTimeMillis();
            
            result = new DataModelBuilder(resultSet, queryContext).build(false, cube);
            
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - beforeBuildCurr) + " to build DataModel.");
        } catch (IndexAndSearchException e) {
            logger.error("query occur when search queryRequest：" + queryContext, e);
            throw new MiniCubeQueryException(e);
        }
        logger.info("executeQuery cost :" + (System.currentTimeMillis() - currentBegin) + " to execute query totally.");
        return result;
    }
    
    
    /**
     * executeQuery
     *
     * @param questionModel
     * @return
     * @throws MiniCubeQueryException
     */
    @Override
    public DataModel queryIndex(QuestionModel questionModel, QueryContext queryContext) throws MiniCubeQueryException {
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel)questionModel;
        Cube cube = configQuestionModel.getCube();
        DataSourceInfo dataSourceInfo = configQuestionModel.getDataSourceInfo();
        
        long current = System.currentTimeMillis();
        long currentBegin = System.currentTimeMillis();
        QueryRequest queryRequest =
                QueryRequestBuilder.buildQueryRequest(dataSourceInfo, cube, queryContext, configQuestionModel.isUseIndex(), questionModel.getPageInfo());
        if (statDimensionNode(queryContext.getRowMemberTrees(), false, false) == 0
                || (statDimensionNode(queryContext.getColumnMemberTrees(), false, false) == 0 && CollectionUtils
                        .isEmpty(queryContext.getQueryMeasures()))) {
            return new DataModelBuilder(null, queryContext).build(true, cube);
        }
        logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to build query request.");
        current = System.currentTimeMillis();
        DataModel result = null;
        try {
            SearchIndexResultSet resultSet = searchService.query(queryRequest);
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to get query result.");
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
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - current) + " to processGroupBy.");
            
            long beforeBuildCurr=System.currentTimeMillis();
            
            result = new DataModelBuilder(resultSet, queryContext).build(false, cube);
            
            logger.info("executeQuery cost :" + (System.currentTimeMillis() - beforeBuildCurr) + " to build DataModel.");
        } catch (IndexAndSearchException e) {
            logger.error("query occur when search queryRequest：" + queryContext, e);
            throw new MiniCubeQueryException(e);
        }
        logger.info("executeQuery cost :" + (System.currentTimeMillis() - currentBegin) + " to execute query totally.");
        return result;
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

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryService#hasIndex
     * (com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest)
     */
    public boolean hasIndex(QueryRequest query) {
        return this.searchService.hasIndexMeta(query);
    }

    

}
