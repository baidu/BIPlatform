
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.parser.exception.RegisterFunctionException;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月2日 
 * @since jdk 1.8 or after
 */
public class RegisterFunction {
    
    
    /** 
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegisterFunction.class);
    
    /** 
     * functionNames 注册的函数名称
     */
    private static Map<String, Constructor<? extends FunctionNode>> functionNames = 
        new HashMap<String, Constructor<? extends FunctionNode>>();
    
    
    /** 
     * register
     * @param functionName
     * @param funClass
     * @return
     * @throws ClassNotFoundException
     * @throws RegisterFunctionException
     */
    public static boolean register(String functionName, String funClass) 
        throws ClassNotFoundException, RegisterFunctionException {
        if(functionNames.containsKey(functionName)) {
            return false;
        }
        if(StringUtils.isBlank(funClass)) {
            throw new IllegalArgumentException("function class can not be empty by function name:" + functionName);
        }
        
        Class<?> clazz = Class.forName(funClass);
        
        return register(functionName, clazz);
    }
    
    
    
    /** 
     * register
     * @param functionName
     * @param funClass
     * @return
     * @throws RegisterFunctionException
     */
    public static boolean register(String functionName, Class<?> funClass) throws RegisterFunctionException {
        try {
            // get array constructor
            if(functionNames.containsKey(functionName)) {
                return false;
            }
            if (funClass == null) {
                throw new IllegalArgumentException("function class can not be empty by function name:" + functionName);
            }
            functionNames.put(functionName.toLowerCase(), 
                funClass.asSubclass(FunctionNode.class).getConstructor());
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.error("Register function:{} by class:{} catch error,error message:{}", 
                functionName, funClass, e.getMessage());
            throw new RegisterFunctionException(functionName, funClass.getName(), e.getMessage());
        }
        
        
        return true;
    }
    
    
    public static Constructor<? extends FunctionNode> getConstructorByFunctionName(String functionName) {
        if(StringUtils.isBlank(functionName)) {
            throw new IllegalArgumentException("function name can not be empty!");
        }
        return functionNames.get(functionName.toLowerCase());
    }

}

