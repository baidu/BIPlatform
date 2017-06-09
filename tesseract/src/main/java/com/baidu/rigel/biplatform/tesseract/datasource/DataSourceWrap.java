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
package com.baidu.rigel.biplatform.tesseract.datasource;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;

/**
 * 返回的数据源封装接口
 * @author xiaoming.chen
 *
 */
public interface DataSourceWrap {
    
    /**
     * 返回数据源类型
     * @return 数据源类型
     */
    DataSourceType getDataSourceType();
    
    
    /** 
     * 获取数据源的失败次数
     * getFailCount
     * @return
     */
    int getFailCount();
    
    
    /** 
     * 数据源的错误次数加1
     * increaseFailCount
     */
    void increaseFailCount();
    
    
    /** 
     * 重置数据源的失败次数
     * resetFailCount
     */
    void resetFailCount();
    
    
    /** 
     * getFailTime
     * @return
     */
    long getFailTime();
    
}
