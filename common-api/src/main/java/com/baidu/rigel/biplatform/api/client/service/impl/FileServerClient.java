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
package com.baidu.rigel.biplatform.api.client.service.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ma.common.file.protocol.Request;
import com.baidu.rigel.biplatform.ma.common.file.protocol.Response;
import com.baidu.rigel.biplatform.ma.common.file.protocol.ResponseStatus;

/**
 * 文件服务客户端
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public final class FileServerClient {

    /**
     * FileServerClient
     */
    private static final FileServerClient INSTANCE = new FileServerClient();

    /**
     * 日志记录器
     */
    private Logger logger = LoggerFactory.getLogger(FileServerClient.class);

    /**
     * 构造函数
     */
    private FileServerClient() {

    }

    /**
     * 获取请求
     * 
     * @param server 服务器地址
     * @param port 服务器端口
     * @param request 请求对象
     * @return 请求结果
     */
    public Response doRequest(String server, int port, final Request request) {
        EventLoopGroup work = new NioEventLoopGroup(1);
        logger.info("request info =====: " + request);
        // String message = null;
        ChannelFuture future = null;
        try {
            final Response rs = new Response(ResponseStatus.FAIL, "failed", null);
            ChannelHandlerAdapter requestHandler = new ChannelInboundHandlerAdapter() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    logger.info("successfully connect to file server");
                    ctx.write(request);
                    ctx.flush();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    logger.info("successfuly recieve message from file server {}", msg);
                    Response tmpRs = (Response) msg;
                    rs.setDatas(tmpRs.getDatas());
                    rs.setMessage(tmpRs.getMessage());
                    rs.setStatus(tmpRs.getStatus());
                    ctx.flush();
                    ctx.close();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                    ctx.flush();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    logger.error(cause.getMessage());
                    rs.setMessage(cause.getMessage());
                    rs.setStatus(ResponseStatus.FAIL);
                    ctx.close();
                }
            };
            Bootstrap strap = new Bootstrap();
            strap.group(work).option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {

                        @Override
                        protected void initChannel(NioSocketChannel chl) throws Exception {
                            // 对象序列化解码器
                            chl.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(requestHandler.getClass()
                                            .getClassLoader())));
                            chl.pipeline().addLast(new ObjectEncoder());
                            chl.pipeline().addLast(requestHandler);
                        }

                    });
            long begin = System.currentTimeMillis();
            logger.debug("Begin invoke do file operation request ... ...");
            future = strap.connect(server, port);
            future.channel().closeFuture().sync();
            logger.debug("Success execute request option cost time: " + (System.currentTimeMillis() - begin) + "ms");
            return rs;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // message = e.getMessage();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (future != null) {
                future.channel().disconnect();
            }
            work.shutdownGracefully();
        }
    }

    public static FileServerClient newInstance() {
        return INSTANCE;
    }

}
