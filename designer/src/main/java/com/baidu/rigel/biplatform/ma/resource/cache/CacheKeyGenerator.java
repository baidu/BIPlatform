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
package com.baidu.rigel.biplatform.ma.resource.cache;

import org.springframework.util.StringUtils;

/**
 * 缓存key的生成方式
 * @author zhongyi
 *
 *         2014-8-6
 */
public final class CacheKeyGenerator {
    
    /**
     * DIV
     */
    private static final String DIV = "_";
    
    /**
     * REPORT_PREFIX
     */
    private static final String REPORT_PREFIX = "report_";
    
    /**
     * RUNTIME_PREFIX
     */
    private static final String RUNTIME_PREFIX = "runtime_";
    
    /**
     * 构造函数
     */
    private CacheKeyGenerator() {
        
    }
    
    /**
     * 
     * @param args
     * @return
     */
    public static String generateJointedKey(String... args) {
        String keyBase = StringUtils.arrayToDelimitedString(args, DIV);
        return keyBase;
    }
    
    /**
     * 
     * @param product
     * @param report
     * @return
     */
    public static String generateSessionReportKey(String sessionId, String reportId,
        String productLine) {
        return REPORT_PREFIX + productLine + DIV + reportId + DIV + sessionId;
    }
    
    /**
     * 
     * @param product
     * @param report
     * @return
     */
    public static String generateRuntimeReportKey(String sessionId, String reportId,
        String productLine) {
        return RUNTIME_PREFIX + productLine + DIV + reportId + DIV + sessionId;
    }
    
}