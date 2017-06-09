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
package com.baidu.rigel.biplatform.ac.model;

/**
 * 
 * 
 * @author david.wang
 *
 */
public interface TimeDimCommonKeystore {
    /**
     * YEAR_TIME_NAME
     */
    String YEAR_TIME_NAME = "TimeYear";
    
    /**
     * TIME_HALF_YEAR
     */
    String TIME_HALF_YEAR = "TimeHalfYear";
    /**
     * QUARTER_TIME_NAME
     */
    String QUARTER_TIME_NAME = "TimeQuarter";
    
    /**
     * WEEK_TIME_NAME
     */
    String WEEK_TIME_NAME = "TimeWeekly";
    
    /**
     * HOUR_TIME_NAME
     */
    String HOUR_TIME_NAME = "TimeHour";
    
    /**
     * MINUTE_TIME_NAME
     */
    String MINUTE_TIME_NAME = "TimeMinute";
    
    /**
     * SECOND_TIME_NAME
     */
    String SECOND_TIME_NAME = "TimeSecond";
    
    /**
     * DEFAULT_TIME_FORMAT
     */
    String DEFAULT_TIME_FORMAT = "yyyy-MM-dd";
}
