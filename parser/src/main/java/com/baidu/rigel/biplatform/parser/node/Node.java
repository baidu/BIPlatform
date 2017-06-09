/**
 * Copyright (C) 2014 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.rigel.biplatform.parser.node;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;

/**
 * 计算节点
 * @author chenxiaoming01
 *
 */
public interface Node extends Serializable {
    
    /**
     * 返回结果
     * @return 结果 
     * @throws IllegalCompileContextException 
     */
    ComputeResult getResult(CompileContext context) throws IllegalCompileContextException;
    
    void setResult(ComputeResult result);
    
    NodeType getNodeType();
    
    Map<Condition, Set<String>> collectVariableCondition();
    
    void check();
    
    /** 
     * 是否是原型节点，基本节点条件会被上层节点条件覆盖
     * isProtoType
     * @return
     */
    default boolean isProtoType() {
        return true;
    }
    
    public enum NodeType {
        Numeric,
        Variable,
        Calculate,
        Function
    }
    
}
