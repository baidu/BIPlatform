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
package com.baidu.rigel.biplatform.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.exception.InvokeFunctionException;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.util.ParserConstant;

/**
 * 
 * @author xiaoming.chen
 * @version 2014年12月1日
 * @since jdk 1.8 or after
 */
public class CompileExpression {
    
    /**  
     * COMPILE_RESULT_KEY
     */
    public static final String COMPILE_RESULT_KEY = "result";
    /** 
     * log
     */
    private static Logger LOG = LoggerFactory.getLogger(CompileExpression.class);
    /** 
     * VAR_PRE 变量前缀
     */
    public static final String SECTION_PRE = "$section";
    

    /** 
     * complieExpressionByPattern
     * @param expression
     * @param pattern
     * @param variablePre
     */
    static Map<String, String> compileExpressionByPattern(String expression, Pattern pattern) {
        Map<String, String> result = new LinkedHashMap<String, String>(1);
        
        Matcher matcher = pattern.matcher(expression);
        int i = 1;
        while(true){
            boolean found = false;
            while(matcher.find()){
                String matchExp = matcher.group();
                
                result.put(SECTION_PRE+i, matchExp);
                
                expression = expression.replace(matchExp, SECTION_PRE + i);
                i++;
                found = true;
            }
            matcher = pattern.matcher(expression);
            if(!found){
                result.put(COMPILE_RESULT_KEY, expression);
                break;
            }
        }
        LOG.info("compile expression :{} into list tokens:{}", expression, result);
        return result;
    }
    
    static CompileContext resolveSections(Map<String, String> sections) throws InvokeFunctionException{
        
        CompileSection complieSection = new CompileSection(sections);
        
        Map<String,Node> resultNodes = complieSection.complie();
        LOG.info("compile sections into node map {}",resultNodes);
        Node resultNode = resultNodes.get(COMPILE_RESULT_KEY);
        resultNode.check();
        return new CompileContext(resultNode);
    }
    
    
    public static CompileContext compile(String expression) {
        CompileContext context = resolveSections(compileExpressionByPattern(expression, ParserConstant.MIX_PATTERN));
        context.setExpression(expression);
        return context;
    }
    
    
    
}
