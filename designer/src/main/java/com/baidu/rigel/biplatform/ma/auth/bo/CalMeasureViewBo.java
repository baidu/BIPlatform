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
package com.baidu.rigel.biplatform.ma.auth.bo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * CalMeasureViewBo
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class CalMeasureViewBo implements Serializable {

    /**
     * CalMeasureViewObject.java -- long
     * description:
     */
    private static final long serialVersionUID = -3729981014323836550L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * name
     */
    private String name;
    
    /**
     * formula
     */
    private String formula;
    
    /**
     * caption
     */
    private String caption;
    
    /**
     * 回调指标url
     */
    private String url;
    
    /**
     * 属性信息
     */
    private Map<String, String> properties;
    
    /**
     * 衍生指标
     */
    private List<CalMeasureViewBo> cals = Lists.newArrayList();
    
    /**
     * 同比
     */
    private List<CalMeasureViewBo> tbs = Lists.newArrayList();
    
    /**
     * 环比
     */
    private List<CalMeasureViewBo> hbs = Lists.newArrayList();
    
    /**
     * 回调指标
     */
    private List<CalMeasureViewBo> callback = Lists.newArrayList();
    
    /**
     * referenceId
     */
    private Set<String> referenceNames;

    public CalMeasureViewBo() {
        this.referenceNames = Sets.newLinkedHashSet();
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }

    /**
     * @param formula the formula to set
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * @return the referenceId
     */
    public Set<String> getReferenceNames() {
        if (StringUtils.isNotEmpty(formula)) {
            String[] reference = this.formula.split("}");
            for (String tmp : reference) {
                this.referenceNames.add(tmp.substring(tmp.indexOf("{") + 1));
            }
        } else if (StringUtils.isNotEmpty(name)) {
            referenceNames.add(name.substring(0, name.length() - 3));
        }
        return referenceNames;
    }

    /**
     * @param referenceNames the referenceId to set
     */
    public void setReferenceNames(Set<String> referenceNames) {
        if (referenceNames != null) {
            this.referenceNames = referenceNames;
        }
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the tb
     */
    public List<CalMeasureViewBo> getTbs() {
        return tbs;
    }

    /**
     * @param tbs the tbs to set
     */
    public void setTbs(List<CalMeasureViewBo> tbs) {
        this.tbs = tbs;
    }

    /**
     * @return the cal
     */
    public List<CalMeasureViewBo> getCals() {
        return cals;
    }

    /**
     * @param cal the cal to set
     */
    public void setCals(List<CalMeasureViewBo> cals) {
        this.cals = cals;
    }

    /**
     * @return the hb
     */
    public List<CalMeasureViewBo> getHbs() {
        return hbs;
    }

    /**
     * @param hb the hb to set
     */
    public void setHbs(List<CalMeasureViewBo> hbs) {
        this.hbs = hbs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
    
    /**
     * 
     * @param json
     * @return
     */
    public static CalMeasureViewBo fromJson(String json) {
        try {
            CalMeasureViewBo viewBo = new CalMeasureViewBo();
            JSONObject jsonObj = new JSONObject(json);
            JSONObject extendInd = jsonObj.getJSONObject("extendInds");
            JSONArray rr = extendInd.getJSONArray("rr");
            JSONArray sr = extendInd.getJSONArray("sr");
            JSONArray callback = jsonObj.getJSONArray("callback");
            viewBo.setTbs(generalCalMeasure(sr));
            viewBo.setHbs(generalCalMeasure(rr));
            viewBo.setCallback(generalCalMeasure(callback));
            JSONArray calMembers = jsonObj.getJSONArray("calDeriveInds");
            viewBo.setCals(generalCalMeasure(calMembers));
            // 衍生指标
            return viewBo;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param array
     * @return
     */
    private static List<CalMeasureViewBo> generalCalMeasure(JSONArray array) {
        List<CalMeasureViewBo> rs = Lists.newArrayList();
        if (array == null || array.length() == 0) {
            return rs;
        }
        String json = null;
        for (int i = 0; i < array.length(); ++i) {
            try {
                json = array.get(i).toString();
                rs.add(GsonUtils.fromJson(json, CalMeasureViewBo.class));
            } catch (JSONException e) {
                throw new IllegalArgumentException("bad string : " + json);
            }
        }
        return rs;
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
        if (this.properties == null) {
            this.properties = Maps.newHashMap();
        }
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @return the callback
     */
    public List<CalMeasureViewBo> getCallback() {
        if (this.callback == null) {
            this.callback = Lists.newArrayList();
        }
        return callback;
    }

    /**
     * @param callback the callback to set
     */
    public void setCallback(List<CalMeasureViewBo> callback) {
        this.callback = callback;
    }
    
    
}
