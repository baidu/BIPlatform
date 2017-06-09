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
package com.baidu.rigel.biplatform.tesseract.qsservice.query;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.MeasureParseResult;

/**
 * 指标解析
 * 
 * @author xiaoming.chen
 *
 */
public interface MeasureParseService {

    /**
     * 解析指标
     * 
     * @param cube 指标所属的cube
     * @param measure 指标名称或者表达式
     * @return 解析后的结果
     */
    MeasureParseResult parseMeasure(Cube cube, String measure);

}
