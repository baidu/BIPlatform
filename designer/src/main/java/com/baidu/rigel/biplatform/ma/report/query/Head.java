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
package com.baidu.rigel.biplatform.ma.report.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;

/**
 * 类HeadFiled.java的实现描述：datamodel头部元素信息
 * 
 * @author xiaoming.chen 2013-12-5 下午12:01:58
 */
public class Head implements Serializable {
    
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
     * 该数据的孩子节点，叶子节点不能根据这个判断
     */
    private List<Head> nodeList = new ArrayList<Head>();
    
    /**
     * 当前节点的子节点
     */
    private List<Head> children = Lists.newArrayList();
    
    /**
     * 根据哪个值进行排序的,如果有汇总节点，第一个节点必须是汇总值
     */
    private transient List<Cell> compareDatas = Lists.newArrayList();
    
    /**
     * 是否是汇总节点，在顺序排序完成以后，需要获取最后一个节点进行判断，如果是汇总节点则需要挪动到该层次的第一个
     */
    private boolean isAllNode;
    
    /**
     * 额外信息
     */
    private Map<String, Object> extInfos;
    
    /**
     * 在排序的时候，计算当前节点的汇总值，排序完成以后不会保留
     */
    private transient BigDecimal summarizeData;
    
    /**
     * 当前节点的父节点
     */
    private Head parentFiled;
    
    /**
     * 当前节点本层级的父节点
     */
    private Head parent;
    
    /**
     * 构造方法
     * 
     * @param parentFiled
     *            父节点
     */
    public Head(Head parentFiled) {
        super();
        this.parentFiled = parentFiled;
    }
    
    /**
     * 返回该节点对应的数据信息（行或者列）
     * 
     * @return 行、列上的数据集合
     */
    public List<Cell> getCompareDatas() {
        if (this.compareDatas == null) {
            this.compareDatas = Lists.newArrayList();
        }
        return compareDatas;
    }
    
    /**
     * 设置节点对应的行或者列上的数据值
     * 
     * @param compareDatas
     *            行、列上的数据
     */
    public void setCompareDatas(List<Cell> compareDatas) {
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
     * @param extInfos
     *            额外信息的Map对象
     */
    public void setExtInfos(Map<String, Object> extInfos) {
        this.extInfos = extInfos;
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
            for (Head node : this.getNodeList()) {
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
    public List<Head> getLeafFileds(boolean containCurrentChildren) {
        List<Head> leafFileds = Lists.newArrayList();
        if (getNodeList().isEmpty()) {
            leafFileds.add(this);
        } else {
            for (Head node : this.getNodeList()) {
                leafFileds.addAll(node.getLeafFileds(true));
            }
        }
        if (containCurrentChildren && !getChildren().isEmpty()) {
            for (Head child : this.getChildren()) {
                leafFileds.addAll(child.getLeafFileds(true));
            }
        }
        return leafFileds;
    }
    
    @Override
    public String toString() {
        String obj = "HeadFiled [value=" + value + ", caption=" + caption + ", nodeList="
                + nodeList + ", isAllNode=" + isAllNode + ", extInfos=" + extInfos + ", summarizeData="
                + summarizeData + "]";
        
        return obj;
    }
    
    /**
     * 简单克隆HeadFiled，只保留有限的几个属性，慎用
     * 
     * @return 返回简单克隆后的HeadFiled对象
     */
    public Head simpleClone() {
        Head cloneBean = new Head(this.getParentFiled());
        cloneBean.setAllNode(this.isAllNode);
        cloneBean.setCaption(this.caption);
        // cloneBean.setCompareDatas(this.compareDatas);
        // cloneBean.setExtInfos(this.extInfos);
        // cloneBean.setNodeList(this.nodeList);
        cloneBean.setValue(this.value);
        
        return cloneBean;
    }
    
    /**
     * default generate get isAllNode
     * 
     * @return the isAllNode
     */
    public boolean isAllNode() {
        return isAllNode;
    }
    
    /**
     * default generate isAllNode param set method
     * 
     * @param isAllNode
     *            the isAllNode to set
     */
    public void setAllNode(boolean isAllNode) {
        this.isAllNode = isAllNode;
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
     * @param summarizeData
     *            the summarizeData to set
     */
    public void setSummarizeData(BigDecimal summarizeData) {
        this.summarizeData = summarizeData;
    }
    
    /**
     * default generate get parentFiled
     * 
     * @return the parentFiled
     */
    public Head getParentFiled() {
        return parentFiled;
    }
    
    /**
     * default generate parentFiled param set method
     * 
     * @param parentFiled
     *            the parentFiled to set
     */
    public void setParentFiled(Head parentFiled) {
        this.parentFiled = parentFiled;
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
     * @param value
     *            the value to set
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
     * @param caption
     *            the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * default generate get nodeList
     * 
     * @return the nodeList
     */
    public List<Head> getNodeList() {
        if (this.nodeList == null) {
            this.nodeList = Lists.newArrayList();
        }
        return nodeList;
    }
    
    /**
     * default generate nodeList param set method
     * 
     * @param nodeList
     *            the nodeList to set
     */
    public void setNodeList(List<Head> nodeList) {
        this.nodeList = nodeList;
    }
    
    /**
     * default generate get children
     * 
     * @return the children
     */
    public List<Head> getChildren() {
        if (this.children == null) {
            this.children = Lists.newArrayList();
        }
        return children;
    }
    
    /**
     * default generate children param set method
     * 
     * @param children
     *            the children to set
     */
    public void setChildren(List<Head> children) {
        this.children = children;
    }
    
    /**
     * default generate get parent
     * 
     * @return the parent
     */
    public Head getParent() {
        return parent;
    }
    
    /**
     * default generate parent param set method
     * 
     * @param parent
     *            the parent to set
     */
    public void setParent(Head parent) {
        this.parent = parent;
    }
    
}
