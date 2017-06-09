/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.netty.service;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService;
import com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler;
import com.baidu.rigel.biplatform.tesseract.netty.message.AbstractMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.MessageHeader;
import com.baidu.rigel.biplatform.tesseract.netty.message.NettyAction;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.CopyIndexResultMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;

/**
 * @author lijin
 *
 */
@Sharable
public class CopyIndexInfoReturnHandler extends AbstractChannelInboundHandler{
	
	
	/**
     * 支持的action
     */
    private static final NettyAction ACTION_SUPPORT = NettyAction.NETTY_ACTION_RETURN_COPYINDEX_FEEDBACK;
    /**
     * 返回的消息action
     */
    private static final NettyAction ACTION_FEEDBACK = NettyAction.NETTY_ACTION_SERVER_FEEDBACK;
    
    
    /**
     * 日志记录
     */
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * IndexService
	 */
	private IndexService idxService;
	
    
    
    /**
     * 单例
     */
    private static CopyIndexInfoReturnHandler COPYINDEX_INFO_RETURN_HANDLER;
    
    /**
     * 
     * getChannelHandler
     * 
     * @return IndexServerHandler
     */
    public static synchronized CopyIndexInfoReturnHandler getChannelHandler() {
        if (COPYINDEX_INFO_RETURN_HANDLER == null) {
        	COPYINDEX_INFO_RETURN_HANDLER = new CopyIndexInfoReturnHandler ();
        }
        return COPYINDEX_INFO_RETURN_HANDLER;
    }
    

	/**
	 * @param supportedAction
	 * @param feedBackAction
	 */
	private CopyIndexInfoReturnHandler() {
		super(ACTION_SUPPORT, ACTION_FEEDBACK);
		this.idxService=(IndexService)ApplicationContextHelper.getContext().getBean("indexService");	
		
	}

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#messageReceived(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {
			
		CopyIndexResultMessage resultMessage=(CopyIndexResultMessage)msg;
		this.idxService.setCopyIndexTaskResult(resultMessage.getShardName(), resultMessage.getSuccList());		
		
		MessageHeader mh=new MessageHeader(ACTION_FEEDBACK);
        String result=FileUtils.SUCC;
        String message="set copy info success";
        ServerFeedbackMessage backMessage=new ServerFeedbackMessage(mh,result,message);
        
        ChannelFuture sendBack=ctx.writeAndFlush(backMessage);
        if(sendBack.isDone()){
        	ctx.close();
        }
		
	}

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.netty.AbstractChannelInboundHandler#getMessage()
	 */
	@Override
	public AbstractMessage getMessage() {
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
