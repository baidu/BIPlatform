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
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexWriterFactory;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShardState;
import com.baidu.rigel.biplatform.tesseract.isservice.search.service.IndexSearcherFactory;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.IndexMessage;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.IndexDataResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.IndexDataResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexServerHandler 处理建索引请求,并返回IndexMessage给client
 * 
 * @author lijin
 *
 */
@Sharable
public class IndexServerHandler extends AbstractChannelInboundHandler {
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger (IndexServerHandler.class);
    
    /**
     * ACTION_SUPPORT_SET 支持的处理请求类型
     */
    private static final Set<NettyAction> ACTION_SUPPORT_SET = new HashSet<NettyAction> ();
    /**
     * 返回消息的action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_INDEX_FEEDBACK;
    
    /**
     * init ACTION_SUPPORT_SET IndexServerHandler支持：索引、更新、初始化索引、修订索引等操作
     */
    static {
        ACTION_SUPPORT_SET.add (NettyAction.NETTY_ACTION_INDEX);
        ACTION_SUPPORT_SET.add (NettyAction.NETTY_ACTION_UPDATE);
        ACTION_SUPPORT_SET.add (NettyAction.NETTY_ACTION_INITINDEX);
        ACTION_SUPPORT_SET.add (NettyAction.NETTY_ACTION_MOD);
    }
    
    /**
     * Constructor by
     */
    private IndexServerHandler() {
        super (ACTION_FEEDBACK);
    }
    
    /**
     * 单例
     */
    private static IndexServerHandler INDEX_SERVER_HANDLER;
    
