package com.baidu.rigel.biplatform.ma.report.query.newtable.bo;

/**
 * 表格基础数据对象基类
 * 
 * @author majun04
 *
 */
public abstract class BasicTableDefine {
    /**
     * 表格行或者列的id，通常为uniqueName
     */
    private String id;
    /**
     * 指标或者维度的中文含义
     */
    private String name;
    /**
     * 是否是展开状态
     */
    private boolean isExpanded = false;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

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
     * @return the isExpanded
     */
    public boolean getIsExpanded() {
        return isExpanded;
    }

    /**
     * @param isExpanded the isExpanded to set
     */
    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

}
