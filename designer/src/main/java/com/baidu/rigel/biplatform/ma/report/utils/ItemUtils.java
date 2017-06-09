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
package com.baidu.rigel.biplatform.ma.report.utils;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.report.model.Item;

/**
 * Item 的工具类
 * @author zhongyi
 *
 */
public final class ItemUtils {
    
    /**
     * 构造函数
     */
    private ItemUtils() {
        
    }
    
    /**
     * item是否是时间维度
     * @param item
     * @param schema
     * @param cubeId
     * @return
     */
    public static boolean isTimeDim(Item item, Schema schema, String cubeId) {
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                cubeId, item.getOlapElementId());
        if (element instanceof Dimension) {
            if (((Dimension) element).getType() == DimensionType.TIME_DIMENSION) {
                return true;
            }
        }
        return false;
    }
    
    public static OlapElement getOlapElementByItem(Item item, Schema schema, String cubeId) {
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                cubeId, item.getOlapElementId());
        return element;
    }
}
