
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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.parser.exception.IllegalTokenException;
import com.baidu.rigel.biplatform.parser.exception.InvokeFunctionException;
import com.baidu.rigel.biplatform.parser.exception.NotAllowedOperationException;
import com.baidu.rigel.biplatform.parser.node.CalculateNode;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.node.Node.NodeType;
import com.baidu.rigel.biplatform.parser.node.impl.AddCalculateNode;
import com.baidu.rigel.biplatform.parser.node.impl.DataNode;
import com.baidu.rigel.biplatform.parser.node.impl.DivideCalculateNode;
import com.baidu.rigel.biplatform.parser.node.impl.MultiplyCalculateNode;
import com.baidu.rigel.biplatform.parser.node.impl.NodeFactory;
import com.baidu.rigel.biplatform.parser.node.impl.SubtractCalculateNode;
import com.baidu.rigel.biplatform.parser.node.impl.VariableNode;
import com.baidu.rigel.biplatform.parser.util.ParserConstant;

/** 
 * 
 * @author xiaoming.chen
 * @version  2014年12月18日 
 * @since jdk 1.8 or after
 */
class CompileSection {
    
    
    /** 
     * sections
     */
    private Map<String, String> sections;
    
    
    private Map<String, Node> resolveNodes;
    
    
    
    protected CompileSection(Map<String, String> sections) {
        this.sections = sections;
        resolveNodes = new HashMap<String, Node>(sections.size());
    }
    
    protected Map<String, Node> complie() throws InvokeFunctionException{
        compileSections();
        return this.resolveNodes;
    }
    
    
    protected void compileSections() throws InvokeFunctionException {
        for(Map.Entry<String, String> entry : sections.entrySet()) {
            compileSection(entry.getKey(), entry.getValue());
        }
    }
    
