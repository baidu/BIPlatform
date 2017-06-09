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
package com.baidu.rigel.biplatform.tesseract.dataquery.udf;

import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.context.EmptyCondition;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.node.impl.RateFunNode;
import com.baidu.rigel.biplatform.parser.node.impl.VariableNode;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.RateCondition;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.RateCondition.RateType;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * @author david.wang
 *
 */
abstract class TesseractRateFunction extends RateFunNode {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4597180338628840464L;
    
    public TesseractRateFunction() {
    }
    
    /**
     * 
     * @param type
     * @return Map<Condition, Set<String>>
     */
    Map<Condition, Set<String>> collectVariableCondition(RateCondition.RateType type) {
        Set<String> variables = Sets.newHashSet();
        VariableNode variable = (VariableNode) this.getArgs().get(0);
        variables.add(variable.getVariableExp());
        Map<Condition, Set<String>> rs = Maps.newHashMap();
        Condition numeratorCondition = EmptyCondition.getInstance();//new RateCondition(true, type, variable.getVariableExp());
        rs.put(numeratorCondition, variables);
        Condition denominatorCondition = new RateCondition(false, type, variable.getVariableExp());
        rs.put(denominatorCondition, variables);
        return rs;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.FunctionNode#preSetNodeResult(com.baidu.rigel.biplatform.parser.context.CompileContext)
     */
    @Override
    protected void preSetNodeResult(CompileContext context) {
        VariableNode variable = (VariableNode) this.getArgs().get(0);
        Condition condition = new RateCondition(false, getType(), variable.getVariableExp());
        variable.setResult(context.getVariablesResult().get(condition).get(variable.getVariableExp()));
        
        VariableNode numeratorNodeVar = DeepcopyUtils.deepCopy(variable);
        numeratorNodeVar.setResult(context.getVariablesResult().get(EmptyCondition.getInstance()).get(variable.getVariableExp()));
        this.getArgs().set(0, numeratorNodeVar);
        this.getArgs().add(variable);
        
//        super.preSetNodeResult(context);
    }
    
    abstract RateType getType();

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.impl.RateFunNode#check()
     */
    @Override
    public void check() {
//        super.check();
        if (this.getArgs() != null && this.getArgs().size() != 1) {
            throw new IllegalStateException("该函数必须包含并且只能包含一个参数");
        }
        Node node = this.getArgs().get(0);
        if (node == null || !(node instanceof VariableNode)) {
            throw new IllegalArgumentException("错误的参数类型,当前函数只接受Variable参数");
        }
    }

    @Override
    public int getArgsLength() {
        return 1;
    }
}
