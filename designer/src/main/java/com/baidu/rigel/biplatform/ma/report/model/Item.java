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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.google.common.collect.Maps;

/**
 * 
 * 报表行或列的元定义
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public class Item implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = -437694326022361554L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * 立方体id
     */
    private String cubeId;
    
    /**
     * schema id
     */
    private String schemaId;
    
    /**
     * 报表id
     */
    private String reportId;
    
    /**
     * olapElementId
     */
    private String olapElementId;
    
    /**
     * 扩展区域id
     */
    private String areaId;
    
//    /**
//     * 格式定义
//     */
//    private FormatModel formatModel;
    
    /**
     * 参数定义 key 为 参数名称 value为默认值
     */
    private Map<String, Object> params;
    
    /**
     * 当前条目所在位置
     */
    private PositionType positionType;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCubeId() {
        return cubeId;
    }
    
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }
    
    public String getSchemaId() {
        return schemaId;
    }
    
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public String getAreaId() {
        return areaId;
    }
    
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }
    
//    public FormatModel getFormatModel() {
//        return formatModel;
//    }
//    
//    public void setFormatModel(FormatModel formatModel) {
//        this.formatModel = formatModel;
//    }
    
    public Map<String, Object> getParams() {
        if (params != null) {
            return this.params;
        }
        params = Maps.newHashMap();
        return this.params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public PositionType getPositionType() {
        return positionType;
    }
    
    public void setPositionType(PositionType positionType) {
        this.positionType = positionType;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof Item) {
            Item obj = (Item) object;
            if (!StringUtils.hasText(this.olapElementId)) {
                return false;
            }
            return this.getOlapElementId().equals(obj.getOlapElementId());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return StringUtils.hasText(this.olapElementId) ? this.olapElementId.hashCode() : 0;
    }
    
    /**
     * get the dimId
     * 
     * @return the dimId
     */
    public String getOlapElementId() {
        return olapElementId;
    }
    
    /**
     * set the olapElementId
     * 
     * @param olapElementId
     *            the olapElementId to set
     */
    
    public void setOlapElementId(String olapElementId) {
        this.olapElementId = olapElementId;
    }
    
}
