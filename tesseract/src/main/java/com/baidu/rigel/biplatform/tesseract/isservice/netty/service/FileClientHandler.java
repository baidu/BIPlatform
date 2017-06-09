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
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelHandlerContext;

import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerExceptionMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;

/**
 * FileClientHandler
 * 
 * @author lijin
 *
 */
public class FileClientHandler extends AbstractChannelInboundHandler {
    
    /**
     * 支持的action
     */
    private static final NettyAction ACTION_SUPPORT = null;//NettyAction.NETTY_ACTION_COPYFILE_FEEDBACK;
    /**
     * 返回消息的action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_NULL;
    
    private AbstractMessage message;
    
    /**
     * Constructor by
     */
    public FileClientHandler() {
        super(ACTION_SUPPORT, ACTION_FEEDBACK);
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #messageReceived(io.netty.channel.ChannelHandlerContext,
     * java.lang.Object)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ServerFeedbackMessage) {
            message = (ServerFeedbackMessage) msg;
        } else {
            message = (ServerExceptionMessage) msg;
        }
        ctx.channel().close();
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #getMessage()
     */
    @SuppressWarnings("unchecked")
    @Override
    public AbstractMessage getMessage() {
        return message;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#setMessage(com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage)
     */
    @Override
    public <T extends AbstractMessage> void setMessage(T t) {
        this.message=t;
        
    }
    
    
    
}
