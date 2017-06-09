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

import java.util.List;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;

/**
 * 
 * 索引元数据Service，与索引元数据相关的操作
 * 
 * @author lijin
 *
 */
public interface IndexMetaService {
    
    /**
     * 
     * 初始化cube的索引元数据，设置cube的维度、指标、事实表、数据源信息，设置索引元数据状态为UN_INIT;不进行索引分片的分配、
     * 元数据在这个方法中不存储
     * 
     * @param cubeList
     *            cube数据
     * @param dataSourceInfoList
     *            数据源信息
     * @return List<IndexMeta> 初始化后的索引元数据,初始化不成功返回空集，不会返回null
     */
    List<IndexMeta> initMiniCubeIndexMeta(List<Cube> cubeList, List<DataSourceInfo> dataSourceInfoList);
    
    /**
     * 
     * cubeId对应的索引元数据
     * 
     * @param cubeId
     *            cube名
     * @param storeKey
     *            存储区的key
     * @return IndexMeta 返回查到结果，如果没有查到，返回null
     */
    IndexMeta getIndexMetaByCubeId(String cubeId, String storeKey);
    
    /**
     * 
     * factTableName事实表对应的索引元数据
     * 
     * @param factTableName
     *            事实表名。如果是分表的情况，则为前缀+正则；
     * @param storeKey
     *            存储区的key
     * @return IndexMeta 返回查到结果，如果没有查到，返回null
     */
    List<IndexMeta> getIndexMetasByFactTableName(String factTableName, String storeKey);
    
    /**
     * 
     * 保存或更新索引元数据到存储区中
     * 
     * @param idxMeta
     *            索引元数据
     * @return boolean 保存成功返回 true，否则false
     */
    boolean saveOrUpdateIndexMeta(IndexMeta idxMeta);
    
    /**
     * 
     * productLineName产品线对应的索引元数据
     * 
     * @param productLineName
     *            产品线名称。
     * @param storeKey
     *            存储区的key
     * @return IndexMeta 返回查到结果，如果没有查到，返回null
     */
    List<IndexShard> getIndexShardListByProductLine(String productLineName, String storeKey);
    
    /**
     * 
     * 为当前的IndexMeta分配IndexShard
     * 
     * @param idxMeta
     *            元数据
     * @param clusterName
     *            申请分片所在集群名
     * @return IndexMeta 分配成功则返回更新后的IndexMeta；否则返回原始IndexMeta
     */
    IndexMeta assignIndexShard(IndexMeta idxMeta, String clusterName);
    
    /**
     * 
     * 合并索引元数据 合并策略： 0.维度、指标完全相同，直接复用，不设置indexMeta的状态
     * 1.维度相同，指标不同，合并复用，设置indexMeta的状态
     * 
     * @param idxMeta
     *            索引元数据
     * @return IndexMeta
     */
    IndexMeta mergeIndexMeta(IndexMeta idxMeta);
    
    /**
     * 判断当前索引中的索引分片是否全部都满了
     * 
     * @param idxMeta
     *            索引元数据
     * @return boolean
     */
    boolean isIndexShardFull(IndexMeta idxMeta);
    
    /**
     * 
     * getIndexMetasByDataSourceKey
     * 
     * @param dataSourceKey
     *            dataSourceKey
     * @return List<IndexMeta>
     */
    List<IndexMeta> getIndexMetasByDataSourceKey(String dataSourceKey);
    
    /**
     * saveIndexMetaLocally 写索引元数据到本地镜像
     * 
     * @param idxMeta
     * @return
     * @throws Exception
     */
    boolean saveIndexMetaLocally(IndexMeta idxMeta) throws Exception;
    
    /**
     * loadIndexMetasLocalImage 从本地镜像中读取索引元数据信息
     * 
     * @param idxBaseDir
     * @param currNodeKey
     * @param clusterName
     * @return
     */
    List<IndexMeta> loadIndexMetasLocalImage(String idxBaseDir, String currNodeKey,
        String clusterName);
    
    /**
     * recoverLocalIndexMetaWithCluster
     * 
     * @param idxMetaList
     *            待恢复的idxMeta列表
     * @param clusterName
     *            集群名称
     */
    void recoverLocalIndexMetaWithCluster(List<IndexMeta> idxMetaList, String clusterName);
    
    /**
     * getIndexMetaByIndexMetaId
     * 
     * @param idxMetaId
     *            idxMetaId
     * @param storeKey
     *            storeKey
     * @return IndexMeta
     */
    IndexMeta getIndexMetaByIndexMetaId(String idxMetaId, String storeKey);
    
    /**
     * 判断当前分片是否已满
     * 
     * @param idxShard
     * @return boolean
     */
    boolean isIndexShardFull(IndexShard idxShard);
    
    
    /**
     * 解锁
     * 
     * @param idxMeta
     */
    void unLockIndexMeta(IndexMeta idxMeta);
    
}
