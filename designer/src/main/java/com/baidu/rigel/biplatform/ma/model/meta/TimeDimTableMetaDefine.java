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

import java.io.Serializable;

/**
 * 
 * 时间维度表定义
 * @author david.wang
 * @version 1.0.0.1
 */
public class TimeDimTableMetaDefine extends DimTableMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 8300360552739758388L;
    
    /**
     * 维度类型
     */
    private TimeDimType type;
    
    /**
     * 时间格式
     */
    private String format;
    
    /**
     * 构造函数
     * 
     * @param type
     *            TimeDimType
     */
    public TimeDimTableMetaDefine(TimeDimType type) {
        if (type == null) {
            throw new IllegalArgumentException("维度类型不能为空");
        }
        this.type = type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public TimeDimType getDimType() {
        return type;
    }
    
    /**
     * get the format
     * 
     * @return the format
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * set the format
     * 
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }
    
}
