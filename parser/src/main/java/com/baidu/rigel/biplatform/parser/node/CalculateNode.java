
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.exception.NodeCompileException;
import com.baidu.rigel.biplatform.parser.exception.NotAllowedOperationException;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月19日 
 * @since jdk 1.8 or after
 */
public abstract class CalculateNode extends AbstractNode {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 5911795968489003342L;
    
    
    private Node left;
    
    private Node right;
    
    /**
     * 计算的方式
     * @author chenxiaoming01
     *
     */
    public enum CalculateOperation {
        Add('+', 1), Subtract('-', 1), Multiply('*', 2), Divide('/', 2);

        /**
         * symbol 计算符号
         */
        private char symbol;
        
        
        /** 
         * priority 优先级
         */
        private int priority;

        private CalculateOperation(char symbol, int priority) {
            this.symbol = symbol;
            this.priority = priority;
        }

        /**
         * get symbol
         * 
         * @return the symbol
         */
        public char getSymbol() {
            return symbol;
        }
        
        /** 
         * getPriority
         * @return
         */
        public int getPriority() {
            return priority;
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.Calculate;
    }
    
    @Override
    public boolean isProtoType() {
        return false;
    }
    
    /** 
     * processNodes
     * @throws IllegalCompileContextException 
     */
    protected void processNodes(CompileContext context) throws IllegalCompileContextException {
        if(left == null || right == null) {
            throw new NotAllowedOperationException("calculate argument is illegal.left arg:" + left + " right arg:" + right);
        }else {
            List<Node> nodes = new ArrayList<Node>();
            nodes.add(left);
            nodes.add(right);
            super.processNodes(nodes, context);
        }
    }
    
    
    /** 
     * 获取 result 
     * @return the result 
     * @throws IllegalCompileContextException 
     */
    @Override
    public ComputeResult getResult(CompileContext context) throws IllegalCompileContextException {
        processNodes(context);
        if(this.result == null) {
            this.result = new SingleComputeResult();
        }
        return this.result;
    }

    
    /** 
     * getOperation
     * @return
     */
    public abstract CalculateOperation getOperation();

    /** 
     * 获取 left 
     * @return the left 
     */
    public Node getLeft() {
        return left;
    }

    /** 
     * 设置 left 
     * @param left the left to set 
     */
    public void setLeft(Node left) {
    
        this.left = left;
    }

    /** 
     * 获取 right 
     * @return the right 
     */
    public Node getRight() {
    
        return right;
    }

    /** 
     * 设置 right 
     * @param right the right to set 
     */
    public void setRight(Node right) {
    
        this.right = right;
    }
    
    @Override
    public Map<Condition, Set<String>> collect() {
        Map<Condition, Set<String>> leftCondition = left.collectVariableCondition();
        Map<Condition, Set<String>> rightCondition = right.collectVariableCondition();
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>();
        
        result.putAll(leftCondition);
        if(MapUtils.isEmpty(leftCondition)) {
            result.putAll(rightCondition);
        } else if (MapUtils.isNotEmpty(rightCondition)){
            rightCondition = new HashMap<Condition, Set<String>>(rightCondition);
            for(Entry<Condition, Set<String>> entry : result.entrySet()) {
                if(rightCondition.containsKey(entry.getKey())) {
                    entry.getValue().addAll(rightCondition.get(entry.getKey()));
                    rightCondition.remove(entry.getKey());
                }
            }
            if(MapUtils.isNotEmpty(rightCondition)) {
                result.putAll(rightCondition);
            }
        }
        return result;
        
    }
    
    @Override
    public void check() {
        if(left == null || right == null) {
            throw new NodeCompileException(this, "calculate argument can not be null");
        }
        left.check();
        right.check();
    }

    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "("+getLeft().toString() + " " + getOperation().symbol+ " "  + getRight().toString() + ")";
    }
}

