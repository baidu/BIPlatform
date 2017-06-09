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
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.HashMap;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * cube的实现定义
 * 
 * @author xiaoming.chen
 *
 */
@JsonIgnoreProperties
public class MiniCube extends OlapElementDef implements Cube {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8160947716561846011L;

    /**
     * schema cube belongs to schema
     */
    @JsonIgnore
    @GsonIgnore
    private Schema schema;

    /**
     * dimensions cube dimensions
     */
    private Map<String, Dimension> dimensions;

    /**
     * measures cube measures
     */
    private Map<String, Measure> measures;

    /**
     * enableCache
     */
    private boolean enableCache = true;

    /**
     * source cube数据来源
     */
    private String source;

    /**
     * 是否有多个事实表
     */
    private boolean mutilple;

    /**
     * 分表策略
     */
    private DivideTableStrategyVo divideTableStrategyVo;
    /**
     * productLine 产品线
     */
    private String productLine;

    public MiniCube() {
        this("undefine_name");
    }

    /**
     * construct with cube name
     * 
     * @param name cube name
     */
    public MiniCube(String name) {
        super(name);
    }

    /**
     * construct with cube name and cube schema object
     * 
     * @param name
     * @param schema
     */
    public MiniCube(String name, Schema schema) {
        super(name);
        this.schema = schema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Cube#getSchema()
     */
    @JsonIgnore
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Cube#getDimensions()
     */
    @Override
    public Map<String, Dimension> getDimensions() {
        if (this.dimensions == null) {
            this.dimensions = new HashMap<>();
        }
        return this.dimensions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Cube#getMeasures()
     */
    @Override
    public Map<String, Measure> getMeasures() {
        if (this.measures == null) {
            this.measures = new HashMap<String, Measure>();
        }
        return this.measures;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.biplatform.meta.Cube#enableCache()
     */
    @Override
    public boolean enableCache() {
        return this.enableCache;
    }

    /**
     * setter method for property enableCache
     * 
     * @param enableCache the enableCache to set
     */
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * setter method for property schema
     * 
     * @param schema the schema to set
     */
    @JsonIgnore
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * setter method for property dimensions
     * 
     * @param dimensions the dimensions to set
     */
    public void setDimensions(Map<String, Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * setter method for property measures
     * 
     * @param measures the measures to set
     */
    public void setMeasures(Map<String, Measure> measures) {
        this.measures = measures;
    }

    /**
     * getter method for property source
     * 
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * setter method for property source
     * 
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    public boolean isMutilple() {
        return mutilple;
    }

    public void setMutilple(boolean mutilple) {
        this.mutilple = mutilple;
    }

    
    /** 
     * 获取 divideTableStrategyVo 
     * @return the divideTableStrategyVo 
     */
    public DivideTableStrategyVo getDivideTableStrategyVo() {
    
        return divideTableStrategyVo;
    }

    /** 
     * 设置 divideTableStrategyVo 
     * @param divideTableStrategyVo the divideTableStrategyVo to set 
     */
    public void setDivideTableStrategyVo(DivideTableStrategyVo divideTableStrategyVo) {
    
        this.divideTableStrategyVo = divideTableStrategyVo;
    }

    /** 
     * 获取 serialversionuid 
     * @return the serialversionuid 
     */
    public static long getSerialversionuid() {
    
        return serialVersionUID;
    }

    /** 
     * 获取 enableCache 
     * @return the enableCache 
     */
    public boolean isEnableCache() {
    
        return enableCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{id : " + this.getId());
        str.append(",name : " + this.getName());
        str.append(", source : " + this.getSource());
        str.append(", schema : " + this.schema);
        str.append("}");
        return str.toString();
    }

    @Override
    public String getUniqueName() {
        return MetaNameUtil.makeUniqueName(getName());
    }

    /**
     * get productLine
     * 
     * @return the productLine
     */
    public String getProductLine() {
        return productLine;
    }

    /**
     * set productLine with productLine
     * 
     * @param productLine the productLine to set
     */
    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }
}
