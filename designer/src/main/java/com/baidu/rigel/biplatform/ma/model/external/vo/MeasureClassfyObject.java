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
package com.baidu.rigel.biplatform.ma.model.external.vo;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 *Description: 指标分类定义
 * @author david.wang
 *
 */
public class MeasureClassfyObject {
    
    /**
     * 指标元数据或者分类名称，
     * 如该字段描述指标列名称，要求名称为： [事实表名称.]事实表字段名称 格式 其中[] 中内容为可选字段
     * 
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String caption;
    
    /**
     * 指标描述信息
     */
    private String desc;
    
    /**
     * 当前分类选中状态
     */
    private Boolean selected = false;
    
    private List<MeasureClassfyObject> children;
    
    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
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
    public Boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the children
     */
    public List<MeasureClassfyObject> getChildren() {
        if (children == null) {
            this.children = Lists.newArrayList ();
        }
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<MeasureClassfyObject> children) {
        this.children = children;
    }

    /* 
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode ());
        return result;
    }

    /* 
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass () != obj.getClass ()) {
            return false;
        }
        MeasureClassfyObject other = (MeasureClassfyObject) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals (other.name)) {
            return false;
        }
        return true;
    }
    
    
}
