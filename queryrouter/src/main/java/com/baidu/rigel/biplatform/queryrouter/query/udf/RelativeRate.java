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
package com.baidu.rigel.biplatform.queryrouter.query.udf;

import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.queryrouter.query.udf.condition.RateCondition.RateType;

/**
 * 
 * 环比计算用户自定义函数实现
 * @author david.wang
 *
 */
public class RelativeRate extends TesseractRateFunction {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1995442354532812165L;

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.impl.RateFunNode#getName()
     */
    @Override
    public String getName() {
        return "rRate";
    }

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.parser.node.AbstractNode#collectVariableCondition()
     */
    @Override
    public Map<Condition, Set<String>> collectVariableCondition() {
        return this.collectVariableCondition(RateType.RR);
    }

    @Override
    RateType getType() {
        return RateType.RR;
    }

}
