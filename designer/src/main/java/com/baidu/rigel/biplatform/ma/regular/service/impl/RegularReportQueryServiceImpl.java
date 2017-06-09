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
package com.baidu.rigel.biplatform.ma.regular.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.MiniCubeDriverManager;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportQueryService;
import com.baidu.rigel.biplatform.ma.regular.utils.ReportModel2QuestionModelUtils;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.google.common.collect.Maps;

/**
 *Description:
 * @author david.wang
 *
 */
@Service
public class RegularReportQueryServiceImpl implements RegularReportQueryService {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger (RegularReportQueryServiceImpl.class);
    
    private static final int THREAD_SIZE = Runtime.getRuntime ().availableProcessors () * 5;
    
    private static final ExecutorService ENGINE = Executors.newFixedThreadPool (THREAD_SIZE);
    
    /**
     * 
     * {@inheritDoc}
     * 
     * @see com.baidu.rigel.biplatform.ma.regular.service.RegularReportQueryService
     *      #queryReportData(com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo,
     *      com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel,
     *      java.util.Map)
     */
    @Override
    public Map<String, DataModel> queryReportData(DataSourceInfo dsInfo, 
        ReportDesignModel model, Map<String, String> params) {
        long begin = System.currentTimeMillis ();
        if (dsInfo == null || model == null) {
            LOG.error ("ds or model is null : ds : {} ---- model : {}", dsInfo, model);
            throw new RuntimeException ("缺少必要参数");
        }
        CompletionService<Map<String, DataModel>> executor = new ExecutorCompletionService<> (ENGINE);
        final MiniCubeConnection conn = MiniCubeDriverManager.getConnection(dsInfo);
        Map<String, QuestionModel> questionModels = 
            ReportModel2QuestionModelUtils.genQuestionModelsExcludeDs (model, params);
        questionModels.forEach ((k, v) ->{
            ConfigQuestionModel questionModel = (ConfigQuestionModel) v;
            questionModel.setDataSourceInfo (dsInfo);
            questionModel.setDataSourceInfoKey (dsInfo.getDataSourceKey ());
            executor.submit (new QueryTask (conn, k, questionModel));
        });
        Map<String, DataModel> rs = Maps.newHashMap ();
        int expect = questionModels.size ();
        for (int i = 0; i < expect; ++i) {
            try {
                Future<Map<String, DataModel>> f = executor.take ();
                rs.putAll (f.get ());
            } catch (Exception e) {
                LOG.error (e.getMessage (), e);
            }
        }
        
        int actual = rs.size ();
        if (actual != expect) {
            LOG.info ("result size can not matched expected. actual : [{}], expected : [{}]", actual, expect);
            throw new RuntimeException ("未正确执行所有区域查询，期望值 : [" + expect+ "], 真实值:[" + actual +"]");
        }
        long end = System.currentTimeMillis ();
        LOG.info ("generate report data cost {} ms", (end - begin));
        return rs;
    }
    
    
    /**
     * 
     *Description: QueryTask
     * @author david.wang
     *
     */
    private static class QueryTask implements Callable<Map<String, DataModel>> {

        protected MiniCubeConnection conn;
        protected String  areaId;
        protected QuestionModel questionModel;
        
        
        public QueryTask(MiniCubeConnection conn, String areaId, QuestionModel questionModel) {
            super ();
            this.conn = conn;
            this.areaId = areaId;
            this.questionModel = questionModel;
        }


        @Override
        public Map<String, DataModel> call() throws Exception {
            long begin = System.currentTimeMillis ();
            DataModel model = conn.query (questionModel);
            long cost = System.currentTimeMillis () - begin;
            Map<String, DataModel> rs = new HashMap<>();
            rs.put (areaId, model);
            LOG.info ("query data with question model cost {} ms", cost);
            return rs;
        }
        
    }
}
