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
package com.baidu.rigel.biplatform.ac.util;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.deserialize.CubeDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.DataSourceInfoDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.DimensionDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.LevelDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.MeasureDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.MetaConditionDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.QuestionModelDeserialize;
import com.baidu.rigel.biplatform.ac.util.deserialize.SchemaDeserialize;
import com.baidu.rigel.biplatform.ac.util.serialize.CubeSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.DataSourceInfoSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.DimensionSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.LevelSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.MeasureSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.MetaConditionSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.QuestionModelSerialize;
import com.baidu.rigel.biplatform.ac.util.serialize.SchemaSerialize;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * answercore 常量
 * 
 * @author xiaoming.chen
 *
 */
public class AnswerCoreConstant {

    /**
     * GSON 忽略掉属性上加了GsonIgnore注解的属性
     */
    public static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder()
        .disableHtmlEscaping();
        // 添加如果有GsonIgnore注解，忽略序列化
        builder.addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                GsonIgnore gsonIgnore = f.getAnnotation(GsonIgnore.class);
                if (gsonIgnore != null) {
                    return gsonIgnore.ignore();
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        // 需要将对象的接口和实现都添加到类型映射中
        builder.registerTypeAdapter(QuestionModel.class, new QuestionModelDeserialize());
        builder.registerTypeAdapter(Dimension.class, new DimensionDeserialize());
        builder.registerTypeAdapter(Level.class, new LevelDeserialize());
        builder.registerTypeAdapter(Cube.class, new CubeDeserialize());
        builder.registerTypeAdapter(Measure.class, new MeasureDeserialize());
        builder.registerTypeAdapter(MetaCondition.class, new MetaConditionDeserialize());
        builder.registerTypeAdapter(DataSourceInfo.class, new DataSourceInfoDeserialize());
        builder.registerTypeAdapter(QuestionModel.class, new QuestionModelSerialize());
        builder.registerTypeAdapter(Dimension.class, new DimensionSerialize());
        builder.registerTypeAdapter(Level.class, new LevelSerialize());
        builder.registerTypeAdapter(Cube.class, new CubeSerialize());
        builder.registerTypeAdapter(Schema.class, new SchemaSerialize());
        builder.registerTypeAdapter(DataSourceInfo.class, new DataSourceInfoSerialize());
        builder.registerTypeAdapter(Measure.class, new MeasureSerialize());
        builder.registerTypeAdapter(MetaCondition.class, new MetaConditionSerialize());
        builder.registerTypeAdapter(Schema.class, new SchemaDeserialize());
        GSON = builder.create();
    }

}
