
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
package com.baidu.rigel.biplatform.parser.util;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/** 
 * 相关的一些常量定义和载入信息
 * @author xiaoming.chen
 * @version  2014年12月3日 
 * @since jdk 1.8 or after
 */
public class ParserConstant {
    /** 
     * LOG
     */
    private static Logger LOG = LoggerFactory.getLogger(ParserConstant.class);
    
    
    public static final String FUNCTION_PATTERN_KEY = "function.pattern";

    public static final String PARENTHESES_PATTERN_KEY = "parentheses.pattern";
    
    public static final String VARIABLE_PATTERN_KEY = "variable.pattern";
    
    public static final String DEFAULT_RESOURCE_LOCATION = "conf/default.properties";
    
    /** 
     * DEFAULT_PARENTHES_PATTERN_STR 匹配小括号的正则字符串
     */
    public static final String DEFAULT_PARENTHES_PATTERN_STR = "\\([^\\(\\)]+\\)";
    
    
    /** 
     * DEFAULT_FUNCTION_PATTERN_STR 匹配函数正则字符串
     */
    public static final String DEFAULT_FUNCTION_PATTERN_STR = "[a-z0-9]\\w*\\s*\\([^\\(\\)]*\\)";
    
    
    /** 
     * DEFAULT_VARIABLE_PATTERN_STR 默认变量正则表达式
     */
    public static final String DEFAULT_VARIABLE_PATTERN_STR = "\\$\\{\\w+\\}";
    
    
    /** 
     * PARENTHES_PATTERN
     */
    public static Pattern PARENTHES_PATTERN;
    
    
    /** 
     * FUNCTION_PATTERN
     */
    public static Pattern FUNCTION_PATTERN;
    
    
    /** 
     * MIX_PATTERN
     */
    public static Pattern MIX_PATTERN;
    
    /** 
     * VARIABLE_PATTERN
     */
    public static String VARIABLE_PATTERN_STR;
    
    /** 
     * NUMBER_PATTERN_STR 整数，小数正则
     */
    public static final String NUMBER_PATTERN_STR = "\\d+(\\.\\d+)?";
    
    /** 
     * ARITHMETIC_PATTERN 运算符正则
     */
    public static final Pattern ARITHMETIC_PATTERN = Pattern.compile("[\\+\\-\\*/]");
    
    
    /** 
     * COMPUTE_SCALE 精度
     */
    public static final int COMPUTE_SCALE = 8;
    
    
    
    
    static {
        try {
            Properties properties = PropertiesUtil.loadPropertiesFromPath(DEFAULT_RESOURCE_LOCATION);
            String parenthesPatternStr = properties.getProperty(PARENTHESES_PATTERN_KEY, DEFAULT_PARENTHES_PATTERN_STR);
            String functionPatternStr = properties.getProperty(FUNCTION_PATTERN_KEY, DEFAULT_PARENTHES_PATTERN_STR);
            PARENTHES_PATTERN = Pattern.compile(parenthesPatternStr, Pattern.CASE_INSENSITIVE);
            FUNCTION_PATTERN = Pattern.compile(functionPatternStr, Pattern.CASE_INSENSITIVE);
            MIX_PATTERN = Pattern.compile(functionPatternStr + "|" + parenthesPatternStr, Pattern.CASE_INSENSITIVE);
            
            VARIABLE_PATTERN_STR = properties.getProperty(VARIABLE_PATTERN_KEY, DEFAULT_VARIABLE_PATTERN_STR);
        } catch (IOException e) {
            LOG.error("load resource from default error,use default", e);            
        }
    }
}

