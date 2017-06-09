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

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * dimension 反序列化
 * 
 * @author xiaoming.chen
 *
 */
public class DimensionDeserialize implements JsonDeserializer<Dimension> {

    @Override
    public Dimension deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Dimension dimension = null;
        if (json.isJsonObject()) {
            JsonObject dimensionObject = json.getAsJsonObject();

            if (dimensionObject.get("type") != null) {
                DimensionType dimensionType = DimensionType.valueOf(dimensionObject.get("type").getAsString());
                if (dimensionType.equals(DimensionType.TIME_DIMENSION)) {
                    dimension = context.deserialize(json, TimeDimension.class);
                } else {
                    dimension = context.deserialize(json, StandardDimension.class);
                }
                if (dimension != null && MapUtils.isNotEmpty(dimension.getLevels())) {
                    for (Level level : dimension.getLevels().values()) {
                        level.setDimension(dimension);
                    }
                }
            }
        }
        return dimension;
    }
}
