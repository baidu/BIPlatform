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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.service;

import com.baidu.rigel.biplatform.queryrouter.handle.model.QueryHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;



/**
 * 类CountNumService.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年11月16日 下午2:44:28
 */
public interface CountNumService {

    
    /**
     * getTotalRecordSize
     * 
     * @param questionModel
     *            questionModel
     * @param QueryExpression
     *            sqlExpression
     * @return DataModel DataModel
     */
    public int getTotalRecordSize(PlaneTableQuestionModel planeQuestionModel,
            QueryHandler newQueryRequest);

}
