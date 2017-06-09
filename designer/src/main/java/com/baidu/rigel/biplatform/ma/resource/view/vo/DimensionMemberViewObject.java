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
package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 维度成员VO类
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class DimensionMemberViewObject implements Serializable {

    /**
     * DimensionMemberViewObject.java -- long
     * description:
     */
    private static final long serialVersionUID = -2085711831290400280L;
    
    /**
     * uniqueName
     */
    private String name;
    
    /**
     * 展示值
     */
    private String caption;
    
    /**
     * 选中状态
     */
    private boolean selected = false;
    
    /**
     * needLimit
     */
    private boolean needLimit = false;
    
    /**
     * 孩子节点
     */
    private List<DimensionMemberViewObject> children;

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
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the needLimit
     */
    public boolean isNeedLimit() {
        return needLimit;
    }

    /**
     * @param needLimit the needLimit to set
     */
    public void setNeedLimit(boolean needLimit) {
        this.needLimit = needLimit;
    }

    /**
     * @return the children
     */
    public List<DimensionMemberViewObject> getChildren() {
        if (this.children == null) {
            return Lists.newArrayList();
        }
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<DimensionMemberViewObject> children) {
        this.children = children;
    }
    
    
}
