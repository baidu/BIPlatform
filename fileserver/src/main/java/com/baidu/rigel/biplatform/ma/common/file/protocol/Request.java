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
package com.baidu.rigel.biplatform.ma.common.file.protocol;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * 请求对象
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public class Request implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 686714690446844985L;
    
    /**
     * 请求指令
     */
    private Command command;
    
    /**
     * 请求参数
     */
    private Map<String, Object> params;
    
    public Request() {
        
    }
    
    /**
     * 构造函数
     * 
     * @param command
     * @param params
     */
    public Request(Command command, Map<String, Object> params) {
        super();
        this.command = command;
        this.params = params;
    }
    
    /**
     * 
     * @return 请求指令
     */
    public Command getCommand() {
        return this.command;
    }
    
    /**
     * 
     * @return 请求参数
     */
    public Map<String, Object> getParams() {
        return this.params;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    @Override
    public String toString() {
        return "[command : " + this.command.name() + " , params : " + this.params + "]";
    }
}
