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

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.PropertiesFileUtils;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryRequest;

/**
 * 类TesseractHttpUtils.java的实现描述：TODO 类实现描述
 * 
 * @author luowenlei 2015年11月17日 下午4:52:00
 */
public class TesseractHttpUtils {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(TesseractHttpUtils.class);
    
    /**
     * 查询tesseract是否有索引
     *
     * @return
     */
    public static boolean isQueryIndex(QuestionModel questionModel, QueryRequest queryRequest) {
        long current = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        SqlDataSourceInfo sqlDataSourceInfo = (SqlDataSourceInfo) configQuestionModel
                .getDataSourceInfo();
        String tesseractHost = PropertiesFileUtils.getPropertiesKey(
                TesseractHttpConstants.TESSERACT_CONNECTION_FILE,
                TesseractHttpConstants.TESSERACT_CONNECTION);
        if (sqlDataSourceInfo.getDataBase() != DataBase.MYSQL || StringUtils.isEmpty(tesseractHost)) {
            return false;
        }
        params.put("query", AnswerCoreConstant.GSON.toJson(queryRequest));
        Map<String, String> headerParams = new HashMap<String, String>();
        headerParams.put(TesseractHttpConstants.BIPLATFORM_QUERY_ROUTER_SERVER_TARGET_PARAM,
                TesseractHttpConstants.TESSERACT_TYPE);
        headerParams.put(TesseractHttpConstants.BIPLATFORM_PRODUCTLINE_PARAM, configQuestionModel
                .getDataSourceInfo().getProductLine());
        long curr = System.currentTimeMillis();
        logger.info("begin execute query with tesseract ");
        try {
            String response = HttpRequest.sendPost(tesseractHost + "/hasindex", params,
                    headerParams);
            logger.info("queryId:{} execute query with tesseract cost {} ms",
                    questionModel.getQueryId(), System.currentTimeMillis() - curr);
            ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(response,
                    ResponseResult.class);
            if (StringUtils.isBlank(responseResult.getData()) && responseResult.getStatus() != 0) {
                logger.error("queryId:{} search has index with tesseract occur an Error!"
                        + "pls check tesseract log; tesseract msg:{}", questionModel.getQueryId(),
                        responseResult.getStatusInfo());
                throw new MiniCubeQueryException("search has index with tesseract occur an Error");
            } else {
                String result = responseResult.getData();
                logger.info("queryId:{} has index:{} cost {} ms", questionModel.getQueryId(),
                        result, System.currentTimeMillis() - current);
                if ("true".equals(result)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("queryId:{} execute query with tesseract occur an Error!"
                    + "pls check tesseract log; tesseract msg:{}", questionModel.getQueryId(),
                    e.getMessage());
            e.printStackTrace();
            throw new MiniCubeQueryException(e);
        }
    }
}
