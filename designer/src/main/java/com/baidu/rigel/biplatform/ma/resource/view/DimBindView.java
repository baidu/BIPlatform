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
package com.baidu.rigel.biplatform.ma.resource.view;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ma.resource.view.dimview.CallbackDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.CustDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.DateDimBindView;
import com.baidu.rigel.biplatform.ma.resource.view.dimview.NormalDimBindView;
import com.google.common.collect.Lists;

/**
 * 
 * 维度绑定关系定义
 * @author zhongyi
 *
 *         2014-7-31
 */
public class DimBindView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5719450034014063654L;
    
    /**
     * 普通维度绑定关系
     */
    private List<NormalDimBindView> normal;
    
    /**
     * 时间维度绑定关系
     */
    private List<DateDimBindView> date;
    
    /**
     * 回调维度映射关系
     */
    private List<CallbackDimBindView> callback;
    
    /**
     * 用户自定义维度映射关系
     */
    private List<CustDimBindView> custom;
    
    /**
     * get the normal
     * 
     * @return the normal
     */
    public List<NormalDimBindView> getNormal() {
        if (normal == null) {
            normal = Lists.newArrayList();
        }
        return normal;
    }
    
    /**
     * set the normal
     * 
     * @param normal
     *            the normal to set
     */
    public void setNormal(List<NormalDimBindView> normal) {
        this.normal = normal;
    }
    
    /**
     * get the date
     * 
     * @return the date
     */
    public List<DateDimBindView> getDate() {
        if (date == null) {
            date = Lists.newArrayList();
        }
        return date;
    }
    
    /**
     * set the date
     * 
     * @param date
     *            the date to set
     */
    public void setDate(List<DateDimBindView> date) {
        this.date = date;
    }
    
    /**
     * get the callback
     * 
     * @return the callback
     */
    public List<CallbackDimBindView> getCallback() {
        if (callback == null) {
            callback = Lists.newArrayList();
        }
        return callback;
    }
    
    /**
     * set the callback
     * 
     * @param callback
     *            the callback to set
     */
    public void setCallback(List<CallbackDimBindView> callback) {
        this.callback = callback;
    }
    
    /**
     * get the custom
     * 
     * @return the custom
     */
    public List<CustDimBindView> getCustom() {
        if (custom == null) {
            custom = Lists.newArrayList();
        }
        return custom;
    }
    
    /**
     * set the custom
     * 
     * @param custom
     *            the custom to set
     */
    public void setCustom(List<CustDimBindView> custom) {
        this.custom = custom;
    }
    
}