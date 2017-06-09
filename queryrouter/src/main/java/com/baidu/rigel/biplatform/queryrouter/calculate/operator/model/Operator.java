package com.baidu.rigel.biplatform.queryrouter.calculate.operator.model;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.model.Aggregator;

/**
 * 算子枚举
 * 
 * @author luowenlei
 *
 */
public class Operator implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2192117771688705184L;
    
    /**
     * aggregator
     */
    private Aggregator aggregator;
    
    /**
     * formula
     */
    private String formula;
    
    /**
     * default generate get aggregator
     * @return the aggregator
     */
    public Aggregator getAggregator() {
        return aggregator;
    }
    
    /**
     * default generate set aggregator
     * @param aggregator the aggregator to set
     */
    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }
    /**
     * default generate get formula
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }
    /**
     * default generate set formula
     * @param formula the formula to set
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }
}
