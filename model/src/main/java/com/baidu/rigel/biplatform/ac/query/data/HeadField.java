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
package com.baidu.rigel.biplatform.ac.query.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.annotation.GsonIgnore;

/**
 * 类HeadField.java的实现描述：datamodel头部元素信息
 * 
 * @author xiaoming.chen 2013-12-5 下午12:01:58
 */
public class HeadField implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 4027435178812558748L;

    /**
     * 数据的KEY
     */
    private String value;

    /**
     * 数据显示的名称
     */
    private String caption;

    /**
     * nodeUniqueName
     */
    /**
     * nodeUniqueName 节点的UniqueName，用上层节点 拼上 本身的 value
     */
    private String nodeUniqueName;

    /**
     * 该数据的孩子节点，叶子节点不能根据这个判断
     */
    private List<HeadField> nodeList = new ArrayList<HeadField>();

    /**
     * 当前节点的子节点
     */
    private List<HeadField> children = new ArrayList<HeadField>();

    /**
     * 根据哪个值进行排序的,如果有汇总节点，第一个节点必须是汇总值
     */
    @GsonIgnore
    private transient List<BigDecimal> compareDatas = new ArrayList<BigDecimal>();

    /**
     * 额外信息
     */
    private Map<String, Object> extInfos;

    /**
     * 在排序的时候，计算当前节点的汇总值，排序完成以后不会保留
     */
    private transient BigDecimal summarizeData;

    /**
     * 当前节点的父节点(非同一个层级)
     */
    @GsonIgnore
    private HeadField parentLevelField;

    /**
     * 当前节点本层级的父节点
     */
    @GsonIgnore
    private HeadField parent;
    
    /**
     * hasChildren
     */
    private boolean hasChildren;

    /**
     * 构造方法
     * 
     * @param parentFiled 上一个层级的节点
     */
    public HeadField(HeadField parentLevelField) {
        super();
        this.parentLevelField = parentLevelField;
    }

    public HeadField() {
    }

    /**
     * constructor
     * 
     * @param parentLevelField 上一个层级的节点
     * @param parent 本层级的父节点
     */
    public HeadField(HeadField parentLevelField, HeadField parent) {
        this(parentLevelField);
        this.parent = parent;
    }

    /**
     * 返回该节点对应的数据信息（行或者列）
     * 
     * @return 行、列上的数据集合
     */
    public List<BigDecimal> getCompareDatas() {
        if (this.compareDatas == null) {
            this.compareDatas = new ArrayList<BigDecimal>();
        }
        return compareDatas;
    }

    /**
     * 设置节点对应的行或者列上的数据值
     * 
     * @param compareDatas 行、列上的数据
     */
    public void setCompareDatas(List<BigDecimal> compareDatas) {
        this.compareDatas = compareDatas;
    }

    /**
     * 返沪节点的额外信息
     * 
     * @return 额外信息的Map对象
     */
    public Map<String, Object> getExtInfos() {
        if (this.extInfos == null) {
            this.extInfos = new HashMap<String, Object>(1);
        }
        return extInfos;
    }

    /**
     * 设置节点的额外信息
     * 
     * @param extInfos 额外信息的Map对象
     */
    public void setExtInfos(Map<String, Object> extInfos) {
        this.extInfos = extInfos;
    }

    /**
     * 判断2个HeadFiled的值（包括子节点的值）是否相等
     * 
     * @param field 和当前节点比较的节点
     * @return 2个节点是否相等
     */
    public boolean fieldEquals(HeadField field) {
        if (this.value.equals(field.getValue())
                && (CollectionUtils.size(this.getNodeList()) == CollectionUtils.size(field.nodeList))) {
            if (CollectionUtils.isNotEmpty(this.getNodeList())) {
                for (int i = 0; i < this.getNodeList().size(); i++) {
                    if (!this.getNodeList().get(i).fieldEquals(field.nodeList.get(i))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 将2个节点的值和对应叶子节点的值相加
     * 
     * @param field 加到当前节点的节点
     */
    public void addCompareDatas(HeadField field) {
        if (this.fieldEquals(field)) {
            if (CollectionUtils.isNotEmpty(this.compareDatas) && CollectionUtils.isNotEmpty(field.getCompareDatas())) {
                this.compareDatas.addAll(field.getCompareDatas());
            } else if (CollectionUtils.isEmpty(this.compareDatas)
                    && CollectionUtils.isNotEmpty(field.getCompareDatas())) {
                this.setCompareDatas(field.getCompareDatas());
            }
            if (CollectionUtils.isNotEmpty(this.nodeList)) {
                for (int i = 0; i < this.nodeList.size(); i++) {
                    this.nodeList.get(i).addCompareDatas(field.getNodeList().get(i));
                }
            }
            if (CollectionUtils.isNotEmpty(this.children)) {
                for (int i = 0; i < this.children.size(); i++) {
                    this.children.get(i).addCompareDatas(field.getChildren().get(i));
                }
            }
        }
    }

    /**
     * 返回当前节点的叶子节点的数目
     * 
     * @return 叶子节点的总数
     */
    public int getLeafSize() {
        if (this.getNodeList().isEmpty()) {
            return 0;
        } else {
            int count = 0;
            for (HeadField node : this.getNodeList()) {
                count += node.getLeafFileds(true).size();
            }
            return count;
        }
    }

    /**
     * 返回当前节点的所有叶子节点
     * 
     * @return 叶子节点的集合
     */
    public List<HeadField> getLeafFileds(boolean containCurrentChildren) {
        List<HeadField> leafFileds = new ArrayList<HeadField>();
        if (getNodeList().isEmpty()) {
            leafFileds.add(this);
        } else {
            for (HeadField node : this.getNodeList()) {
                leafFileds.addAll(node.getLeafFileds(true));
            }
        }
        if (containCurrentChildren && !getChildren().isEmpty()) {
            for (HeadField child : this.getChildren()) {
                leafFileds.addAll(child.getLeafFileds(true));
            }
        }
        return leafFileds;
    }

    @Override
    public String toString() {
        String obj =
                "HeadFiled [value=" + value + ", caption=" + caption + ", nodeList=" + nodeList + ", extInfos="
                        + extInfos + ", summarizeData=" + summarizeData + "]";

        return obj;
    }

    /**
     * 简单克隆HeadFiled，只保留有限的几个属性，慎用
     * 
     * @return 返回简单克隆后的HeadFiled对象
     */
    public HeadField simpleClone() {
        HeadField cloneBean = new HeadField(this.getParentLevelField());
        cloneBean.setCaption(this.caption);
        // cloneBean.setCompareDatas(this.compareDatas);
        // cloneBean.setExtInfos(this.extInfos);
        // cloneBean.setNodeList(this.nodeList);
        cloneBean.setValue(this.value);

        return cloneBean;
    }

    /**
     * default generate get summarizeData
     * 
     * @return the summarizeData
     */
    public BigDecimal getSummarizeData() {
        return summarizeData;
    }

    /**
     * default generate summarizeData param set method
     * 
     * @param summarizeData the summarizeData to set
     */
    public void setSummarizeData(BigDecimal summarizeData) {
        this.summarizeData = summarizeData;
    }

    /**
     * default generate get value
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * default generate value param set method
     * 
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * default generate get caption
     * 
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * default generate caption param set method
     * 
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * default generate get nodeList
     * 
     * @return the nodeList
     */
    public List<HeadField> getNodeList() {
        if (this.nodeList == null) {
            this.nodeList = new ArrayList<HeadField>();
        }
        return nodeList;
    }

    /**
     * default generate nodeList param set method
     * 
     * @param nodeList the nodeList to set
     */
    public void setNodeList(List<HeadField> nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * default generate get children
     * 
     * @return the children
     */
    public List<HeadField> getChildren() {
        if (this.children == null) {
            this.children = new ArrayList<HeadField>();
        }
        return children;
    }

    /**
     * default generate children param set method
     * 
     * @param children the children to set
     */
    public void setChildren(List<HeadField> children) {
        this.children = children;
    }

    /**
     * default generate get parent
     * 
     * @return the parent
     */
    public HeadField getParent() {
        return parent;
    }

    /**
     * default generate parent param set method
     * 
     * @param parent the parent to set
     */
    public void setParent(HeadField parent) {
        this.parent = parent;
    }

    /**
     * get parentLevelField
     * 
     * @return the parentLevelField
     */
    public HeadField getParentLevelField() {
        return parentLevelField;
    }

    /**
     * set parentLevelField with parentLevelField
     * 
     * @param parentLevelField the parentLevelField to set
     */
    public void setParentLevelField(HeadField parentLevelField) {
        this.parentLevelField = parentLevelField;
    }

    /**
     * get nodeUniqueName
     * 
     * @return the nodeUniqueName
     */
    public String getNodeUniqueName() {
        if (StringUtils.isBlank(nodeUniqueName)) {
            String uniqueName = "{" + this.getValue() + "}";
            this.nodeUniqueName = uniqueName;
            if (this.parentLevelField != null) {
                this.nodeUniqueName = parentLevelField.getNodeUniqueName() + "." + uniqueName;
            }
            return uniqueName;
        }
        return nodeUniqueName;
    }

    /**
     * set nodeUniqueName with nodeUniqueName
     * 
     * @param nodeUniqueName the nodeUniqueName to set
     */
    public void setNodeUniqueName(String nodeUniqueName) {
        this.nodeUniqueName = nodeUniqueName;
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((caption == null) ? 0 : caption.hashCode());
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((extInfos == null) ? 0 : extInfos.hashCode());
        result = prime * result + (hasChildren ? 1231 : 1237);
        result = prime * result + ((nodeList == null) ? 0 : nodeList.hashCode());
        result = prime * result + ((nodeUniqueName == null) ? 0 : nodeUniqueName.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((parentLevelField == null) ? 0 : parentLevelField.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
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
        if (!(obj instanceof HeadField)) {
            return false;
        }
        HeadField other = (HeadField) obj;
        if (caption == null) {
            if (other.caption != null) {
                return false;
            }
        } else if (!caption.equals(other.caption)) {
            return false;
        }
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (extInfos == null) {
            if (other.extInfos != null) {
                return false;
            }
        } else if (!extInfos.equals(other.extInfos)) {
            return false;
        }
        if (hasChildren != other.hasChildren) {
            return false;
        }
        if (nodeList == null) {
            if (other.nodeList != null) {
                return false;
            }
        } else if (!nodeList.equals(other.nodeList)) {
            return false;
        }
        if (nodeUniqueName == null) {
            if (other.nodeUniqueName != null) {
                return false;
            }
        } else if (!nodeUniqueName.equals(other.nodeUniqueName)) {
            return false;
        }
//        if (parent == null) {
//            if (other.parent != null) {
//                return false;
//            }
//        } else if (!parent.equals(other.parent)) {
//            return false;
//        }
        if (parentLevelField == null) {
            if (other.parentLevelField != null) {
                return false;
            }
        } else if (!parentLevelField.equals(other.parentLevelField)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    
}
