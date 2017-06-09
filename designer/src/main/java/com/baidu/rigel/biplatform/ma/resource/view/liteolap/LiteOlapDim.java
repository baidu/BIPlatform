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
/**
 * 
 */
package com.baidu.rigel.biplatform.ma.resource.view.liteolap;

import java.io.Serializable;

/**
 * LiteOlap的维度视图
 * 
 * @author zhongyi
 *
 */
public class LiteOlapDim implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3564866541855034000L;
    
    // private String align = "LEFT";
    private boolean asFilter = false;
    private String caption = "未命名";
    private String cubeName = "未命名";
    private boolean fixed = false;
    private boolean isConfig = true;
    private boolean isShareDim = true;
    private boolean isTimeDim = false;
    private String name = "nan";
    private boolean selected = false;
    private int status = 2;
    private String uniqName = "nan";
    
    /**
     * @return the asFilter
     */
    public boolean isAsFilter() {
        return asFilter;
    }
    /**
     * @param asFilter the asFilter to set
     */
    public void setAsFilter(boolean asFilter) {
        this.asFilter = asFilter;
    }
    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }
    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    /**
     * @return the cubeName
     */
    public String getCubeName() {
        return cubeName;
    }
    /**
     * @param cubeName the cubeName to set
     */
    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }
    /**
     * @return the fixed
     */
    public boolean isFixed() {
        return fixed;
    }
    /**
     * @param fixed the fixed to set
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    /**
     * @return the isConfig
     */
    public boolean getIsConfig() {
        return isConfig;
    }
    /**
     * @param isConfig the isConfig to set
     */
    public void setIsConfig(boolean isConfig) {
        this.isConfig = isConfig;
    }
    /**
     * @return the isShareDim
     */
    public boolean getIsShareDim() {
        return isShareDim;
    }
    /**
     * @param isShareDim the isShareDim to set
     */
    public void setIsShareDim(boolean isShareDim) {
        this.isShareDim = isShareDim;
    }
    /**
     * @return the isTimeDim
     */
    public boolean isTimeDim() {
        return isTimeDim;
    }
    /**
     * @param isTimeDim the isTimeDim to set
     */
    public void setTimeDim(boolean isTimeDim) {
        this.isTimeDim = isTimeDim;
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
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }
    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
    /**
     * @return the uniqName
     */
    public String getUniqName() {
        return uniqName;
    }
    /**
     * @param uniqName the uniqName to set
     */
    public void setUniqName(String uniqName) {
        this.uniqName = uniqName;
    }
}
