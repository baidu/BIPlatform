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
 * ServerExceptionMessage
 * @author lijin
 *
 */
public class ServerExceptionMessage extends AbstractMessage {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = 1877530453448318617L;

    /**
     * 消息主体
     */
    private Throwable dataBody;
    
    /**
     * 异常信息
     */
    private String exceptionMessage;
    
    /**
     * Constructor by 
     * @param messageHeader
     */
    public ServerExceptionMessage(MessageHeader messageHeader,Throwable cause,String msg) {
        super(messageHeader);
        this.dataBody=cause;
        this.exceptionMessage=msg;
        this.messageHeader.setMd5sum(Md5Util.encode(cause.toString()));
        
    }

    /**
     * getter method for property dataBody
     * @return the dataBody
     */
    public Serializable getDataBody() {
        return dataBody;
    }
    
    public Throwable getCause(){
        return this.dataBody;
    }

    /**
     * setter method for property dataBody
     * @param dataBody the dataBody to set
     */
    public void setDataBody(Throwable dataBody) {
        this.dataBody = dataBody;
    }

    /**
     * getter method for property exceptionMessage
     * @return the exceptionMessage
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * setter method for property exceptionMessage
     * @param exceptionMessage the exceptionMessage to set
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#getMessageBody()
     */
    @Override
    public Serializable getMessageBody() {
        return this.dataBody;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#getMessageBodyMd5sum()
     */
    @Override
    public String getMessageBodyMd5sum() {
        if(this.dataBody!=null){
            return Md5Util.encode(this.dataBody.toString());
        }
        return null;
    }
    
    
    
}
