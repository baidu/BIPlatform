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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 索引分片规则
 * 
 * @author lijin
 *
 */
public class IndexShardRule {
    /**
     * 
     * 规则类型
     * 
     * @author lijin
     *
     */
    public enum RuleType {
        /**
         * 适用于按岗位ID划分那种
         */
        RULE_TYPE_ID_MAP,
        /**
         * 适用场景： 1、每天一个分片 2、每个行业+产品线组合，一个分片
         */
        RULE_TYPE_DISTINCTCOUNTNUM,
        /**
         * 默认分片规则:默认按索引分片大小（eg:64M）分片
         */
        RULE_TYPE_DEFAULT
    }
    
    /**
     * DEFAULT_RULECOLUMNSET_VALUE
     */
    public static final String DEFAULT_RULECOLUMNSET_VALUE = "DEFAULT_RULECOLUMNSET_VALUE";
    /**
     * DEFAULT_RULEVALUESET_MAP_KEY
     */
    public static final String DEFAULT_RULEVALUESET_MAP_KEY = "DISTINCTCOUNTNUM";
    /**
     * DEFAULT_RULEVALUESET_VALUE
     */
    public static final Map<String, Object> DEFAULT_RULEVALUESET_VALUE = new HashMap<String, Object>();
    /**
     * DEFAULT_IDXSHARD_BLOCK
     */
    public static final int DEFAULT_IDXSHARD_BLOCK = 64;
    
    /**
     * init DEFAULT_RULEVALUESET_VALUE
     */
    static {
        DEFAULT_RULEVALUESET_VALUE.put(DEFAULT_RULECOLUMNSET_VALUE, DEFAULT_IDXSHARD_BLOCK);
    }
    
    /**
     * 规则类型
     */
    private RuleType ruleType;
    /**
     * 计算分片的列 1、ruleType=RULE_TYPE_DEFAULT，ruleColumnSet只有一个值：
     * DEFAULT_RULECOLUMNSET_VALUE
     * 2、除1之外，ruleColumnSet的size>=1，即可以按照某一列分片，也可以按照某几列分片
     */
    private Set<String> ruleColumnSet;
    
    /**
     * 计算分片时，每列取值情况 1、ruleType=RULE_TYPE_DEFAULT，ruleValueSet只有一个值，即：
     * DEFAULT_RULEVALUESET_VALUE
     * 2、ruleType=RULE_TYPE_ID_MAP,ruleValueSet取决取计算分片的列的取值组合
     * eg:ruleColumnSet只有pos_id
     * ，即只按运营单位划分，如果一共有5家运营单位，则：map1.put("pos_id",运营单位1的ID)，
     * map2.put("pos_id",运营单位2的ID)……
     * eg:ruleColumnSet中有pos_id,trade_id，即按照运营单位与行业的组合划分，则:
     * map1.put("pos_id",运营单位1的ID)，map1.put("trade_id",行业1的id)
     * 3、ruleType=RULE_TYPE_DISTINCTCOUNTNUM
     * ,ruleValueSet只有一个,map.put(DEFAULT_RULEVALUESET_MAP_KEY,个数);
     */
    private Set<Map<String, Object>> ruleValueSet;
    
    /**
     * IndexShardRule
     */
    public IndexShardRule() {
        super();
    }
    
    /**
     * IndexShardRule
     * 
     * @param ruleType
     *            ruleType
     * @param ruleColumnSet
     *            ruleColumnSet
     * @param ruleValueSet
     *            ruleValueSet
     */
    public IndexShardRule(RuleType ruleType, Set<String> ruleColumnSet, Set<Map<String, Object>> ruleValueSet) {
        super();
        this.ruleType = ruleType;
        this.ruleColumnSet = ruleColumnSet;
        this.ruleValueSet = ruleValueSet;
    }
    
    /**
     * getter method for property ruleType
     * 
     * @return the ruleType
     */
    public RuleType getRuleType() {
        return ruleType;
    }
    
    /**
     * setter method for property ruleType
     * 
     * @param ruleType
     *            the ruleType to set
     */
    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
    
    /**
     * getter method for property ruleColumnSet
     * 
     * @return the ruleColumnSet
     */
    public Set<String> getRuleColumnSet() {
        return ruleColumnSet;
    }
    
    /**
     * setter method for property ruleColumnSet
     * 
     * @param ruleColumnSet
     *            the ruleColumnSet to set
     */
    public void setRuleColumnSet(Set<String> ruleColumnSet) {
        this.ruleColumnSet = ruleColumnSet;
    }
    
    /**
     * getter method for property ruleValueSet
     * 
     * @return the ruleValueSet
     */
    public Set<Map<String, Object>> getRuleValueSet() {
        return ruleValueSet;
    }
    
    /**
     * setter method for property ruleValueSet
     * 
     * @param ruleValueSet
     *            the ruleValueSet to set
     */
    public void setRuleValueSet(Set<Map<String, Object>> ruleValueSet) {
        this.ruleValueSet = ruleValueSet;
    }
    
    /**
     * defaultIndexShardRule
     */
    private static IndexShardRule defaultIndexShardRule;
    
    /**
     * 
     * getDefaultIndexShardRule
     * 
     * @return IndexShardRule
     */
    public static IndexShardRule getDefaultIndexShardRule() {
        if (IndexShardRule.defaultIndexShardRule == null) {
            Set<String> ruleColumnSet = new HashSet<String>();
            ruleColumnSet.add(IndexShardRule.DEFAULT_RULECOLUMNSET_VALUE);
            Set<Map<String, Object>> ruleValueSet = new HashSet<Map<String, Object>>();
            ruleValueSet.add(IndexShardRule.DEFAULT_RULEVALUESET_VALUE);
            IndexShardRule.defaultIndexShardRule = new IndexShardRule(RuleType.RULE_TYPE_DEFAULT, ruleColumnSet,
                    ruleValueSet);
        }
        return IndexShardRule.defaultIndexShardRule;
    }
    
}
