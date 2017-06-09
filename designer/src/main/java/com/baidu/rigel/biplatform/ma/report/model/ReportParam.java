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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;

/**
 * Description:
 * @author david.wang
 *
 */
public class ReportParam implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4211394478887810632L;
    
    /**
     * elementId
     */
    private String elementId;
    
    /**
     * defaultValue
     */
    private String defaultValue;
    
    /**
     * needed
     */
    private boolean needed;
    
    /**
     * name
     */
    private String name;

    /**
     * @return the elementId
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * @param elementId the elementId to set
     */
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the needed
     */
    public boolean isNeeded() {
        return needed;
    }

    /**
     * @param needed the needed to set
     */
    public void setNeeded(boolean needed) {
        this.needed = needed;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "[name : " + name + " , elementId : " + elementId + " ]";
    }

}
