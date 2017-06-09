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
package com.baidu.rigel.biplatform.ac.model.callback;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * 
 * Description: 回调指标返回值，这里继承map仅仅是为了适配callback指标协议返回值
 * 
 * @author david.wang
 *
 */
public class CallbackMeasureVaue extends LinkedHashMap<String, List<String>> implements CallbackValue {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1556216018258969488L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("{");
        this.forEach((key, value) -> {
            str.append(key + " : [");
            value.forEach(v -> str.append( v + ", "));
            str.append("], ");
        }); 
        str.append("}");
        return str.toString();
    }
}
