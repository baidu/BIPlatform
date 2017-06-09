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
package com.baidu.rigel.biplatform.tesseract.isservice.index.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.tesseract.config.IndexConfig;
import com.baidu.rigel.biplatform.tesseract.dataquery.service.DataQueryService;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.datasource.impl.SqlDataSourceWrap;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexMetaWriteImageEvent;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexUpdateEvent;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexUpdateEvent.IndexUpdateInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexMetaIsNullException;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexAction;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShardState;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexState;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.IndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.service.IndexAndSearchClient;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.IndexDataResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.IndexDataResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.util.IndexFileSystemConstants;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexService 实现类
 * 
 * @author lijin
 *
 */
@Service("indexService")

public class IndexServiceImpl implements IndexService {
	/**
	 * LOGGER
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(IndexServiceImpl.class);
	/**
	 * LOGGER
	 */
	private static final String RESULT_KEY_DATA = "RESULT_KEY_DATA";
	/**
	 * RESULT_KEY_INDEXSHARD
	 */
	private static final String RESULT_KEY_INDEXSHARD = "RESULT_KEY_INDEXSHARD";

	/**
	 * RESULT_KEY_MAXID
	 */
	private static final String RESULT_KEY_MAXID = "RESULT_KEY_MAXID";
	
	@Resource
	private IndexConfig indexConfig;
	
	
//	@Value("${index.indexInterval}")
//    private int indexInterval;
//	
//	@Value("${index.copyIdxTimeOut}")
//	private int copyIdxTimeOut;
//	
//	@Value("${index.copyIdxCheckInterval}")
//	private int copyIdxCheckInterval;
//	
//	@Value("${index.shardReplicaNum}")
//	private int shardReplicaNum;
//	
//	@Value("${index.indexShardSize}")
//    private long idxShardSize;

	/**
	 * indexMetaService
	 */
	@Resource
	private IndexMetaService indexMetaService;

	@Resource
	private IsNodeService isNodeService;

	@Resource
	private StoreManager storeManager;

	@Resource
	private DataSourcePoolService dataSourcePoolService;

	/**
	 * dataQueryService
	 */
	@Resource(name = "sqlDataQueryService")
	private DataQueryService dataQueryService;

	/**
	 * dataQueryService
	 */

	private IndexAndSearchClient isClient;
	
	/*
	 * 索引副本拷贝任务执行结果
	 */
	private ConcurrentHashMap<String,List<String>> copyIndexTaskResult;

