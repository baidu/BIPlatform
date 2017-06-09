/**
 * Copyright (C) 2014 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.rigel.biplatform.parser.node.impl;

import java.math.BigDecimal;

import com.baidu.rigel.biplatform.parser.node.CalculateNode;




/**
 * 需要进行计算的节点
 * 
 * @author chenxiaoming01
 *
 */
public class SubtractCalculateNode extends CalculateNode {

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
        return CalculateOperation.Subtract;
    }


    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        return arg1.subtract(arg2);
    }

}
