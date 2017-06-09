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
package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;


/**
 * NameCheckUtils
 * @author david.wang
 *
 */
public final class NameCheckUtils {
    
    /**
     * 默认名称匹配规则
     */
    private static final String DEFAULT_INVALIDATE_RULE =
    		"[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    
    /**
     * 构造函数
     */
    private NameCheckUtils() {
        
    }
    
    /**
     * 
     * @param name
     * @return boolean
     */
    public static boolean isInvalidName(String name) {
        return checkNameWithIllegalRule(name, DEFAULT_INVALIDATE_RULE);
    }
    
    /**
     * 
     * @param name
     * @param ruleRegEx
     * @return boolean
     */
    public static boolean checkNameWithIllegalRule(String name, String ruleRegEx) {
        if (!StringUtils.hasText (name)) {
            return true;
        }
        if (name.length () > 250) {
            return true;
        }
        if (StringUtils.isEmpty (ruleRegEx)) {
            ruleRegEx = DEFAULT_INVALIDATE_RULE;
        }
        Pattern p = Pattern.compile (ruleRegEx);
        Matcher m = p.matcher (name);
        if (m.find ()) {
            return true;
        }
        return false;
    }
}
