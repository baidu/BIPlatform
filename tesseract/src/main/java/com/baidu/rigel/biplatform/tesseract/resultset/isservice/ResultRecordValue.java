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
package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Description:
 * @author david.wang
 *
 */
public class ResultRecordValue implements java.io.Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1879211638031492027L;
    
    /**
     * 整数正则表达式
     */
    private static final String NUM_REG = "^\\d{1}(\\.\\d{1})?$";
    
    /**
     * 正则表达式校验器
     */
    private static final Pattern PATTERN = Pattern.compile(NUM_REG);
    /**
     * 对于数字类型的val，需要用bigDecimal包装
     */
    private final BigDecimal realValue;
    
    /**
     * isNumberic
     */
    private final boolean isNumberic;
    
    /**
     * 结果集字面串 备用 在distinct count时使用
     */
    private final String strVal;
    
    /**
     * 构造函数
     * @param val
     */
    public ResultRecordValue(String val) {
        this.strVal = val;
        this.isNumberic = PATTERN.matcher(val).find();
        if (isNumberic) {
            this.realValue = new BigDecimal(val);
        } else {
            // TODO 字符串统计个数使用
            this.realValue = new BigDecimal(1);
        }
    }
    
    /**
     * 
     * @return String
     */
    public String getValueStr() {
        return this.realValue == null ? "1" : this.realValue.toString();
    }

    /**
     * 
     */
    public  ResultRecordValue add(ResultRecordValue augend) {
        return new ResultRecordValue(this.realValue.add(augend.realValue).toString());
    }



    /**
     * 
     * @param subtrahend
     * @return ResultRecordValue
     */
    public ResultRecordValue subtract(ResultRecordValue subtrahend) {
        return new ResultRecordValue(this.realValue.subtract(subtrahend.realValue).toString());
    }


    /**
     * 
     * @param multiplicand
     * @return ResultRecordValue
     */
    public ResultRecordValue multiply(ResultRecordValue multiplicand) {
        return new ResultRecordValue(this.realValue.subtract(multiplicand.realValue).toString());
    }

    /**
     * 
     * @param divisor
     * @param scale
     * @param roundingMode
     * @return ResultRecordValue
     */
    public ResultRecordValue divide(ResultRecordValue divisor, int scale, int roundingMode) {
        return new ResultRecordValue(this.realValue.subtract(divisor.realValue).toString());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((strVal == null) ? 0 : strVal.hashCode());
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
        if (!(obj instanceof ResultRecordValue)) {
            return false;
        }
        ResultRecordValue other = (ResultRecordValue) obj;
        if (strVal == null) {
            if (other.strVal != null) {
                return false;
            }
        } else if (!strVal.equals(other.strVal)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.strVal;
    }
    

}
