package com.baidu.rigel.biplatform.ma.report.query.newtable.bo;

/**
 * 操作列对象描述
 * 
 * @author majun04
 *
 */
public class OperationColumnDefine {
    /**
     * 操作列名称
     */
    private String name;
    /**
     * 跳转信息
     */
    private String linkBridge;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the linkBridge
     */
    public String getLinkBridge() {
        return linkBridge;
    }

    /**
     * @param linkBridge the linkBridge to set
     */
    public void setLinkBridge(String linkBridge) {
        this.linkBridge = linkBridge;
    }

}
