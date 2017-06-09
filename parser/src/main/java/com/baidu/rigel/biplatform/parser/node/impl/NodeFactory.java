
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
package com.baidu.rigel.biplatform.parser.node.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.parser.RegisterFunction;
import com.baidu.rigel.biplatform.parser.exception.IllegalFunctionException;
import com.baidu.rigel.biplatform.parser.node.AbstractNode;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月10日 
 * @since jdk 1.8 or after
 */
public class NodeFactory {
    
    public static FunctionNode makeFunctionNodeByFunctionName(String name) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        if(StringUtils.isNotBlank(name)) {
            Constructor<? extends FunctionNode> construct = RegisterFunction.getConstructorByFunctionName(name);
            if(construct != null) {
                
                return construct.newInstance();
            } else {
                throw new IllegalFunctionException("function name:" + name + " has not implement.");
            }
        } else {
            throw new IllegalArgumentException("function name can not be null!");
        }
        
    }
    
    
    public static AbstractNode makeNodeByOperation(char operation) {
        switch (operation) {
            case '+' :
                return new AddCalculateNode(); 
            case '-' :
                return new SubtractCalculateNode();
            case '*' :
                return new MultiplyCalculateNode();
            case '/' :
                return new DivideCalculateNode();
            default :
                throw new IllegalArgumentException("illegal operation:" + operation);
        }
    }

}

