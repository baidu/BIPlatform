
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
package com.baidu.rigel.biplatform.queryrouter.query.udf.condition;

import com.baidu.rigel.biplatform.parser.context.AbstractCondition;

/** 
 *  只是为了说明这一类的指标都是Callback类型，具体的实现和
 * @author xiaoming.chen
 * @version  2015年1月19日 
 * @since jdk 1.8 or after
 */
public class CallbackCondition extends AbstractCondition {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -8344995432286276590L;
    
    
    /** 
     * instance 单例
     */
    private static CallbackCondition instance = new CallbackCondition();
    
    
    /** 
     * 构造函数
     */
    private CallbackCondition() {
    }
    
    
    /** 
     * 获取Callback条件的实例
     * getInstance
     * @return
     */
    public static CallbackCondition getInstance() {
        return instance;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.Other;

    }

    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.parser.context.Condition#processCondition(java.lang.Object) 
     */
    @Override
    public <T> T processCondition(T source) {
        return source;

    }

}

