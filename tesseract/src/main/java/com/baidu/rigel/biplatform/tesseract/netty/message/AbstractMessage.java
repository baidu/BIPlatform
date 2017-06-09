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

import org.apache.commons.lang.StringUtils;

/**
 * 
 * AbstractMessage
 * 
 * @author lijin
 *
 */
public abstract class AbstractMessage implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2167332504234686219L;
    
    /**
     * messageHeader
     */
    protected MessageHeader messageHeader;
    
    /**
     * getter method for property messageHeader
     * 
     * @return the messageHeader
     */
    public MessageHeader getMessageHeader() {
        return messageHeader;
    }
    
    /**
     * setter method for property messageHeader
     * 
     * @param messageHeader
     *            the messageHeader to set
     */
    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }
    
    /**
     * @param messageHeader
     */
    public AbstractMessage(MessageHeader messageHeader) {
        super();
        this.messageHeader = messageHeader;
    }
    
    /**
     * 
     * Constructor by no param
     */
    public AbstractMessage() {
        super();
    }
    
    /**
     * 
     * md5检查
     * 
     * @return
     */
    public boolean md5Check() {
        if (this.messageHeader != null && !StringUtils.isEmpty(this.getMessageBodyMd5sum())
                && !StringUtils.isEmpty(this.messageHeader.getMd5sum())) {
            return this.getMessageBodyMd5sum().equals(this.messageHeader.getMd5sum());
        }
        return false;
    }
    
    /**
     * 
     * 获取数据主体
     * 
     * @return
     */
    public abstract Serializable getMessageBody();
    
    /**
     * 
     * 获取数据主体的md5
     * 
     * @return
     */
    public abstract String getMessageBodyMd5sum();
    
}
