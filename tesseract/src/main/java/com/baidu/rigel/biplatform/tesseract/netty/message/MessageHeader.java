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
package com.baidu.rigel.biplatform.tesseract.netty.message;

import java.io.Serializable;

import com.baidu.rigel.biplatform.tesseract.node.meta.Node;

/**
 * MessageHeader 消息头
 * 
 * @author lijin
 *
 */
public class MessageHeader implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2453018368081583989L;
    
    /**
     * action
     */
    private NettyAction action;
    /**
     * 消息源信息
     */
    private Node fromNode;
    /**
     * md5sum
     */
    private String md5sum;
    
    /**
     * @param action
     */
    public MessageHeader(NettyAction action) {
        super();
        this.action = action;
    }
    
    /**
     * @param action
     * @param md5sum
     */
    public MessageHeader(NettyAction action, String md5sum) {
        super();
        this.action = action;
        this.md5sum = md5sum;
    }
    
    /**
     * getter method for property action
     * 
     * @return the action
     */
    public NettyAction getAction() {
        return action;
    }
    
    /**
     * setter method for property action
     * 
     * @param action
     *            the action to set
     */
    public void setAction(NettyAction action) {
        this.action = action;
    }
    
    /**
     * getter method for property md5sum
     * 
     * @return the md5sum
     */
    public String getMd5sum() {
        return md5sum;
    }
    
    /**
     * setter method for property md5sum
     * 
     * @param md5sum
     *            the md5sum to set
     */
    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MessageHeader [action=" + action + ", md5sum=" + md5sum + "]";
    }

	/**
	 * @return the fromNode
	 */
	public Node getFromNode() {
		return fromNode;
	}

	/**
	 * @param fromNode the fromNode to set
	 */
	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}
    
    
    
}
