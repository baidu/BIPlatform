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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.service.AnalysisChartBuildService;
import com.baidu.rigel.biplatform.ma.report.utils.ItemUtils;
import com.google.common.collect.Lists;

/**
 * 
 * 统计图服务实现
 * @author zhongyi
 *
 *         2014-8-12
 */
@Service("analysisChartBuildService")
public class AnalysisChartBuildServiceImpl implements AnalysisChartBuildService {
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.AnalysisChartBuildService
     * #generateTrendChartModel
     * (com.baidu.rigel.biplatform.ma.report.model.LogicModel)
     */
    @Override
    public LogicModel generateTrendChartModel(LogicModel sourceLogicModel,
                Schema schema, String cubeId, List<Item> row, 
                List<Item> cols, Item timeDimItem) throws QueryModelBuildException {
                
        /**
         * 表中行上的维度要放到图中的系列中
         * 每一行一个系列，对应一个logicModel
         */
        LogicModel chartModel = new LogicModel();
        List<Item> chartCols = Lists.newArrayList();
        for (Item item : row) {
            if (!item.equals(timeDimItem)) {
                item.setPositionType(PositionType.Y);
                chartCols.add(item);
            }
        }
        for (Item item : cols) {
            if (item != null && !item.equals(timeDimItem)) {
                item.setPositionType(PositionType.Y);
                chartCols.add(item);
            }
        }
        chartModel.addColumns(chartCols.toArray(new Item[0]));
        List<Item> sliceItems = Lists.newArrayList();
        for (Item item : sourceLogicModel.getSlices()) {
            if (ItemUtils.isTimeDim(item, schema, cubeId)) {
                timeDimItem = item;
                continue;
            }
            item.setPositionType(PositionType.S);
            sliceItems.add(item);
        }
        if (timeDimItem == null) {
            throw new QueryModelBuildException("No time dim found! ");
        }
        chartModel.addSlices(sliceItems.toArray(new Item[0]));
        timeDimItem.setPositionType(PositionType.X);
        Item[] time = new Item[]{timeDimItem};
        chartModel.addRows(time);
        chartModel.setId(sourceLogicModel.getId() + "_" + 1);
        return chartModel;
    }
    
}