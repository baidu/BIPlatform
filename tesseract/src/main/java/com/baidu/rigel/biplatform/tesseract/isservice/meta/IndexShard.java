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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.util.StringTools;

/**
 * IndexShard
 * 
 * @author lijin
 *
 */
public class IndexShard implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7727279768348029673L;
    
    
    /**
     * 当前分片数据是否有变动
     */
    private transient boolean isUpdate=false;
    
    /**
     * 分区名称：productLine_facttable_shard_shardId
     */
    private String shardName;
    /**
     * shardId
     */
    private Long shardId;
    
    /**
     * 集群名称
     */
    private String clusterName;    
    
    /**
     * nodekey 主节点key
     */    
    private String nodeKey;
    
    /**
     * 数据复本所在的节点KEY
     */
    private List<String> replicaNodeKeyList;
    
    
    
    /**
     * idxShardStrategy
     */
    private IndexShardStrategy idxShardStrategy;
    /**
     * filePath 用于创建/更新索引的路径
     */
    private String filePath;
    /**
     * idxFilePath 用于提供索引服务的路径
     */
    private String idxFilePath;
    /**
     * 当前分片的版本
     */
    private long idxVersion;
    /**
     * 当前分片是否写满
     */
    private boolean isFull;
    
    /**
     * 当前分片实际占用的物理空间大小，单位bytes
     */
    private long diskSize;
    
    
    private String shardDimBase;
    
    private Set<String> shardDimValueSet;
    
    /**
     * 索引状态
     */
    private IndexState idxState = IndexState.INDEX_UNINIT;
    
    /**
     * 索引分片状态（数据状态）
     */
    private IndexShardState idxShardState=IndexShardState.INDEXSHARD_UNINIT;
    
    /**
     * 构造函数
     * 
     * @param shardName
     *            shardName
     * @param node
     *            node
     */
    public IndexShard(String shardName, Node node) {
        super();
        this.shardName = shardName;
        this.nodeKey = node.getNodeKey();
    }
    
    /**
     * getter method for property shardName
     * 
     * @return the shardName
     */
    public String getShardName() {
        return shardName;
    }
    
    /**
     * setter method for property shardName
     * 
     * @param shardName
     *            the shardName to set
     */
    public void setShardName(String shardName) {
        this.shardName = shardName;
    }
    
    /**
     * getter method for property shardId
     * 
     * @return the shardId
     */
    public Long getShardId() {
        return shardId;
    }
    
    /**
     * setter method for property shardId
     * 
     * @param shardId
     *            the shardId to set
     */
    public void setShardId(Long shardId) {
        this.shardId = shardId;
    }
    
    /**
	 * @return the nodeKey
	 */
	public String getNodeKey() {
		return nodeKey;
	}

	/**
	 * @param nodeKey the nodeKey to set
	 */
	public void setNodeKey(String nodeKey) {
		this.nodeKey = nodeKey;
	}

	/**
	 * @return the replicaNodeKeyList
	 */
	public List<String> getReplicaNodeKeyList() {
		if(this.replicaNodeKeyList==null){
			this.replicaNodeKeyList=new ArrayList<String>();
		}
		return replicaNodeKeyList;
	}

	/**
	 * @param replicaNodeKeyList the replicaNodeKeyList to set
	 */
	public void setReplicaNodeKeyList(List<String> replicaNodeKeyList) {
		this.replicaNodeKeyList = replicaNodeKeyList;
	}

	/**
     * getter method for property idxShardStrategy
     * 
     * @return the idxShardStrategy
     */
    public IndexShardStrategy getIdxShardStrategy() {
        return idxShardStrategy;
    }
    
    /**
     * setter method for property idxShardStrategy
     * 
     * @param idxShardStrategy
     *            the idxShardStrategy to set
     */
    public void setIdxShardStrategy(IndexShardStrategy idxShardStrategy) {
        this.idxShardStrategy = idxShardStrategy;
    }
    
    /**
     * getter method for property filePath
     * 
     * @return the filePath
     */
    public String getFilePath(Node node) {
        return this.filePath;
    }
    
    public String getFilePath() {
        return this.filePath;
    }
    
    public String getAbsoluteFilePath(String nodeIndexBaseDir) {
        return StringTools.concatIndexBaseDir(this.filePath, nodeIndexBaseDir);
    }
    
    
    /**
     * setter method for property filePath
     * 
     * @param filePath
     *            the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setFilePathWithAbsoluteFilePath(String absoluteFilePath, Node node) {
        
        this.filePath = this.trimIndexBaseDir(absoluteFilePath, node);
    }
    
    /**
     * getter method for property serialversionuid
     * 
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    
    
    
    /**
     * getter method for property idxFilePath
     * 
     * @return the idxFilePath
     */
    public String getIdxFilePath(Node node) {
        
        return this.idxFilePath;
    }
    
    public String getIdxFilePath() {
        
        return this.idxFilePath;
    }
    
    public String getAbsoluteIdxFilePath(String nodeIndexBaseDir) {
        
        return StringTools.concatIndexBaseDir(this.idxFilePath, nodeIndexBaseDir);
    }
    
    
    /**
     * setter method for property idxFilePath
     * 
     * @param idxFilePath
     *            the idxFilePath to set
     */
    public void setIdxFilePath(String idxFilePath) {
        this.idxFilePath = idxFilePath;
    }
    
    public void setIdxFilePathWithAbsoluteIdxFilePath(String absoluteIdxFilePath, Node node) {
        this.idxFilePath = this.trimIndexBaseDir(absoluteIdxFilePath, node);
    }
    
    /**
     * getter method for property idxVersion
     * 
     * @return the idxVersion
     */
    public long getIdxVersion() {
        return idxVersion;
    }
    
    /**
     * setter method for property idxVersion
     * 
     * @param idxVersion
     *            the idxVersion to set
     */
    public void setIdxVersion(long idxVersion) {
        this.idxVersion = idxVersion;
    }
    
    /**
     * getter method for property isFull
     * 
     * @return the isFull
     */
