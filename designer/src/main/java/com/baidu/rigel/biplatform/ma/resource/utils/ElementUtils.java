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
package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.List;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.util.DerivativeIndUtils;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 判断指标、维度的改变状态
 * 
 * @author zhongyi
 *
 */
public final class ElementUtils {
    
    /**
     * ElementUtils
     */
    private ElementUtils() {
        
    }
    
    /**
     * 
     * @param model
     * @param cube
     * @return Set<String>
     */
    public static Set<String> getChangableDimNames(ReportDesignModel model, Cube cube) {

        Set<String> dimSet = Sets.newHashSet();
        List<Dimension> dimGroups = Lists.newArrayList();
        /**
         * get all dims and all dim groups
         */
        for (Dimension dimension : cube.getDimensions().values()) {
            if (dimension.getType() == DimensionType.STANDARD_DIMENSION) {
                dimSet.add(dimension.getName());
            } else if (dimension.getType() == DimensionType.GROUP_DIMENSION) {
                dimGroups.add(dimension);
            }
        }
        /**
         * get rid of ones used in table or chart
         */
        for (ExtendArea area : model.getExtendAreaList()) {
            for (Item item : area.listAllItems().values()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                        area.getCubeId(), item.getOlapElementId());
                if (element != null) {
                    dimSet.remove(element.getName());
                }
            }
        }
        /**
         * get rid of ones used in group dims
         */
        for (Dimension group : dimGroups) {
            for (Level level : group.getLevels().values()) {
                String dimName = level.getDimension().getName();
                dimSet.remove(dimName);
            }
        }
        return dimSet;
    }
    
    public static Set<String> getChangableIndNames(ReportDesignModel model, Cube cube) {
        Set<String> indSet = Sets.newHashSet();
        List<Measure> deriInds = Lists.newArrayList();
        /**
         * get all inds and all derivative inds
         */
        for (Measure ind : cube.getMeasures().values()) {
            if (ind.getType() == MeasureType.COMMON) {
                indSet.add(ind.getName());
            } else if (ind.getType() == MeasureType.CAL) {
                deriInds.add(ind);
            } else if (ind.getType() == MeasureType.RR) {
                deriInds.add(ind);
            } else if (ind.getType() == MeasureType.SR) {
                deriInds.add(ind);
            }
            
        }
        /**
         * get rid of ones used in table or chart
         */
        for (ExtendArea area : model.getExtendAreaList()) {
            for (Item item : area.listAllItems().values()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                        cube.getId(), item.getOlapElementId());
                if (element != null) {
                    indSet.remove(element.getName());
                }
            }
        }
        /**
         * get rid of ones used in group dims
         */
        for (Measure deriInd : deriInds) {
            List<String> oriInds = DerivativeIndUtils.getOriIndNames(deriInd);
            for (String oriInd : oriInds) {
                indSet.remove(oriInd);
            }
        }
        return indSet;
    }
}
