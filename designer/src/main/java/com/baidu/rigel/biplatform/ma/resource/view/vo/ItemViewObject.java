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
package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;

/**
 * 
 * ItemViewObject
 * @author david.wang
 * @version 1.0.0.1
 */
public class ItemViewObject implements Serializable {

    /**
     * ItemViewObject.java -- long
     * description:
     */
    private static final long serialVersionUID = -4599200848023521349L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * caption
     */
    private String caption;
    
    /**
     * name
     */
    private String name;
    
    /**
     * cubeId
     */
    private String cubeId;
    
    /**
     * olapElementId
     */
    private String olapElementId;
    
    /**
     * used，是否已经用在横轴、纵轴、条件上，供前端展现使用
     */
    private boolean used;
    
    /**
     * 指标对应的图类型，纬度属性值为null
     */
    private String chartType;

    /**
     * dimGroup
     */
    private boolean dimGroup = false;
    
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
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }

    /**
     * @param cubeId the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
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

    /**
     * @return the used
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * @param used the used to set
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * @return the chartType
     */
    public String getChartType() {
        return chartType;
    }

    /**
     * @param chartType the chartType to set
     */
    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    /**
     * @return the dimGroup
     */
    public boolean isDimGroup() {
        return dimGroup;
    }

    /**
     * @param dimGroup the dimGroup to set
     */
    public void setDimGroup(boolean dimGroup) {
        this.dimGroup = dimGroup;
    }
    
}
