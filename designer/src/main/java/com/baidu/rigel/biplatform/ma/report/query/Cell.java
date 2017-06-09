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
package com.baidu.rigel.biplatform.ma.report.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 单元格中的数据接口。
 * 
 * @author mengran
 * 
 */
public class Cell implements Serializable {
    
    /**
     * serialize id
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * cellId
     */
    private String cellId;
    
    /**
     * v
     */
    private BigDecimal v;
    
    /**
     * str
     */
    private String str;
    
    /**
     * 
     */
    private String formattedValue;
    
    /**
     * style
     */
    private Map<String, String> style = new HashMap<String, String>();
    
    /**
     * @return the cellId
     */
    public String getCellId() {
        return cellId;
    }
    
    /**
     * @param cellId
     *            the cellId to set
     */
    public void setCellId(String cellId) {
        this.cellId = cellId;
    }
    
    /**
     * @return the value
     */
    public BigDecimal getValue() {
        return v;
    }
    
    /**
     * @param value
     *            the value to set
     */
    public void setValue(BigDecimal value) {
        
        this.v = value;
    }
    
    /**
     * @return
     */
    public String getFormattedValue() {
        return formattedValue;
    }
    
    /**
     * @param formattedValue
     */
    public void setFormattedValue(String formattedValue) {
        this.formattedValue = formattedValue;
    }
    
    public Map<String, String> getStyle() {
        return style;
    }
    
    public void setStyle(Map<String, String> style) {
        this.style = style;
    }
    
    @Override
    public String toString() {
        return "CellData [cellId=" + cellId + ", value=" + v + ", formattedValue=" + formattedValue
            + "]";
    }
    
    public String getStr() {
        return str;
    }
    
    public void setStr(String str) {
        this.str = str;
    }
    
}
