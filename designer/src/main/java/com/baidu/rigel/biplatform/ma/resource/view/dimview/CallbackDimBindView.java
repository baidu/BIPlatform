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
package com.baidu.rigel.biplatform.ma.resource.view.dimview;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CallbackDimDetail;
import com.google.common.collect.Lists;

/**
 * BO类：回调维度与回调维度定义视图之间的映射关系
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class CallbackDimBindView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3114690409424350154L;
    
    /**
     * 立方体定义
     */
    private String cubeId;
    
    /**
     * 维度详细定义信息
     */
    private List<CallbackDimDetail> children;
    
    /**
     * get the children
     * 
     * @return the children
     */
    public List<CallbackDimDetail> getChildren() {
        if (children == null) {
            children = Lists.newArrayList();
        }
        return children;
    }
    
    /**
     * set the children
     * 
     * @param children
     *            the children to set
     */
    public void setChildren(List<CallbackDimDetail> children) {
        this.children = children;
    }
    
    /**
     * get the cubeId
     * 
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }
    
    /**
     * set the cubeId
     * 
     * @param cubeId
     *            the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }
}