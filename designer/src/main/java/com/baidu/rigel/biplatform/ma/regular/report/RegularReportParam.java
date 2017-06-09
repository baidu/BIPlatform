

/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.regular.report;

import java.io.Serializable;
import java.util.Map;

/** 
 * 固定报表参数 
 * @author yichao.jiang 
 * @version  2015年8月10日 
 * @since jdk 1.8 or after
 */
public class RegularReportParam implements Serializable {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -5092462224051268856L;

    /**
     * 参数id
     */
    private String paramId;
    
    /**
     * 参数名称
     */
    private String paramName;
    
    /**
     * 参数显示名称
     */
    private String caption;
    
    /**
     * 参数值
     */
    private Map<String, Object> paramValue;
    
    /** 
     * 获取 paramId 
     * @return the paramId 
     */
    public String getParamId() {
        return paramId;
    }

    /** 
     * 设置 paramId 
     * @param paramId the paramId to set 
     */
    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    /** 
     * 获取 paramName 
     * @return the paramName 
     */
    public String getParamName() {
        return paramName;
    }

    /** 
     * 设置 paramName 
     * @param paramName the paramName to set 
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /** 
     * 获取 caption 
     * @return the caption 
     */
    public String getCaption() {
        return caption;
    }

    /** 
     * 设置 caption 
     * @param caption the caption to set 
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /** 
     * 获取 paramValue 
     * @return the paramValue 
     */
    public Map<String, Object> getParamValue() {
        return paramValue;
    }

    /** 
     * 设置 paramValue 
     * @param paramValue the paramValue to set 
     */
    public void setParamValue(Map<String, Object> paramValue) {
        this.paramValue = paramValue;
    }
    
    /**
     * 
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RegularReportParam) {
            RegularReportParam param = (RegularReportParam) obj;
            return this.paramId.equals(param.paramId);
        }
        return false;
    }
    
    /**
     * 
     */
    @Override
    public int hashCode() {
        return this.paramId.hashCode();
    }
}

