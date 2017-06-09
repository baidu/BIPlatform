/*
 * Copyright 2000-2011 baidu.com All right reserved. 
 */
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;
import java.util.List;

/**
 * 指标查询条件
 * 
 * @author yichao.jiang
 *
 */
public class SQLCondition implements Serializable {
    // // 等于
    // EQ("=") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // },
    // // 不等于
    // NOT_EQ("<>") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // },
    //
    // // 小于
    // LT("<") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // },
    //
    // // 大于
    // GT(">") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // },
    //
    // // between and
    // BETWEEN_AND("between and") {
    // public String parseToExpression () {
    // List<String> conditionValues = this.getConditionValues();
    // if (!check(conditionValues)) {
    // return null;
    // }
    // String leftValue = conditionValues.get(0);
    // String rightValue = conditionValues.get(1);
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append("between ");
    // stringBuilder.append(leftValue);
    // stringBuilder.append(" and ");
    // stringBuilder.append(rightValue);
    // return stringBuilder.toString();
    // }
    //
    // /**
    // * 重写条件数值检查方法
    // */
    // boolean check(List<String> conditionValues) {
    // if (conditionValues == null || conditionValues.size() == 0 || conditionValues.size() != 2) {
    // return false;
    // }
    // return true;
    // }
    // },
    //
    // // in
    // IN("in") {
    // public String parseToExpression () {
    // List<String> conditionValues = this.getConditionValues();
    // if (!check(conditionValues)) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append("in (");
    // for (String conditionValue : conditionValues) {
    // stringBuilder.append(conditionValue);
    // stringBuilder.append(",");
    // }
    // // 替换最后一个","
    // stringBuilder.replace(stringBuilder.lastIndexOf(","), stringBuilder.length(), "");
    // stringBuilder.append(")");
    // return stringBuilder.toString();
    // }
    //
    // /**
    // * 重写条件数值检查方法
    // */
    // boolean check(List<String> conditionValues) {
    // if (conditionValues == null || conditionValues.size() == 0) {
    // return false;
    // }
    // return true;
    // }
    // },
    //
    // // 小于等于
    // LT_EQ("<=") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // },
    //
    // // 大于等于
    // GT_EQ(">=") {
    // public String parseToExpression () {
    // if (!check(this.getConditionValues())) {
    // return null;
    // }
    // StringBuilder stringBuilder = new StringBuilder();
    // stringBuilder.append(this.getMetaName());
    // stringBuilder.append(this.getValue());
    // stringBuilder.append(this.getConditionValues().get(0));
    // return stringBuilder.toString();
    // }
    // };

    /**
     * 序列号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 条件对应的数值
     */
    private List<String> conditionValues;

    /**
     * 指标对应的名称
     */
    private String metaName;

    /**
     * 对应的sql条件，=,<=,等
     */
    private SQLConditionType condition;

    // /**
    // * 构造函数
    // * @param value
    // */
    // private SQLCondition(String value) {
    // this.setValue(value);
    // }

    /**
     * 设置values值
     * 
     * @param values
     */
    public void setConditionValues(List<String> conditionValues) {
        this.conditionValues = conditionValues;
    }

    /**
     * 获取values值
     * 
     * @return
     */
    public List<String> getConditionValues() {
        return this.conditionValues;
    }

    /**
     * @return the metaName
     */
    public String getMetaName() {
        return metaName;
    }

    /**
     * @param metaName the metaName to set
     */
    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }

    // /**
    // * 将SQL条件转为具体的表达式
    // * @return 表达式
    // */
    // public abstract String parseToExpression();

    /**
     * default generate get condition
     * 
     * @return the condition
     */
    public SQLConditionType getCondition() {
        return condition;
    }

    /**
     * default generate set condition
     * 
     * @param condition the condition to set
     */
    public void setCondition(SQLConditionType condition) {
        this.condition = condition;
    }

    // @Override
    // public String toString() {
    // return "{condition : " + this.name()
    // + ", value : ["
    // + StringUtils.arrayToDelimitedString(this.conditionValues.toArray(), ",")
    // + "]}";
    // }

    /**
     * SQL条件类型枚举类
     * 
     * @author yichao.jiang 2015年5月28日 下午5:51:55
     */
    public enum SQLConditionType {
        /**
         *  等于
         */
        EQ,
        
        /**
         * 不等于
         */
        NOT_EQ,
        /**
         * 小于
         */
        LT,
        /**
         * 小于等于
         */
        LT_EQ,
        /**
         * 大于
         */
        GT,
        /**
         * 大于等于
         */
        GT_EQ,
        /**
         * between and
         */
        BETWEEN_AND,
        /**
         * in
         */
        IN,
        /**
         * like
         */
        LIKE

    }
}
