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
package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.google.common.collect.Maps;

/**
 * Measure View Object
 * @author zhongyi
 *
 */
public class MeasureObject implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -411127173674431549L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * name meta name
     */
    private String name;
    
    /**
     * type
     */
    private MeasureType type;
    
    /**
     * aggregator measure agg type
     */
    private Aggregator aggregator = Aggregator.SUM;
    
    /**
     * caption meta caption
     */
    private String caption;
    
    /**
     * visible
     */
    private boolean visible;
    
    /**
     * canToDim
     */
    private boolean canToDim;
    
    /**
     * 表达式
     */
    private String formula;
    
    /**
     * url
     */
    private String url;
    
    /**
     * properties
     */
    private Map<String, String> properties = Maps.newHashMap();
    
    /**
     * expr
     */
    private String expr;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isCanToDim() {
        return canToDim;
    }
    
    public void setCanToDim(boolean canToDim) {
        this.canToDim = canToDim;
    }
    
    public Aggregator getAggregator() {
        return aggregator;
    }
    
    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }
    
    public String getExpr() {
        return expr;
    }
    
    public void setExpr(String expr) {
        this.expr = expr;
    }
    
    public MeasureType getType() {
        return type;
    }
    
    public void setType(MeasureType type) {
        this.type = type;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    
}
