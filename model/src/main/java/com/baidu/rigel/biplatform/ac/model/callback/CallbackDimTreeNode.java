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
package com.baidu.rigel.biplatform.ac.model.callback;

import java.util.List;

/**
 * Description: 回调纬度节点数据
 * @author david.wang
 *
 */
public class CallbackDimTreeNode implements CallbackValue {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3464184628076416403L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * name
     */
    private String name;
    
    /**
     * 当前维度节点所有叶子节点id信息
     */
    private List<String> csIds;
    
    /**
     *孩子节点
     */
    private List<CallbackDimTreeNode> children;
    
    /**
     * 是否包含孩子节点
     */
    private boolean hasChildren;
    
    
    
    /**
     * @return the hasChildren
     */
    public boolean isHasChildren() {
        return hasChildren;
    }

    /**
     * @param hasChildren the hasChildren to set
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
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
     * @return the csIds
     */
    public List<String> getCsIds() {
        return csIds;
    }

    /**
     * @param csIds the csIds to set
     */
    public void setCsIds(List<String> csIds) {
        this.csIds = csIds;
    }

    /**
     * @return the children
     */
    public List<CallbackDimTreeNode> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<CallbackDimTreeNode> children) {
        this.children = children;
    }

    /**
     * @return the hasChildern
     */
    public boolean isHasChildern() {
        return (this.children != null && this.children.size() > 0) || this.hasChildren;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("{ name : " + this.name);
        str.append(", id : " + id);
        str.append(", csIds : [");
        if (this.csIds != null) {
            for (String s : this.csIds) {
                str.append(s + ",");
            }
        }
        str.append("], children : [");
        if (this.children != null) {
            for (CallbackDimTreeNode child : this.children) {
                str.append(child.toString() + ", ");
            }
        }
        str.append("]");
        return super.toString();
    }

}
