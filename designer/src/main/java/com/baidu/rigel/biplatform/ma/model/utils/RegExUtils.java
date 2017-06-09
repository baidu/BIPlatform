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
package com.baidu.rigel.biplatform.ma.model.utils;

import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 * 
 * @author jiangyichao
 *
 */
public final class RegExUtils {

    /**
     * RegExUtils
     */
    private RegExUtils() {
        
    }
    /**
     * 根据正则表达式对表名进行分类
     * 
     * @param tableName
     *            表名List
     * @param regexRule
     *            正则表达式List
     * @return 按照正则表达式分类好的表堆
     * @throws Exception
     */
    public static Hashtable<String, String[]> regExTableName(List<String> tableName,
        List<String> regExRule) throws IllegalArgumentException {
        Hashtable<String, String[]> cluster = new Hashtable<String, String[]>();
        try {
            if (tableName == null) {
                throw new IllegalArgumentException("表名列表为空");
            }
            for (String tableNameStr : tableName) {
                if (tableNameStr == null) {
                    throw new IllegalArgumentException("表名列表中含有为null的元素");
                }
                if ("".equals(tableNameStr)) {
                    throw new IllegalArgumentException("表名列表中含有为''的元素");
                }
                boolean match = false;
                for (String regExStr : regExRule) {
                    if (regExStr == null || "".equals(regExStr) || "[]".equals(regExStr)) {
                        continue;
                    }
                    Pattern p1 = Pattern.compile(regExStr);
                    Matcher m1 = p1.matcher(tableNameStr);
                    // 验证与正则表达式是否匹配，当第一次与某个正则表达式匹配时就会退出当前循环，然后验证下一个表名
                    if (m1.matches()) {
                        match = true;
                        String[] valArray = handle(cluster, regExStr, tableNameStr);
                        if (valArray != null) {
                            cluster.put(regExStr, valArray);
                        }
                        break;
                    }
                }
                // 当前表名与任意正则表达式不匹配
                if (!match) {
                    String[] valArray = handle(cluster, "other", tableNameStr);
                    if (valArray != null) {
                        cluster.put("other", valArray);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        }
        return cluster;
    }
    
    /**
     * Hashtable中key对应的数组
     * 
     * @param ht
     * @param key
     *            输入的key
     * @param value
     *            输入的value
     * @return 返回数组
     */
    private static String[] handle(Hashtable<String, String[]> ht, String key, String value) {
        String[] valArray = null;
        // ht中已包含key对应的元素
        if (ht.containsKey(key)) {
            String[] oldVals = (String[]) ht.get(key);
            valArray = new String[oldVals.length + 1];
            for (int i = 0; i < oldVals.length; i++) {
                valArray[i] = oldVals[i];
            }
            valArray[oldVals.length] = value;
        } else {
            valArray = new String[1];
            valArray[0] = value;
        }
        return valArray;
    }
}
