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
package com.baidu.rigel.biplatform.ac.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.ConfigInfoUtils;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;

/**
 * minicube的查询接口
 * 
 * @author xiaoming.chen
 *
 */
public interface MiniCubeConnection {

    /**
     * DATASOURCETYPE_SQL SQL类型数据源
     */
    int DATASOURCETYPE_SQL = 1;

    /**
     * DATASOURCETYPE_FILE
     */
    int DATASOURCETYPE_FILE = 2;

    /**
     * DATASOURCETYPE_CUSTOM
     */
    int DATASOURCETYPE_CUSTOM = 3;

    /**
     * QUESTIONMODEL_PARAM_KEY
     */
    String QUESTIONMODEL_PARAM_KEY = "question";
    
    /**
     * QUERYREQUEST_PARAM_KEY
     */
    String QUERYREQUEST_PARAM_KEY="query";

    /**
     * QUERYCONTEXT_PARAM_KEY
     */
    String QUERYCONTEXT_PARAM_KEY = "context";

    /**
     * SPLITSTRATEGY_PARAM_KEY
     */
    String SPLITSTRATEGY_PARAM_KEY = "splitStrategy";

    /**
     * DATASOURCEINFO_PARAM_KEY
     */
    String DATASOURCEINFO_PARAM_KEY = "dataSourceInfo";

    /**
     * UNIQUENAME_PARAM_KEY
     */
    String UNIQUENAME_PARAM_KEY = "uniqueName";

    /**
     * CUBE_PARAM_KEY
     */
    String CUBE_PARAM_KEY = "cube";    
    
    /** 
     * DATASET_PARAM_KEY
     */
    String DATASET_PARAM_KEY = "datasets";
    
    /**
     * 事实表更新主键区间对应的参数名称
     */
    String PARAMS = "params";

    /**
     * 查询结果
     * 
     * @param questionModel
     * @return
     * @throws MiniCubeQueryException
     */
    DataModel query(QuestionModel questionModel) throws MiniCubeQueryException;

    /**
     * 发布模型接口
     * 
     * @param cubes 发布的模型列表
     * @param dataSourceInfo List 模型对应的数据源列表信息
     * @return 发布是否成功
     */
    default boolean publishCubes(List<Cube> cubes, List<DataSourceInfo> dataSourceInfoList) {
        Map<String, String> params = new HashMap<String, String>(5);
        params.put(DATASOURCEINFO_PARAM_KEY, AnswerCoreConstant.GSON.toJson(dataSourceInfoList));
        params.put(CUBE_PARAM_KEY, AnswerCoreConstant.GSON.toJson(cubes));

        String responseJson = HttpRequest.sendPost(ConfigInfoUtils.getServerAddress() + "/publish", params);
        ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(responseJson, ResponseResult.class);
        if (responseResult.getData() != null) {
            return true;
        }
        return false;
    }

    /**
     * 关闭连接，清除cube池子中的cube
     */
    void close();

    

    /**
     * connnection type
     * 
     * @author xiaoming.chen
     *
     */
    public enum DataSourceType {
        SQL(1), // if a sql connection ,we need a datasource
        FILE(2), // not implement
        HIVE(4),
        CUSTOM(3), // user custom datasource,support user custom data process
        PALO(6), // palo
        DRUID(7), // Druid
        ASYN(8); // ASYN 异步数据源
        /**
         * id ID
         */
        private int id;

        /**
         * constructor
         * 
         * @param id 类型ID
         */
        DataSourceType(int id) {
            this.id = id;
        }

        /**
         * getter method for property id
         * 
         * @return the id
         */
        public int getId() {
            return id;
        }

    }
    
    /**
     * TODO
     * @author david.wang
     *
     */
    public static class ConnectionUtil {
        
        private static final Logger LOG = LoggerFactory.getLogger (ConnectionUtil.class);
            /**
         * 刷新当前connection的缓存
         */
        public static boolean refresh(List<DataSourceInfo> dataSourceInfoList, String[] dataSets, String conditions) {
            final Map<String, String> params = new HashMap<String, String>(5);
            params.put(DATASOURCEINFO_PARAM_KEY, AnswerCoreConstant.GSON.toJson(dataSourceInfoList));
            params.put(DATASET_PARAM_KEY, StringUtils.join(dataSets, ','));
            if (!StringUtils.isEmpty(conditions)) {
                params.put(PARAMS, conditions);
            }
            LOG.info ("=========================update data detail info =========================");
            LOG.info ("========================= url : {}", ConfigInfoUtils.getServerAddress ());
            LOG.info ("========================= request params : {}", params); 
            String responseJson = HttpRequest.sendPost(ConfigInfoUtils.getServerAddress() + "/refresh", params);
            LOG.info ("========================= response : {}", responseJson);
            
            ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(responseJson, ResponseResult.class);
            if (responseResult.getData() != null) {
                return true;
            }
            return false;
        }
        
    }

}
