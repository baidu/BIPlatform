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
import java.util.List;

/**
 * 基础指标和对应的偏移条件
 * @author xiaoming.chen
 *
 */
public class MeasureCondition implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7498125711503514982L;


    private String name;
    
    /**
     * condition 查询这个基础指标对应查询上下文的偏移条件，比如查询昨天的，上周的等，
     * TODO 后续需要完善这个偏移条件
     */
    private List<Object> condition;
    
    /**
     * constructor
     * @param name
     */
    public MeasureCondition(String name) {
        this.name = name;
    }

    /**
     * get name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * set name with name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get condition
     * @return the condition
     */
    public List<Object> getCondition() {
        return condition;
    }

    /**
     * set condition with condition
     * @param condition the condition to set
     */
    public void setCondition(List<Object> condition) {
        this.condition = condition;
    }
}
