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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 占位符工具类,支持占位符格式为 ${name}
 * 
 * @author xiaoming.chen
 *
 */
public class PlaceHolderUtils {

    /**
     * 从一个源字符串中解析所有占位符信息 如 abc/${1}/${2}/abc.action,解析出来的就是 ${1},${2}
     * 
     * @param source 源字符串
     * @return 解析到的占位符信息
     */
    public static List<String> getPlaceHolders(String source) {
        if (StringUtils.isBlank(source)) {
            throw new IllegalArgumentException("can not get place hode list by blank source");
        }
        Pattern pattern = Pattern.compile("\\$\\{[^\\}]+\\}");
        Matcher match = pattern.matcher(source);
        List<String> result = new ArrayList<String>();
        while (match.find()) {
            result.add(match.group());
        }
        return result;
    }

    /**
     * 从占位符中解析KEY，占位符实例： ${abc}
     * <p>
     * 如果不符合占位符表达式：^\\$\\{[^\\}]+\\}$ 直接返回
     * </p>
     * 
     * @param placeHolder 占位符，
     * @return 从占位符中解析出来的KEY
     */
    public static String getKeyFromPlaceHolder(String placeHolder) {
        if (StringUtils.isBlank(placeHolder)) {
            throw new IllegalArgumentException("can not get key from empty placeHolder");
        }
        if (!Pattern.matches("^\\$\\{[^\\}]+\\}$", placeHolder)) {
            return placeHolder;
        } else {
            return placeHolder.substring(2, placeHolder.length() - 1);
        }
    }
    
    
    /** 
     * 从一个源字符串中解析所有占位符信息 如 abc/${1}/${2}/${1}/abc.action,解析出来的就是1,2
     * getPlaceHolderKeys
     * @param source
     * @return
     */
    public static Set<String> getPlaceHolderKeys(String source) {
        List<String> placeHolders = getPlaceHolders(source);
        Set<String> result = new HashSet<String>();
        if(CollectionUtils.isNotEmpty(placeHolders)) {
            for(String placeHolder : placeHolders) {
                result.add(getKeyFromPlaceHolder(placeHolder));
            }
        }
        return result;
    }

    /**
     * 将源字符串中的占位符替换成指定value 自动判断是否是占位符格式，如果不是补全以后替换
     * 
     * @param source 源字符串
     * @param placeHolder 占位符，可以直接传一个KEY，也可以输入完成占位符，如 url 或者 ${url}
     * @param value 占位符替换的值，可以为null
     * @return 返回替换完成后的字符串
     * @throws IllegalArgumentException 源字符串为空或者占位符为空
     */
    public static String replacePlaceHolderWithValue(String source, String placeHolder, String value) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(placeHolder)) {
            throw new IllegalArgumentException("params error, source : " + source + " placeHolder:" + placeHolder);
        }
        String oldKey = placeHolder;
        if (!Pattern.matches("^\\$\\{[^\\}]+\\}$", placeHolder)) {
            oldKey = "${" + oldKey + "}";
        }

        return source.replace(oldKey, value);

    }

}
