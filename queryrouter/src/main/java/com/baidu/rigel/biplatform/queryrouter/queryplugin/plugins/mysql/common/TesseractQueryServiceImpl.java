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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.mysql.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.PropertiesFileUtils;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryService;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;

/**
 * tesseract查询接口实现
 * 
 * @author 罗文磊
 *
 */
@Service("queryTesseractService")
@Scope("prototype")
public class TesseractQueryServiceImpl implements QueryService {
    
    /**
     * the logger object
     */
    private static Logger logger = LoggerFactory.getLogger(TesseractQueryServiceImpl.class);
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.queryrouter.query.service.QueryService#query
     * (com.baidu.rigel.biplatform.ac.query.model.QuestionModel,
     * com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext)
     */
    @Override
    public DataModel query(QuestionModel questionModel, QueryContext queryContext) throws Exception {
        // TODO Auto-generated method stub
        long current = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        
        params.put(TesseractHttpConstants.QUESTIONMODEL_PARAM_KEY,
                AnswerCoreConstant.GSON.toJson(configQuestionModel));
        
        QueryContext queryContextNet = new QueryContext();
        queryContextNet.setColumnMemberTrees(queryContext.getColumnMemberTrees());
        queryContextNet.setDimsNeedSumBySubLevel(queryContext.getDimsNeedSumBySubLevel());
        queryContextNet.setFilterMemberValues(queryContext.getFilterMemberValues());
        queryContextNet.setFilterExpression(queryContext.getFilterExpression());
        queryContextNet.setQueryMeasures(queryContext.getQueryMeasures());
        queryContextNet.setRowMemberTrees(queryContext.getRowMemberTrees());
        // queryContextNet.
        params.put(MiniCubeConnection.QUERYCONTEXT_PARAM_KEY,
                AnswerCoreConstant.GSON.toJson(queryContextNet));
        Map<String, String> headerParams = new HashMap<String, String>();
        headerParams.put(TesseractHttpConstants.BIPLATFORM_QUERY_ROUTER_SERVER_TARGET_PARAM,
                TesseractHttpConstants.TESSERACT_TYPE);
        headerParams.put(TesseractHttpConstants.BIPLATFORM_PRODUCTLINE_PARAM, configQuestionModel
                .getDataSourceInfo().getProductLine());
        long curr = System.currentTimeMillis();
        logger.info("begin execute query with tesseract ");
        String tesseractHost = "";
        tesseractHost = PropertiesFileUtils.getPropertiesKey(
                TesseractHttpConstants.TESSERACT_CONNECTION_FILE,
                TesseractHttpConstants.TESSERACT_CONNECTION);
        try {
            String response = HttpRequest.sendPost(tesseractHost + "/queryIndex", params,
                    headerParams);
            logger.info("queryId:{} execute query with tesseract cost {} ms",
                    questionModel.getQueryId(), System.currentTimeMillis() - curr);
            ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(response,
                    ResponseResult.class);
            if (StringUtils.isBlank(responseResult.getData()) && responseResult.getStatus() != 0) {
                logger.error("queryId:{} execute query with tesseract occur an Error!"
                        + "pls check tesseract log; tesseract msg:{}", questionModel.getQueryId(),
                        responseResult.getStatusInfo());
                throw new Exception("execute query with tesseract occur an Error");
            } else {
                String dataModelJson = responseResult.getData();
                DataModel dataModel = JsonUnSeriallizableUtils.dataModelFromJson(dataModelJson);
                logger.info("queryId:{} execute query questionModel cost {} ms",
                        questionModel.getQueryId(), System.currentTimeMillis() - current);
                dataModel.setOthers(responseResult.getStatusInfo());
                return dataModel;
            }
        } catch (Exception e) {
            logger.error("queryId:{} execute query with tesseract occur an Error!"
                    + "pls check tesseract log; tesseract msg:{}", questionModel.getQueryId(),
                    e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
}
