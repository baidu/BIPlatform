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
package com.baidu.rigel.biplatform.ma.report.service;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;

/**
 * 
 * 报表模型数据查询服务接口
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public interface ReportModelQueryService {
    
    /**
     * 获取维度成员
     * 
     * @param dim
     *            维度定义
     * @return
     * @throws DataSourceOperationException 
     * @throws MiniCubeQueryException 
     */
    public List<List<Member>> getMembers(Cube cube, Dimension dim, Map<String, String> params, String securityKey)
            throws MiniCubeQueryException, DataSourceOperationException;
    
    /**
     * 获取维度成员
     * 
     * @param dimensin
     *            维度定义
     * @param level
     *            级别定义
     * @return 成员信息
     * @throws MiniCubeQueryException 
     * @throws DataSourceOperationException 
     */
    public List<Member> getMembers(Cube cube, Dimension dimensin, Level level, Map<String, String> params, 
                String securityKey)
            throws MiniCubeQueryException, DataSourceOperationException;
    
    /**
     * 依据查询请求查询数据
     * 
     * @param model
     * @param action
     *            查询请求
     * @param usingCache
     * @return 结果集
     * @throws DataSourceOperationException
     * @throws QueryModelBuildException
     * @throws MiniCubeQueryException
     */
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action, boolean usingCache,
                boolean needSumary, String securityKey)
            throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException;
    
    
    /**
     * 依据查询请求查询数据
     * 
     * @param model
     * @param action
     *            查询请求
     * @param usingCache
     * @param requestParams 请求参数
     * @return 结果集
     * @throws DataSourceOperationException
     * @throws QueryModelBuildException
     * @throws MiniCubeQueryException
     */
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action, boolean usingCache, boolean needSumary, 
            Map<String, Object> requestParams, String securityKey)
            throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException;

    /**
     * 依据查询请求查询数据，针对平面表
     * @param model 模型
     * @param action 查询action
     * @param usingCache 是否使用缓存
     * @param requestParams 请求参数
     * @param pageInfo 分页信息
     * @param securityKey
     * @return
     * @throws DataSourceOperationException
     * @throws QueryModelBuildException
     * @throws MiniCubeQueryException
     */
    public ResultSet queryDatas(ReportDesignModel model, QueryAction action, boolean usingCache, Map<String, Object> requestParams,
    		PageInfo pageInfo, String securityKey) throws DataSourceOperationException, QueryModelBuildException, MiniCubeQueryException;
    /**
     * 根据uniqueName查询叶子节点members
     * @param tmpCube
     * @param currentUniqueName
     * @param params
     * @param securityKey
     * @return List<Member>
     */
    public List<Member> getMembers(Cube tmpCube, String currentUniqueName,
            Map<String, String> params, String securityKey);
    
}
