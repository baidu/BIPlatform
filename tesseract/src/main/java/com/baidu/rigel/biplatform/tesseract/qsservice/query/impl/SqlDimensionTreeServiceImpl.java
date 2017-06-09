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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.impl;

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.meta.MetaDataService;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.DimensionTreeService;

/**
 * 暂时不用，以后维护维度缓存需要
 * 
 * @author xiaoming.chen
 *
 */
public class SqlDimensionTreeServiceImpl implements DimensionTreeService<SqlDataSourceInfo> {

    /**
     * instance 单例
     */
    private static SqlDimensionTreeServiceImpl instance;

    /**
     * log
     */
//    private Logger log = Logger.getLogger(this.getClass());

    /**
     * construct with
     */
    private SqlDimensionTreeServiceImpl() {
    }

    /**
     * 获取单例
     * 
     * @return SqlDimensionTreeServiceImpl实例
     */
    public synchronized static SqlDimensionTreeServiceImpl getInstance() {
        if (instance == null) {
            instance = new SqlDimensionTreeServiceImpl();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.tesseract.qsservice.query.DimensionTreeService#generateDimensionMemberTreeByCube(com.baidu.rigel
     * .biplatform.ac.model.Cube, com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public void generateDimensionMemberTreeByCube(Cube cube, SqlDataSourceInfo dataSourceInfo) throws MetaException {
        MetaDataService.checkCube(cube);
        MetaDataService.checkDataSourceInfo(dataSourceInfo);

        if (MapUtils.isEmpty(cube.getDimensions())) {
            throw new IllegalArgumentException("can not generate dimension member tree by empty dimension in cube"
                    + cube);
        }

        // for(Dimension dimension : cube.getDimensions().values()){
        //
        // }
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.tesseract.qsservice.query.DimensionTreeService#generateMemberNodeTree(com.baidu.rigel.biplatform
     * .ac.model.Dimension, com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public MemberNodeTree generateMemberNodeTree(Dimension dimension, SqlDataSourceInfo dataSourceInfo) {
        MetaDataService.checkDataSourceInfo(dataSourceInfo);
        if (dimension == null) {
            throw new IllegalArgumentException("argumeng is illegal,dimension:" + dimension + " dataSourceInfo:"
                    + dataSourceInfo);
        }
        if (!dimension.getType().equals(DimensionType.STANDARD_DIMENSION)) {
            return null;
        }

        return null;
    }

}
