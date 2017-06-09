package com.baidu.rigel.biplatform.ma.report.query.newtable.bo;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 重构表格的维度信息描述对象
 * 
 * @author majun04
 *
 */
public class DimDataDefine extends BasicTableDefine {
    /**
     * 是否有展开的孩子节点
     */
    private boolean hasExpandChildren = false;
    /**
     * 是否需要下钻
     */
    private boolean canDrill = false;
    /**
     * 下钻的孩子节点集合
     */
    private List<DimDataDefine> drillChildren = Lists.newArrayList();
    /**
     * 展开的孩子节点集合
     */
    private List<DimDataDefine> expandChildren = Lists.newArrayList();

    /**
     * @return the drillChildren
     */
    public List<DimDataDefine> getDrillChildren() {
        return drillChildren;
    }

    /**
     * @param drillChildren the drillChildren to set
     */
    public void setDrillChildren(List<DimDataDefine> drillChildren) {
        this.drillChildren = drillChildren;
    }

    /**
     * @return the expandChildren
     */
    public List<DimDataDefine> getExpandChildren() {
        return expandChildren;
    }

    /**
     * @param expandChildren the expandChildren to set
     */
    public void setExpandChildren(List<DimDataDefine> expandChildren) {
        this.expandChildren = expandChildren;
    }

    /**
     * @return the hasExpandChildren
     */
    public boolean isHasExpandChildren() {
        return hasExpandChildren;
    }

    /**
     * @param hasExpandChildren the hasExpandChildren to set
     */
    public void setHasExpandChildren(boolean hasExpandChildren) {
        this.hasExpandChildren = hasExpandChildren;
    }

    /**
     * @return the canDrill
     */
    public boolean isCanDrill() {
        return canDrill;
    }

    /**
     * @param canDrill the canDrill to set
     */
    public void setCanDrill(boolean canDrill) {
        this.canDrill = canDrill;
    }

}
