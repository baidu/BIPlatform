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
package com.baidu.rigel.biplatform.ac.query.model;


/**
 * 指标条件
 * 
 * @author xiaoming.chen
 *
 */
public class MeasureCondition implements MetaCondition {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5059328459757316603L;

    /**
     * metaName 元数据的name
     */
    private String metaName;

    /**
     * measureConditions 指标条件
     */
    private SQLCondition measureConditions;
    
    private MetaType metaType;

    /**
     * construct with metaUniqueName
     * 
     * @param metaName meta unique name
     */
    public MeasureCondition(String metaName) {
        this.metaName = metaName;
        this.metaType = MetaType.Measure;
    }
    
    public MeasureCondition() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ac.query.model.MetaCondition#getMetaUniqueName ()
     */
    @Override
    public String getMetaName() {
        return metaName;
    }

    /**
     * getter method for property measureConditions
     * 
     * @return the measureConditions
     */
    public SQLCondition getMeasureConditions() {
        return measureConditions;
    }

    /**
     * setter method for property measureConditions
     * 
     * @param measureConditions the measureConditions to set
     */
    public void setMeasureConditions(SQLCondition measureConditions) {
        this.measureConditions = measureConditions;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MeasureCondition [metaUniqueName=" + metaName + ", measureConditions=" + measureConditions + "]";
    }    
}
