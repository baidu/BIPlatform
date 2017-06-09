package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

/**
 * SqlSelectColumnType
 * 
 * @author luowenlei
 *
 */
public enum SqlSelectColumnType {

    COMMON("COMMON"),// 非agg计算的普通select，
    OPERATOR_COMMON("OPERATOR_COMMON"), // 非DISTINCT_COUNT agg计算的agg select，
    OPERATOR_DISTINCT_COUNT("DISTINCT_COUNT"); // DISTINCT_COUNT agg select，

    private String name;

    private SqlSelectColumnType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
