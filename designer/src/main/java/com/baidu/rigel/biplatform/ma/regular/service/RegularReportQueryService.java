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
package com.baidu.rigel.biplatform.ma.regular.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 *Description:
 * @author david.wang
 *
 */
public interface RegularReportQueryService {
    
    /**
     * 根据报表模型获取报表中所有数据区域对应的数据
     * @param model 报表模型
     * @param dsInfo 数据源信息
     * @param params 参数信息
     * @return Map<String, DataModel> key为区域模型id， model为报表模型数据
     */
    Map<String, DataModel> queryReportData(DataSourceInfo dsInfo, ReportDesignModel model, Map<String, String> params);
}
