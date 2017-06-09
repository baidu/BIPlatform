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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 对每个指标进行解析
 * 
 * @author xiaoming.chen
 *
 */
public class MeasureParseResult implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8776815596786643840L;

    /**
     * calateExpression 计算公式，类型还未确定 TODO 后续需要修改Object为公式对应的类型
     */
    private Object caculateExpression;

    /**
     * baseMeasureCondition 基础指标对应的偏移条件，如果没有偏移，不用设置就是查询基础指标
     */
    private List<MeasureCondition> baseMeasureCondition;

    /**
     * get baseMeasureCondition
     * 
     * @return the baseMeasureCondition
     */
    public List<MeasureCondition> getBaseMeasureCondition() {
        if (this.baseMeasureCondition == null) {
            this.baseMeasureCondition = new ArrayList<MeasureCondition>(1);
        }
        return baseMeasureCondition;
    }

    /**
     * set baseMeasureCondition with baseMeasureCondition
     * 
     * @param baseMeasureCondition the baseMeasureCondition to set
     */
    public void setBaseMeasureCondition(List<MeasureCondition> baseMeasureCondition) {
        this.baseMeasureCondition = baseMeasureCondition;
    }

    /**
     * 创建一个简单指标的指标解析结果，只包含本身的基础指标
     * 
     * @param measureName 指标名称
     * @return 解析结果
     */
    public static MeasureParseResult createBaseMeasure(String measureName) {
        MeasureParseResult result = new MeasureParseResult();
        result.getBaseMeasureCondition().add(new MeasureCondition(measureName));
        return result;
    }

    /**
     * get caculateExpression
     * 
     * @return the caculateExpression
     */
    public Object getCaculateExpression() {
        return caculateExpression;
    }

    /**
     * set caculateExpression with caculateExpression
     * 
     * @param caculateExpression the caculateExpression to set
     */
    public void setCaculateExpression(Object caculateExpression) {
        this.caculateExpression = caculateExpression;
    }

}
