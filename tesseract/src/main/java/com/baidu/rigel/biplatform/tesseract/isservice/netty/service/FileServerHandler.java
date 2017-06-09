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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.SendFileMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;

/**
 * FileServerHandler
 * 
 * @author lijin
 *
 */
@Sharable
public class FileServerHandler extends AbstractChannelInboundHandler {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);
    
    // private ConcurrentHashMap<String,FileChannel> fcoutMap;
    
    /**
     * 当前handler支持的操作NETTY_ACTION_COPYFILE
     */
    private static final NettyAction ACTION_SUPPORT = NettyAction.NETTY_ACTION_COPYFILE;
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_SERVER_FEEDBACK;
    
    /**
     * 单例
     */
    private static FileServerHandler FILE_SERVER_HANDLER;
    
    /**
     * 
     * getChannelHandler
     * 
     * @return FileServerHandler
     */
    public static synchronized FileServerHandler getChannelHandler() {
        if (FILE_SERVER_HANDLER == null) {
            FILE_SERVER_HANDLER = new FileServerHandler();
        }
        return FILE_SERVER_HANDLER;
    }
    
    /**
     * Constructor by
     */
    public FileServerHandler() {
        super(ACTION_SUPPORT, ACTION_FEEDBACK);
        // fcoutMap=new ConcurrentHashMap<String,FileChannel>();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #messageReceived(io.netty.channel.ChannelHandlerContext,
     * java.lang.Object)
     */
    // @Override
    // public void messageReceived(ChannelHandlerContext ctx, Object msg) throws
    // Exception {
    // LOGGER.info("get message from client " + msg);
    // Request req = (Request) msg;
    // // 本地文件操作服务
    // switch (req.getCommand()) {
    // case WRITE:
    // doWrite(ctx, req);
    // break;
    // default:
    // Response rs = new Response(ResponseStatus.UNKNOW, "未知请求", null);
    // ctx.writeAndFlush(rs);
    // return;
    // }
    //
    // }
    
    /**
     * private FileChannel getFileChannel(String filePath) throws
     * FileNotFoundException { if(this.fcoutMap.containsKey(filePath)){ return
     * this.fcoutMap.get(filePath); } File fout = new File(filePath);
     * if(fout.exists()){ fout.delete(); } FileChannel fcout = null; fcout = new
     * FileOutputStream(fout, true).getChannel(); this.fcoutMap.put(filePath,
     * fcout); return fcout;
     * 
     * }
     * 
     * private boolean releaseFileChannel(String filePath) throws IOException {
     * if(this.fcoutMap.containsKey(filePath)){ FileChannel
     * f=this.fcoutMap.get(filePath); f.close(); this.fcoutMap.remove(filePath);
     * return true; }
     * 
     * return false; }
     * 
     * 
     * @throws IOException 
     **/
    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg)
        throws IndexAndSearchException, IOException {
        LOGGER.info("get message from client " + msg);
        SendFileMessage sendFileMessage = (SendFileMessage) msg;
        
        LOGGER.info("GET "+sendFileMessage.getIdx()+" MSG");
        
        MessageHeader messageHeader = new MessageHeader(NettyAction.NETTY_ACTION_SERVER_FEEDBACK);
        
        if (sendFileMessage == null || StringUtils.isEmpty(sendFileMessage.getTargetFilePath())) {
            ServerFeedbackMessage backMessage = new ServerFeedbackMessage(messageHeader,
                TesseractConstant.FEED_BACK_MSG_RESULT_FAIL, "sendFileMessage empty");
            ctx.writeAndFlush(backMessage);
        }
        String targetFilePath=sendFileMessage.getTargetFilePath();
        String fileName=sendFileMessage.getFileName();
        
        File targetDir=new File(targetFilePath);
        if(targetDir.exists() && sendFileMessage.isFirst()){
        	FileUtils.deleteFile(targetDir);
        }
        
        
        File fout = new File(targetDir.getParent()+File.separator+"tmp"+File.separator+fileName);
        if (fout.exists() && sendFileMessage.isFirst()) {
            fout.delete();
        }else if(!fout.getParentFile().exists()){
            fout.getParentFile().mkdirs();
        }
        

        FileOutputStream fos = null;
        FileChannel fcout = null;
        try {
            fos = new FileOutputStream(fout, true);
            fcout = fos.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(TesseractConstant.FILE_BLOCK_SIZE*2);
            buffer.put(sendFileMessage.getContent());
            buffer.flip();
            fcout.write(buffer);
            
            fos.flush();
            buffer.clear();
            
        } catch (Exception e) {
            throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                IndexAndSearchExceptionType.INDEX_EXCEPTION), e.getCause(),
                IndexAndSearchExceptionType.INDEX_EXCEPTION);
        } finally {
            try {
                fcout.close();
                fos.close();
            } catch (IOException e) {
                throw new IndexAndSearchException(TesseractExceptionUtils.getExceptionMessage(
                    IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
                    IndexAndSearchExceptionType.INDEX_EXCEPTION), e.getCause(),
                    IndexAndSearchExceptionType.INDEX_EXCEPTION);
            }
            
        }
        MessageHeader mh=new MessageHeader(NettyAction.NETTY_ACTION_SERVER_FEEDBACK);
        String result=FileUtils.SUCC;
        String message="copy success";
        
        
        if(sendFileMessage.isLast()){
            try {
            	String resultFilePath=null;
            	resultFilePath=FileUtils.doUncompressFile(fout.getAbsolutePath(), targetFilePath);
            	if(StringUtils.isEmpty(resultFilePath)){
                	result=FileUtils.FAIL;
                }
            } catch (IOException e) {
            	LOGGER.warn("Exception occured",e);
                throw e;
            }
        }
        
        ServerFeedbackMessage backMessage=new ServerFeedbackMessage(mh,result,message);
        
        ctx.writeAndFlush(backMessage);
        
        
    }
    
   
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler
     * #getMessage()
     */
    @Override
    public <T extends AbstractMessage> T getMessage() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#setMessage(com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage)
     */
    @Override
    public <T extends AbstractMessage> void setMessage(T t) {
        return ; 
        
    }
    
}
