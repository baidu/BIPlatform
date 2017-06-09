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
package com.baidu.rigel.biplatform.ac.minicube;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;

/**
 * mini cube measure define
 * 
 * @author xiaoming.chen
 *
 */
public class MiniCubeMeasure extends OlapElementDef implements Measure {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4961420388712436605L;

    /**
     * aggregator measure agg type
     */
    private Aggregator aggregator = Aggregator.SUM;

    /**
     * 指标类型
     */
    private MeasureType type = MeasureType.COMMON;

    /**
     * 指标列名称
     */
    private String define;

    /**
     * measure 所在cube
     */
    @GsonIgnore
    private transient Cube cube;

    /**
     * construct with measure name
     * 
     * @param name measure name
     */
    public MiniCubeMeasure(String name) {
        super(name);
    }

    @Override
    public Aggregator getAggregator() {
        return aggregator;
    }

    /**
     * setter method for property aggregator
     * 
     * @param aggregator the aggregator to set
     */
    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public void setDefine(String define) {
        this.define = define;
    }

    @Override
    public String getDefine() {
        return this.define;
    }

    public MeasureType getType() {
        return type;
    }

    public void setType(MeasureType type) {
        this.type = type;
    }

    public Cube getCube() {
        return cube;
    }

    public void setCube(Cube cube) {
        this.cube = cube;
    }

}
