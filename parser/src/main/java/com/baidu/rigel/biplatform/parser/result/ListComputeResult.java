
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;



/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月18日 
 * @since jdk 1.8 or after
 */
public class ListComputeResult extends AbstractResult<List<BigDecimal>> {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 2411927169550598780L;
    
    public ListComputeResult(List<BigDecimal> data) {
        setData(data);
    }
    
    public ListComputeResult() {
    }
    
    @Override
    public List<BigDecimal> getData() {
        if (super.getData () == null) {
            this.setData (new ArrayList<BigDecimal> ());
        }
        return super.getData ();
    }
    @Override
    public ResultType getResultType() {
        return ResultType.LIST;
    }
    
    
    /** 
     * transfer 将一个简单的结果转换成List结果，每个值都是简单结果的值
     * @param singleResult
     * @param size
     * @return
     */
    public static ListComputeResult transfer(ComputeResult trsan, int size) {
        if(trsan == null) {
            throw new IllegalArgumentException("single result is null");
        }
        ListComputeResult result = new ListComputeResult();
        if (trsan.getResultType().equals(ResultType.LIST)) {
            ListComputeResult ori = (ListComputeResult) trsan;
            
            if(CollectionUtils.isNotEmpty(ori.getData())) {
                int oriSize = ori.getData().size();
                if (oriSize == size) {
                    return ori;
                } else if (oriSize < size) {
                    result.getData().addAll(ori.getData());
                    for(int i = oriSize; i < size; i++) {
                        result.getData().add(null);
                    }
                } else {
                    for(int i = oriSize; i < size; i++) {
                        result.getData().add(ori.getData().get(i));
                    }
                }
            }
        } else {
            BigDecimal data = ((SingleComputeResult)trsan).getData();
            List<BigDecimal> datas = new ArrayList<BigDecimal>(size);
            for(int i = 0; i < size; i++) {
                datas.add(data);
            }
            result.setData(datas);
        }
        return result;
    }

}

