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
import java.util.List;

import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.google.common.collect.Lists;

/**
 * SeriesInputInfo
 * 
 * @author zhongyi
 *
 */
public class SeriesInputInfo implements Serializable {
    
    /**
     * 
     * serialize id
     * 
     */
    private static final long serialVersionUID = -8413476911392060290L;
    
    /**
     * SeriesUnitType
     *
     * @author david.wang
     * @version 1.0.0.1
     */
    public enum SeriesUnitType {
        
        /**
         * pie
         */
        PIE("pie"),
        
        /**
         * line
         */
        LINE("line"),
        
        /**
         * bar
         */
        BAR("bar"),
        
        /**
         * break
         */
        BEAKER("beaker"),
        
        /**
         * empty
         */
        EMPTY(""),
        
        /**
         * 
         */
        
        
        /**
         * map
         */
        MAP("map"),
        
        /**
         * column
         */
        COLUMN("column"),
        
        /**
         * TIME_TREND
         */
        TIME_TREND("TIME_TREND");
        
        /**
         * name
         */
        private String name;
        
        /**
         * 
         * @param name
         */
        private SeriesUnitType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        /**
         * 
         * @param type
         * @return
         */
        public static SeriesUnitType parseSeriesUnitType(String type) {
            if (type.equals("pie")) {
                return PIE;
            } else if (type.equals("line")) {
                return LINE;
            } else if (type.equals("bar")) {
                return BAR;
            } else if (type.equals("beaker")) {
                return BEAKER;
            } else {
                return EMPTY;
            }
        }
    }
    
    /**
     * name
     */
    private String name;
    
    /**
     * indHead
     */
    private boolean indHead;
    
    /**
     * inds
     */
    private List<Item> inds;
    
    /**
     * dims
     */
    private List<Item> dims;
    
    /**
     * type
     */
    private SeriesUnitType type;
    
    /**
     * yAxisName
     */
    private String yAxisName;
    
    /**
     * 
     * @param yAxis
     * @return
     */
    public static SeriesInputInfo generateEmptySeriesInputInfo(String yAxis) {
        
        SeriesInputInfo seriesUnit = new SeriesInputInfo();
        seriesUnit.setDims(Lists.<Item>newArrayList());
        seriesUnit.setInds(Lists.<Item>newArrayList());
        seriesUnit.setType(SeriesUnitType.LINE);
        return seriesUnit;
    }
    
    public List<Item> getInds() {
        return inds;
    }
    
    public void setInds(List<Item> inds) {
        this.inds = inds;
    }
    
    public List<Item> getDims() {
        return dims;
    }
    
    public void setDims(List<Item> dims) {
        this.dims = dims;
    }
    
    public SeriesUnitType getType() {
        return type;
    }
    
    public void setType(SeriesUnitType type) {
        this.type = type;
    }
    
    public boolean isIndHead() {
        return indHead;
    }
    
    public void setIndHead(boolean indHead) {
        this.indHead = indHead;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getyAxisName() {
        return yAxisName;
    }
    
    public void setyAxisName(String yAxisName) {
        this.yAxisName = yAxisName;
    }
    
}