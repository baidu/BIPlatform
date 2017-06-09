package com.baidu.rigel.biplatform.queryrouter.query.vo.sql;

/**
 * SqlSelectColumnType
 * 
 * @author luowenlei
 *
 */
public enum SqlSelectColumnType {
    // 非agg计算的普通select，
    COMMON("COMMON"),
    // 非DISTINCT_COUNT agg计算的agg select，
    OPERATOR_COMMON("OPERATOR_COMMON"),
    // DISTINCT_COUNT agg select，
    OPERATOR_DISTINCT_COUNT("DISTINCT_COUNT");
    
    private String name;
    
    private SqlSelectColumnType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
