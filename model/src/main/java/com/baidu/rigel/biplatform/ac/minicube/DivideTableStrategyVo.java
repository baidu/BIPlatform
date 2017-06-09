

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

package com.baidu.rigel.biplatform.ac.minicube;

import java.io.Serializable;

/** 
 * 分表策略
 * @author yichao.jiang 
 * @version  2015年6月17日 
 * @since jdk 1.8 or after
 */
public class DivideTableStrategyVo implements Serializable{

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 分表类型，如时间、地区、岗位等
     */
    private String type;
    
    /**
     * 分表粒度
     */
    private String condition;
    
    /**
     * 表名前缀
     */
    private String prefix;

    /** 
     * 获取 type 
     * @return the type 
     */
    public String getType() {
    
        return type;
    }

    /** 
     * 设置 type 
     * @param type the type to set 
     */
    public void setType(String type) {
    
        this.type = type;
    }

    /** 
     * 获取 condition 
     * @return the condition
     */
    public String getCondition() {
    
        return condition;
    }

    /** 
     * 设置 condition 
     * @param condition the condition to set 
     */
    public void setCondition(String condition) {
    
        this.condition = condition;
    }

    /** 
     * 获取 prefix 
     * @return the prefix 
     */
    public String getPrefix() {
    
        return prefix;
    }

    /** 
     * 设置 prefix 
     * @param prefix the prefix to set 
     */
    public void setPrefix(String prefix) {
    
        this.prefix = prefix;
    }
}


