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
package com.baidu.rigel.biplatform.tesseract.isservice.index.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexAction;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;

/**
 * 
 * IndexService
 * 
 * @author lijin
 *
 */
public interface IndexService {
    
    /**
     * 
     * initMiniCubeIndex 初始化新Cube索引
     * 
     * @param cubeList
     *            待初始化的cube列表
     * @param dataSourceInfo
     *            数据源信息
     * @param indexAsap
     *            是否立即索引
     * @param limited
     *            是否是sample模式索引
     * @return boolean true表示成功；
     */
    boolean initMiniCubeIndex(List<Cube> cubeList, List<DataSourceInfo> dataSourceInfoList,
        boolean indexAsap, boolean limited) throws IndexAndSearchException;
    
    
    /**
     * updateIndexByDataSourceKey 跟据数据源、事实表增量更新索引数据
     * @param dataSourceKeyList 数据源信息
     * @param factTableNames 事实表信息
     * @param dataSetMap 待修订的事实表及起始ID信息
     * 
     * @throws Exception 
     */
    void updateIndexByDataSourceKey(List<String> dataSourceKeyList, String[] factTableNames,
            Map<String, Map<String, BigDecimal>> dataSetMap) throws Exception;
    
    /**
     * 暂存分片拷贝结果信息
     * @param shardName 分片名
     * @param succList 成功的nodeKeyList
     */
    void setCopyIndexTaskResult(String shardName,List<String> succList) ;
    
    
	/**
	 * doIndexByIndexAction
	 * 
	 * @param indexMeta
	 *            索引元数据
	 * @param idxAction
	 *            索引动作
	 * @param dataMap
	 *            修订数据时，提供修订的起止范围
	 * @throws Exception
	 *             有可能抛出异常
	 */
	void doIndexByIndexAction(IndexMeta indexMeta, IndexAction idxAction,
			Map<String, BigDecimal> dataMap) throws Exception;
}
