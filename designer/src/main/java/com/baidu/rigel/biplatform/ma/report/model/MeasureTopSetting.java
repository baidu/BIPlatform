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

/**
 * 指标top/bottomN设置
 * @author david.wang
 *
 */
public class MeasureTopSetting implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5893492933636273757L;
    
    /**
     * 排序方式
     */
    private TopType topType = TopType.DESC;
    
    /**
     * 指标id
     */
    private String measureId;
    
    /**
     * 纪录数
     */
    private int recordSize;
    
    /**
     * 区域id
     */
    private String areaId;

    /**
     * @return the topType
     */
    public TopType getTopType() {
        return topType;
    }

    /**
     * @param topType the topType to set
     */
    public void setTopType(TopType topType) {
        this.topType = topType;
    }

    /**
     * @return the measureId
     */
    public String getMeasureId() {
        return measureId;
    }

    /**
     * @param measureId the measureId to set
     */
    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    /**
     * @return the recordSize
     */
    public int getRecordSize() {
        return recordSize;
    }

    /**
     * @param recordSize the recordSize to set
     */
    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    /**
     * @return the areaId
     */
    public String getAreaId() {
        return areaId;
    }

    /**
     * @param areaId the areaId to set
     */
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }
    
}
