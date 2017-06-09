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

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Schema;

/**
 * 
 * 逻辑模型管理服务接口，用户管理逻辑模型，包括： 模型的持久化服务、模型的更新、删除服务以及对维度、指标的修改服务
 * 
 * @author david.wang
 *
 */
public interface LogicModelManageService {
    
    /**
     * 持久化存储schema信息
     * 
     * @param schema
     * @param persistence
     *            是否持久化到文件 默认为true
     * @return
     */
    public boolean saveOrUpdateSchema(Schema schema, boolean persistence);
    
    /**
     * 持久化schema
     * 
     * @param schema
     * @return
     */
    public boolean saveOrUpdateSchema(Schema schema);
    
    /**
     * 
     * 为Schema添加或者修改cube
     * 
     * @param schemaId
     * @param cube
     * @return
     */
    public boolean updateSchemaWithCube(String schemaId, Cube cube);
    
    /**
     * 批量添加或者修改cube
     * 
     * @param schemaId
     * @param cube
     * @return
     */
    public boolean updateSchemaWithCubes(String schemaId, Cube[] cube);
    
    /**
     * 删除schema中定义的cube
     * 
     * @param schemaId
     * @param cubeId
     * @return
     */
    public String removeCubeFromSchema(String schemaId, String cubeId);
    
    /**
     * 删除已经存在的schema定义
     * 
     * @param id
     * @return
     */
    public boolean removeSchema(String id);
    
}
