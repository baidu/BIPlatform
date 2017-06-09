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
package com.baidu.rigel.biplatform.tesseract.netty.message.isservice;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShardState;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;

/**
 * IndexMessage
 * 
 * @author lijin
 *
 */
public class IndexMessage extends AbstractMessage {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6548560612969097919L;
    
    /**
     * 建索引临时目录
     */
    private String idxPath;
    /**
     * 索引服务目录
     */
    private String idxServicePath;
    
    /**
     * 索引分片状态
     */
    private IndexShardState idxShardState;
    /**
     * 数据主体
     */
    private TesseractResultSet dataBody;
//    /**
//     * 维度信息
//     */
//    private List<String> measureInfo;
    /**
     * 用于服务端返回占用磁盘空间
     */
    private long diskSize;
    /**
     * 用户客户端指定这个shard的大小；
     */
    private long blockSize;
    
    /**
     * 是否是最后一批建索引的数据
     */
    private boolean lastPiece;
    
    /**
     * 索引建成后，返回已建索引数据最大ID
     */
    private BigDecimal maxId;
    
    /**
     * 用于表示ID字段名称
     */
    private String idName;
    
    /**
     * 构造函数
     */
    public IndexMessage() {
        super();
        
    }
    
    /**
     * @param messageHeader
     */
    public IndexMessage(MessageHeader messageHeader) {
        super(messageHeader);
    }
    
    public IndexMessage(MessageHeader messageHeader, TesseractResultSet dataBody) {
        super(messageHeader);
        this.dataBody = dataBody;
        this.messageHeader.setMd5sum(Md5Util.encode(this.dataBody.toString()));
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
     * getMessageBody()
     */
    @Override
    public Serializable getMessageBody() {
        return this.dataBody;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
     * getMessageBodyMd5sum()
     */
    @Override
    public String getMessageBodyMd5sum() {
        return Md5Util.encode(this.dataBody.toString());
    }
    
    /**
     * getter method for property idxPath
     * 
     * @return the idxPath
     */
    public String getIdxPath() {
        return idxPath;
    }
    
    /**
     * setter method for property idxPath
     * 
     * @param idxPath
     *            the idxPath to set
     */
    public void setIdxPath(String idxPath) {
        this.idxPath = idxPath;
    }
    
    /**
     * getter method for property dataBody
     * 
     * @return the dataBody
     */
    public TesseractResultSet getDataBody() {
        return dataBody;
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
     * getter method for property blockSize
     * 
     * @return the blockSize
     */
    public long getBlockSize() {
        return blockSize;
    }
    
    /**
     * setter method for property blockSize
     * 
     * @param blockSize
     *            the blockSize to set
     */
    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }
    
    /**
     * getter method for property idxServicePath
     * 
     * @return the idxServicePath
     */
    public String getIdxServicePath() {
        return idxServicePath;
    }
    
    /**
     * setter method for property idxServicePath
     * 
     * @param idxServicePath
     *            the idxServicePath to set
     */
    public void setIdxServicePath(String idxServicePath) {
        this.idxServicePath = idxServicePath;
    }
    
    /**
     * setter method for property dataBody
     * 
     * @param dataBody
     *            the dataBody to set
     */
    public void setDataBody(TesseractResultSet dataBody) {
        this.dataBody = dataBody;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IndexMessage [idxPath=" + idxPath + ", idxServicePath=" + idxServicePath
            + ", dataBody=" + dataBody + ", diskSize=" + diskSize + ", blockSize=" + blockSize
            + ", lastPiece=" + lastPiece + "]";
    }
    
    /**
     * getter method for property lastPiece
     * 
     * @return the lastPiece
     */
    public boolean isLastPiece() {
        return lastPiece;
    }
    
    /**
     * setter method for property lastPiece
     * 
     * @param lastPiece
     *            the lastPiece to set
     */
    public void setLastPiece(boolean lastPiece) {
        this.lastPiece = lastPiece;
    }

    /**
     * getter method for property maxId
     * @return the maxId
     */
    public BigDecimal getMaxId() {
        return maxId;
    }

    /**
     * setter method for property maxId
     * @param maxId the maxId to set
     */
    public void setMaxId(BigDecimal maxId) {
        this.maxId = maxId;
    }

    /**
     * getter method for property idName
     * @return the idName
     */
    public String getIdName() {
        return idName;
    }

    /**
     * setter method for property idName
     * @param idName the idName to set
     */
    public void setIdName(String idName) {
        this.idName = idName;
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
    
    
}
