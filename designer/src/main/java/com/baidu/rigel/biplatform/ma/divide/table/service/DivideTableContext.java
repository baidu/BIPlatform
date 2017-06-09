

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
 * 平面表分表查询上下文
 * @author yichao.jiang 
 * @version  2015年6月18日 
 * @since jdk 1.8 or after
 */
public class DivideTableContext {  
    
    /**
     * 分表服务
     */
    private DivideTableService divideTableService;

    /** 
     * 获取 divideTableService 
     * @return the divideTableService 
     */
    public DivideTableService getDivideTableService() {
    
        return divideTableService;
    }

    /** 
     * 设置 divideTableService 
     * @param divideTableService the divideTableService to set 
     */
    public void setDivideTableService(DivideTableService divideTableService) {
    
        this.divideTableService = divideTableService;
    }
    
    /**
     * 获取所有事实表名称
     * getAllFactTableName
     * @param divideTableStrategy
     * @param condition
     * @return
     */
    public String getAllFactTableName(DivideTableStrategyVo divideTableStrategy, Map<String, Object> condition) {
        return this.divideTableService.getAllFactTableName(divideTableStrategy, condition);
    }
}

