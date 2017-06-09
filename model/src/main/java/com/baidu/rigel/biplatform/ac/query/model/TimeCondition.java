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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.model.TimeType;

/**
 * 问题模型中的时间条件 其中start，end，timeRange至少有2个不能为null <li>start end
 * 忽略timeRange，根据start和end解析出来的时间确定范围</li> <li>start timeRange 根据start数值算出时间范围，如
 * start:2014-07-28 timeRange : -2D 时间范围就是： start 2014-07-26 end 2014-07-28</li>
 * <li>end timeRange同上</li>
 * 
 * @author xiaoming.chen
 *
 */
public class TimeCondition implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7475687204338022222L;
    
    /**
     * start 其实日期，格式为 yyyy-MM-dd HH:mm:ss
     */
    private String start;
    
    /**
     * end 格式同start
     */
    private String end;
    
    /**
     * range 可以为空
     */
    private TimeRange timeRange;
    
    /**
     * timeGranularity 时间类型，默认天粒度
     */
    private TimeType timeGranularity = TimeType.TimeDay;
    
    /**
     * no args construct
     */
    public TimeCondition() {
    }
    
    /**
     * constart with start,timerange,timegranularity
     * 
     * @param start time start
     * @param timeRange time range
     * @param timeGranularity time granularity
     */
    public TimeCondition(String start, TimeRange timeRange, TimeType timeGranularity) {
        this(start, null, timeRange, timeGranularity);
    }
    
    /**
     * constart with start,end,timegranularity
     * 
     * @param start time start
     * @param end time end
     * @param timeGranularity time granularity
     */
    public TimeCondition(String start, String end, TimeType timeGranularity) {
        this(start, end, null, timeGranularity);
    }
    
    /**
     * constart with start,end default timetype day
     * 
     * @param start time start
     * @param end time end
     */
    public TimeCondition(String start, String end) {
        this(start, end, null, TimeType.TimeDay);
    }
    
    /**
     * constart with start,end,timerange,timegranularity
     * 
     * @param start time start
     * @param end time end
     * @param timeRange time range
     * @param timeGranularity time granularity
     */
    public TimeCondition(String start, String end, TimeRange timeRange, TimeType timeGranularity) {
        this.start = start;
        this.end = end;
        this.timeRange = timeRange;
        this.timeGranularity = timeGranularity;
    }
    
    /**
     * getter method for property start
     * 
     * @return the start
     */
    public String getStart() {
        return start;
    }
    
    /**
     * setter method for property start
     * 
     * @param start the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }
    
    /**
     * getter method for property end
     * 
     * @return the end
     */
    public String getEnd() {
        return end;
    }
    
    /**
     * setter method for property end
     * 
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }
    
    /**
     * getter method for property timeRange
     * 
     * @return the timeRange
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }
    
    /**
     * setter method for property timeRange
     * 
     * @param timeRange the timeRange to set
     */
    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }
    
    /**
     * getter method for property timeGranularity
     * 
     * @return the timeGranularity
     */
    public TimeType getTimeGranularity() {
        return timeGranularity;
    }
    
    /**
     * setter method for property timeGranularity
     * 
     * @param timeGranularity the timeGranularity to set
     */
    public void setTimeGranularity(TimeType timeGranularity) {
        this.timeGranularity = timeGranularity;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TimeCondition [start=" + start + ", end=" + end + ", timeRange=" + timeRange
            + ", timeGranularity=" + timeGranularity + "]";
    }
    
}