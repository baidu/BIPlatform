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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.Md5Util;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.tesseract.config.IndexConfig;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexAction;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.IndexClientHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.SearchClientHandler;
import com.baidu.rigel.biplatform.tesseract.isservice.netty.service.ServerFeedBackClientHandler;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.CopyIndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.CopyIndexResultMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.IndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SearchRequestMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SearchResultMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SendFileMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerExceptionMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexAndSearchClient 
 * 
 * @author lijin
 *
 */
public class IndexAndSearchClient {
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(IndexAndSearchClient.class);
    /**
     * Netty客户端
     */
    private Bootstrap b;
    /**
     * group
     */
    private EventLoopGroup group;
    /**
     * INSTANCE
     */
    private static IndexAndSearchClient INSTANCE;
    
    
    
    /**
     * 私有构造函数
     */
    private IndexAndSearchClient() {
        b = new Bootstrap();
        group = new NioEventLoopGroup();
        b.group(group);
        b.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("encode", new ObjectEncoder());
                pipeline.addLast("decode",
                    new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.weakCachingConcurrentResolver(null)));
            }
        });
        
       	
        logger.info("IndexAndSearchClient init finished");
    }
    
    
    
    /**
     * getNodeClient
     * @return IndexAndSearchClient的实例
     */
    public static synchronized IndexAndSearchClient getNodeClient() {
        if (INSTANCE == null) {
            INSTANCE = new IndexAndSearchClient();
        }
        
        return INSTANCE;
    }
    
    /**
     * NodeAddress 内部类
     * @author lijin
     *
     */
    public class NodeAddress {
    	/**
    	 * ip
    	 */
        private String ip;
        /**
         * 端口
         */
        private int port;
        
        /**
         * 构造函数
         * @param ip ip
         * @param port port
         */
        public NodeAddress(String ip, int port) {
            super();
            this.ip = ip;
            this.port = port;
        }
        
        /**
         * getter method for property ip
         * 
         * @return the ip
         */
        public String getIp() {
            return ip;
        }
        
        /**
         * setter method for property ip
         * 
         * @param ip
         *            the ip to set
         */
        public void setIp(String ip) {
            this.ip = ip;
        }
        
        /**
         * getter method for property port
         * 
         * @return the port
         */
        public int getPort() {
            return port;
        }
        
        /**
         * setter method for property port
         * 
         * @param port
         *            the port to set
         */
        public void setPort(int port) {
            this.port = port;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((ip == null) ? 0 : ip.hashCode());
            result = prime * result + port;
            return result;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof NodeAddress)) {
                return false;
            }
            NodeAddress other = (NodeAddress) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (ip == null) {
                if (other.ip != null) {
                    return false;
                }
            } else if (!ip.equals(other.ip)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            return true;
        }
        
        /**
         * getOuterType
         * @return 外部类：IndexAndSearchClient
         */
        private IndexAndSearchClient getOuterType() {
            return IndexAndSearchClient.this;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "NodeAddress [ip=" + ip + ", port=" + port + "]";
        }
        
    }
    
    /**
     * 获取指定IP及端口的连接channel
     * @param ipAddress 要连接的ip
     * @param port 要连接的端口
     * @return Channel 
     * @throws IndexAndSearchException
     */
    public Channel getChannelByAddressAndPort(String ipAddress, int port)
        throws IndexAndSearchException {
        logger.info("getChannelByAddressAndPort:[address=" + ipAddress + "][port=" + port
            + "] start");
        long currTime=System.currentTimeMillis();
        if (StringUtils.isEmpty(ipAddress) || port <= 0) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.ILLEGALARGUMENT_EXCEPTION),
                IndexAndSearchExceptionType.ILLEGALARGUMENT_EXCEPTION);
        }
        Channel channel = null;
        String address = ipAddress;
        
        try {
            if (b != null) {
                channel = b.connect(address, port).sync().channel();
                logger.info("getChannelByAddressAndPort:connect server success [address=" + address
                    + "][port=" + port + "]");
            }
            
        } catch (Exception e) {
            logger.error("getChannelByAddressAndPort:connect server [address=" + address
                + "][port=" + port + "] exception.", e);
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION), e,
                IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
        System.out.println("getChannelByAddressAndPort cost : "+ (System.currentTimeMillis()-currTime)+" ms");
        logger.info("getChannelByAddressAndPort:[address=" + address + "][port=" + port
            + "] connect sucess");
        return channel;
        
    }    

    /**
     * index 索引请求
     * @param data data数据
     * @param idxAction 动作
     * @param idxShard 分片
     * @param idName 数据的主键字段名
     * @param lastPiece 是否为最后一块数据
     * @return IndexMessage
     * @throws IndexAndSearchException
     */
    public IndexMessage index(TesseractResultSet data, IndexAction idxAction, IndexShard idxShard,Node node,
        String idName, boolean lastPiece) throws IndexAndSearchException {
    	
    	IndexConfig indexConfig=(IndexConfig)ApplicationContextHelper.getContext().getBean("indexConfig");
    	
    	
        logger.info("index:[data=" + data + "][idxAction=" + idxAction + "][idxShard=" + idxShard
            + "][idName:" + idName + "] start");
        if (data == null || idxShard == null || StringUtils.isEmpty(idxShard.getFilePath())
                || StringUtils.isEmpty(idxShard.getIdxFilePath())) {
            throw new IllegalArgumentException();
        }
        
		NettyAction action = null;
//		if (idxAction.equals(IndexAction.INDEX_UPDATE)) {
//			action = NettyAction.NETTY_ACTION_UPDATE;
//		} else if (idxAction.equals(IndexAction.INDEX_MOD)) {
//			action = NettyAction.NETTY_ACTION_MOD;
//		} else if (idxAction.equals(IndexAction.INDEX_MERGE)
//				    || idxAction.equals(IndexAction.INDEX_INIT)
//				    || idxAction.equals(IndexAction.INDEX_INIT_LIMITED)) {
//			action = NettyAction.NETTY_ACTION_INITINDEX;
//		} else {
//			action = NettyAction.NETTY_ACTION_INDEX;
//		}
//        MessageHeader messageHeader = new MessageHeader(action, data.toString());
//        IndexMessage message = new IndexMessage(messageHeader, data);
//        message.setIdxPath(idxShard.getAbsoluteFilePath(node.getIndexBaseDir()));
//        message.setIdxServicePath(idxShard.getAbsoluteIdxFilePath(node.getIndexBaseDir()));
//        message.setBlockSize(indexConfig.getIdxShardSize());
//        message.setIdName(idName);
//        message.setLastPiece(lastPiece);
		
		if(idxAction.equals(IndexAction.INDEX_INDEX) || idxAction.equals(IndexAction.INDEX_MERGE)){
			action = NettyAction.NETTY_ACTION_INDEX;
		}else if(idxAction.equals(IndexAction.INDEX_MOD)){
			action = NettyAction.NETTY_ACTION_MOD;
		}
		
		
		
		
		
        MessageHeader messageHeader = new MessageHeader(action, data.toString());
        IndexMessage message = new IndexMessage(messageHeader, data);
        message.setIdxPath(idxShard.getAbsoluteFilePath(node.getIndexBaseDir()));
        message.setIdxServicePath(idxShard.getAbsoluteIdxFilePath(node.getIndexBaseDir()));
        message.setBlockSize(indexConfig.getIdxShardSize());
        message.setIdName(idName);
        message.setLastPiece(lastPiece);
        /**
         * 增加索引分片状态 Jin 20150513
         */
        message.setIdxShardState(idxShard.getIdxShardState());

        logger.info("ready to send index message:" + message.toString());
        AbstractMessage ret = null;
        IndexMessage result = null;
        IndexClientHandler handler = new IndexClientHandler();
        try {
            ret = this.executeAction(action, message, handler, node);
            if (ret instanceof IndexMessage) {
                result = (IndexMessage) ret;
            } else {
                throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                    IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                    IndexAndSearchExceptionType.INDEX_EXCEPTION),
                    ((ServerExceptionMessage) ret).getCause(),
                        IndexAndSearchExceptionType.INDEX_EXCEPTION);
            }
        } catch (Exception e) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION), e,
                IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
        logger.info("index:[data=" + data + "][idxAction=" + idxAction + "][idxShard=" + idxShard
            + "] finished index ");
        return result;
    }
    
    /**
     * getIndexFileCopyTmpDirPath 
     * @param filePath
     * @return
     */
    private String getIndexFileCopyTmpDirPath(String filePath){
    	StringBuffer sb=new StringBuffer();
    	if(!StringUtils.isEmpty(filePath)){
    		sb.append(filePath.substring(0, filePath.indexOf("indexbase")));
    		sb.append("indexbase");
    		sb.append(File.separator);
    		sb.append("copytmp");
    		sb.append(File.separator);
    	}
    	return sb.toString();
    }
    
    public ServerFeedbackMessage startIndexDataCopy(String shardName,String srcFilePath , String targetFilePath, Node fromNode,List<Node> toNodeList) throws Exception{
		logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"startIndexDataCopy", "[srcFilePath:" + srcFilePath
						+ "][targetFilePath:" + targetFilePath + "][fromNode:" + fromNode + "][toNodeList:"+toNodeList+"]"));

		if(StringUtils.isEmpty(srcFilePath) || StringUtils.isEmpty(targetFilePath) || fromNode == null || CollectionUtils.isEmpty(toNodeList)){
			logger.warn(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
	                "startIndexDataCopy", "[srcFilePath:" + srcFilePath
					+ "][targetFilePath:" + targetFilePath + "][fromNode:" + fromNode + "][toNodeList:"+toNodeList+"]"));
	            throw new IllegalArgumentException();
		}
		NettyAction action=NettyAction.NETTY_ACTION_START_COPYINDEX;
		MessageHeader mh=new MessageHeader(action);
		CopyIndexMessage cim=new CopyIndexMessage(shardName,mh,srcFilePath,targetFilePath,toNodeList);
		ServerFeedBackClientHandler handler=new ServerFeedBackClientHandler();
		AbstractMessage bMessage=null;
		ServerFeedbackMessage backMessage = null;
		try {
			bMessage = this.executeAction(action, cim, handler, fromNode);			
		} catch (Exception e) {
			logger.error("startIndexDataCopy Exception", e.getCause());
			throw e;
		}
		
		if (bMessage instanceof ServerFeedbackMessage) {
            backMessage = (ServerFeedbackMessage) bMessage;
        } else {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION),
                ((ServerExceptionMessage) bMessage).getCause(),
                    IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
		
		return backMessage;
    }
    
    
    public ServerFeedbackMessage returnIndexDataCopyInfo(String shardName,List<String> succList,Node node) throws Exception{
		logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"returnIndexDataCopyInfo", "[shardName:" + shardName
						+ "][succList:" + succList + "][node:" + node + "]"));

		if(StringUtils.isEmpty(shardName) || node == null){
			logger.warn(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"returnIndexDataCopyInfo", "[shardName:" + shardName
							+ "][succList:" + succList + "][node:" + node + "]"));
	            throw new IllegalArgumentException();
		}
		NettyAction action=NettyAction.NETTY_ACTION_RETURN_COPYINDEX_FEEDBACK;
		MessageHeader mh=new MessageHeader(action);
		CopyIndexResultMessage cirm=new CopyIndexResultMessage(shardName,mh,succList);
		ServerFeedBackClientHandler handler=new ServerFeedBackClientHandler();
		AbstractMessage bMessage=null;
		ServerFeedbackMessage backMessage = null;
		try {
			bMessage = this.executeAction(action, cirm, handler, node);			
		} catch (Exception e) {
			logger.error("returnIndexDataCopyInfo Exception", e.getCause());
			throw e;
		}
		
		if (bMessage instanceof ServerFeedbackMessage) {
            backMessage = (ServerFeedbackMessage) bMessage;
        } else {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION),
                ((ServerExceptionMessage) bMessage).getCause(),
                    IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
		
		return backMessage;
    }
    
    
    
    
    /**
     * copyIndexDataToRemoteNode 拷贝索引数据到其它节点
     * @param filePath 当前要拷贝的目录
     * @param targetFilePath 目标机器目录
     * @param replace replace
     * @param node node
     * @return ServerFeedbackMessage
     * @throws IndexAndSearchException
     */
    public ServerFeedbackMessage copyIndexDataToRemoteNode(String filePath, String targetFilePath, boolean replace,
        Node node) throws Exception {
        logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "copyIndexDataToRemoteNode", "[filePath:" + filePath + "][replace:" + replace
                + "][Node:" + node + "]"));
        
        if (StringUtils.isEmpty(filePath) || node == null) {
            logger.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "copyIndexDataToRemoteNode", "[filePath:" + filePath + "][replace:" + replace
                    + "][nodeList:" + node + "]"));
            throw new IllegalArgumentException();
        }
        String tmpBaseDir=getIndexFileCopyTmpDirPath(filePath);
        File tmpBaseDirFile=new File(tmpBaseDir);
        if(!tmpBaseDirFile.exists()){
        	tmpBaseDirFile.mkdirs();
        }
        // 压缩
        String compressedFilePath = tmpBaseDir + Md5Util.encode(filePath)+"_"+System.currentTimeMillis() + ".tar.gz";
        File compressedFile = new File(compressedFilePath);
        FileUtils.deleteFile(compressedFile);
        
        try {
            compressedFilePath = FileUtils.doCompressFile(filePath,compressedFilePath);
        } catch (IOException e2) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION), e2,
                    IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
        
        // 读文件
        File fin = new File(compressedFilePath);
        FileChannel fcin = null;
        try {
            fcin = new RandomAccessFile(fin, "r").getChannel();
        } catch (FileNotFoundException e1) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION), e1,
                    IndexAndSearchExceptionType.INDEX_EXCEPTION);
        }
        ByteBuffer rBuffer = ByteBuffer.allocate(TesseractConstant.FILE_BLOCK_SIZE);
        ServerFeedbackMessage backMessage = null;
        boolean isFirst = true;
        boolean isLast = false;
        int idx=0;
        
        logger.info("STARTING TO SEND FILE :"+ (targetFilePath + File.separator + fin.getName()));
        try {
            while (true) {
                rBuffer.clear();
                int r = fcin.read(rBuffer);
                
                if (r == -1) {
                    break;
                }
                
                if (rBuffer.position() < rBuffer.capacity()) {
                    isLast = true;
                }
                rBuffer.flip();
                
                NettyAction action = NettyAction.NETTY_ACTION_COPYFILE;
                MessageHeader mh = new MessageHeader(action);
                SendFileMessage sfm = new SendFileMessage(mh, rBuffer.array(), targetFilePath, fin.getName());
                if (isFirst) {
                    sfm.setFirst(isFirst);
                    isFirst=false;
                } else {
                    sfm.setFirst(false);
                }
                
                sfm.setLast(isLast);
                sfm.setIdx(idx++);

                ServerFeedBackClientHandler handler = new ServerFeedBackClientHandler();
                AbstractMessage bMessage = this.executeAction(action, sfm, handler, node);
                
                if (bMessage instanceof ServerFeedbackMessage) {
                    backMessage = (ServerFeedbackMessage) bMessage;
                } else {
                    throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                        IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                        IndexAndSearchExceptionType.INDEX_EXCEPTION),
                        ((ServerExceptionMessage) bMessage).getCause(),
                            IndexAndSearchExceptionType.INDEX_EXCEPTION);
                }
                
                if (backMessage == null
                        || backMessage.getResult().equals(TesseractConstant.FEED_BACK_MSG_RESULT_FAIL)) {
                    throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                        IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                        IndexAndSearchExceptionType.INDEX_EXCEPTION),
                        IndexAndSearchExceptionType.INDEX_EXCEPTION);
                }
                
            }
        } catch (Exception e) {
        	logger.warn("Exception occured",e);
            throw e;
        } finally {
            try {
                fcin.close();
            } catch (IOException e) {
            	logger.warn("Exception occured",e);
                throw e;
            }
        }
        return backMessage;
    }
    
    /**
     * search
     * @param query query 
     * @param idxShard idxShard
     * @param searchNode searchNode
     * @return SearchResultMessage
     * @throws IndexAndSearchException
     */
    public SearchResultMessage search(QueryRequest query, IndexShard idxShard, Node searchNode)
        throws IndexAndSearchException {
        NettyAction action = NettyAction.NETTY_ACTION_SEARCH;
        
        MessageHeader messageHeader = new MessageHeader(action);
        
        SearchRequestMessage message = new SearchRequestMessage(messageHeader, query);
        message.setIdxPath(idxShard.getAbsoluteIdxFilePath(searchNode.getIndexBaseDir()));
        
        AbstractMessage ret = null;
        
        SearchClientHandler handler = new SearchClientHandler();
        SearchResultMessage result = null;
        
        try {
            ret = this.executeAction(action, message, handler, searchNode);
            if (ret instanceof SearchResultMessage) {
                result = (SearchResultMessage) ret;
            } else {
                throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                    IndexAndSearchException.QUERYEXCEPTION_MESSAGE,
                    IndexAndSearchExceptionType.SEARCH_EXCEPTION),
                    ((ServerExceptionMessage) ret).getCause(),
                        IndexAndSearchExceptionType.SEARCH_EXCEPTION);
            }
        } catch (Exception e) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.QUERYEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.SEARCH_EXCEPTION), e.getCause(),
                IndexAndSearchExceptionType.SEARCH_EXCEPTION);
        }
        
        return result;
    }
    
    /**
     * executeAction
     * @param action 动作
     * @param message 消息
     * @param handler handler
     * @param node 节点
     * @return AbstractMessage的子类
     * @throws Exception
     */
    public <T extends AbstractMessage, R extends AbstractMessage, S extends AbstractChannelInboundHandler> T executeAction(
        NettyAction action, R message, S handler, Node node) throws Exception {
        logger.info("executeAction:[NettyAction=" + action + "][Message=" + message + "][Handler="
            + handler + "]");
        T returnMessage = null;
        if (action == null || handler == null) {
            logger.info("executeAction:[NettyAction=" + action + "][Message=" + message
                + "][Handler=" + handler + "]-Exception:IllegalArgumentException");
            throw new IllegalArgumentException();
        }

        Channel channel = null;
        channel = this.getChannelByAddressAndPort(node.getAddress(), node.getPort());
        channel.pipeline().addLast(handler);
        
        //写入源节点
        
        Node fromNode=((IsNodeService)ApplicationContextHelper.getContext().getBean("isNodeService")).getCurrentNode();
        if(message!=null){
        	message.getMessageHeader().setFromNode(fromNode);
        }        
        
        channel.writeAndFlush(message);
        channel.closeFuture().sync ();
        
        returnMessage = handler.getMessage();
        
        handler.setMessage(null);
        channel.close ();
        
        logger.info("executeAction:[NettyAction=" + action + "][Message=" + message + "][Handler="
            + handler + "] success");
        
        return returnMessage;
        
    }
    
    public void shutDown() {
        this.b.group().shutdownGracefully();
    }



	
    
    

}
