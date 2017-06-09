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

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Measure对象反序列化
 * 
 * @author xiaoming.chen
 *
 */
public class MeasureDeserialize implements JsonDeserializer<Measure> {

    @Override
    public Measure deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject jsObj = json.getAsJsonObject();
            
            String agg = jsObj.get("aggregator").getAsString();
            if(agg.equals(Aggregator.CALCULATED.name())) {
                String type = jsObj.get("type").getAsString();
                if (type.equals(MeasureType.CALLBACK.name())) {
                    return context.deserialize(jsObj, CallbackMeasure.class);
                }
                return context.deserialize(jsObj, ExtendMinicubeMeasure.class);
            }
            return context.deserialize(jsObj, MiniCubeMeasure.class);
        }
        return null;
    }

}
