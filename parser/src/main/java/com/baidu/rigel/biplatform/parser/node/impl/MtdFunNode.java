

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
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.context.EmptyCondition;
import com.baidu.rigel.biplatform.parser.context.StringCondition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.exception.NodeCompileException;
import com.baidu.rigel.biplatform.parser.exception.NotAllowedOperationException;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;

/** 
 * 未实现，无效的mtd函数
 * @author chenxiaoming01 
 * @version  2014年11月24日 
 * @since jdk 1.8 or after
 */
@Deprecated
public class MtdFunNode extends FunctionNode {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 2729972626803503825L;
    
    public static final StringCondition MTD_CONDITION = new StringCondition();
    
    static {
        MTD_CONDITION.getConditions().put("time", "0M");
    }
    
    
    public MtdFunNode(VariableNode node) {
        super(node);
    }
    
    public MtdFunNode() {
//        super(1);
    }

    @Override
    public String getName() {
        return "MTD";
    }

    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        throw new NotAllowedOperationException("mtd function not support compute");
    }

    

    @Override
    public Map<Condition, Set<String>> mergeCondition(Node arg) {
        // 修改时间条件
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>();
        Map<Condition, Set<String>> condition = arg.collectVariableCondition();
        result.put(MTD_CONDITION, condition.get(EmptyCondition.getInstance()));
        return result;
    }

    @Override
    public void check() {
        super.check();
        Node node = getArgs().get(0);
        if(!node.getNodeType().equals(NodeType.Variable)) {
            throw new NodeCompileException(this,"mtd function only support one variable argument");
        }
    }
    
    /** 
     * 函数的参数如果需要特殊设置，覆盖此方法
     * preSetNodeResult
     * @param context
     */
//    @Override
//    protected void preSetNodeResult(CompileContext context) {
//        VariableNode node = (VariableNode) getArgs().get(0);
//        super.checkCompileContext(context);
//        Map<String, ComputeResult> result = context.getVariablesResult().get(MTD_CONDITION);
//        node.setResult(result.get(node.getVariableExp()));
//    }

    @Override
    public ComputeResult getResult(CompileContext context)
            throws IllegalCompileContextException {
        return null;
    }

    @Override
    public int getArgsLength() {
        return 1;
    }


    

}

