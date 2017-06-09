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
package com.baidu.rigel.biplatform.ma.report.query.pivottable;

import java.io.Serializable;

/**
 * 列上的属性定义。
 * 
 * @author mengran
 * 
 */
public class ColDefine implements Serializable {

    /**
     * serialized id
     */
    private static final long serialVersionUID = 1L;

    /**
     * uniqueName
     */
    private String uniqueName;

    /**
     * format
     */
    private String format;

    /**
     * caption
     */
    private String caption;

    /**
     * colDefinedId
     */
    private String colDefineId;
    /**
     * olapElementId
     */
    private String olapElementId;

    /**
     * showAxis
     */
    private String showAxis;

    /**
     * linkBridge
     */
    private String linkBridge;

    /**
     * currentSort
     */
    private String currentSort = "NULL";

    /**
     * 指标提示文本
     */
    private String toolTip;

    /**
     * 文本对齐
     */
    private String align;

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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getShowUniqueName() {
        return colDefineId;
    }

    public void setShowUniqueName(String showUniqueName) {
        this.colDefineId = showUniqueName;
    }

    public String getShowAxis() {
        return showAxis;
    }

    public void setShowAxis(String showAxis) {
        this.showAxis = showAxis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ColDefine [uniqueName=" + uniqueName + ", format=" + format + ", caption=" + caption
                + ", showUniqueName=" + colDefineId + ", ShowAxis=" + showAxis + ", currentSort=" + currentSort + "]";
    }

    public String getCurrentSort() {
        return currentSort;
    }

    public void setCurrentSort(String currentSort) {
        this.currentSort = currentSort;
    }

    public String getLinkBridge() {
        return linkBridge;
    }

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
     * 
     * @return the align
     */
    public String getAlign() {
        return align;
    }

    /**
     * 
     * @param align 对齐方式
     */
    public void setAlign(String align) {
        this.align = align;
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
