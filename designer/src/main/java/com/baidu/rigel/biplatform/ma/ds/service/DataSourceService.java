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
package com.baidu.rigel.biplatform.ma.ds.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;

/**
 * 
 * 数据源管理服务
 * 
 * @author david.wang
 *
 */
public interface DataSourceService {
    
    /**
     * 
     * 增加或者修改数据源定义,新增数据源返回值中包含数据源id，如果更新数据源返回值与参数一致
     * 
     * @param ds
     *            数据源定义
     * @return 修改后的数据源定义
     * @throws DataSourceOperationException
     *             数据源保存或者更新失败抛出异常
     */
    public DataSourceDefine saveOrUpdateDataSource(DataSourceDefine ds, String securityKey)
            throws DataSourceOperationException;
    
    /**
     * 删除数据源
     * 
     * @param id
     *            数据源id
     * @return 成功删除数据源返回true，数据源不存在false
     * @throws DataSourceOperationException
     *             删除失败抛出异常
     */
    public boolean removeDataSource(String id) throws DataSourceOperationException;
    
    /**
     * 检测数据源名称是否存在，业务流程要求同一产品线名称唯一
     * 
     * @param name
     * @return
     * @throws DataSourceOperationException
     */
    public boolean isNameExist(String name) throws DataSourceOperationException;
    
    /**
     * 检测数据源连接是否有效
     * 
     * @param ds
     *            数据源定义
     * @return 成功连接返回true 失败返回false
     */
    public boolean isValidateConn(DataSourceDefine ds, String securityKey);
    
    /**
     * 检测产品线下定义的所有数据源
     * 
     * 
     * @return 数据源定义列表
     * @throws DataSourceOperationException
     *             不能正确操作抛出异常
     */
    public DataSourceDefine[] listAll() throws DataSourceOperationException;
    
    /**
     * 
     * 查询特定产品线下的数据源定义
     * 
     * @param id
     *            数据源id
     * 
     * @return 数据源定义
     * @throws DataSourceOperationException
     */
    public DataSourceDefine getDsDefine(String id) throws DataSourceOperationException;
    
    /**
     * 
     * 查询特定产品线下的数据源定义
     * 
     * @param id
     *            数据源id
     * 
     * @return 数据源定义
     * @throws DataSourceOperationException
     */
    public DataSourceDefine getDsDefine(String queryDsId,
            @SuppressWarnings("rawtypes") Map requestParams) throws DataSourceOperationException;

    /**
     * 依据产品线和数据源名称获取数据源定义信息
     * @param productLine 产品线名称
     * @param dsName 数据源名称
     * @return DataSourceDefine
     */
    public DataSourceDefine getDsDefine(String productLine, String dsName) throws DataSourceOperationException;
}
