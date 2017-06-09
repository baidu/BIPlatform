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
package com.baidu.rigel.biplatform.ac.minicube;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.TimeType;

/**
 * 
 * 时间维度
 * 
 * @author xiaoming.chen
 *
 */
public class TimeDimension extends MiniCubeDimension {

    /**
     * DEFAULT_TIME_YEAR_DIMENSION_NAME 系统保留的默认年到日的时间维度名称
     */
    public static final String DEFAULT_TIME_YEAR_DIMENSION_NAME = "Time_Year";

    /**
     * DEFAULT_TIME_WEEK_DIMENSION_NAME 系统保留的默认周，日层级的维度名称
     */
    public static final String DEFAULT_TIME_WEEK_DIMENSION_NAME = "Time_Weekly";

    /**
     * DEFAULT_TIME_DIMENSION_NAME 默认时间维度名称
     */
    public static final String DEFAULT_TIME_DIMENSION_NAME = "Time";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 571158052838499538L;

    /**
     * dataTimeType 事实表的时间维度格式
     */
    private TimeType dataTimeType = TimeType.TimeDay;

    /**
     * timeFormat 事实表的时间格式
     */
    private String timeFormat = dataTimeType.getFormat();

    /**
     * tableName 时间维度表的表名
     */
    private String tableName;

    /**
     * type 维度类型
     */
    private DimensionType type = DimensionType.TIME_DIMENSION;

    // /**
    // * levels 维度的层级
    // */
    // @XmlElementWrapper(name="levels")
    // private Map<String, Level> levels;

    /**
     * 需要一个维度名称的构造方法
     * 
     * @param name 维度名称
     */
    public TimeDimension(String name) {
        super(name);
        if (StringUtils.isBlank(name)) {
            this.setName(DEFAULT_TIME_DIMENSION_NAME);
        }
    }

    public TimeDimension() {
        super(null);
        this.setName(DEFAULT_TIME_DIMENSION_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Dimension#getType()
     */
    @Override
    public DimensionType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Dimension#isTimeDimension()
     */
    @Override
    public boolean isTimeDimension() {
        return true;
    }

    /**
     * getter method for property timeFormat
     * 
     * @return the timeFormat
     */
    public String getTimeFormat() {
        return timeFormat;
    }

    /**
     * setter method for property timeFormat
     * 
     * @param timeFormat the timeFormat to set
     */
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * getter method for property tableName
     * 
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * setter method for property tableName
     * 
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    // /**
    // * setter method for property levels
    // * @param levels the levels to set
    // */
    // public void setLevels(Map<String, Level> levels) {
    // this.levels = levels;
    // }

    /**
     * getter method for property dataTimeType
     * 
     * @return the dataTimeType
     */
    public TimeType getDataTimeType() {
        return dataTimeType;
    }

    /**
     * setter method for property dataTimeType
     * 
     * @param dataTimeType the dataTimeType to set
     */
    public void setDataTimeType(TimeType dataTimeType) {
        this.dataTimeType = dataTimeType;
    }

    /**
     * set type with type
     * 
     * @param type the type to set
     */
    public void setType(DimensionType type) {
        this.type = type;
    }

}
