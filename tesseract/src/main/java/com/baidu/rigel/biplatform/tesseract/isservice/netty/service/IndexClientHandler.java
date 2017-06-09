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
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.IndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerExceptionMessage;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * 
 * IndexClientHandler
 * 处理NETTY_ACTION_INDEX_FEEDBACK，即发需要建索引的数据给server，自己接收server处理的结果
 * 
 * @author lijin
 *
 */
public class IndexClientHandler extends AbstractChannelInboundHandler {
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(IndexClientHandler.class);
    /**
     * 当前handler支持的操作NETTY_ACTION_INDEX_FEEDBACK
     */
    private static final NettyAction ACTION_SUPPORT = NettyAction.NETTY_ACTION_INDEX_FEEDBACK;
    /**
     * 当前返回消息的action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_NULL;
    /**
     * 用于接收server处理的结果
     */
    private volatile AbstractMessage message;
    
    /**
     * 
     * Constructor by no param
     */
    public IndexClientHandler() {
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
        logger.info(String.format(LogInfoConstants.INFO_PATTERN_MESSAGE_RECEIVED_BEGIN,
            "IndexClientHandler"));
        logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
                "IndexClientHandler",msg,"messageReceived"));
        if (msg instanceof IndexMessage) {
            message = (AbstractMessage)msg ;
        } else {
            message = (ServerExceptionMessage)msg; 
        }
        
        logger.info(String.format(LogInfoConstants.INFO_PATTERN_MESSAGE_RECEIVED_END,
            "IndexClientHandler"));
        ctx.channel().close();
    }
    
    
    
    /* (non-Javadoc)
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        
        super.channelReadComplete(ctx);
//        logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
//                "IndexClientHandler",this.message,"read complete"));
        
    }

    /**
     * getter method for property message
     * 
     * @return the message
     */
    @SuppressWarnings("unchecked")
    public AbstractMessage getMessage() {
        
        return message;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#setMessage(com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage)
     */
    @Override
    public <T extends AbstractMessage> void setMessage(T t) {
        this.message=(AbstractMessage)t ;
        
    }
    
}
