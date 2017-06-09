
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
package com.baidu.rigel.biplatform.parser.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;

/** 
 * 一个计算列表达式编译后的结果
 * @author xiaoming.chen
 * @version  2014年12月20日 
 * @since jdk 1.8 or after
 */
public class CompileContext implements Serializable {
    
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -6073819616291336122L;
    
    private Node node;

    private Map<Condition,Set<String>> conditionVariables;
    
    private Map<Condition, Map<String, ComputeResult>> variablesResult;
    
    private String expression;
    /** 
     * 构造函数
     */
    public CompileContext(Node node) {
        super();
        this.node = node;
    }
    
    /** 
     * 获取 node 
     * @return the node 
     */
    public Node getNode() {
    
        return node;
    }

    /** 
     * 设置 node 
     * @param node the node to set 
     */
    public void setNode(Node node) {
    
        this.node = node;
    }

    /** 
     * 获取 conditionVariables 
     * @return the conditionVariables 
     */
    public Map<Condition, Set<String>> getConditionVariables() {
        if(this.conditionVariables == null) {
            this.conditionVariables = this.node.collectVariableCondition();
        }
        return conditionVariables;
    }

    /** 
     * 设置 conditionVariables 
     * @param conditionVariables the conditionVariables to set 
     */
    public void setConditionVariables(Map<Condition, Set<String>> conditionVariables) {
        this.conditionVariables = conditionVariables;
    }

    /** 
     * 获取 variablesResult 
     * @return the variablesResult 
     */
    public Map<Condition, Map<String, ComputeResult>> getVariablesResult() {
        if(this.variablesResult == null) {
            this.variablesResult = new HashMap<Condition, Map<String,ComputeResult>>(1);
        }
        return variablesResult;
    }

    /** 
     * 设置 variablesResult 
     * @param variablesResult the variablesResult to set 
     */
    public void setVariablesResult(Map<Condition, Map<String, ComputeResult>> variablesResult) {
        this.variablesResult = variablesResult;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "CompileContext [node=" + node + ", conditionVariables=" + conditionVariables + ", variablesResult="
                + variablesResult + "]";
    }

    /** 
     * 获取 expresion 
     * @return the expresion 
     */
    public String getExpression() {
    
        return expression;
    }

    /** 
     * 设置 expresion 
     * @param expresion the expresion to set 
     */
    public void setExpression(String expresion) {
    
        this.expression = expresion;
    }
    
    

}

