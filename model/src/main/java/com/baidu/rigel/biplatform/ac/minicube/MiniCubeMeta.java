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

import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;

/**
 * MiniCube的元数据
 * @author xiaoming.chen
 *
 */
public abstract class MiniCubeMeta implements OlapElement {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4543925294209520973L;
    
    /**
     * name meta name
     */
    private String name;
    
    /**
     * uniqueName meta unique name
     */
    private String uniqueName;
    
    /**
     * caption meta caption
     */
    private String caption;
    
    /**
     * description meta description
     */
    private String description;
    
    /**
     * expression 获取元数据的表达式，如字段
     */
    private String expression;
    
    /**
     * visible visiblity
     */
    private boolean visible = true;
    
    /**
     * connection 获取元数据的连接
     */
    private transient MiniCubeConnection connection;
    
    
    /**
     * construct with meta name
     * @param name meta name
     */
    public MiniCubeMeta(String name){
        this.name = name;
        this.uniqueName = MetaNameUtil.makeUniqueName(name);
    }

    /* (non-Javadoc)
     * @see com.baidu.biplatform.meta.OlapElement#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.biplatform.meta.OlapElement#getUniqueName()
     */
    @Override
    public String getUniqueName() {
        return this.uniqueName;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.biplatform.meta.OlapElement#getCaption()
     */
    @Override
    public String getCaption() {
        if(StringUtils.isBlank(this.caption)){
            return this.getName();
        }
        return this.caption;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.biplatform.meta.OlapElement#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see com.baidu.biplatform.meta.OlapElement#isVisible()
     */
    @Override
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * setter method for property name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * setter method for property uniqueName
     * @param uniqueName the uniqueName to set
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     * setter method for property caption
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * setter method for property description
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * setter method for property visible
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * getter method for property expression
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * setter method for property expression
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * getter method for property connection
     * @return the connection
     */
    public MiniCubeConnection getConnection() {
        return connection;
    }

    /**
     * setter method for property connection
     * @param connection the connection to set
     */
    public void setConnection(MiniCubeConnection connection) {
        this.connection = connection;
    }
    
}
