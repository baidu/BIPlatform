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
package com.baidu.rigel.biplatform.tesseract.node.service;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;

/**
 * 
 * ISNodeService
 * 
 * @author lijin
 *
 */
public interface IsNodeService {
    
    /**
     * 
     * 跟据当前节点情况就近分配新的节点
     * 
     * @param existNodeList
     *            备选节点列表
     * @param blockCount
     *            需要的索引块个数
     * @param clusterName
     *            集群名称
     * @return List<Node> 返回分配的节点及每个节点上分配的块数
     */
    Map<Node, Integer> assignFreeNodeByNodeList(List<Node> existNodeList, int blockCount,
        String clusterName);
    
    /**
     * 
     * 跟据节点空闲情况分配新的节点
     * 
     * @param blockCount
     *            需要的节点个数
     * @param clusterName
     *            集群名称
     * @return List<Node> 返回分配的节点及每个节点上分配的块数
     */
    Map<Node, Integer> assignFreeNode(int blockCount, String clusterName);
    

    /**
     * 分配数据副本所在的节点
     * @param blockCount 需要的个数
     * @param nodeKey 指定的节点KEY
     * @param clusterName 所在集群名称
     * @return Map<String,Node>
     */
    Map<String,Node> assignFreeNodeForReplica(int blockCount, String nodeKey, String clusterName);
    
    /**
     * 
     * 跟据集群名称，获取该集群下的所有机器节点
     * 
     * @param clusterName
     *            集群名
     * @return List<Node> 该集群下的所有机器节点
     */
    List<Node> getNodeListByClusterName(String clusterName);
    
    /**
     * 
     * 保存节点信息
     * 
     * @param node
     *            需要保存的节点
     * @return boolean 保存成功返回true,否则为false
     */
    boolean saveOrUpdateNodeInfo(Node node);
    

    
    /**
     * 
     * getAvailableNodeListByIndexShard 获取当前索引分片下存活的节点列表
     * 
     * @param idxShard
     *            索引分片
     * @param clusterName 集群名称
     * @return List<Node> List<Node>
     */
    List<Node> getAvailableNodeListByIndexShard(IndexShard idxShard, String clusterName);
    
    
    /**
     * 
     * 标记同一集群中的其它节点是否为bad结点
     * 
     *
     */
    void markClusterBadNode();
    
    /**
     * 
     * 跟据给定的节点，从缓存中取到最新的结点信息
     * 
     * @param node
     * 
     * @return Node 返回找到的结点
     */
    Node getNodeByCurrNode(Node node);
    
    /**
     * 
     * 保存节点镜像
     * 
     * @param node
     *            node
     */
    void saveNodeImage(Node node);
    
    /**
     * 
     * loadLocalNodeImage 从本地镜像中恢复node数据
     * 
     * @param node
     *            当前待恢复的节点
     * @return ode 恢复后的节点信息
     */
    Node loadLocalNodeImage(Node node);
    
    /**
     * 
     * 取得当前节点
     * 
     * @return
     */
    Node getCurrentNode();
    
	/**
	 * getNodeMapByNodeKey 跟据集群名称、nodeKey列表，拿到对应的节点信息
	 * 
	 * @param clusterName
	 *            集群名称
	 * @param nodeKeyList
	 *            nodeKey列表
	 * @param isAvailable 是否可用
	 * @return Map<String,Node>
	 */
	Map<String, Node> getNodeMapByNodeKey(String clusterName, List<String> nodeKeyList, boolean isAvailable);
	
	/**
	 * getNodeByNodeKey 跟据集群名称、nodeKey，拿到对应的节点信息，不区分节点状态
	 * @param clusterName 集群名称
	 * @param nodeKeyL nodeKey
	 * @param isAvailable 是否可用
	 * @return Node
	 */
	Node getNodeByNodeKey(String clusterName, String nodeKey, boolean isAvailable);

	/**
	 * getNodeMapByClusterName 跟据集群名称拿到集群中节点信息，不区分节点状态
	 * 
	 * @param clusterName
	 *            集群名称
	 * @param isAvailable
	 *            是否过滤不可用节点
	 * @return Map<String,Node>
	 */
	Map<String, Node> getNodeMapByClusterName(String clusterName, boolean isAvailable);
    
}
