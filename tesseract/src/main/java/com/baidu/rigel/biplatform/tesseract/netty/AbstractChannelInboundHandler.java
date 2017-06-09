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
package com.baidu.rigel.biplatform.tesseract.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.baidu.rigel.biplatform.tesseract.netty.exception.MessageDamagedException;
import com.baidu.rigel.biplatform.tesseract.netty.exception.MessageHeaderNullException;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerExceptionMessage;

/**
 * AbstractChannelInboundHandler 抽像hanlder
 * 
 * @author lijin
 *
 */
public abstract class AbstractChannelInboundHandler extends ChannelInboundHandlerAdapter {
    
    /**
     * 支持的操作类型
     */
    private NettyAction supportedAction;
    /**
     * 返回的操作类型
     */
    private NettyAction feedBackAction;
    
    /**
     * 
     * Constructor by no param
     */
    public AbstractChannelInboundHandler() {
        super();
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param supportedAction
     *            supportedAction
     */
    public AbstractChannelInboundHandler(NettyAction supportedAction, NettyAction feedBackAction) {
        super();
        this.supportedAction = supportedAction;
        this.feedBackAction = feedBackAction;
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param feedBackAction
     */
    public AbstractChannelInboundHandler(NettyAction feedBackAction) {
        super();
        this.feedBackAction = feedBackAction;
    }
    
    /**
     * 
     * support 判断是否支持action
     * 
     * @param action
     *            操作类型
     * @return boolean
     */
    public boolean support(NettyAction action) {
        if (this.supportedAction.equals(action)) {
            return true;
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel
     * .ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AbstractMessage message = (AbstractMessage) msg;
        if (message.getMessageHeader() == null) {
            exceptionCaught(ctx, new MessageHeaderNullException());
            return;
        } else if (!message.md5Check()) {
            exceptionCaught(ctx, new MessageDamagedException(message.toString()));
            return;
        }
        if (this.support(message.getMessageHeader().getAction())) {
            this.messageReceived(ctx, msg);
            
        } else {
            ctx.fireChannelRead(msg);
        }
    }
    
    /**
     * 
     * messageReceived 对接收的消息进行处理
     * 
     * @param ctx
     *            ChannelHandlerContext
     * @param msg
     *            Object
     * @throws Exception
     *             消息处理过程中可能会抛出异常
     */
    public abstract void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception;
    
    /**
     * 
     * getMessage
     * 
     * @return AbstractMessage
     */
    public abstract <T extends AbstractMessage> T getMessage();
    
    /**
     * setMessage
     * @param t
     */
    public abstract <T extends AbstractMessage> void setMessage(T t);
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty
     * .channel.ChannelHandlerContext, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        
        super.exceptionCaught(ctx, cause);
        MessageHeader mh = new MessageHeader(feedBackAction);
        ServerExceptionMessage severExceptionMessage = new ServerExceptionMessage(mh, cause,
                "Exception caught in Server");
        ctx.writeAndFlush(severExceptionMessage);
        ctx.close();
    }
    
}
