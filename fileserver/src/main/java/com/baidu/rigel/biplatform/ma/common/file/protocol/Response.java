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
 * 响应
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class Response implements Serializable {
    
    /**
     * serialized id
     */
    private static final long serialVersionUID = 8703812658204812119L;
    
    /**
     * 响应状态
     */
    private ResponseStatus status;
    
    /**
     * 响应信息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private Object datas;
    
    public Response() {
        
    }
    
    public Response(ResponseStatus status, String message, Map<String, Object> datas) {
        super();
        this.status = status;
        this.message = message;
        this.datas = datas;
    }
    
    public ResponseStatus getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getDatas() {
        return datas;
    }
    
    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setDatas(Object datas) {
        this.datas = datas;
    }
    
    /**
     * 
     */
    @Override
    public String toString() {
        return "[status : " + this.status + ", message : " + this.message + ", datas : "
            + this.datas + "]";
    }
    
}
