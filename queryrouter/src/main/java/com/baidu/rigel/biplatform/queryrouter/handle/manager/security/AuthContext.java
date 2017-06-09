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
package com.baidu.rigel.biplatform.queryrouter.handle.manager.security;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 用户身份认证上下文
 * 
 * @author wangyuxue
 * @version 1.0.0.1
 */
public class AuthContext {
    
    /**
     * repository
     */
    private Map<String, String> repository = null;
    
    /**
     * 
     * AuthContext
     */
    public AuthContext() {
        this.repository = Maps.newHashMap();
    }
    
    /**
     * @return the repository
     */
    public Map<String, String> getRepository() {
        return Collections.unmodifiableMap(this.repository);
    }
    
    /**
     * 添加属性信息
     * 
     * @param key
     *            String
     * @param value
     *            String
     */
    public void put(String key, String value) {
        this.repository.put(key, value);
    }
    
}
