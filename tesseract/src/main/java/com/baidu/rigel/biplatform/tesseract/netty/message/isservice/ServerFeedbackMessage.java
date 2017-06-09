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
package com.baidu.rigel.biplatform.tesseract.netty.message.isservice;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;

/**
 * ServerFeedbackMessage
 * 
 * @author lijin
 *
 */
public class ServerFeedbackMessage extends AbstractMessage {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = -2286755143267706248L;
    /**
     * 结果字符串
     */
    private String result;
    /**
     * 返回的消息
     */
    private String msg;
    
    /**
     * 消息集合
     */
    private StringBuilder msgAll;
    
    /**
     * 返回消息的节点key
     */
    private String nodeKey;
    
    /**
     * Constructor by
     * 
     * @param messageHeader
     * @param result
     * @param msg
     */
    public ServerFeedbackMessage(MessageHeader messageHeader, String result, String msg) {
        super(messageHeader);
        this.result = result;
        this.msg = msg;
        msgAll = new StringBuilder();
        msgAll.append(String.format(TesseractConstant.FEED_BACK_MSG_PATTERN_MSG_ALL, this.result,
                this.msg));
        
        this.messageHeader.setMd5sum(Md5Util.encode(this.msgAll.toString()));
    }
    
    @Override
    public Serializable getMessageBody() {
        return msgAll.toString();
    }
    
    @Override
    public String getMessageBodyMd5sum() {
        if (this.msgAll != null) {
            return Md5Util.encode(this.msgAll.toString());
        }
        return null;
    }
    
    /**
     * getter method for property result
     * 
     * @return the result
     */
    public String getResult() {
        return result;
    }
    
    /**
     * setter method for property result
     * 
     * @param result
     *            the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * getter method for property msg
     * 
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * setter method for property msg
     * 
     * @param msg
     *            the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
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
    
    
    
}
