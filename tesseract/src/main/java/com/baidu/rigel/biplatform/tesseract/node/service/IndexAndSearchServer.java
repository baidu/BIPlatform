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
package com.baidu.rigel.biplatform.tesseract.node.service;

import java.net.InetAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.CopyIndexInfoReturnHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.CopyIndexServerHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.FileServerHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.IndexServerHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.SearchServerHandler;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.util.NetworkUtils;

@Service("isServer")
public class IndexAndSearchServer {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexAndSearchServer.class);
   
    
    /**
     * BIZGROUPSIZE
     */
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * BIZTHREADSIZE
     */
    protected static final int BIZTHREADSIZE = (int)(Runtime.getRuntime().availableProcessors() * 6) ;
    /**
     * bossGroup
     */
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    /**
     * workerGroup
     */
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);
    /**
     * isRunning
     */
    private boolean isRunning;
    
    /**
     * serverChannelFuture
     */
    private ChannelFuture serverChannelFuture;
    
    /**
     * serverThread
     */
    private Thread serverThread;
    
    /**
     * node
     */
    @Resource
    private Node node;
    
    /**
     * Constructor by
     * 
     * @param isRunning
     */
    public IndexAndSearchServer() {
        super();
        this.isRunning = false;
        serverThread = new Thread(new IndexAndSearchServerRunner());
        LOGGER.info("Index and Search server finish init...");
        
    }
    
    /**
     * 启动
     * 
     */
    public void start() {
        serverThread.start();
    }
    
    /**
     * IndexAndSearchServerRunner
     * 
     * @author lijin
     *
     */
    class IndexAndSearchServerRunner implements Runnable {
        
        @Override
        public void run() {
            LOGGER.info("Index and Search server call start...");
            try {
                startServer();
            } catch (Exception e) {
                new RuntimeException(e.getCause());
            }
            
        }
        
    }
    
    /**
     * startServer
     * 
     * @throws Exception
     */
    protected void startServer() throws Exception {
        LOGGER.info("Index and Search server ready to start...");
        LOGGER.info("Server has {0} bossGroup and {1} workerGroup",BIZGROUPSIZE,BIZTHREADSIZE);
        
        LOGGER.info("Server has "+BIZGROUPSIZE+" bossGroup and "+BIZTHREADSIZE+" workerGroup");
        
        long curr = System.currentTimeMillis();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.SO_BACKLOG, 1000000);
          
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                
                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * io.netty.channel.ChannelInitializer#initChannel(io.netty.
                 * channel.Channel)
                 */
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("encode", new ObjectEncoder());
                    pipeline.addLast("decode",
                        new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.weakCachingConcurrentResolver(null)));
                    pipeline.addLast(IndexServerHandler.getChannelHandler());
                    pipeline.addLast(SearchServerHandler.getChannelHandler());
                    pipeline.addLast(FileServerHandler.getChannelHandler());
                    pipeline.addLast(CopyIndexServerHandler.getChannelHandler());
                    pipeline.addLast(CopyIndexInfoReturnHandler.getChannelHandler());
                    
                }
                
            });
            
            // ChannelFuture f = b.bind(IP, PORT).sync();
            // f.channel().closeFuture().sync();
            
            int currPort = NetworkUtils.getAvailablePort(this.node.getPort());
            String hostIp=InetAddress.getLocalHost().getHostAddress();
            ChannelFuture f = b.bind(hostIp, currPort).sync();
            
            if (currPort != this.node.getPort()) {
                this.node.setPort(currPort);
            }
            
            serverChannelFuture = f;
            LOGGER.info("Index and Search server started at Port:" + this.node.getPort());
            LOGGER.info("Index and Search server started in " + (System.currentTimeMillis() - curr)
                + "ms");
            this.isRunning = true;
            
            serverChannelFuture.channel().closeFuture().sync(); //.channel();
            
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    @PreDestroy
    public void stop() {
        serverChannelFuture.channel().close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        while(true){
            if(NetworkUtils.isPortAvailable(this.node.getPort())){
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.isRunning = false;
        LOGGER.info("Index and Search server stoped");
    }
    
    /**
     * getter method for property isRunning
     * 
     * @return the isRunning
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * getter method for property node
     * 
     * @return the node
     */
    public Node getNode() {
        return node;
    }
    
    /**
     * setter method for property node
     * 
     * @param node
     *            the node to set
     */
    public void setNode(Node node) {
        this.node = node;
    }
    
}
