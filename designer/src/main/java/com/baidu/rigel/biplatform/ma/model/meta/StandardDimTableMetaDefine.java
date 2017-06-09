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
 * 标准维度定义元数据描述：描述标准维度的维度表定义信息。 标准维度指：除内置时间维度，callback维度、退化维度、自定义维度之外的维度定义
 * 
 * @author david.wang
 *
 */
public class StandardDimTableMetaDefine extends DimTableMetaDefine implements Serializable {
    
    /**
     * 序列化id
     */
    private static final long serialVersionUID = -6323631436887909360L;
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public StandardDimType getDimType() {
        return StandardDimType.STANDARD;
    }
    
}
