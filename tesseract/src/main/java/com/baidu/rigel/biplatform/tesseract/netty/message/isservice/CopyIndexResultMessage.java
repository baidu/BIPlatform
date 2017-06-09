/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.netty.message.isservice;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;

/**
 * @author lijin
 *
 */
public class CopyIndexResultMessage extends AbstractMessage {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 4576199585664468816L;
	
	/**
	 * 索引分片名
	 */
	private String shardName;
	/**
	 * 成功的节点列表
	 */
	private List<String> succList;

	/**
	 * constructor
	 * 
	 * @param messageHeader
	 * @param succList
	 * @param failList
	 * 
	 */
	public CopyIndexResultMessage(String shardName,MessageHeader messageHeader,
			List<String> succList) {
		super(messageHeader);
		this.shardName=shardName;
		this.succList = succList;
		messageHeader.setMd5sum(Md5Util.encode(succList.toString()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
	 * getMessageBody()
	 */
	@Override
	public Serializable getMessageBody() {
		return (Serializable) succList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
	 * getMessageBodyMd5sum()
	 */
	@Override
	public String getMessageBodyMd5sum() {
		return Md5Util.encode(this.succList.toString());
	}

	/**
	 * @return the shardName
	 */
	public String getShardName() {
		return shardName;
	}

	/**
	 * @return the succList
	 */
	public List<String> getSuccList() {
		return succList;
	}
	
	
}
