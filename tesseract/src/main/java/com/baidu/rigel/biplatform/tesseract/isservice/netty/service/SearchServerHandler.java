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
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.SearcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.isservice.search.agg.AggregateCompute;
import com.baidu.rigel.biplatform.tesseract.isservice.search.collector.TesseractResultSetCollector;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.IndexSearcherFactory;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SearchRequestMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SearchResultMessage;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.baidu.rigel.biplatform.tesseract.util.QueryRequestUtil;

/**
 * SearchServerHandler
 * 
 * @author lijin
 *
 */
@Sharable
public class SearchServerHandler extends AbstractChannelInboundHandler {
    /**
     * 支持的action
     */
    private static final NettyAction ACTION_SUPPORT = NettyAction.NETTY_ACTION_SEARCH;
    /**
     * 返回的消息action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_SEARCH_FEEDBACK;
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    // private static SearchServerHandler SEARCH_HANDLER;
    /**
     * 
     */
    public SearchServerHandler() {
        super(ACTION_SUPPORT, ACTION_FEEDBACK);
        
    }
    
    private static SearchServerHandler SEARCH_SERVER_HANDLER;
    
    public static synchronized SearchServerHandler getChannelHandler() {
        if (SEARCH_SERVER_HANDLER == null) {
            SEARCH_SERVER_HANDLER = new SearchServerHandler();
        }
        return SEARCH_SERVER_HANDLER;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #messageReceived(io.netty.channel.ChannelHandlerContext,
     * java.lang.Object)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        SearchRequestMessage searchReqeustMessage = (SearchRequestMessage) msg;
        
        String idxPath = searchReqeustMessage.getIdxPath();
        
        QueryRequest queryRequest = (QueryRequest) searchReqeustMessage.getMessageBody();
        Query queryAll = QueryRequestUtil.transQueryRequest2LuceneQuery(queryRequest);
        
        List<String> measureFieldList = new ArrayList<String>();
        List<String> dimFieldList = new ArrayList<String>();
        if (queryRequest.getSelect().getQueryMeasures() != null) {
            for (QueryMeasure qm : queryRequest.getSelect().getQueryMeasures()) {
                measureFieldList.add(qm.getProperties());
            }
        }
        
        if (queryRequest.getSelect().getQueryProperties() != null) {
            dimFieldList.addAll(queryRequest.getSelect().getQueryProperties());
        }
        
        
        SearchIndexResultSet searchResult = null;
        IndexSearcher is = null;
        long current = System.currentTimeMillis();
        SearcherManager searcherManager = IndexSearcherFactory.getInstance().getSearcherManager(idxPath,true);
        try {
            is = searcherManager.acquire();
            QueryWrapperFilter filter = new QueryWrapperFilter(queryAll);
            logger.info("cost " + (System.currentTimeMillis() - current) + " in trans QUERY --> filter:");
            CachingWrapperFilter cachingFilter =new CachingWrapperFilter (filter);
            logger.info("cost " + (System.currentTimeMillis() - current) + " in trans QUERY --> cachedFilter:");
            
            long gcurrent = System.currentTimeMillis();
            Set<String> groupBy = new HashSet<>();
            if (queryRequest.getGroupBy() != null) {
                groupBy = queryRequest.getGroupBy().getGroups();
            }
            
            TesseractResultSetCollector collector = new TesseractResultSetCollector(dimFieldList.toArray(new String[0]), measureFieldList.toArray(new String[0]));
//            TesseractResultRecordCollector collector = new TesseractResultRecordCollector(
//                dimFieldList.toArray(new String[0]), measureFieldList.toArray(new String[0]), groupBy);
            
            logger.info("cost " + (System.currentTimeMillis() - gcurrent) + " in init TesseractResultRecordCollector ");
            is.search(new MatchAllDocsQuery(), cachingFilter, collector);
            searchResult = collector.buildResultSet(groupBy);
//            for (int docId : collector.getResultDocIdList()) {
//                Document doc = is.getIndexReader().document(docId);
//                ResultRecord record = new ResultRecord(doc);
//                // 替换维度数据的明细节点的上层结点信息
//                // List<ResultRecord> recordList =
//                // QueryRequestUtil.mapLeafValue2ValueOfRecord(record,
//                // leafValueMap);
//                
//                resultRecordList.add(record);
//                
//            }
            logger.info("cost " + (System.currentTimeMillis() - current) + " in search,result:"
                    + searchResult.size());
            current = System.currentTimeMillis();
        } finally {
            searcherManager.release(is);
        }
        
        // process result
        // group by
        if (queryRequest.getGroupBy() != null) {
            int dimSize = queryRequest.getSelect().getQueryProperties().size();
            
            
            List<SearchIndexResultRecord> datas = AggregateCompute.aggregate(searchResult.getDataList(), dimSize, queryRequest.getSelect()
                    .getQueryMeasures());
            searchResult.setDataList(datas);
        }
        
        logger.info("cost " + (System.currentTimeMillis() - current) 
                + " in result group by and prepare netty message.");
        current = System.currentTimeMillis();
        MessageHeader mh = new MessageHeader(NettyAction.NETTY_ACTION_SEARCH_FEEDBACK,
            Md5Util.encode(searchResult.toString()));
        SearchResultMessage searchResultMessage = new SearchResultMessage(mh, searchResult);
        ctx.writeAndFlush(searchResultMessage);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #getMessage()
     */
    @Override
    public <T extends AbstractMessage> T getMessage() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#setMessage(com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage)
     */
    @Override
    public <T extends AbstractMessage> void setMessage(T t) {
        return ; 
        
    }
    
}
