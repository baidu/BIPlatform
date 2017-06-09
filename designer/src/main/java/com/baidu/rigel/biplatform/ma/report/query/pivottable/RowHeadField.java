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
 * 行头信息。
 * 
 * @author mengran
 * 
 */
public class RowHeadField implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * dimName
     */
    private String dimName; // dim's name
    
    /**
     * uniqueNameAll
     */
    private String uniqueNameAll; // 完整行uniqueName
    
    /**
     * valueAll
     */
    private String valueAll; // 完整行caption
    
    /**
     * uniqueName
     */
    private String uniqueName; // 行当前单元格uniqueName
    
    /**
     * v
     */
    private String v; // 行当前单元格caption
    
    /**
     * drillByLink
     */
    private boolean drillByLink;
    
    /**
     * Start from <code>1</code>
     */
    private Integer indent;
    
    /**
     * <code>NULL</code> means leaf node. <code>FALSE</code> means expanded
     * already.
     */
    private Boolean expand;
    
    /**
     * rowspan
     */
    private Integer rowspan;
    
    /**
     * colspan
     */
    private Integer colspan = 1;
    
    /**
     * @return the indent
     */
    public Integer getIndent() {
        return indent;
    }
    
    /**
     * @param indent
     *            the indent to set
     */
    public void setIndent(Integer indent) {
        this.indent = indent;
    }
    
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
     * @return the expand
     */
    public Boolean getExpand() {
        return expand;
    }
    
    /**
     * @param expand
     *            the expand to set
     */
    public void setExpand(Boolean expand) {
        this.expand = expand;
    }
    
    public boolean isDrillByLink() {
        return drillByLink;
    }
    
    public void setDrillByLink(boolean drillByLink) {
        this.drillByLink = drillByLink;
    }
    
    public String getDimName() {
        return dimName;
    }
    
    public void setDimName(String dimName) {
        this.dimName = dimName;
    }
    
    /**
     * @return the rowSpan
     */
    public Integer getRowspan() {
        return rowspan;
    }
    
    /**
     * @param rowSpan
     *            the rowSpan to set
     */
    public void setRowspan(Integer rowSpan) {
        this.rowspan = rowSpan;
    }
    
    public String getUniqueNameAll() {
        return uniqueNameAll;
    }
    
    public void setUniqueNameAll(String uniqueNameAll) {
        this.uniqueNameAll = uniqueNameAll;
    }
    
    public String getUniqueName() {
        return uniqueName;
    }
    
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    public String getValueAll() {
        return valueAll;
    }
    
    public void setValueAll(String valueAll) {
        this.valueAll = valueAll;
    }
    
    @Override
    public String toString() {
        return "RowHeadField [uniqueNameAll=" + uniqueNameAll + ", valueAll=" + valueAll
            + ", uniqueName=" + uniqueName + ", v=" + v + ", indent=" + indent + ", expand="
            + expand + ", rowspan=" + rowspan + "]";
    }
    
    /**
     * default generate get colspan
     * 
     * @return the colspan
     */
    public Integer getColspan() {
        return colspan;
    }
    
    /**
     * default generate colspan param set method
     * 
     * @param colspan
     *            the colspan to set
     */
    public void setColspan(Integer colspan) {
        this.colspan = colspan;
    }
    
}
