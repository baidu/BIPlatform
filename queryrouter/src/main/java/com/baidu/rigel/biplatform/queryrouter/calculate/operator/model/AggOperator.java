package com.baidu.rigel.biplatform.queryrouter.calculate.operator.model;

import com.baidu.rigel.biplatform.ac.model.Aggregator;

/**
 * 算子枚举
 * 
 * @author luowenlei
 *
 */
public enum AggOperator {
    /**
     * SUM
     */
    SUM("SUM", Aggregator.SUM),
    /**
     * COUNT
     */
    COUNT("COUNT", Aggregator.COUNT),
    /**
     * CALCULATED
     */
    CALCULATED("CALCULATED", Aggregator.CALCULATED),
    
    /**
     * DISTINCT_COUNT
     */
    DISTINCT_COUNT("DISTINCT_COUNT", Aggregator.DISTINCT_COUNT),
    /**
     * NONE
     */
    NONE("NONE", Aggregator.NONE);

    private String name;
    
    private Aggregator aggregator;

    /**
     * getName
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * getAggregator
     * 
     * @return the aggregator
     */
    public Aggregator getAggregator() {
        return aggregator;
    }

    private AggOperator(String name, Aggregator aggregator) {
        this.name = name;
        this.aggregator = aggregator;
    }
}
