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
package com.baidu.rigel.biplatform.ma.model.external.service;

import java.util.List;

import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.external.vo.MeasureClassfyObject;

/**
 * 
 *Description: 动态指标分类表操作接口
 * @author david.wang
 *
 */
public interface MeasureClassfyService {
    
    /**
     * 能够动态变换的指标元数据描述表
     */
    final String CHANGABLE_MEASURE_TABLE = "fact_tab_col_meta_class";
    
    /**
     * 获取指标元数据分类信息
     * @param ds 数据源信息
     * @param factTab 事实表名称
     * @param areaId 表格的区域ID
     * @return List<MeasureClassfyObject> 该方法返回值包含指标
     * @throws Exception 查询异常信息
     */
    public List<MeasureClassfyObject> getChangableMeasureClassfyMeta(String factTab, 
            DataSourceDefine ds, String securityKey, String areaId) 
            throws Exception;
    
    /**
     * 依据事实表信息获取指标元数据列信息
     * @param factTable 事实表名称
     * @param ds 数据源
     * @param securityKey
     * @param areaId
     * @return 指标元数据列构成的数组
     */
    public List<String> getChangalbeMeasuerMeta(String factTable, DataSourceDefine ds,
            String securityKey, String areaId);
    
}