    /**
     * 
     * getChannelHandler
     * 
     * @return IndexServerHandler
     */
    public static synchronized IndexServerHandler getChannelHandler() {
        if (INDEX_SERVER_HANDLER == null) {
            INDEX_SERVER_HANDLER = new IndexServerHandler ();
        }
        return INDEX_SERVER_HANDLER;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #support(com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction)
     */
    @Override
    public boolean support(NettyAction action) {
        if (ACTION_SUPPORT_SET.contains (action)) {
            logger.info ("IndexServerHandler support action:" + action);
            return true;
        } else {
            return false;
        }
    }
    
	private IndexWriter prepareIndexEnv(IndexMessage indexMsg) throws Exception {
		File idxFile = new File(indexMsg.getIdxPath());
		File idxServiceFile = new File(indexMsg.getIdxServicePath());
		logger.info("Path:"+idxFile+"--IndexState:"+indexMsg.getIdxShardState());
		if (indexMsg.getIdxShardState().equals(
				IndexShardState.INDEXSHARD_UNINIT)
				|| indexMsg.getIdxShardState().equals(
						IndexShardState.INDEXSHARD_INDEXED)) {
			// 1、uninit 分建分片 ; indexed 在已有数据上做更新 ;

			// 清理写索引路径
			FileUtils.deleteFile(idxFile);
			if (!indexMsg.getIdxShardState().equals(
					IndexShardState.INDEXSHARD_UNINIT)
					&& idxServiceFile.exists()) {
				// 索引更新\修订，复制索引目录
				FileUtils.copyFolder(indexMsg.getIdxServicePath(),
						indexMsg.getIdxPath());
			}

		}
		IndexWriter idxWriter = IndexWriterFactory.getIndexWriter (indexMsg.getIdxPath ());
		return idxWriter;

	}
	
	
	private IndexDataResultSet getDataProcess(IndexMessage indexMsg,IndexWriter idxWriter) throws Exception {
		IndexDataResultSet data = null;

		if (indexMsg.getMessageHeader().getAction()
				.equals(NettyAction.NETTY_ACTION_MOD) && !indexMsg.getIdxShardState().equals(
						IndexShardState.INDEXSHARD_UNINIT)) {
			// 索引修订
			// S1:查找索引中存在的数据
			List<IndexDataResultRecord> dataQ = ((IndexDataResultSet) indexMsg
					.getDataBody()).getDataList();
			Iterator<IndexDataResultRecord> it = dataQ.iterator();

			List<IndexDataResultRecord> dataProcess = new ArrayList<IndexDataResultRecord>();
			List<Query> deleteQueryList = new ArrayList<Query>();
			while (it.hasNext()) {
				IndexDataResultRecord currRecord = it.next();
				// 查询
				Query query = existInIndex(currRecord, indexMsg.getIdxPath(),
						indexMsg.getIdName(),
						((IndexDataResultSet) indexMsg.getDataBody()).getMeta());
				if (query != null) {
					// 如果存在，则从队列中删除
					dataProcess.add(currRecord);
					it.remove();
					deleteQueryList.add(query);
				}
			}
			// S2:删除旧数据
			if (!CollectionUtils.isEmpty(deleteQueryList)) {
				idxWriter
						.deleteDocuments(deleteQueryList.toArray(new Query[0]));
				idxWriter.commit();
			}
			// S3:设置需要重建索引的数据
			data = new IndexDataResultSet(
					((IndexDataResultSet) indexMsg.getDataBody()).getMeta(),
					dataProcess.size());
			data.setDataList(dataProcess);
		}
		if (data == null) {
			data = (IndexDataResultSet) indexMsg.getDataBody();
		}
		
		return data;
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
    public void messageReceived(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_MESSAGE_RECEIVED_BEGIN,
                "IndexServerHandler"));
        IndexMessage indexMsg = (IndexMessage) msg;
        // 从消息中获取索引路径
       
        IndexWriter idxWriter = prepareIndexEnv(indexMsg);
        IndexDataResultSet data = this.getDataProcess(indexMsg, idxWriter);
        
        logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "IndexServerHandler", "Begin to index "
                        + (data == null ? 0 : data.size ()) + " data"));
        long currDiskSize = FileUtils.getDiskSize (indexMsg.getIdxPath ());
        BigDecimal currMaxId = BigDecimal.ZERO;
        // 读取数据建索引
        
        long count=0;
        if (currDiskSize < indexMsg.getBlockSize ()) {
            while (( currDiskSize < indexMsg.getBlockSize ()) && data.next ()) {
                Document doc = new Document ();
                String[] fieldNameArr = data.getFieldNameArray ();
                for (String select : fieldNameArr) {
                    if (select.equals (indexMsg.getIdName ()) && currMaxId.longValue() < data.getBigDecimal (select).longValue()) {
                        currMaxId = data.getBigDecimal (select);
                    }
                    
                    doc.add (new StringField (select, data.getString (select),
                            Field.Store.NO));
                }
                count++;
                idxWriter.addDocument (doc);
                
                if ((currDiskSize + idxWriter.ramBytesUsed ()) > indexMsg
                        .getBlockSize ()) {
                    // 预估数据大于分片大小，则提交当前的数据
                    idxWriter.commit ();
                    // 重计算实际索引目示大小
                    currDiskSize = FileUtils.getDiskSize (indexMsg
                            .getIdxPath ());
                }
            }
            idxWriter.commit ();
            
        }
        
        logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "IndexServerHandler", " finish index "+count+" record"));
        String feedBackIndexServicePath = null;
        String feedBackIndexFilePath = null;
        // 如果当前分片写满了 or 是当前数据的最后一片，释放indexWriter\设置服务路径
        
        List<IndexDataResultRecord> dataQ = ((IndexDataResultSet) indexMsg.getDataBody()).getDataList();
        if(data.size()!=0 && indexMsg.getMessageHeader ().getAction ().equals (NettyAction.NETTY_ACTION_MOD)){
        	dataQ.addAll(data.getDataList());
        }
        long totalDiskSize = FileUtils.getDiskSize (indexMsg.getIdxPath ());
        if (totalDiskSize > indexMsg.getBlockSize () || indexMsg.isLastPiece ()) {
            
            IndexWriterFactory.destoryWriters (indexMsg.getIdxPath ());
            feedBackIndexServicePath = indexMsg.getIdxPath ();
            feedBackIndexFilePath = indexMsg.getIdxServicePath ();
        } else {
            feedBackIndexServicePath = indexMsg.getIdxServicePath ();
            feedBackIndexFilePath = indexMsg.getIdxPath ();
        }
        
        MessageHeader messageHeader = new MessageHeader (
                NettyAction.NETTY_ACTION_INDEX_FEEDBACK);
        
        IndexMessage indexFeedbackMsg = new IndexMessage (messageHeader,
                indexMsg.getDataBody ());
        indexFeedbackMsg.setBlockSize (indexMsg.getBlockSize ());
        indexFeedbackMsg.setDiskSize (totalDiskSize);
        indexFeedbackMsg.setIdxServicePath (feedBackIndexServicePath);
        indexFeedbackMsg.setIdxPath (feedBackIndexFilePath);
        indexFeedbackMsg.setIdName (indexMsg.getIdName ());
        indexFeedbackMsg.setMaxId (currMaxId);
        indexFeedbackMsg.setIdxShardState(IndexShardState.INDEXSHARD_INDEXING);
        
        
        logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "IndexServerHandler", " send message back "));
        ChannelFuture sendBack = ctx.writeAndFlush (indexFeedbackMsg);
        if (sendBack.isDone ()) {
            ctx.close ();
        }
        if (indexMsg.getMessageHeader ().getAction ()
                .equals (NettyAction.NETTY_ACTION_MOD)) {
            IndexSearcherFactory.getInstance ().refreshSearchManager (
                    indexMsg.getIdxPath ());
            logger.info (String.format (
                    LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                    "IndexServerHandler",
                    "refresh searchManager[" + indexMsg.getIdxPath () + "]"));
        }
        
        logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_MESSAGE_RECEIVED_END,
                "IndexServerHandler"));
    }
    
    private Query existInIndex(IndexDataResultRecord currRecord,
            String idxPath, String queryField, Meta meta) throws Exception {
        Query result = null;
        SearcherManager searcherManager = IndexSearcherFactory.getInstance ()
                .getSearcherManager (idxPath, false);
        IndexSearcher is = null;
        is = searcherManager.acquire ();
        Query query = new TermQuery (new Term (queryField, currRecord.getField (
                meta.getFieldIndex (queryField)).toString ()));
        TopDocs sresult = is.search (query, 1);
        if (sresult.totalHits > 0) {
            result = query;
        }
        searcherManager.release (is);
        return result;
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
        // Server do not need getMessage
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #setMessage
     * (com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage)
     */
    @Override
    public <T extends AbstractMessage> void setMessage(T t) {
        return;
        
    }
    
}
