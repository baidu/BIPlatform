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
package com.baidu.rigel.biplatform.ac.util;

import org.springframework.util.Assert;
import org.springframework.util.SerializationUtils;

/**
 * 
 * 深度拷贝工具类
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public class DeepcopyUtils {

    /**
     * 构造函数
     */
    private DeepcopyUtils() {
    }

    /**
     * 深度拷贝
     * 
     * @param <T> 传递进行需要进行深度拷贝的类型进行自动转换
     * 
     * @param source 源
     * @return 拷贝的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T source) {
        Assert.notNull (source);
        byte[] content = SerializationUtils.serialize(source);
        return (T) SerializationUtils.deserialize(content);
    }
}
