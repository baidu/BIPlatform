
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
package com.baidu.rigel.biplatform.parser.exception;

import com.baidu.rigel.biplatform.parser.node.Node;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月23日 
 * @since jdk 1.8 or after
 */
public class NodeCompileException extends RuntimeException {
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -5574571351365519539L;
    
    private Node node;
    
    // 后续会把错误消息归类，设置错误码
    private String message;
    
    public NodeCompileException(Node node, String message) {
        super("compile node catch error,node:" + node + " error message:" + message);
        this.node = node;
        this.message = message;
    }

    /** 
     * 获取 node 
     * @return the node 
     */
    public Node getNode() {
    
        return node;
    }

    /** 
     * 设置 node 
     * @param node the node to set 
     */
    public void setNode(Node node) {
    
        this.node = node;
    }

    /** 
     * 获取 message 
     * @return the message 
     */
    public String getMessage() {
    
        return message;
    }

    /** 
     * 设置 message 
     * @param message the message to set 
     */
    public void setMessage(String message) {
    
        this.message = message;
    }

}

