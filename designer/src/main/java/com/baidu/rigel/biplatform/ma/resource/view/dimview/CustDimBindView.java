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

import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.CustDimDetail;
import com.google.common.collect.Lists;

/**
 * 
 * BO类：用户自定义维度与定义视图的映射关系
 * @author zhongyi
 *
 *         2014-7-31
 */
public class CustDimBindView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7311641828840944012L;
    
    /**
     * 立方体id
     */
    private String cubeId;
    
    /**
     * 用户定义维度详细信息
     */
    private List<CustDimDetail> children;
    
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
    
    /**
     * get the children
     * 
     * @return the children
     */
    public List<CustDimDetail> getChildren() {
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
    public void setChildren(List<CustDimDetail> children) {
        this.children = children;
    }
}