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

import java.util.HashSet;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.model.Aggregator;

/**
 * 
 * 衍生指标（计算成员）定义：衍生指标（计算成员）是指在原有基础指标基础之上通过一定的计算表达式完成的计算指标
 * @author david.wang
 * @version 1.0.0.1
 */
public class ExtendMinicubeMeasure extends MiniCubeMeasure {

    /**
     * ExtendMinicubeMeasure.java -- long
     * description:
     */
    private static final long serialVersionUID = 3186753384022835609L;
    
    /**
     * formula
     */
    private String formula;
    
    /**
     * refIndNames
     */
    private Set<String> refIndNames;

    /**
     * constructor
     * @param name
     * ExtendMinicubeMeasure
     */
    public ExtendMinicubeMeasure(String name) {
        super(name);
    }

    /**
     * eg: (${csm} + ${cnt}) / ${cnt}
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }

    /**
     * @param formula the formula to set
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * 
     * @return the refIndNames
     * 
     */
    public Set<String> getRefIndNames() {
        if (this.refIndNames == null) {
            this.refIndNames = new HashSet<String>();
        }
        return refIndNames;
    }

    /**
     * 
     * @param refIndNames the refIndNames to set
     * 
     */
    public void setRefIndNames(Set<String> refIndNames) {
        this.refIndNames = refIndNames;
    }

    
    @Override
    public Aggregator getAggregator() {
        return Aggregator.CALCULATED;
        
    }

    
}
