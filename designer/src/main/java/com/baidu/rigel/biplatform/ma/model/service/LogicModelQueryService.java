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
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.Schema;

/**
 * 
 * 逻辑模型查询服务，提供对逻辑模型的查询操作的支持，包括： 查询schema，查询schema包含的cube，查询cube中的维度
 * 指标信息以及获取维度、指标的成员信息等
 * 
 * @author david.wang
 *
 */
public interface LogicModelQueryService {
    /**
     * 依据schema id 获取schema定义信息
     * 
     * @param id
     * @return
     */
    public Schema getSchema(String schemaId);
    
    /**
     * 获取Schema中包含的cube定义信息
     * 
     * @param id
     * @return
     */
    public Cube[] getCubes(String schemaId);
    
    /**
     * 获取cube中维度定义信息
     * 
     * @param schemaId
     * @param cubeId
     * @return
     */
    public Dimension[] getDimensions(String schemaId, String cubeId);
    
    /**
     * 获取cube中指标定义信息
     * 
     * @param schemaId
     * @param cubeId
     * @return
     */
    public Measure[] getMeasures(String schemaId, String cubeId);
    
    /**
     * 获取维度层级关系定义信息
     * 
     * @param schemaId
     * @param cubeId
     * @param dimId
     * @return
     */
    public Level[] getLevels(String schemaId, String cubeId, String dimId);
    
    /**
     * 获取维度的成员信息
     * 
     * @param schemaId
     * @param cubeId
     * @param dimId
     * @return
     */
    public Member[] getMembers(String schemaId, String cubeId, String dimId);
    
    /**
     * 获取维度的成员信息
     * 
     * @param schemaId
     * @param cubeId
     * @param dimId
     * @param levelId
     * @return
     */
    public Member[] getMembers(String schemaId, String cubeId, String dimId, String levelId);
}
