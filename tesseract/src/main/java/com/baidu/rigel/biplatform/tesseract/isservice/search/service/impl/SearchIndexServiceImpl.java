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
/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.search.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.tesseract.dataquery.service.DataQueryService;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.datasource.impl.SqlDataSourceWrap;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexState;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.SearchService;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.service.IndexAndSearchClient;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.Expression;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryObject;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.baidu.rigel.biplatform.tesseract.util.QueryRequestUtil;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * SearchService 实现类
 * 
 * @author lijin
 *
 */
@Service("searchService")
public class SearchIndexServiceImpl implements SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchIndexServiceImpl.class);

    /**
     * IndexAndSearchClient
     */

    private IndexAndSearchClient isClient;

    /**
     * 索引元数据服务
     */
    @Resource(name = "indexMetaService")
    private IndexMetaService idxMetaService;

    @Resource
    private IsNodeService isNodeService;

    /**
     * dataQueryService
     */
    @Resource(name = "sqlDataQueryService")
    private DataQueryService dataQueryService;

    /**
     * dataSourcePoolService
     */
    @Resource
    private DataSourcePoolService dataSourcePoolService;
    
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    
    /**
     * nodeKeyQueue 
     */
    private ConcurrentLinkedQueue<Node> nodeKeyQueue = new ConcurrentLinkedQueue<Node>();
    
    
    /**
     * Constructor by no param
     */
    public SearchIndexServiceImpl() {
        super();
        this.isClient = IndexAndSearchClient.getNodeClient();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.isservice.search.service.SearchService
     * #hasIndexMeta(com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.
     * QueryRequest)
     */
    public boolean hasIndexMeta(QueryRequest query) {
        if (query == null || StringUtils.isEmpty(query.getCubeId())) {
            LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION, "query",
                    "[query:" + query + "]"));
            throw new IllegalArgumentException();
        }
        IndexMeta idxMeta = idxMetaService.getIndexMetaByCubeId(query.getCubeId(), query
                .getDataSourceInfo().getDataSourceKey());
        
        return !this.queryUseDatabase(query, idxMeta);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.isservice.search.SearchService#query
     * (com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest)
     */
    @Override
    public SearchIndexResultSet query(QueryRequest query) throws IndexAndSearchException {
//        LOGGER.debug(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "query", "[query:" + query + "]"));
        // 1. Does all the existed index cover this query
        // 2. get index meta and index shard
        // 3. trans query to Query that can used for searching
        // 4. dispatch search query
        // 5. do search
        // 6. merge result
        // 7. return
        if (query == null || StringUtils.isEmpty(query.getCubeId())) {
            LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION, "query", "[query:" + query + "]"));
            throw new IndexAndSearchException(getExceptionMessage (),
                IndexAndSearchExceptionType.ILLEGALARGUMENT_EXCEPTION);
        }
        IndexMeta idxMeta =
                idxMetaService
                .getIndexMetaByCubeId(query.getCubeId(), query.getDataSourceInfo().getDataSourceKey());

        SearchIndexResultSet result = null;
        if (queryUseDatabase (query, idxMeta)) {
            result = queryWithDatabase (query);
        } else {
            result = queryWithIndex (query, idxMeta);
//         // 多个分片，需要进行再次进行agg计算
//            if (idxMeta.getIdxShardList ().size () > 1) {
//                List<SearchIndexResultRecord> rs = AggregateCompute.aggregate (result.getDataList (), query);
//                result.setDataList (rs);
//            }
        }

//        LOGGER.debug (String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "query",
//                "merging final result"));

