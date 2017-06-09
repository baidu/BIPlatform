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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

/**
 * 平面表column类型定义：包括普通、callback以及时间字段
 *
 * @author 罗文磊
 * @version 1.0.0.1
 */
public enum ColumnType {
    /**
     * 通用指标
     */
    COMMON,
    /**
     * group字段
     */
    GROUP,
    /**
     * 计算列字段
     */
    CAL,
    /**
     * 需要join关联的字段
     */
    JOIN,
    /**
     * 时间
     */
    TIME,
    /**
     * 回调维度
     */
    CALLBACK,
    
    /**
     * 回调指标
     */
    MEASURE_CALLBACK
}
