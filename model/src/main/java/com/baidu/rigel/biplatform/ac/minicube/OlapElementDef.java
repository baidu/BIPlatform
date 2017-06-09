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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;

/**
 * 
 * 多维对象基础定义：包含名称、标识名称、显示名称、描述 以及可见性
 * 
 * @author xiaoming.chen
 *
 */
public abstract class OlapElementDef implements OlapElement, Serializable, Cloneable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4543925294209520973L;

    /**
     * id
     */
    private String id;

    /**
     * name meta name
     */
    private String name;

    /**
     * caption meta caption
     */
    private String caption;

    /**
     * description meta description
     */
    private String description;

    // /**
    // * expression 获取元数据的表达式，如字段
    // */
    // private String expression;

    /**
     * visible visiblity
     */
    private boolean visible = true;

    /**
     * 自定义级别对应事实表中的列的信息，与该级别对应的维度列信息一致
     */
    private String primaryKey;

    /**
     * construct with meta name
     * 
     * @param name meta name
     */
    public OlapElementDef(String name) {
        this.name = name;
//         this.uniqueName = MetaNameUtil.makeUniqueName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getCaption() {
        if (StringUtils.isBlank(this.caption)) {
            return this.getName();
        }
        return this.caption;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * setter method for property caption
     * 
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * setter method for property description
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * setter method for property visible
     * 
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return DeepcopyUtils.deepCopy(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((caption == null) ? 0 : caption.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((getUniqueName() == null) ? 0 : getUniqueName().hashCode());
        result = prime * result + (visible ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
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
        if (!(obj instanceof OlapElementDef)) {
            return false;
        }
        OlapElementDef other = (OlapElementDef) obj;
        if (caption == null) {
            if (other.caption != null) {
                return false;
            }
        } else if (!caption.equals(other.caption)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (getUniqueName() == null) {
            if (other.getUniqueName() != null) {
                return false;
            }
        } else if (!getUniqueName().equals(other.getUniqueName())) {
            return false;
        }
        if (visible != other.visible) {
            return false;
        }
        return true;
    }

    /**
     * get primaryKey
     * 
     * @return the primaryKey
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * set primaryKey with primaryKey
     * 
     * @param primaryKey the primaryKey to set
     */
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    // /**
    // * getter method for property expression
    // * @return the expression
    // */
    // public String getExpression() {
    // return expression;
    // }
    //
    // /**
    // * setter method for property expression
    // * @param expression the expression to set
    // */
    // public void setExpression(String expression) {
    // this.expression = expression;
    // }

}
