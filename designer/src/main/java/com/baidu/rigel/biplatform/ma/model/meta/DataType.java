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
 * 字段类型定义：用于描述业务数据元数据中表的字段数据类型
 * 
 * @author david.wang
 *
 */
public enum DataType {
    
    /**
     * 整数类型
     */
    INTEGER("integer"),
    
    /**
     * 字符类型
     */
    STRING("string"),
    
    /**
     * 浮点类型
     */
    DOUBLE("double");
    
    /**
     * 字段类型描述信息
     */
    private String value;
    
    /**
     * 构造函数
     * 
     * @param value
     */
    private DataType(String value) {
        this.value = value;
    }
    
    /**
     * 获取表元数据字段类型描述信息
     * 
     * @return 数据字段类型描述
     */
    public String value() {
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name();
    }
}
