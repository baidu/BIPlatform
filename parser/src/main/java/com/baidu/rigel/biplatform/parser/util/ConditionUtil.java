
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
package com.baidu.rigel.biplatform.parser.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月26日 
 * @since jdk 1.8 or after
 */
public class ConditionUtil {
    
    
    /** 
     * 将多个contex的Condition进行简单合并
     * simpleMergeContexsCondition
     * @param contexs
     * @return
     */
    public static Map<Condition, Set<String>> simpleMergeContexsCondition(Collection<CompileContext> contexs) {
        
        Map<Condition, Set<String>> result = new HashMap<Condition, Set<String>>();
        if(CollectionUtils.isNotEmpty(contexs)) {
            Map<Condition, Set<String>> condition = null;
            for(CompileContext contex : contexs) {
                // 扔到新集合中，避免对原来的有影响
                condition = new HashMap<Condition, Set<String>>(contex.getConditionVariables());
                if(result.isEmpty()) {
                    result.putAll(condition);
                } else if (MapUtils.isNotEmpty(condition)){
                    for(Entry<Condition, Set<String>> entry : result.entrySet()) {
                        if(condition.containsKey(entry.getKey())) {
                            entry.getValue().addAll(condition.get(entry.getKey()));
                            condition.remove(entry.getKey());
                        }
                    }
                    if(MapUtils.isNotEmpty(condition)) {
                        result.putAll(condition);
                    }
                }
            }
        }
        return result;
    }

}

