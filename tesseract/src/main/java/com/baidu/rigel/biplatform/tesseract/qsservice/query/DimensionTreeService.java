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
package com.baidu.rigel.biplatform.tesseract.qsservice.query;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection.DataSourceType;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.impl.SqlDimensionTreeServiceImpl;

/**
 * 维度维值树构建服务
 * 
 * @author xiaoming.chen
 *
 */
public interface DimensionTreeService<T extends DataSourceInfo> {

    /**
     * 初始化维度
     * 
     * @param cube 待初始化的cube
     * @param dataSourceInfo 数据源信息
     * @throws MetaException
     */
    void generateDimensionMemberTreeByCube(Cube cube, T dataSourceInfo) throws MetaException;

    /**
     * @param dimension
     * @param dataSourceInfo
     * @return
     */
    MemberNodeTree generateMemberNodeTree(Dimension dimension, T dataSourceInfo);

    /**
     * 
     * @param dataSourceInfo
     * @return
     */
    default DimensionTreeService<? extends DataSourceInfo> getInstance(T dataSourceInfo) {
        if (dataSourceInfo.getDataSourceType().equals(DataSourceType.SQL)) {
            return SqlDimensionTreeServiceImpl.getInstance();
        }
        throw new UnsupportedOperationException("just implement sql datasource type..");
    }

}
