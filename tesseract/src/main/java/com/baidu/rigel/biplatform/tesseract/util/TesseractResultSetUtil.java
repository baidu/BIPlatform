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
/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.util;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Aggregator;

/**
 * TesseractResultSetUtil
 * 
 * @author lijin
 *
 */
public class TesseractResultSetUtil {
    
    /**
     * PROPERTYKEY_CONN_STR
     */
    private static final String PROPERTYKEY_CONN_STR = "_";
    
    /**
     * 
     * getQueryMeasureFieldName getQueryMeasureFieldName
     * 
     * @param measure
     *            measure
     * @param aggregator
     *            aggregator
     * @return String
     */
    public static String getQueryMeasureFieldName(String measure, Aggregator aggregator) {
        if (!StringUtils.isEmpty(measure) && aggregator != null
            && !StringUtils.isEmpty(aggregator.name())) {
            return measure + PROPERTYKEY_CONN_STR + aggregator.name();
        }
        return null;
    }
    
}
