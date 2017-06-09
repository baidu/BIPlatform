/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition;

import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;

/**
 * 同环比计算条件定义
 * @author david.wang
 *
 */
public class RateCondition implements Condition {
 
    /**
     * isNumberator
     */
    public final boolean isNumerator;
    
    /**
     * rateType
     */
    public final RateType rateType;
    
    /**
     * variableName
     */
    public final String variableName;
    
    /**
     * 构造函数
     * @param isNumerator
     * @param rateType
     * @param variableName
     */
    public RateCondition(boolean isNumerator, RateType rateType, String variableName) {
        super();
        this.variableName = variableName;
        this.isNumerator = isNumerator;
        this.rateType = rateType;
    }

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3953122517325633375L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConditionType getConditionType() {
        return ConditionType.Other;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T processCondition(T source) {
        if (!(source instanceof QueryContext)) {
            throw new IllegalArgumentException("请求参数必须为QueryContext");
        }
        QueryContext context = (QueryContext) source;
        RateCalStrategy strategy = genRateCalStrategy();
        return (T) RateConditionProcessHandlerFactory.getInstance(strategy).processCondition(context);
    }
    
    /**
     * 
     * @param numerator
     * @param rateType
     * @return RateCalStrategy
     */
    private RateCalStrategy genRateCalStrategy() {
        if (this.rateType == null) {
            throw new IllegalStateException("计算类型为空");
        }
        switch (this.rateType) {
            case RR:
                return isNumerator ? RateCalStrategy.RR_NUMERATOR : RateCalStrategy.RR_DENOMINATOR;
            case SR:
                return isNumerator ? RateCalStrategy.SR_NUMERATOR : RateCalStrategy.SR_DENOMINATOR;
        }
        throw new IllegalStateException("未知计算请求");
    }
    
    


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isNumerator ? 1231 : 1237);
        result = prime * result
                + ((rateType == null) ? 0 : rateType.hashCode());
        result = prime * result
                + ((variableName == null) ? 0 : variableName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RateCondition other = (RateCondition) obj;
        if (isNumerator != other.isNumerator) {
            return false;
        }
        if (rateType != other.rateType) {
            return false;
        }
        if (variableName == null) {
            if (other.variableName != null) {
                return false;
            }
        } else if (!variableName.equals(other.variableName)) {
            return false;
        }
        return true;
    }




    /**
     * 
     * 同比/环比类型定义
     * @author david.wang
     *
     */
    public static enum RateType {
        /**
         * 同比
         */
        SR,
        
        /**
         * 环比
         */
        RR;
    }

}
