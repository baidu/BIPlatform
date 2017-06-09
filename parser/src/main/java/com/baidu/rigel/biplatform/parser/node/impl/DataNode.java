/**
 * Copyright (C) 2014 Baidu, Inc. All Rights Reserved.
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
import com.baidu.rigel.biplatform.parser.exception.NodeCompileException;
import com.baidu.rigel.biplatform.parser.node.AbstractNode;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;

/**
 * 数据节点
 * @author chenxiaoming01
 *
 */
public class DataNode extends AbstractNode {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2648103012012236498L;
    

    /**
     * constructor
     * @param data
     */
    public DataNode(BigDecimal data) {
        setResult(new SingleComputeResult(data));
    }
    
    
    /** 
     * 构造函数
     */
    public DataNode(String numStr) {
        this(new BigDecimal(numStr));
    }
    
    
    
    /** 
     * 构造函数
     */
    public DataNode(int num) {
        this(new BigDecimal(num));
    }
    
    
    /** 
     * 构造函数
     */
    public DataNode(double num) {
        this(new BigDecimal(num));
    }
    
    
    /** 
     * 构造函数
     */
    public DataNode(long num) {
        this(new BigDecimal(num));
    }
    


    @Override
    public BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        throw new UnsupportedOperationException("not support compute");
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.Numeric;
    }


    @Override
    public ComputeResult getResult(CompileContext context) {
        return this.result;
        
    }


    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return this.result.toString();
    }


    @Override
    public Map<Condition, Set<String>> collect() {
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>(1);
        result.put(EmptyCondition.getInstance(), new HashSet<String>(1));
        return result;
        
    }


    @Override
    public void check() {
        if(this.result == null) {
            throw new NodeCompileException(this, "DataNode result can not be null");
        }
    }
 

}
