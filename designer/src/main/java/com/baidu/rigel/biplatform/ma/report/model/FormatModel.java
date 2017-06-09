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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 格式定义
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class FormatModel implements Serializable {

    /**
     * serialize id
     */
    private static final long serialVersionUID = 452576215631369839L;

    /**
     * 数据格式
     */
    private Map<String, String> dataFormat = Maps.newHashMap();

    /**
     * 条件格式
     */
    private Map<String, String> conditionFormat = Maps.newHashMap();

    /**
     * 指标提示信息
     */
    private Map<String, String> toolTips = Maps.newHashMap();

    /**
     * 指标颜色定义
     */
    private Map<String, String> colorFormat = Maps.newHashMap();

    /**
     * 指标位置定义
     */
    private Map<String, String> positions = Maps.newHashMap();

    /**
     * 表格指标对齐定义
     */
    private Map<String, String> textAlignFormat = Maps.newHashMap();
    /**
     * 多维表格跳转信息定义,其中key为指标id，value为要跳转到的映射对象
     */
    private Map<String, LinkInfo> linkInfo = Maps.newLinkedHashMap();

    /**
     * @return the dataFormat
     */
    public Map<String, String> getDataFormat() {
        return dataFormat;
    }

    /**
     * @return the conditionFormat
     */
    public Map<String, String> getConditionFormat() {
        return conditionFormat;
    }

    /**
     * @return the toolTips
     */
    public Map<String, String> getToolTips() {
        if (this.toolTips == null) {
            this.toolTips = Maps.newConcurrentMap();
        }
        return toolTips;
    }

    /**
     * @param toolTips the toolTips to set
     */
    public void setToolTips(Map<String, String> toolTips) {
        this.toolTips = toolTips;
    }

    /**
     * reset format model;
     */
    public void reset() {
        this.dataFormat = Maps.newHashMap();
        this.conditionFormat = Maps.newHashMap();
        this.toolTips = Maps.newHashMap();
        this.colorFormat = Maps.newHashMap();
        this.textAlignFormat = Maps.newHashMap();
        this.linkInfo = Maps.newHashMap();
    }

    public void removeItem(String id) {
        this.getDataFormat().remove(id);
        this.getToolTips().remove(id);
        this.getConditionFormat().remove(id);
        this.getColorFormat().remove(id);
        this.getPositions().remove(id);
        this.getTextAlignFormat().remove(id);
    }

    public void init(String name) {
        this.getDataFormat().put(name, null);
        this.getToolTips().put(name, name);
        this.getConditionFormat().put(name, null);
        this.getColorFormat().put(name, null);
        this.getPositions().put(name, "0");
        this.getTextAlignFormat().put(name, "left");
    }

    /**
     * @return the colorFormat
     */
    public Map<String, String> getColorFormat() {
        if (this.colorFormat == null) {
            return Maps.newHashMap();
        }
        return colorFormat;
    }

    /**
     * @param colorFormat the colorFormat to set
     */
    public void setColorFormat(Map<String, String> colorFormat) {
        this.colorFormat = colorFormat;
    }

    /**
     * @return the positions
     */
    public Map<String, String> getPositions() {
        if (this.positions == null) {
            this.positions = Maps.newHashMap();
        }
        return positions;
    }

    /**
     * @param positions the positions to set
     */
    public void setPositions(Map<String, String> positions) {
        this.positions = positions;
    }

    /**
     * 设置文本对齐样式
     * 
     * @param textAlignFormat
     */
    public void setTextAlignFormat(Map<String, String> textAlignFormat) {
        this.textAlignFormat = textAlignFormat;
    }

    /**
     * 获取文件对齐样式
     * 
     * @return 文件对齐样式
     */
    public Map<String, String> getTextAlignFormat() {
        if (this.textAlignFormat == null) {
            this.textAlignFormat = Maps.newHashMap();
        }
        return textAlignFormat;
    }

    /**
     * @return the linkInfo
     */
    public Map<String, LinkInfo> getLinkInfo() {
        if (this.linkInfo == null) {
            this.linkInfo = Maps.newLinkedHashMap();
        }
        return linkInfo;
    }

    /**
     * @param linkInfo the linkInfo to set
     */
    public void setLinkInfo(Map<String, LinkInfo> linkInfo) {
        this.linkInfo = linkInfo;
    }

}
