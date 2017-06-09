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
package com.baidu.rigel.biplatform.tesseract.meta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;

/**
 * 元数据实际查询操作接口
 * 
 * @author xiaoming.chen
 *
 */
public interface MetaDataService {

    /**
     * CUBE_CACHE_NAME cube对象的缓存
     */
    String CUBE_CACHE_NAME = "cubePool";

    /**
     * 从缓存中获取立方体
     * 
     * @param cubeId 立方体的ID
     * @return 立方体对象
     */
    Cube getCube(String cubeId) throws MiniCubeQueryException;

    /**
     * 缓存cube模型
     * 
     * @param dataSourceInfoKey cube对应的是那个数据源的KEY
     * @param cube cube模型
     */
    void cacheCube(Cube cube);

    /**
     * 将数据源信息对应的一系列cube执行发布事件，并缓存数据源信息和cube信息
     * 
     * @param cubes 待发布的事件
     * @param dataSourceInfoList 发布数据源列表
     * @throws Exception
     */
    void publish(List<Cube> cubes, List<DataSourceInfo> dataSourceInfoList) throws Exception;
    
    
    
    /** 
     * refresh 刷新
     * @param dataSourceInfoList
     * @param dataSetStr
     * @throws Exception
     */
    void refresh(List<DataSourceInfo> dataSourceInfoList, String dataSetStr) throws Exception;
    
    /**
     * refresh 刷新
     * @param dataSourceInfoList
     * @param dataSetStr
     * @param params
     * @throws Exception
     */
    void refresh(List<DataSourceInfo> dataSourceInfoList, String dataSetStr,
            Map<String, Map<String, BigDecimal>> params) throws Exception;

    /**
     * 获取level的members
     * 
     * @param dataSourceKey 数据源的KEY
     * @param cubeId cubeId
     * @param dimensionName 维度名称
     * @param levelName 层级的名称
     * @return 返回members
     * @throws MiniCubeQueryException 查询异常
     * @throws MetaException cube is illegal
     */
    List<MiniCubeMember> getMembers(String dataSourceKey, String cubeId, String dimensionName, String levelName,
            Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSource
     * @param cube
     * @param dimensionName
     * @param levelName
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    List<MiniCubeMember> getMembers(DataSourceInfo dataSource, Cube cube, String dimensionName, String levelName,
            Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * 获取指定UniqueName的children
     * 
     * @param dataSourceKey 数据源的KEY
     * @param cubeId 数据模型ID
     * @param uniqueName 维值的UniqueName
     * @param params Callback的参数（Callback维度需要提供）
     * @return 指定维值的孩子
     * @throws MiniCubeQueryException 查询节点异常
     * @throws MetaException 获取元数据信息异常
     */
    List<MiniCubeMember> getChildren(String dataSourceKey, String cubeId, String uniqueName, Map<String, String> params)
            throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSourceKey
     * @param cubeId
     * @param member
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    List<MiniCubeMember> getChildren(String dataSourceKey, String cubeId, MiniCubeMember member,
            Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSource
     * @param cube
     * @param uniqueName
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    List<MiniCubeMember> getChildren(DataSourceInfo dataSource, Cube cube, String uniqueName, Map<String,
            String> params) throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSource
     * @param cube
     * @param member
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    List<MiniCubeMember> getChildren(DataSourceInfo dataSource, Cube cube, MiniCubeMember member,
            Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSourceKey
     * @param cubeId
     * @param uniqueName
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    MiniCubeMember lookUp(String dataSourceKey, String cubeId, String uniqueName, Map<String, String> params)
            throws MiniCubeQueryException, MetaException;

    /**
     * @param dataSource
     * @param cube
     * @param uniqueName
     * @param params
     * @return
     * @throws MiniCubeQueryException
     * @throws MetaException
     */
    MiniCubeMember lookUp(DataSourceInfo dataSource, Cube cube, String uniqueName, Map<String, String> params)
            throws MiniCubeQueryException, MetaException;

    /**
     * 校验数据源信息
     * 
     * @param dataSourceInfo 数据源信息
     */
    public static void checkDataSourceInfo(DataSourceInfo dataSourceInfo) {
        if (dataSourceInfo == null || !dataSourceInfo.validate()) {
            throw new IllegalArgumentException("dataSourceInfo is illgeal:" + dataSourceInfo);
        }
    }
    
    public static boolean validateDataSourceInfo(DataSourceInfo dataSourceInfo) {
        if (dataSourceInfo == null || !dataSourceInfo.validate()) {
            return false;
        }
        return true;
    }
    

    /**
     * 校验cube
     * 
     * @param cube cube
     * @throws MetaException cube不合法
     */
    public static void checkCube(Cube cube) throws MetaException {
        if (cube == null) {
            throw new IllegalArgumentException("cube is null");
        }
        if (MapUtils.isEmpty(cube.getDimensions()) || MapUtils.isEmpty(cube.getMeasures())) {
            throw new MetaException("cube is illegal,no dimension or no measure:" + cube);
        }

    }

    List<MiniCubeMember> lookUp(DataSourceInfo dataSourceInfo, Cube cube,
            List<String> uniqueNameList, Map<String, String> params) throws Exception;
}
