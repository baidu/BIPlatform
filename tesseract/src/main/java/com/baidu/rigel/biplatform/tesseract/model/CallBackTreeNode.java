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
package com.baidu.rigel.biplatform.tesseract.model;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 类PosTreeNode.java的实现描述
 * 
 * @author xiaoming.chen 2013-12-15 下午8:49:10
 */
public class CallBackTreeNode implements TreeModel {

    /**
     * default generate serialVersionUID
     */
    private static final long serialVersionUID = 8835259876881009914L;

    /**
     * 岗位ID
     */
    @Deprecated
    private String posId;
    
    
    /** 
     * id 查询ID
     */
    private String id;

    /**
     * 岗位名字
     */
    private String name;

    /**
     * 是否有孩子节点
     */
    private boolean hasChildren;

    /**
     * 当前节点管辖的一线节点ID列表
     */
    @Deprecated
    private List<String> csPosIds;
    
    
    /** 
     * csIds 叶子节点ID列表
     */
    
    private List<String> csIds;

    /**
     * 当前岗位的子岗位
     */
    private List<CallBackTreeNode> children;

    /**
     * default generate get posId
     * 
     * @return the posId
     */
    @Deprecated
    public String getPosId() {
        return posId;
    }

    /**
     * default generate posId param set method
     * 
     * @param posId the posId to set
     */
    @Deprecated
    public void setPosId(String posId) {
        this.posId = posId;
    }

    /**
     * default generate get name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * default generate name param set method
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * default generate get hasChildren
     * 
     * @return the hasChildren
     */
    public boolean isHasChildren() {
        return hasChildren;
    }

    /**
     * default generate hasChildren param set method
     * 
     * @param hasChildren the hasChildren to set
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    /**
     * default generate get children
     * 
     * @return the children
     */
    public List<CallBackTreeNode> getChildren() {
        return children;
    }

    /**
     * default generate children param set method
     * 
     * @param children the children to set
     */
    public void setChildren(List<CallBackTreeNode> children) {
        this.children = children;
    }

    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "CallBackTreeNode [posId=" + posId + ", id=" + id + ", name=" + name + ", hasChildren=" + hasChildren
                + ", csPosIds=" + csPosIds + ", csIds=" + csIds + ", children=" + children + "]";
    }

    /**
     * default generate get csPosIds
     * 
     * @return the csPosIds
     */
    @Deprecated
    public List<String> getCsPosIds() {
        return csPosIds;
    }

    /**
     * default generate csPosIds param set method
     * 
     * @param csPosIds the csPosIds to set
     */
    @Deprecated
    public void setCsPosIds(List<String> csPosIds) {
        this.csPosIds = csPosIds;
    }

    /** 
     * 获取 id 
     * @return the id 
     */
    public String getId() {
        if(StringUtils.isBlank(this.id)) {
            return this.posId;
        }
        return id;
    }

    /** 
     * 设置 id 
     * @param id the id to set 
     */
    public void setId(String id) {
    
        this.id = id;
    }

    /** 
     * 获取 csIds 
     * @return the csIds 
     */
    public List<String> getCsIds() {
        if(CollectionUtils.isEmpty(csIds)) {
            return this.csPosIds;
        }
        return csIds;
    }

    /** 
     * 设置 csIds 
     * @param csIds the csIds to set 
     */
    public void setCsIds(List<String> csIds) {
        this.csIds = csIds;
    }

}
