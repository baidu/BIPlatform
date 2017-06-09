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
package com.baidu.rigel.biplatform.ac.model;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;

/**
 * 用户配置的数据建模对象：无具体业务含义，可以认为一个schema对应一个多维数据库
 * 
 * @author xiaoming.chen
 *
 */
public interface Schema extends OlapElement {

    /**
     * 当前Schema下的cube列表
     * 
     * @return cube 名称作为KEY的map
     */
    Map<String, ? extends Cube> getCubes();

    /**
     * 获取schema对应的数据源名称
     * 
     * @return 数据源名称
     */
    String getDatasource();

    @Override
    default public String getUniqueName() {
        return MetaNameUtil.makeUniqueName(getName());
    }

}
