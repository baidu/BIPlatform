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
package com.baidu.rigel.biplatform.ac.util.serialize;

import java.lang.reflect.Type;

import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SqlQuestionModel;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 类QuestionModelSerialize.java的实现描述：TODO 类实现描述
 * 
 * @author luowenlei 2016年2月3日 下午3:10:55
 */
public class QuestionModelSerialize implements JsonSerializer<QuestionModel> {
    
    @Override
    public JsonElement serialize(QuestionModel src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.getAxisMetas() == null) {
            return context.serialize(src, SqlQuestionModel.class);
        } else {
            return context.serialize(src, ConfigQuestionModel.class);
        }
    }
    
}
