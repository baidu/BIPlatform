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

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MeasureSerialize implements JsonSerializer<Measure> {
    
    @Override
    public JsonElement serialize(Measure src, Type typeOfSrc, JsonSerializationContext context) {
        if(src.getAggregator().equals(Aggregator.CALCULATED)) {
            if(src.getType().equals(MeasureType.CALLBACK)) {
                return context.serialize(src, CallbackMeasure.class);
            }
            return context.serialize(src, ExtendMinicubeMeasure.class);
        }
        return context.serialize(src, MiniCubeMeasure.class);
    }
}
