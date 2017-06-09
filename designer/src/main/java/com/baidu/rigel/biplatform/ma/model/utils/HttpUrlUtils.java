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
/**
 * 
 */
package com.baidu.rigel.biplatform.ma.model.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.model.exception.HttpUrlException;
import com.baidu.rigel.biplatform.ma.model.service.impl.StarModelBuildServiceImpl;
import com.google.common.collect.Maps;

/**
 * HttpUrl处理工具
 * 
 * @author zhongyi
 *
 */
public final class HttpUrlUtils {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(StarModelBuildServiceImpl.class);
    
    private HttpUrlUtils() {
        
    }
    
    /**
     * 获得基本URL
     * @param totalUrl 完整URL
     * @return 基本URL
     */
    public static final String getBaseUrl(String totalUrl) {
        String[] urlElements = totalUrl.split("\\?");
        if (urlElements.length > 2) {
            LOG.error("url is wrong for multi charactor of '?'.");
            throw new HttpUrlException("url is wrong for multi charactor of '?'.");
        }
        String baseUrl = urlElements[0];
        return baseUrl;
    }
    
    /**
     * 获得参数集合
     * @param totalUrl 完整URL
     * @return 参数集合
     */
    public static final Map<String, String> getParams(String totalUrl) {
        String[] urlElements = totalUrl.split("\\?");
        if (urlElements.length > 2) {
            LOG.error("url is wrong for multi charactor of '?'.");
            throw new HttpUrlException("url is wrong for multi charactor of '?'.");
        }
        Map<String, String> params = Maps.newHashMap();
        if (urlElements.length == 1) {
            return params;
        }
        
        String[] paramStrs = urlElements[1].split("&");
        for (String param : paramStrs) {
            String[] paramParts = param.split("=");
            if (paramParts.length != 2) {
                throw new HttpUrlException("url is wrong for param.");
            }
            String key = paramParts[0];
            String value = paramParts[1];
            params.put(key, value);
        }
        return params;
    }
    
    public static final String generateTotalUrl(String baseUrl, Map<String, String> params) {
        StringBuilder totalUrl = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (!StringUtils.hasText(value)) {
                /**
                 * 跳过没有值的参数
                 */
                LOG.debug("ignore empty value param.");
                continue;
            }
            totalUrl.append(key);
            totalUrl.append("=");
            totalUrl.append(value);
            totalUrl.append("&");
        }
        int length = totalUrl.length();
        String paramPart = null;
        if (length > 0 && totalUrl.charAt(length - 1) == '&') {
            paramPart = totalUrl.substring(0, length - 1);
        } else {
            paramPart = totalUrl.toString();
        }
        if (!baseUrl.contains("?")) {
            baseUrl = baseUrl + "?";
        }
        return baseUrl + paramPart;
    }
}
