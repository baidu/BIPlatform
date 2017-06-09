
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
package com.baidu.rigel.biplatform.parser.node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.exception.IllegalFunctionArgumentException;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;


/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月2日 
 * @since jdk 1.8 or after
 */
public abstract class FunctionNode extends AbstractNode {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 253967588676769399L;
    
    /** 
     * args function args
     */
    private List<Node> args;
    
//    private int argsLength;

    /** 
     * 构造函数
     */
    public FunctionNode(Node... nodes) {
        this.args = Arrays.asList(nodes);
//        this.argsLength = nodes.length;
    }
    
    
//    public FunctionNode(int length) {
//        super();
//        this.argsLength = length;
//    }
    
    /** 
     * 返回函数的名称
     * @return the name 
     */
    public abstract String getName();

    /** 
     * 获取 args 
     * @return the args 
     */
    public List<Node> getArgs() {
        if(CollectionUtils.isEmpty(args)) {
            this.args = new ArrayList<Node>();
        }
        return args;
    }
    
    
    @Override
    public Map<Condition, Set<String>> collect() {
        
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>();
        if(CollectionUtils.isNotEmpty(args)) {
            Map<Condition, Set<String>> condition = null;
            for(Node node : args) {
                // 扔到新集合中，避免对原来的有影响
                condition = new HashMap<Condition, Set<String>>(mergeCondition(node));
                if(result.isEmpty()) {
                    result.putAll(condition);
                } else if (MapUtils.isNotEmpty(condition)){
                    for(Entry<Condition, Set<String>> entry : result.entrySet()) {
                        if(condition.containsKey(entry.getKey())) {
                            entry.getValue().addAll(condition.get(entry.getKey()));
                            condition.remove(entry.getKey());
                        }
                    }
                    if(MapUtils.isNotEmpty(condition)) {
                        result.putAll(condition);
                    }
                }
            }
        }
        return result;
    }
    
    public abstract Map<Condition,Set<String>> mergeCondition(Node node);
    
    
    @Override
    public NodeType getNodeType() {
        return NodeType.Function;
    }


    /** 
     * 设置 args 
     * @param args the args to set 
     */
    public void setArgs(List<Node> args) {
        if(CollectionUtils.isEmpty(args)) {
            throw new IllegalArgumentException("function args is empty,illegal.");
        }
        this.args = args;
    }

    @Override
    public boolean isProtoType() {
        return false;
    }
    
    
    /*
     * 默认实现的函数的结果集处理方案
     * 
     */
    @Override
    public abstract ComputeResult getResult(CompileContext context) throws IllegalCompileContextException; 
//    {
//        //预处理下函数的参数，比如从context中根据函数的条件获取变量的值
//        preSetNodeResult(context);
//        processNodes(args, context);
//        if(this.result == null) {
//            this.result = new SingleComputeResult();
//        }
//        return this.result;
//    }

    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        throw new UnsupportedOperationException ();
    }
//    /** 
//     * 函数的参数如果需要特殊设置，覆盖此方法
//     * preSetNodeResult
//     * @param context
//     */
//    protected void preSetNodeResult(CompileContext context) {
//        
//    }
    
    /** 
     * 校验函数的参数，并且处理函数的参数节点
     * preCheckArgResult
     * @param args
     * @param context
     */
    @Override
    public void check() {
        if(getArgs().size() != this.getArgsLength ()) {
            throw new IllegalFunctionArgumentException(this.getName(),this.args,this.getArgsLength ());
        }
        if(CollectionUtils.isNotEmpty(this.args)) {
            for (Node node : args) {
                node.check();
            }
        }
    }

    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return getName() + "(" + args +")";
    }


    /** 
     * 获取 argsLength 
     * @return the argsLength 
     */
    public abstract int getArgsLength() ;
//    {
//     
//        return argsLength;
//    }


    /** 
     * 设置 argsLength 
     * @param argsLength the argsLength to set 
     */
//    public void setArgsLength(int argsLength) {
//        this.argsLength = argsLength;
//    }

}

