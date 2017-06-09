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

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeLevel;
import com.baidu.rigel.biplatform.ac.minicube.UserCustomLevel;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * level对象反序列
 * 
 * @author xiaoming.chen
 *
 */
public class LevelDeserialize implements JsonDeserializer<Level> {

    @Override
    public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject levelJsonObj = json.getAsJsonObject();

            if (levelJsonObj.get("type") != null) {
                LevelType levelType = LevelType.valueOf(levelJsonObj.get("type").getAsString());
                if (levelType.equals(LevelType.CALL_BACK)) {
                    return context.deserialize(levelJsonObj, CallbackLevel.class);
                } else if (levelType.name().equals(LevelType.USER_CUSTOM)) {
                    return context.deserialize(levelJsonObj, UserCustomLevel.class);
                } else {
                    return context.deserialize(levelJsonObj, MiniCubeLevel.class);
                }

            }
        }

        return null;
    }

}
