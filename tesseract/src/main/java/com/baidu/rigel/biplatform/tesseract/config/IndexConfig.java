package com.baidu.rigel.biplatform.tesseract.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baidu.rigel.biplatform.tesseract.util.IndexFileSystemConstants;

@Component("indexConfig")
public class IndexConfig {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(IndexConfig.class);

	@Value("${index.indexInterval}")
	private int indexInterval;

	@Value("${index.copyIdxTimeOut}")
	private int copyIdxTimeOut;

	@Value("${index.copyIdxCheckInterval}")
	private int copyIdxCheckInterval;

	@Value("${index.shardReplicaNum}")
	private int shardReplicaNum;

	@Value("${index.indexShardSize}")
	private long idxShardSize;

	/**
	 * @return the indexInterval
	 */
	public int getIndexInterval() {
		return indexInterval;
	}

	/**
	 * @return the copyIdxTimeOut
	 */
	public int getCopyIdxTimeOut() {
		return copyIdxTimeOut;
	}

	/**
	 * @return the copyIdxCheckInterval
	 */
	public int getCopyIdxCheckInterval() {
		return copyIdxCheckInterval;
	}

	/**
	 * @return the shardReplicaNum
	 */
	public int getShardReplicaNum() {
		return shardReplicaNum;
	}

	/**
	 * @return the idxShardSize
	 */
	public long getIdxShardSize() {
		
		
		return idxShardSize * IndexFileSystemConstants.DEFAULT_INDEX_SHARD_SIZE_UNIT;
	}

	@PostConstruct
	public void initConfig() {

		LOGGER.info("Checking and set config");
		if (this.shardReplicaNum <= 0) {
			this.shardReplicaNum = IndexFileSystemConstants.DEFAULT_SHARD_REPLICA_NUM;
		}
		if (this.copyIdxCheckInterval <= 0) {
			this.copyIdxCheckInterval = IndexFileSystemConstants.DEFAULT_COOPYINDEX_CHECKINTERVAL;
		}
		if (this.copyIdxTimeOut <= 0) {
			this.copyIdxTimeOut = IndexFileSystemConstants.DEFAULT_COPYINDEX_TIMEOUT;
		}
		if (this.indexInterval <= 0) {
			this.indexInterval = IndexFileSystemConstants.DEFAULT_INDEX_INTERVAL;
		}

		if (this.idxShardSize <= 0) {
			this.idxShardSize = IndexFileSystemConstants.DEFAULT_INDEX_SHARD_SIZE;
		}

		LOGGER.info("After check and set config,now config is :[shardReplicaNum:"
				+ this.shardReplicaNum
				+ "][copyIdxCheckInterval:"
				+ this.copyIdxCheckInterval
				+ "][copyIdxTimeOut:"
				+ this.copyIdxTimeOut
				+ "][indexInterval:"
				+ this.indexInterval
				+ "]");
	}

}
