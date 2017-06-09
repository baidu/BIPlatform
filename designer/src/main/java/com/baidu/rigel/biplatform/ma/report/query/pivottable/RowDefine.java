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
 * 行上的属性定义。
 * 
 * @author zhongyi
 *
 */
public class RowDefine implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 
     *
     * @version 1.0.0.1
     */
    public enum XType {
        
        /**
         * CATEGORY
         */
        CATEGORY("category"), 
        
        /**
         * YEAR
         */
        YEAR("year"), 
        
        /**
         * QUARTER
         */
        QUARTER("quarter"), 
        
        /**
         * MONTH
         */
        MONTH("month"),
        
        /**
         * DATE
         */
        DATE("datetime"), 
        
        /**
         * WEEK
         */
        WEEK("week");
        
        /**
         * name
         */
        private String name;
        
        /**
         * 
         * @param name
         */
        private XType(String name) {
            this.setName(name);
        }
        
        /**
         * 
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * 
         * @return String
         */
        public String getName() {
            return name;
        }
    }
    
    /**
     * uniqueName
     */
    private String uniqueName;
    
    /**
     * showXAxis
     */
    private String showXAxis;
    
    /**
     * xAxisType
     */
    private String xAxisType;
    
    /**
     * selected
     */
    private boolean selected;
    
    /**
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }
    
    /**
     * @param uniqueName
     *            the uniqueName to set
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    public String getShowXAxis() {
        return showXAxis;
    }
    
    public void setShowXAxis(String showXAxis) {
        this.showXAxis = showXAxis;
    }
    
    public String getxAxisType() {
        return xAxisType;
    }
    
    public void setxAxisType(String xAxisType) {
        this.xAxisType = xAxisType;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public String toString() {
        return "RowDefine [uniqueName=" + uniqueName + "]";
    }
    
}
