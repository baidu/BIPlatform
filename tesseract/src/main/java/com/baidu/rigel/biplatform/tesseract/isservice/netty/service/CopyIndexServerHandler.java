/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.CopyIndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.service.IndexAndSearchClient;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.StringTools;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * @author lijin
 *
 */
@Sharable
public class CopyIndexServerHandler extends AbstractChannelInboundHandler {
	/**
     * 支持的action
     */
    private static final NettyAction ACTION_SUPPORT = NettyAction.NETTY_ACTION_START_COPYINDEX;
    /**
     * 返回的消息action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_SERVER_FEEDBACK;
    
   
    
    /**
     * taskExecutor
     */
    private ThreadPoolTaskExecutor taskExecutor;
    

    /**
     * logger
     */
	private Logger logger = LoggerFactory.getLogger(CopyIndexServerHandler.class);
	
	/**
	 * isClient
	 */
	private IndexAndSearchClient isClient;
	
	/**
     * 单例
     */
    private static CopyIndexServerHandler COPYINDEX_SERVER_HANDLER;
    
    /**
     * 
     * getChannelHandler
     * 
     * @return IndexServerHandler
     */
    public static synchronized CopyIndexServerHandler getChannelHandler() {
        if (COPYINDEX_SERVER_HANDLER == null) {
        	COPYINDEX_SERVER_HANDLER = new CopyIndexServerHandler ();
        }
        return COPYINDEX_SERVER_HANDLER;
    }
	
	
	
	
	/**
	 * @param supportedAction
	 * @param feedBackAction
	 */
	public CopyIndexServerHandler() {
		super(ACTION_SUPPORT, ACTION_FEEDBACK);
		isClient=IndexAndSearchClient.getNodeClient();
		taskExecutor=(ThreadPoolTaskExecutor)
		    ApplicationContextHelper.getContext().getBean(ThreadPoolTaskExecutor.class);
		
	}

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#messageReceived(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		logger.info (String.format (
                LogInfoConstants.INFO_PATTERN_MESSAGE_RECEIVED_BEGIN,
                "CopyIndexServerHandler"));
        CopyIndexMessage copyIndexMsg = (CopyIndexMessage) msg;
        
        MessageHeader mh=new MessageHeader(ACTION_FEEDBACK);
        String result=FileUtils.SUCC;
        String message="start copy success";
        ServerFeedbackMessage backMessage=new ServerFeedbackMessage(mh,result,message);
        
        ChannelFuture sendBack=ctx.writeAndFlush(backMessage);
        if(sendBack.isDone()){
        	ctx.close();
        }
        
        
        Node remoteNode=copyIndexMsg.getMessageHeader().getFromNode();
        this.processCopyIndexTask(copyIndexMsg,remoteNode);
                

	}
	
	private void processCopyIndexTask(CopyIndexMessage copyIndexMsg,Node remoteNode) throws Exception {
		
		ExecutorCompletionService<ServerFeedbackMessage> completionService = new ExecutorCompletionService<>(taskExecutor);		
		
        List<Node> toNodeList=copyIndexMsg.getToNodeList();
        String srcFilePath=copyIndexMsg.getSrcFilePath();
        String targetFilePath=copyIndexMsg.getTargetFilePath();
        
        List<String> succList=new ArrayList<String>();
        if(!CollectionUtils.isEmpty(toNodeList)){        	
        	for(Node toNode:toNodeList){
        		String currTargetFilePath=StringTools.concatIndexBaseDir(targetFilePath, toNode.getIndexBaseDir());        		
        		completionService.submit(new Callable<ServerFeedbackMessage>(){
					@Override
					public ServerFeedbackMessage call() throws Exception {
						// TODO Auto-generated method stub
						int retryTimes=0;
						ServerFeedbackMessage backMessage = null;
						while (retryTimes < TesseractConstant.RETRY_TIMES) {
							try{
								backMessage = isClient.copyIndexDataToRemoteNode(srcFilePath, currTargetFilePath, true, toNode);
							}catch(Exception e){
								logger.warn("exception occured when copy index data to remote node",e);
							}
							
							if (backMessage!=null && backMessage.getResult().equals(FileUtils.SUCC)) {						
								logger.info(String
										.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
												"writeIndex", "copy index success to "
														+ toNode));
								backMessage.setNodeKey(toNode.getNodeKey());
								break;
							} else {
								logger.info(String
										.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
												"writeIndex", "retry copy index to "
														+ toNode));
								retryTimes++;
							}

						}
						if(backMessage==null){
							
							MessageHeader mh=new MessageHeader(ACTION_FEEDBACK);
					        String result=FileUtils.FAIL;
					        String message="copy failed";
					        backMessage=new ServerFeedbackMessage(mh,result,message);
						}
						return backMessage;
					}
        			
        		}); 
        		
        	}
        	
        	try{
        		for(int i=0;i<toNodeList.size();i++){
            		ServerFeedbackMessage backMessage = completionService.take().get();
            		if(backMessage.getResult().equals(FileUtils.SUCC)){
            			succList.add(backMessage.getNodeKey());
            		}
            		
            	}
        	}catch (Exception e){
        		logger.warn("Exception occured",e);
        		throw e;
        	}
        	
        	
        }
        logger.info("SENDING COPY RESULT BACK");
        this.isClient.returnIndexDataCopyInfo(copyIndexMsg.getShardName(), succList, remoteNode);
	}
	
	

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#getMessage()
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
		// TODO Auto-generated method stub

	}



	

}
