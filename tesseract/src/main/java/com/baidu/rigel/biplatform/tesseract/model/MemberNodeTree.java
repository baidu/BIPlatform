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
package com.baidu.rigel.biplatform.tesseract.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;

/**
 * 维值树
 * 
 * @author xiaoming.chen
 *
 */
public class MemberNodeTree implements Serializable, Comparable<MemberNodeTree> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8511984568585171226L;

    /**
     * name 维值name
     */
    private String name;

    /**
     * uniqueName 维值UniqueName
     */
    private String uniqueName;

    /**
     * caption 维值的显示名称
     */
    private String caption;

    /**
     * children 维值的子节点
     */
    private List<MemberNodeTree> children;

    /**
     * parent 维值的父节点
     */
    @GsonIgnore
    private MemberNodeTree parent;

    /**
     * ordinal 排序
     */
    private String ordinal;

    /**
     * querySource leafId对应的查询的源信息（SQL表示事实表的字段）
     */
    private String querySource;
    
    /**
     * hasChildren 是否有孩子
     */
    private boolean hasChildren;
    
    /**
     * isSummary 是否是汇总节点
     */
    private boolean isSummary;

    /**
     * 节点实际查询使用的叶子ID列表
     */
    private Set<String> leafIds;
    
    
    private boolean isTime;
    
    /**
     * summaryIds
     */
    private Set<String> summaryIds;
    
    private boolean isCallback;
    

    /**
     * @return the isCallback
     */
    public boolean isCallback() {
        return isCallback;
    }

    /**
     * @param isCallback the isCallback to set
     */
    public void setCallback(boolean isCallback) {
        this.isCallback = isCallback;
    }

    public boolean isTime() {
		return isTime;
	}

	public void setTime(boolean isTime) {
		this.isTime = isTime;
	}


	/**
     * construct with
     * 
     * @param parent
     */
    public MemberNodeTree(MemberNodeTree parent) {
        this.parent = parent;
    }

    public MemberNodeTree() {
    }

    /**
     * getter method for property name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * setter method for property name
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter method for property uniqueName
     * 
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * setter method for property uniqueName
     * 
     * @param uniqueName the uniqueName to set
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     * getter method for property caption
     * 
     * @return the caption
     */
    public String getCaption() {
        return caption;
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
     * getter method for property children
     * 
     * @return the children
     */
    public List<MemberNodeTree> getChildren() {
        if (this.children == null) {
            this.children = new ArrayList<MemberNodeTree>();
        }
        return children;
    }

    /**
     * setter method for property children
     * 
     * @param children the children to set
     */
    public void setChildren(List<MemberNodeTree> children) {
        this.children = children;
    }

    /**
     * getter method for property parent
     * 
     * @return the parent
     */
    public MemberNodeTree getParent() {
        return parent;
    }

    /**
     * setter method for property parent
     * 
     * @param parent the parent to set
     */
    public void setParent(MemberNodeTree parent) {
        this.parent = parent;
    }

    /**
     * getter method for property ordinal
     * 
     * @return the ordinal
     */
    public String getOrdinal() {
        return ordinal;
    }

    /**
     * setter method for property ordinal
     * 
     * @param ordinal the ordinal to set
     */
    public void setOrdinal(String ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * get leafIds
     * 
     * @return the leafIds
     */
    public Set<String> getLeafIds() {
        if (this.leafIds == null) {
            this.leafIds = new HashSet<String>();
        }
        return leafIds;
    }

    /**
     * set leafIds with leafIds
     * 
     * @param leafIds the leafIds to set
     */
    public void setLeafIds(Set<String> leafIds) {
        this.leafIds = leafIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MemberNodeTree [name=" + name + ", uniqueName=" + uniqueName + ", caption=" + caption + ", children="
                + children + ", parent=" + (parent != null ? parent.getName() : null) + ", ordinal=" + ordinal
                + ", leafIds=" + leafIds + "]";
    }

    @Override
    public int compareTo(MemberNodeTree o) {

        return this.ordinal.compareTo(o.getOrdinal());
    }

    /**
     * get querySource
     * 
     * @return the querySource
     */
    public String getQuerySource() {
        return querySource;
    }

    /**
     * set querySource with querySource
     * 
     * @param querySource the querySource to set
     */
    public void setQuerySource(String querySource) {
        this.querySource = querySource;
    }

    /**
     * get hasChildren
     * @return the hasChildren
     */
    public boolean isHasChildren() {
        return hasChildren;
    }

    /**
     * set hasChildren with hasChildren
     * @param hasChildren the hasChildren to set
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    /**
     * @return the isSummary
     */
    public boolean isSummary() {
        return isSummary;
    }

    /**
     * @param isSummary the isSummary to set
     */
    public void setSummary(boolean isSummary) {
        this.isSummary = isSummary;
    }

    /**
     * 根据给定的uniqueName，找到该tree对应uniqueName所在的节点
     * @param uniqueName uniqueName
     * @return 返回指定uniqueName对应的MemberNodeTree节点
     */
    public MemberNodeTree getMemberNodeTreeByUniqueName(String uniqueName) {
        MemberNodeTree rs = null;
        if (StringUtils.isEmpty(uniqueName)) {
            return null;
        }
        if (uniqueName.equals(this.getUniqueName())) {
            return this;
        }
        if (CollectionUtils.isNotEmpty(this.getChildren())) {
            for (MemberNodeTree child : this.getChildren()) {
                rs = child.getMemberNodeTreeByUniqueName(uniqueName);
                if (rs == null) {
                    continue;
                }
                return rs;
            }
        } 
        return rs;
    }
    
    /**
     * 根据给定的name，找到该tree对应name所在的节点
     * 
     * @param name name
     * @return 返回指定name对应的MemberNodeTree节点
     */
    public MemberNodeTree getMemberNodeTreeByName(String name) {
        MemberNodeTree rs = null;
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        if (name.equals(this.getName())) {
            return this;
        }
        if (CollectionUtils.isNotEmpty(this.getChildren())) {
            for (MemberNodeTree child : this.getChildren()) {
                rs = child.getMemberNodeTreeByName(name);
                if (rs == null) {
                    continue;
                }
                return rs;
            }
        } 
        return rs;
    }
    
    /**
     * 将MemberNodeTree里的层级关系转换为list列表平铺模式返回
     * 
     * @param rs 要返回的结果集
     * @param memberNodeTree memberNodeTree
     * @return 返回MemberNodeTree里的层级关系转换为list的列表
     */
    public List<MemberNodeTree> transMemberNodeTreeToList(List<MemberNodeTree> rs, MemberNodeTree memberNodeTree) {
        if (rs == null) {
            rs = new ArrayList<MemberNodeTree>();
        }
        if (!StringUtils.isEmpty(memberNodeTree.getName())) {
            if (!rs.contains(memberNodeTree)) {
                rs.add(memberNodeTree);
            }
            for (int i = 0; i < memberNodeTree.getChildren().size(); i++) {
                if (memberNodeTree.getChildren() != null) {
                    MemberNodeTree child = memberNodeTree.getChildren().get(i);
                    if (child != null) {
                        if (!rs.contains(child)) {
                            rs.add(child);
                        }
                        if (child != null) {
                            transMemberNodeTreeToList(rs, child);
                        }
                    }
                }
            }
        } else {
            transMemberNodeTreeToList(rs, memberNodeTree.getChildren().get(0));
        }
        return rs;
    }
    
    /**
     * 根据给定的树节点，找到该节点下对应的所有最细粒度子节点集合
     * 
     * @param rs 要返回的结果集
     * @param memberNodeTree 要查找的tree节点
     * @return 返回该节点下对应的所有最细粒度子节点集合
     */
    public List<MemberNodeTree> getLastChildNodes(List<MemberNodeTree> rs, MemberNodeTree memberNodeTree) {
        if (rs == null) {
            rs = new ArrayList<MemberNodeTree>();
        }
        if (!CollectionUtils.isEmpty(memberNodeTree.getChildren())) {
            for (MemberNodeTree child : memberNodeTree.getChildren()) {
                if (CollectionUtils.isEmpty(child.getChildren())) {
                    if (!rs.contains(child)) {
                        rs.add(child);
                    }
                } else {
                    this.getLastChildNodes(rs, child);
                }
            }
        }
        return rs;
    }
    
    public void sort(SortType sortType) {
        if(CollectionUtils.isNotEmpty(this.children)) {
            if(sortType == SortType.DESC) {
                Collections.sort(this.children, (o1, o2) -> {
                		if(o1.getCaption().equals("百度")){
                			return 1;
                		} else if(o2.getCaption().equals("百度")) {
                			return -1;
                		}
                    return o2.getName().compareTo(o1.getName());
                });
            } else {
                Collections.sort(this.children, (o1, o2) -> {
                	if(o1.getCaption().equals("百度")){
            			return -1;
            		} else if(o2.getCaption().equals("百度")) {
            			return 1;
            		}
                    return o1.getName().compareTo(o2.getName());
                });
            }
            this.children.forEach(o -> {
               o.sort(sortType); 
            });
        }
    }

    /**
     * @return the summaryIds
     */
    public Set<String> getSummaryIds() {
        return summaryIds;
    }

    /**
     * @param summaryIds the summaryIds to set
     */
    public void setSummaryIds(Set<String> summaryIds) {
        this.summaryIds = summaryIds;
    }
    
    
}
