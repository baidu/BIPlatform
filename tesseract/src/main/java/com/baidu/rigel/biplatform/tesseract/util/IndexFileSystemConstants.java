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
package com.baidu.rigel.biplatform.tesseract.util;

/**
 * 
 * IndexFileSystemConstants
 * @author lijin
 *
 */
public class IndexFileSystemConstants {
    
    /**
     * DEFAULT_INDEX_SHARD_SIZE
     */
    public static final long DEFAULT_INDEX_SHARD_SIZE = 64;
    
    
    public static final long DEFAULT_INDEX_SHARD_SIZE_UNIT=1024*1024;
    
    /**
     * FETCH_SIZE_FROM_DATASOURCE
     */
    public static final long FETCH_SIZE_FROM_DATASOURCE = 1000000;
    
    /**
     * INDEX_DATA_TOTAL_IN_LIMITEDMODEL
     */
    public static final long INDEX_DATA_TOTAL_IN_LIMITEDMODEL = 1000;
    
    /**
     * 默认复本数
     */
    public static final int DEFAULT_SHARD_REPLICA_NUM = 2;
    
    /**
     * 默认检查副本拷贝情况超时时间
     */
    public static final int DEFAULT_COPYINDEX_TIMEOUT=600000;
    
    /**
     * 默认检查副本拷贝情况间隔时间
     */
    public static final int DEFAULT_COOPYINDEX_CHECKINTERVAL=100000;
    
    /**
     * 默认一个索引元数据建索引的间隔
     */
    public static final int DEFAULT_INDEX_INTERVAL=1800000;
    
    /**
     * FACTTABLE_KEY
     */
    public static final String FACTTABLE_KEY = "id";
    
	/**
	 * MOD_KEY_START
	 */
	public static final String MOD_KEY_START = "begin";
	/**
	 * MOD_KEY_END
	 */
	public static final String MOD_KEY_END = "end";
	
	/**
	 * 索引元数据本地镜像文件后缀
	 */
	public static final String INDEX_META_IMAGE_FILE_SAVED=".timg";	
	/**
	 * 索引元数据本地镜像临时文件后缀
	 */
	public static final String INDEX_META_IMAGE_FILE_NEW=".new";

	/**
	 * 索引元数据本地镜像备份文件后缀
	 */
	public static final String INDEX_META_IMAGE_FILE_BAK=".bak";
}
