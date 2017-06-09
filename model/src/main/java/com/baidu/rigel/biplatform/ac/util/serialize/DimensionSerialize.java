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

import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 维度序列化
 * 
 * @author xiaoming.chen
 *
 */
public class DimensionSerialize implements JsonSerializer<Dimension> {

    @Override
    public JsonElement serialize(Dimension src, Type typeOfSrc, JsonSerializationContext context) {

        if (src.getType().equals(DimensionType.TIME_DIMENSION)) {
            return context.serialize(src, TimeDimension.class);
        } else {
            return context.serialize(src, StandardDimension.class);
        }
    }

}
