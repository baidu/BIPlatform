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
package com.baidu.rigel.biplatform.ma.file.serv.service;

/**
 * 文件存储路径：文件服务器指定的用于存储文件的跟路径
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class FileLocation {
    
    /**
     * 路径信息
     */
    private final String value;
    
    /**
     * 构造函数
     * @param value
     */
    public FileLocation(String value) {
        this.value = value;
    }
    
    /**
     * 获取路径信息
     * @return
     */
    public String value() {
        return value;
    }
}
