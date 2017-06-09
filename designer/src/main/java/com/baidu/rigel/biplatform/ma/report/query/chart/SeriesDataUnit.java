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
package com.baidu.rigel.biplatform.ma.report.query.chart;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.util.StringUtils;

/**
 * 
 * @author zhongyi
 *
 */
public class SeriesDataUnit implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = -5239813800291781589L;
    
    /**
     * data
     */
    private BigDecimal[] data;
    
    /**
     * format
     */
    private String format;
    
    /**
     * name
     */
    private String name;
    
    /**
     * type
     */
    private String type;
    
    /**
     * yAxisName
     */
    private String yAxisName;
    
    /**
     * 当前指标数据对应颜色
     */
    private String colorDefine;
    
    
    private String position = "0";
    
    /**
     * 地图数据对应地域信息，后期考虑此处模型逻辑是否合法
     */
    private String[][] properties;
    
    /**
     * 通过这个方法得到不同的数据对象
     * 
     * @return
     */
    public BigDecimal[] getData() {
        return data;
    }
    
    public void setData(BigDecimal[] data) {
        this.data = data;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getyAxisName() {
        return yAxisName;
    }
    
    public void setyAxisName(String yAxisName) {
        this.yAxisName = yAxisName;
    }

    /**
     * @return the properties
     */
    public String[][] getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(String[][] properties) {
        this.properties = properties;
    }

    /**
     * @return the colorDefine
     */
    public String getColorDefine() {
        return colorDefine;
    }

    /**
     * @param colorDefine the colorDefine to set
     */
    public void setColorDefine(String colorDefine) {
        this.colorDefine = colorDefine;
    }

    /**
     * @return the position
     */
    public String getPosition() {
        if (StringUtils.isEmpty (position)) {
            return "0";
        }
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(String position) {
        this.position = position;
    }
    
    
}