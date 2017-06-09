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
package com.baidu.rigel.biplatform.ac.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.ConfigInfoUtils;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ServerUtils;

/**
 * 
 * 数据立方体接口。多维分析中的逻辑模型，通常是由对特定业务的衡量标准（指标）以及指标的属性组成（维度）。 指标构成立方体中的KPI属性，而属性则为分析KPI的角度。
 * 
 * @author xiaoming.chen
 * 
 */
public interface Cube extends OlapElement {

    /**
     * LOGGER
     */
    Logger LOGGER = LoggerFactory.getLogger(Cube.class);

    /**
     * cube所属的Schema
     * 
     * @return cube所属的Schema
     */
    Schema getSchema();

    /**
     * cube中的维度列表
     * 
     * @return 维度map
     */
    Map<String, Dimension> getDimensions();

    /**
     * cube中的指标集合
     * 
     * @return 指标map
     */
    Map<String, Measure> getMeasures();

    /**
     * 是否启用cache
     * 
     * @return 缓存启用状态
     */
    boolean enableCache();

    /**
     * 是否由多个事实表组成
     * 
     * @return
     */
    boolean isMutilple();

    /**
     * @return 产品线
     */
    String getProductLine();

    /**
     * 根据UniqueName查找Member
     * 
     * @param dataSourceInfo
     * @param uniqueName
     * @return
     * @throws MiniCubeQueryException
     */
    default Member lookUp(DataSourceInfo dataSourceInfo, String uniqueName, Map<String, String> params)
            throws MiniCubeQueryException {

        ConfigQuestionModel questionModel = new ConfigQuestionModel();
        questionModel.setDataSourceInfo(dataSourceInfo);
        questionModel.setCube(this);
        questionModel.setRequestParams(params);

        DimensionCondition dimCondition = new DimensionCondition(MiniCubeConnection.UNIQUENAME_PARAM_KEY);
        dimCondition.getQueryDataNodes().add(new QueryData(uniqueName));
        questionModel.getQueryConditions().put(MiniCubeConnection.UNIQUENAME_PARAM_KEY, dimCondition);

        Map<String, String> requestParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();
        String questionModelJson = AnswerCoreConstant.GSON.toJson(questionModel);
        requestParams.put(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY,
                questionModelJson);

        long current = System.currentTimeMillis();
        ServerUtils.setServerProperties(questionModelJson,
                ((ConfigQuestionModel) questionModel).getDataSourceInfo().getProductLine(),
                requestParams, headerParams);
        String response = HttpRequest.sendPost(ConfigInfoUtils.getServerAddress() + "/lookUp",
                requestParams, headerParams);
        ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(response, ResponseResult.class);

        if (StringUtils.isNotBlank(responseResult.getData())) {
            String metaJson = responseResult.getData();
            MetaJsonDataInfo metaJsonInfo = AnswerCoreConstant.GSON.fromJson(metaJson, MetaJsonDataInfo.class);
            Member member = JsonUnSeriallizableUtils.parseMetaJson2Member(this, metaJsonInfo);

            StringBuilder sb = new StringBuilder();
            sb.append("lookup uniquename:").append(uniqueName).append(" cost:")
                    .append(System.currentTimeMillis() - current).append("ms");
            LOGGER.info(sb.toString());
            return member;
        }
        throw new MiniCubeQueryException("query occur error,msg:" + responseResult.getStatusInfo());
    }

    @Override
    default public String getUniqueName() {
        return MetaNameUtil.makeUniqueName(getName());
    }

}
