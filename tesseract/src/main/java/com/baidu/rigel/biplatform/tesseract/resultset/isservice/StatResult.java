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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * StatResult
 * 
 * @author lijin
 *
 */
public class StatResult implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8977016093923720210L;
    
    /**
     * statFieldValueList
     */
    private List<BigDecimal> statFieldValueList;
    
    /**
     * 
     * Constructor by no param
     */
    public StatResult() {
        super();
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param statList
     *            statList
     */
    public StatResult(List<BigDecimal> statList) {
        this.statFieldValueList = statList;
    }
    
    /**
     * 
     * add
     * 
     * @param statResult
     *            statResult
     * @return StatResult
     */
    public StatResult add(StatResult statResult) {
        return new StatResult();
    }
    
    /**
     * getter method for property statFieldValueList
     * 
     * @return the statFieldValueList
     */
    public List<BigDecimal> getStatFieldValueList() {
        return statFieldValueList;
    }
    
    /**
     * setter method for property statFieldValueList
     * 
     * @param statFieldValueList
     *            the statFieldValueList to set
     */
    public void setStatFieldValueList(List<BigDecimal> statFieldValueList) {
        this.statFieldValueList = statFieldValueList;
    }
    
}
