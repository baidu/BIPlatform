
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


/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月22日 
 * @since jdk 1.8 or after
 */
public class EmptyCondition extends AbstractCondition  {

    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -4956296041484112699L;
    
    private static EmptyCondition instance = new EmptyCondition();
    
    private EmptyCondition() {
    }
    
    public static EmptyCondition getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.expression.parser.context.Condition#getConditionType() 
     */
    @Override
    public ConditionType getConditionType() {
        return ConditionType.None;
    }

    @Override
    public int hashCode() {
        return 20;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "EmptyCondition";
    }

    @Override
    public <T> T processCondition(T source) {
        return source;
        
    }
    

}

