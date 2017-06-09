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
package com.baidu.rigel.biplatform.ma.model.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.DivideTableStrategyVo;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceInfoReaderServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnInfo;
import com.baidu.rigel.biplatform.ma.model.meta.ColumnMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.FactTableMetaDefine;
import com.baidu.rigel.biplatform.ma.model.meta.TableInfo;
import com.baidu.rigel.biplatform.ma.model.service.CubeMetaBuildService;
import com.baidu.rigel.biplatform.ma.model.utils.RegExUtils;
import com.google.common.collect.Lists;

/**
 * 
 * 立方体构建服务
 * @author zhongyi
 *
 *         2014-8-1
 */
@Service("cubeMetaBuildService")
public class CubeMetaBuildServiceImpl implements CubeMetaBuildService {
    
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(CubeMetaBuildServiceImpl.class);
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.model.service.CubeBuildService#getAllTable
     * (java.lang.String)
     */
    @Override
    public List<TableInfo> getAllTable(String dsId, String securityKey) throws DataSourceOperationException {
        DataSourceDefine ds = null;
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            ds = dsService.getDsDefine(dsId);
            dsInfoReaderService = DataSourceInfoReaderServiceFactory.
                    getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
            List<TableInfo> tables = dsInfoReaderService.getAllTableInfos(ds, securityKey);
            return tables;
        } catch (Exception e) {
            logger.error("[ERROR] --- --- --- --- Fail in read ds by id: " + dsId + ": {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DataSourceOperationException(e);
        } 
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.model.service.CubeBuildService#initCubeTables
     * (java.util.List, java.util.List)
     */
    @Override
    public List<FactTableMetaDefine> initCubeTables(String dsId, List<String> tableIds,
            List<String> regxs, String securityKey) throws DataSourceOperationException {
        return this.getAllFactTableMetaDefine(dsId, tableIds, regxs, null, securityKey);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FactTableMetaDefine> initCubeTables(String dsId, List<String> tableIds,
            Map<String, DivideTableStrategyVo> divideTableStrategys,
            String securityKey) throws DataSourceOperationException {
        List<FactTableMetaDefine> tableMetas = Lists.newArrayList(); 
        DataSourceDefine ds = null;
        try {
            ds = dsService.getDsDefine(dsId);
        } catch (DataSourceOperationException e) {
            logger.info("can not get datasource define with id : " + dsId, e);
            throw e;
        }
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            dsInfoReaderService = DataSourceInfoReaderServiceFactory.
                getDataSourceInfoReaderServiceInstance(ds.getDataSourceType().name ());
            for (String tableId : tableIds) {
                FactTableMetaDefine tableMeta = new FactTableMetaDefine();
                tableMeta = new FactTableMetaDefine();
                tableMeta.setCubeId(tableId);
                tableMeta.setName(tableId);
                if (divideTableStrategys != null && divideTableStrategys.containsKey(tableId)) {
                    tableMeta.setMutilple(true);
                    tableMeta.setDivideTableStrategyVo(divideTableStrategys.get(tableId));
                } else {
                    tableMeta.setMutilple(false);                    
                }
                List<ColumnInfo> cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, tableId);
                addColumnToTableMeta(tableMeta, cols);
                tableMetas.add(tableMeta);                
            }
        } catch (Exception e) {
            logger.error("[ERROR] --- --- --- --- Fail in read ds by id: " + dsId + ": {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DataSourceOperationException(e);
        }
        return tableMetas;        
    }
    
    /**
     * 
     * getAllFactTableMetaDefine
     * @param dsId
     * @param tableIds
     * @param regxs
     * @param divideTableStrategyVo
     * @param securityKey
     * @return
     * @throws DataSourceOperationException
     */
    private List<FactTableMetaDefine> getAllFactTableMetaDefine(String dsId, List<String> tableIds, List<String> regxs,
            DivideTableStrategyVo divideTableStrategyVo, String securityKey) throws DataSourceOperationException {
        List<FactTableMetaDefine> tableMetas = Lists.newArrayList();
        // 按照正则表达式分表
        Map<String, String[]> tableMap = RegExUtils.regExTableName(tableIds, regxs);
        
        DataSourceDefine ds = null;
        try {
            ds = dsService.getDsDefine(dsId);
        } catch (DataSourceOperationException e) {
            logger.info("can not get datasource define with id : " + dsId, e);
            throw e;
        }
        DataSourceInfoReaderService dsInfoReaderService = null;
        try {
            dsInfoReaderService =
                    DataSourceInfoReaderServiceFactory.getDataSourceInfoReaderServiceInstance(
                            ds.getDataSourceType().name ());
            for (String key : tableMap.keySet()) {
                String[] tables = tableMap.get(key);
                FactTableMetaDefine tableMeta = null;
                if ("other".equals(key)) {
                    for (String table : tables) {
                        tableMeta = new FactTableMetaDefine();
                        tableMeta.setCubeId(table);
                        tableMeta.setName(table);
                        tableMeta.setMutilple(false);
                        List<ColumnInfo> cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, table);
                        addColumnToTableMeta(tableMeta, cols);
                        tableMetas.add(tableMeta);
                    }
                } else {
                    tableMeta = new FactTableMetaDefine();
                    tableMeta.setCubeId(key);
                    tableMeta.setName(key);
                    tableMeta.setMutilple(true);
                    tableMeta.setRegExp(key);
                    if (tables != null && tables.length > 0) {
                        String tableExample = tables[0];
                        List<ColumnInfo> cols = dsInfoReaderService.getAllColumnInfos(ds, securityKey, tableExample);
                        addColumnToTableMeta(tableMeta, cols);
                    }
                    tableMetas.add(tableMeta);
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR] --- --- --- --- Fail in read ds by id: " + dsId + ": {}", e.getMessage());
            logger.error("[ERROR] --- --- --- --- stackTrace :", e);
            throw new DataSourceOperationException(e);
        }
        return tableMetas;
    }
    private void addColumnToTableMeta(FactTableMetaDefine tableMeta, List<ColumnInfo> cols) {
        for (ColumnInfo col : cols) {
            ColumnMetaDefine column = new ColumnMetaDefine();
            column.setName(col.getName());
            column.setCaption(col.getComment());
//            column.setName(StringUtils.hasText(col.getComment())? col.getName() : col.getId());
//            column.setCaption(StringUtils.hasText(col.getComment())? col.getComment() :col.getName());
            tableMeta.addColumn(column);
        }
    }

   
}