    protected void compileSection(String key, String section) throws InvokeFunctionException {
        
        int i = 0;
        char c = ' ';
        
        StringBuilder currentToken = new StringBuilder();
        boolean isFunction = false;
        
        FunctionNode function = null; 
        
        Stack<Node> resolveSectionNodes = new Stack<Node>();
        while(i < section.length()) {
            c = section.charAt(i);
            switch (c) {
                case ' ':
                    // skip blank character
                    break;
                    
                case '(' :
                    // 判断当前token中是否有东西，如果有的话，那么说明这个section就是函数
                    if(currentToken.length() > 0) {
                        isFunction = true;
                        String funName = currentToken.toString();
                        try {
                            function = NodeFactory.makeFunctionNodeByFunctionName(funName);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException e) {
                            throw new InvokeFunctionException(funName, e.getMessage());
                        }
                    }
                    currentToken.setLength(0);
                    break;
                case ',' :
                    // 解析完成一个参数，将这个参数封装成完整的函数的一个参数
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),null);
                    function.getArgs().add(arrangeNodes(resolveSectionNodes));
                    currentToken.setLength(0);
                    break;
                case ')' :
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),null);
                    // 结束了。。不需要管其它的
                    if(isFunction) {
                        if(!resolveSectionNodes.isEmpty()) {
                            function.getArgs().add(arrangeNodes(resolveSectionNodes));
                        }
                        resolveNodes.put(key, function);
                    }else {
                        resolveNodes.put(key, arrangeNodes(resolveSectionNodes));
                    }
                    currentToken.setLength(0);
                    break;
                case '*' : //42
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),new MultiplyCalculateNode());
                    currentToken.setLength(0);
                    break;
                case '/' : //47
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),new DivideCalculateNode());
                    currentToken.setLength(0);
                    break;
                case '+' : //43
                    if(currentToken.length () == 0) {
                        currentToken.append ("0");
                    }
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),new AddCalculateNode());
                    currentToken.setLength(0);
                    break;
                case '-' : //45
                    if(currentToken.length () == 0) {
                        currentToken.append ("0");
                    }
                    sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),new SubtractCalculateNode());
                    currentToken.setLength(0);
                    break;
                default:
                    currentToken.append(c);
                    break;
            }
            
            i++;
        }
        // 可能这个表达式没有括号
        if(currentToken.length() > 0) {
            sectionProcess(resolveSectionNodes,resolveToken(currentToken.toString()),null);
            resolveNodes.put(key, arrangeNodes(resolveSectionNodes));
        }
    }
    
    
    /**
     * 整理从section中处理出来的节点
     * arrangeNodes
     * @param nodes
     * @return
     */
    private Node arrangeNodes(Stack<Node> nodes) {
        if(!nodes.isEmpty()) {
            Node node = null;
            Node leafNode = null;
            
            CalculateNode result = null;
            while (!nodes.isEmpty()) {
                node = nodes.pop();
                if(node.getNodeType().equals(NodeType.Calculate)) {
                    result = (CalculateNode) node;
                    if(leafNode == null) {
                        leafNode = node;
                    } else {
                        result.setRight(leafNode);
                        leafNode = result;
                    }
                } else {
                    return node;
                }
            }
            return result;
                
        }
        return null;
    }
    
    
    private void sectionProcess(Stack<Node> nodes, Node tokenNode, CalculateNode calcNode) {
        if(nodes.isEmpty()) {
            if(calcNode == null) {
                nodes.push(tokenNode);
            }else {
                calcNode.setLeft(tokenNode);
                nodes.push(calcNode);
            }
        } else {
            // 从暂存节点列表中取出的肯定是计算节点
            if(!nodes.peek().getNodeType().equals(NodeType.Calculate)) {
                // 不可能走到这。。
                throw new NotAllowedOperationException("not allowed");
            }
            CalculateNode node = (CalculateNode) nodes.peek();
            if (calcNode == null) {
                node.setRight(tokenNode);
                return;
            }
            if(node.getOperation().getPriority() >= calcNode.getOperation().getPriority()) {
                node.setRight(tokenNode);
                nodes.pop();
                nodeProcess(nodes, node, calcNode);
            } else {
                calcNode.setLeft(tokenNode);
                nodes.push(calcNode);
            }
        }
    }
    
    
    
    /** 
     * nodeProcess 处理节点塞入时候，运算符优先级
     * @param nodes
     * @param tokenNode
     * @param calcNode
     */
    private void nodeProcess(Stack<Node> nodes, CalculateNode tokenNode, CalculateNode calcNode) {
        Objects.requireNonNull(calcNode, "calc node can not be null");
        if(nodes.isEmpty()) {
            calcNode.setLeft(tokenNode);
        } else {
            if(tokenNode.getOperation().getPriority() == calcNode.getOperation().getPriority()) {
                calcNode.setLeft(tokenNode);
            } else {
                Node node = nodes.pop();
                
                if(node.getNodeType().equals(NodeType.Calculate)) {
                    CalculateNode newNode = (CalculateNode) node;
                    if(calcNode.getOperation().getPriority() <= newNode.getOperation().getPriority()) {
                        newNode.setRight(tokenNode);
                        calcNode.setLeft(newNode);
                    } else {
                        nodeProcess(nodes, newNode, calcNode);
                    }
                } else {
                    calcNode.setLeft(tokenNode);
                }
            }
            
            
        }
        nodes.push(calcNode);
    }
    
    
    private Node resolveToken(String token) {
        if(StringUtils.isNotBlank(token)) {
            // 1.判断是否是数字或者变量，如果2者都不是，那么就异常，不支持
            // 2.
            if(token.startsWith(CompileExpression.SECTION_PRE)) {
                return resolveNodes.get(token);
            } else if (Pattern.matches(ParserConstant.VARIABLE_PATTERN_STR, token)) {
                // 这个token是个变量，返回变量节点
                return new VariableNode(token);
            } else if (Pattern.matches(ParserConstant.NUMBER_PATTERN_STR, token)) {
                return new DataNode(new BigDecimal(token));
            } else {
                throw new IllegalTokenException(token, "CAN NOT RECONIZE TOKEN:" + token);
            }
        }else {
            throw new IllegalTokenException(token, "NULL TOKEN");
        }
        
    }

}

