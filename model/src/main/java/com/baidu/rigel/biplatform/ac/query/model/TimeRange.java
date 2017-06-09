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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.model.TimeType;

/**
 * time range define
 * 
 * @author xiaoming.chen
 *
 */
public class TimeRange implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7667456036857706762L;
    
    /**
     * range time range
     */
    private int range;
    
    /**
     * timeType time range type
     */
    private TimeType timeType;
    
    /**
     * construct with range and time type
     * 
     * @param range time range
     * @param timeType time type
     */
    public TimeRange(int range, TimeType timeType) {
        this.range = range;
        this.timeType = timeType;
    }
    
    /**
     * getter method for property range
     * 
     * @return the range
     */
    public int getRange() {
        return range;
    }
    
    /**
     * setter method for property range
     * 
     * @param range the range to set
     */
    public void setRange(int range) {
        this.range = range;
    }
    
    /**
     * getter method for property timeType
     * 
     * @return the timeType
     */
    public TimeType getTimeType() {
        return timeType;
    }
    
    /**
     * setter method for property timeType
     * 
     * @param timeType the timeType to set
     */
    public void setTimeType(TimeType timeType) {
        this.timeType = timeType;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TimeRange [range=" + range + ", timeType=" + timeType + "]";
    }
    
}
