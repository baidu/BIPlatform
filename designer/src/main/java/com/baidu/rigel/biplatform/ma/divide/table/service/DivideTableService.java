

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

package com.baidu.rigel.biplatform.ma.divide.table.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;


/** 
 * 数据分表策略
 * @author yichao.jiang 
 * @version  2015年6月17日 
 * @since jdk 1.8 or after
 */
public interface DivideTableService {

    /**
     * 获取对应分表策略下的所有事实表名称
     * getAllFactTableName
     * @param divideTableStrategy 分表策略
     * @param context 查询上下文
     * @return 所有事实表名称
     */
    public String getAllFactTableName(DivideTableStrategyVo divideTableStrategy, Map<String, Object> context);
}

