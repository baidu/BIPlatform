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
package com.baidu.rigel.biplatform.ma.resource.utils;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.ElementMeta;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.IndCandicateForChart;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.LiteOlapDim;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.LiteOlapInd;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.MetaData;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.MetaStatusData;
import com.baidu.rigel.biplatform.ma.resource.view.liteolap.SelectedItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * liteOlap的视图工具类
 * 
 * @author zhongyi
 *
 */
public final class LiteOlapViewUtils {
    
    /**
     * LiteOlapViewUtils
     */
    private LiteOlapViewUtils() {
        
    }
    /**
     * 
     * @param liteOlapArea
     * @param schema
     * @return MetaData
     */
    public static MetaData parseMetaData(LiteOlapExtendArea liteOlapArea, Schema schema) {
        LogicModel logicModel = liteOlapArea.getLogicModel();
        MetaData metaData = new MetaData();
        
        Map<String, LiteOlapDim> liteOlapDims = Maps.newLinkedHashMap();
        Map<String, LiteOlapInd> liteOlapInds = Maps.newLinkedHashMap();
        /**
         * 未选中的LiteOlapDim
         */
        for (Item item : liteOlapArea.getCandDims().values()) {
            LiteOlapDim dim = parseDim(item, schema, false);
            liteOlapDims.put(dim.getName(), dim);
        }
        /**
         * 未选中的LiteOlapInd
         */
        for (Item item : liteOlapArea.getCandInds().values()) {
            LiteOlapInd ind = parseInd(item, schema, false);
            liteOlapInds.put(ind.getName(), ind);
        }
        /**
         * 选中的LiteOlapDim
         */
        for (Item item : logicModel.getItems()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    item.getCubeId(), item.getOlapElementId());
            if (element instanceof Dimension) {
                LiteOlapDim dim = parseDim(item, schema, false);
                liteOlapDims.put(dim.getName(), dim);
            } else if (element instanceof Measure) {
                LiteOlapInd ind = parseInd(item, schema, false);
                liteOlapInds.put(ind.getName(), ind);
            }
        }
        metaData.setCubeNames(new String[]{liteOlapArea.getCubeId()});
        metaData.setDims(liteOlapDims.values().toArray(new LiteOlapDim[0]));
        metaData.setInds(liteOlapInds.values().toArray(new LiteOlapInd[0]));
        metaData.setSelectCubeNum(1);
        return metaData;
    }
    
    /**
     * 
     * @param liteOlapArea
     * @param schema
     * @return MetaStatusData
     */
    public static MetaStatusData parseMetaStatusData(LiteOlapExtendArea liteOlapArea, Schema schema) {
        MetaStatusData metaStatusData = new MetaStatusData();
        LogicModel logicModel = liteOlapArea.getLogicModel();
        List<String> selectedDims = Lists.newArrayList();
        List<String> validDims = Lists.newArrayList();
        List<String> selectedInds = Lists.newArrayList();
        List<String> validInds = Lists.newArrayList();
        for (Item candItem : liteOlapArea.getCandDims().values()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    candItem.getCubeId(), candItem.getOlapElementId());
            validDims.add(element.getId());
        }
        for (Item candItem : liteOlapArea.getCandInds().values()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    candItem.getCubeId(), candItem.getOlapElementId());
            validInds.add(element.getId());
        }
        for (Item selectedItem : logicModel.getItems()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    selectedItem.getCubeId(), selectedItem.getOlapElementId());
            if (element instanceof Dimension) {
                selectedDims.add(element.getId());
            } else if (element instanceof Measure) {
                selectedInds.add(element.getId());
            }
        }
        ElementMeta dimMetas = new ElementMeta();
        dimMetas.setSelectedMetaNames(selectedDims.toArray(new String[0]));
        dimMetas.setValidMetaNames(validDims.toArray(new String[0]));
        ElementMeta indMetas = new ElementMeta();
        indMetas.setSelectedMetaNames(selectedInds.toArray(new String[0]));
        indMetas.setValidMetaNames(validInds.toArray(new String[0]));
        metaStatusData.setDimMetas(dimMetas);
        metaStatusData.setIndMetas(indMetas);
        return metaStatusData;
    }
    
    /**
     * 
     * @param item
     * @param schema
     * @param selected
     * @return LiteOlapDim
     */
    public static LiteOlapDim parseDim(Item item, Schema schema, boolean selected) {
        LiteOlapDim dim = new LiteOlapDim();
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                item.getCubeId(), item.getOlapElementId());
        dim.setCaption(element.getCaption());
        dim.setCubeName(item.getCubeId());
        dim.setName(element.getName());
        dim.setSelected(selected);
        dim.setUniqName(element.getId());
        dim.setStatus(2);
        dim.setIsConfig(true);
        return dim;
    }
    
    /**
     * 
     * @param item
     * @param schema
     * @param selected
     * @return LiteOlapInd
     */
    public static LiteOlapInd parseInd(Item item, Schema schema, boolean selected) {
        LiteOlapInd ind = new LiteOlapInd();
        OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                item.getCubeId(), item.getOlapElementId());
        ind.setCaption(element.getCaption());
        ind.setName(element.getName());
        ind.setSelected(selected);
        ind.setUniqName(element.getId());
        ind.setVisible(true);
        ind.setStatus(2);
        return ind;
    }
    
    /**
     * 
     * @param liteOlapArea
     * @param schema
     * @return Map<String, Object>
     */
    public static Map<String, Object> parseSelectedItemMap(LiteOlapExtendArea liteOlapArea, Schema schema) {
        LogicModel logicModel = liteOlapArea.getLogicModel();
        List<SelectedItem> cols = Lists.newArrayList();
        List<SelectedItem> rows = Lists.newArrayList();
        List<SelectedItem> filter = Lists.newArrayList();
        for (Item item : logicModel.getItems()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    item.getCubeId(), item.getOlapElementId());
            SelectedItem selected = parseSelectedItem(element, liteOlapArea.getCubeId());
            switch (item.getPositionType()) {
                case X:
                    rows.add(selected);
                    break;
                case Y:
                    cols.add(selected);
                    break;
                case S:
                    filter.add(selected);
                    break;
                default:
                    break;
            }
        }
        Map<String, Object> selectedMapResult = Maps.newHashMap();
        selectedMapResult.put("COLUMN", cols.toArray(new SelectedItem[0]));
        selectedMapResult.put("ROW", rows.toArray(new SelectedItem[0]));
        selectedMapResult.put("FILTER", filter.toArray(new SelectedItem[0]));
        return selectedMapResult;
    }
    
    /**
     * 
     * @param element
     * @param cubeId
     * @return SelectedItem
     */
    private static SelectedItem parseSelectedItem(OlapElement element, String cubeId) {
        SelectedItem selected = new SelectedItem();
        selected.setCaption(element.getCaption());
        selected.setName(element.getName());
        selected.setCubeName(cubeId);
        selected.setUniqName(element.getId());
        selected.setFixed(false);
        selected.setSelected(false);
        return selected;
    }
    
    /**
     * 
     * @param element
     * @return IndCandicateForChart
     */
    public static IndCandicateForChart parseIndForChart(OlapElement element) {
        IndCandicateForChart ind = new IndCandicateForChart();
        ind.setCaption(element.getCaption());
        ind.setCustIndName(element.getId());
        return ind;
    }
}
