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
package com.baidu.rigel.biplatform.ac.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.reflect.TypeToken;

/**
 * 维度的level定义:Level是指对同一维度的不同描述角度。如： 时间维度可以从年、季度、月份的角度进行描述。 需要注意的是：各个描述角度不在模型层面不存在父子关系。 而在存储方面，是存在父子关系的。
 * 拿时间维度为例，把维度（时间）看作向量的起点，描述角度（年、季度、月份）看作向量的终点， 则年、季度、月份对应向量的长度均相等，只不过向量的方向不同。但是在业务处理过程中，通常统计
 * 季度、月份会增加定语：哪一年的哪个季度、哪个月份，从而确定了物理存储模型中的父子关系。
 * 
 * @author xiaoming.chen
 * @see java.lang.Cloneable
 */
public interface Level extends OlapElement, Cloneable {

    /**
     * Level type
     * 
     * @return level type
     */
    LevelType getType();

    /**
     * level belongs to dimension
     * 
     * @return level dimension
     */
    Dimension getDimension();

    /**
     * 手动设置维度的Dimension
     * 
     * @param dimension level所属的维度
     */
    void setDimension(Dimension dimension);

    /**
     * 取得level和事实表关联字段
     * 
     * @return 关联字段
     */
    String getFactTableColumn();

    /**
     * levels members
     * 
     * @return level members
     * @throws MiniCubeQueryException 
     */
    @JsonIgnore
    default List<Member> getMembers(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params) throws MiniCubeQueryException {
        long current = System.currentTimeMillis();
        ConfigQuestionModel questionModel = new ConfigQuestionModel();
        questionModel.setCube(cube);
        questionModel.setDataSourceInfo(dataSourceInfo);
        questionModel.setRequestParams(params);
        
        DimensionCondition dimCondition = new DimensionCondition(this.getDimension().getName());
        dimCondition.getQueryDataNodes().add(new QueryData(getUniqueName()));
        questionModel.getQueryConditions().put(dimCondition.getMetaName(), dimCondition);
        String questionModelJson = AnswerCoreConstant.GSON.toJson(questionModel);
        Map<String,String> requestParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();
        requestParams.put(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY, questionModelJson);
        
        ServerUtils.setServerProperties(questionModelJson,
                ((ConfigQuestionModel) questionModel).getDataSourceInfo().getProductLine(),
                requestParams, headerParams);
        
        String response = HttpRequest.sendPost(ConfigInfoUtils.getServerAddress() + "/meta/getMembers",
                requestParams, headerParams);
        ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(response, ResponseResult.class);
        if (StringUtils.isNotBlank(responseResult.getData())) {
            String memberListJson = responseResult.getData();
            List<MetaJsonDataInfo> metaJsons = AnswerCoreConstant.GSON.fromJson(memberListJson, new TypeToken<List<MetaJsonDataInfo>>(){}.getType());
            List<Member> members = new ArrayList<Member>(metaJsons.size());
            if(CollectionUtils.isNotEmpty(metaJsons)){
                metaJsons.forEach((metaJson) -> {
                   members.add(JsonUnSeriallizableUtils.parseMetaJson2Member(cube, metaJson)); 
                });
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("execute query questionModel:").append(questionModel).append(" cost:")
                    .append(System.currentTimeMillis() - current).append("ms");
            return members;
        }
        throw new MiniCubeQueryException("query occur error,msg:" + responseResult.getStatusInfo());
        
    }

    /**
     * 维度表
     * 
     * @return 维度表表名
     */
    String getDimTable();

    /**
     * 维度表与事实表关联的外键列
     * 
     * @return 维度表与事实表关联外键列
     */
    String getPrimaryKey();

    @Override
    default public String getUniqueName() {
        return MetaNameUtil.makeUniqueName(getDimension(), getName());
    }
}
