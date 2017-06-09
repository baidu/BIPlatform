package com.baidu.rigel.biplatform.ma.report.query.newtable.bo;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 重构表格的指标信息描述对象
 * 
 * @author majun04
 *
 */
public class IndDataDefine extends BasicTableDefine {
    /**
     * 数据格式
     */
    private String format;
    /**
     * 当前排序，注：后续可能不需要后端进行排序了，改为前端直接按照已有顺序排序
     */
    private String sortType;
    /**
     * 是否需要有指标描述？
     */
    private boolean hasHelper = false;
    /**
     * 指标显示align，默认居中显示
     */
    private String align = "center";
    /**
     * olapElementId
     */
    private String olapElementId;
    /**
     * 跳转信息
     */
    private String linkBridge;
    /**
     * toolTip信息
     */
    private String toolTip;
    /**
     * 可展开孩子节点
     */
    private List<IndDataDefine> expandChildren = Lists.newArrayList();

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the sortType
     */
    public String getSortType() {
        return sortType;
    }

    /**
     * @param sortType the sortType to set
     */
    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    /**
     * @return the hasHelper
     */
    public boolean isHasHelper() {
        return hasHelper;
    }

    /**
     * @param hasHelper the hasHelper to set
     */
    public void setHasHelper(boolean hasHelper) {
        this.hasHelper = hasHelper;
    }

    /**
     * @return the expandChildren
     */
    public List<IndDataDefine> getExpandChildren() {
        return expandChildren;
    }

    /**
     * @param expandChildren the expandChildren to set
     */
    public void setExpandChildren(List<IndDataDefine> expandChildren) {
        this.expandChildren = expandChildren;
    }

    /**
     * @return the align
     */
    public String getAlign() {
        return align;
    }

    /**
     * @param align the align to set
     */
    public void setAlign(String align) {
        this.align = align;
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

    /**
     * @return the toolTip
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * @param toolTip the toolTip to set
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    /**
     * @return the olapElementId
     */
    public String getOlapElementId() {
        return olapElementId;
    }

    /**
     * @param olapElementId the olapElementId to set
     */
    public void setOlapElementId(String olapElementId) {
        this.olapElementId = olapElementId;
    }

}
