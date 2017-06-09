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
package com.baidu.rigel.biplatform.tesseract.node.meta;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.store.meta.StoreMeta;

/**
 * 
 * Node
 * 
 * @author lijin
 *
 */
@Component
public class Node extends StoreMeta implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6542256563659355594L;
    /**
     * DATA_STORE_NAME_NODE_META
     */
    private static final String DATA_STORE_NAME_NODE_META = "DATA_STORE_NAME_NODE_META";
    
    /**
     * 默认集群名称
     */
    private static final String CLUSTER_NAME_DEFAULT = "CLUSTER_NAME_DEFAULT";
    /**
     * NODE_IMAGE_PREFIX
     */
    private static final String NODE_IMAGE_PREFIX = "nodeimage.";
    
    /**
     * 机器节点IP
     */
    private String address;
    /**
     * 端口
     */
    @Value("${indexServer.port}")
    private int port;
    
    /**
     * 所属集群
     */
    private String clusterName;
    
    /**
     * 索引服务路径根目录
     */
    @Value("${indexServer.indexBaseDir}")
    private String indexBaseDir;
    
    @Value("${cluster.nodeImageDir}")
    private String imageDir;
    
    @Value("${indexServer.blockCapacity}")
    private int blockCapacity;
    
    /**
     * 每个IndexShard占物理空间上限
     */
    private int blockSize;
    
    /**
     * 已有的分片数
     */
    private int currBlockUsed;
    
    
    /**
     * NodeState 节点状态
     */
    private NodeState nodeState;
    
    /**
     * 节点状态更新时间
     */
    private long lastStateUpdateTime;
    
    /**
     * 查询请求数 一个查询到来时，+1；一个查询结束，-1
     */
    private int searchRequestCount;
    
    /**
     * Constructor by
     * 
     * @throws UnknownHostException
     */
    public Node() throws UnknownHostException {
        super();
        
        InetAddress currAddress = InetAddress.getLocalHost();
        this.address = currAddress.getHostAddress();
        if (StringUtils.isEmpty(this.clusterName)) {
            this.clusterName = CLUSTER_NAME_DEFAULT;
        }
        
    }
    
    /**
     * 构造函数
     * 
     * @param address
     *            address
     * @param port
     *            port
     * @param clusterName
     *            clusterName
     */
    public Node(String address, int port, String clusterName) {
        super();
        this.address = address;
        this.port = port;
        this.clusterName = clusterName;
        
    }
    
    /**
     * 
     * getAddress
     * 
     * @return String
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * 
     * setAddress
     * 
     * @param address
     *            address
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * 
     * getPort
     * 
     * @return String
     */
    public int getPort() {
        return port;
    }
    
    /**
     * setPort
     * 
     * @param port
     *            port
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * 
     * getClusterName
     * 
     * @return String
     */
    public String getClusterName() {
        return clusterName;
    }
    
    /**
     * 
     * setClusterName
     * 
     * @param clusterName
     *            clusterName
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    /**
     * getBlockSize
     * 
     * @return int
     */
    public int getBlockSize() {
        return blockSize;
    }
    
    /**
     * setBlockSize
     * 
     * @param blockSize
     *            blockSize
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
    /**
     * getter method for property currBlockUsed
     * 
     * @return the currBlockUsed
     */
    public int getCurrBlockUsed() {        
        return currBlockUsed;
    }
    
    /**
     * setter method for property currBlockUsed
     * 
     * @param currBlockUsed
     *            the currBlockUsed to set
     */
    public void setCurrBlockUsed(int currBlockUsed) {
        this.currBlockUsed = currBlockUsed;
    }
    
    /**
     * getFreeBlockNum
     * 
     * @return int
     */
    public int getFreeBlockNum() {
        return this.blockCapacity - this.getCurrBlockUsed();
    }
    
    /**
     * 
     * getDataStoreName
     * 
     * @return String
     */
    public static String getDataStoreName() {
        return DATA_STORE_NAME_NODE_META;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.store.meta.MetaStore#getStoreKey()
     */
    @Override
    public String getStoreKey() {
        // 机器节点以所在的集群名称为storeKey
        StringBuffer sb = new StringBuffer();
        sb.append(this.clusterName);
        return Md5Util.encode(sb.toString());
    }
    
    /**
     * getter method for property nodeState
     * 
     * @return the nodeState
     */
    public NodeState getNodeState() {
        return nodeState;
    }
    
    /**
     * setter method for property nodeState
     * 
     * @param nodeState
     *            the nodeState to set
     */
    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }
    
    /**
     * getter method for property searchRequestCount
     * 
     * @return the searchRequestCount
     */
    public int getSearchRequestCount() {
        return searchRequestCount;
    }
    
    /**
     * setter method for property searchRequestCount
     * 
     * @param searchRequestCount
     *            the searchRequestCount to set
     */
    public void setSearchRequestCount(int searchRequestCount) {
        this.searchRequestCount = searchRequestCount;
    }
    
    public synchronized void searchRequestCountAdd() {
        ++this.searchRequestCount;
    }
    
    public synchronized void searchrequestCountSub() {
        this.searchRequestCount--;
    }
    
    /**
     * getter method for property lastStateUpdateTime
     * 
     * @return the lastStateUpdateTime
     */
    public long getLastStateUpdateTime() {
        return lastStateUpdateTime;
    }
    
    /**
     * setter method for property lastStateUpdateTime
     * 
     * @param lastStateUpdateTime
     *            the lastStateUpdateTime to set
     */
    public void setLastStateUpdateTime(long lastStateUpdateTime) {
        this.lastStateUpdateTime = lastStateUpdateTime;
    }
    
    /**
     * getter method for property indexBaseDir
     * 
     * @return the indexBaseDir
     */
    public String getIndexBaseDir() {
        return indexBaseDir;
    }
    
    
    
    /**
	 * @param indexBaseDir the indexBaseDir to set
	 */
	public void setIndexBaseDir(String indexBaseDir) {
		this.indexBaseDir = indexBaseDir;
	}

	/**
     * getter method for property blockCapacity
     * 
     * @return the blockCapacity
     */
    public int getBlockCapacity() {
        return blockCapacity;
    }
    
    /**
     * 获取节点镜像路径
     * @return String
     */
    public String getImageFilePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.imageDir);
        sb.append(File.separator);
        sb.append(NODE_IMAGE_PREFIX);
        sb.append(this.port);
        return sb.toString();
    }
    
    /**
     * 获取节点KEY
     * @return
     */
    public String getNodeKey(){
    	StringBuffer sb=new StringBuffer();
    	sb.append("NODEKEY_"+this.clusterName+"_"+this.address+"_"+this.port);
    	return sb.toString();
    	
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result
				+ ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result + port;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Node)) {
			return false;
		}
		Node other = (Node) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (clusterName == null) {
			if (other.clusterName != null) {
				return false;
			}
		} else if (!clusterName.equals(other.clusterName)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

    /**
     * @param blockCapacity the blockCapacity to set
     */
    public void setBlockCapacity(int blockCapacity) {
        this.blockCapacity = blockCapacity;
    }
    
    
    
    
}
