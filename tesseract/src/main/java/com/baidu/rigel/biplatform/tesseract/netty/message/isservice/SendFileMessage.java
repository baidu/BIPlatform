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

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;

/**
 * SendFileMessage
 * 
 * @author lijin
 *
 */
public class SendFileMessage extends AbstractMessage {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = 5485286046850992147L;
    /**
     * idx
     */
    private int idx;
    /**
     * 当前发送的字节
     */
    private byte[] content;
    /**
     * 目标路径
     */
    private String targetFilePath;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 是否是最后一片
     */
    private boolean isLast;
    
    /**
     * 是否是第一片
     */
    private boolean isFirst;
    
    /**
     * Constructor by
     * 
     * @param messageHeader
     */
    public SendFileMessage(MessageHeader messageHeader, byte[] content, String targetFilePath , String fileName) {
        super(messageHeader);
        this.content = content;
        this.targetFilePath = targetFilePath;
        this.fileName=fileName;
        this.messageHeader.setMd5sum("1");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
     * getMessageBody()
     */
    @Override
    public Serializable getMessageBody() {
        
        return this.content;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
     * getMessageBodyMd5sum()
     */
    @Override
    public String getMessageBodyMd5sum() {
        if (this.content != null) {
            return "1";
        }
        return null;
    }
    
    /**
     * getter method for property content
     * 
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }
    
    /**
     * setter method for property content
     * 
     * @param content
     *            the content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    /**
     * getter method for property targetFilePath
     * 
     * @return the targetFilePath
     */
    public String getTargetFilePath() {
        return targetFilePath;
    }
    
    /**
     * setter method for property targetFilePath
     * 
     * @param targetFilePath
     *            the targetFilePath to set
     */
    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }
    
    /**
     * getter method for property isLast
     * 
     * @return the isLast
     */
    public boolean isLast() {
        return isLast;
    }
    
    /**
     * setter method for property isLast
     * 
     * @param isLast
     *            the isLast to set
     */
    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }
    
    /**
     * getter method for property isFirst
     * 
     * @return the isFirst
     */
    public boolean isFirst() {
        return isFirst;
    }
    
    /**
     * setter method for property isFirst
     * 
     * @param isFirst
     *            the isFirst to set
     */
    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

	/**
	 * @return the idx
	 */
	public int getIdx() {
		return idx;
	}

	/**
	 * @param idx the idx to set
	 */
	public void setIdx(int idx) {
		this.idx = idx;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
    
    
}
