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
package com.baidu.rigel.biplatform.tesseract.dataquery.udf;

import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.RateCondition.RateType;

/**
 * 
 * 同比计算用户自定义函数
 * @author david.wang
 *
 */
public class SimilitudeRate extends TesseractRateFunction {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8847247054235974455L;

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.impl.RateFunNode#getName()
     */
    @Override
    public String getName() {
        return "sRate";
    }

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.AbstractNode#collectVariableCondition()
     */
    @Override
    public Map<Condition, Set<String>> collectVariableCondition() {
        return this.collectVariableCondition(RateType.SR);
    }

    @Override
    RateType getType() {
        return RateType.SR;
    }

    
}
