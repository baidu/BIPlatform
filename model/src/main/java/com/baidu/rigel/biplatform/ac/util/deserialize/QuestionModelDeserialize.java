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
package com.baidu.rigel.biplatform.ac.util.deserialize;

import java.lang.reflect.Type;

import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SqlQuestionModel;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * questionModel对象反序列
 * 
 * @author luowenlei
 *
 */
public class QuestionModelDeserialize implements JsonDeserializer<QuestionModel> {
    
    @Override
    public QuestionModel deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject questionModelJsonObj = json.getAsJsonObject();
            
            if (questionModelJsonObj.get("sql") != null) {
                return context.deserialize(questionModelJsonObj, SqlQuestionModel.class);
            } else if (questionModelJsonObj.get("cube") != null) {
                return context.deserialize(questionModelJsonObj, ConfigQuestionModel.class);
            }
        }
        return null;
    }
    
}
