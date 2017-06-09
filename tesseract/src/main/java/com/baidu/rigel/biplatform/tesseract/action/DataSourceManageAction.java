
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
package com.baidu.rigel.biplatform.tesseract.action;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ResponseResultUtils;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;


/** 
 * 数据源管理接口
 * @author xiaoming.chen
 * @version  2015年1月5日 
 * @since jdk 1.8 or after
 */
@RestController
@RequestMapping("/datasource")
public class DataSourceManageAction {
    
    @Resource
    private DataSourcePoolService dataSourcePoolService;
    
    
    
    /** 
     * addDataSourceInfo
     * @param dataSourceInfo
     * @return
     */
    @RequestMapping("/add")
    public ResponseResult addDataSourceInfo(@RequestBody String updateJson) {
        try {
            Map<String, String> requestParams = MetaQueryAction.parseRequestJson(updateJson);
            String dataSourceJson = requestParams.get(MiniCubeConnection.DATASOURCEINFO_PARAM_KEY);
            DataSourceInfo dataSourceInfo = AnswerCoreConstant.GSON.fromJson(dataSourceJson, DataSourceInfo.class);
            dataSourcePoolService.initDataSourceInfo(dataSourceInfo);
        } catch (DataSourceException e) {
            ResponseResultUtils.getErrorResult(e.getMessage(), 100);
        }
        return ResponseResultUtils.getCorrectResult("OK", null);
    }
    
    @RequestMapping("/destroy/{dsId}")
    public ResponseResult destroyDataSource(@PathVariable String dsId) {
        // TODO 改完集群缓存方案后需要修改，需要通知所有节点进行删除
        try {
            dataSourcePoolService.destroyDataSourceInfo(dsId);
        } catch (DataSourceException e) {
            ResponseResultUtils.getErrorResult(e.getMessage(), 101);
        }
        return ResponseResultUtils.getCorrectResult("destroy datasourceinfo success", null);
    }
    
    
    @RequestMapping("/update")
    public ResponseResult updateDataSource(@RequestBody String updateJson) {
        try {
            Map<String, String> requestParams = MetaQueryAction.parseRequestJson(updateJson);
            String dataSourceJson = requestParams.get(MiniCubeConnection.DATASOURCEINFO_PARAM_KEY);
            DataSourceInfo dataSourceInfo = AnswerCoreConstant.GSON.fromJson(dataSourceJson, DataSourceInfo.class);
            // TODO 改完集群缓存方案后需要修改，需要通知所有节点进行更新
            dataSourcePoolService.updateDataSourceInfo(dataSourceInfo);
        } catch (DataSourceException e) {
            ResponseResultUtils.getErrorResult(e.getMessage(), 102);
        }
        return ResponseResultUtils.getCorrectResult("update datasourceinfo success", null);
    }

}

