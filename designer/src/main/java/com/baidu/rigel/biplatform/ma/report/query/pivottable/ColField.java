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
package com.baidu.rigel.biplatform.ma.report.query.pivottable;

import java.io.Serializable;

/**
 * 列头信息。
 * 
 * @author mengran
 *
 */
public class ColField implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * v
     */
    private String v;
    
    /**
     * colspan
     */
    private Integer colspan;
    
    /**
     * rowspan
     */
    private Integer rowspan;
    
    /**
     * uniqName
     */
    private String uniqName;
    
    /**
     * @return the value
     */
    public String getV() {
        return v;
    }
    
    /**
     * @param value
     *            the value to set
     */
    public void setV(String value) {
        this.v = value;
    }
    
    /**
     * @return the colSpan
     */
    public Integer getColspan() {
        return colspan;
    }
    
    /**
     * @param colSpan
     *            the colSpan to set
     */
    public void setColSpan(Integer colSpan) {
        this.colspan = colSpan;
    }
    
    public Integer getRowspan() {
        return rowspan;
    }
    
    public void setRowspan(Integer rowspan) {
        this.rowspan = rowspan;
    }
    
    public String getUniqName() {
        return uniqName;
    }
    
    public void setUniqName(String uniqName) {
        this.uniqName = uniqName;
    }
    
    @Override
    public String toString() {
        return "ColField [value=" + v + ", colspan=" + colspan + ", rowspan=" + rowspan
            + ", uniqName=" + uniqName + "]";
    }
}
