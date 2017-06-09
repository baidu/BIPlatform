/**
 * Copyright (C) 2014 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.rigel.biplatform.parser.node.impl;

import java.math.BigDecimal;

import com.baidu.rigel.biplatform.parser.exception.NodeCompileException;
import com.baidu.rigel.biplatform.parser.node.CalculateNode;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;
import com.baidu.rigel.biplatform.parser.util.ParserConstant;




/**
 * 需要进行计算的节点
 * 
 * @author chenxiaoming01
 *
 */
public class DivideCalculateNode extends CalculateNode {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5875038668013750461L;

    /**
     * get operation
     * 
     * @return the operation
     */
    public CalculateOperation getOperation() {
        return CalculateOperation.Divide;
    }
    
    @Override
    public void check() {
        super.check();
        // 检测是否除以了常量0
        if(this.getRight().getNodeType().equals(NodeType.Numeric)) {
            SingleComputeResult singleResult = (SingleComputeResult) this.getRight().getResult(null);
            if (BigDecimal.ZERO.equals(singleResult.getData())) {
//                throw new NodeCompileException(this, "divide calcaulate denominator can not be zero.");
            }
        }
    }

    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        if(BigDecimal.ZERO.equals(arg2)) {
            return null;
        }
        // divede 0 exception no check
        try {
            return arg1.divide(arg2, ParserConstant.COMPUTE_SCALE, BigDecimal.ROUND_HALF_UP);
        } catch (ArithmeticException e) {
            return null;
            
        }
    }
    
    

}
