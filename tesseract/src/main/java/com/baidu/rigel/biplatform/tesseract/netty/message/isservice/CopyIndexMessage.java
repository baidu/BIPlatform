/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.netty.message.isservice;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;

/**
 * @author lijin
 *
 */
public class CopyIndexMessage extends AbstractMessage {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6963351782008485545L;
	/**
	 * shardName
	 */
	private String shardName;

	/**
	 * 原始文件
	 */
	private String srcFilePath;
	/**
	 * 目标文件
	 */
	private String targetFilePath;
	/**
	 * 待拷贝的节点列表
	 */
	private List<Node> toNodeList;

	/**
	 * constructor
	 * 
	 * @param messageHeader
	 */
	public CopyIndexMessage(String shardName,MessageHeader messageHeader, String srcFilePath,
			String targetFilePath, List<Node> toNodeList) {
		super(messageHeader);
		this.shardName=shardName;
		this.srcFilePath = srcFilePath;
		this.targetFilePath = targetFilePath;
		this.toNodeList = toNodeList;
		messageHeader.setMd5sum(Md5Util.encode(this.shardName+this.srcFilePath
				+ this.targetFilePath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
	 * getMessageBody()
	 */
	@Override
	public Serializable getMessageBody() {

		return this.srcFilePath + this.targetFilePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
	 * getMessageBodyMd5sum()
	 */
	@Override
	public String getMessageBodyMd5sum() {
		return Md5Util.encode(this.shardName+this.srcFilePath + this.targetFilePath);
	}

	/**
	 * @return the srcFilePath
	 */
	public String getSrcFilePath() {
		return srcFilePath;
	}

	/**
	 * @return the targetFilePath
	 */
	public String getTargetFilePath() {
		return targetFilePath;
	}

	/**
	 * @return the toNodeList
	 */
	public List<Node> getToNodeList() {
		return toNodeList;
	}

	/**
	 * @return the shardName
	 */
	public String getShardName() {
		return shardName;
	}
	
	

}
