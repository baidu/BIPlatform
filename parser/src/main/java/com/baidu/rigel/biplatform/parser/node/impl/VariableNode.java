

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

package com.baidu.rigel.biplatform.parser.node.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.context.EmptyCondition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.exception.IllegalVariableResultException;
import com.baidu.rigel.biplatform.parser.exception.NotAllowedOperationException;
import com.baidu.rigel.biplatform.parser.node.AbstractNode;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;

/** 
 *  
 * @author chenxiaoming01 
 * @version  2014年11月24日 
 * @since jdk 1.8 or after
 */
public class VariableNode extends AbstractNode {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -5808971726720898789L;
    
    /** 
     * variableExp
     */
    private String variableExp;
    
    /** 
     * 构造函数
     */
    public VariableNode(String variableExp) {
        super();
        this.variableExp = variableExp;
    }

    /** 
     * 获取 variableExp 
     * @return the variableExp 
     */
    public String getVariableExp() {
    
        return variableExp;
    }

    /** 
     * 设置 variableExp 
     * @param variableExp the variableExp to set 
     */
    public void setVariableExp(String variableExp) {
    
        this.variableExp = variableExp;
    }

    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        throw new NotAllowedOperationException("variable node not support compute");
    }
    
    
    @Override
    public NodeType getNodeType() {
        return NodeType.Variable;
    }

    @Override
    public ComputeResult getResult(CompileContext context) throws IllegalCompileContextException {
        if(this.result != null) {
            return this.result;
        }
        checkCompileContext(context);
        if(context.getVariablesResult().containsKey(EmptyCondition.getInstance())) {
            return context.getVariablesResult().get(EmptyCondition.getInstance()).get(variableExp);
        }
        throw new IllegalVariableResultException("can not found result from context,please check context variable result:" + context.getVariablesResult());
    }
    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return variableExp;
    }

    @Override
    public Map<Condition, Set<String>> collect() {
        
        Set<String> variable = new HashSet<String>(1);
        variable.add(this.variableExp);
        
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>();
        result.put(EmptyCondition.getInstance(), variable);
        return result;
        
    }

    @Override
    public void check() {
        // nothing to check
    }


}

