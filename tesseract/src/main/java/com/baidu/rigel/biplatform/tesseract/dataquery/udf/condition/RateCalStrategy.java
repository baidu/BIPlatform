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

/**
 * Description: 同环比计算方式策略，目前共分为四种：同比分子计算，同比分母计算，环比分子计算，环比分母计算
 * @author davie.wang
 *
 */
public enum RateCalStrategy {
    
    /**
     * 同比分子
     */
    SR_NUMERATOR,
    
    /**
     * 环比分子
     */
    RR_NUMERATOR,
    
    /**
     * 同比分母
     */
    SR_DENOMINATOR,
    
    /**
     * 环比分母
     */
    RR_DENOMINATOR;
}
