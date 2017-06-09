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
package com.baidu.rigel.biplatform.ma.model.utils;

import java.util.Random;
import java.util.UUID;

/**
 * 
 * UUID 生成工具类
 * 
 * @author david.wang
 *
 */
public final class UuidGeneratorUtils {
    
    /**
     * 构造函数
     */
    private UuidGeneratorUtils() {
    }
    
    /**
     * uuid 生成
     * 
     * @return uuid
     */
    public static String generate() {
        Random random = new Random();
        String uuid = new UUID(random.nextLong(), random.nextLong()).toString();
        // 去掉“-”符号
        return uuid.replace("-", "");
    }
}
