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
package com.baidu.rigel.biplatform.queryrouter.query.service;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContextSplitResult;

/**
 * 根据查询上下文进行拆分任务
 * 
 * @author xiaoming.chen
 *
 */
public interface QueryContextSplitService {

    /**
     * 拆分策略
     * 
     * @author xiaoming.chen
     */
    public static enum QueryContextSplitStrategy {
        MeasureType, Column, Row;

        /**
         * 获取指定策略的下一个策略,如果指定策略为空，返回第一个策略，若为最后一个，返回null
         * 
         * @param strategy 指定策略
         * @return 如果指定策略为空，返回第一个策略，若为最后一个，返回null
         */
        public static QueryContextSplitStrategy getNextStrategy(QueryContextSplitStrategy strategy) {
            QueryContextSplitStrategy[] strategys = QueryContextSplitStrategy.values();
            if (strategy == null) {
                return strategys[0];
            } else {
                int ordinal = strategy.ordinal() + 1;
                if (strategys.length >= (ordinal + 1)) {
                    return strategys[ordinal];
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 拆分接口:
     *  1.根据上次的拆分策略，取得当前拆分策略
     *  2. 根据拆分策略进行拆分
     *  3. 返回拆分结果
     * 
     * @param cube cube信息，主要获取指标信息
     * @param queryContext 问题模型转换处理的查询条件，包括维值树，过滤条件，查询指标等。
     * @param preSplitStrategy 上次的拆分类型
     * @return 拆分后的结果
     */
    QueryContextSplitResult split(QuestionModel question, DataSourceInfo dsInfo, Cube cube, QueryContext queryContext,
         QueryContextSplitStrategy preSplitStrategy);
    
    
    /** 
     * mergeDataModel
     * @param splitResult
     * @param cube
     * @return
     */
    DataModel mergeDataModel(QueryContextSplitResult splitResult);

}
