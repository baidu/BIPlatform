
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
package com.baidu.rigel.biplatform.parser.result;

import java.math.BigDecimal;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月18日 
 * @since jdk 1.8 or after
 */
public class SingleComputeResult extends AbstractResult<BigDecimal> {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -7067450614505078590L;
    
    
    public SingleComputeResult(BigDecimal data) {
        setData(data);
    }
    
    public SingleComputeResult(int data) {
        setData(new BigDecimal(data));
    }
    
    public SingleComputeResult(String data) {
        setData(new BigDecimal(data));
    }
    
    public SingleComputeResult(double data) {
        setData(new BigDecimal(data));
    }
    
    public SingleComputeResult(long data) {
        setData(new BigDecimal(data));
    }
    
    public SingleComputeResult() {
        setData(BigDecimal.ZERO);
    }

    @Override
    public ResultType getResultType() {
        return ResultType.SINGLE;
    }
    
}


