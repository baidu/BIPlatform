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
 * 层级类型
 * 
 * @author xiaoming.chen
 *
 */
public enum LevelType {

    /**
     * Indicates that the level is not related to time.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_REGULAR(0x0000)</code>.
     * </p>
     */
    REGULAR,

    /**
     * Indicates that a level refers to years.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_YEARS(0x0014)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_YEARS,

    /**
     * Indicates that a level refers to half years.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_HALF_YEAR(0x0304)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_HALF_YEAR,

    /**
     * Indicates that a level refers to quarters.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_QUARTERS(0x0044)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_QUARTERS,

    /**
     * Indicates that a level refers to months.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_MONTHS(0x0084)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_MONTHS,

    /**
     * Indicates that a level refers to weeks.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_WEEKS(0x0104)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_WEEKS,

    /**
     * Indicates that a level refers to days.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_DAYS(0x0204)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_DAYS,

    /**
     * Indicates that a level refers to hours.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_HOURS(0x0304)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_HOURS,

    /**
     * Indicates that a level refers to minutes.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_MINUTES(0x0404)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_MINUTES,

    /**
     * Indicates that a level refers to seconds.
     *
     * <p>
     * Corresponds to the OLE DB for OLAP constant <code>MDLEVEL_TYPE_TIME_SECONDS(0x0804)</code>.
     * </p>
     *
     * <p>
     * It must be used in a dimension whose type is {@link org.olap4j.metadata.Dimension.Type#TIME}.
     * </p>
     */
    TIME_SECONDS,

    /**
     * CALL_BACK callback 层级
     */
    CALL_BACK,

    /**
     * PARENT_CHILD 父子结构的层级
     */
    PARENT_CHILD,

    /**
     * USER_CUSTOM 用户自定义维度
     */
    USER_CUSTOM

}