//    public boolean isFull() {
//        if ((isFull == false) && this.diskSize >= IndexFileSystemConstants.DEFAULT_INDEX_SHARD_SIZE) {
//            isFull = true;
//        }
//        return isFull;
//    }
    
    public boolean getFull(){
    	return this.isFull;
    }
    
    /**
     * setter method for property isFull
     * 
     * @param isFull
     *            the isFull to set
     */
    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }
    
    /**
     * getter method for property diskSize
     * 
     * @return the diskSize
     */
    public long getDiskSize() {
        return diskSize;
    }
    
    /**
     * setter method for property diskSize
     * 
     * @param diskSize
     *            the diskSize to set
     */
    public void setDiskSize(long diskSize) {
        this.diskSize = diskSize;
    }
    
    /**
     * getter method for property idxState
     * 
     * @return the idxState
     */
    public IndexState getIdxState() {
        return idxState;
    }
    
    /**
     * setter method for property idxState
     * 
     * @param idxState
     *            the idxState to set
     */
    public void setIdxState(IndexState idxState) {
        this.idxState = idxState;
    }
    
   
    
    private String trimIndexBaseDir(String absolutePath, Node node) {
        String result = null;
        if (!StringUtils.isEmpty(absolutePath) && node != null
                && !StringUtils.isEmpty(node.getIndexBaseDir())) {
            result = absolutePath.substring((node.getIndexBaseDir() + File.separator).length());
        }
        return result;
    }
    
    
    
    /**
	 * @param isUpdate the isUpdate to set
	 */
	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}
	
	

	/**
	 * @return the isUpdate
	 */
	public boolean isUpdate() {
		return isUpdate;
	}
	
	

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((idxFilePath == null) ? 0 : idxFilePath.hashCode());
		result = prime * result + ((shardId == null) ? 0 : shardId.hashCode());
		result = prime * result
				+ ((shardName == null) ? 0 : shardName.hashCode());
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
		if (!(obj instanceof IndexShard)) {
			return false;
		}
		IndexShard other = (IndexShard) obj;
		if (clusterName == null) {
			if (other.clusterName != null) {
				return false;
			}
		} else if (!clusterName.equals(other.clusterName)) {
			return false;
		}
		if (filePath == null) {
			if (other.filePath != null) {
				return false;
			}
		} else if (!filePath.equals(other.filePath)) {
			return false;
		}
		if (idxFilePath == null) {
			if (other.idxFilePath != null) {
				return false;
			}
		} else if (!idxFilePath.equals(other.idxFilePath)) {
			return false;
		}
		if (shardId == null) {
			if (other.shardId != null) {
				return false;
			}
		} else if (!shardId.equals(other.shardId)) {
			return false;
		}
		if (shardName == null) {
			if (other.shardName != null) {
				return false;
			}
		} else if (!shardName.equals(other.shardName)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the idxShardState
	 */
	public IndexShardState getIdxShardState() {
		return idxShardState;
	}

	/**
	 * @param idxShardState the idxShardState to set
	 */
	public void setIdxShardState(IndexShardState idxShardState) {
		this.idxShardState = idxShardState;
	}

	/**
	 * @return the shardDimBase
	 */
	public String getShardDimBase() {
		return shardDimBase;
	}

	/**
	 * @param shardDimBase the shardDimBase to set
	 */
	public void setShardDimBase(String shardDimBase) {
		this.shardDimBase = shardDimBase;
	}

	/**
	 * @return the shardDimValueSet
	 */
	public Set<String> getShardDimValueSet() {
		if(CollectionUtils.isEmpty(this.shardDimValueSet)){
			this.shardDimValueSet=new HashSet<String>();
		}
		return shardDimValueSet;
	}

	/**
	 * @param shardDimValueSet the shardDimValueSet to set
	 */
	public void setShardDimValueSet(Set<String> shardDimValueSet) {
		this.shardDimValueSet = shardDimValueSet;
	}
	
	

	
    
}
