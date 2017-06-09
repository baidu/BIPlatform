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

import java.util.List;

import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.google.common.collect.Lists;

/**
 * 扩展区的工具类
 * @author zhongyi
 *
 */
public final class ExtendAreaUtils {
    
    /**
     * 构造函数
     */
    private ExtendAreaUtils() {
        
    }
    
    /**
     * 生成区域列表
     * 对于普通区域，列表中只有一个元素
     * 对于liteOlap区域，列表中有图、表、外壳三个元素
     * 
     * @param areaType 区域类型
     * @param referenceAreaId 依赖的区域id
     * @return 生成的区域列表
     */
    public static List<ExtendArea> genereateExtendAreas(String areaType, String referenceAreaId) {
        List<ExtendArea> areas = Lists.newArrayList();
        ExtendArea area = null;
        ExtendAreaType type = ExtendAreaType.valueOf(areaType);
        String extendAreaId = UuidGeneratorUtils.generate();
        if (type == ExtendAreaType.LITEOLAP) {
            /**
             * liteOlap由三个子组件构成
             */
            area = new LiteOlapExtendArea();
            ExtendArea selection = genereateExtendAreas(ExtendAreaType.SELECTION_AREA.name(), extendAreaId).get(0);
            ExtendArea table = genereateExtendAreas(ExtendAreaType.LITEOLAP_TABLE.name(), extendAreaId).get(0);
            ExtendArea chart = genereateExtendAreas(ExtendAreaType.LITEOLAP_CHART.name(), extendAreaId).get(0);
            areas.add(selection);
            areas.add(table);
            areas.add(chart);
            ((LiteOlapExtendArea) area).setChartAreaId(chart.getId());
            ((LiteOlapExtendArea) area).setTableAreaId(table.getId());
            ((LiteOlapExtendArea) area).setSelectionAreaId(selection.getId());
        } else {
            area = new ExtendArea();
        }
        areas.add(0, area);
        area.setId(extendAreaId);
        area.setType(ExtendAreaType.valueOf(areaType));
        area.setReferenceAreaId(referenceAreaId);
        return areas;
    }
}
