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
package com.baidu.rigel.biplatform.ma.model.meta;

/**
 * 非时间维度类型定义：指定维度表中维度类型 该类型指与表关联，同一张维度表中维度类型一致
 * 
 * @author david.wang
 *
 */
public enum StandardDimType implements DimType {
    
    /**
     * 标准/普通维度
     */
    STANDARD,
    
    /**
     * 回调维度
     */
    CALLBACK,
    
    /**
     * 自定义维度
     */
    USERDEFINE,
    
    /**
     * 退化维度
     */
    REGRESS;
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.name();
    }
    
}
