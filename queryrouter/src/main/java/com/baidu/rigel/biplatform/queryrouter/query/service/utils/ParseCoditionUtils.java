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
package com.baidu.rigel.biplatform.queryrouter.query.service.utils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryContextAdapter;

/**
 * 
 * Description: RateCoditionBuildeUtils
 * @author david.wang
 *
 */
public final class ParseCoditionUtils {

    /**
     * RateCoditionBuildeUtils
     */
    private ParseCoditionUtils() {
        
    }
    
    /**
     * 包装queryContext
     * @param queryContext
     * @param questionModel
     * @param cube
     * @param dataSourceInfo
     * @return QueryContext
     */
    public static QueryContext decorateQueryContext(QueryContext queryContext, 
            QuestionModel questionModel, Cube cube, DataSourceInfo dataSourceInfo, QueryContextBuilder builder) {
        return new QueryContextAdapter(queryContext, questionModel, cube, dataSourceInfo, builder);
    }
}