	/**
	 * Constructor by no param
	 */
	public IndexServiceImpl() {
		super();
		this.isClient = IndexAndSearchClient.getNodeClient();
		this.copyIndexTaskResult=new ConcurrentHashMap<String,List<String>>();
		
	}
	
//	@PostConstruct
//	public void initConfig() {
//
//		this.LOGGER.info("Checking and set config");
//		if (this.shardReplicaNum <= 0) {
//			this.shardReplicaNum = IndexFileSystemConstants.DEFAULT_SHARD_REPLICA_NUM;
//		}
//		if (this.copyIdxCheckInterval <= 0) {
//			this.copyIdxCheckInterval = IndexFileSystemConstants.DEFAULT_COOPYINDEX_CHECKINTERVAL;
//		}
//		if (this.copyIdxTimeOut <= 0) {
//			this.copyIdxTimeOut = IndexFileSystemConstants.DEFAULT_COPYINDEX_TIMEOUT;
//		}
//		if (this.indexInterval <= 0) {
//			this.indexInterval = IndexFileSystemConstants.DEFAULT_INDEX_INTERVAL;
//		}
//		
//		if (this.idxShardSize <= 0){
//			this.idxShardSize = IndexFileSystemConstants.DEFAULT_INDEX_SHARD_SIZE;
//		}
//
//		this.LOGGER
//				.info("After check and set config,now config is :[shardReplicaNum:"
//						+ this.shardReplicaNum
//						+ "][copyIdxCheckInterval:"
//						+ this.copyIdxCheckInterval
//						+ "][copyIdxTimeOut:"
//						+ this.copyIdxTimeOut + "][indexInterval:"+this.indexInterval+"]");
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService
	 * #initMiniCubeIndex(java.util.List,
	 * com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo, boolean,
	 * boolean)
	 */
	@Override
	public boolean initMiniCubeIndex(List<Cube> cubeList,
			List<DataSourceInfo> dataSourceInfoList, boolean indexAsap, boolean limited) {
		/**
		 * 当通过MiniCubeConnection.publishCubes(List<String> cubes, DataSourceInfo
		 * dataSourceInfo);通知索引服务端建立索引数据
		 */
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"initMiniCubeIndex", "[cubeList:" + cubeList
						+ "][dataSourceInfoList:" + dataSourceInfoList + "][indexAsap:"
						+ indexAsap + "][limited:" + limited + "]"));

		// step 1 process cubeList and fill indexMeta infomation
		List<IndexMeta> idxMetaList = this.indexMetaService
				.initMiniCubeIndexMeta(cubeList, dataSourceInfoList);

		if (idxMetaList.size() == 0) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"initMiniCubeIndex", "[cubeList:" + cubeList
							+ "][dataSourceInfoList:" + dataSourceInfoList
							+ "][indexAsap:" + indexAsap + "][limited:"
							+ limited + "]", "Init MiniCube IndexMeta failed"));
			return false;
		} else {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex", "Success init " + idxMetaList.size()
							+ " MiniCube"));
		}

		// step 2 merge indexMeta with exist indexMetas and update indexMeta
		LOGGER.info(String.format(
				LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
				"initMiniCubeIndex", "Merging IndexMeta with exist indexMetas"));

		LinkedList<IndexMeta> idxMetaListForIndex = new LinkedList<IndexMeta>();
		for (IndexMeta idxMeta : idxMetaList) {
			idxMeta = this.indexMetaService.mergeIndexMeta(idxMeta);

			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex",
					"Merge indexMeta success. After merge:["
							+ idxMeta.toString() + "]"));

			idxMetaListForIndex.add(idxMeta);
		}

		// step 3 if(indexAsap) then call doIndex else return
		boolean result=true;
		if (indexAsap) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex", "index as soon as possible"));
			// if need index as soon as possible
			IndexAction idxAction = IndexAction.INDEX_INDEX;
			while (idxMetaListForIndex.size() > 0) {
				IndexMeta idxMeta = idxMetaListForIndex.poll();
				if (idxMeta.getIdxState().equals(
						IndexState.INDEX_AVAILABLE_MERGE)) {
					idxMeta.setIdxState(IndexState.INDEX_AVAILABLE);
					this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
					continue;
				} else if (idxMeta.getIdxState().equals(
						IndexState.INDEX_AVAILABLE_NEEDMERGE)) {
					idxAction = IndexAction.INDEX_MERGE;
				}

				try {
					result=doIndex(idxMeta, idxAction, null);
					
				} catch (Exception e) {
					LOGGER.error(String.format(
							LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
							"initMiniCubeIndex", "[cubeList:" + cubeList
									+ "][dataSourceInfoList:" + dataSourceInfoList
									+ "][indexAsap:" + indexAsap + "][limited:"
									+ limited + "]"), e);
					result=false;
				} finally {
					LOGGER.info(String
							.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
									"initMiniCubeIndex", "[Index indexmeta : "
											+ idxMeta.toString()));
					
				}
			}
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"initMiniCubeIndex", "[cubeList:" + cubeList
						+ "][dataSourceInfoList:" + dataSourceInfoList + "][indexAsap:"
						+ indexAsap + "][limited:" + limited + "]"));
		return result;
	}

	/**
	 * publishIndexUpdateEvent 发布索引更新事件，通知集群中的各节点刷新自己的Searcher
	 * 
	 * @param metaList
	 *            索引元数据列表
	 * @throws Exception
	 *             可能抛出异常
	 */
	private void publishIndexUpdateEvent(List<IndexMeta> metaList) throws Exception {
		if (metaList != null && metaList.size() > 0) {
			List<String> idxServiceList = new ArrayList<String>();
			List<String> idxNoServiceList = new ArrayList<String>();
			for (IndexMeta meta : metaList) {
				for (IndexShard idxShard : meta.getIdxShardList()) {
					/**
					 * TODO 发出的更新事件中，索引的路径不该有机器节点的indexbase信息，应该是由各机器收到消息后，在自己的索引数据目录内进行匹配
					 */
					idxServiceList.add(idxShard.getIdxFilePath());
					idxNoServiceList.add(idxShard.getFilePath());
				}
			}
			IndexUpdateInfo udpateInfo = new IndexUpdateInfo(idxServiceList,
					idxNoServiceList);
			IndexUpdateEvent updateEvent = new IndexUpdateEvent(udpateInfo);
			this.storeManager.postEvent(updateEvent);
		}
	}
	
	/**
	 * publistIndexMetaWriteEvent 发布索引元数据保存事件，通知集群中的各节点保存相关的元数据镜像
	 * @param metaList
	 * @throws Exception
	 */
	private void publistIndexMetaWriteEvent(IndexMeta meta) throws Exception{
		IndexMetaWriteImageEvent event=new IndexMetaWriteImageEvent(meta);
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
				"updateIndexByDataSourceKey", "post IndexMetaWriteImageEvent with IndexMetaId:"+meta.getIndexMetaId()));
		this.storeManager.postEvent(event);
	}

	@Override
	public void updateIndexByDataSourceKey(List<String> dataSourceKeyList, String[] factTableNames,
			Map<String, Map<String, BigDecimal>> dataSetMap)
			throws Exception {

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"updateIndexByDataSourceKey", dataSourceKeyList));
		if (CollectionUtils.isEmpty(dataSourceKeyList)) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"updateIndexByDataSourceKey", dataSourceKeyList));
			throw new IllegalArgumentException();
		}
		
		for(String dataSourceKey:dataSourceKeyList){
		    List<IndexMeta> metaList = new ArrayList<IndexMeta>();
	        IndexAction idxAction = IndexAction.INDEX_INDEX;

	        if (MapUtils.isEmpty(dataSetMap)) {
	            if (!ArrayUtils.isEmpty(factTableNames)) {
	                for (String factTableName : factTableNames) {
	                    List<IndexMeta> fTableMetaList = this.indexMetaService
	                            .getIndexMetasByFactTableName(factTableName,
	                                    dataSourceKey);
	                    if (!CollectionUtils.isEmpty(fTableMetaList)) {
	                        metaList.addAll(fTableMetaList);
	                    } else {
	                        LOGGER.info(String.format(
	                                LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
	                                "updateIndexByDataSourceKey", dataSourceKey,
	                                "can not find IndexMeta for Facttable:["
	                                        + factTableNames + "]"));
	                    }
	                }
	            } else {
	                metaList = this.indexMetaService
	                        .getIndexMetasByDataSourceKey(dataSourceKey);
	            }
	        } else {
	            idxAction = IndexAction.INDEX_MOD;
	            for (String factTableName : dataSetMap.keySet()) {
	                List<IndexMeta> fTableMetaList = this.indexMetaService
	                        .getIndexMetasByFactTableName(factTableName,
	                                dataSourceKey);
	                if (!CollectionUtils.isEmpty(fTableMetaList)) {
	                    metaList.addAll(fTableMetaList);
	                }
	            }

	        }

	        for (IndexMeta meta : metaList) {
	            Map<String, BigDecimal> tableDataSetMap = null;
	            if (!MapUtils.isEmpty(dataSetMap)) {
	                tableDataSetMap = dataSetMap.get(meta.getFacttableName());
	            }
	            try {
	                doIndex(meta, idxAction, tableDataSetMap);
	            } catch (Exception e) {
	                LOGGER.warn(String.format(
	                        LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
	                        "updateIndexByDataSourceKey",
	                        "DataSourceKey:[" + dataSourceKey + "] FactTable:["
	                                + meta.getFacttableName() + "] IndexMetaId:["
	                                + meta.getIndexMetaId() + "]"),e);
	                
	                continue;
	            }
	        }
	        
	        try {
	            publishIndexUpdateEvent(metaList);
	        } catch (Exception e) {
	            LOGGER.warn(String.format(
	                    LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
	                    "updateIndexByDataSourceKey", dataSourceKey),e);
	            
	            continue;
	        }
	        
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
            "updateIndexByDataSourceKey", dataSourceKeyList));

		
	}
	
	
	
	/**
	 * 检查indexMeta是否可以建索引，若可以，则给元数据加锁并返回元数据；否则返回null
	 * @param indexMeta
	 * @return IndexMeta 
	 * @throws Exception 
	 */
	private IndexMeta checkIndexMetaBeforeIndex(IndexMeta indexMeta) throws Exception {
		
		//从缓存中拿最新的索引元数据
		IndexMeta idxMeta = this.indexMetaService.getIndexMetaByIndexMetaId(
				indexMeta.getIndexMetaId(), indexMeta.getStoreKey());

		//查看加锁情况
		if ((idxMeta.getLocked().equals(Boolean.FALSE))
				|| ((System.currentTimeMillis() - idxMeta.getIdxVersion()) > this.indexConfig
						.getIndexInterval())) {
			idxMeta.setLocked(Boolean.TRUE);
			this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
			LOGGER.info("index meta check pass,locked"+idxMeta);
			return idxMeta;
		} else {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"checkIndexMetaBeforeIndex",
					"[indexMeta:"
							+ indexMeta.getIndexMetaId()
							+ ", Locked:"
							+ idxMeta.getLocked()
							+ ", last update:"
							+ idxMeta.getIdxVersion()
							+ " ,indexInterval:"
							+ this.indexConfig.getIndexInterval()
							+ ",test (System.currentTimeMillis()-idxMeta.getIdxVersion()):"
							+ (System.currentTimeMillis() - idxMeta
									.getIdxVersion()) + "]", "[skip index]"));
			return null;
		}
	}
	
	private boolean checkParams(IndexMeta indexMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap){
		if(indexMeta == null || idxAction == null ){
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"checkParams", "[indexMeta:" + indexMeta
							+ "][idxAction:" + idxAction + "]"));
			return false;
		}
		if(idxAction.equals(IndexAction.INDEX_MOD)  && MapUtils.isEmpty(dataMap)){
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"checkParams", "[IndexAction:" + idxAction
							+ "][DataMap is empty:" + dataMap + "]"));
			return false;
		}
		
		return true;
	}
	
	private SqlDataSourceWrap getSqlDataSourceWrapFromIndexMeta(IndexMeta idxMeta) throws DataSourceException{
		SqlDataSourceWrap dataSourceWrape = (SqlDataSourceWrap) this.dataSourcePoolService
				.getDataSourceByKey(idxMeta.getDataSourceInfo());
		if (dataSourceWrape == null) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"getSqlDataSourceWrapFromIndexMeta", "[indexMeta:" + idxMeta +"]",
					"getDataSourceByKey return null"));
			throw new DataSourceException();
		}
		
		return dataSourceWrape;
	}
	
	private long getTotalCountBeforeIndex(SqlQuery sqlQuery,IndexMeta indexMeta,boolean limited) throws DataSourceException{
		long total = 0 ;
		if (limited) {
			total = IndexFileSystemConstants.INDEX_DATA_TOTAL_IN_LIMITEDMODEL;
		} else {
			total = this.dataQueryService.queryForLongWithSql(
					getCountSQLBySQLQuery(sqlQuery), this.getSqlDataSourceWrapFromIndexMeta(indexMeta));
		}
		return total;
	}
	
	private IndexDataResultSet getDataForIndex(SqlQuery sqlQuery,IndexMeta indexMeta,long limitStart,long limitEnd) throws IOException, DataSourceException{
		return this.dataQueryService
				.queryForDocListWithSQLQuery(sqlQuery, this.getSqlDataSourceWrapFromIndexMeta(indexMeta),
						limitStart, limitEnd);
	}
	
	private BigDecimal processShardedData(Map<String,IndexDataResultSet> shardedData,IndexMeta idxMeta,IndexAction idxAction,BigDecimal startId,boolean isLastPiece) throws Exception{
		BigDecimal maxId=startId;		
		boolean currLast=Boolean.FALSE;
		Iterator<String> keyIt=shardedData.keySet().iterator();
		while(keyIt.hasNext()){
			String key=keyIt.next();
			if(!keyIt.hasNext()){
				currLast=isLastPiece;
			}
			IndexDataResultSet currData=shardedData.get(key);
			BigDecimal currMaxId=processData(currData,key,idxMeta,idxAction,currLast);
			if(maxId.longValue()<currMaxId.longValue()){
				maxId=currMaxId;
			}
		}
		return maxId;
	}
	
	private BigDecimal processData(IndexDataResultSet dataResultSet,String shardDimValue,IndexMeta idxMeta,IndexAction idxAction,boolean isLastPiece) throws Exception{
		
		BigDecimal currMaxId=null;
		IndexShard currIdxShard = null;
		int currIdxShardIdx = -1;
		
		while (dataResultSet.size() != 0) {
			// 当前数据待处理，获取待处理的索引分片
			if (currIdxShard == null) {   
				currIdxShardIdx = this.getIndexShardIdByIndexAction(idxMeta,idxAction);
				currIdxShard = idxMeta.getIdxShardList().get(
						currIdxShardIdx);
				currIdxShard.setFull(Boolean.FALSE);
				
			}

			// 处理
			Map<String, Object> result = writeIndex(dataResultSet,	idxAction, currIdxShard, isLastPiece, idxMeta.getDataDescInfo().getIdStr());
			
			//更新时间戳
			this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);

			dataResultSet = (IndexDataResultSet) result.get(RESULT_KEY_DATA);
			currMaxId = (BigDecimal) result.get(RESULT_KEY_MAXID);
			currIdxShard = (IndexShard) result.get(RESULT_KEY_INDEXSHARD);
			if(!StringUtils.isEmpty(currIdxShard.getShardDimBase())){
				currIdxShard.getShardDimValueSet().add(shardDimValue);
			}
			
			if (this.indexMetaService.isIndexShardFull(currIdxShard) || isLastPiece) {
				//当前分片写满了，需要再申请一片
				//正常的index/update/merge过程，isLastPiece只出现在最后一份数据；但是在mod情况下，每次更新分片，isLastPiece都为true
				currIdxShard=null;				
			}		
			
		}
		return currMaxId;
	}
	
	private String getIdCondition(SqlQuery sqlQuery,BigDecimal currMaxId){
		String currWhereStr = sqlQuery.getIdName() + " > "
				+ currMaxId.longValue();
		return currWhereStr;
	}
	
	private void clearPreviousIdCondition(SqlQuery sqlQuery,BigDecimal currMaxId){
		String currWhereStr=getIdCondition(sqlQuery,currMaxId);
		if (sqlQuery.getWhereList().contains(currWhereStr)) {
			sqlQuery.getWhereList().remove(currWhereStr);
		}
		return ;
	}
	
	private void setCurrIdCondition(SqlQuery sqlQuery,BigDecimal currMaxId){
		String currWhereStr=getIdCondition(sqlQuery,currMaxId);
		sqlQuery.getWhereList().add(currWhereStr);
		return ;
	}
	
	private void saveIndexShardMetaData(IndexMeta idxMeta,IndexAction idxAction){
		Iterator<IndexShard> idxShardIt=idxMeta.getIdxShardList().iterator();
		while(idxShardIt.hasNext()){
			IndexShard idxShard=idxShardIt.next();
			if (idxShard.isUpdate()) {
				String servicePath = idxShard.getFilePath();
				String bakFilePath = idxShard.getIdxFilePath();
				idxShard.setIdxFilePath(servicePath);
				idxShard.setFilePath(bakFilePath);
				idxShard.setIdxState(IndexState.INDEX_AVAILABLE);
				if(idxAction.equals(IndexAction.INDEX_MOD) && (idxShard.getShardId() < idxMeta.getIdxShardList().size())){
					idxShard.setFull(Boolean.TRUE);
				}
				
				if(this.indexConfig.getShardReplicaNum()>1 && !CollectionUtils.isEmpty(idxShard.getReplicaNodeKeyList()) && idxShard.getReplicaNodeKeyList().size() > 0){
					//获取副本拷贝情况				
					List<String> replicaNodeKeyList=null;
					try {
						replicaNodeKeyList = this.getCopyIndexTaskInfo(idxShard.getShardName(), this.indexConfig.getCopyIdxTimeOut());
					} catch (InterruptedException e) {
						e.printStackTrace();
						LOGGER.error("InterruptedException Occur",e);
					}
					if(!CollectionUtils.isEmpty(replicaNodeKeyList)){
						idxShard.setReplicaNodeKeyList(replicaNodeKeyList);
					}else{
						idxShard.setReplicaNodeKeyList(new ArrayList<String>());
					}
				}		
				
				
			} else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
				idxShardIt.remove();
			}
		}
	}
	
	private void saveMetaDataAfterIndex(IndexMeta idxMeta,IndexAction idxAction,Map<String, BigDecimal> maxDataIdMap){
		if (!idxAction.equals(IndexAction.INDEX_MOD)) {
			// 除了修订的情况外，init merge update都需要保存上次索引后的最大id
			idxMeta.getDataDescInfo().setMaxDataIdMap(maxDataIdMap);
		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)) {
			idxMeta.getCubeIdSet().addAll(idxMeta.getCubeIdMergeSet());
			idxMeta.getCubeIdMergeSet().clear();

			idxMeta.getDimSet().addAll(idxMeta.getDimInfoMergeSet());
			idxMeta.getMeasureSet().addAll(idxMeta.getMeasureInfoMergeSet());
			idxMeta.getDimInfoMergeSet().clear();
			idxMeta.getMeasureInfoMergeSet().clear();

		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)
				|| idxMeta.getIdxState().equals(IndexState.INDEX_UNINIT)) {
			idxMeta.setIdxState(IndexState.INDEX_AVAILABLE);
		}

		
		saveIndexShardMetaData(idxMeta,idxAction);			
		this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
		LOGGER.info("INDEX FIN,SAVING METAS");
		
	}
	
	
	public boolean doIndex(IndexMeta indexMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap) throws Exception{
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"doIndexByIndexAction", "[indexMeta:" + indexMeta
						+ "][idxAction:" + idxAction + "]"));
		//0. checkIndexMeta & checkParams
		IndexMeta idxMeta = this.checkIndexMetaBeforeIndex(indexMeta);		
		if(!checkParams(idxMeta,idxAction,dataMap)){
			//参数检查失败
			return false;
		}
		
		// 1. IndexMeta-->SQLQuery
		Map<String, SqlQuery> sqlQueryMap = transIndexMeta2SQLQueryByIndexAction(idxMeta, idxAction, dataMap);
		// 0. init maxDataIdMap
		Map<String, BigDecimal> maxDataIdMap = new HashMap<String, BigDecimal>();
		
		for (String tableName : sqlQueryMap.keySet()) {
			SqlQuery sqlQuery = sqlQueryMap.get(tableName);
			BigDecimal currMaxId = sqlQuery.getInitMaxId() == null ? BigDecimal.valueOf(-1):sqlQuery.getInitMaxId();
			
			long total = this.getTotalCountBeforeIndex(sqlQuery, indexMeta,
					idxAction.equals(IndexAction.INDEX_INIT_LIMITED));
			long pcount = IndexFileSystemConstants.FETCH_SIZE_FROM_DATASOURCE > total ? total:IndexFileSystemConstants.FETCH_SIZE_FROM_DATASOURCE;
			boolean isLastPiece = false;
			
			for (int i = 0; i * pcount < total; i++) {
				long limitStart = 0;
				long limitEnd = pcount;
				if ((i + 1) * pcount >= total
						|| idxAction.equals(IndexAction.INDEX_MOD)) {
					isLastPiece = true;
				}
				//取数 
				IndexDataResultSet currResult = getDataForIndex(sqlQuery, indexMeta, limitStart, limitEnd);
				//清理sqlQuery中的 where条件
				clearPreviousIdCondition(sqlQuery,currMaxId);
				//数据分桶
				Map<String,IndexDataResultSet> dataAfterShard=null;
				if(StringUtils.isEmpty(idxMeta.getShardDimBase())){
					dataAfterShard=new HashMap<String,IndexDataResultSet>();
					dataAfterShard.put("1", currResult);
				}else {
					dataAfterShard=shardDataByShardDim(currResult,idxMeta.getShardDimBase());
				}
				//建索引
				currMaxId=processShardedData(dataAfterShard,idxMeta,idxAction,currMaxId,isLastPiece);
				setCurrIdCondition(sqlQuery,currMaxId);
			}
			
			//记录当前表格最大的id;
			maxDataIdMap.put(tableName, currMaxId);
			
		}
		
		LOGGER.info("FINISH INDEX AND BEGIN TO SET METADATA");
		saveMetaDataAfterIndex(idxMeta,idxAction,maxDataIdMap);
		
		LOGGER.info("TO PUBLISH INDEXMETA WRITE LOCALLY EVENT");
		publistIndexMetaWriteEvent(idxMeta);
		
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"doIndex", "[indexMeta:" + indexMeta + "][idxAction:"
						+ idxAction + "]"));
		this.indexMetaService.unLockIndexMeta(idxMeta);
		return true;
		
		
	}
	
	/**
	 * 按照指定维度对数据进行分桶
	 * @param data 原始数据
	 * @param shardDimBase 指定分桶维度
	 * @return Map<String,IndexDataResultSet>
	 */
	private Map<String,IndexDataResultSet> shardDataByShardDim(IndexDataResultSet data,String shardDimBase){
		Map<String,IndexDataResultSet> result=new TreeMap<String,IndexDataResultSet>();
		Meta meta=data.getMeta();
		for(IndexDataResultRecord currData:data.getDataList()){
			String currShardDimValue=currData.getField(meta.getFieldNames().get(shardDimBase)).toString();
			IndexDataResultSet currDataSet=result.containsKey(currShardDimValue)?result.get(currShardDimValue):new IndexDataResultSet(meta);
			currDataSet.addRecord(currData);
			if(!result.containsKey(currShardDimValue)){
				result.put(currShardDimValue, currDataSet);
			}
		}
		return result;
	}
	
	
	

	/**
	 * doIndexByIndexAction
	 * @param indexMeta 索引元数据
	 * @param idxAction 索引动作
	 * @param dataMap 修订数据时，提供修订的起止范围
	 * @throws Exception 有可能抛出异常
	 */
	public void doIndexByIndexAction(IndexMeta indexMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap)
			throws Exception {
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"doIndexByIndexAction", "[indexMeta:" + indexMeta
						+ "][idxAction:" + idxAction + "]"));
		IndexMeta idxMeta = this.indexMetaService.getIndexMetaByIndexMetaId(indexMeta.getIndexMetaId(), indexMeta.getStoreKey());
		
		if ((idxMeta.getLocked().equals(Boolean.FALSE)) || ((System.currentTimeMillis()-idxMeta.getIdxVersion()) > this.indexConfig.getIndexInterval())) {
			idxMeta.setLocked(Boolean.TRUE);
			this.indexMetaService.saveIndexMetaLocally(idxMeta);
		}else {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"doIndexByIndexAction",
					"[indexMeta:" + indexMeta.getIndexMetaId() + ", Locked:"
							+ idxMeta.getLocked() + ", last update:"
							+ idxMeta.getIdxVersion() + " ,indexInterval:"
							+ this.indexConfig.getIndexInterval() + ",test (System.currentTimeMillis()-idxMeta.getIdxVersion()):"+(System.currentTimeMillis()-idxMeta.getIdxVersion())+"]","[skip index]"));
			return ;
		}		
		
		if (idxMeta == null || idxAction == null) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"doIndexByIndexAction", "[indexMeta:" + indexMeta
							+ "][idxAction:" + idxAction + "]"));
			throw new IllegalArgumentException();
		}
		// 1. IndexMeta-->SQLQuery
		Map<String, SqlQuery> sqlQueryMap = transIndexMeta2SQLQuery(idxMeta, idxAction, dataMap);
		// 2. get a connection
		SqlDataSourceWrap dataSourceWrape = (SqlDataSourceWrap) this.dataSourcePoolService
				.getDataSourceByKey(idxMeta.getDataSourceInfo());
		if (dataSourceWrape == null) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"doIndexByIndexAction", "[indexMeta:" + indexMeta
							+ "][idxAction:" + idxAction + "]"),
					"getDataSourceByKey return null");
			throw new DataSourceException();
		}
		// 3. get data & write index
		Map<String, BigDecimal> maxDataIdMap = new HashMap<String, BigDecimal>();

		for (String tableName : sqlQueryMap.keySet()) {
			SqlQuery sqlQuery = sqlQueryMap.get(tableName);

			if (!StringUtils.isEmpty(sqlQuery.getIdName())) {
				sqlQuery.getOrderBy().add(sqlQuery.getIdName());
			}

			long total = 0;
			BigDecimal currMaxId = BigDecimal.valueOf(-1);

			if (idxMeta.getDataDescInfo().getMaxDataId(tableName) != null
					&& !idxAction.getFromScratch()) {
				// 数据正常更新，最上次的最大ID
				currMaxId = idxMeta.getDataDescInfo().getMaxDataId(tableName);
			}
			// 其它情况，init、merge情况currMaxId=0，mod情况currMaxId可以为0
			String currWhereStr = sqlQuery.getIdName() + " > "
					+ currMaxId.longValue();
			sqlQuery.getWhereList().add(currWhereStr);
			if (idxAction.equals(IndexAction.INDEX_INIT_LIMITED)) {
				total = IndexFileSystemConstants.INDEX_DATA_TOTAL_IN_LIMITEDMODEL;
			} else {
				total = this.dataQueryService.queryForLongWithSql(
						getCountSQLBySQLQuery(sqlQuery), dataSourceWrape);
			}

			boolean isLastPiece = false;
			long pcount = IndexFileSystemConstants.FETCH_SIZE_FROM_DATASOURCE;
			// 目前是跟据数据量进行划分
			if (pcount > total) {
				pcount = total;
			}
			if (!StringUtils.isEmpty(sqlQuery.getIdName())
					&& !CollectionUtils.isEmpty(sqlQuery.getSelectList())) {
				sqlQuery.getSelectList().add(sqlQuery.getIdName());
			}

			for (int i = 0; i * pcount < total; i++) {
				long limitStart = 0;
				long limitEnd = pcount;
				if ((i + 1) * pcount >= total
						|| idxAction.equals(IndexAction.INDEX_MOD)) {
					isLastPiece = true;
				}

				if (sqlQuery.getWhereList().contains(currWhereStr)) {
					sqlQuery.getWhereList().remove(currWhereStr);
				}

				currWhereStr = sqlQuery.getIdName() + " > "
						+ currMaxId.longValue();
				sqlQuery.getWhereList().add(currWhereStr);

				TesseractResultSet currResult = this.dataQueryService
						.queryForDocListWithSQLQuery(sqlQuery, dataSourceWrape,
								limitStart, limitEnd);

				IndexShard currIdxShard = null;
				int currIdxShardIdx = -1;

				while (currResult.size() != 0) {
					// 当前数据待处理，获取待处理的索引分片
					if (currIdxShard == null) {
						currIdxShardIdx = this.getIndexShardByIndexAction(
								idxMeta, idxAction, currIdxShardIdx);
						currIdxShard = idxMeta.getIdxShardList().get(
								currIdxShardIdx);
						currIdxShard.setFull(Boolean.FALSE);
					}

					// 处理
					Map<String, Object> result = writeIndex(currResult,	idxAction, currIdxShard, isLastPiece, sqlQuery.getIdName());
					
					//更新时间戳
					this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);

					currResult = (TesseractResultSet) result.get(RESULT_KEY_DATA);
					currMaxId = (BigDecimal) result.get(RESULT_KEY_MAXID);
					currIdxShard = (IndexShard) result.get(RESULT_KEY_INDEXSHARD);
					if (this.indexMetaService.isIndexShardFull(currIdxShard) || isLastPiece) {
						// 设置当前分片的状态为内容已变更
						currIdxShard = null;
						if (idxAction.equals(IndexAction.INDEX_MOD)
								|| idxAction.equals(IndexAction.INDEX_MERGE)
								|| idxAction
										.equals(IndexAction.INDEX_MERGE_NORMAL)) {
							currIdxShardIdx++;
							if(idxAction.equals(IndexAction.INDEX_MERGE_NORMAL)){
								idxAction=IndexAction.INDEX_MERGE;
							}							
							if(currIdxShardIdx>=idxMeta.getIdxShardList().size()){
								idxAction=IndexAction.INDEX_NORMAL;
							}
						} else {
							currIdxShardIdx = -1;
							if (!idxAction.equals(IndexAction.INDEX_MOD)
									&& !idxAction
											.equals(IndexAction.INDEX_MERGE_NORMAL)) {
								idxAction = IndexAction.INDEX_NORMAL;
							}
						}

					}else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
						idxAction = IndexAction.INDEX_MERGE_NORMAL;
					} else if (!idxAction.equals(IndexAction.INDEX_MOD)
							&& !idxAction
									.equals(IndexAction.INDEX_MERGE_NORMAL)) {
						idxAction = IndexAction.INDEX_NORMAL;
					}

				}

			}

			maxDataIdMap.put(tableName, currMaxId);

		}
		
		this.LOGGER.info("FINISH INDEX AND BEGIN TO SET METADATA");

		if (!idxAction.equals(IndexAction.INDEX_MOD)) {
			// 除了修订的情况外，init merge update都需要保存上次索引后的最大id
			idxMeta.getDataDescInfo().setMaxDataIdMap(maxDataIdMap);
		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)) {
			idxMeta.getCubeIdSet().addAll(idxMeta.getCubeIdMergeSet());
			idxMeta.getCubeIdMergeSet().clear();

			idxMeta.getDimSet().addAll(idxMeta.getDimInfoMergeSet());
			idxMeta.getMeasureSet().addAll(idxMeta.getMeasureInfoMergeSet());
			idxMeta.getDimInfoMergeSet().clear();
			idxMeta.getMeasureInfoMergeSet().clear();

		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)
				|| idxMeta.getIdxState().equals(IndexState.INDEX_UNINIT)) {
			idxMeta.setIdxState(IndexState.INDEX_AVAILABLE);
		}