//        LOGGER.debug(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "query", "[query:" + query + "]"));
        return result;
    }

    private String getExceptionMessage() {
        return TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.QUERYEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.ILLEGALARGUMENT_EXCEPTION);
    }

    /**
     * 根据查询请求通过索引查询数据
     * @param query 查询请求
     * @param idxMeta 索引元数据信息
     * @return SearchIndexResultSet
     * @throws IndexAndSearchException
     */
    private SearchIndexResultSet queryWithIndex(QueryRequest query, IndexMeta idxMeta) throws IndexAndSearchException {
        SearchIndexResultSet result;
        long current = System.currentTimeMillis();
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "query", "use index"));
//        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " before prepare get record.");
//            current = System.currentTimeMillis();
        List<SearchIndexResultSet> idxShardResultSetList = executeQueryWithIndex (query, idxMeta);
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "query",
                "merging result from multiple index"));
        result = mergeResultSet(idxShardResultSetList, query);
        StringBuilder sb = new StringBuilder();
        sb.append("cost :").append(System.currentTimeMillis() - current)
        .append(" in get result record,result size:").append(result.size()).append(" shard size:")
        .append(idxShardResultSetList.size());
        
        LOGGER.info(sb.toString());
        return result;
    }

    private List<SearchIndexResultSet> executeQueryWithIndex(QueryRequest query, IndexMeta idxMeta)
        throws IndexAndSearchException {
        ExecutorCompletionService<SearchIndexResultSet> completionService = 
            new ExecutorCompletionService<>(taskExecutor);
        int count=0;
        for (IndexShard idxShard : idxMeta.getIdxShardList()) {
            	if(idxShard.getIdxState().equals(IndexState.INDEX_UNINIT) || !shardFitQuery(idxShard,query)){
            	    continue;
            	}
            final Callable<SearchIndexResultSet> queryTask = genQueryTask (query, idxMeta, idxShard);
            completionService.submit(queryTask);
            count++;
        }
        List<SearchIndexResultSet> idxShardResultSetList = buildResultListWithTask (completionService, count);
        if(CollectionUtils.isEmpty(idxShardResultSetList)){
        	List<String> measureFieldList = new ArrayList<String>();
            List<String> dimFieldList = new ArrayList<String>();
            if (query.getSelect().getQueryMeasures() != null) {
                for (QueryMeasure qm : query.getSelect().getQueryMeasures()) {
                    measureFieldList.add(qm.getProperties());
                }
            }
            
            if (query.getSelect().getQueryProperties() != null) {
                dimFieldList.addAll(query.getSelect().getQueryProperties());
            }
           
            Meta meta = new Meta((String[]) ArrayUtils.addAll(dimFieldList.toArray(new String[0]), measureFieldList.toArray(new String[0])));
            SearchIndexResultSet result = new SearchIndexResultSet(meta, 0);
            idxShardResultSetList.add(result);
        }
        return idxShardResultSetList;
    }
    
	private boolean shardFitQuery(IndexShard idxShard, QueryRequest query) {
		boolean result = true;
		if (!StringUtils.isEmpty(idxShard.getShardDimBase())) {			
			if (query != null && query.getWhere() != null
					&& !CollectionUtils.isEmpty(query.getWhere().getAndList())) {
                for (Expression ex : query.getWhere().getAndList()) {
                    if (ex.getProperties().equals(idxShard.getShardDimBase())
                        && !CollectionUtils.isEmpty(ex.getQueryValues())) {
                        Set<String> currLeafValues = new HashSet<String>();                        
                        for (QueryObject qo : ex.getQueryValues()) {
                            currLeafValues.addAll(qo.getLeafValues());
                        }
                        if (CollectionUtils.isEmpty(CollectionUtils.intersection(
                            idxShard.getShardDimValueSet(), currLeafValues))) {
                            result = false;
                            break;
                        }
                    }
                }
			}
		}
		return result;
	}

    private List<SearchIndexResultSet> buildResultListWithTask(
            ExecutorCompletionService<SearchIndexResultSet> completionService,
            int count) throws IndexAndSearchException {
        List<SearchIndexResultSet> idxShardResultSetList = new ArrayList<SearchIndexResultSet>();
        for(int i = 0; i < count; i++) {
            try {
                idxShardResultSetList.add(completionService.take().get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error (e.getMessage (), e);
                throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                        IndexAndSearchException.QUERYEXCEPTION_MESSAGE,
                        IndexAndSearchExceptionType.NETWORK_EXCEPTION), e,
                        IndexAndSearchExceptionType.NETWORK_EXCEPTION);
            }
        }
        
       
        return idxShardResultSetList;
    }
    
    private synchronized Node getFreeSearchNodeByIndexShard(IndexShard idxShard,String clusterName){
        	List<Node> idxShardNodeList=this.isNodeService.getAvailableNodeListByIndexShard(idxShard, clusterName);
        
        @SuppressWarnings("unchecked")
        Collection<Node> nodeLeft=CollectionUtils.subtract(idxShardNodeList, this.nodeKeyQueue);
        	Node result=null;
        	Iterator<Node> it=null;
        	if(!CollectionUtils.isEmpty(nodeLeft)){
        		//取第一个
        		//塞到nodeKeyQueue尾
        		it=nodeLeft.iterator();
        		if(it.hasNext()){
        			result=it.next();
        		}
        		
        	}else {
        		it=this.nodeKeyQueue.iterator();
        		while(it.hasNext()){
        			Node node = it.next();
        			if(idxShardNodeList.indexOf(node) != -1){
        				it.remove();
        				result=node;
        				break;
        			}
        		}
        	}
        	if(result!=null){
        		this.nodeKeyQueue.add(result);
        	}
        	return result;
    }

    private Callable<SearchIndexResultSet> genQueryTask(QueryRequest query,
        IndexMeta idxMeta, IndexShard idxShard) {
        return new Callable<SearchIndexResultSet>() {
            
            @Override
            public SearchIndexResultSet call() throws Exception {
                try {
                    long current = System.currentTimeMillis();
                    Node searchNode = getFreeSearchNodeByIndexShard(idxShard,idxMeta.getClusterName());                    
                    
                    LOGGER.info("begin search in shard:{},node:{}", idxShard.getShardName(),searchNode.getNodeKey());
                    SearchIndexResultSet result = (SearchIndexResultSet) isClient.search(query, idxShard, searchNode).getMessageBody();                    
                    
                    LOGGER.info("compelete search in shard:{},take:{} ms",idxShard, System.currentTimeMillis() - current);
                    return result;
                } catch (Exception e) {
                    throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                            IndexAndSearchException.QUERYEXCEPTION_MESSAGE,
                            IndexAndSearchExceptionType.NETWORK_EXCEPTION), e,
                            IndexAndSearchExceptionType.NETWORK_EXCEPTION);
                }
                
            }
        };
    }

    private SearchIndexResultSet queryWithDatabase(QueryRequest query)
            throws IndexAndSearchException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "query", "use database"));
        // index does not exist or unavailable,use db query
        long begin = System.currentTimeMillis ();
        long curr = System.currentTimeMillis();
        SqlQuery sqlQuery = QueryRequestUtil.transQueryRequest2SqlQuery(query);
        LOGGER.info("queryWithDatabase cost: " + (System.currentTimeMillis() - curr)
                + " ms to transQueryRequest2SqlQuery");
        curr = System.currentTimeMillis();
        SqlDataSourceWrap dataSourceWrape = null;
        try {
            dataSourceWrape =
                    (SqlDataSourceWrap) this.dataSourcePoolService.getDataSourceByKey(query.getDataSourceInfo());
        } catch (DataSourceException e) {
            LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION, "query", "[query:" + query
                    + "]", e));
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                    IndexAndSearchException.QUERYEXCEPTION_MESSAGE, IndexAndSearchExceptionType.SQL_EXCEPTION), e,
                    IndexAndSearchExceptionType.SQL_EXCEPTION);
        }
        if (dataSourceWrape == null) {
            throw new IllegalArgumentException();
        }

        LOGGER.info("queryWithDatabase cost: " + (System.currentTimeMillis() - curr)
                + " ms to getDataSource");
        curr = System.currentTimeMillis();
        long limitStart = 0;
        long limitSize = 0;
        if (query.getLimit() != null) {
            limitStart = query.getLimit().getStart();
            if (query.getLimit().getSize() > 0) {
                limitSize = query.getLimit().getSize();
            }
        }
        SearchIndexResultSet currResult =
                this.dataQueryService.queryForListWithSQLQueryAndGroupBy(sqlQuery, dataSourceWrape, limitStart,
                        limitSize, query);
        LOGGER.info("queryWithDatabase cost: " + (System.currentTimeMillis() - curr)
                + " ms to queryForListWithSQLQueryAndGroupBy");
        LOGGER.info ("current execute sql is : {} cost {} ms", sqlQuery.toSql (), System.currentTimeMillis () - begin);
        LOGGER.info("current execute used db return " + currResult.size() + " records");
        return currResult;
    }

    private boolean queryUseDatabase(QueryRequest query, IndexMeta idxMeta) {
        return idxMeta == null
                || idxMeta.getIdxState().equals(IndexState.INDEX_UNAVAILABLE)
                || idxMeta.getIdxState().equals(IndexState.INDEX_UNINIT) 
                || CollectionUtils.isEmpty(idxMeta.getIdxShardList()) 
                || !query.isUseIndex()
                || (query.getFrom() != null && query.getFrom().getFrom() != null && !idxMeta.getDataDescInfo()
                        .getTableNameList().contains(query.getFrom().getFrom()))
                || !indexMetaContains(idxMeta, query);
    }

    /**
     * 
     * mergeResultSet
     * 
     * @param resultList 要合并的TesseractResultSet集合
     * @return TesseractResultSet
     */
    private SearchIndexResultSet mergeResultSet(List<SearchIndexResultSet> resultList, QueryRequest query) {
        int totalSize = 0;
        for (SearchIndexResultSet tr : resultList) {
            totalSize += tr.size();
        }
        
        SearchIndexResultSet result = new SearchIndexResultSet(resultList.get(0).getMeta(), totalSize);
        resultList.forEach(set -> {
            result.getDataList().addAll(set.getDataList()); 
        });
        return result;
    }
    
    /**
     * 
     * 当前查询的指标与维度是否存在于索引元数据中
     * @param idxMeta 索引元数据
     * @param query 查询
     * @return boolean
     */
    private boolean indexMetaContains(IndexMeta idxMeta, QueryRequest query) {
        boolean result = false;
        if (idxMeta == null || query == null) {
            return result;
        }
        Set<String> idxSelect = idxMeta.getSelectList(false);
        
        if (!CollectionUtils.isEmpty(idxSelect)
                && idxSelect.containsAll(query.getSelect().getQueryProperties())) {
            for (QueryMeasure qm : query.getSelect().getQueryMeasures()) {
                if (!idxSelect.contains(qm.getProperties())) {
                    result = false;
                    break;
                } else {
                    result = true;
                }
            }
            
            if(query.getWhere()!=null && query.getWhere().getAndList()!=null && result){
                for(Expression ex:query.getWhere().getAndList()){
                    if(!idxSelect.contains(ex.getProperties())){
                        result=false;
                        break;
                    }else{
                        result=true;
                    }
                }
            }
            
        }
        return result;
        
    }

}
