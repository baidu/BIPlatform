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

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * gson builder utils
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public final class GsonUtils {
    
    /**
     * gson
     */
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    
    /**
     * 构造函数
     */
    private GsonUtils() {
        
    }
    
    /**
     * 将json转换炜java对象
     * 
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return GSON.fromJson(json, clazz);
    }
    
    /**
     * 
     * @param json
     * @param type
     * @return T
     */
    public static <T> T fromJson(String json, Type type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return GSON.fromJson(json, type);
    }
    
    /**
     * 
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }
}
