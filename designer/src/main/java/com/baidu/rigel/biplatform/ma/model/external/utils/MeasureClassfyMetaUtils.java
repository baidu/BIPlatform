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
package com.baidu.rigel.biplatform.ma.model.external.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ma.model.external.vo.MeasureClassfyObject;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.google.common.collect.Lists;

/**
 * 
 *Description:
 * @author david.wang
 *
 */
public final class MeasureClassfyMetaUtils {
    
    private MeasureClassfyMetaUtils() {
        
    }
    
    /**
     * 修改备选指标选择状态
     * @param source
     * @param logicModel
     * @param cube
     * @param measureClassfyMetas
     * @return List<MeasureClassfyObject>
     */
    public static List<MeasureClassfyObject> changeIndMetaSelectStatus(String source,
            LogicModel logicModel, Cube cube,
            List<MeasureClassfyObject> measureClassfyMetas) {
        List<MeasureClassfyObject> tmp = Lists.newArrayList ();
        for (MeasureClassfyObject meta : measureClassfyMetas) {
            tmp.addAll (getLeafMeasureMeta(meta));
        }
        MiniCubeMeasure m = null;
        for (Item item : logicModel.getColumns ()) {
            m = (MiniCubeMeasure) cube.getMeasures ().get (item.getOlapElementId ());
            for (MeasureClassfyObject leaf : tmp) {
                if (leaf.isSelected () == null || leaf.isSelected ()) {
                    continue;
                }
                if (m.getName ().equals (leaf.getName ()) || (source + "." + m.getName ()).equals (leaf.getName ())) {
                    leaf.setSelected (true);
                    break;
                }
            } // end inner for loop
        }
        return measureClassfyMetas;
    }
    
    /**
     * 
     * @param meta
     * @return
     */
    public static List<MeasureClassfyObject> getLeafMeasureMeta(MeasureClassfyObject meta) {
        if (meta == null) {
            throw new IllegalArgumentException ("meta 为空");
        }
        List<MeasureClassfyObject> rs = Lists.newArrayList ();
        if (CollectionUtils.isEmpty (meta.getChildren ())) {
            rs.add (meta);
        } else {
            for (MeasureClassfyObject child : meta.getChildren ()) {
                rs.addAll (getLeafMeasureMeta (child));
            }
        }
        return rs;
    }
}
