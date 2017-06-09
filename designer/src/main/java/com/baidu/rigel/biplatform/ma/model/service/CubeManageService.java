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
package com.baidu.rigel.biplatform.ma.model.service;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Schema;

/**
 * 
 * 立方体逻辑模型管理服务
 * 
 * @author david.wang
 *
 */
public interface CubeManageService {
    
    /**
     * 修改维度定义
     * 
     * @param schemaId
     * @param cubeId
     * @param dim
     * @return
     */
    public Schema modifyDimension(String schemaId, String cubeId, Dimension dim);
    
    /**
     * 
     * 修改指标定义
     * 
     * @param schemaId
     * @param cubeId
     * @param measure
     * @return
     */
    public Schema modifyMeasure(String schemaId, String cubeId, Measure measure);
    
    /**
     * 将指标转换为维度
     * 
     * @param schemaId
     * @param cubeId
     * @param measure
     * @return
     */
    public Schema converMeasure2Dim(String schemaId, String cubeId, Measure measure);
    
    /**
     * 将维度转换成指标
     * 
     * @param schemaId
     * @param cubeId
     * @param dim
     * @return
     */
    public Schema convertDim2Measure(String schemaId, String cubeId, Dimension dim);
    
}
