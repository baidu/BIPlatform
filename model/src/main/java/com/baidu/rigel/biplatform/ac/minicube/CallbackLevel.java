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

import java.util.Map;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.exception.MiniCubeException;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

/**
 * Callback的层级
 * 
 * @author xiaoming.chen
 * 
 */
@JsonIgnoreProperties
public class CallbackLevel extends OlapElementDef implements Level {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8532587623390954980L;

    /**
     * callbackUrl Callback的URL,URL支持多个host，多个host以逗号分隔后放到中括号中 如 http://[127.0.0.1:8080,123.1.1.1:8080]/abc/abc.action
     * url中支持占位符，占位符以$开头，$结尾，如： http://${url}/abc/${action}.action 请求的时候，需要把占位符放入callbackParams中，以上url的Callbackparams必须
     * 有 url:127.0.0.1,action:callback，系统在请求Callback url的时候会自动替换url中占位符
     */
    private String callbackUrl;

    /**
     * callbackParams Callback的参数
     */
    private Map<String, String> callbackParams;

    /**
     * refreshInterval 刷新间隔， -1表示和cube缓存一起刷新， 0表示不缓存，每次都取最新 正数，刷新间隔
     */
    private int refreshInterval = -1;

    /**
     * type 层级类型
     */
    private LevelType type = LevelType.CALL_BACK;

    /**
     * dimension 层级所属dimension
     */
    @JsonIgnore
    @GsonIgnore
    private Dimension dimension;

    /**
     * 对应事实表中的列
     */
    private String factTableColumn;

    // /**
    // * 是否对回调维度进行汇总
    // */
    // private boolean hasAllDim;

    /**
     * callback层级的构造函数
     * 
     * @param name callback level name
     * @param callbackUrl callback url
     */
    public CallbackLevel(String name, String callbackUrl) {
        super(name);
        this.callbackUrl = callbackUrl;
    }

    public CallbackLevel() {
        super(null);
    }

    @Override
    public LevelType getType() {
        return type;
    }

    /**
     * getter method for property callbackUrl
     * 
     * @return the callbackUrl
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * setter method for property callbackUrl
     * 
     * @param callbackUrl the callbackUrl to set
     */
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * getter method for property callbackParams
     * 
     * @return the callbackParams
     */
    public Map<String, String> getCallbackParams() {
        if (this.callbackParams == null) {
            this.callbackParams = Maps.newHashMap ();
        }
        return callbackParams;
    }

    /**
     * setter method for property callbackParams
     * 
     * @param callbackParams the callbackParams to set
     */
    public void setCallbackParams(Map<String, String> callbackParams) {
        this.callbackParams = callbackParams;
    }

    /**
     * getter method for property refreshInterval
     * 
     * @return the refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * setter method for property refreshInterval
     * 
     * @param refreshInterval the refreshInterval to set
     */
    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * setter method for property type
     * 
     * @param type the type to set
     */
    public void setType(LevelType type) {
        this.type = type;
    }

    @Override
    @JsonIgnore
    public Dimension getDimension() {
        return this.dimension;
    }

    /**
     * setter method for property dimension
     * 
     * @param dimension the dimension to set
     */
    @JsonIgnore
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    @JsonIgnore
    public String getDimTable() {
        throw new MiniCubeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallbackLevel clone() throws CloneNotSupportedException {
        return DeepcopyUtils.deepCopy(this);
    }

    /**
     * @return the factTableColumn
     */
    public String getFactTableColumn() {
        return factTableColumn;
    }

    /**
     * @param factTableColumn the factTableColumn to set
     */
    public void setFactTableColumn(String factTableColumn) {
        this.factTableColumn = factTableColumn;
    }

}
