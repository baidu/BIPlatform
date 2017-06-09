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

import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;

/**
 * Level View Object
 * 
 * @author zhongyi
 *
 */
public class LevelObject implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -142299107502399691L;
    
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
    
    public String getCaption() {
        return caption;
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
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * setter method for property caption
     * 
     * @param caption
     *            the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            super.clone();
            return DeepcopyUtils.deepCopy(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}