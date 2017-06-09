package com.baidu.rigel.biplatform.tesseract.netty.message;

/**
 * MessageStatus 消息状态
 * @author lijin
 *
 */
public enum MessageStatus {
	/**
	 * MESSAGE_STATUS_INIT
	 */
	MESSAGE_STATUS_INIT("MESSAGE_STATUS_INIT"),
	/**
	 * MESSAGE_STATUS_CONT
	 */
	MESSAGE_STATUS_CONT("MESSAGE_STATUS_CONT"),
	/**
	 * MESSAGE_STATUS_FIN
	 */
	MESSAGE_STATUS_FIN("MESSAGE_STATUS_FIN");
	
	/**
	 * 状态名称
	 */
	private String status; 
	
	/**
	 * 私有构造方法
	 * @param statusName
	 */
	private MessageStatus(String statusName){
		status=statusName;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
