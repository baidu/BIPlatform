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

import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MeasureCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition.MetaType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 元数据条件反序列化实现
 * 
 * @author xiaoming.chen
 *
 */
public class MetaConditionDeserialize implements JsonDeserializer<MetaCondition> {

    @Override
    public MetaCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject conditionObj = json.getAsJsonObject();

            MetaType metaType = context.deserialize(conditionObj.get("metaType"), MetaType.class);
            if (metaType.equals(MetaType.Dimension)) {
                return context.deserialize(json, DimensionCondition.class);
            } else {
                return context.deserialize(json, MeasureCondition.class);
            }
        }
        return null;
    }

}
