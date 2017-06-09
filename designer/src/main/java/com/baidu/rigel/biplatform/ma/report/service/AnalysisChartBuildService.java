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
package com.baidu.rigel.biplatform.ma.report.service;

import java.util.List;

import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;

/**
 * 
 * 图形构建服务
 * @author zhongyi
 *
 * 2014-8-12
 */
public interface AnalysisChartBuildService {

    /**
     * 生成趋势图的逻辑模型
     * @param sourceLogicModel
     * @param schema
     * @param cubeId
     * @param row
     * @param cols
     * @param timeDimItem
     * @return
     * @throws QueryModelBuildException 
     */
    LogicModel generateTrendChartModel(LogicModel sourceLogicModel, Schema schema, String cubeId,
            List<Item> row, List<Item> cols, Item timeDimItem) throws QueryModelBuildException;
}