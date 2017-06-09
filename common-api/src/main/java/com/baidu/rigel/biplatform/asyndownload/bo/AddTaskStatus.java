package com.baidu.rigel.biplatform.asyndownload.bo;

/**
 * 类AddTaskStatus.java的实现描述：AddTaskStatus 类实现描述 
 * @author luowenlei 2015年8月31日 上午10:54:26
 */
public class AddTaskStatus {
    /**
     * status
     */
    private Integer status;
    
    /**
     * msg
     */
    private String msg;

    /**
     * default generate get status
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * default generate set status
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * default generate get msg
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * default generate set msg
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    /**AddTaskStatus
     * @param status
     * @param msg
     */
    public AddTaskStatus(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
