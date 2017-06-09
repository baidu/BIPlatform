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
package com.baidu.rigel.biplatform.queryrouter.query.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;

/**
 * 元数据实际查询操作接口
 * 
 * @author xiaoming.chen
 *
 */
public interface MetaDataService {

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
     * lookUp
     *
     * @param dataSourceInfo
     * @param cube
     * @param uniqueNameList
     * @param params
     * @return
     * @throws Exception
     */
    List<MiniCubeMember> lookUp(DataSourceInfo dataSourceInfo, Cube cube,
            List<String> uniqueNameList, Map<String, String> params) throws Exception;

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

}
