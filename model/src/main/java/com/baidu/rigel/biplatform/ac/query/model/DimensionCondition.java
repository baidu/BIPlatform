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
package com.baidu.rigel.biplatform.ac.query.model;

import java.util.ArrayList;
import java.util.List;

import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;

/**
 * 维度条件
 * 
 * @author xiaoming.chen
 *
 */
public class DimensionCondition implements MetaCondition {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2349532573193839217L;

    /**
     * queryDataNodes 查询的维度的节点信息
     */
    private List<QueryData> queryDataNodes;

    /**
     * exceptDataNodes 查询排除的节点列表
     */
    private List<QueryData> exceptDataNodes;

    /**
     * dimUniqueName 维度的UniqueName
     */
    private String metaName;

    /**
     * metaType 条件对应的类型
     */
    private MetaType metaType;
    
    
    /** 
     * memberSortType 维度维值的排序方式，默认升序，不支持NONE
     */
    private SortType memberSortType = SortType.ASC;

    /**
     * construct with dimUniqueName
     * 
     * @param dimUniqueName 维度的UniqueName
     */
    public DimensionCondition(String metaName) {
        this.metaName = metaName;
        this.metaType = MetaType.Dimension;
    }

    public DimensionCondition() {
    }

    /**
     * getter method for property queryDataNodes
     * 
     * @return the queryDataNodes
     */
    public List<QueryData> getQueryDataNodes() {
        if (this.queryDataNodes == null) {
            this.queryDataNodes = new ArrayList<QueryData>();
        }
        return queryDataNodes;
    }

    /**
     * setter method for property queryDataNodes
     * 
     * @param queryDataNodes the queryDataNodes to set
     */
    public void setQueryDataNodes(List<QueryData> queryDataNodes) {
        this.queryDataNodes = queryDataNodes;
    }

    /**
     * getter method for property exceptDataNodes
     * 
     * @return the exceptDataNodes
     */
    public List<QueryData> getExceptDataNodes() {
        return exceptDataNodes;
    }

    /**
     * setter method for property exceptDataNodes
     * 
     * @param exceptDataNodes the exceptDataNodes to set
     */
    public void setExceptDataNodes(List<QueryData> exceptDataNodes) {
        this.exceptDataNodes = exceptDataNodes;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DimensionCondition [queryDataNodes=" + queryDataNodes + ", exceptDataNodes=" + exceptDataNodes
                + ", metaName=" + metaName + "]";
    }

    @Override
    public String getMetaName() {
        return this.metaName;
    }

    /** 
     * 获取 memberSortType 
     * @return the memberSortType 
     */
    public SortType getMemberSortType() {
        return memberSortType;
    }

    /** 
     * 设置 memberSortType 
     * @param memberSortType the memberSortType to set 
     */
    public void setMemberSortType(SortType memberSortType) {
        this.memberSortType = memberSortType;
    }

    //
    // public static void main(String[] args) {
    // DimensionCondition condition = new DimensionCondition("dim_group");
    // QueryData queryData = new QueryData("[dim_group].[0]");
    // condition.getQueryDataNodes().add(queryData);
    //
    // System.out.println(AnswerCoreConstant.GSON.toJson(condition));
    // }

}