//		for (IndexShard idxShard : idxMeta.getIdxShardList()) {
//			if (idxShard.isUpdate()) {
//				String servicePath = idxShard.getFilePath();
//				String bakFilePath = idxShard.getIdxFilePath();
//				idxShard.setIdxFilePath(servicePath);
//				idxShard.setFilePath(bakFilePath);
//				idxShard.setIdxState(IndexState.INDEX_AVAILABLE);
//			} else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
//				idxMeta.getIdxShardList().remove(idxShard);
//			}
//		}
		
		Iterator<IndexShard> idxShardIt=idxMeta.getIdxShardList().iterator();
		while(idxShardIt.hasNext()){
			IndexShard idxShard=idxShardIt.next();
			if (idxShard.isUpdate()) {
				String servicePath = idxShard.getFilePath();
				String bakFilePath = idxShard.getIdxFilePath();
				idxShard.setIdxFilePath(servicePath);
				idxShard.setFilePath(bakFilePath);
				idxShard.setIdxState(IndexState.INDEX_AVAILABLE);
				if(idxAction.equals(IndexAction.INDEX_MOD) && (idxShard.getShardId() < idxMeta.getIdxShardList().size())){
					idxShard.setFull(Boolean.TRUE);
				}	
				
				
				if(this.indexConfig.getShardReplicaNum()>1 && !CollectionUtils.isEmpty(idxShard.getReplicaNodeKeyList()) && idxShard.getReplicaNodeKeyList().size() > 0){
//				if(IndexShard.getDefaultShardReplicaNum()>1){	
					//获取副本拷贝情况				
					List<String> replicaNodeKeyList=this.getCopyIndexTaskInfo(idxShard.getShardName(), this.indexConfig.getCopyIdxTimeOut());
					if(!CollectionUtils.isEmpty(replicaNodeKeyList)){
						idxShard.setReplicaNodeKeyList(replicaNodeKeyList);
					}else{
						idxShard.setReplicaNodeKeyList(new ArrayList<String>());
					}
				}		
				
				
			} else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
				idxShardIt.remove();
			}
		}
		
		idxMeta.setLocked(Boolean.FALSE);
		LOGGER.info("INDEX FIN,SAVING METAS");
		this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
		
		LOGGER.info("TO PUBLISH INDEXMETA WRITE LOCALLY EVENT");
		publistIndexMetaWriteEvent(idxMeta);
		
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"doIndex", "[indexMeta:" + indexMeta + "][idxAction:"
						+ idxAction + "]"));
		return;

	}
	
	/**
	 * 检查分片副本拷贝情况
	 * @param shardName 分片名
	 * @param timeOut 超时时间
	 * @return List<String>
	 * @throws InterruptedException
	 */
	private List<String> getCopyIndexTaskInfo(String shardName,int timeOut) throws InterruptedException{
		int copyTaskTimeOut=timeOut;
		if(copyTaskTimeOut<=0){
			copyTaskTimeOut=IndexFileSystemConstants.DEFAULT_COPYINDEX_TIMEOUT;
		}
		
		List<String> result=null;
		
		long curr=System.currentTimeMillis();
		
		while((System.currentTimeMillis()-curr) < copyTaskTimeOut){
			if(this.copyIndexTaskResult.get(shardName)!=null){
				result=this.copyIndexTaskResult.get(shardName);
				break;
			}
			Thread.sleep(this.indexConfig.getCopyIdxCheckInterval());
		}
		if((System.currentTimeMillis()-curr)>=copyTaskTimeOut && CollectionUtils.isEmpty(result)){
			this.LOGGER.info("getCopyIndexTaskInfo time out,shardName:"+shardName);
		}
		return result;
	}

	/**
	 * getIndexShardByIndexAction 获取当前待处理的分片的数组下标
	 * 
	 * @param idxMeta
	 *            索引元数据
	 * @param idxAction
	 *            动作
	 * @param idxShardIdx
	 *            要获取的数组下标
	 * @return
	 */
	private int getIndexShardByIndexAction(IndexMeta idxMeta,
			IndexAction idxAction, int idxShardIdx) {
		if (idxAction.getFromScratch() && idxShardIdx >= 0
				&& idxShardIdx < idxMeta.getIdxShardList().size()) {
			// idxShardIdx正常，且idxAction为从0开始的，则直接反回
			return idxShardIdx;
		} else if (idxAction.getFromScratch() && idxShardIdx == -1
				&& !CollectionUtils.isEmpty(idxMeta.getIdxShardList())) {
			for (int i = 0; i < idxMeta.getIdxShardList().size(); i++) {
				if (!idxMeta.getIdxShardList().get(i).isUpdate()) {
					idxShardIdx = i;
					break;
				}
			}

			return idxShardIdx;
		} else {
			return getFreeIndexShardIndexForIndex(idxMeta,false);
		}

	}
	
	/**
	 * Index:若当前分片有未满的，返回;否则返回一个空的分片
	 * Merge: 返回一个空的分片
	 * Mod: 从0开始
	 * @param idxMeta 索引元数据
	 * @param idxAction 动作
	 * @param idxShardIdx 要获取的数组下标
	 * @return
	 */
	private int getIndexShardIdByIndexAction(IndexMeta idxMeta,IndexAction idxAction){
		int result=-1;
		if(idxAction.equals(IndexAction.INDEX_INDEX)){
			result= getFreeIndexShardIndexForIndex(idxMeta,false);
		}else if(idxAction.equals(IndexAction.INDEX_MOD) || idxAction.equals(IndexAction.INDEX_MERGE)){
			for(int i=0;i<idxMeta.getIdxShardList().size();i++){
				if(!idxMeta.getIdxShardList().get(i).isUpdate()){
					result=i;
					break;
				}
			}
			if(result==-1){
				return getFreeIndexShardIndexForIndex(idxMeta,true);
			}else if(idxAction.equals(IndexAction.INDEX_MERGE) && idxMeta.getIdxShardList().get(result).getIdxShardState().equals(IndexShardState.INDEXSHARD_INDEXED)){
				idxMeta.getIdxShardList().get(result).setIdxShardState(IndexShardState.INDEXSHARD_UNINIT);
			}
		}
		
		return result;
	}

	

	/**
	 * 从索引元数据中获取空闲索引分片的数组下标
	 * 
	 * @param indexMeta
	 * @return int 若找到，返回>-1的值，否则为-1
	 */
	private int getFreeIndexShardIndexForIndex(IndexMeta indexMeta,boolean onlyEmpty) {
		int result = -1;
		IndexMeta idxMeta = indexMeta;
		if (idxMeta == null || idxMeta.getIdxShardList() == null) {
			throw new IllegalArgumentException();
		}
		if (this.indexMetaService.isIndexShardFull(idxMeta) || onlyEmpty) {
			idxMeta = this.indexMetaService.assignIndexShard(idxMeta,
					this.isNodeService.getCurrentNode().getClusterName());
		}
		for (int i = 0; i < idxMeta.getIdxShardList().size(); i++) {
			if (idxMeta.getIdxShardList().get(i) != null
					&& (!this.indexMetaService.isIndexShardFull(idxMeta
							.getIdxShardList().get(i)) && !idxMeta
							.getIdxShardList().get(i).isUpdate() || idxMeta
							.getIdxShardList().get(i).getIdxState()
							.equals(IndexState.INDEX_UNINIT)
							&& onlyEmpty)) {
				result = i;
				break;
			}
		}
		return result;
	}

	

	/**
	 * 
	 * 跟据sqlQuery得到select count语句
	 * 
	 * @param sqlQuery
	 *            sqlQuery
	 * @return String select count语句
	 */
	private String getCountSQLBySQLQuery(SqlQuery sqlQuery) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(*) from (");
		sb.append(sqlQuery.toSql());
		sb.append(") as t");
		return sb.toString();
	}

	/**
	 * 
	 * writeIndex 向指定的分片中写数据，如果该分片写满或者写入的数据是最后一片，则启动数据拷贝线程
	 * 
	 * @param data
	 *            要写的数据
	 * @param isUpdate
	 *            是否是更新
	 * @param idxShard
	 *            索引分片
	 * @param lastPiece
	 *            是否是最后一片数据
	 * @return Map<String,Object>
	 * @throws IndexAndSearchException 
	 * 
	 * 
	 */
	public Map<String, Object> writeIndex(TesseractResultSet data,
			IndexAction idxAction, IndexShard idxShard, boolean lastPiece,
			String idName) throws IndexAndSearchException  {

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"writeIndex", "[data:" + data + "][idxAction:" + idxAction
						+ "][idxShard:" + idxShard + "][lastPiece:" + lastPiece
						+ "][idName:" + idName + "]"));
		
		Node node=this.isNodeService.getNodeByNodeKey(idxShard.getClusterName(), idxShard.getNodeKey(), Boolean.TRUE);
		
		IndexMessage message = null;
		message = isClient.index(data, idxAction, idxShard,node, idName, lastPiece);

		LOGGER.info(String.format(
				LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
				"writeIndex", "index success"));

		idxShard.setDiskSize(message.getDiskSize());
		idxShard.setIdxShardState(message.getIdxShardState());

		if (this.indexMetaService.isIndexShardFull(idxShard) || lastPiece) {
			// 设置提供服务的目录：
			String absoluteIdxFilePath = message.getIdxServicePath();
			String targetFilePath=idxShard.getFilePath();
			
			idxShard.setUpdate(Boolean.TRUE);
			idxShard.setIdxShardState(IndexShardState.INDEXSHARD_INDEXED);
			// 启动数据copy线程拷贝数据到备分节点上
			
			Map<String,Node> assignedNodeMap=new HashMap<String,Node>();
			if (CollectionUtils.isEmpty(idxShard.getReplicaNodeKeyList())) {
				
				assignedNodeMap=this.isNodeService.assignFreeNodeForReplica(this.indexConfig.getShardReplicaNum() - 1,
								idxShard.getNodeKey(),idxShard.getClusterName());

				if (MapUtils.isNotEmpty(assignedNodeMap)) {
					if (assignedNodeMap.keySet().contains(idxShard.getNodeKey())) {
						assignedNodeMap.remove(idxShard.getNodeKey());
					}
					List<String> replicaNodeKeyList= new ArrayList<String>();
					if(MapUtils.isNotEmpty(assignedNodeMap)){
						replicaNodeKeyList.addAll(assignedNodeMap.keySet());
					}					
					idxShard.setReplicaNodeKeyList(replicaNodeKeyList);
				}else{
					this.LOGGER.info("can not assign free replica node for shard:"+idxShard.getShardName());
				}

			}
			
			assignedNodeMap=this.isNodeService.getNodeMapByNodeKey(idxShard.getClusterName(), idxShard.getReplicaNodeKeyList(), true);
			if(!MapUtils.isEmpty(assignedNodeMap)){
				List<Node> toNodeList=new ArrayList<Node>();
				toNodeList.addAll(assignedNodeMap.values());
				
				try {
					ServerFeedbackMessage bMessage=this.isClient.startIndexDataCopy(idxShard.getShardName(),absoluteIdxFilePath, targetFilePath, node, toNodeList);
					
				} catch (Exception e) {
					LOGGER.warn("Exception occured when start to copy index to other nodes",e);
				}
			}			
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put(RESULT_KEY_INDEXSHARD, idxShard);
		result.put(RESULT_KEY_DATA, message.getDataBody());
		result.put(RESULT_KEY_MAXID, message.getMaxId());

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"writeIndex", "[data:" + data + "][idxAction:" + idxAction
						+ "][idxShard:" + idxShard + "][lastPiece:" + lastPiece
						+ "][idName:" + idName + "]"));
		return result;

	}
	
	
	
	
	/**
	 * 根据IndexMeta获取从事实表中取数据的SQLQuery对像
	 * 
	 * @param idxMeta
	 *            当前的idxMeta
	 * @param idxAction
	 *            索引动作
	 * @return Map<String, SqlQuery> 返回sqlquery对像
	 * @throws IndexMetaIsNullException
	 *             当idxMeta为空时会抛出异常
	 */
	private Map<String, SqlQuery> transIndexMeta2SQLQueryByIndexAction(IndexMeta idxMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap)
			throws IndexMetaIsNullException {
		Map<String, SqlQuery> result = new HashMap<String, SqlQuery>();
		
		//get idName
		String idName = idxMeta.getDataDescInfo().getIdStr();
		
		boolean needMerge = Boolean.FALSE;
		if (idxAction.equals(IndexAction.INDEX_MERGE)) {
			needMerge = Boolean.TRUE;
		}
		//get select 
		Set<String> selectList = idxMeta.getSelectList(needMerge) ;		
		
		for (String tableName : idxMeta.getDataDescInfo().getTableNameList()) {
			SqlQuery sqlQuery = new SqlQuery();
			LinkedList<String> fromList = new LinkedList<String>();
			fromList.add(tableName);
			sqlQuery.setFromList(fromList);
			sqlQuery.getSelectList().addAll(selectList);
			
			BigDecimal start = null;
			BigDecimal end = null;
			boolean needEqual=false; 
			//1、mod操作,start \ end 由外界传入(这里需要>= and <=)
			if (idxAction.equals(IndexAction.INDEX_MOD)
					&& !MapUtils.isEmpty(dataMap)) {
				start = dataMap.get(IndexFileSystemConstants.MOD_KEY_START);
				end = dataMap.get(IndexFileSystemConstants.MOD_KEY_END);
				needEqual=true;
			}else if (idxMeta.getDataDescInfo().getMaxDataId(tableName) != null
					&& idxAction.equals(IndexAction.INDEX_INDEX)) {
				// 数据正常建索引，最上次的最大ID(这里需要>)
				start = idxMeta.getDataDescInfo().getMaxDataId(tableName);
				
			}else {
				// Merge索引(这里需要>)
				start = BigDecimal.valueOf(-1);
			}
			
			//设置 id 字段			
			if (!StringUtils.isEmpty(idName)) {
				sqlQuery.setIdName(idName);
				sqlQuery.getOrderBy().add(sqlQuery.getIdName());
                if (!CollectionUtils.isEmpty(sqlQuery.getSelectList())
                    && !sqlQuery.getSelectList().contains(sqlQuery.getIdName())) {
                    sqlQuery.getSelectList().add(sqlQuery.getIdName());
                }
			}
			
//			//设置排序字段
//			if(!StringUtils.isEmpty(idxMeta.getShardDimBase())){
//				sqlQuery.getOrderBy().add(idxMeta.getShardDimBase());
//			}else if (!StringUtils.isEmpty(sqlQuery.getIdName())){
//				sqlQuery.getOrderBy().add(sqlQuery.getIdName());
//			}
//			
			//设置数据读取的limit
			if (start != null) {
				if(needEqual){
					sqlQuery.getWhereList().add(idName + " >= " + start);
				}else {
					sqlQuery.getWhereList().add(idName + " > " + start);
					sqlQuery.setInitMaxId(start);
				}
				
				if (end != null) {
					sqlQuery.getWhereList().add(idName + " <= " + end);
				}
			}
			
			result.put(tableName, sqlQuery);

		}

		return result;
	}
	

	

	/**
	 * 根据IndexMeta获取从事实表中取数据的SQLQuery对像
	 * 
	 * @param idxMeta
	 *            当前的idxMeta
	 * @param idxAction
	 *            索引动作
	 * @return Map<String, SqlQuery> 返回sqlquery对像
	 * @throws IndexMetaIsNullException
	 *             当idxMeta为空时会抛出异常
	 */
	private Map<String, SqlQuery> transIndexMeta2SQLQuery(IndexMeta idxMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap)
			throws IndexMetaIsNullException {
		Map<String, SqlQuery> result = new HashMap<String, SqlQuery>();
		if (idxMeta == null || idxMeta.getDataDescInfo() == null) {
			throw generateIndexMetaIsNullException(idxMeta);
		}

		if (idxAction.equals(IndexAction.INDEX_MOD)
				&& MapUtils.isEmpty(dataMap)) {
			throw new IllegalArgumentException();
		}

		boolean needMerge = Boolean.FALSE;
		if (idxAction.equals(IndexAction.INDEX_MERGE)) {
			needMerge = Boolean.TRUE;
		}
		Set<String> selectList = idxMeta.getSelectList(needMerge);
		if (selectList == null) {
			selectList = new HashSet<String>();
		}
		String idName = idxMeta.getDataDescInfo().getIdStr();
		BigDecimal start = null;
		BigDecimal end = null;
		if (idxAction.equals(IndexAction.INDEX_MOD)
				&& !MapUtils.isEmpty(dataMap)) {
			start = dataMap.get(IndexFileSystemConstants.MOD_KEY_START);
			end = dataMap.get(IndexFileSystemConstants.MOD_KEY_END);
		}
		for (String tableName : idxMeta.getDataDescInfo().getTableNameList()) {
			SqlQuery sqlQuery = new SqlQuery();
			LinkedList<String> fromList = new LinkedList<String>();
			fromList.add(tableName);
			sqlQuery.setFromList(fromList);
			sqlQuery.getSelectList().addAll(selectList);
			result.put(tableName, sqlQuery);
			if (!StringUtils.isEmpty(idName)) {
				sqlQuery.setIdName(idName);
			}
			if (start != null) {
				sqlQuery.getWhereList().add(idName + " >= " + start);
				if (end != null) {
					sqlQuery.getWhereList().add(idName + " <= " + end);
				}
			}
			
			if (!StringUtils.isEmpty(sqlQuery.getIdName())) {
				sqlQuery.getOrderBy().add(sqlQuery.getIdName());
				if(!CollectionUtils.isEmpty(sqlQuery.getSelectList())){
					sqlQuery.getSelectList().add(sqlQuery.getIdName());
				}
			}

		}

		return result;
	}

	/**
	 * 
	 * 统一的生成IndexMetaIsNullException
	 * 
	 * @param idxMeta
	 *            索引元数据
	 * @return IndexMetaIsNullException
	 */
	private IndexMetaIsNullException generateIndexMetaIsNullException(
			IndexMeta idxMeta) {
		StringBuffer sb = new StringBuffer();
		if (idxMeta == null) {
			sb.append("IndexMeta [");
			sb.append(idxMeta);
			sb.append("]");
		} else {
			sb.append(idxMeta.toString());
		}
		LOGGER.info("IndexMetaIsNullException ocurred:" + sb.toString());
		return new IndexMetaIsNullException(sb.toString());
	}

	

	/*
	 * (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService#setCopyIndexTaskResult(java.lang.String, java.util.List)
	 */
	public void setCopyIndexTaskResult(String shardName,List<String> succList) {
		if(this.copyIndexTaskResult == null){
			this.copyIndexTaskResult=new ConcurrentHashMap<String,List<String>>();
		}
		this.copyIndexTaskResult.put(shardName, succList);
		
	}

	
	


    
}
