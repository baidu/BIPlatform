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
import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;

/**
 * SearchResultMessage
 * 
 * @author lijin
 *
 */
public class SearchResultMessage extends AbstractMessage {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6590179154429815062L;
    /**
     * 数据主体
     */
    private TesseractResultSet dataBody;
    
    /**
     * 
     * Constructor by no param
     */
    public SearchResultMessage() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * Constructor by 
     * @param messageHeader messageHeader
     */
    public SearchResultMessage(MessageHeader messageHeader) {
        super(messageHeader);
    }
    
    /**
     * 
     * Constructor by 
     * @param messageHeader messageHeader
     * @param data data
     */
    public SearchResultMessage(MessageHeader messageHeader, TesseractResultSet data) {
        super(messageHeader);
        this.dataBody = data;
        super.getMessageHeader().setMd5sum(Md5Util.encode(dataBody.toString()));
        
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage#
     * getMessageBody()
     */
    @Override
    public Serializable getMessageBody() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return Md5Util.encode(this.dataBody.toString());
    }
    
}
