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
import java.util.List;

import com.baidu.rigel.biplatform.ac.exception.DerivativeIndException;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;

/**
 * 衍生指标的工具类
 * 
 * @author zhongyi
 *
 */
public class DerivativeIndUtils {
    
    /**
     * 获取衍生指标中的基本指标名称列表
     * 
     * @param measure 衍生指标
     * @return 基本指标名称列表
     * @throws Exception 
     */
    public static List<String> getOriIndNames(Measure measure) {
        if (measure == null) {
            throw new DerivativeIndException("Measure can not be null !");
        }
        List<String> oriIndNames = new ArrayList<String>();
        if (measure.getType() != MeasureType.CAL) {
            return oriIndNames;
        }
        ExtendMinicubeMeasure m = (ExtendMinicubeMeasure) measure;
        oriIndNames.addAll(m.getRefIndNames());
        return oriIndNames;
    }
}
