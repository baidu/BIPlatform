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

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;

/**
 * 结果集查询服务接口
 * 
 * @author xiaoming.chen
 *
 */
public interface QueryService {

    /**
     * 给一个问题模型，返回结果
     * 
     * @param questionModel 问题模型
     * @param queryContext 查询上下文
     * @param preSplitStrategy 拆分策略
     * @return 结果
     */
    public DataModel query(QuestionModel questionModel,
            QueryContext queryContext) throws Exception, RuntimeException;

}
