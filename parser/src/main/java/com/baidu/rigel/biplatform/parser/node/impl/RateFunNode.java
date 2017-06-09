
package com.baidu.rigel.biplatform.parser.node.impl;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.exception.NodeCompileException;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;
import com.baidu.rigel.biplatform.parser.util.ParserConstant;

public class RateFunNode extends FunctionNode {

    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 6966191881881585349L;
    
    /** 
     * 构造函数
     */
    public RateFunNode(Node numeratorNode, Node denominatorNode) {
        super(numeratorNode, denominatorNode);
        
    }
    
    public RateFunNode() {
    }

    @Override
    public String getName() {
        return "Rate";
    }

    /*
     * 默认实现的函数的结果集处理方案
     * 
     */
    @Override
    public ComputeResult getResult(CompileContext context) throws IllegalCompileContextException {
        //预处理下函数的参数，比如从context中根据函数的条件获取变量的值
        preSetNodeResult(context);
        processNodes(getArgs(), context);
        if(this.result == null) {
            this.result = new SingleComputeResult();
        }
        return this.result;
    }
    
    protected void preSetNodeResult(CompileContext context) {
    }

    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        if(BigDecimal.ZERO.equals(arg2)) {
            return null;
        }
        try {
            return arg1.divide(arg2, ParserConstant.COMPUTE_SCALE, BigDecimal.ROUND_HALF_UP).subtract(BigDecimal.ONE);
        } catch (ArithmeticException e) {
            return null;
        }
    }


    @Override
    public Map<Condition, Set<String>> mergeCondition(Node node) {
        return node.collectVariableCondition();
        
    }

    @Override
    public void check() {
        super.check();
        Node node = getArgs().get(1);
        if(node.getNodeType().equals(NodeType.Numeric)) {
            SingleComputeResult result = (SingleComputeResult) node.getResult(null);
            if (BigDecimal.ZERO.equals(result.getData())) {
                throw new NodeCompileException(this, "rate function denominator can not be zero.");
            }
        }
    }

    @Override
    public int getArgsLength() {
        return 2;
    }

}

