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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.util.Map;

/**
 * 
 * 维度划分规则
 * @author lijin
 *
 */
public class DimRule {
    /**
     * 维度名称
     */
    private String dimName;
    /**
     * 规则类型：精确、范围
     */
    private int ruleType;
    /**
     * 规则
     * 范围：start:start_value;end:end_value
     * 精确: start:value;end_value
     */
    private Map<String, Object> ruleMap;
    
    /**
     * getter method for property dimName
     * @return the dimName
     */
    public String getDimName() {
        return dimName;
    }
    /**
     * setter method for property dimName
     * @param dimName the dimName to set
     */
    public void setDimName(String dimName) {
        this.dimName = dimName;
    }
    /**
     * getter method for property ruleType
     * @return the ruleType
     */
    public int getRuleType() {
        return ruleType;
    }
    /**
     * setter method for property ruleType
     * @param ruleType the ruleType to set
     */
    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }
    /**
     * getter method for property ruleMap
     * @return the ruleMap
     */
    public Map<String, Object> getRuleMap() {
        return ruleMap;
    }
    /**
     * setter method for property ruleMap
     * @param ruleMap the ruleMap to set
     */
    public void setRuleMap(Map<String, Object> ruleMap) {
        this.ruleMap = ruleMap;
    }
    
    
    
    
}
