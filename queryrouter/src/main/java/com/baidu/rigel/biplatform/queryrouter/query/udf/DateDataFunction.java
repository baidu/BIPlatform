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
package com.baidu.rigel.biplatform.queryrouter.query.udf;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.context.EmptyCondition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.node.CalculateNode;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.node.impl.DataNode;
import com.baidu.rigel.biplatform.parser.node.impl.VariableNode;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;
import com.baidu.rigel.biplatform.queryrouter.query.udf.condition.DateDataCondition;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 *Description: 根据时间偏移量计算指标数据
 * @author david.wang
 *
 */
public class DateDataFunction extends FunctionNode {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -146754081406551706L;

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.FunctionNode#getName()
     */
    @Override
    public String getName() {
        return "dateData";
    }
    
    /*
     *  (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.FunctionNode#mergeCondition(com.baidu.rigel.biplatform.parser.node.Node)
     */
    @Override
    public Map<Condition, Set<String>> mergeCondition(Node node) {
        Set<String> variables = Sets.newHashSet();
        VariableNode variable = (VariableNode) this.getArgs().get(0);
        variables.add(variable.getVariableExp());
        Map<Condition, Set<String>> rs = Maps.newHashMap();
        Condition empty = EmptyCondition.getInstance();
        rs.put(empty, variables);
        CalculateNode calculateNode = (CalculateNode) this.getArgs().get(1);
        char operator = calculateNode.getOperation().getSymbol();
        DataNode dataNode = (DataNode) (calculateNode.getRight());
        SingleComputeResult result = (SingleComputeResult) dataNode.getResult(null);
        int intValue = result.getData().intValue();
        Condition condition =
                new DateDataCondition(variable.getVariableExp(), Integer.valueOf(operator + "" + intValue));
        rs.put(condition, variables);
        return rs;

    }
    
    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.FunctionNode#getResult(com.baidu.rigel.biplatform.parser.context.CompileContext)
     */
    @Override
    public ComputeResult getResult(CompileContext context) throws IllegalCompileContextException {
        Integer tmp = ((SingleComputeResult) this.getArgs().get(1).getResult(context)).getData().intValue();
        // SingleComputeResult tmp = (SingleComputeResult) dataNode.getResult (null);
        VariableNode variable = (VariableNode) this.getArgs().get(0);
        Condition condition = new DateDataCondition(variable.getVariableExp(), tmp);
        this.result = context.getVariablesResult ().get (condition).get (variable.getVariableExp ());
        if (this.result == null) {
            this.result = new SingleComputeResult();
        } 
//        else if (this.result instanceof ListComputeResult) {
//            // 进行汇总计算，并放到第一个
//            ListComputeResult rs = (ListComputeResult) this.result;
//            BigDecimal sum = BigDecimal.ZERO;
//            for (BigDecimal tmpData : rs.getData ()) {
//                if (tmpData != null) {
//                    sum = sum.add (tmpData);
//                }
//            }
//            boolean hasSum = CollectionUtils.isNotEmpty (rs.getData ()) 
//                && (rs.getData ().get (0) == null || rs.getData ().get (0).intValue () == 0);
//            if (hasSum) {
//                rs.getData ().add (0, sum);
//            }
//        }
        return this.result;
    }
    
    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.FunctionNode#getArgsLength()
     */
    @Override
    public int getArgsLength() {
        return 2;
    }
    
    @Override
    public void check() {
        if (this.getArgs() != null && this.getArgs().size() != 2) {
            throw new IllegalStateException("该函数必须包含并且只能包含两个参数");
        }
        Node node = this.getArgs().get(0);
        if (node == null || !(node instanceof VariableNode)) {
            throw new IllegalArgumentException("错误的参数类型,第一个参数只接受Variable参数");
        }
//        Node node2 = this.getArgs ().get (1);
//        if (node == null || !(node2 instanceof DataNode)) {
//            throw new IllegalArgumentException ("错误的参数类型, 第二个参数必须为数值类型常量");
//        }
    }
    
    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        return arg1;
    }
}
