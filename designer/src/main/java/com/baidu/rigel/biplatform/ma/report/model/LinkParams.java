package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;

/**
 * 多维报表跳转平面报表时的参数传值vo对象
 * 
 * @author majun04
 *
 */
public class LinkParams implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8684919756835186146L;
    /**
     * 平面报表需要的参数名称
     */
    private String paramName = null;
    /**
     * 多维报表传入的维度名称
     */
    private String dimName = null;
    /**
     * uniqueName
     */
    private String uniqueName = null;
    /**
     * 维度未经getMembers的原始维值
     */
    private String originalDimValue = null;

    /**
     * @return the paramName
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @param paramName the paramName to set
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * @return the dimName
     */
    public String getDimName() {
        return dimName;
    }

    /**
     * @param dimName the dimName to set
     */
    public void setDimName(String dimName) {
        this.dimName = dimName;
    }

    /**
     * @return the originalDimValue
     */
    public String getOriginalDimValue() {
        return originalDimValue;
    }

    /**
     * @param originalDimValue the originalDimValue to set
     */
    public void setOriginalDimValue(String originalDimValue) {
        this.originalDimValue = originalDimValue;
    }

    /**
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * @param uniqueName the uniqueName to set
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

}