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
package com.baidu.rigel.biplatform.tesseract.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * String2DateUtils
 * @author lijin
 *
 */
public class StringTools {
    /**
     * 
     * 获取日期字符串类型
     * @param str 日期字符串
     * @return DateFormatType
     */
    public static DateFormatType dateFormatType(String str) {
        DateFormatType result = null;
        if (!StringUtils.isBlank(str)) {
            if (str.matches("[0-9]*-[0-1][0-9]-[0-3][0-9]")) {
                result = DateFormatType.DATE_FORMAT_YYYY_MM_DD;
            } else if (str.matches("[0-9]*") && str.length() == 8) {
                result = DateFormatType.DATE_FORMAT_YYYYMMDD;
            }
            
        }
        return result;
    }
    
    /**
     * 
     * concatIndexBaseDir
     * 
     * @param filePath
     *            相对路径
     * @param nodeIndexBaseDir
     *            节点数据根路径
     * @return String
     */
    public static String concatIndexBaseDir(String filePath, String nodeIndexBaseDir) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(nodeIndexBaseDir)) {
            sb.append(nodeIndexBaseDir);
            sb.append(File.separator);
        }
        sb.append(filePath);
        return sb.toString();
    }
    
}
