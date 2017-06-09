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
 * 
 * 用户自定义维度 维度来源。 维度来源可以是SQL, 问本文件、Excel、或者固定值 目前指支持SQL
 * 
 * @author david.wang
 *
 */
public enum DimSourceType {
    /**
     * 通过sql语句构建维度
     */
    SQL,
    
    /**
     * 通过文件构建维度
     */
    FILE,
    
    /**
     * 通过excel构建维度
     */
    EXCEL,
    
    /**
     * 其他来源，比如固定格式字符串等
     */
    OTHERS;
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.name();
    }
}
