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
import java.util.LinkedHashMap;

import com.google.common.collect.Maps;

/**
 * 
 * 时间控件所在区域逻辑模型，区别于普通逻辑模型：
 *  时间控件所在区域无行、列、过滤轴概念
 *  时间控件逻辑模型需要粒度信息描述
 *  
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class TimerAreaLogicModel extends LogicModel {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 2692928208926779246L;
    
    /**
     * 时间维度层次结构，value表示默认区域范围以及时间维度对应的时间粒度
     */
    private LinkedHashMap<Item, TimeRange> timeDimensions;
    
    /**
     * 构造函数
     * 
     * TimerAreaLogicModel
     */
    public TimerAreaLogicModel() {
        this.timeDimensions = Maps.newLinkedHashMap();
    }
    
    /**
     * @return the timeDimensions
     */
    public LinkedHashMap<Item, TimeRange> getTimeDimensions() {
        return timeDimensions;
    }


    /**
     * @param timeDimensions the timeDimensions to set
     */
    public void setTimeDimensions(LinkedHashMap<Item, TimeRange> timeDimensions) {
        this.timeDimensions = timeDimensions;
    }
    
    /**
     * 
     * @param item
     */
    public void removeTimeDimension(Item item) {
        this.timeDimensions.remove(item);
    }

    /**
     * 
     * @param item
     * @param defaultValue
     */
    public void addTimeDimension(Item item, TimeRange defaultValue) {
        this.timeDimensions.put(item, defaultValue);
    }


    /**
     * 时间粒度单位
     *
     * @author david.wang
     * @version 1.0.0.1
     */
    public static enum TimeUnit {
        
        /**
         * 年
         */
        Y,
        
        /**
         * 季度
         */
        Q,
        
        /**
         * 月
         */
        M,
        
        /**
         * 星期
         */
        W,
        
        /**
         * 天
         */
        D;
    }
    
    
    /**
     * 
     * 时间控件表示范围：如果表示当天，起止时间一致，时间范围表示完整的时间闭区间
     *
     * @author david.wang
     * @version 1.0.0.1
     */
    public static class TimeRange implements Serializable {

        /**
         * TimerAreaLogicModel.java -- long
         * description:
         */
        private static final long serialVersionUID = -7645009802244976383L;
        
        /**
         * 开始时间
         */
        public final String begin;
        
        /**
         * 终止时间
         */
        public final String end;
        
        /**
         * 时间数据格式
         */
        public final String pattern;
        
        /**
         * 时间单位
         */
        public final TimeUnit timeUnit;

        /**
         * 
         * @param begin 开始时间
         * @param end 终止时间
         * @param pattern 时间格式
         * @param timeUnit 时间单位
         * TimeRange
         */
        public TimeRange(String begin, String end, String pattern, TimeUnit timeUnit) {
            super();
            this.begin = begin;
            this.end = end;
            this.pattern = pattern;
            this.timeUnit = timeUnit;
        }
        
    }
}
