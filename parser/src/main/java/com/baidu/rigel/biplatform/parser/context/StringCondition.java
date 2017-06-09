
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
package com.baidu.rigel.biplatform.parser.context;

import java.util.HashMap;
import java.util.Map;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月22日 
 * @since jdk 1.8 or after
 */
@Deprecated
public class StringCondition extends AbstractCondition {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 6610327113435216266L;
    
    /** 
     * conditions
     */
    private Map<String, String> conditions;

    /** 
     * 构造函数
     */
    public StringCondition(Map<String, String> conditions) {
        super();
        this.conditions = conditions;
    }
    
    /** 
     * 构造函数
     */
    public StringCondition() {
        super();
        this.conditions = new HashMap<String, String>();
    }
    
    
    /** 
     * 获取 conditions 
     * @return the conditions 
     */
    public Map<String, String> getConditions() {
        if(this.conditions == null) {
            this.conditions = new HashMap<String, String>();
        }
        return conditions;
    }

    /** 
     * 设置 conditions 
     * @param conditions the conditions to set 
     */
    public void setConditions(Map<String, String> conditions) {
    
        this.conditions = conditions;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#hashCode() 
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        return result;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#equals(java.lang.Object) 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StringCondition other = (StringCondition) obj;
        if (conditions == null) {
            if (other.conditions != null)
                return false;
        } else if (!conditions.equals(other.conditions))
            return false;
        return true;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.String;
        
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "StringCondition [conditions=" + conditions + "]";
    }

    @Override
    public <T> T processCondition(T source) {
        return null;
    }

    

}

