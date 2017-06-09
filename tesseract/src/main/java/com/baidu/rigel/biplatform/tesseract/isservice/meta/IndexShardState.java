package com.baidu.rigel.biplatform.tesseract.isservice.meta;

/**
 * 索引分片状态
 * @author lijin
 *
 */
public enum IndexShardState {
	
	/**
	 * 索引分片未被初始化
	 */
	INDEXSHARD_UNINIT,
	/**
	 * 索引分片准备建索引
	 */
	INDEXSHARD_PREPARED,
	/**
	 * 索引分片正在建索引
	 */
	INDEXSHARD_INDEXING,
	/**
	 * 索引分片有索引数据
	 */
	INDEXSHARD_INDEXED

}
